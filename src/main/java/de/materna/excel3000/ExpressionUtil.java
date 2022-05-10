package de.materna.excel3000;

import de.materna.util.TailCall;
import de.materna.util.TailRec;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtil {
  public static Set<String> getVars(String expression, Pattern variablePattern) {
    return tailRecGetVars(
        HashSet.empty(),
        Pattern.compile("\\$(" + variablePattern.pattern() + ")").matcher(expression)
    ).invoke();
  }

  private static TailCall<Set<String>> tailRecGetVars(Set<String> result, Matcher matcher) {
    return !matcher.find()
        ? TailRec.done(result)
        : TailRec.call(() -> tailRecGetVars(result.add(matcher.group(1)), matcher));
  }

  public static Double evaluate(String expression, Map<String, Double> vars) {
    return new ExpressionBuilder(expression)
        .variables(vars.keySet())
        .build()
        .setVariables(vars)
        .evaluate();
  }

  public static Double evaluate(String expression) {
    return new ExpressionBuilder(expression)
        .build()
        .evaluate();
  }

  public static boolean isFormula(String expression) {
    return expression.startsWith("=");
  }

  public static String toCanonicalForm(String expression) {
    return isFormula(expression)
        ? toCanonicalForm(expression.substring(1))
        : expression.replace("$", "");
  }
}
