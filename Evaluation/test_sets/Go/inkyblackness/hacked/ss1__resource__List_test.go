package resource_test

import (
	"testing"

	"github.com/inkyblackness/hacked/ss1/resource"

	"github.com/stretchr/testify/assert"
)

func someResource(key byte) resource.View {
	return resource.Resource{
		Blocks: resource.BlocksFrom([][]byte{{key}}),
	}
}

func TestListWith(t *testing.T) {
	t.Run("nil list", func(t *testing.T) {
		var base resource.List
		result := base.With(someResource(0x01))
		assert.Equal(t, resource.List([]resource.View{someResource(0x01)}), result)
	})

	t.Run("append", func(t *testing.T) {
		base := resource.List([]resource.View{someResource(0x01)})
		result := base.With(someResource(0x03))
		assert.Equal(t, resource.List([]resource.View{someResource(0x01), someResource(0x03)}), result)
	})

	t.Run("immutable source", func(t *testing.T) {
		raw := make([]resource.View, 1, 2)
		raw[0] = someResource(0x02)
		base := resource.List(raw)
		result := base.With(someResource(0x04))
		assert.Equal(t, 2, len(result))
		assert.Equal(t, resource.List([]resource.View{someResource(0x02)}), base)
	})
}

func TestListJoined(t *testing.T) {
	var empty resource.List
	t.Run("two lists", func(t *testing.T) {
		base := empty.With(someResource(0x01)).With(someResource(0x02))
		other := empty.With(someResource(0x03)).With(someResource(0x04))
		result := base.Joined(other)
		assert.Equal(t, resource.List([]resource.View{someResource(0x01), someResource(0x02), someResource(0x03), someResource(0x04)}), result)
	})
	t.Run("nil with nil is nil", func(t *testing.T) {
		result := empty.Joined(empty)
		assert.Nil(t, result)
	})
}
