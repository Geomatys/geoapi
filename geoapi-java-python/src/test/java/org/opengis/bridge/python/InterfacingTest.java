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

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.IOException;
import java.lang.reflect.Field;
import org.opengis.annotation.UML;
import org.opengis.annotation.ResourceBundles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests {@link Interfacing}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
@SuppressWarnings("exports")
public final class InterfacingTest {
    /**
     * The {@link Interfacing#INSTANCE} constant.
     */
    private final Interfacing geoapi;

    /**
     * Creates a new test case.
     */
    public InterfacingTest() {
        geoapi = Interfacing.INSTANCE;
    }

    /**
     * Gets a private field.
     */
    private <T> T getPrivateField(final Class<T> type, final String name) {
        try {
            final Field field = Interfacing.class.getDeclaredField(name);
            field.setAccessible(true);
            return assertInstanceOf(type, field.get(geoapi));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns {@link Interfacing#typesForNames}.
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> typesForNames() {
        return (Map<String, String>) getPrivateField(Map.class, "typesForNames");
    }

    /**
     * Returns {@link Interfacing#subclassed}.
     */
    private Set<?> subclassed() {
        return getPrivateField(Set.class, "subclassed");
    }

    /**
     * Verifies the {@code *_CAPACITY} constant values in {@link Interfacing}.
     */
    @Test
    public void verifyCapacities() {
        int count = subclassed().size();
        int r = count / 3;          // Dividing 'count' by 0.75 is equivalent to multiplying by 1.333333…
        if ((count % 3) != 0) {
            r++;
        }
        count += r;
        assertEquals(count, Interfacing.SUBCLASSED_CAPACITY);
    }

    /**
     * Verifies the content of {@link Interfacing} mapping from names to classes.
     *
     * @throws IOException if an error occurred while reading the {@code "class-index.properties"} file.
     * @throws ClassNotFoundException if a value in {@code "class-index.properties"} is invalid.
     */
    @Test
    public void verifyTypesForNames() throws IOException, ClassNotFoundException {
        final Properties umlToClass = ResourceBundles.classIndex();
        /*
         * Create a map of Python type names to Java classes. In this process, we are going to have name collisions
         * as a result of prefix removal. For example, "MD_Identifier" and "RS_Identifier" both become "Identifier".
         * However in such case, one of the types is deprecated. The 'deprecated' set will contain those deprecated
         * types that we need to omit for avoiding name collisions.
         */
        final Set<Class<?>> deprecated = new HashSet<>();
        final ClassLoader loader = UML.class.getClassLoader();
        final Map<String,Class<?>> typesForNames = HashMap.newHashMap(umlToClass.size());
        for (final Map.Entry<Object,Object> e : umlToClass.entrySet()) {
            String name = (String) e.getKey();
            name = name.substring(name.indexOf('_') + 1);
            final Class<?> type = Class.forName(((String) e.getValue()), false, loader);
            final Class<?> previous = typesForNames.putIfAbsent(name, type);
            if (previous != null && previous != type) {
                if (previous.isAnnotationPresent(Deprecated.class)) {
                    assertSame(previous, typesForNames.put(name, type));
                    assertTrue(deprecated.add(previous));
                } else if (type.isAnnotationPresent(Deprecated.class)) {
                    assertTrue(deprecated.add(type));
                } else {
                    fail("Name collision: " + name);
                }
            }
        }
        /*
         * Assert that Interfacing knows the correct set of interfaces to exclude.
         * Tested first because other tests in this class are likely to fail if this list
         * of excluded interfaces is wrong.
         */
        final Set<String> excludes = new HashSet<>();
        for (final Class<?> c : deprecated) {
            assertTrue(excludes.add(c.getAnnotation(UML.class).identifier()));
        }
        assertEquals(excludes, Interfacing.excludes(), "excludes");
        /*
         * Assert that Interfacing has the correct list of Java interfaces.
         * In particular, in case of name collision the interface shall be the non-deprecated one.
         */
        for (final Map.Entry<String, String> e : typesForNames().entrySet()) {
            final String key = e.getKey();
            final Class<?> type = typesForNames.remove(key);
            assertNotNull(type, key);
            assertEquals(type.getName(), e.getValue(), key);
        }
    }

    /**
     * Verifies the content of {@link Interfacing} list of sub-classed classed.
     *
     * @throws ClassNotFoundException if a value in {@code "class-index.properties"} is invalid.
     */
    @Test
    public void verifySubclassed() throws ClassNotFoundException {
        final Map<String, String> typesForNames = typesForNames();
        /*
         * For each interface, set a flag telling us whether that interface has subtypes or not.
         * Environment uses this information for reducing the number of relatively costly checks
         * for subtypes.
         */
        final ClassLoader loader = UML.class.getClassLoader();
        final Map<Class<?>, Boolean> hasSubTypes = HashMap.newHashMap(typesForNames.size());
        for (final String name : typesForNames.values()) {
            final Class<?> type = Class.forName(name, false, loader);
            hasSubTypes.put(type, Boolean.FALSE);
        }
        for (final String name : typesForNames.values()) {
            final Class<?> type = Class.forName(name, false, loader);
            for (final Class<?> parent : type.getInterfaces()) {
                hasSubTypes.replace(parent, Boolean.TRUE);
            }
        }
        /*
         * List the interfaces having sub-types, in alphabetical order.
         * Then compare with content of the "subclassed.txt" file.
         */
        hasSubTypes.values().removeIf((v) -> !v);
        if (!hasSubTypes.keySet().equals(subclassed())) {
            final String[] parentTypes = new String[hasSubTypes.size()];
            int i = 0;
            for (final Class<?> c : hasSubTypes.keySet()) {
                parentTypes[i++] = c.getName();
            }
            assertEquals(parentTypes.length, i);
            Arrays.sort(parentTypes);
            final String lineSeparator = System.lineSeparator();
            final StringBuilder buffer = new StringBuilder(500).append("Content of ")
                    .append(Interfacing.SUBCLASSED_LIST).append(" should be:")
                    .append(lineSeparator);
            for (final String type : parentTypes) {
                buffer.append(type).append(lineSeparator);
            }
            fail(buffer.toString());
        }
    }
}
