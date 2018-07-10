package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BartholdyTests {

  @Test
  void version() {
    assertEquals("DEVELOPMENT", Bartholdy.version());
  }

  @Test
  void privateConstructorThrows() {
    assertThrows(Exception.class, () -> Bartholdy.class.getDeclaredConstructor().newInstance());
  }
}
