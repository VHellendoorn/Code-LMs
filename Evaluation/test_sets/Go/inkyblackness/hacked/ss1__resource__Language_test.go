package resource_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/inkyblackness/hacked/ss1/resource"
)

func TestLanguageIncludes(t *testing.T) {
	tt := []struct {
		primary   resource.Language
		secondary resource.Language
		expected  bool
	}{
		{resource.LangAny, resource.LangAny, true},
		{resource.LangAny, resource.LangGerman, true},
		{resource.LangFrench, resource.LangAny, false},
		{resource.LangGerman, resource.LangFrench, false},
		{resource.LangFrench, resource.LangFrench, true},
	}

	for _, tc := range tt {
		td := tc
		t.Run(fmt.Sprintf("Expecting %v including %v should be %v", td.primary, td.secondary, td.expected), func(t *testing.T) {
			result := td.primary.Includes(td.secondary)
			assert.Equal(t, td.expected, result)
		})
	}
}

func TestLanguageString(t *testing.T) {
	tt := []struct {
		lang     resource.Language
		expected string
	}{
		{resource.LangAny, "Any"},
		{resource.LangDefault, "Default"},
		{resource.LangFrench, "French"},
		{resource.LangGerman, "German"},
		{resource.Language(0x40), "Unknown40"},
	}

	for _, tc := range tt {
		td := tc
		t.Run(td.expected, func(t *testing.T) {
			result := td.lang.String()
			assert.Equal(t, td.expected, result)
		})
	}
}

func TestLanguages(t *testing.T) {
	result := resource.Languages()
	assert.Equal(t, 3, len(result))
}
