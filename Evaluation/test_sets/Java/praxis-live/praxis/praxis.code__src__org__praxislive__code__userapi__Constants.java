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
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */

package org.praxislive.code.userapi;

import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Constants {

    private Constants() {}
    
    public final static LogLevel ERROR = LogLevel.ERROR;
    public final static LogLevel WARNING = LogLevel.WARNING;
    public final static LogLevel INFO = LogLevel.INFO;
    public final static LogLevel DEBUG = LogLevel.DEBUG;

    public final static double PI = Math.PI;
    public final static double HALF_PI = PI / 2;
    public final static double THIRD_PI = PI / 3;
    public final static double QUARTER_PI = PI / 4;
    public final static double TWO_PI = PI * 2;
    public final static double DEG_TO_RAD = PI/180;
    public final static double RAD_TO_DEG = 180/PI;

}
