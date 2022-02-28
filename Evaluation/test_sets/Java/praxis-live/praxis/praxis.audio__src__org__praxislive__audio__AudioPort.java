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
 */
package org.praxislive.audio;

import java.util.stream.Stream;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PMap;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith
 */
public abstract class AudioPort implements Port {

    public static abstract class Input extends AudioPort {
        
        private PortInfo info;

        public Input() {
            this(PMap.EMPTY);
        }

        public Input(PMap properties) {
            info = PortInfo.create(AudioPort.class, PortInfo.Direction.IN, properties);
        }

        public final void connect(Port port) throws PortConnectionException {
            if (port instanceof Output) {
                port.connect(this);
            } else {
                throw new PortConnectionException();
            } 
        }

        public final void disconnect(Port port) {
            if (port instanceof Output) {
                port.disconnect(this);
            }
        }

        public final PortInfo getInfo() {
            return info;
        }

        protected abstract void addAudioOutputPort(Output port, Pipe source) throws PortConnectionException;

        protected abstract void removeAudioOutputPort(Output port, Pipe source);
    }

    public static abstract class Output extends AudioPort {

        private PortInfo info;

        public Output() {
            this(PMap.EMPTY);
        }

        public Output(PMap properties) {
            info = PortInfo.create(AudioPort.class, PortInfo.Direction.OUT, properties);
        }

        public final PortInfo getInfo() {
            return info;
        }

        protected final void makeConnection(Input port, Pipe source) throws PortConnectionException {
            port.addAudioOutputPort(this, source);
        }

        protected final void breakConnection(Input port, Pipe source) {
            port.removeAudioOutputPort(this, source);
        }
    }
    
    public static class Provider implements Port.TypeProvider {

        @Override
        public Stream<Type<?>> types() {
            return Stream.of(new Port.Type<>(AudioPort.class));
        }
        
    }
    
}
