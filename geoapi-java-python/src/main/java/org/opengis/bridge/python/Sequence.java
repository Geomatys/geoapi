/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    Copyright © 2006-2024 Open Geospatial Consortium, Inc.
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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.AbstractSequentialList;
import java.util.NoSuchElementException;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Array;


/**
 * A Python sequence, represented as a read-only Java list. While we are mapping a sequence
 * (the main collection type used in GeoAPI Python interfaces), this implementation accepts
 * any container capable to provide an iterator.
 *
 * @author  Martin Desruisseaux (Geomatys)
 *
 * @param <E> Type of elements in the sequence.
 */
final class Sequence<E> extends AbstractSequentialList<E> implements Wrapper {
    /**
     * Information about the Python environment (builtin functions, etc).
     * The same instance is shared by all {@code Sequence}.
     */
    private final Environment environment;

    /**
     * The Python sequence. Can actually be any collection capable to provide an iterator.
     * This object shall have no reference to {@code this} in order to allow
     * the Python object to be released when {@code this} is garbage-collected.
     */
    private final PyObject collection;

    /**
     * Function to apply on each element in the list for
     * converting from Python objects to Java objects.
     *
     * @todo This is not sufficient, unless {@code <E>} is a final class.
     *       Each element of the list could be a different subtype.
     */
    private final Converter<? extends E> converter;

    /**
     * Iterator over the elements returned by {@link #get(int)}, cached for performance reasons.
     * This is {@code null} if not yet requested.
     */
    private transient Iterator<E> getter;

    /**
     * Index of the next element to be returned by {@link #getter}.
     */
    private transient int nextGetIndex;

    /**
     * Creates a new sequence for the given Python collection.
     *
     * @param type        the class of elements in this list.
     * @param collection  the Python sequence. Can actually be any collection capable to provide an iterator.
     */
    Sequence(final Environment environment, final Class<E> type, final PyObject collection) {
        this.environment = environment;
        this.collection  = collection;
        this.converter   = Converter.fromPythonToJava(environment, type);
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
        return collection.address(bindings);
    }

    /**
     * Returns the length of the Python sequence wrapped by this list.
     */
    @Override
    public int size() {
        try (PyObject length = environment.builtins.call("len", collection)) {
            return length.getIntValue();
        }
    }

    /**
     * Returns the element at the given index.
     * This method is optimized for accesses with increasing indices.
     */
    @Override
    public E get(final int index) {
        if (index >= 0) {
            if (getter == null || index < nextGetIndex) {
                getter = iterator();
                nextGetIndex = 0;
            }
            try {
                E element;
                do {
                    element = getter.next();
                } while (++nextGetIndex <= index);
                return element;
            } catch (NoSuchElementException e) {
                getter = null;
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException(index).initCause(e);
            }
        }
        throw new IndexOutOfBoundsException(index);
    }

    /**
     * Returns an iterator over the elements in the Python sequence.
     * This method is preferred to {@link #listIterator()} if there is no need to move backward.
     */
    @Override
    public Iterator<E> iterator() {
        return iterator(false);
    }

    /**
     * Returns a bidirectional iterator over the elements in the Python sequence.
     */
    @Override
    public ListIterator<E> listIterator() {
        return (BIter) iterator(true);
    }

    /**
     * Returns a bidirectional iterator over the elements in the Python sequence starting at the given index.
     *
     * @param index  index of first element to be returned from the list.
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index);
        }
        final ListIterator<E> iterator = listIterator();
        if (index != 0) try {
            do iterator.next();
            while (--index != 0);
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
        return iterator;
    }

    /**
     * Creates a new iterator or list iterator.
     *
     * @param  list  whether to create a list iterator instead of an iterator.
     * @return the iterator.
     */
    private Iter iterator(final boolean list) {
        try (final PyObject iter = environment.builtins.call("iter", collection)) {
            final Iter wrapper = list ? new BIter(iter) : new Iter(iter);
            environment.cleaner.register(wrapper, iter);
            return wrapper;
        }
    }

