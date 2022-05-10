package de.materna.excel300;

import io.vavr.collection.List;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtil {
  public static Set<String> getVars(String expression, Pattern variablePattern) {
    List<String> result = List.empty();
    Matcher matcher = Pattern.compile("\\$(" + variablePattern.pattern() + ")").matcher(expression);
    while (matcher.find()) {
      result = result.append(matcher.group(1));
    }
    return result.toJavaSet();
  }

  public static Double evaluate(String expression, Map<String,Double> vars) {
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
