package movie

import (
	"bytes"

	"github.com/inkyblackness/hacked/ss1/content/audio"
	"github.com/inkyblackness/hacked/ss1/content/text"
)

// ContainSoundData packs a sound data into a container and encodes it.
func ContainSoundData(soundData audio.L8) []byte {
	var container Container
	container.Audio.Sound = soundData

	buffer := bytes.NewBuffer(nil)
	_ = Write(buffer, container, text.DefaultCodepage())
	return buffer.Bytes()
}
