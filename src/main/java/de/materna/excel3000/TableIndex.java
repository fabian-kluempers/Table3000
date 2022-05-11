package de.materna.excel3000;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.math.IntMath;
import de.materna.util.StreamUtil;
import de.materna.util.TailCall;
import de.materna.util.TailRec;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TableIndex {
  private static final BiMap<Character, Integer> alphabet = StreamUtil.zip(IntStream.rangeClosed('a', 'z').boxed(), Stream.iterate(1, i -> ++i))
      .reduce(
          new ImmutableBiMap.Builder<Character, Integer>(),
          (builder, pair) -> builder.put((char) pair.first.intValue(), pair.second),
          (builder1, builder2) -> builder1.putAll(builder2.build())
      ).build();

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableIndex that = (TableIndex) o;
    return row == that.row && col == that.col;
  }

  @Override public int hashCode() {
    return Objects.hash(row, col);
  }

  public static final Pattern INDEXING_PATTERN = Pattern.compile("([a-zA-Z]+)([1-9][0-9]*)");

  public final int row;
  public final int col;

  private TableIndex(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public static TableIndex of(int row, int col) {
    return new TableIndex(row, col);
  }

  public static TableIndex ofExcelFormat(String value) {
    return Optional.of(INDEXING_PATTERN.matcher(value))
        .filter(Matcher::matches)
        .map(matcher -> TableIndex.of(getColIterative(matcher.group(1).toLowerCase()), Integer.parseInt(matcher.group(2))))
        .orElseThrow(() -> new IllegalArgumentException("Value " + value + " does not match pattern " + INDEXING_PATTERN.pattern()));
  }

  private static int getColIterative(String chars) {
    int col = 0;
    for (int i = 0; i < chars.length(); i++) {
      col += IntMath.pow(alphabet.size(), chars.length() - i - 1) * alphabet.get(chars.charAt(i));
    }
    return col;
  }


  private static int getCol(String chars) {
    return getCol(chars, 0).invoke();
  }

  private static TailCall<Integer> getCol(String chars, int i) {
    if (chars.length() == 0) {
      return TailRec.done(i);
    }
    return TailRec.call(() ->
        getCol(
            chars.substring(1),
            i + IntMath.pow(alphabet.size(), chars.length() - 1) * alphabet.get(chars.charAt(0))
        )
    );
  }

  public String toExcelFormat() {
    //TODO broken
    return null;
  }

  @Override public String toString() {
    return "TableIndex{" +
        "row=" + row +
        ", col=" + col +
        '}';
  }
}
