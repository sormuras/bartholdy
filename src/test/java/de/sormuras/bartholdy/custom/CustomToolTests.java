package de.sormuras.bartholdy.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.bartholdy.Configuration;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CustomToolTests {

  @Test
  void runWithoutArguments() {
    var tool = new CustomTool(42);
    var code = tool.run();
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
    var tool = new CustomTool(42);
    var result = tool.run(conf);
    assertEquals(0, result.getExitCode());
    assertEquals(Duration.ofNanos(1), result.getDuration());
    assertEquals("put", result.getOutput("out"));
    assertEquals("or", result.getOutput("err"));
  }
}
