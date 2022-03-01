package objects

import (
	. "github.com/zubairhamed/betwixt"
)

type AccessControlObject struct {
	Model ObjectDefinition
}

func (o *AccessControlObject) OnExecute(instanceId int, resourceId int, req Lwm2mRequest) Lwm2mResponse {
	return Unauthorized()
}

func (o *AccessControlObject) OnCreate(instanceId int, resourceId int, req Lwm2mRequest) Lwm2mResponse {
	return Unauthorized()
}

func (o *AccessControlObject) OnDelete(instanceId int, req Lwm2mRequest) Lwm2mResponse {
	return Unauthorized()
}

func (o *AccessControlObject) OnRead(instanceId int, resourceId int, req Lwm2mRequest) Lwm2mResponse {
	if resourceId == -1 {
		// Read Object Instance
	} else {
		// Read Resource Instance
		var val Value

		// resource := o.Model.GetResource(resourceId)
		switch instanceId {
		case 0:
			switch resourceId {
			case 0:
				val = Integer(1)
				break

			case 1:
				val = Integer(0)
				break

			case 2:
				// "/0/2/101", []byte{0, 15}
				break

			case 3:
				break
			}
			break

		case 1:
			switch resourceId {
			case 0:
				break

			case 1:
				break

			case 2:
				break

			case 3:
				break
			}
			break

		case 2:
			switch resourceId {
			case 0:
				break

			case 1:
				break

			case 2:
				break

			case 3:
				break
			}
			break

		case 3:
			switch resourceId {
			case 0:
				break

			case 1:
				break

			case 2:
				break

			case 3:
				break
			}
			break

		case 4:
			switch resourceId {
			case 0:
				break

			case 1:
				break

			case 2:
				break

			case 3:
				break
			}
			break
		}
		if val == nil {
			return NotFound()
		} else {
			return Content(val)
		}
	}
	return NotFound()
}

func (o *AccessControlObject) OnWrite(instanceId int, resourceId int, req Lwm2mRequest) Lwm2mResponse {
	return Unauthorized()
}

/*
	data.Put("/0/0", 1)
	data.Put("/0/1", 0)
	data.Put("/0/2/101", []byte{0, 15})
	data.Put("/0/3", 101)

	data.Put("1/0", 1)
	data.Put("1/1", 1)
	data.Put("1/2/102", []byte{0, 15})
	data.Put("1/3", 102)

	data.Put("2/0", 3)
	data.Put("2/1", 0)
	data.Put("2/2/101", []byte{0, 15})
	data.Put("2/2/102", []byte{0, 1})
	data.Put("2/3", 101)

	data.Put("3/0", 4)
	data.Put("3/1", 0)
	data.Put("3/2/101", []byte{0, 1})
	data.Put("3/2/0", []byte{0, 1})
	data.Put("3/3", 101)

	data.Put("4/0", 5)
	data.Put("4/1", 65535)
	data.Put("4/2/101", []byte{0, 16})
	data.Put("4/3", 65535)
*/

func NewExampleAccessControlObject(reg Registry) *AccessControlObject {
	return &AccessControlObject{
		Model: reg.GetDefinition(OMA_OBJECT_LWM2M_SECURITY),
	}
}
