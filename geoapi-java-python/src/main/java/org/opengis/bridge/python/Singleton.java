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

import java.util.List;
import java.lang.reflect.Type;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationHandler;
import org.opengis.annotation.UML;
import org.jpy.PyObject;


/**
 * Delegates Java method calls on a single GeoAPI object to the equivalent Python object.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 4.0
 * @since   4.0
 */
final class Singleton implements InvocationHandler {
    /**
     * Information about the Python environment (builtin functions, etc).
     */
    private final Environment environment;

    /**
     * The Python object to wrap in a Java object.
     */
    private final PyObject object;

    /**
     * Creates a new handler for the given Python object.
     */
    private Singleton(final Environment environment, final PyObject object) {
        this.environment = environment;
        this.object      = object;
    }

    /**
     * Wraps the given Python object in a Java object of the given type.
     * The given type should be a GeoAPI interface.
     *
     * @param <T>     compile-time value of the {@code type} argument.
     * @param object  the Python object to wrap in a Java object.
     * @param type    interface to be implemented by the desired Java wrapper.
     */
    static <T> T create(final Environment environment, final PyObject object, final Class<T> type) {
        return type.cast(Proxy.newProxyInstance(Singleton.class.getClassLoader(),
                    new Class<?>[] {type}, new Singleton(environment, object)));
    }

    /**
     * Returns the {@code identifier()} value of the given annotation or {@code null} if none or empty.
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
     * @param  args    arguments to transfer to the Python method.
     * @return the result of the invocation of the Python method.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, Object[] args) {
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
                        return environment.builtins.call("str", object).getStringValue();
                    } else if (name.equals("hashCode")) {
                        return object.hashCode();
                    }
                    break;
                }
                case 1: {
                    if (name.equals("equals")) {
                        final Object arg = args[0];
                        if (arg != null && arg.getClass() == proxy.getClass()) {
                            return object.equals(((Singleton) Proxy.getInvocationHandler(arg)).object);
                        } else {
                            return false;
                        }
                    }
                    break;
                }
            }
        }
        /*
         * If there is arguments, convert all of them from Java to Python objects. If some argument cannot
         * be converted, they will be left as-is. They may cause an exception to be thrown at callMethod(…)
         * execution time, depending on JPY implementation.
         */
        name = CharSequences.camelCaseToSnake(name);
        final PyObject result;
        if (args != null) {
            for (int i=0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    if (arg instanceof CharSequence) {
                        arg = arg.toString();
                    } else if (arg instanceof Number) {
                        continue;                           // Assuming a wrapper for a primitive type, there is nothing to do.
                    } else if (Proxy.isProxyClass(arg.getClass())) {
                        final InvocationHandler h = Proxy.getInvocationHandler(arg);
                        if (!(h instanceof Singleton)) continue;
                        arg = ((Singleton) h).object;
                    } else {
                        continue;
                    }
                    args[i] = arg;
                }
            }
            result = object.callMethod(name, args);
        } else {
            result = object.getAttribute(name);
        }
        /*
         * Convert the result of the Python method call to the type expected by the Java method.
         * This may be a collection, in which case each element will be converted on-the-fly.
         */
        Class<?> type = method.getReturnType();
        if (Iterable.class.isAssignableFrom(type)) {
            if (result != null) {
                type = boundOfParameterizedProperty(method.getGenericReturnType());
                return new Sequence<>(environment, type, result);
            } else {
                return List.of();
            }
        } else if (object.equals(result)) {
            // Slight optimization: share the same InvocationHandler if the Python object is the same.
            if (getClass().equals(proxy.getClass())) {
                return this;
            } else {
                return Proxy.newProxyInstance(Singleton.class.getClassLoader(), new Class<?>[] {type}, this);
            }
        } else {
            return Converter.instance(environment, type).apply(result);
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
                    break;                              // Unknown type.
                }
            }
        }
        throw new UnconvertibleTypeException(type);
    }
}
