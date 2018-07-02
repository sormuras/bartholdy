package de.sormuras.bartholdy.process;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

abstract class AbstractProcessTool implements Tool {

  @Override
  public Result run(Configuration configuration) {
    var command = createCommand(configuration);
    var builder = new ProcessBuilder(command);
    builder.directory(configuration.getWorkingDirectory().toFile());
    builder.environment().put("JAVA_HOME", getCurrentJdkHome().toString());
    builder.environment().putAll(configuration.getEnvironment());
    try {
      var process = builder.start();
      var out = read(process.getInputStream());
      var err = read(process.getErrorStream());
      var exitCode = process.waitFor();
      process.destroy();
      return Result.builder()
          .setExitCode(exitCode)
          .setOutput("out", out)
          .setOutput("err", err)
          .build();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("run failed", e);
    }
  }

  private List<String> createCommand(Configuration configuration) {
    var program = createProgram();
    var command = new ArrayList<String>();
    command.add(program);
    command.addAll(configuration.getArguments());
    var commandLineLength = String.join(" ", command).length();
    if (commandLineLength < 32000) {
      return command;
    }
    //      if (!tool.isArgumentsFileSupported) {
    //        info("large command line (%s) detected, but %s does not support @argument file",
    //            commandLineLength, this);
    //           return strings;
    //      }
    var timestamp = Instant.now().toString().replace("-", "").replace(":", "");
    var prefix = "bartholdy-" + getName() + "-arguments-" + timestamp + "-";
    try {
      var temporaryDirectory = configuration.getTemporaryDirectory();
      var temporaryFile = Files.createTempFile(temporaryDirectory, prefix, ".txt");
      return List.of(program, "@" + Files.write(temporaryFile, configuration.getArguments()));
    } catch (IOException e) {
      throw new UncheckedIOException("creating temporary arguments file failed", e);
    }
  }

  protected abstract String createProgram();

  private String read(InputStream inputStream) {
    var reader = new BufferedReader(new InputStreamReader(inputStream));
    var joiner = new StringJoiner(System.lineSeparator());
    reader.lines().iterator().forEachRemaining(joiner::add);
    return joiner.toString();
  }

  static Path getCurrentJdkHome() {
    Path executable = ProcessHandle.current().info().command().map(Paths::get).orElseThrow();
    // path element count is 3 or higher: "<JAVA_HOME>/bin/java[.exe]"
    return executable.getParent().getParent().toAbsolutePath();
  }
}
