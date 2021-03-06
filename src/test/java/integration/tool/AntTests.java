package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.Ant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AntTests {

  @Test
  void ant_1_10_5() {
    version("1.10.5");
  }

  @Test
  void ant_1_10_4() {
    version("1.10.4");
  }

  @Test
  void ant_1_10_3() {
    version("1.10.3");
  }

  @Test
  void ant_1_9_12() {
    version("1.9.12");
  }

  private void version(String version) {
    var destination = Path.of("build", "bartholdy", "tools");
    var tool = Ant.install(version, destination);
    assertTrue(tool.getVersion().contains(version));
    assertEquals("ant", tool.getName());
    assertTrue(Files.isDirectory(tool.getHome()));
    assertEquals("ANT_HOME", tool.getNameOfEnvironmentHomeVariable());

    var result = tool.run(Configuration.of("-version"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines = List.of("Apache Ant\\(TM\\) version " + version + " compiled on .+");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }
}
