package de.materna.calculator;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.function.Function;
import java.util.regex.Pattern;

public class IOController {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");

  private final Calculator calculator;

  public IOController(Calculator calculator) {
    this.calculator = calculator;
  }

  public Try<String> validateVariableName(String variableName) {
    return VARIABLE_PATTERN.matcher(variableName).matches()
        ? Try.success(variableName)
        : Try.failure(new IllegalArgumentException("Illegal variable name: " + variableName));
  }

  public Either<String, Double> parseInput(String userInput) {
    String trim = userInput.trim();
    if (userInput.equalsIgnoreCase("clear")) {
      calculator.clearDefinitions();
      return Either.left("Info: Variable definitions cleared");
    } else return trim.contains("=")
        ? parseVariableDeclaration(trim)
        : calculator.evaluateExpression(trim).toEither().mapLeft(fail -> "Syntax error: " + fail.getMessage());
  }

  private Either<String, Double> parseVariableDeclaration(String userInput) {
    return Option.of(userInput.split("\\s*=\\s*"))
        .filter(split -> split.length == 2)
        .map(split -> defineVariable(split[0], split[1]))
        .toEither("No valid Expression on right-hand side of assignment")
        .flatMap(Function.identity());
  }

  private Either<String, Double> defineVariable(String variable, String expression) {
    return validateVariableName(variable)
        .flatMap(validatedName -> calculator.defineVariable(validatedName, expression))
        .transform(result -> result.isSuccess()
            ? Either.right(result.get())
            : Either.left(result.getCause().getMessage()));
  }

}
