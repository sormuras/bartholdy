package de.sormuras.bartholdy.tool;

import de.sormuras.bartholdy.Bartholdy;
import java.nio.file.Path;

/**
 * You can use the {@code java} command to launch a Java application.
 *
 * @see <a href="https://docs.oracle.com/javase/10/tools/java.htm">java</a>
 */
public class Java extends AbstractTool {

  @Override
  public Path getHome() {
    return Bartholdy.currentJdkHome();
  }

  @Override
  public String getName() {
    return "java";
  }

  @Override
  public final String getProgram() {
    return "java";
  }

  @Override
  public String getVersion() {
    return Runtime.version().toString();
  }
}
