package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.Maven;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class MavenTests {

  @Test
  void maven_3_5_3() {
    version("3.5.3");
  }

  @Test
  void maven_3_5_4() {
    version("3.5.4");
  }

  private void version(String version) {
    var destination = Paths.get("build", "bartholdy", "tools");
    var tool = Maven.install(version, destination);
    // assertTrue(tool.getVersion().contains(version));
    assertEquals("maven", tool.getName());
    assertTrue(Files.exists(tool.getHome()));

    var result = tool.run(Configuration.of("-version"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines =
        List.of("Apache Maven " + version + " .+", "Maven home: .+", ">> more details >>");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }
}
