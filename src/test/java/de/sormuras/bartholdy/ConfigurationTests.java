package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigurationTests {

  @Test
  void isInterface() {
    assertTrue(Configuration.class.isInterface());
  }

  @Test
  void defaults() {
    var cfg = Configuration.builder().build();
    assertEquals(List.of(), cfg.getArguments());
    assertEquals(Map.of(), cfg.getEnvironment());
    assertEquals(Duration.ofSeconds(9), cfg.getTimeout());
    assertEquals(Paths.get(System.getProperty("java.io.tmpdir")), cfg.getTemporaryDirectory());
    assertEquals(Paths.get(".").normalize().toAbsolutePath(), cfg.getWorkingDirectory());
  }

  @Test
  void defaultStringRepresentation() {
    var expected =
        "Configuration\\{"
            + "arguments=\\[], "
            + "timeout=PT9S, "
            + "environment=\\{}, "
            + "temporaryDirectory=.+, "
            + "workingDirectory=.+"
            + "}";
    assertLinesMatch(List.of(expected), List.of(Configuration.builder().build().toString()));
  }

  @Test
  void argumentsOnlyFactory() {
    assertIterableEquals(List.of(), Configuration.of().getArguments());
    assertIterableEquals(List.of("1"), Configuration.of(1).getArguments());
    assertIterableEquals(List.of("1", "ii"), Configuration.of(1, "ii").getArguments());
  }

  @Test
  void fromBuilderToConfigurationAndBackAgain() {
    var expected = List.of("a", "11", "NEW");
    var builder = Configuration.builder();
    assertTrue(builder.isMutable());
    assertDoesNotThrow(builder::checkMutableState);
    builder.setArguments('a');
    builder.getArguments().add(String.valueOf(0xb));
    builder.addArgument(Thread.State.NEW);
    assertIterableEquals(expected, builder.getArguments());

    var configuration = builder.build();
    assertFalse(builder.isMutable());
    assertThrows(IllegalStateException.class, builder::checkMutableState);
    assertIterableEquals(expected, configuration.getArguments());
    assertThrows(UnsupportedOperationException.class, () -> configuration.getArguments().clear());
    assertThrows(UnsupportedOperationException.class, () -> configuration.getArguments().add(""));

    var second = configuration.toBuilder();
    assertTrue(second.isMutable());
    assertIterableEquals(expected, second.getArguments());
    second.addArgument("D");
    assertIterableEquals(List.of("a", "11", "NEW", "D"), second.getArguments());
  }

  @Test
  void iterableArguments() {
    var configuration = Configuration.builder().setArguments(List.of(1, 2, 3)).build();
    assertIterableEquals(List.of("1", "2", "3"), configuration.getArguments());
  }
}
