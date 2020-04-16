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

package grevend.persistencelite.util;

import grevend.jacoco.Generated;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Option<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 5775395636702270419L;
    private final T value;

    @Contract(pure = true)
    private Option() {
        this.value = null;
    }

    @Contract(pure = true)
    private Option(T value) {
        this.value = value;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull <T extends Serializable> Option<T> empty() {
        return new Option<T>();
    }

    @Contract("!null -> new")
    public static @NotNull <T extends Serializable> Option<T> of(T value) {
        return value == null ? empty() : new Option<>(value);
    }

    public static @NotNull <T extends Serializable> Option<T> ofOptional(@NotNull Optional<T> value) {
        return value.isEmpty() ? empty() : new Option<>(value.get());
    }

    public @NotNull T get() {
        if (this.value == null) {
            throw new NoSuchElementException("No value present");
        } else {
            return this.value;
        }
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public boolean isEmpty() {
        return this.value == null;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Option<?> option = (Option<?>) o;
        return Objects.equals(this.value, option.value);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public String toString() {
        return this.value != null ? String.format("Option[%s]", this.value) : "Option.empty";
    }

}
