package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResultTests {
  @Test
  void isInterface() {
    assertTrue(Result.class.isInterface());
  }
}
