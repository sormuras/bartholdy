package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.AbstractTool;
import de.sormuras.bartholdy.Bartholdy;
import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.tool.Gradle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class GradleTests {

  @Test
  void gradle_4_8() {
    version("4.8");
  }

  @Test
  void gradle_4_9() {
    version("4.9");
  }

  private void version(String version) {
    var destination = Paths.get("build", "bartholdy", "tools");
    var tool = Gradle.install(version, destination);
    assertEquals("gradle", tool.getName());
    assertTrue(Files.exists(tool.getHome()));

    version(version, tool);
  }

  private void version(String version, Tool tool) {
    var result = tool.run(Configuration.of("-version", "--no-daemon"));
    assertEquals(0, result.getExitCode());
    // assertEquals("", result.getOutput("err")); // Illegal Access is reported here
    var expectedLines =
        List.of(
            ">> blank line, Welcome message, etc... >>",
            "------------------------------------------------------------",
            "Gradle " + version,
            "------------------------------------------------------------",
            "",
            "Build time:   .+ .+ UTC",
            "Revision:     .*",
            ">> blank line, Kotlin, etc... >>",
            "Groovy:       2.4.12",
            "Ant:          Apache Ant(TM) version 1.9.11 compiled on March 23 2018",
            "JVM:          .+",
            "OS:           .+");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }

  @Test
  void gradleWrapper() {
    var tool = new GradleWrapper();
    version(tool.getVersion(), tool);
  }

  class GradleWrapper extends AbstractTool {

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
}
