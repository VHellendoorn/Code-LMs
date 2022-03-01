/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.audio.code;

import java.lang.reflect.Field;
import org.praxislive.audio.AudioPort;
import org.praxislive.audio.code.userapi.AudioIn;
import org.praxislive.code.CodeConnector;
import org.praxislive.code.CodeContext;
import org.praxislive.code.PortDescriptor;
import org.praxislive.code.userapi.AuxIn;
import org.praxislive.code.userapi.In;
import org.praxislive.core.Port;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.logging.LogLevel;
import org.jaudiolibs.pipes.Buffer;
import org.praxislive.audio.DefaultAudioInputPort;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class AudioInPort extends DefaultAudioInputPort {

    private final AudioInPipe in;

    private AudioInPort(AudioInPipe in) {
        super(in);
        this.in = in;
    }
    
    AudioInPipe getPipe() {
        return in;
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            In ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.In, ann.value(), field);
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            AuxIn ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.AuxIn, ann.value(), field);
    }

    private static Descriptor createDescriptor(CodeConnector<?> connector,
            PortDescriptor.Category category, int index, Field field) {
        if (!AudioIn.class.isAssignableFrom(field.getType())) {
            return null;
        }
        String id = connector.findID(field);
        return new Descriptor(id, category, index, field);
    }

    static class AudioInPipe extends AudioIn {

        @Override
        protected void process(Buffer buffer, boolean rendering) {

        }

    }

    static class Descriptor extends PortDescriptor {

        private final static PortInfo INFO = PortInfo.create(AudioPort.class, PortInfo.Direction.IN, PMap.EMPTY);

        private final Field field;
        private AudioInPort port;

        private Descriptor(String id,
                PortDescriptor.Category category,
                int index,
                Field field) {
            super(id, category, index);
            field.setAccessible(true);
            this.field = field;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof AudioInPort) {
                port = (AudioInPort) previous;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new AudioInPort(new AudioInPipe());
            }
            try {
                field.set(context.getDelegate(), port.in);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public AudioInPort getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return INFO;
        }

    }

}
