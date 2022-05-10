package de.materna.excel300;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.math.IntMath;
import de.materna.util.TailCall;
import de.materna.util.TailRec;

import io.vavr.collection.Stream;
import io.vavr.control.Option;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableIndex {
  private static final BiMap<Character, Integer> alphabet = Stream.rangeClosed('a', 'z')
      .zip(Stream.iterate(1, i -> i + 1))
      .foldLeft(new ImmutableBiMap.Builder<Character, Integer>(), (map, value) -> map.put(value.toEntry()))
      .build();

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableIndex that = (TableIndex) o;
    return row == that.row && col == that.col;
  }

  @Override public int hashCode() {
    return Objects.hash(row, col);
  }

  public static final Pattern INDEXING_PATTERN = Pattern.compile("([a-zA-Z]+)([0-9]+)");

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
    return Option.of(INDEXING_PATTERN.matcher(value))
        .filter(Matcher::matches)
        .map(matcher -> TableIndex.of(getColIterative(matcher.group(1).toLowerCase()), Integer.parseInt(matcher.group(2))))
        .getOrElseThrow(() -> new IllegalArgumentException("Value " + value + " does not match pattern " + INDEXING_PATTERN.pattern()));
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
    List list = new ArrayList<Character>();
    int tempCol = col;
    while (tempCol > 1) {
      list.add(alphabet.inverse().get(tempCol % alphabet.size()));
      tempCol %= alphabet.size();
    }
    return list.toString();
  }

  @Override public String toString() {
    return "TableIndex{" +
        "row=" + row +
        ", col=" + col +
        '}';
  }
}
