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

public interface ToolResult {

  static Builder builder() {
      return new Builder();
  }

  int getExitCode();

  class Builder implements ToolResult {
    int exitCode;

    @Override
    public int getExitCode() {
      return exitCode;
    }

    public Builder setExitCode(int exitCode) {
      this.exitCode = exitCode;
      return this;
    }
  }
}
