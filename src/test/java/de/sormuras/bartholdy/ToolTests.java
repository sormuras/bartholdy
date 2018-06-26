package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToolTests {

  @Test
  void isInterface() {
    assertTrue(Tool.class.isInterface());
  }
}
