package bitmap_test

import (
	"bytes"
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/inkyblackness/hacked/ss1/content/bitmap"
	"github.com/inkyblackness/hacked/ss1/resource"
)

func TestReadAnimationErrorsLength(t *testing.T) {
	for i := 0; i < 20; i++ {
		data := make([]byte, i)
		_, err := bitmap.ReadAnimation(bytes.NewReader(data))
		assert.Error(t, err, fmt.Sprintf("Error expected for %d bytes", i))
	}
}

func TestReadAnimationErrorsTags(t *testing.T) {
	tt := []struct {
		info string
		data []byte
	}{
		{
			info: "end tag data missing",
			data: []byte{
				0x00, 0x00,
				0x02, 0x02,
				0x04, 0x04,
				0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
				0x0C, 0x0C,
				0x0C,
			},
		},
		{
			info: "end tag data wrong",
			data: []byte{
				0x00, 0x00,
				0x02, 0x02,
				0x04, 0x04,
				0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
				0x0C, 0x0C,
				0x0C, 0x00,
			},
		},
		{
			info: "unknown tag",
			data: []byte{
				0x00, 0x00,
				0x02, 0x02,
				0x04, 0x04,
				0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
				0x0C, 0x0C,
				0x0B,
			},
		},
	}
	for _, tc := range tt {
		_, err := bitmap.ReadAnimation(bytes.NewReader(tc.data))
		assert.Error(t, err, fmt.Sprintf("Error expected for %s", tc.info))
	}
}

func TestReadWriteAnimationEntries(t *testing.T) {
	tt := []struct {
		info     string
		data     []byte
		expected bitmap.Animation
	}{
		{
			info: "empty list of entries",
			data: []byte{
				0x12, 0x34,
				0x56, 0x78,
				0xAA, 0xBB,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0xCC, 0xDD,
				0x0C, 0x01,
			},
			expected: bitmap.Animation{
				Width:      0x3412,
				Height:     0x7856,
				ResourceID: resource.ID(0xBBAA),
				IntroFlag:  0xDDCC,
				Entries:    nil,
			},
		},

		{
			info: "two entries",
			data: []byte{
				0x12, 0x34,
				0x56, 0x78,
				0xAA, 0xBB,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0xCC, 0xDD,
				0x04, 0x00, 0x0A, 0x10, 0x20,
				0x04, 0x0B, 0x10, 0x30, 0x40,
				0x0C, 0x01,
			},
			expected: bitmap.Animation{
				Width:      0x3412,
				Height:     0x7856,
				ResourceID: resource.ID(0xBBAA),
				IntroFlag:  0xDDCC,
				Entries: []bitmap.AnimationEntry{
					{FirstFrame: 0x00, LastFrame: 0x0A, FrameTime: 0x2010},
					{FirstFrame: 0x0B, LastFrame: 0x10, FrameTime: 0x4030},
				},
			},
		},
	}
	for _, tc := range tt {
		td := tc
		t.Run(tc.info, func(t *testing.T) {
			anim, err := bitmap.ReadAnimation(bytes.NewReader(td.data))
			require.Nil(t, err, "No error expected")
			assert.Equal(t, td.expected, anim, "Animation data not as expected after reading")

			output := bytes.NewBuffer(nil)
			err = bitmap.WriteAnimation(output, anim)
			require.Nil(t, err, "No error expected")
			assert.Equal(t, td.data, output.Bytes(), "Data mismatch after re-encoding")
		})
	}
}
