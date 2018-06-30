package de.sormuras.bartholdy;

import java.time.Duration;

public class CustomTool implements Tool {

  private int exitCode;

  CustomTool(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public Result run(Configuration configuration) {
    return Result.builder()
        .setExitCode(exitCode)
        .setDuration(Duration.ofNanos(1))
        .setOutput("out", "put");
  }
}
