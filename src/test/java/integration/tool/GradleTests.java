package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.Gradle;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class GradleTests {

  @Test
  void gradle_4_8() {
    version("4.8");
  }

  @Test
  void gradle_4_9_rc_2() {
    version("4.9-rc-2");
  }

  private void version(String version) {
    var destination = Paths.get("build", "bartholdy", "tools");
    var tool = Gradle.install(version, destination);
    assertEquals("gradle", tool.getName());
    assertTrue(Files.exists(tool.getHome()));

    var result = tool.run(Configuration.of("-version", "--no-daemon"));
    assertEquals(0, result.getExitCode());
    // assertEquals("", result.getOutput("err")); // Illegal Access is reported here
    var expectedLines =
        List.of(
            "",
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
}
