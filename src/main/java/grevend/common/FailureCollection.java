/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.common;

import grevend.sequence.Seq;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FailureCollection<E> implements ResultCollection<E>, Failure<Collection<E>> {

    private final Failure<E> failure;

    @Contract(pure = true)
    private FailureCollection(@NotNull Failure<?> failure) {
        this.failure = Failure.of(failure);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <E> @NotNull FailureCollection<E> of(@NotNull Failure<?> failure) {
        return new FailureCollection<>(failure);
    }

    @NotNull
    @Override
    public Throwable reason() {
        return this.failure.reason();
    }

    /**
     * {@inheritDoc}
     *
     * @return the number of elements in this collection
     */
    @Override
    @Contract(pure = true)
    public int size() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if this collection contains no elements
     */
    @Override
    @Contract(pure = true)
    public boolean isEmpty() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param o element whose presence in this collection is to be tested
     *
     * @return {@code true} if this collection contains the specified element
     *
     * @throws ClassCastException   if the type of the specified element is incompatible with this
     *                              collection (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this collection does not
     *                              permit null elements (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    @Contract(pure = true)
    public boolean contains(Object o) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return an {@code Iterator} over the elements in this collection
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public Iterator<E> iterator() {
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                return null;
            }

        };
    }

    /**
     * {@inheritDoc}
     *
     * @return an array, whose {@linkplain Class#getComponentType runtime component type} is {@code
     * Object}, containing all of the elements in this collection
     *
     * @apiNote This method acts as a bridge between array-based and collection-based APIs. It
     * returns an array whose runtime type is {@code Object[]}. Use {@link #toArray(Object[])
     * toArray(T[])} to reuse an existing array.
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * @param a the array into which the elements of this collection are to be stored, if it is big
     *          enough; otherwise, a new array of the same runtime type is allocated for this
     *          purpose.
     *
     * @return an array containing all of the elements in this collection
     *
     * @throws ArrayStoreException  if the runtime type of any element in this collection is not
     *                              assignable to the {@linkplain Class#getComponentType runtime
     *                              component type} of the specified array
     * @throws NullPointerException if the specified array is null
     * @apiNote This method acts as a bridge between array-based and collection-based APIs. It
     * allows an existing array to be reused under certain circumstances. Use {@link #toArray()} to
     * create an array whose runtime type is {@code Object[]}.
     *
     * <p>Suppose {@code x} is a collection known to contain only strings.
     * The following code can be used to dump the collection into a previously allocated {@code
     * String} array:
     *
     * <pre>
     *     String[] y = new String[SIZE];
     *     ...
     *     y = x.toArray(y);</pre>
     *
     * <p>The return value is reassigned to the variable {@code y}, because a
     * new array will be allocated and returned if the collection {@code x} has too many elements to
     * fit into the existing array {@code y}.
     *
     * <p>Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public <T> T[] toArray(@NotNull T[] a) {
        return a;
    }

    /**
     * {@inheritDoc}
     *
     * @param e element whose presence in this collection is to be ensured
     *
     * @return {@code true} if this collection changed as a result of the call
     *
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by this
     *                                       collection
     * @throws ClassCastException            if the class of the specified element prevents it from
     *                                       being added to this collection
     * @throws NullPointerException          if the specified element is null and this collection
     *                                       does not permit null elements
     * @throws IllegalArgumentException      if some property of the element prevents it from being
     *                                       added to this collection
     * @throws IllegalStateException         if the element cannot be added at this time due to
     *                                       insertion restrictions
     */
    @Override
    @Contract(pure = true)
    public boolean add(E e) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param o element to be removed from this collection, if present
     *
     * @return {@code true} if an element was removed as a result of this call
     *
     * @throws ClassCastException            if the type of the specified element is incompatible
     *                                       with this collection (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this collection
     *                                       does not permit null elements (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by
     *                                       this collection
     */
    @Override
    @Contract(pure = true)
    public boolean remove(Object o) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param c collection to be checked for containment in this collection
     *
     * @return {@code true} if this collection contains all of the elements in the specified
     * collection
     *
     * @throws ClassCastException   if the types of one or more elements in the specified collection
     *                              are incompatible with this collection (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one or more null elements
     *                              and this collection does not permit null elements (<a
     *                              href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null.
     * @see #contains(Object)
     */
    @Override
    @Contract(pure = true)
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param c collection containing elements to be added to this collection
     *
     * @return {@code true} if this collection changed as a result of the call
     *
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by
     *                                       this collection
     * @throws ClassCastException            if the class of an element of the specified collection
     *                                       prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a null element and
     *                                       this collection does not permit null elements, or if
     *                                       the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the specified
     *                                       collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at this time due
     *                                       to insertion restrictions
     * @see #add(Object)
     */
    @Override
    @Contract(pure = true)
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param c collection containing elements to be removed from this collection
     *
     * @return {@code true} if this collection changed as a result of the call
     *
     * @throws UnsupportedOperationException if the {@code removeAll} method is not supported by
     *                                       this collection
     * @throws ClassCastException            if the types of one or more elements in this collection
     *                                       are incompatible with the specified collection (<a
     *                                       href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more null elements
     *                                       and the specified collection does not support null
     *                                       elements (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    @Contract(pure = true)
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param c collection containing elements to be retained in this collection
     *
     * @return {@code true} if this collection changed as a result of the call
     *
     * @throws UnsupportedOperationException if the {@code retainAll} operation is not supported by
     *                                       this collection
     * @throws ClassCastException            if the types of one or more elements in this collection
     *                                       are incompatible with the specified collection (<a
     *                                       href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more null elements
     *                                       and the specified collection does not permit null
     *                                       elements (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    @Contract(pure = true)
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException if the {@code clear} operation is not supported by this
     *                                       collection
     */
    @Override
    @Contract(pure = true)
    public void clear() {}

    @NotNull
    @Override
    public <S extends Seq<E, S>> Seq<E, S> sequence() {
        return Seq.empty();
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public Supplier<ResultCollection<E>> factory() {
        return SuccessCollection::new;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        return "FailureCollection{failure=" + this.failure + '}';
    }

}
