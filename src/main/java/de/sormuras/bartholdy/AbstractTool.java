package de.sormuras.bartholdy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractTool implements Tool {

  @Override
  public Result run(Configuration configuration) {
    var timeout = configuration.getTimeout().toMillis();
    var command = createCommand(configuration);
    var builder = new ProcessBuilder(command);
    builder.directory(configuration.getWorkingDirectory().toFile());
    builder.environment().put("JAVA_HOME", Bartholdy.currentJdkHome().toString());
    builder.environment().putAll(configuration.getEnvironment());
    try {
      var process = builder.start();
      var executor = Executors.newScheduledThreadPool(3);
      try (var inputStream = process.getInputStream();
          var errorStream = process.getErrorStream()) {
        var out = executor.submit(() -> Bartholdy.read(inputStream));
        var err = executor.submit(() -> Bartholdy.read(errorStream));
        executor.schedule(process::destroy, timeout, TimeUnit.MILLISECONDS);
        process.waitFor();
        return Result.builder()
            .setExitCode(process.exitValue())
            .setOutput("out", out.get(timeout, TimeUnit.MILLISECONDS))
            .setOutput("err", err.get(timeout, TimeUnit.MILLISECONDS))
            .build();

      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        throw new RuntimeException("run failed", e);
      } finally {
        process.destroy();
        executor.shutdownNow();
      }
    } catch (IOException e) {
      throw new UncheckedIOException("starting process failed", e);
    }
  }

  private List<String> createCommand(Configuration configuration) {
    var program = createProgram(createPathToProgram());
    var command = new ArrayList<String>();
    command.add(program);
    command.addAll(getToolArguments());
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

  protected Path getHome() {
    return Paths.get(".");
  }

  protected Path createPathToProgram() {
    return getHome().resolve("bin").resolve(getProgram());
  }

  protected String createProgram(Path pathToProgram) {
    return pathToProgram.normalize().toAbsolutePath().toString();
  }

  protected List<String> getToolArguments() {
    return List.of();
  }
}
