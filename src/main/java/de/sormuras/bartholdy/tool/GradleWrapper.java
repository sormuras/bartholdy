package de.sormuras.bartholdy.tool;

import de.sormuras.bartholdy.Bartholdy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class GradleWrapper extends AbstractTool {

  private final Path home;

  public GradleWrapper() {
    this(Paths.get("."));
  }

  public GradleWrapper(Path home) {
    this.home = home;
  }

  @Override
  public Path getHome() {
    return home;
  }

  @Override
  protected Path createPathToProgram() {
    return getHome().resolve(getProgram());
  }

  @Override
  public String getName() {
    return "gradle-wrapper";
  }

  @Override
  public String getProgram() {
    var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    return "gradlew" + (win ? ".bat" : "");
  }

  @Override
  public String getVersion() {
    var jar = getHome().resolve("gradle").resolve("wrapper").resolve("gradle-wrapper.jar");
    var text = Bartholdy.read(jar, "/build-receipt.properties", System.lineSeparator(), "?");
    return Bartholdy.readProperty(text, "versionNumber", "unknown");
  }
}
