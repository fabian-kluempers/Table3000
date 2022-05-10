package de.materna.excel3000;

import com.google.common.collect.Maps;
import io.vavr.control.Option;

import java.util.*;

public class Excel3000 {
  private final Map<TableIndex, String> table;

  public Excel3000() {
    this.table = Maps.newHashMap();
  }

  public void setCell(int row, int col, String value) {
    setCell(TableIndex.of(row, col), value);
  }

  public void setCell(TableIndex index, String value) {
    table.put(index, value);
  }

  public Option<String> getCell(int row, int col) {
    return getCell(TableIndex.of(row, col));
  }

  public Option<String> getCell(TableIndex index) {
    return Option.of(table.get(index));
  }

  public Option<String> getCell(String index) {
    return getCell(TableIndex.ofExcelFormat(index));
  }

  /**
   * @param index
   * @param value
   * @throws IllegalArgumentException
   */
  public void setCell(String index, String value) {
    setCell(TableIndex.ofExcelFormat(index), value);
  }

  public Excel3000 evaluate() {
    Excel3000 result = new Excel3000();
    Deque<Map.Entry<TableIndex, String>> work = new ArrayDeque<>(table.entrySet());
    while (!work.isEmpty()) {
      Map.Entry<TableIndex, String> current = work.remove();
      String formula = current.getValue();
      if (ExpressionUtil.isFormula(formula)) {
        // get vars
        Set<String> vars = ExpressionUtil.getVars(formula, TableIndex.INDEXING_PATTERN);
        formula = ExpressionUtil.toCanonicalForm(formula);

        // calc var assignments
        Map<String, Double> assignments = new HashMap<>();
        for (String variable : vars) {
          assert (!ExpressionUtil.isFormula(variable));
          assignments.put(variable, ExpressionUtil.evaluate(getCell(variable).get()));
          result.setCell(variable, String.valueOf(assignments.get(variable)));
        }

        // calc formula
        result.setCell(current.getKey(), String.valueOf(ExpressionUtil.evaluate(formula, assignments)));
      } else {
        // calc expression
        result.setCell(current.getKey(), String.valueOf(ExpressionUtil.evaluate(formula)));
      }
    }
    return result;
  }
}
