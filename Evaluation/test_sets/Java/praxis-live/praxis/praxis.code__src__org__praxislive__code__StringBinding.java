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
package org.praxislive.code;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.praxislive.code.userapi.Type;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.core.Value;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class StringBinding extends PropertyControl.Binding {

    private final List<PString> allowed;
    private final List<PString> suggested;
    private final PString mime;
    private final PString template;
    private final boolean emptyIsDefault;
    final PString def;

    private StringBinding(String mime,
            String template,
            String def,
            String[] suggested,
            boolean emptyIsDefault) {
        this.mime = mime == null ? PString.EMPTY : PString.of(mime);
        this.template = template == null ? PString.EMPTY : PString.of(template);
        this.def = def == null ? PString.EMPTY : PString.of(def);
        if (suggested.length > 0) {
            this.suggested = Stream.of(suggested)
                    .map(PString::of)
                    .collect(Collectors.toList());
        } else {
            this.suggested = Collections.EMPTY_LIST;
        }
        this.allowed = Collections.EMPTY_LIST;
        this.emptyIsDefault = emptyIsDefault;
    }

    private StringBinding(String[] allowedValues, String def) {
        if (allowedValues.length == 0) {
            throw new IllegalArgumentException();
        }
        allowed = Stream.of(allowedValues)
                .distinct()
                .map(PString::of)
                .collect(Collectors.toList());
        PString d = PString.of(def);
        if (!allowed.contains(d)) {
            d = allowed.get(0);
        }
        this.def = d;
        mime = PString.EMPTY;
        template = PString.EMPTY;
        this.suggested = Collections.EMPTY_LIST;
        this.emptyIsDefault = false;
    }

    @Override
    public void set(Value value) throws Exception {
        PString pstr = PString.coerce(value);
        if (allowed.isEmpty() || allowed.contains(pstr)) {
            setImpl(pstr);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void set(double value) throws Exception {
        set(PNumber.of(value));
    }

    abstract void setImpl(PString value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        PMap keys = PMap.EMPTY;
        if (!allowed.isEmpty()) {
            keys = PMap.of(PString.KEY_ALLOWED_VALUES,
                    PArray.of(allowed));
        } else if (!mime.isEmpty()) {
            if (!template.isEmpty()) {
                keys = PMap.of(
                        PString.KEY_MIME_TYPE, mime,
                        PString.KEY_TEMPLATE, template);
            } else {
                keys = PMap.of(PString.KEY_MIME_TYPE, mime);
            }
        } else if (!suggested.isEmpty()) {
            if (emptyIsDefault) {
                keys = PMap.of(PString.KEY_SUGGESTED_VALUES,
                    PArray.of(suggested),
                    PString.KEY_EMPTY_IS_DEFAULT,
                    true);
            } else {
                keys = PMap.of(PString.KEY_SUGGESTED_VALUES,
                    PArray.of(suggested));
            }
        } else if (emptyIsDefault) {
            keys = PMap.of(PString.KEY_EMPTY_IS_DEFAULT, true);
        }
        return ArgumentInfo.of(PString.class, keys);
    }

    @Override
    public Value getDefaultValue() {
        return PString.of(def);
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == String.class || type.isEnum();
    }

    static StringBinding create(CodeConnector<?> connector, Field field) {
        String[] allowed = new String[0];
        String[] suggested = new String[0];
        boolean emptyIsDefault = false;
        String mime = "";
        String def = "";
        String template = "";
        Type.String ann = field.getAnnotation(Type.String.class);
        if (ann != null) {
            allowed = ann.allowed();
            suggested = ann.suggested();
            emptyIsDefault = ann.emptyIsDefault();
            mime = ann.mime();
            def = ann.def();
            template = ann.template();
        }
        Class<?> type = field.getType();
        if (type == String.class) {
            if (allowed.length > 0) {
                return new StringField(field, allowed, def);
            } else {
                return new StringField(field, mime, template, def, suggested, emptyIsDefault);
            }
        } else if (type.isEnum()) {
            List<String> filter = Arrays.asList(allowed);
            allowed = Stream.of(type.getEnumConstants())
                    .map(Object::toString)
                    .filter(s -> filter.isEmpty() || filter.contains(s))
                    .toArray(String[]::new);
            int defIdx = 0;
            if (!def.isEmpty()) {
                defIdx = Arrays.asList(allowed).indexOf(def);
            }
            return new EnumField(field, (Class<? extends Enum>) type, allowed, allowed[defIdx]);
        } else {
            if (allowed != null && allowed.length > 0) {
                return new NoField(allowed, def);
            } else {
                return new NoField(mime, template, def, suggested, emptyIsDefault);
            }
        }
    }

    private static class NoField extends StringBinding {

        private PString value;

        private NoField(String mime,
                String template,
                String def,
                String[] suggested,
                boolean emptyIsDefault) {
            super(mime, template, def, suggested, emptyIsDefault);
            value = this.def;
        }
        
        private NoField(String[] allowed, String def) {
            super(allowed, def);
            value = this.def;
        }
               
        void setImpl(PString value) throws Exception {
            this.value = value;
        }

        @Override
        public Value get() {
            return value;
        }
        
    }
    
    private static class StringField extends StringBinding {
        
        private final Field field;
        private CodeDelegate delegate;
        
        private StringField(Field field,
                String mime,
                String template,
                String def,
                String[] suggested,
                boolean emptyIsDefault) {
            super(mime, template, def, suggested, emptyIsDefault);
            this.field = field;
        }
        
        private StringField(Field field, String[] allowed, String def) {
            super(allowed, def);
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(def);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void setImpl(PString value) throws Exception {
            field.set(delegate, value.toString());
        }

        @Override
        public Value get() {
            try {
                return PString.of(field.get(delegate));
            } catch (Exception ex) {
                return PString.EMPTY;
            }
        }
        
    }
    private static class EnumField extends StringBinding {
        
        private final Field field;
        private final Class<? extends Enum> type;
        private CodeDelegate delegate;
                
        private EnumField(Field field,
                Class<? extends Enum> type,
                String[] allowed,
                String def) {
            super(allowed, def);
            this.field = field;
            this.type = type;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(def);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void setImpl(PString value) throws Exception {
            field.set(delegate, Enum.valueOf(type, value.toString()));
        }

        @Override
        public Value get() {
            try {
                return PString.of(field.get(delegate));
            } catch (Exception ex) {
                return PString.EMPTY;
            }
        }
        
    }

}
