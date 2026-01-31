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

import java.io.File;
import java.util.Map;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;


/**
 * Handlers to CPython native functions needed by this package.
 * The CPython interpreter can be shutdown by invoking {@link #shutdown()}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 *
 * @see <a href="https://docs.python.org/3/c-api/">Python/C API Reference Manual</a>
 */
@SuppressWarnings({"restricted", "UseSpecificCatch"})
final class CPython {
    /**
     * Whether {@link #shutdown()} has already been executed.
     * The intend is to avoid decrementing the reference count of a Python
     * object after the object has been deallocated by CPython shutdown.
     */
    private volatile boolean closed;

    /**
     * Whether the CPython interpreter was initialized by this class.
     * If {@code false}, then {@link #shutdown()} should do nothing.
     */
    private final boolean initializedByUs;

    /**
     * The arena used for loading the library.
     */
    private final Arena arena;

    /**
     * The lookup for retrieving the address of a symbol in the native library.
     */
    private final SymbolLookup symbols;

    /**
     * The linker to use for fetching method handles from the {@linkplain #symbols}.
     * In current version, this is always {@link Linker#nativeLinker()}.
     */
    private final Linker linker;

    /**
     * {@code PyGILState_STATE PyGILState_Ensure(void)} where PyGILState_STATE is a C/C++ {@code enum}.
     * Most CPython functions must be invoked in a block holding this lock.
     */
    private final MethodHandle lock;

    /**
     * {@code void PyGILState_Release(PyGILState_STATE)} where PyGILState_STATE is a C/C++ {@code enum}.
     * Must be invoked in a {@code finally} block after {@link #lock}.
     */
    private final MethodHandle unlock;

    /**
     * {@code void Py_DecRef(PyObject *o)}.
     * Decrements the references count and free the object when the count reaches zero.
     * Does nothing if the memory address given to this function is null.
     */
    private final MethodHandle decrementRef;

    /**
     * {@code PyObject *PyImport_ImportModule(const char *name)}.
     * A higher-level interface that calls the current “import hook function”.
     *
     * <p>This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.</p>
     */
    private final MethodHandle importModule;

    /**
     * {@code const char *PyUnicode_AsUTF8(PyObject *unicode)}.
     * Note that this version may result in truncated strings
     * if there are embedded null characters in the string.
     *
     * <p>This function returns a borrowed reference:
     * the return value shall <em>not</em> be deallocated by the caller.</p>
     */
    private final MethodHandle asUTF8;

    /**
     * {@code long PyLong_AsLong(PyObject *obj)}.
     * Returns the object value as an integer.
     */
    private final MethodHandle asLong;

    /**
     * {@code double PyFloat_AsDouble(PyObject *pyfloat)}.
     * Returns the object value as a floating-point number.
     */
    private final MethodHandle asDouble;

    /**
     * {@code PyObject *PyUnicode_FromString(const char *str)}.
     * This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.
     */
    private final MethodHandle fromUTF8;

    /**
     * {@code PyObject *PyLong_FromLong(long v)}.
     * This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.
     */
    private final MethodHandle fromLong;

    /**
     * {@code PyObject *PyFloat_FromDouble(double v)}.
     * This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.
     */
    private final MethodHandle fromDouble;

    /**
     * {@code PyObject *PyTuple_New(Py_ssize_t len)} with a {@code Py_ssize_t} assumed to 64 bits.
     * Returns a new tuple of the specified size, or {@code null} with an exception set.
     *
     * <p>This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.</p>
     */
    private final MethodHandle newTuple;

    /**
     * {@code int PyTuple_SetItem(PyObject *p, Py_ssize_t pos, PyObject *o)}.
     * Inserts a reference to the given object at the specified position of the tuple.
     * Return 0 on success.
     */
    private final MethodHandle setTupleItem;

    /**
     * {@code PyObject *PyObject_GetAttrString(PyObject *o, const char *attr_name)}.
     * Retrieve an attribute of the given name from the given object, or null on failure.
     *
     * <p>This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.</p>
     */
    private final MethodHandle getAttribute;

    /**
     * {@code PyObject *PyObject_CallObject(PyObject *callable, PyObject *args)}.
     * Call a callable Python object with arguments given by the tuple {@code args}.
     * If no arguments are needed, then {@code args} can be null.
     *
     * <p>This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.</p>
     */
    private final MethodHandle callObject;

    /**
     * {@code int PyCallable_Check(PyObject *o)}.
     * Determines if the object is callable.
     */
    private final MethodHandle isCallable;

