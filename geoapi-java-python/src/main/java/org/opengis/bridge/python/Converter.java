/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2018-2023 Open Geospatial Consortium, Inc.
 *    http://www.geoapi.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.opengis.bridge.python;

import java.util.Locale;
import org.opengis.util.CodeList;
import org.opengis.util.ControlledVocabulary;
import org.opengis.util.InternationalString;


/**
 * Converter from Python objects to Java objects.
 * A single converter is built once-for-all before to convert an arbitrary number of objects.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
abstract class Converter<T> {
    /**
     * The Java type of converted objects.
     */
    final Class<T> type;

    /**
     * Creates a new converter.
     */
    private Converter(final Class<T> type) {
        this.type = type;
    }

    /**
     * Converts the given Python object to a Java object of the converter {@link #type}.
     * If the implementation needs to keep a reference to the given Python object, then
     * it shall invoke {@link PyObject#automaticRelease(Cleaner, Object)}.
     * This method is intended to be invoked in the following pattern:
     *
     * {@snippet lang="java" :
     *     Object java;
     *     try (PyObject python = ...) {
     *         java = converter.apply(python);
     *     }
     *     }
     *
     * @param  value  the Python object.
     * @return the Java object.
     */
    public abstract T apply(final PyObject value);

    /** Shared converter from Python objects to {@code int} primitive. */
    private static final Converter<Boolean> PRIMITIVE_BOOLEAN = new Converter<Boolean>(Boolean.class) {
        @Override public Boolean apply(final PyObject value) {
            return (value != null) ? value.getIntValue() != 0 : Boolean.FALSE;
        }
    };

    /** Shared converter from Python objects to {@code int} primitive numbers. */
    private static final Converter<Integer> PRIMITIVE_INTEGER = new Converter<Integer>(Integer.class) {
        @Override public Integer apply(final PyObject value) {
            return (value != null) ? value.getIntValue() : 0;
        }
    };

    /** Shared converter from Python objects to {@code double} primitive numbers. */
    private static final Converter<Double> PRIMITIVE_DOUBLE = new Converter<Double>(Double.class) {
        @Override public Double apply(final PyObject value) {
            return (value != null) ? value.getDoubleValue() : Double.NaN;
        }
    };

    /** Shared converter from Python objects to {@code Boolean} wrapper. */
    private static final Converter<Boolean> BOOLEAN = new Converter<Boolean>(Boolean.class) {
        @Override public Boolean apply(final PyObject value) {
            return (value != null) ? value.getIntValue() != 0 : null;
        }
    };

    /** Shared converter from Python objects to {@code Integer} wrapped numbers. */
    private static final Converter<Integer> INTEGER = new Converter<Integer>(Integer.class) {
        @Override public Integer apply(final PyObject value) {
            return (value != null) ? value.getIntValue() : null;
        }
    };

    /** Shared converter from Python objects to {@code Double} wrapped numbers. */
    private static final Converter<Double> DOUBLE = new Converter<Double>(Double.class) {
        @Override public Double apply(final PyObject value) {
            return (value != null) ? value.getDoubleValue() : null;
        }
    };

    /** Shared converter from Python objects to {@link String} instances. */
    private static final Converter<String> STRING = new Converter<String>(String.class) {
        @Override public String apply(final PyObject value) {
            return (value != null) ? value.getStringValue() : null;
        }
    };

    /** Shared converter from Python objects to {@link InternationalString} instances. */
    private static final Converter<InternationalString> I18N = new Converter<InternationalString>(InternationalString.class) {
        @Override public InternationalString apply(final PyObject value) {
            return (value != null) ? new Literal(value.getStringValue()) : null;
        }
    };

    /**
     * Converter for code list values. The conversion is based only on the enumumerated names, case-insensitive.
     */
    private static final class ForCodeList<T extends CodeList<T>> extends Converter<T> {
        /** Creates a new converter for the given code list class. */
        ForCodeList(final Class<T> type) {
            super(type);
        }

        /** Returns the name of the given enumeration or code list value. */
        static String name(final PyObject value) {
            if (value != null) {
                try (PyObject code = value.getAttribute("value")) {
                    String name = code.getStringValue();
                    if (name != null && !(name = name.trim()).isEmpty()) {
                        return name;
                    }
                }
            }
            return null;
        }

        /** Converts the given Python enumerated value to a Java {@code CodeList} value. */
        @Override public T apply(final PyObject value) {
            final String name = name(value);
            if (name == null) return null;
            return CodeList.valueOf(type, name, null).orElse(null);
        }
    }

    /**
     * Converter for enumerated values. The conversion is based only on the enum name, case-insensitive.
     */
    private static final class ForEnum<T extends Enum<T>> extends Converter<T> {
        /** Creates a new converter for the given enumeration class. */
        ForEnum(final Class<T> type) {
            super(type);
        }

        /** Converts the given Python enumerated value to a Java enumerated value. */
        @Override public T apply(final PyObject value) {
            final String name = ForCodeList.name(value);
            if (name != null) try {
                return Enum.valueOf(type, name.toUpperCase(Locale.US));     // Fast check (sufficient in most cases).
            } catch (IllegalArgumentException e) {
                for (final T c : type.getEnumConstants()) {                 // Fallback on more costly check.
                    if (c instanceof ControlledVocabulary) {
                        for (final String n : ((ControlledVocabulary) c).names()) {
                            if (name.equalsIgnoreCase(n)) return c;
                        }
                    }
                }
                throw e;
            }
            return null;
        }
    }

    /**
     * Converter from Python objects to Java objects implementing a GeoAPI interface.
     * This converter is used when the given type has no known sub-type.
     * This is the case of a majority of GeoAPI interfaces.
     */
    private static class GeoAPI<T> extends Converter<T> {
        /** Information about the Python environment (builtin functions, etc). */
        protected final Environment environment;

        /** Creates a new converter for the given Java type. */
        protected GeoAPI(final Environment environment, final Class<T> type) {
            super(type);
            this.environment = environment;
        }

        /** Converts the given Python object to a Java object of the converter {@link #type}. */
        @Override public T apply(final PyObject value) {
            return (value != null) ? environment.pythonToJava(value, type) : null;
        }
    }

    /**
     * Converter from Python objects to Java objects implementing a GeoAPI interface.
     * This converter is used when the given type is known to have sub-types.
     */
    private static final class Specializable<T> extends GeoAPI<T> {
        /** Creates a new converter for the given Java type. */
        Specializable(final Environment environment, final Class<T> type) {
            super(environment, type);
        }

        /** Converts the given Python object to a Java object of the converter {@link #type}. */
        @Override public T apply(final PyObject value) {
            if (value == null) {
                return null;
            }
            Class<? extends T> subtype = Interfacing.INSTANCE.getJavaType(type, value, environment.builtins);
            return environment.pythonToJava(value, subtype);
        }
    }

    /**
     * Returns a converter from Python objects to the given Java type.
     * The given {@code type} argument can be any of the following:
     *
     * <ul>
     *   <li>A {@link Double}, {@link Integer} or {@link Boolean}.</li>
     *   <li>A {@link CharSequence}, {@link String} or {@link InternationalString}.</li>
     *   <li>An enumeration such as {@link org.opengis.annotation.Obligation}.</li>
     *   <li>A code list such as {@link org.opengis.metadata.Datatype}.</li>
     *   <li>A GeoAPI interface (not an implementation class) such as {@link org.opengis.metadata.Metadata}.</li>
     *   <li>A non-GeoAPI interface such as {@link java.util.function.Supplier}.</li>
     * </ul>
     *
     * @param  <T>     compile-time value of the {@code type} argument.
     * @param  type    interface to be implemented by the desired Java wrappers.
     * @return converter from Python objects to Java instances of the given type.
     * @throws UnconvertibleTypeException if this method does not know how to convert Python objects to the given type.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> Converter<? extends T> fromPythonToJava(final Environment environment, final Class<T> type) {
        // `if` statements should be ordered from most frequently-used to less frequently-used.
        final Converter<?> c;
        if (CharSequence.class.isAssignableFrom(type)) {
            if (InternationalString.class.isAssignableFrom(type)) {
                c = I18N;
            } else {
                c = STRING;
            }
        } else if (type.isInterface()) {
            if (Interfacing.INSTANCE.hasKnownSubtypes(type)) {
                c = new Specializable<>(environment, type);
            } else {
                c = new GeoAPI<>(environment, type);
            }
        } else if (Number.class.isAssignableFrom(type)) {
            if (Double.class.equals(type) || Float.class.equals(type)) {
                c = DOUBLE;
            } else {
                c = INTEGER;
            }
        } else if (type.isPrimitive()) {
            if (type == Double.TYPE || type == Float.TYPE) {
                c = PRIMITIVE_DOUBLE;
            } else if (type == Boolean.TYPE) {
                c = PRIMITIVE_BOOLEAN;
            } else {                                // We forget the 'char' case for now.
                c = PRIMITIVE_INTEGER;
            }
        } else if (CodeList.class.isAssignableFrom(type)) {
            return new ForCodeList(type.asSubclass(CodeList.class));
        } else if (type.isEnum()) {
            return new ForEnum(type.asSubclass(Enum.class));
        } else if (Boolean.class.equals(type)) {
            c = BOOLEAN;
        } else {
            throw new UnconvertibleTypeException(type);
        }
        if (type.isAssignableFrom(c.type)) {
            return (Converter<? extends T>) c;
        } else {
            throw new UnconvertibleTypeException(type);
        }
    }
}
