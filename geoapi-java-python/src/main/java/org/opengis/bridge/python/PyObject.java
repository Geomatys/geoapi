/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2026 Open Geospatial Consortium, Inc.
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

import java.lang.ref.Cleaner;
import java.lang.foreign.MemorySegment;


/**
 * Handler for an instance of {@code PyObject} from the C <abbr>API</abbr> of the Python interpreter.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class PyObject implements Wrapper, AutoCloseable, Runnable {
    /**
     * Bindings to native functions.
     */
    private final CPython bindings;

    /**
     * Memory address of the C <abbr>API</abbr> type {@code PyObject} which this class represents.
     */
    private final MemorySegment pointer;

    /**
     * Whether the call to {@link #run()} is managed by the garbage-collector.
     */
    private boolean managed;

    /**
     * Creates a new handler for a C <abbr>API</abbr> type {@code PyObject}.
     *
     * <h4>Memory management</h4>
     * Caller shall either use the new object in a try-with-resource,
     * or register it immediately in a {@link java.lang.ref.Cleaner}.
     *
     * @param  bindings  bindings to native functions.
     * @param  pointer   memory address of the C <abbr>API</abbr> type {@code PyObject}.
     */
    PyObject(final CPython bindings, final MemorySegment pointer) {
        this.bindings = bindings;
        this.pointer  = pointer;
    }

    /**
     * Requests this object to be automatically closed when the given wrapper is garbage-collected.
     *
     * @param  cleaner  where to register a future call to {@link #run()}.
     * @param  wrapper  the object to watch for garbage collection.
     */
    final synchronized void automaticRelease(final Cleaner cleaner, final Object wrapper) {
        assert !managed;
        cleaner.register(wrapper, this);
        managed = true;
    }

    /**
     * Returns the address of the native object if using the given bindings.
     *
     * @param  bindings  the bindings used for accessing native objects.
     * @return address of the native object.
     * @throws PythonException if the native object is not managed by the specified bindings.
     */
    @Override
    public MemorySegment address(final CPython bindings) {
        if (this.bindings == bindings) {
            return pointer;
        }
        throw new PythonException("Not a Python object from the same bindings.");
    }

    /**
     * Returns whether the given object is wrapping the same Python object than {@code this}.
     * This is possible because Python uses reference count for deciding when to deallocate an object.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PyObject other) {
            return bindings == other.bindings && pointer.address() == other.pointer.address();
        }
        return false;
    }

    /**
     * Returns a hash code value based on the memory address of the Python object.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(pointer.address());
    }

    /**
     * Returns this object value as an integer.
     *
     * @return the value of the Python object.
     */
    final int getIntValue() {
        return Math.toIntExact(bindings.getLong(pointer));
    }

    /**
     * Returns this object value as a floating-point value.
     *
     * @return the value of the Python object.
     */
    final double getDoubleValue() {
        return bindings.getDouble(pointer);
    }

    /**
     * Returns this Python object as a Java character string.
     *
     * @return the character string.
     */
    final String getStringValue() {
        return bindings.getString(pointer);
    }

    /**
     * Gets the value of a Python attribute.
     *
     * <h4>Memory management</h4>
     * Caller shall either use the returned object in a try-with-resource,
     * or register it immediately in a {@link java.lang.ref.Cleaner}.
     *
     * @param  name  name of the Python attribute.
     * @return a wrapper for the attribute value.
     */
    final PyObject getAttribute(String name) {
        return bindings.call(pointer, name, null, null);
    }

    /**
     * Invokes a method or a callable object with the given name and a single argument.
     *
     * <h4>Memory management</h4>
     * Caller shall either use the returned object in a try-with-resource,
     * or register it as an action in a {@link java.lang.ref.Cleaner}, or
     * {@linkplain #close() close} directly (e.g. in case of error).
     *
     * @param  name      name of a Python method or callable object.
     * @param  argument  argument for the call as a Python object.
     * @return a wrapper for the returned Python object.
     */
    final PyObject call(final String name, final PyObject argument) {
        return bindings.call(pointer, name, new MemorySegment[] {argument.address(bindings)}, null);
    }

    /**
     * Invokes a method or a callable object with the given name and a single argument.
     * The argument is usually an index in a list.
     *
     * <h4>Memory management</h4>
     * Caller shall either use the returned object in a try-with-resource,
     * or register it as an action in a {@link java.lang.ref.Cleaner}, or
     * {@linkplain #close() close} directly (e.g. in case of error).
     *
     * @param  name      name of a Python method or callable object.
     * @param  argument  argument for the call as a Python object.
     * @return a wrapper for the returned Python object.
     */
    final PyObject call(final String name, final int argument) {
        return bindings.call(pointer, name, new MemorySegment[1], new Integer[] {argument});
    }

    /**
     * Converts the given argument to Python objects, then invokes a method or a callable object.
     *
     * <h4>Memory management</h4>
     * Caller shall either use the returned object in a try-with-resource,
     * or register it as an action in a {@link java.lang.ref.Cleaner}, or
     * {@linkplain #close() close} directly (e.g. in case of error).
     *
     * @param  name       name of a Python method or callable object.
     * @param  arguments  arguments for the call, or {@code null} if none.
     * @return a wrapper for the returned Python object.
     */
    final PyObject convertAndCall(final String name, final Object[] arguments) {
        final MemorySegment[] pointers;
        if (arguments == null) {
            pointers = null;
        } else {
            pointers = new MemorySegment[arguments.length];
            for (int i=0; i<pointers.length; i++) {
                Object arg = arguments[i];
                if (arg == null) {
                    pointers[i] = MemorySegment.NULL;
                } else if (arg instanceof Wrapper w) {
                    pointers[i] = w.address(bindings);
                }
            }
        }
        return bindings.call(pointer, name, pointers, arguments);
    }

    /**
     * Releases the native {@code PyObject} if not already done or not automatically managed.
     * This method is intended to be invoked in a try-with-resource block.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (managed) return;
            managed = true;
        }
        bindings.decrementRef(pointer);
    }

    /**
     * Releases the native {@code PyObject}. This method should be called by {@link Cleaner} only.
     * It shall be invoked exactly once (or zero if the <abbr>JVM</abbr> is shutdown before release).
     */
    @Override
    public void run() {
        bindings.decrementRef(pointer);
    }
}
