package de.sormuras.bartholdy.tool;

import de.sormuras.bartholdy.Bartholdy;
import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTool implements Tool {

  @Override
  public Result run(Configuration configuration) {
    var timeout = configuration.getTimeout().toMillis();
    var command = createCommand(configuration);
    var builder = new ProcessBuilder(command);
    builder.directory(configuration.getWorkingDirectory().toFile());
    builder.environment().put("JAVA_HOME", Bartholdy.currentJdkHome().toString());
    builder.environment().put(getNameOfEnvironmentHomeVariable(), getHome().toString());
    builder.environment().putAll(configuration.getEnvironment());
    try {
      var errfile = Files.createTempFile("bartholdy-err-", ".txt");
      var outfile = Files.createTempFile("bartholdy-out-", ".txt");
      builder.redirectError(errfile.toFile());
      builder.redirectOutput(outfile.toFile());
      var start = Instant.now();
      var process = builder.start();
      try {
        var timedOut = false;
        if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
          timedOut = true;
          process.destroy();
        }
        var duration = Duration.between(start, Instant.now());
        return Result.builder()
            .setTimedOut(timedOut)
            .setExitCode(process.exitValue())
            .setDuration(duration)
            .setOutput("err", readAllLines(errfile))
            .setOutput("out", readAllLines(outfile))
            .build();
      } catch (InterruptedException e) {
        throw new RuntimeException("run failed", e);
      } finally {

        Files.deleteIfExists(errfile);
        Files.deleteIfExists(outfile);
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
    return Path.of(".");
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

  private static List<String> readAllLines(Path path) {
    try {
      return Files.readAllLines(path);
    } catch (IOException e) {
      // ignore
    }
    var lines = new ArrayList<String>();
    try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
      for (String line; (line = br.readLine()) != null; ) {
        lines.add(line);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("reading lines failed: " + path, e);
    }
    return lines;
  }
}
