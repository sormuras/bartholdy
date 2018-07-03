package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigurationIntegrationTests {

  @Test
  void nullIsNotAllowed() {
    var npe = NullPointerException.class;
    var builder = Configuration.builder();
    assertThrows(npe, () -> builder.addArgument(null));
    assertThrows(npe, () -> builder.setArguments((Object[]) null));
    assertThrows(npe, () -> builder.setArguments((List<String>) null));
    assertThrows(npe, () -> builder.putEnvironment(null, "value"));
    assertThrows(npe, () -> builder.putEnvironment("key", null));
    assertThrows(npe, () -> builder.setTemporaryDirectory(null));
    assertThrows(npe, () -> builder.setWorkingDirectory(null));
  }

  @Test
  void mutable() {
    var builder = Configuration.builder();
    assertTrue(builder.isMutable());
    builder.build();
    assertFalse(builder.isMutable());
  }

  @Test
  void temporaryDirectory() {
    var expected = Paths.get("expected");
    var configuration = Configuration.builder().setTemporaryDirectory(expected).build();
    assertSame(expected, configuration.getTemporaryDirectory());
  }

  @Test
  void workingDirectory() {
    var expected = Paths.get("expected");
    var configuration = Configuration.builder().setWorkingDirectory(expected).build();
    assertSame(expected, configuration.getWorkingDirectory());
  }

  @Test
  void timeoutMillis() {
    var expected = 12345L;
    var configuration = Configuration.builder().setTimeoutMillis(expected).build();
    assertEquals(expected, configuration.getTimeoutMillis());
  }
}
