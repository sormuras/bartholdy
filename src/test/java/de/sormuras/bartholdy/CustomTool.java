package de.sormuras.bartholdy;

public class CustomTool implements Tool {

  private int exitCode;

  CustomTool(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public Result run(Configuration configuration) {
    return Result.builder().setExitCode(exitCode);
  }
}
