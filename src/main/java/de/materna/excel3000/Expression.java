package de.materna.excel3000;

import de.materna.util.TailCall;
import de.materna.util.TailRec;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expression {
  private final String value;

  @Override public String toString() {
    return value;
  }

  private Expression(String value) {
    this.value = value;
  }

  private static <T> Set<T> add(Set<T> set, T value) {
    set.add(value);
    return set;
  }

  public Set<String> getVars(Pattern variablePattern) {
    return tailRecGetVars(
        new HashSet<>(),
        Pattern.compile("\\$(" + variablePattern.pattern() + ")").matcher(this.value)
    ).invoke();
  }

  private static TailCall<Set<String>> tailRecGetVars(Set<String> result, Matcher matcher) {
    return !matcher.find()
        ? TailRec.done(Set.copyOf(result)) // make immutable
        : TailRec.call(() -> tailRecGetVars(add(result, matcher.group(1)), matcher));
  }

  public Double evaluate(Map<String, Double> vars) {
    return new ExpressionBuilder(this.value)
        .variables(vars.keySet())
        .build()
        .setVariables(vars)
        .evaluate();
  }

  public Double evaluate() {
    return new ExpressionBuilder(this.value)
        .build()
        .evaluate();
  }

  public boolean isFormula() {
    return this.value.startsWith("=");
  }

  public Expression toCanonicalForm() {
    return isFormula()
        ? Expression.of(this.value.substring(1)).toCanonicalForm()
        : Expression.of(value.replace("$", ""));
  }

  public static Expression of(String value) {
    return new Expression(value);
  }
}