    /**
     * Implementation of the iterator returned by {@link Sequence#iterator()}.
     * This implementation does not cache anything.
     */
    private class Iter implements Iterator<E> {
        /**
         * The Python iterator.
         */
        private final PyObject iter;

        /**
         * The next object to return, or {@code null} if not yet determined.
         * Valid only if {@link #fetched} is {@code true}.
         */
        private E next;

        /**
         * Whether the next Python element to return has been stored in {@link #next}.
         * We use this flag because {@code null} may be a valid value for {@code next}.
         */
        private boolean fetched;

        /**
         * Whether the iteration is finished.
         */
        private boolean finished;

        /**
         * Creates a new iterator.
         */
        Iter(final PyObject iter) {
            this.iter = iter;
        }

        /**
         * Get the next element from Python iterator, or throws {@link RuntimeException} if there is no more elements.
         */
        private void fetch() {
            try (PyObject object = environment.builtins.call("next", iter)) {
                next = converter.apply(object);
            }
            fetched = true;
        }

        /**
         * Returns {@code true} if there is another object to return from the iterator.
         */
        @Override
        public final boolean hasNext() {
            if (!(fetched | finished)) try {
                fetch();
            } catch (RuntimeException e) {
                finished = true;
                requireStopIteration(e);
            }
            return fetched;
        }

        /**
         * Returns the next element in iteration order.
         */
        @Override
        public E next() {
            if (!(fetched | finished)) try {
                fetch();
            } catch (RuntimeException e) {
                finished = true;
                requireStopIteration(e);
                throw new NoSuchElementException(e);
            } else if (finished) {
                throw new NoSuchElementException();
            }
            return next;
        }

        /**
         * Re-throws the given exception if it is not a Python "StopIteration" exception.
         */
        private static void requireStopIteration(final RuntimeException e) {
            final String message = e.getMessage();
            if (message == null || !message.contains("StopIteration")) {
                throw e;
            }
        }

        /**
         * Unsupported operation.
         */
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Implementation of the iterator returned by {@link Sequence#listIterator()}.
     * This implementation caches the elements seen during the iteration, in case
     * the caller wants to invoke {@link #previous()}. We do not share the cache
     * between iterators because the content of the Python collection may change.
     */
    private final class BIter extends Iter implements ListIterator<E> {
        /**
         * Index of the next element to store in the {@link #elements} array.
         */
        private int nextIndex;

        /**
         * Index of the next element to fetch.
         */
        private int fetchIndex;

        /**
         * The elements as Java object, filled when first needed.
         * In the case of metadata objects, this array is usually short (typically of length 1).
         */
        private final E[] elements;

        /**
         * Creates a new iterator.
         */
        @SuppressWarnings("unchecked")
        BIter(final PyObject iter) {
            super(iter);
            elements = (E[]) Array.newInstance(converter.type, size());
        }

        /**
         * Returns the next element in the list and advances the cursor position.
         */
        @Override
        public E next() {
            final E element;
            if (nextIndex < fetchIndex) {
                element = elements[nextIndex++];
            } else {
                element = super.next();
                elements[fetchIndex] = element;
                nextIndex = ++fetchIndex;
            }
            return element;
        }

        /**
         * Returns the previous element in the list and moves the cursor position backwards.
         */
        @Override
        public E previous() {
            if (nextIndex != 0) {
                return elements[--nextIndex];
            }
            throw new NoSuchElementException();
        }

        @Override public int     nextIndex()     {return nextIndex;}
        @Override public int     previousIndex() {return nextIndex - 1;}
        @Override public boolean hasPrevious()   {return nextIndex != 0;}
        @Override public void    set(E e) {throw new UnsupportedOperationException();}
        @Override public void    add(E e) {throw new UnsupportedOperationException();}
    }
}
