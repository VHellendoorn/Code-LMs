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
package org.praxislive.core.components;

import org.praxislive.code.GenerateTemplate;

import org.praxislive.core.code.CoreCodeDelegate;

// default imports
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.praxislive.core.*;
import org.praxislive.core.types.*;
import org.praxislive.code.userapi.*;
import static org.praxislive.code.userapi.Constants.*;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(CoreRoutingGate.TEMPLATE_PATH)
public class CoreRoutingGate extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/routing_gate.pxj";

    // PXJ-BEGIN:body
    
    @P(1) boolean active;
    @P(2) @Type(cls = PArray.class) @OnChange("updatePattern")
    Property pattern;
    @P(3) @ReadOnly
    int index;
    
    @Out(1) Output out;
    @Out(2) Output discard;
    
    double[] pt;

    @Override
    public void init() {
        updatePattern();
    }

    @In(1) void in(Value arg) {
        if (checkSend()) {
            out.send(arg);
        } else {
            discard.send(arg);
        }
    }
    
    @T(1) void retrigger() {
        index = 0;
    }
    
    boolean checkSend() {
        if (active) {
            if (pt.length == 0) {
                return true;
            }
            double p = pt[index];
            index++;
            index %= pt.length;
            if (p > 0.999999) {
                return true;
            } else if (p < 0.000001) {
                return false;
            } else {
                return random(1) < p;
            }
        } else {
            return false;
        }
    }
    
    void updatePattern() {
        try {
            PArray arr = PArray.coerce(pattern.get());
            pt = new double[arr.size()];
            for (int i = 0; i < pt.length; i++) {
                double d = PNumber.coerce(arr.get(i)).value();
                pt[i] = d;
            }
            if (pt.length == 0) {
                index = 0;
            } else {
                index %= pt.length;
            }
        } catch (ValueFormatException ex) {
            log(WARNING, "Invalid pattern");
            pt = new double[0];
            index = 0;
            pattern.set(PArray.EMPTY);
        }
    }
    
    // PXJ-END:body
    
}
