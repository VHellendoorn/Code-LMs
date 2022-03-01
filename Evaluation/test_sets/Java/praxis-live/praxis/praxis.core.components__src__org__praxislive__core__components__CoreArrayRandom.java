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
@GenerateTemplate(CoreArrayRandom.TEMPLATE_PATH)
public class CoreArrayRandom extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/array_random.pxj";

    // PXJ-BEGIN:body
    
    @P(1) @Type(cls = PArray.class) @OnChange("extractArray")
    Property values;
    @P(2) @ReadOnly
    int index;
    
    @Out(1) Output out;
    
    PArray array;
    
    @Override
    public void init() {
        extractArray();
    }

    @T(1) void trigger() {
        if (array.isEmpty()) {
            index = -1;
            out.send();
        } else {
            index = (int) random(array.size());
            out.send(array.get(index));
        }
    }
    
    void extractArray() {
        try {
            array = PArray.coerce(values.get());
        } catch (ValueFormatException ex) {
            log(ERROR, ex, "values isn't an array");
            array = PArray.EMPTY;
        }
    }
    
    // PXJ-END:body
    
}
