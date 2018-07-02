package de.sormuras.bartholdy.process;

/**
 * You can use the {@code java} command to launch a Java application.
 *
 * @see <a href="https://docs.oracle.com/javase/10/tools/java.htm">java</a>
 */
public class Java extends AbstractProcessTool {

  @Override
  protected String createProgram() {
    var path = getCurrentJdkHome().resolve("bin");
    var executable = path.resolve(getName());
    return executable.normalize().toAbsolutePath().toString();
  }

  @Override
  public String getName() {
    return "java";
  }

  @Override
  public String getVersion() {
    return "?";
  }
}
