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
package org.praxislive.video.impl.components;

import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;
import org.praxislive.impl.AbstractComponentFactory;
import org.praxislive.meta.TypeRewriter;
import org.praxislive.video.impl.components.blob.BlobTracker;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoFactoryProvider implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            // ROOT
            addRoot("root:video", DefaultVideoRoot.class);

            // COMPONENTS
            addComponent("video:output", VideoOutput.class);

            // ANALYSIS
            addComponent("video:analysis:simple-tracker", BlobTracker.class);

            // FX
            addComponent("video:fx:ripple", data(Ripple.class));

            /// CONTAINER
            addComponent("video:container:in", data(VideoContainerInput.class).deprecated());
            addComponent("video:container:out", data(VideoContainerOutput.class).deprecated());

        }
    }
}
