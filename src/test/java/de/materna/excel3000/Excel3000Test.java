package de.materna.excel3000;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Excel3000Test {
  @Test public void evaluate() {
    Excel3000 subject = new Excel3000();
    subject.setCell("A1", "7 + 5");
    subject.setCell("ZB5", "2");
    subject.setCell("A2", "=$A1 + $ZB5 - 3");
    subject = subject.evaluate();
    assertEquals("12.0", subject.getCell("A1").get());
    assertEquals("2.0", subject.getCell("ZB5").get());
    assertEquals("11.0", subject.getCell("A2").get());
  }

  @Test public void setAndGet() {
    Excel3000 subject = new Excel3000();
    subject.setCell("A1", "7 + 5");
    assertEquals("7 + 5", subject.getCell("A1").get());
  }

}