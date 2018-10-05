package de.sormuras.bartholdy.tool;

import de.sormuras.bartholdy.Bartholdy;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractTool implements Tool {

  @Override
  public Result run(Configuration configuration) {
    var outLines = new ArrayList<String>();
    var errLines = new ArrayList<String>();
    var timeout = configuration.getTimeout().toMillis();
    var command = createCommand(configuration);
    var builder = new ProcessBuilder(command);
    builder.directory(configuration.getWorkingDirectory().toFile());
    builder.environment().put("JAVA_HOME", Bartholdy.currentJdkHome().toString());
    builder.environment().put(getNameOfEnvironmentHomeVariable(), getHome().toString());
    builder.environment().putAll(configuration.getEnvironment());
    try {
      var start = Instant.now();
      var process = builder.start();
      var executor = Executors.newScheduledThreadPool(3);
      try (var inputStream = process.getInputStream();
          var errorStream = process.getErrorStream()) {
        executor.submit(new StreamGobbler(inputStream, outLines::add));
        executor.submit(new StreamGobbler(errorStream, errLines::add));
        var destroyer = executor.schedule(process::destroy, timeout, TimeUnit.MILLISECONDS);
        process.waitFor();
        var duration = Duration.between(start, Instant.now());
        return Result.builder()
            .setTimedOut(!destroyer.cancel(true))
            .setExitCode(process.exitValue())
            .setDuration(duration)
            .setOutput("out", String.join(System.lineSeparator(), outLines))
            .setOutput("err", String.join(System.lineSeparator(), errLines))
            .build();

      } catch (InterruptedException e) {
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

  public Path getHome() {
    return Paths.get(".");
  }

  public String getNameOfEnvironmentHomeVariable() {
    return getClass().getSimpleName().toUpperCase() + "_HOME";
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

  static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumeInputLine;

    StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
      this.inputStream = inputStream;
      this.consumeInputLine = consumeInputLine;
    }

    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
    }
  }
}
