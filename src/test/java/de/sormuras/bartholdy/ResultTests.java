package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResultTests {
  @Test
  void isInterface() {
    assertTrue(Result.class.isInterface());
  }

  @Test
  void defaults() {
    var result = Result.builder().build();
    assertEquals(0, result.getExitCode());
  }
}
