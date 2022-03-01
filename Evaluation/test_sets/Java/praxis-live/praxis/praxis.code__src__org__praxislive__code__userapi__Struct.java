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
package org.praxislive.code.userapi;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Objects;
import java.util.OptionalInt;
import org.praxislive.core.DataObject;
import org.praxislive.util.ArrayUtils;

/**
 * A basic Struct-like DataObject for grouping other DataObjects or primitive arrays.
 * 
 * Use like -
 * <pre>
 * {@code
 * static class Particle extends Struct {
 *   PVector position = register(new PVector());
 *   PVector velocity = register(new PVector());
 * }
 * }
 * </pre>
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public abstract class Struct implements DataObject {
    
    private DataObject[] data = new DataObject[0];
    private OptionalInt size = OptionalInt.of(0);
    
    protected <T extends DataObject> T register(T dob) {
        data = ArrayUtils.add(data, Objects.requireNonNull(dob));
        if (size.isPresent()) {
            OptionalInt dobSize = dob.size();
            if (dobSize.isPresent()) {
                size = OptionalInt.of(size.getAsInt() + dobSize.getAsInt());
            } else {
                size = OptionalInt.empty();
            }
        }
        return dob;
    }
    
    protected double[] register(double[] data) {
        register(new DoubleArrayDataObject(data));
        return data;
    }
    
    protected float[] register(float[] data) {
        register(new FloatArrayDataObject(data));
        return data;
    }
    
    protected int[] register(int[] data) {
        register(new IntArrayDataObject(data));
        return data;
    }
    
    @Override
    public void writeTo(DataOutput out) throws Exception {
        for (DataObject dob : data) {
            dob.writeTo(out);
        }
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        for (DataObject dob : data) {
            dob.readFrom(in);
        }
    }

    @Override
    public OptionalInt size() {
        return size;
    }
    
    
    private static class DoubleArrayDataObject implements DataObject {
        
        private final double[] data;
        private final OptionalInt size;
        
        private DoubleArrayDataObject(double[] data) {
            this.data = data;
            size = OptionalInt.of(data.length * Double.BYTES);
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            for (double d : data) {
                out.writeDouble(d);
            }
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            for (int i=0; i<data.length; i++) {
                data[i] = in.readDouble();
            }
        }

        @Override
        public OptionalInt size() {
            return size;
        }
        
    }
    private static class FloatArrayDataObject implements DataObject {
        
        private final float[] data;
        private final OptionalInt size;
        
        private FloatArrayDataObject(float[] data) {
            this.data = data;
            size = OptionalInt.of(data.length * Float.BYTES);
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            for (float f : data) {
                out.writeFloat(f);
            }
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            for (int i=0; i<data.length; i++) {
                data[i] = in.readFloat();
            }
        }

        @Override
        public OptionalInt size() {
            return size;
        }
        
    }
    private static class IntArrayDataObject implements DataObject {
        
        private final int[] data;
        private final OptionalInt size;
        
        private IntArrayDataObject(int[] data) {
            this.data = data;
            size = OptionalInt.of(data.length * Integer.BYTES);
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            for (int i : data) {
                out.writeInt(i);
            }
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            for (int i=0; i<data.length; i++) {
                data[i] = in.readInt();
            }
        }

        @Override
        public OptionalInt size() {
            return size;
        }
        
    }
    
    
}
