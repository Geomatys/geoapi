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

import java.util.Objects;
import java.lang.ref.Cleaner;


/**
 * Interfaces Java applications with an environment in which a Python interpreter is running.
 * Only one instance of {@code Environment} is needed. This object should be used in a
 * {@code try} … {@code finally} block for releasing resources when no longer needed.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
public class Environment implements AutoCloseable {
    /**
     * The bindings, or {@code null} if the environment has been {@linkplain #close() closed}.
     */
    private CPython bindings;

    /**
     * Accessor to Python built-in functions.
     * Used for {@code len(collection)}, {@code iter(collection)}, {@code next(collection)} and {@code str(object)}.
     */
    final PyObject builtins;

    /**
     * Cleaner for releasing Python objects after the Java objects is no longer reachable.
     */
    final Cleaner cleaner;

    /**
     * Creates a new environment. Current implementation accepts only binding to CPython.
     * But a future version could allow different implementations of Python interpreter.
     */
    private Environment(final CPython bindings) {
        this.bindings = bindings;
        cleaner = Cleaner.create();
        builtins = bindings.importModule("builtins");
    }

    /**
     * Creates a new environment using the CPython interpreter.
     * If a CPython interpreter is already initialized, it will be used.
     * Otherwise, CPython will be automatically initialized with the given configuration options.
     *
     * @param  config   interpreter configuration.
     * @return connection to a CPython interpreter.
     * @throws EnvironmentException if a configuration error prevents the Java-Python bridge to work normally.
     */
    public static Environment forCPython(Configuration config) {
        try {
            final CPython bindings = new CPython(Objects.requireNonNull(config));
            try {
                bindings.configuration();
                return new Environment(bindings);
            } catch (Throwable e) {
                bindings.shutdown();
                throw e;
            }
        } catch (EnvironmentException e) {
            throw e;
        } catch (Throwable e) {
            throw new EnvironmentException("Cannot bind to CPython interpreter.", e);
        }
    }

    /**
     * Wraps the given Python object in a Java object of the given type.
     * The given type should be a GeoAPI interface.
     * The given {@code PyObject} will be automatically closed
     * when the returned object is garbage-collected.
     *
     * @param  <T>     compile-time value of the {@code type} argument.
     * @param  object  the Python object to wrap in a Java object.
     * @param  type    interface to be implemented by the desired Java wrapper.
     * @return Java object implementing the given interface by delegating the the Python object.
     */
    final <T> T pythonToJava(final PyObject object, final Class<T> type) {
        final var proxy = new Singleton(this, object);
        final T result = type.cast(proxy.newProxyInstance(type));
        object.automaticRelease(cleaner, result);
        return result;
    }

    /**
     * Releases the resources used by this environment.
     * If the environment is already closed, then this method does nothing.
     */
    @Override
    public void close() {
        final CPython close;
        synchronized (this) {
            close = bindings;
            bindings = null;
        }
        if (close != null) {
            builtins.close();
            close.shutdown();
        }
    }
}
