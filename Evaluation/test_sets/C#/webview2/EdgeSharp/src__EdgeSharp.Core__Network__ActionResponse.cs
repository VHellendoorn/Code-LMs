// Copyright (c) 2021 The EdgeSharp Authors. All rights reserved.
// Use of this source code is governed by MIT license that can be found in the LICENSE file.

using EdgeSharp.Core.Infrastructure;
using System.Collections.Generic;
using System.Net;

namespace EdgeSharp.Core.Network
{
    /// <summary>
    /// The EdgeLite response.
    /// </summary>
    public class ActionResponse : IActionResponse
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ActionResponse"/> class.
        /// </summary>
        /// <param name="requestId">
        /// The request id.
        /// </param>
        public ActionResponse()
        {
            Headers = new Dictionary<string, string[]>
            {
                { ResponseConstants.Header_AccessControlAllowOrigin,      new string[] { "*" } },
                { ResponseConstants.Header_CacheControl,                  new string[] { "private" } },
                { ResponseConstants.Header_AccessControlAllowMethods,     new string[] { "*" } },
                { ResponseConstants.Header_AccessControlAllowHeaders,     new string[] { ResponseConstants.Header_ContentType } },
                { ResponseConstants.Header_ContentType,                   new string[] { ResponseConstants.Header_ContentTypeValue } }
            };

            StatusCode = HttpStatusCode.OK;
            ReasonPhrase = ResponseConstants.StatusOKText;
        }

        public HttpStatusCode StatusCode { get; set; }
        public string ReasonPhrase { get; set; }
        public object Content { get; set; }
        public bool HasRouteResponse { get; set; }
        public IDictionary<string, string[]> Headers { get; }
    }
}
