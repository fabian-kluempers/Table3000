package de.materna.excel3000;

import com.google.common.collect.Maps;
import io.vavr.control.Option;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;


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

  /**
   * @param index the {@link TableIndex}.
   * @throws IllegalArgumentException if index does not conform to {@link TableIndex#INDEXING_PATTERN}.
   * @return the value at the specified index or none.
   */
  public Option<String> getCell(String index) {
    return getCell(TableIndex.ofExcelFormat(index));
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
      result.evaluateCell(index, this, marked);
    }
    return result;
  }

  public String evaluateCell(TableIndex index, Excel3000 old, Set<TableIndex> marked) {
    Option<String> value = getCell(index);
    if (!value.isDefined()) {
      String expression = old.getCell(index).get();
      String result;
      if (ExpressionUtil.isFormula(expression)) {
        if (marked.contains(index)) throw new IllegalStateException("Circuit found");
        marked.add(index);
        Map<String, Double> vars = ExpressionUtil.getVars(expression, TableIndex.INDEXING_PATTERN).toMap(
            Function.identity(),
            key -> Double.valueOf(evaluateCell(TableIndex.ofExcelFormat(key), old, marked))
        ).toJavaMap();
        result = String.valueOf(ExpressionUtil.evaluate(ExpressionUtil.toCanonicalForm(expression), vars));
      } else {
        result = String.valueOf(ExpressionUtil.evaluate(expression));
      }
      setCell(index, result);
      return result;
    } else {
      return value.get();
    }
  }

  public void export(OutputStream out) throws IOException {
    try (Workbook sheets = new XSSFWorkbook()) {
      Sheet sheet = sheets.createSheet();
      for (Map.Entry<TableIndex, String> entry : table.entrySet()) {
        sheet
            .createRow(entry.getKey().row)
            .createCell(entry.getKey().col, CellType.STRING)
            .setCellValue(entry.getValue());
      }
      sheets.write(out);
    }
  }
}
