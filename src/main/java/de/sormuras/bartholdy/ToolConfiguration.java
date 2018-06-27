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

import java.util.Objects;

/** Tool configuration providing environment and execution data. */
public interface ToolConfiguration {

  static Builder builder() {
    return new Builder();
  }

  static ToolConfiguration of(Object... args) {
    return builder().setArguments(args).build();
  }

  Object[] getArguments();

  class Builder implements ToolConfiguration {

    private Object[] arguments;

    ToolConfiguration build() {
      Objects.requireNonNull(arguments, "arguments must not be null");
      return this;
    }

    @Override
    public Object[] getArguments() {
      return arguments;
    }

    public Builder setArguments(Object[] arguments) {
      this.arguments = arguments;
      return this;
    }
  }
}
