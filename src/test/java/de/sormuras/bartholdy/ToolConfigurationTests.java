package de.sormuras.bartholdy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolConfigurationTests {

  @Test
  void isInterface() {
    assertTrue(ToolConfiguration.class.isInterface());
  }

  @Test
  void arguments() {
    assertEquals(0, ToolConfiguration.of().getArguments().length);
    assertEquals(1, ToolConfiguration.of(1).getArguments().length);
    assertEquals(2, ToolConfiguration.of(1, 2).getArguments().length);
  }
}
