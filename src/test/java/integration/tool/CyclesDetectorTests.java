package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.fail;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.CyclesDetector;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class CyclesDetectorTests {

  @Test
  void analyze_ASM_4_1() {
    new CyclesDetector(jar("asm-4.1")).run();
  }

  @Test
  void analyze_JUnit_Platform_Commons_1_3_1() {
    new CyclesDetector(jar("junit-platform-commons-1.3.1")).run();
  }

  @Test
  void analyze_JUnit_Jupiter_Engine_5_3_1() {
    var jar = jar("junit-jupiter-engine-5.3.1");
    var result = new CyclesDetector(jar).run(Configuration.of());

    assertEquals(1, result.getExitCode());
    assertLinesMatch(
        List.of(
            "org.junit.jupiter.engine.discovery"
                + " -> org.junit.jupiter.engine.extension"
                + " -> org.junit.jupiter.engine.execution"
                + " -> org.junit.jupiter.engine.descriptor"
                + " -> org.junit.jupiter.engine"),
        result.getOutputLines("cycles"));
  }

  private static Path jar(String name) {
    try {
      return Paths.get(CyclesDetectorTests.class.getResource("/jars/" + name + ".jar").toURI());
    } catch (URISyntaxException e) {
      return fail("Failed to create path for: " + name, e);
    }
  }
}
