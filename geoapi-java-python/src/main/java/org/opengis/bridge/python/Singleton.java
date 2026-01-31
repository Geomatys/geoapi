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

import java.util.List;
import java.lang.reflect.Type;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationHandler;
import java.lang.foreign.MemorySegment;
import org.opengis.annotation.UML;


/**
 * Delegates Java method calls from an instance of a GeoAPI type to the equivalent Python object.
 * This class is used for singleton. For list backed by a Python sequence, see {@link Sequence}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class Singleton implements Wrapper, InvocationHandler {
    /**
     * Information about the Python environment (builtin functions, etc).
     * The same instance is shared by all {@code Singleton}.
     */
    private final Environment environment;

    /**
     * The Python object to wrap in a Java object.
     * This object shall have no reference to {@code this} in order to allow
     * the Python object to be released when {@code this} is garbage-collected.
     */
    private final PyObject object;

    /**
     * Creates a new handler for the given Python object.
     */
    Singleton(final Environment environment, final PyObject object) {
        this.environment = environment;
        this.object = object;
    }

    /**
     * Creates a new proxy implementing the given interface with this handler.
     *
     * @param  type  interface to be implemented by the desired Java wrapper.
     * @return Java object implementing the given interface by delegating the the Python object.
     */
    final Object newProxyInstance(final Class<?> type) {
        return Proxy.newProxyInstance(
                Singleton.class.getClassLoader(),
                new Class<?>[] {type, Wrapper.class},
                this);
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
        return object.address(bindings);
    }

    /**
     * Returns the {@code identifier()} value of the given annotation, or {@code null} if none or empty.
     */
    private static String identifier(final UML uml) {
        if (uml != null) {
            final String id = uml.identifier();
            if (!id.isEmpty()) return id;
        }
        return null;
    }

    /**
     * Forwards a call to a method from the Java interface to the equivalent method or property in Python.
     *
     * @param  proxy   the proxy object on which a method has been invoked.
     * @param  method  the invoked Java method.
     * @param  args    arguments to transfer to the Python method, or {@code null} if the method takes no arguments.
     * @return the result of the invocation of the Python method.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        String name = identifier(method.getAnnotation(UML.class));
        if (name == null) {
            name = method.getName();
            /*
             * The Object.hashCode(), equals(Object) and toString() methods are dispatched here and
             * need to be handled in a special way (other Object methods are not dispatched here).
             * The toString() method delegates to Python string representation, and the equals(…)
             * method checks if the underlying Python objects are the same.
             */
            switch (args.length) {
                case 0: {
                    if (name.equals("toString")) {
                        try (PyObject str = environment.builtins.call("str", object)) {
                            return str.getStringValue();
                        }
                    } else if (name.equals("hashCode")) {
                        return object.hashCode();
                    }
                    break;
                }
                case 1: {
                    if (name.equals("address")) {
                        return object.address((CPython) args[0]);
                    } else if (name.equals("equals")) {
                        final Object arg = args[0];
                        if (arg != null && arg.getClass() == proxy.getClass()) {
                            return object.equals(((Singleton) Proxy.getInvocationHandler(arg)).object);
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                    break;
                }
            }
        }
        name = CharSequences.camelCaseToSnake(name);
        try (final PyObject python = object.convertAndCall(name, args)) {
            if (object.equals(python) && proxy.getClass() == getClass()) {
                return this;
            }
            /*
             * Convert the result of the Python method call to the type expected by the Java method.
             * This may be a collection, in which case each element will be converted on-the-fly.
             */
            final Object java;
            Class<?> type = method.getReturnType();
            if (Iterable.class.isAssignableFrom(type)) {
                if (python == null) {
                    return List.of();
                }
                type = boundOfParameterizedProperty(method.getGenericReturnType());
                java = new Sequence<>(environment, type, python);
                python.automaticRelease(environment.cleaner, java);
            } else {
                java = Converter.fromPythonToJava(environment, type).apply(python);
            }
            return java;
        }
    }

    /**
     * Returns the upper bounds of the parameterized type. For example, if a method returns {@code Collection<String>},
     * then {@code boundOfParameterizedProperty(method.getGenericReturnType())} should return {@code String.class}.
     */
    private static Class<?> boundOfParameterizedProperty(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] p = ((ParameterizedType) type).getActualTypeArguments();
            while (p != null && p.length == 1) {
                type = p[0];
                if (type instanceof WildcardType) {
                    p = ((WildcardType) type).getUpperBounds();
                } else {
                    if (type instanceof ParameterizedType) {
                        type = ((ParameterizedType) type).getRawType();
                    }
                    if (type instanceof Class<?>) {
                        return (Class<?>) type;
                    }
                    break;      // Unknown type.
                }
            }
        }
        throw new UnconvertibleTypeException(type);
    }
}
