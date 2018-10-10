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
            "org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants",
            "org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine",
            "org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants",
            "org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants"),
        result.getOutputLines("err"));
    assertLinesMatch(
        List.of(
            "Edge org.junit.jupiter.engine.descriptor->org.junit.jupiter.engine creates a cycle in graph:"
                + "[org.junit.jupiter.engine->org.apiguardian.api,"
                + " org.junit.jupiter.engine->org.junit.jupiter.engine.descriptor,"
                + " org.junit.jupiter.engine->org.junit.jupiter.engine.discovery,"
                + " org.junit.jupiter.engine->org.junit.jupiter.engine.execution,"
                + " org.junit.jupiter.engine->org.junit.platform.engine,"
                + " org.junit.jupiter.engine->org.junit.platform.engine.support.config,"
                + " org.junit.jupiter.engine->org.junit.platform.engine.support.hierarchical,"
                + " org.junit.jupiter.engine.descriptor->org.apiguardian.api,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.api,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.api.extension,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.api.function,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.api.parallel,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.engine.execution,"
                + " org.junit.jupiter.engine.descriptor->org.junit.jupiter.engine.extension,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.commons,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.commons.logging,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.commons.util,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.engine,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.engine.reporting,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.engine.support.descriptor,"
                + " org.junit.jupiter.engine.descriptor->org.junit.platform.engine.support.hierarchical"
                + "]",
            ">> 3 // 'bad' edges >>"),
        result.getOutputLines("messages"));
  }

  private static Path jar(String name) {
    try {
      return Paths.get(CyclesDetectorTests.class.getResource("/jars/" + name + ".jar").toURI());
    } catch (URISyntaxException e) {
      return fail("Failed to create path for: " + name, e);
    }
  }
}
