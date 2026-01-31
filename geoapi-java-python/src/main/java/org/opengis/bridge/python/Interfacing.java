/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2018-2024 Open Geospatial Consortium, Inc.
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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.opengis.annotation.ResourceBundles;
import org.opengis.annotation.UML;


/**
 * Specifies how a Python object should be interfaced to a Java implementation of a given interface.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class Interfacing {
    /**
     * The prefix of Python class names handled by {@code GeoAPI}.
     */
    private static final String PYTHON_PREFIX = "opengis.";

    /**
     * The file in the {@link GeoAPI} package containing the list of all GeoAPI interfaces having subclasses.
     * This is used for populating {@link #subclassed}.
     */
    static final String SUBCLASSED_LIST = "subclassed.txt";

    /**
     * Initial capacity for the {@link #subclassed} set,
     * as the number of lines in the {@value #SUBCLASSED_LIST} file divided by 0.75.
     */
    static final int SUBCLASSED_CAPACITY = 52;

    /**
     * The unique instance.
     */
    static final Interfacing INSTANCE;
    static {
        try {
            INSTANCE = new Interfacing();
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The Java classes for given Python type names. The content of this map is derived
     * from the content of the {@code "class-index.properties"} file distributed with GeoAPI.
     * This map shall not be modified after construction for thread-safety reasons.
     */
    private transient final Map<String, String> typesForNames;

    /**
     * The interfaces from the {@link #typesForNames} entries which have at least one sub-type.
     * Used for determining if it is worth to perform the relatively costly detection of the
     * subtype implemented by a Python object.
     */
    private transient final Set<Class<?>> subclassed;

    /**
     * Creates the singleton instance.
     */
    private Interfacing() throws ClassNotFoundException, IOException {
        /*
         * Load the list of all classes without resolving the Class instances yet,
         * except for resolving ambiguities. The number of classes is potentially
         * large and only a small number of them are typically used.
         */
        final Set<String> excludes = excludes();
        final Properties p = ResourceBundles.classIndex();
        typesForNames = HashMap.newHashMap(Math.round(p.size()));
        for (final Map.Entry<Object, Object> e : p.entrySet()) {
            String type = (String) e.getKey();
            if (!excludes.contains(type)) {
                type = type.substring(type.indexOf('_') + 1).intern();
                typesForNames.put(type, ((String) e.getValue()).intern());
            }
        }
        /*
         * Load the list of classes that may have sub-types. We convert the String
         * into Class instances here because the number of classes is smaller.
         */
        subclassed = new HashSet<>(SUBCLASSED_CAPACITY);
        final ClassLoader loader = UML.class.getClassLoader();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                Interfacing.class.getResourceAsStream(SUBCLASSED_LIST), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = in.readLine()) != null) {
                subclassed.add(Class.forName(line, false, loader));
            }
        }
    }

    /**
     * Types to ignore.
     */
    static Set<String> excludes() {
        return Set.of("DQ_Scope");      // GeoAPI 3.1 branch also has "RS_Identifier".
    }

    /**
     * Returns {@code true} if the given Java interface may potentially have sub-interfaces recognized by
     * {@link #toJavaType(PyObject)}. This is an optimization for avoiding potentially costly checks.
     * In case of doubt, conservatively returns {@code true}.
     *
     * @param  type  the Java interface to check for sub-typing.
     * @return {@code true} if the given interface may have sub-interfaces known to {@link #toJavaType(PyObject)}.
     */
    protected boolean hasKnownSubtypes(Class<?> type) {
        return subclassed.contains(type);
    }

    /**
     * Returns the Java type for the given Python type, taking in account only types assignable to the given base.
     * Caller should have verified that {@link #hasKnownSubtypes(Class)} returns {@code true} before to invoke this
     * method.
     *
     * @param  <T>       compile-time value of the {@code base} argument.
     * @param  base      the base type of the desired interface.
     * @param  object    the Python object for which to get the Java type.
     * @param  builtins  the value of {@link Environment#builtins}.
     * @return the Python object type as a type assignable to {@code base}. May be {@code base} itself.
     *
     * @see Environment#getJavaType(Class, PyObject)
     */
    final <T> Class<? extends T> getJavaType(final Class<T> base, final PyObject object, final PyObject builtins) {
        try (final PyObject type = builtins.call("type", object)) {
            final Class<?> c = specialize(base, type);
            return (c != null) ? c.asSubclass(base) : base;
        }
    }

    /**
     * Returns the Java type for the given Python type, or {@code null} if unknown.
     * This method check only the given type. In case of unrecognized type, it does
     * not verify if a parent of the given type would be recognized.
     *
     * @param  type  the Python type as given by {@code type(object)} in Python.
     * @return the Java type for the given Python type, or {@code null} if unknown.
     *
     * @see Environment#getJavaType(Class, PyObject)
     */
    protected Class<?> toJavaType(final PyObject type) {
        if (type != null) {
            final String module;
            try (final PyObject obj = type.getAttribute("__module__")) {
                module = obj.getStringValue();
            }
            if (module != null && module.startsWith(PYTHON_PREFIX)) {
                String name;
                try (final PyObject obj = type.getAttribute("__name__")) {
                    name = obj.getStringValue();
                }
                name = typesForNames.get(name);
                if (name != null) try {
                    return Class.forName(name, false, UML.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new EnvironmentException("Inconsistent \"class-index.properties\" file.", e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the Java type for the given Python type, taking in account only types assignable to the given base.
     *
     * @param  base  the base type of the desired interface.
     * @param  type  the Python type, as returned by {@code type(object)} in Python.
     * @return an interface assignable to {@code base}, or {@code null} if none.
     *
     * @see #getJavaType(Class, PyObject, PyObject)
     */
    private Class<?> specialize(final Class<?> base, final PyObject type) {
        if (type != null) {
            try (final PyObject bases = type.getAttribute("__bases__")) {
                final int length;
                try (final PyObject obj = bases.getAttribute("__len__")) {
                    length = obj.getIntValue();
                }
                for (int i=0; i<length; i++) {
                    try (final PyObject parent = bases.call("__getitem__", i)) {
                        Class<?> c = toJavaType(parent);
                        if (c == null) {
                            c = specialize(base, parent);
                            if (c != null) return c;
                        } else if (base.isAssignableFrom(c)) {
                            return c;
                        } else {
                            /*
                             * Do not search the parent if the class that we found is not assignable to base.
                             * If `c` is not a subtype of `base`, its parents will not be subtypes neither.
                             */
                        }
                    }
                }
            }
        }
        return null;
    }
}
