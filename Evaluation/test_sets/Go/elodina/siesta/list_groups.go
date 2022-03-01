/* Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

package siesta

type ListGroupsRequest struct{}

// Key returns the Kafka API key for ListGroupsRequest.
func (*ListGroupsRequest) Key() int16 {
	return 16
}

// Version returns the Kafka request version for backwards compatibility.
func (*ListGroupsRequest) Version() int16 {
	return 0
}

func (*ListGroupsRequest) Write(encoder Encoder) {}

type ListGroupsResponse struct {
	Error  error
	Groups map[string]string
}

func (lgr *ListGroupsResponse) Read(decoder Decoder) *DecodingError {
	errCode, err := decoder.GetInt16()
	if err != nil {
		return NewDecodingError(err, reasonInvalidListGroupsResponseErrorCode)
	}
	lgr.Error = BrokerErrors[errCode]

	groupsLen, err := decoder.GetInt32()
	if err != nil {
		return NewDecodingError(err, reasonInvalidListGroupsResponseGroupsLength)
	}

	lgr.Groups = make(map[string]string, groupsLen)
	for i := int32(0); i < groupsLen; i++ {
		groupID, err := decoder.GetString()
		if err != nil {
			return NewDecodingError(err, reasonInvalidListGroupsResponseGroupID)
		}

		protocolType, err := decoder.GetString()
		if err != nil {
			return NewDecodingError(err, reasonInvalidListGroupsResponseProtocolType)
		}
		lgr.Groups[groupID] = protocolType
	}

	return nil
}

const (
	reasonInvalidListGroupsResponseErrorCode    = "Invalid error code in ListGroupsResponse"
	reasonInvalidListGroupsResponseGroupsLength = "Invalid groups length in ListGroupsResponse"
	reasonInvalidListGroupsResponseGroupID      = "Invalid group id in ListGroupsResponse"
	reasonInvalidListGroupsResponseProtocolType = "Invalid protocol type in ListGroupsResponse"
)
