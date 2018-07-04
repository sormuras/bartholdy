/*
 * Copyright (C) 2018 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sormuras.bartholdy;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tool configuration providing environment and execution data. */
public interface Configuration {

  static Builder builder() {
    return new Builder();
  }

  static Configuration of(Object... args) {
    return builder().setArguments(args).build();
  }

  List<String> getArguments();

  Map<String, String> getEnvironment();

  Path getTemporaryDirectory();

  Path getWorkingDirectory();

  Duration getTimeout();

  default Builder toBuilder() {
    return builder()
        .setArguments(new ArrayList<>(getArguments()))
        .setEnvironment(new HashMap<>(getEnvironment()));
  }

  class Builder implements Configuration {

    private final System.Logger logger = System.getLogger(getClass().getCanonicalName());
    private boolean mutable = true;
    private List<String> arguments = new ArrayList<>();
    private Map<String, String> environment = new HashMap<>();
    private Path temporaryDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
    private Path workingDirectory = Paths.get(".").normalize().toAbsolutePath();
    private Duration timeout = Duration.ofSeconds(9);

    public Configuration build() {
      mutable = false;
      arguments = List.copyOf(arguments);
      environment = Map.copyOf(environment);
      return this;
    }

    @Override
    public String toString() {
      return "Configuration{"
          + "arguments="
          + arguments
          + ", timeout="
          + timeout
          + ", environment="
          + environment
          + ", temporaryDirectory="
          + temporaryDirectory
          + ", workingDirectory="
          + workingDirectory
          + '}';
    }

    void checkMutableState() {
      if (isMutable()) {
        return;
      }
      throw new IllegalStateException("immutable");
    }

    public boolean isMutable() {
      return mutable;
    }

    @Override
    public List<String> getArguments() {
      return arguments;
    }

    public Builder addArgument(Object argument) {
      checkMutableState();
      this.arguments.add(String.valueOf(requireNonNull(argument, "argument must not be null")));
      return this;
    }

    public Builder setArguments(List<String> arguments) {
      checkMutableState();
      this.arguments = requireNonNull(arguments, "arguments must not be null");
      return this;
    }

    public Builder setArguments(Object... arguments) {
      checkMutableState();
      this.arguments.clear();
      for (var argument : arguments) {
        if (argument instanceof Iterable) {
          logger.log(DEBUG, "unrolling iterable argument: " + argument);
          ((Iterable<?>) argument).forEach(this::addArgument);
          continue;
        }
        addArgument(argument);
      }
      return this;
    }

    @Override
    public Map<String, String> getEnvironment() {
      return environment;
    }

    Builder setEnvironment(Map<String, String> environment) {
      checkMutableState();
      this.environment = environment;
      return this;
    }

    public Builder putEnvironment(String key, String value) {
      checkMutableState();
      requireNonNull(key, "key must not be null");
      requireNonNull(value, "value must not be null");
      environment.put(key, value);
      return this;
    }

    @Override
    public Path getTemporaryDirectory() {
      return temporaryDirectory;
    }

    public Builder setTemporaryDirectory(Path temporaryDirectory) {
      checkMutableState();
      requireNonNull(temporaryDirectory, "temporaryDirectory must not be null");
      this.temporaryDirectory = temporaryDirectory;
      return this;
    }

    @Override
    public Path getWorkingDirectory() {
      return workingDirectory;
    }

    public Builder setWorkingDirectory(Path workingDirectory) {
      checkMutableState();
      requireNonNull(workingDirectory, "workingDirectory must not be null");
      this.workingDirectory = workingDirectory;
      return this;
    }

    @Override
    public Duration getTimeout() {
      return timeout;
    }

    public Builder setTimeoutMillis(long timeoutMillis) {
      setTimeout(Duration.ofMillis(timeoutMillis));
      return this;
    }

    public Builder setTimeout(Duration timeout) {
      checkMutableState();
      requireNonNull(timeout, "timeout must not be null");
      this.timeout = timeout;
      return this;
    }
  }
}
