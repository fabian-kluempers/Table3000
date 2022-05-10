package de.materna.calculator;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.beans.Expression;


public class Calculator {

  private Map<String, Double> variables = HashMap.empty();


  public double defineVariable(String variableName, double value) {
    updateVariable(variables.put(variableName, value));
    return value;
  }

  public Try<Double> defineVariable(String variableName, String value) {
    return evaluateExpression(value)
        .map(result -> defineVariable(variableName, result));
  }

  public Try<Double> evaluateExpression(String value) {
    return Try.of(() -> new ExpressionBuilder(value)
        .variables(variables.keySet().toJavaSet())
        .build()
        .setVariables(variables.toJavaMap())
        .evaluate());
  }

  private void updateVariable(Map<String, Double> variables) {
    this.variables = variables;
  }

  public void clearDefinitions() {
    updateVariable(HashMap.empty());
  }
}
