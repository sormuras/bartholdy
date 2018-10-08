package de.sormuras.bartholdy.tool;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.jdk.Jdeps;
import de.sormuras.bartholdy.util.AcyclicDirectedGraph;
import de.sormuras.bartholdy.util.CyclicEdgeException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/** Package cycles detector tool. */
public class CyclesDetector implements Tool {

  private final Path path;

  public CyclesDetector(Path path) {
    this.path = path;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Result run(Configuration configuration) {
    var result = Result.builder();
    try {
      var cycles = new ArrayList<String>();
      detectCycles(path, cycles);
      result.setExitCode(cycles.isEmpty() ? 0 : 1);
      result.setOutput("err", cycles);
    } catch (Exception e) {
      e.printStackTrace();
      result.setExitCode(-1);
    }
    return result.build();
  }

  private void detectCycles(Path path, List<String> cycles) throws Exception {
    var configuration = Configuration.builder();
    if (new JarFile(path.toFile()).isMultiRelease()) {
      configuration.addArgument("--multi-release").addArgument(Runtime.version().feature());
    }
    configuration.addArgument("-verbose:class");
    configuration.addArgument(path);

    var result = new Jdeps().run(configuration.build());
    if (result.getExitCode() != 0) {
      throw new RuntimeException("Running jdeps failed: " + result);
    }

    var lines =
        result
            .getOutputLines("out")
            .stream()
            .filter(line -> line.startsWith("   "))
            .filter(line -> line.contains("->"))
            .map(String::trim)
            .collect(Collectors.toList());

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
    for (var item : items) {
      try {
        graph.addEdge(item.sourcePackage, item.targetPackage);
      } catch (CyclicEdgeException e) {
        cycles.add(item.toString());
      }
    }
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

  private static class Item {
    private final String sourceClass;
    private final String targetClass;
    private final String sourcePackage;
    private final String targetPackage;

    Item(String line) {
      var split = line.split("->");
      var target = split[1].trim();
      this.sourceClass = classNameOf(split[0].trim());
      this.targetClass = classNameOf(target.substring(0, target.indexOf(' ')));
      this.sourcePackage = packageNameOf(sourceClass);
      this.targetPackage = packageNameOf(targetClass);
    }

    @Override
    public String toString() {
      return sourceClass + " -> " + targetClass;
    }
  }
}
