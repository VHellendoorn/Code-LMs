using System;
using ServiceStack.Text;

namespace NMoneys.Serialization.Tests.Service_Stack.Support
{
	public class ConfigScope : IDisposable
	{
		private readonly JsConfigScope _scope;

		public ConfigScope(Func<Money, string> serializer, JsConfigScope scope = null)
		{
			_scope = scope;
			JsConfig<Money>.RawSerializeFn = serializer;
		}

		public ConfigScope(Func<string, Money> deSerializer, JsConfigScope scope = null)
		{
			_scope = scope;
			JsConfig<Money>.RawDeserializeFn = deSerializer;
		}

		public void Dispose()
		{
			if (_scope != null) _scope.Dispose();
			JsConfig<Money>.Reset();
		}
	}
}