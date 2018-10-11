package de.sormuras.bartholdy.util;

public class CycleDetectedException extends IllegalArgumentException {

  CycleDetectedException(String message) {
    super("Cycle detected: " + message);
  }
}
