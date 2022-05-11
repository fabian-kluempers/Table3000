package de.materna.excel3000;

import de.materna.util.TailCall;
import de.materna.util.TailRec;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expression {
  private final String stringRepresentation;
  private final double result;

  public boolean isEvaluated() {
    // maybe introduce flag instead of checking for NaN because NaN might be a valid result of an expression?
    return !Double.isNaN(result);
  }

  private static final Predicate<String> isFormula = s -> s.startsWith("=");

  /**
   * @return the result of this expression, if it has been evaluated.
   * @throws IllegalStateException if this expression has not been evaluated.
   */
  public double getResult() {
    if (!isEvaluated()) throw new IllegalStateException("Expression is not yet evaluated");
    else return result;
  }

  @Override public String toString() {
    return isEvaluated() ? String.valueOf(result) : stringRepresentation;
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }

  private Expression(String expression, Double result) {
    this.result = result;
    this.stringRepresentation = expression;
  }

  private Expression(String expression) {
    this.stringRepresentation = expression;
    this.result = Double.NaN;
  }

  private static <T> Set<T> add(Set<T> set, T value) {
    set.add(value);
    return set;
  }

  public Set<String> getVars(Pattern variablePattern) {
    return tailRecGetVars(
        new HashSet<>(),
        Pattern.compile("\\$(" + variablePattern.pattern() + ")").matcher(this.stringRepresentation)
    ).invoke();
  }

  private static TailCall<Set<String>> tailRecGetVars(Set<String> result, Matcher matcher) {
    return !matcher.find()
        ? TailRec.done(Set.copyOf(result)) // make immutable
        : TailRec.call(() -> tailRecGetVars(add(result, matcher.group(1)), matcher));
  }

  public Expression evaluate(Map<String, Double> vars) {
    return new Expression(stringRepresentation, new ExpressionBuilder(toCanonicalForm(stringRepresentation))
        .variables(vars.keySet())
        .build()
        .setVariables(vars)
        .evaluate());
  }

  public Expression evaluate() {
    return new Expression(stringRepresentation, new ExpressionBuilder(this.stringRepresentation)
        .build()
        .evaluate());
  }

  public boolean isFormula() {
    return isFormula.test(stringRepresentation);
  }

  private static String toCanonicalForm(String expression) {
    return isFormula.test(expression)
        ? toCanonicalForm(expression.substring(1))
        : expression.replace("$", "");
  }

  public static Expression of(String value) {
    return new Expression(value);
  }
}
