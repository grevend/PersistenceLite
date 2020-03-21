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

package grevend.persistence.lite.util.sequence;

import grevend.persistence.lite.util.TriFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public interface Seq<T> {

  static @NotNull <T> Seq<T> empty() {
    return () -> new Iterator<>() {

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public T next() {
        return null;
      }

    };
  }

  static @NotNull <T> Seq<T> of(@NotNull Iterator<T> iterator) {
    return () -> iterator;
  }

  static @NotNull <T> Seq<T> of(@NotNull Iterable<T> iterable) {
    return of(iterable.iterator());
  }

  @SafeVarargs
  static @NotNull <T> Seq<T> of(T... values) {
    return of(Arrays.asList(values));
  }

  static @NotNull <T, S> Seq<T> range(@NotNull T startInclusive,
      @NotNull T endInclusive, @NotNull TriFunction<T, T, S, T> stepper, S stepSize) {
    return new RangeSeq<>(startInclusive, endInclusive, stepper, stepSize);
  }

  static @NotNull Seq<Integer> range(int startInclusive, int endInclusive, int stepSize) {
    return range(startInclusive, endInclusive,
        startInclusive < endInclusive ? (current, end, step) -> {
          var value = current + step;
          return value > end ? end : value;
        } : (current, end, step) -> {
          var value = current - step;
          return value < end ? end : value;
        }, stepSize);
  }

  static @NotNull Seq<Integer> range(int startInclusive, int endInclusive) {
    return range(startInclusive, endInclusive, 1);
  }

  static @NotNull Seq<Double> range(double startInclusive, double endInclusive, double stepSize) {
    return range(startInclusive, endInclusive,
        startInclusive < endInclusive ? (current, end, step) -> {
          var value = current + step;
          return value > end ? end : value;
        } : (current, end, step) -> {
          var value = current - step;
          return value < end ? end : value;
        }, stepSize);
  }

  static @NotNull Seq<Double> range(double startInclusive, double endInclusive) {
    return range(startInclusive, endInclusive, 1.0);
  }

  static @NotNull Seq<Character> range(char startInclusive, char endInclusive, int stepSize) {
    return range((int) startInclusive, (int) endInclusive, stepSize)
        .map(character -> (char) character.intValue());
  }

  static @NotNull Seq<Character> range(char startInclusive, char endInclusive) {
    return range(startInclusive, endInclusive, 1);
  }

  static @NotNull <T> Seq<T> generate(@NotNull Supplier<T> supplier) {
    return new GeneratorSeq<>(supplier);
  }

  @NotNull Iterator<T> iterator();

  default @NotNull Seq<T> filter(@NotNull Predicate<? super T> predicate) {
    return new FilterSeq<>(this, predicate);
  }

  default @NotNull <R> Seq<R> map(@NotNull Function<? super T, ? extends R> function) {
    return new MapSeq<>(this, function);
  }

  default @NotNull <R> Seq<R> flatMap(
      @NotNull Function<? super T, ? extends Seq<? extends R>> function) {
    return new FlatMapSeq<>(this, function);
  }

  default @NotNull T reduce(@NotNull T identity, @NotNull BinaryOperator<T> accumulator) {
    var current = identity;
    var iterator = this.iterator();
    if (iterator.hasNext()) {
      while (iterator.hasNext()) {
        current = accumulator.apply(current, iterator.next());
      }
    }
    return current;
  }

  default @NotNull Optional<T> reduce(@NotNull BinaryOperator<T> accumulator) {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    } else {
      var current = iterator.next();
      if (iterator.hasNext()) {
        while (iterator.hasNext()) {
          current = accumulator.apply(current, iterator.next());
        }
      }
      return Optional.ofNullable(current);
    }
  }

  default @NotNull Seq<T> peek(@NotNull Consumer<T> consumer) {
    return new PeekSeq<>(this, consumer);
  }

  default @NotNull Seq<T> distinct() {
    return new DistinctSeq<>(this);
  }

  default @NotNull Seq<T> sorted() {
    return this.sorted(new GenericComparator<>());
  }

  default @NotNull Seq<T> sorted(@NotNull Comparator<? super T> comparator) {
    var list = this.toList();
    list.sort(comparator);
    return of(list);
  }

  default @NotNull Seq<T> reversed() {
    var list = this.toList();
    Collections.reverse(list);
    return of(list);
  }

  default @NotNull Seq<T> limit(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Value of maxSize must be greater then 0.");
    }
    return new LimitSeq<>(this, maxSize);
  }

  default @NotNull Seq<T> skip(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Value of maxSize must be greater then 0.");
    }
    return new SkipSeq<>(this, maxSize);
  }

  default void forEach(@NotNull Consumer<? super T> consumer) {
    var iterator = this.iterator();
    while (iterator.hasNext()) {
      consumer.accept(iterator.next());
    }
  }

  default void forEach(@NotNull BiConsumer<? super T, Integer> consumer) {
    var iterator = this.iterator();
    var i = 0;
    while (iterator.hasNext()) {
      consumer.accept(iterator.next(), i);
      i++;
    }
  }

  default @NotNull <R, A> R collect(@NotNull Collector<? super T, A, R> collector) {
    var iterator = this.iterator();
    var resultContainer = collector.supplier().get();
    var accumulator = collector.accumulator();
    while (iterator.hasNext()) {
      accumulator.accept(resultContainer, iterator.next());
    }
    return collector.finisher().apply(resultContainer);
  }

  default @NotNull Seq<T> concat(@NotNull Seq<? extends T>... sequences) {
    return new ConcatSeq<>(this, sequences);
  }

  default @NotNull Seq<T> merge(@NotNull Seq<? extends T>... sequences) {
    return new MergeSeq<>(this, sequences);
  }

  default @NotNull List<T> toList() {
    return this.collect(Collectors.toList());
  }

  default @NotNull List<T> toUnmodifiableList() {
    return this.collect(Collectors.toUnmodifiableList());
  }

  default @NotNull Set<T> toSet() {
    return this.collect(Collectors.toSet());
  }

  default @NotNull Set<T> toUnmodifiableSet() {
    return this.collect(Collectors.toUnmodifiableSet());
  }

  default @NotNull String joining() {
    return this.map(T::toString).collect(Collectors.joining());
  }

  default @NotNull String joining(@NotNull CharSequence delimiter) {
    return this.map(T::toString).collect(Collectors.joining(delimiter));
  }

  default @NotNull Optional<T> min(@NotNull Comparator<? super T> comparator) {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    var min = iterator.next();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (comparator.compare(element, min) < 0) {
        min = element;
      }
    }
    return Optional.ofNullable(min);
  }

  default @NotNull Optional<T> max(@NotNull Comparator<? super T> comparator) {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    var max = iterator.next();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (comparator.compare(max, element) > 0) {
        max = element;
      }
    }
    return Optional.ofNullable(max);
  }

  default @NotNull Optional<T> findFirst() {
    var iterator = this.iterator();
    return !iterator.hasNext() ? Optional.empty() : Optional.ofNullable(iterator.next());
  }

  default @NotNull Optional<T> findAny() {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    T last = iterator.next();
    while (last == null && iterator.hasNext()) {
      last = iterator.next();
    }
    return Optional.ofNullable(last);
  }

  default @NotNull Optional<T> findLast() {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    T last = iterator.next();
    while (iterator.hasNext()) {
      last = iterator.next();
    }
    return Optional.ofNullable(last);
  }

  default int count() {
    var count = 0;
    while (this.iterator().hasNext()) {
      count++;
      this.iterator().next();
    }
    return count;
  }

  default boolean anyMatch(@NotNull Predicate<? super T> predicate) {
    var iterator = new MapSeq<>(this, predicate::test).iterator();
    while (iterator.hasNext()) {
      if (iterator.next()) {
        return true;
      }
    }
    return false;
  }

  default boolean allMatch(@NotNull Predicate<? super T> predicate) {
    return !this.anyMatch(predicate.negate());
  }

  default boolean noneMatch(@NotNull Predicate<? super T> predicate) {
    return !this.allMatch(predicate);
  }

  class GenericComparator<T> implements Comparator<T> {

    @Override
    @SuppressWarnings("unchecked")
    public int compare(T a, T b) {
      if (a instanceof Comparable) {
        return ((Comparable<T>) a).compareTo(b);
      }
      return 0;
    }

  }

}