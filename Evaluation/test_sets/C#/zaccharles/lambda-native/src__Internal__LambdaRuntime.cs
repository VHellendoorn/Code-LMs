using System;
using System.Collections;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using LitJson;

namespace LambdaNative.Internal
{
    internal class LambdaRuntime : ILambdaRuntime
    {
        private readonly IDictionary _initialEnvironmentVariables;
        private readonly IDateTime _dateTime;
        private readonly HttpClient _http;
        private readonly JsonWriter _jsonWriter;

        public IEnvironment Environment { get; }

        public bool KeepInvokeLoopRunning()
        {
            return true;
        }

        public LambdaRuntime(IEnvironment environment, IDateTime dateTime, HttpClient http)
        {
            Environment = environment;
            _initialEnvironmentVariables = environment.GetEnvironmentVariables();

            var endpoint = _initialEnvironmentVariables["AWS_LAMBDA_RUNTIME_API"] as string;
            http.BaseAddress = new Uri($"http://{endpoint}/2018-06-01/runtime/");

            _dateTime = dateTime;
            _http = http;
            _jsonWriter = new JsonWriter { LowerCaseProperties = true, PrettyPrint = true };
        }

        public InvokeData GetNextInvocation()
        {
            this.LogDebug("Getting next invocation");

            using (var response = _http.GetAsync("invocation/next").GetAwaiter().GetResult())
            {
                this.LogDebug($"Response status code is {(int)response.StatusCode}");

                if (response.StatusCode != HttpStatusCode.OK)
                {
                    throw new Exception($"Failed to get invocation from runtime API. Status code: {(int)response.StatusCode}");
                }

                this.LogDebug("Reading response headers");

                var requestId = GetHeaderFirstValueOrNull(response.Headers, "Lambda-Runtime-Aws-Request-Id");
                var xAmznTraceId = GetHeaderFirstValueOrNull(response.Headers, "Lambda-Runtime-Trace-Id");
                var invokedFunctionArn = GetHeaderFirstValueOrNull(response.Headers, "Lambda-Runtime-Invoked-Function-Arn");

                var deadlineMs = GetHeaderFirstValueOrNull(response.Headers, "Lambda-Runtime-Deadline-Ms");
                long.TryParse(deadlineMs, out var deadlineMsLong);
                var deadlineDate = DateTimeOffset.FromUnixTimeMilliseconds(deadlineMsLong);

                this.LogDebug("Reading input stream");

                var responseStream = response.Content.ReadAsStreamAsync().GetAwaiter().GetResult();
                var inputStream = new MemoryStream();
                responseStream.CopyTo(inputStream);
                inputStream.Position = 0;

                this.LogDebug($"Input stream contains {inputStream.Length} bytes");

                var context = new LambdaContext(_initialEnvironmentVariables)
                {
                    AwsRequestId = requestId,
                    InvokedFunctionArn = invokedFunctionArn,
                    Logger = LambdaLogger.Instance,
                    RemainingTimeFunc = () => deadlineDate.Subtract(_dateTime.OffsetUtcNow)
                };

                var invokeData = new InvokeData
                {
                    LambdaContext = context,
                    InputStream = inputStream,
                    XAmznTraceId = xAmznTraceId,
                    RequestId = requestId
                };

                this.LogDebug($"Got request {requestId}");

                return invokeData;
            }
        }

        public void ReportInitializationError(Exception exception)
        {
            this.LogDebug($"Creating ExceptionResponse from {exception?.GetType().Name} exception");

            var json = ToJson(ExceptionResponse.Create(exception));

            this.LogDebug("Reporting invocation error");

            using (var response = _http.PostAsync("init/error",
                new StringContent(json)).GetAwaiter().GetResult())
            {
                this.LogDebug($"Response status code is {(int)response.StatusCode}");

                if (response.StatusCode != HttpStatusCode.Accepted)
                {
                    Console.WriteLine("Failed to report initialization error");
                }
            }
        }

        public void ReportInvocationSuccess(string requestId, Stream outputStream)
        {
            this.LogDebug("Reporting invocation success");

            using (var response = _http.PostAsync($"invocation/{requestId}/response",
                new StreamContent(outputStream)).GetAwaiter().GetResult())
            {
                this.LogDebug($"Response status code is {(int)response.StatusCode}");

                if (response.StatusCode != HttpStatusCode.Accepted)
                {
                    Console.WriteLine($"Failed to report success for request {requestId}");
                }
            }
        }

        public void ReportInvocationError(string requestId, Exception exception)
        {
            this.LogDebug($"Creating ExceptionResponse from {exception?.GetType().Name} exception");

            var json = ToJson(ExceptionResponse.Create(exception));

            this.LogDebug("Reporting invocation error");

            using (var response = _http.PostAsync($"invocation/{requestId}/error",
                new StringContent(json)).GetAwaiter().GetResult())
            {
                this.LogDebug($"Response status code is {(int)response.StatusCode}");

                if (response.StatusCode != HttpStatusCode.Accepted)
                {
                    Console.WriteLine($"Failed to report error for request {requestId}");
                }
            }
        }

        private string GetHeaderFirstValueOrNull(HttpHeaders headers, string key)
        {
            if (headers == null || !headers.TryGetValues(key, out var values))
            {
                this.LogDebug($"Header {key} is null");
                return null;
            }

            var value = values.FirstOrDefault();
            this.LogDebug($"Header {key} is '{value}'");

            return value;
        }

        private string ToJson(object obj)
        {
            this.LogDebug($"Converting {obj?.GetType().Name} to JSON");

            _jsonWriter.Reset();
            JsonMapper.ToJson(obj, _jsonWriter);
            return _jsonWriter.ToString();
        }
    }
}