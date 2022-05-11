package de.materna.util;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtil {
  private StreamUtil() {
    
  }

  public static <T, U, R> Stream<R> zipWith(
      Stream<T> as,
      Stream<U> bs,
      BiFunction<T, U, R> zipper
  ) {
    Iterator<U> itr = bs.iterator();
    return as.filter(a -> itr.hasNext()).map(a -> zipper.apply(a, itr.next()));
  }

  @SuppressWarnings("All") // suppress "can convert to record"
  static class Pair<T, U> {
    public final T first;
    public final U second;

    Pair(T first, U second) {
      this.first = first;
      this.second = second;
    }

    public <R> Pair<R, U> mapFirst(Function<T, R> mapper) {
      return new Pair<>(mapper.apply(first), second);
    }

    public <R> Pair<T, R> mapSecond(Function<U, R> mapper) {
      return new Pair<>(first, mapper.apply(second));
    }

    public Pair<U, T> flip() {
      return new Pair<>(second, first);
    }
  }

  public static <T> Stream<Pair<Integer, T>> zipWithIndex(Stream<T> stream) {
    return zipWith(
        IntStream.iterate(0, index -> index + 1).boxed(),
        stream,
        Pair::new
    );
  }

  public static <T, U> Stream<Pair<T, U>> zip(Stream<T> as, Stream<U> bs) {
    return zipWith(as, bs, Pair::new);
  }

  public static <T, U> Pair<Stream<T>, Stream<U>> unzip(Stream<Pair<T, U>> stream) {
    Stream.Builder<T> firstBuilder = Stream.builder();
    Stream.Builder<U> secondBuilder = Stream.builder();
    stream.forEach(pair -> {
      firstBuilder.add(pair.first);
      secondBuilder.add(pair.second);
    });
    return new Pair<>(firstBuilder.build(), secondBuilder.build());
  }
}
