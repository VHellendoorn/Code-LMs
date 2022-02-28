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
package org.praxislive.video.pgl.code;

import java.lang.reflect.Field;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.pgl.PGLGraphics3D;
import org.praxislive.video.pgl.PGLSurface;
import org.praxislive.video.pgl.code.userapi.OffScreen;
import org.praxislive.video.pgl.code.userapi.PGraphics3D;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class P3DOffScreenGraphicsInfo {

    private final int width;
    private final int height;
    private final double scaleWidth;
    private final double scaleHeight;
    private final boolean persistent;
    private final Field field;

    private P3DCodeContext context;
    private PGraphics graphics;
    private PGLGraphics3D pgl;

    private P3DOffScreenGraphicsInfo(Field field,
            int width,
            int height,
            double scaleWidth,
            double scaleHeight,
            boolean persistent) {
        this.field = field;
        this.width = width;
        this.height = height;
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
        this.persistent = persistent;
    }

    void attach(P3DCodeContext context, P3DOffScreenGraphicsInfo previous) {
        this.context = context;
        if (previous != null) {
            pgl = previous.pgl;
            previous.pgl = null;
            if (previous.graphics != null) {
                previous.graphics.pgl = null;
            }
        }
    }

    void validate(PGLSurface output) {
        boolean clear = !persistent;
        if (!isValid(pgl, output)) {
            pgl = output.getContext()
                    .create3DGraphics(calculateWidth(output), calculateHeight(output));
            clear = true;
        }

        if (graphics == null || graphics.width != pgl.width || graphics.height != pgl.height) {
            graphics = new PGraphics(pgl.width, pgl.height);
            try {
                field.set(context.getDelegate(), graphics);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        if (pgl != graphics.pgl) {
            if (graphics.pgl != null) {
                graphics.release();
            }
            graphics.init(pgl, clear);
        }
    }

    void endFrame() {
        if (!persistent && graphics != null) {
            graphics.release();
        }
    }

    void release() {
        if (graphics != null) {
            graphics.pgl = null;
        }
    }

    private boolean isValid(PGLGraphics3D pgl, PGLSurface output) {
        if (pgl == null) {
            return false;
        }
        if (output.getContext() != pgl.getContext()) {
            return false;
        }
        return pgl.width == calculateWidth(output)
                && pgl.height == calculateHeight(output);

    }

    private int calculateWidth(Surface output) {
        int w = width < 1 ? output.getWidth() : width;
        w *= scaleWidth;
        return Math.max(w, 1);
    }

    private int calculateHeight(Surface output) {
        int h = height < 1 ? output.getHeight() : height;
        h *= scaleHeight;
        return Math.max(h, 1);

    }

    static P3DOffScreenGraphicsInfo create(Field field) {
        OffScreen ann = field.getAnnotation(OffScreen.class);

        if (ann == null
                || !PGraphics3D.class
                        .isAssignableFrom(field.getType())) {
            return null;
        }
        field.setAccessible(true);
        int width = ann.width();
        int height = ann.height();
        double scaleWidth = ann.scaleWidth();
        double scaleHeight = ann.scaleHeight();
        boolean persistent = ann.persistent();
        return new P3DOffScreenGraphicsInfo(field,
                width,
                height,
                scaleWidth,
                scaleHeight,
                persistent);

    }

    private class PGraphics extends PGraphics3D {

        PGLGraphics3D pgl;
        private int matrixStackDepth;

        private PGraphics(int width, int height) {
            super(width, height);
        }

        void init(PGLGraphics3D pgl, boolean clear) {
            initGraphics(pgl);
            this.pgl = pgl;
            if (clear) {
                pgl.beginDraw();
                pgl.clear();
            }
        }

        void release() {
            releaseGraphics();
            if (matrixStackDepth != 0) {
                context.getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    pgl.popMatrix();
                    matrixStackDepth--;
                }
            }
            pgl = null;
        }

        @Override
        public void beginDraw() {
            context.beginOffscreen();
            super.beginDraw();
        }

        @Override
        public void endDraw() {
            if (matrixStackDepth != 0) {
                context.getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    pgl.popMatrix();
                    matrixStackDepth--;
                }
            }
            context.endOffscreen();
        }

        @Override
        public void pushMatrix() {
            if (matrixStackDepth == 31) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack full in popMatrix()");
                return;
            }
            matrixStackDepth++;
            super.pushMatrix();
        }

        @Override
        public void popMatrix() {
            if (matrixStackDepth == 0) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack empty in popMatrix()");
                return;
            }
            matrixStackDepth--;
            super.popMatrix();
        }

    }

}
