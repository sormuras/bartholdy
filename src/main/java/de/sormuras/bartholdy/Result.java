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

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Result of a tool run. */
public interface Result {

  static Builder builder() {
    return new Builder();
  }

  int getExitCode();

  Duration getDuration();

  default String getOutput(String key) {
    return getOutput(key, "");
  }

  String getOutput(String key, String defaultValue);

  default List<String> getOutputLines(String key) {
    var value = getOutput(key, null);
    if (value == null) {
      return List.of();
    }
    return List.of(value.split("\\R"));
  }

  class Builder implements Result {

    private int exitCode = Integer.MIN_VALUE;
    private Duration duration = Duration.ZERO;
    private Map<String, String> lines = new HashMap<>();

    public Result build() {
      requireNonNull(duration, "duration must not be null");
      return this;
    }

    @Override
    public String toString() {
      return "Result{"
          + "exitCode="
          + exitCode
          + ", duration="
          + duration
          + ", lines="
          + lines
          + '}';
    }

    @Override
    public int getExitCode() {
      return exitCode;
    }

    public Builder setExitCode(int exitCode) {
      this.exitCode = exitCode;
      return this;
    }

    @Override
    public Duration getDuration() {
      return duration;
    }

    public Builder setDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    @Override
    public String getOutput(String key, String defaultValue) {
      return lines.getOrDefault(key, defaultValue);
    }

    public Builder setOutput(String key, String output) {
      this.lines.put(key, output);
      return this;
    }
  }
}
