package integration.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.process.Ant;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class AntTests {

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
    var tool = Ant.version(Paths.get("build", "bartholdy", "tools"), version);
    assertTrue(tool.getVersion().contains(version));

    var result = tool.run(Configuration.of("-version"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines = List.of("Apache Ant\\(TM\\) version " + version + " compiled on .+");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }
}
