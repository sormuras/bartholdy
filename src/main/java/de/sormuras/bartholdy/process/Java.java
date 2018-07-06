package de.sormuras.bartholdy.process;

import java.nio.file.Path;

/**
 * You can use the {@code java} command to launch a Java application.
 *
 * @see <a href="https://docs.oracle.com/javase/10/tools/java.htm">java</a>
 */
public class Java extends AbstractProcessTool {

  @Override
  protected Path getHome() {
    return getCurrentJdkHome();
  }

  @Override
  public String getName() {
    return "java";
  }

  @Override
  public String getVersion() {
    return Runtime.version().toString();
  }
}
