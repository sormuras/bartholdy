package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.jdk.Jdeps;
import de.sormuras.bartholdy.util.AcyclicDirectedGraph;
import de.sormuras.bartholdy.util.CyclicEdgeException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DetectCyclesTests {

  private static class Item {
    private final String sourceClass;
    private final String targetClass;
    private final String sourcePackage;
    private final String targetPackage;

    Item(String line) {
      var split = line.split("->");
      this.sourceClass = classNameOf(split[0]);
      this.targetClass = classNameOf(split[1]);
      this.sourcePackage = packageNameOf(sourceClass);
      this.targetPackage = packageNameOf(targetClass);
    }

    @Override
    public String toString() {
      return sourceClass + " -> " + targetClass;
    }
  }

  @Test
  void analyze_JUnit_Platform_Commons_1_3_1() throws Exception {
    var cycles = analyze("junit-platform-commons-1.3.1");
    assertEquals(0, cycles.size());
  }

  @Test
  void analyze_JUnit_Jupiter_Engine_5_3_1() throws Exception {
    var cycles = analyze("junit-jupiter-engine-5.3.1");
    assertLinesMatch(
        List.of(
            "org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants",
            "org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine",
            "org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants",
            "org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants"),
        cycles.stream().map(Object::toString).collect(Collectors.toList()));
  }

  @Test
  void analyze_ASM_4_1() throws Exception {
    var cycles = analyze("asm-4.1");
    assertEquals(0, cycles.size());
  }

  private Collection<Item> analyze(String name) throws Exception {
    var path = Paths.get(getClass().getResource("/jars/" + name + ".jar").toURI());
    assertTrue(Files.exists(path));

    var jar = new JarFile(path.toFile());

    var builder = Configuration.builder();
    if (jar.isMultiRelease()) {
      builder.addArgument("--multi-release").addArgument(Runtime.version().feature());
    }

    var destination = Paths.get("build", "test-output", getClass().getSimpleName(), name);
    var configuration =
        builder
            .addArgument("--dot-output")
            .addArgument(destination) // Specifies the destination directory for DOT file output.
            .addArgument("-verbose:class") // Prints class-level dependencies.
            .addArgument("-filter:none") // No "-filter:package" and no "-filter:archive" filtering.
            .addArgument(jar.getName()) // JAR file to analyze.
            .build();

    var result = new Jdeps().run(configuration);

    assertEquals(0, result.getExitCode(), "result = " + result);
    assertEquals("", result.getOutput("out"), "output log isn't empty");
    assertEquals("", result.getOutput("err"), "error log isn't empty");

    var summary = destination.resolve("summary.dot");
    var dot = destination.resolve(name + ".jar.dot");
    var raw = destination.resolve(name + ".raw.dot");

    assertTrue(Files.exists(summary), summary + " doesn't exist");
    assertTrue(Files.exists(dot), dot + " doesn't exist");

    var lines =
        Files.readAllLines(dot) //
            .stream() //
            .filter(line -> line.contains("->"))
            .map(line -> line.replaceAll(" \\(.+\\)", "")) // strip off locations
            .map(line -> line.replace('"', ' ')) // blank out double quotes
            .map(line -> line.replace(';', ' ')) // blank out semicolons
            .map(String::trim)
            .collect(Collectors.toList());
    Files.write(raw, lines);

    var nodes = new TreeSet<String>();
    var items = new ArrayList<Item>();
    for (var line : lines) {
      var item = new Item(line);
      if (item.sourcePackage.equals(item.targetPackage)) {
        continue;
      }

      if (ignorePackage(item.targetPackage)) {
        continue;
      }
      nodes.add(item.sourcePackage);
      nodes.add(item.targetPackage);
      items.add(item);
    }

    var graph = new AcyclicDirectedGraph(nodes);
    var cycles = new ArrayList<Item>();
    for (var item : items) {
      try {
        graph.addEdge(item.sourcePackage, item.targetPackage);
      } catch (CyclicEdgeException e) {
        cycles.add(item);
      }
    }
    return cycles;
  }

  private static Set<String> IGNORE_TARGET_STARTING_WITH = Set.of("java.", "javax.");

  private static String classNameOf(String raw) {
    raw = raw.trim();
    // strip `"` from names
    raw = raw.replaceAll("\"", "");
    // remove leading artifacts, like "9/" from a multi-release jar
    var indexOfSlash = raw.indexOf('/');
    if (indexOfSlash >= 0) {
      raw = raw.substring(indexOfSlash + 1);
    }
    return raw;
  }

  private static String packageNameOf(String className) {
    var indexOfLastDot = className.lastIndexOf('.');
    if (indexOfLastDot < 0) {
      return "";
    }
    return className.substring(0, indexOfLastDot);
  }

  private static boolean ignorePackage(String className) {
    for (var prefix : IGNORE_TARGET_STARTING_WITH) {
      if (className.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
