package de.materna.util;

/**
 * inspired by Venkat Subramaniam
 * @see TailCall
 */
public class TailRec {
  private TailRec() {

  }

  public static <T> TailCall<T> call(TailCall<T> nextCall) {
    return nextCall;
  }

  public static <T> TailCall<T> done(T value) {
    return new TailCall<T>() {
      @Override public TailCall<T> apply() {
        return null;
      }

      @Override public boolean isComplete() {
        return true;
      }

      @Override public T result() {
        return value;
      }
    };
  }
}
