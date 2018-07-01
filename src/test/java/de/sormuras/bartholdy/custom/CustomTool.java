package de.sormuras.bartholdy.custom;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import java.time.Duration;

public class CustomTool implements Tool {

  private int exitCode;

  CustomTool(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public Result run(Configuration configuration) {
    var environment = configuration.getEnvironment();
    var exitCode = Integer.valueOf(environment.getOrDefault("exitCode", "" + this.exitCode));
    var duration = Duration.ofNanos(Integer.valueOf(environment.getOrDefault("duration", "1")));
    var out = environment.getOrDefault("out", "put");
    var err = environment.getOrDefault("err", "or");
    return Result.builder()
        .setExitCode(exitCode)
        .setDuration(duration)
        .setOutput("out", out)
        .setOutput("err", err);
  }
}
