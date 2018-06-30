package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResultTests {
  @Test
  void isInterface() {
    assertTrue(Result.class.isInterface());
  }

  @Test
  void defaults() {
    var result = Result.builder().build();
    assertEquals(Integer.MIN_VALUE, result.getExitCode());
    assertEquals(Duration.ZERO, result.getDuration());
    assertEquals("", result.getOutput(null));
    assertEquals("", result.getOutput("(:"));
    assertEquals(List.of(), result.getOutputLines(":)"));
  }

  @Test
  void multilines() {
    var result = Result.builder().setOutput("*", "1\n2\r3\r\n").build();
    assertEquals(List.of("1", "2", "3"), result.getOutputLines("*"));
  }
}
