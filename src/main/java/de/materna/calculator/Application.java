package de.materna.calculator;

import java.util.Scanner;
import java.util.function.Function;

public class Application {

  public static void main(String[] args) {
    IOController ioController = new IOController(new Calculator());
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit")) {
        break;
      }
      System.out.println(ioController.parseInput(input).fold(Function.identity(), Function.identity()));
    }
  }
}
