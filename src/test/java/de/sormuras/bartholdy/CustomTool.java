package de.sormuras.bartholdy;

public class CustomTool implements Tool {

  private int exitCode;

  CustomTool(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public ToolResult run(ToolConfiguration configuration) {
    return ToolResult.builder().setExitCode(exitCode);
  }
}
