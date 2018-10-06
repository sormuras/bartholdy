package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.bartholdy.Configuration;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomToolTests {

  private final CustomTool custom42 = new CustomTool(42);

  @Test
  void name() {
    assertEquals("Custom Tool", custom42.getName());
  }

  @Test
  void version() {
    assertEquals("0x2A", custom42.getVersion());
  }

  @Test
  void runWithoutArguments() {
    var code = custom42.run();
    assertEquals(42, code);
  }

  @Test
  void runWithConfiguration() {
    var conf =
        Configuration.builder()
            .addArgument("1")
            .putEnvironment("exitCode", "0")
            .putEnvironment("duration", "1")
            .build();
    var result = custom42.run(conf);
    assertEquals(0, result.getExitCode());
    assertEquals(Duration.ofNanos(1), result.getDuration());
    assertEquals(List.of("1", "2"), result.getOutputLines("aux"));
    assertEquals("put", result.getOutput("out"));
    assertEquals("or", result.getOutput("err"));
  }
}
