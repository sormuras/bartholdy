package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigurationTests {

  @Test
  void isInterface() {
    assertTrue(Configuration.class.isInterface());
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
    builder.setArguments('a');
    builder.getArguments().add(String.valueOf(0xb));
    builder.addArgument(Thread.State.NEW);
    assertIterableEquals(expected, builder.getArguments());

    var configuration = builder.build();
    assertFalse(builder.isMutable());
    assertIterableEquals(expected, configuration.getArguments());
    assertThrows(UnsupportedOperationException.class, () -> configuration.getArguments().clear());
    assertThrows(UnsupportedOperationException.class, () -> configuration.getArguments().add(""));

    var second = configuration.toBuilder();
    assertTrue(second.isMutable());
    assertIterableEquals(expected, second.getArguments());
    second.addArgument("D");
    assertIterableEquals(List.of("a", "11", "NEW", "D"), second.getArguments());
  }
}