    /**
     * {@code PyObject *PyErr_GetRaisedException(void)}.
     * Returns the exception being raised, or null if none.
     *
     * <p>This function returns a new reference:
     * caller must free with {@link #decrementRef} after usage.</p>
     */
    private final MethodHandle errorOccurred;

    /**
     * Creates the handles for all CPython functions which will be needed.
     * If this constructor returns successfully, the CPython is initialized.
     *
     * @param  config   interpreter configuration.
     * @throws Throwable if the construction failed.
     */
    CPython(final Configuration config) throws Throwable {
        // A few frequently-used function signatures.
        final var acceptInt                  = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
        final var acceptPointer              = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
        final var returnInt                  = FunctionDescriptor.of(ValueLayout.JAVA_INT);
        final var returnPointer              = FunctionDescriptor.of(ValueLayout.ADDRESS);
        final var acceptIntReturnPointer     = FunctionDescriptor.of(ValueLayout.ADDRESS,  ValueLayout.JAVA_INT);
        final var acceptPointerReturnInt     = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
        final var acceptPointerReturnPointer = FunctionDescriptor.of(ValueLayout.ADDRESS,  ValueLayout.ADDRESS);
        final var acceptTwoPtrsReturnPointer = FunctionDescriptor.of(ValueLayout.ADDRESS,  ValueLayout.ADDRESS, ValueLayout.ADDRESS);
        /*
         * We would like a shared area closed in the `shutdown()` method after CPython shutdown,
         * but closing the Area causes the following error:
         *
         *     SIGSEGV (0xb)
         *     Current thread is native thread:
         *
         *     C  [libc.so.6+0x6f8f0]  __GI___nptl_deallocate_tsd+0xb0
         *     C  [libc.so.6+0x72309]  start_thread+0x189
         *
         * For now we found no other workaround than using the global arena.
         */
        linker = Linker.nativeLinker();
        arena  = Arena.global();
        final Path library = config.getOption(Configuration.Key.LIBRARY);
        if (library != null) {
            symbols = SymbolLookup.libraryLookup(library, arena);
        } else {
            String filename = (File.separatorChar == '\\') ? "python3.dll" : "libpython3.so";
            symbols = SymbolLookup.libraryLookup(filename, arena);
        }
        lock          = lookup("PyGILState_Ensure",        returnInt);
        unlock        = lookup("PyGILState_Release",       acceptInt);
        decrementRef  = lookup("Py_DecRef",                acceptPointer);
        importModule  = lookup("PyImport_ImportModule",    acceptPointerReturnPointer);
        asUTF8        = lookup("PyUnicode_AsUTF8",         acceptPointerReturnPointer);
        asLong        = lookup("PyLong_AsLong",            FunctionDescriptor.of(ValueLayout.JAVA_LONG,   ValueLayout.ADDRESS));
        asDouble      = lookup("PyFloat_AsDouble",         FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS));
        fromUTF8      = lookup("PyUnicode_FromString",     FunctionDescriptor.of(ValueLayout.ADDRESS,     ValueLayout.ADDRESS));
        fromLong      = lookup("PyLong_FromLong",          FunctionDescriptor.of(ValueLayout.ADDRESS,     ValueLayout.JAVA_LONG));
        fromDouble    = lookup("PyLong_FromDouble",        FunctionDescriptor.of(ValueLayout.ADDRESS,     ValueLayout.JAVA_DOUBLE));
        getAttribute  = lookup("PyObject_GetAttrString",   acceptTwoPtrsReturnPointer);
        callObject    = lookup("PyObject_CallObject",      acceptTwoPtrsReturnPointer);
        isCallable    = lookup("PyCallable_Check",         acceptPointerReturnInt);
        newTuple      = lookup("PyTuple_New",              acceptIntReturnPointer);
        setTupleItem  = lookup("PyTuple_SetItem",          FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        errorOccurred = lookup("PyErr_GetRaisedException", returnPointer);
        int status = (int) lookup("Py_IsInitialized", returnInt).invokeExact();
        if (status != 0) {
            // CPython is already running.
            initializedByUs = false;
            return;
        }
        initializedByUs = true;
        final MethodHandle init   = lookup("Py_InitializeFromInitConfig", acceptPointerReturnInt);
        final MethodHandle create = lookup("PyInitConfig_Create", returnPointer);
        final MethodHandle free   = lookup("PyInitConfig_Free",   acceptPointer);
        final MethodHandle setInt = lookup("PyInitConfig_SetInt", FunctionDescriptor.of(
                ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        final var pyConfig = (MemorySegment) create.invokeExact();
        if (isNull(pyConfig)) {
            throw new OutOfMemoryError();
        }
        try (final Arena local = Arena.ofConfined()) {
            for (final Map.Entry<Configuration.Key<?>, Object> entry : config.values.entrySet()) {
                final Configuration.Key<?> key = entry.getKey();
                if (key.name == null) {
                    continue;
                }
                final MemorySegment name = local.allocateFrom(key.name);
                if (false) {
                    // Place holder for future support of options of type `int` or `str`.
                } else {     // All options other than above types are assumed `bool`.
                    long value = ((Boolean) entry.getValue()) ? 1 : 0;
                    status = (int) setInt.invokeExact(pyConfig, name, value);
                }
                if (status != 0) {
                    throw new EnvironmentException(getErrorMessage(local, pyConfig));
                }
            }
            /*
             * Initialization happens here. The constructor should return quickly after initialization
             * (i.e., before to do more work) for allowing the caller to invoke `shutdown()` if an error occurs.
             */
            status = (int) init.invokeExact(pyConfig);
            if (status != 0) {
                throw new EnvironmentException(getErrorMessage(local, pyConfig));
            }
        } finally {
            free.invokeExact(pyConfig);
        }
    }

    /**
     * Returns the error message after a failure to initialize the CPython interpreter.
     *
     * @param  local     confined arena to use for allocating memory.
     * @param  pyConfig  The CPython configuration that we tried to use.
     * @return the error message, or {@code null}.
     * @throws Throwable if an error occurred while invoking the native function.
     */
    private String getErrorMessage(final Arena local, final MemorySegment pyConfig) throws Throwable {
        final MethodHandle method = lookup("PyInitConfig_GetError",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        final MemorySegment ptr = local.allocateFrom(ValueLayout.ADDRESS, MemorySegment.NULL);
        int status = (int) method.invokeExact(pyConfig, ptr);
        if (status != 0) {
            return toString(MemorySegment.ofAddress(ptr.get(ValueLayout.JAVA_LONG, 0)));
        }
        return null;
    }

    /**
     * Returns the method handler for the CPython function of given name and signature.
     * This is a convenience method for initialization of fields in this class.
     *
     * @param  function   name of the C/C++ CPython function to invoke.
     * @param  signature  type of arguments and return type.
     * @return method handler for the CPython function.
     * @throws NoSuchElementException if the given function has not been found.
     */
    private MethodHandle lookup(final String function, final FunctionDescriptor signature) {
        MemorySegment method = symbols.find(function).orElseThrow(
                () -> new EnvironmentException("Function not found: " + function));
        return linker.downcallHandle(method, signature);
    }

    /**
     * Logs information about configuration. This method should be invoked once after construction.
     * It is outside the constructor for allowing the caller to invoke {@link #shutdown()} in case of error.
     */
    final void configuration() throws Throwable {
        var version = (MemorySegment) lookup("Py_GetVersion", FunctionDescriptor.of(ValueLayout.ADDRESS)).invokeExact();
        String message = "Binding GeoAPI to Python " + toString(version);
        System.getLogger("org.opengis.bridge.python").log(System.Logger.Level.INFO, message);
    }

    /**
     * Imports the module of the given name.
     *
     * <h4>Memory management</h4>
     * Caller shall ensure that {@link PyObject#close()} will be invoked on the returned object.
     * It shall be the last field set in {@link Environment}, which should itself be allocated
     * inside the {@code try} statement of try-with-resources.
     *
     * @param  name  name of the module to import.
     * @return module of the given name.
     */
    final PyObject importModule(final String name) {
        MemorySegment pointer = null;
        try (final Arena local = Arena.ofConfined()) {
            final MemorySegment namePtr = local.allocateFrom(name);
            final int state = (int) lock.invokeExact();
            try {
                pointer = (MemorySegment) importModule.invokeExact(namePtr);
            } finally {
                unlock.invokeExact(state);
            }
            if (isNull(pointer)) {
                throw new RuntimeException("Cannot import the \"" + name + "\" module.");
            }
            return new PyObject(this, pointer);
        } catch (Throwable e) {
            throw propagate(e, pointer);
        }
    }

    /**
     * Calls a method or callable object with the given arguments.
     * This method never returns {@code null}. If there is no return value,
     * then the returned object is {@code Py_None} of the C <abbr>API</abbr>.
     *
     * <p>For every argument at index <var>i</var>, the argument value can be specified in two forms:
     * {@code python[i]} is checked first and, if non-null, used directly without looking at {@code java}.
     * Otherwise, {@code java[i]} is converted to a temporary Python object, stored into {@code python[i]},
     * then deallocated after the method call.</p>
     *
     * <h4>Memory management</h4>
     * Caller shall either use the returned object in a try-with-resource,
     * or register it as an action in a {@link java.lang.ref.Cleaner}, or
     * {@linkplain #close() close} directly (e.g. in case of error).
     *
     * @param  object  the object on which to call a method or read an attribute.
     * @param  method  the name of the attribute, method or callable object.
     * @param  python  Python objects to give in arguments, or {@code null} if reading an attribute.
     * @param  java    Java objects to convert to Python object, or {@code null} if none.
     * @return wrapper for the attribute value or result returned by the method.
     * @throws PythonException if the attribute, method or callable object was not found.
     */
    final PyObject call(final MemorySegment   object,
                        final String          method,
                        final MemorySegment[] python,
                        final Object[]        java)
    {
        MemorySegment result = null;
        try (final Arena local = Arena.ofConfined()) {
            final MemorySegment name = local.allocateFrom(method);
            final int state = (int) lock.invokeExact();
            try {
                result = (MemorySegment) getAttribute.invokeExact(object, name);
                if (isNull(result)) {
                    convertPythonException();
                    throw new PythonException("Cannot get the \"" + name + "\" attribute.");
                }
                /*
                 * If the caller requested an attribute, we are ready to convert `result` to a Java object.
                 * Otherwise, we must invoke the method or callable with the specified arguments.
                 */
                if (python != null || ((int) isCallable.invokeExact(result)) != 0) {
                    final MemorySegment callable = result;
                    try {
                        result = null;  // For preventing the reference to be decremented twice.
                        if (python == null || python.length == 0) {
                            result = (MemorySegment) callObject.invokeExact(callable, MemorySegment.NULL);
                            if (isNull(result)) {
                                convertPythonException();
                                throw new PythonException("Cannot call \"" + name + "\".");
                            }
                        } else {
                            final var tuple = (MemorySegment) newTuple.invokeExact(python.length);
                            if (tuple == null) {
                                convertPythonException();
                                throw new PythonException("Cannot call \"" + name + "\".");
                            }
                            int count = 0;  // Number of objects which will need to be deallocated.
                            try {
                                for (int i=0; i<python.length; i++) {
                                    MemorySegment pointer = python[i];
                                    if (pointer == null) {
                                        pointer = javaToPython(local, java[i]);
                                        python[count++] = pointer;
                                    }
                                    int status = (int) setTupleItem.invokeExact(tuple, i, pointer);
                                    if (status != 0) {
                                        convertPythonException();
                                    }
                                }
                                result = (MemorySegment) callObject.invokeExact(callable, tuple);
                                if (isNull(result)) {
                                    convertPythonException();
                                    throw new PythonException("Cannot call \"" + name + "\".");
                                }
                            } finally {
                                decrementRef.invokeExact(tuple);
                                while (--count >= 0) {
                                    decrementRef.invokeExact(python[count]);
                                }
                            }
                        }
                    } finally {
                        decrementRef.invokeExact(callable);
                    }
                }
            } finally {
                unlock.invokeExact(state);
            }
            return new PyObject(this, result);
        } catch (Throwable e) {
            throw propagate(e, result);
        }
    }

    /**
     * Converts the given Java object to a Python object.
     * This method always creates a new reference.
     * Callers are responsible to decrement the reference count.
     *
     * @param  local   arena for creating temporary objects.
     * @param  object  Java object to convert to Python.
     * @return memory address of the Python object. Must be deallocated by the caller.
     * @throws UnconvertibleTypeException if the given object is not convertible.
     */
    private MemorySegment javaToPython(final Arena local, final Object object) throws Throwable {
        final MemorySegment result;
        if (object instanceof Number value) {
            // Most probable types checked first.
            if (object instanceof Integer || object instanceof Long || object instanceof Short || object instanceof Byte) {
                result = (MemorySegment) fromLong.invokeExact(value.longValue());
            } else {
                result = (MemorySegment) fromDouble.invokeExact(value.doubleValue());
            }
        } else if (object instanceof CharSequence) {
            MemorySegment value = local.allocateFrom(object.toString());
            result = (MemorySegment) fromUTF8.invokeExact(value);
        } else {
            throw new UnconvertibleTypeException("Cannot convert Java object of class "
                        + object.getClass().getSimpleName() + " to Python object.");
        }
        if (isNull(result)) {
            convertPythonException();
            throw new PythonException("Cannot create the Python object.");
        }
        return result;
    }

    /**
     * Returns the given Python object as an integer.
     */
    final long getLong(final MemorySegment object) {
        final long result;
        try {
            final int state = (int) lock.invokeExact();
            try {
                result = (long) asLong.invokeExact(object);
                convertPythonException();
            } finally {
                unlock.invokeExact(state);
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
        return result;
    }

    /**
     * Returns the given Python object as a floating point number.
     */
    final double getDouble(final MemorySegment object) {
        final double result;
        try {
            final int state = (int) lock.invokeExact();
            try {
                result = (double) asDouble.invokeExact(object);
                convertPythonException();
            } finally {
                unlock.invokeExact(state);
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
        return result;
    }

    /**
     * Returns the given Python object as a character string.
     */
    final String getString(final MemorySegment object) {
        try {
            final int state = (int) lock.invokeExact();
            try {
                var result = (MemorySegment) asUTF8.invokeExact(object);
                if (isNull(result)) {
                    convertPythonException();
                    throw new PythonException("Cannot get the character string.");
                }
                return toString(result);
            } finally {
                unlock.invokeExact(state);
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    /**
     * Returns the value of a native function returning a null-terminated {@code char*}.
     * The string is assumed encoded in UTF-8.
     *
     * @param  result  the result of a native method call, or {@code null}.
     * @return the result as a string, or {@code null} if the result was null.
     */
    private static String toString(final MemorySegment result) {
        return isNull(result) ? null : result.reinterpret(Integer.MAX_VALUE).getString(0);
    }

    /**
     * Returns whether the given result is null.
     *
     * @param  result  the result of a native method call, or {@code null}.
     * @return whether the given result is null or a C/C++ {@code NULL}.
     */
    private static boolean isNull(final MemorySegment result) {
        return (result == null) || result.address() == 0;
    }

    /**
     * Propagates the given exception as an unchecked exception. This method should be invoked only
     * for exceptions thrown during calls to native methods by {@code MethodHandler.invokeExact(…)},
     * because it assumes that no checked exception should be thrown.
     *
     * @param  exception  the exception thrown by {@code MethodHandler.invokeExact(…)}.
     * @return the exception to be thrown by the caller.
     */
    private static RuntimeException propagate(final Throwable exception) {
        switch (exception) {
            case Error e:            throw e;
            case RuntimeException e: return e;
            case IOException e:      return new UncheckedIOException(e);
            default: return new UndeclaredThrowableException(exception);
        }
    }

    /**
     * Decrements the reference count of the given object, then propagates the given exception.
     *
     * @param  exception  the exception thrown by {@code MethodHandler.invokeExact(…)}.
     * @param  object     the object to release, or {@code null} if none.
     * @return the exception to be thrown by the caller.
     */
    private RuntimeException propagate(final Throwable exception, final MemorySegment object) {
        if (object != null) try {
            decrementRef.invokeExact(object);
        } catch (Throwable e) {
            exception.addSuppressed(e);
        }
        return propagate(exception);
    }

    /**
     * If Python raised an exception, rethrows as a Java exception.
     * Otherwise does nothing. The error flag is cleared by this method.
     *
     * @throws PythonException the Python exception converted to a Java exception.
     */
    private void convertPythonException() throws Throwable {
        final var exception = (MemorySegment) errorOccurred.invokeExact();
        if (exception != null) try {
            var text = (MemorySegment) asUTF8.invokeExact(exception);
            throw new PythonException(toString(text));
        } finally {
            decrementRef.invokeExact(exception);
        }
    }

    /**
     * Release a strong reference to object at the specified address.
     * It indicates that the reference is no longer used and may free
     * the memory if the reference count reaches zero.
     *
     * @see PyObject#close()
     */
    final void decrementRef(final MemorySegment object) {
        if (!closed) try {
            final int state = (int) lock.invokeExact();
            try {
                decrementRef.invokeExact(object);
            } finally {
                unlock.invokeExact(state);
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    /**
     * Shutdowns CPython then unloads the native library.
     * According CPython documentation, this method should be invoked in the same thread
     * than the one that initialized CPython, but this is difficult to ensure.
     */
    final void shutdown() {
        if (initializedByUs) try {
            closed = true;
            // No need to check if already closed because this is a no-op after the first time.
            lookup("Py_Finalize", FunctionDescriptor.ofVoid()).invokeExact();
        } catch (Throwable e) {
            throw propagate(e);
        }
        /*
         * We would like to invoke `arena.close()` here, but it causes SIGSEGV (0xb)
         * in C library: [libc.so.6+0x6f8f0]  __GI___nptl_deallocate_tsd+0xb0.
         */
    }
}
