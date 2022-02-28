package net.neoremind.dynamicproxy.sample;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author zhangxu
 */
public class Sound {

    private int volume;

    public Sound(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Sound sound = (Sound) o;

        return volume == sound.volume;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.volume)
                .hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this.volume).toString();
    }
}
