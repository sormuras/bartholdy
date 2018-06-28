package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CustomToolTests {

  @Test
  void runWithoutArguments() {
    var tool = new CustomTool(42);
    assertEquals(42, tool.run());
  }
}
