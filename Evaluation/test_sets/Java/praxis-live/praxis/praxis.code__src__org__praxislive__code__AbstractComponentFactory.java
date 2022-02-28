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
package org.praxislive.code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.praxislive.core.Component;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.Lookup;
import org.praxislive.core.Root;
import org.praxislive.core.services.ComponentFactoryService;
import org.praxislive.impl.InstanceLookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractComponentFactory implements ComponentFactory {

    private final Map<ComponentType, MetaData> componentMap;

    protected AbstractComponentFactory() {
        componentMap = new LinkedHashMap<>();
    }

    @Override
    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    @Override
    public ComponentType[] getRootComponentTypes() {
        return new ComponentType[0];
    }

    @Override
    public ComponentFactory.MetaData<? extends Component> getMetaData(ComponentType type) {
        return componentMap.get(type);
    }

    @Override
    public ComponentFactory.MetaData<? extends Root> getRootMetaData(ComponentType type) {
        return null;
    }

    @Override
    public Class<? extends ComponentFactoryService> getFactoryService() {
        return CodeComponentFactoryService.class;
    }

    protected void add(Data info) {
        componentMap.put(info.factory.getComponentType(), info.toMetaData());
    }

    protected Data data(CodeFactory<?> factory) {
        return new Data(factory);
    }

    protected String source(String location) {
        return CodeUtils.load(getClass(), location);
    }

    private static class MetaData extends ComponentFactory.MetaData<Component> {

        private final boolean deprecated;
        private final ComponentType replacement;
        private final Lookup lookup;

        private MetaData(
                boolean deprecated,
                ComponentType replacement,
                Lookup lookup) {
            this.deprecated = deprecated;
            this.replacement = replacement;
            this.lookup = lookup;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public Optional<ComponentType> findReplacement() {
            return Optional.ofNullable(replacement);
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

    }

    public static class Data {

        private final CodeFactory<?> factory;
        private final List<Object> lookupList;
        private boolean deprecated;
        private ComponentType replacement;

        private Data(CodeFactory<?> factory) {
            this.factory = factory;
            lookupList = new ArrayList<>();
            lookupList.add(factory);
        }

        public Data deprecated() {
            deprecated = true;
            return this;
        }

        public Data replacement(String type) {
            replacement = ComponentType.of(type);
            deprecated = true;
            return this;
        }

        public Data add(Object obj) {
            lookupList.add(obj);
            return this;
        }

        private MetaData toMetaData() {
            return new MetaData(deprecated, replacement,
                    InstanceLookup.create(lookupList.toArray()));
        }
    }
}
