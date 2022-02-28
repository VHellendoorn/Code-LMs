package object_test

import (
	"fmt"
	"testing"

	"github.com/inkyblackness/hacked/ss1/content/object"

	"github.com/stretchr/testify/assert"
)

func TestRenderTypes(t *testing.T) {
	assert.Equal(t, 13, len(object.RenderTypes()))
}

func TestRenderTypeString(t *testing.T) {
	tt := []struct {
		renderType object.RenderType
		expected   string
	}{
		{renderType: object.RenderTypeUnknown, expected: "Unknown 0x00"},
		{renderType: object.RenderTypeTextPoly, expected: "TextPoly"},
		{renderType: object.RenderTypeBitmap, expected: "Bitmap"},
		{renderType: object.RenderTypeTPoly, expected: "TPoly"},
		{renderType: object.RenderTypeCritter, expected: "Critter"},
		{renderType: object.RenderTypeAnimPoly, expected: "AnimPoly"},
		{renderType: object.RenderTypeVox, expected: "Vox"},
		{renderType: object.RenderTypeNoObject, expected: "NoObject"},
		{renderType: object.RenderTypeTexBitmap, expected: "TexBitmap"},
		{renderType: object.RenderTypeFlatPoly, expected: "FlatPoly"},
		{renderType: object.RenderTypeMultiView, expected: "MultiView"},
		{renderType: object.RenderTypeSpecial, expected: "Special"},
		{renderType: object.RenderTypeTLPoly, expected: "TLPoly"},

		{renderType: object.RenderType(255), expected: "Unknown 0xFF"},
	}

	for _, tc := range tt {
		result := tc.renderType.String()
		assert.Equal(t, tc.expected, result, fmt.Sprintf("Failed for 0x%02X", int(tc.renderType)))
	}
}
