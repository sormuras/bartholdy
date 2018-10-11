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
    var jar = jar("asm-4.1");
    var result = new CyclesDetector(jar).run(Configuration.of());

    assertEquals(0, result.getExitCode(), () -> "result=" + result);

    assertLinesMatch(List.of(), result.getOutputLines("cycles"));

    assertLinesMatch(
        List.of(
            "org.objectweb.asm -> java.lang",
            "org.objectweb.asm -> java.io",
            "org.objectweb.asm -> java.lang.reflect",
            "org.objectweb.asm.signature -> java.lang"),
        result.getOutputLines("edges"));

    assertLinesMatch(
        List.of(
            "org.objectweb.asm.AnnotationVisitor -> java.lang.IllegalArgumentException",
            ">> 110 items >>",
            "org.objectweb.asm.signature.SignatureWriter -> java.lang.StringBuffer"),
        result.getOutputLines("items"));
  }

  @Test
  void analyze_ASM_4_1_excluding_java_targets() {
    var jar = jar("asm-4.1");
    var result = new CyclesDetector(jar, (s, t) -> t.startsWith("java")).run(Configuration.of());

    assertEquals(0, result.getExitCode(), () -> "result=" + result);
    assertLinesMatch(List.of(), result.getOutputLines("cycles"));
    assertLinesMatch(List.of(), result.getOutputLines("edges"));
    assertLinesMatch(List.of(), result.getOutputLines("items"));
  }

  @Test
  void analyze_JUnit_Platform_Commons_1_3_1() {
    assertEquals(0, new CyclesDetector(jar("junit-platform-commons-1.3.1")).run());
  }

  @Test
  void analyze_JUnit_Jupiter_Engine_5_3_1() {
    var jar = jar("junit-jupiter-engine-5.3.1");
    var result = new CyclesDetector(jar).run(Configuration.of());

    assertEquals(1, result.getExitCode());

    assertLinesMatch(
        List.of(
            "Adding edge 'org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants' failed. Cycle detected: Anti-edge: org.junit.jupiter.engine.descriptor <-> org.junit.jupiter.engine",
            "Adding edge 'org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine' failed. Cycle detected: Anti-edge: org.junit.jupiter.engine.discovery <-> org.junit.jupiter.engine",
            "Adding edge 'org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants' failed. Cycle detected: Anti-edge: org.junit.jupiter.engine.execution <-> org.junit.jupiter.engine",
            "Adding edge 'org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants' failed. Cycle detected: From org.junit.jupiter.engine.extension over [org.junit.jupiter.engine, org.junit.jupiter.engine.descriptor] and org.junit.jupiter.engine.execution back to org.junit.jupiter.engine.extension"),
        result.getOutputLines("cycles"));

    assertLinesMatch(
        List.of(
            "org.junit.jupiter.engine -> java.lang",
            ">> 98 edges >>",
            "org.junit.jupiter.engine.script -> javax.script"),
        result.getOutputLines("edges"));

    assertLinesMatch(
        List.of(
            "org.junit.jupiter.engine.Constants -> java.lang.Object",
            ">> 1344 items >>",
            "org.junit.jupiter.engine.script.ScriptExecutionManager -> org.junit.platform.commons.util.Preconditions"),
        result.getOutputLines("items"));
  }

  private static Path jar(String name) {
    try {
      return Paths.get(CyclesDetectorTests.class.getResource("/jars/" + name + ".jar").toURI());
    } catch (URISyntaxException e) {
      return fail("Failed to create path for: " + name, e);
    }
  }
}
