package de.materna.util;

import java.util.stream.Stream;

@FunctionalInterface
public interface TailCall<T> {
  TailCall<T> apply();

  default boolean isComplete() {
    return false;
  }

  default T result() {
    return null;
  }

  default T invoke() {
    return Stream.iterate(this, TailCall::apply)
        .filter(TailCall::isComplete)
        .findFirst()
        .get()
        .result();
  }
}
