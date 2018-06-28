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

import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/** Tool configuration providing environment and execution data. */
public interface Configuration {

  static Builder builder() {
    return new Builder();
  }

  static Configuration of(Object... args) {
    return builder().setArguments(args).build();
  }

  List<String> getArguments();

  default Builder toBuilder() {
    var builder = builder();
    builder.setArguments(new ArrayList<>(getArguments()));
    return builder;
  }

  class Builder implements Configuration {

    private final System.Logger logger = System.getLogger(getClass().getCanonicalName());
    private boolean mutable = true;
    private List<String> arguments = new ArrayList<>();

    Configuration build() {
      mutable = false;
      arguments = List.copyOf(arguments);
      return this;
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
          logger.log(WARNING, "unrolling iterable argument: " + argument);
          ((Iterable<?>) argument).forEach(this::addArgument);
        }
        addArgument(argument);
      }
      return this;
    }
  }
}
