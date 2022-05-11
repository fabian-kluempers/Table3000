package de.materna.excel3000;

import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;


public class Excel3000 {
  private final Map<TableIndex, Expression> table;

  public Excel3000() {
    this.table = Maps.newHashMap();
  }

  public void setCell(int row, int col, String value) {
    setCell(TableIndex.of(row, col), value);
  }

  private void setCell(TableIndex index, String value) {
    table.put(index, Expression.of(value));
  }

  private void setCell(TableIndex index, Expression expression) {
    table.put(index, expression);
  }

  public Optional<String> getCell(int row, int col) {
    return getCell(TableIndex.of(row, col)).map(Expression::toString);
  }

  private Optional<Expression> getCell(TableIndex index) {
    return Optional.ofNullable(table.get(index));
  }

  /**
   * @param index the {@link TableIndex}.
   * @return the value at the specified index or none.
   * @throws IllegalArgumentException if index does not conform to {@link TableIndex#INDEXING_PATTERN}.
   */
  public Optional<String> getCell(String index) {
    return getCell(TableIndex.ofExcelFormat(index)).map(Expression::toString);
  }

  /**
   * @param index the {@link TableIndex}.
   * @param value the value.
   * @throws IllegalArgumentException if index does not conform to {@link TableIndex#INDEXING_PATTERN}.
   */
  public void setCell(String index, String value) {
    setCell(TableIndex.ofExcelFormat(index), value);
  }

  public Excel3000 evaluate() {
    Excel3000 result = new Excel3000();
    HashSet<TableIndex> marked = new HashSet<>();
    for (TableIndex index : table.keySet()) {
      try {
        result.evaluateCell(index, this, marked);
      } catch (IllegalStateException e) {
        throw new IllegalStateException("Circuit encountered while evaluating " + index);
      }
    }
    return result;
  }

  private Expression evaluateCell(TableIndex index, Excel3000 old, Set<TableIndex> marked) {
    Optional<Expression> value = getCell(index);
    if (value.isPresent()) { // don't need to evaluate
      return value.get();
    } else {
      Expression expression = old.getCell(index).orElseThrow();
      if (!expression.isFormula()) {
        // non formulas can be evaluated directly
        expression = expression.evaluate();
      } else {
        // break circuit
        if (marked.contains(index)) throw new IllegalStateException();
        marked.add(index);
        // calc var assignments by recursively evaluating
        Map<String, Double> vars = expression.getVars(TableIndex.INDEXING_PATTERN)
            .stream()
            .collect(toMap(
                Function.identity(),
                key -> evaluateCell(TableIndex.ofExcelFormat(key), old, marked).getResult()
            ));
        // evaluate formula with assigned variables
        expression = expression.evaluate(vars);
      }
      setCell(index, expression);
      return expression;
    }
  }

  public void export(OutputStream out) throws IOException {
    try (Workbook sheets = new XSSFWorkbook()) {
      Sheet sheet = sheets.createSheet();
      for (Map.Entry<TableIndex, Expression> entry : table.entrySet()) {
        sheet
            .createRow(entry.getKey().row)
            .createCell(entry.getKey().col, CellType.STRING)
            .setCellValue(entry.getValue().toString());
      }
      sheets.write(out);
    }
  }
}
