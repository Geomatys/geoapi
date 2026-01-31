/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2018-2025 Open Geospatial Consortium, Inc.
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
import java.nio.file.Path;


/**
 * Configuration options for the Python interpreter.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
public class Configuration {
    /**
     * Keys for configuration options.
     *
     * @param  <T>  type of the option value.
     *
     * @version 4.0
     * @since   4.0
     */
    public static final class Key<T> {
        /**
         * Path to {@code "python3.dll"} (on Windows) or {@code "libpython3.so"} (on Unix) file.
         * If this option is not provided, then the CPython binary will be searched on the library path.
         */
        public static final Key<Path> LIBRARY = new Key<>(null, Path.class);

        /**
         * Whether to enable the isolated mode. The default value is {@code true}.
         * In isolated mode, potentially unsafe paths such as the current directory
         * are not included in the {@code sys.path}.
         *
         * <p>This option is ignored if the CPython interpreter is already initialized
         * when the {@link Environment} is constructed.</p>
         */
        public static final Key<Boolean> ISOLATED = new Key<>("isolated", Boolean.class);

        /**
         * Whether to use environment variable. The default value is {@code false}
         * is {@linkplain #ISOLATED isolated} mode, or {@@code true} otherwise.
         *
         * <p>This option is ignored if the CPython interpreter is already initialized
         * when the {@link Environment} is constructed.</p>
         */
        public static final Key<Boolean> USE_ENVIRONMENT = new Key<>("use_environment", Boolean.class);

        /**
         * Whether to use <abbr>UTF</abbr>-8 mode. If enabled, Python ignores the locale encoding.
         *
         * <p>This option is ignored if the CPython interpreter is already initialized
         * when the {@link Environment} is constructed.</p>
         */
        public static final Key<Boolean> UTF8_MODE = new Key<>("utf8_mode", Boolean.class);

        /**
         * Whether to enable the Python Development Mode. When enabled, CPython introduces
         * additional runtime checks that are too expensive to be enabled by default.
         * This is equivalent to the {@code -X dev} command line option
         * or the {@code PYTHONDEVMODE} environment variable set to 1.
         *
         * <p>This option is ignored if the CPython interpreter is already initialized
         * when the {@link Environment} is constructed.</p>
         */
        public static final Key<Boolean> DEV_MODE = new Key<>("dev_mode", Boolean.class);

        /**
         * Whether to allow CPython to write {@code .pyc} files on the import of source modules.
         *
         * <p>This option is ignored if the CPython interpreter is already initialized
         * when the {@link Environment} is constructed.</p>
         */
        public static final Key<Boolean> WRITE_BYTECODE = new Key<>("write_bytecode", Boolean.class);

        /**
         * The name to use in calls to {@code PyInitConfig_SetInt}, or {@code null} if none.
         */
        final String name;

        /**
         * The type of option values.
         */
        final Class<T> type;

        /**
         * Creates a new key.
         *
         * @param name   name to use in calls to {@code PyInitConfig_SetInt}.
         * @param type   type of option values.
         */
        private Key(final String name, final Class<T> type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * The configuration options.
     */
    final Map<Key<?>, Object> values;

    /**
     * Creates an initially empty configuration.
     */
    public Configuration() {
        values = new HashMap<>();
    }

    /**
     * Returns the value for the given option, or {@code null} if none.
     *
     * @param  <T>  type of value associated to the given key.
     * @param  key  key of the option to fetch.
     * @return value of the given option.
     */
    public <T> T getOption(final Key<T> key) {
        return key.type.cast(values.get(key));
    }

    /**
     * Sets the value of the given option.
     *
     * @param  <T>    type of value associated to the given key.
     * @param  key    key of the option to set.
     * @param  value  new value for the given option.
     */
    public <T> void setOption(final Key<T> key, final T value) {
        values.put(key, value);
    }

    /**
     * Returns a string representation of the options.
     *
     * @return a string representation of the options.
     */
    @Override
    public String toString() {
        final var sb = new StringBuilder();
        values.forEach((key, value) -> {
            sb.append(key.name).append(" = ").append(value).append(System.lineSeparator());
        });
        return sb.toString();
    }
}
