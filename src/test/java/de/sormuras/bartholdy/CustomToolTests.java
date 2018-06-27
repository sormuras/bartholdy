package de.sormuras.bartholdy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomToolTests {

  @Test
  void runWithoutArguments() {
    var tool = new CustomTool(42);
    assertEquals(42, tool.run());
  }
}
