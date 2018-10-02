package integration;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.jdk.Jdeps;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectCyclesTests {

  @Test
  void analyze_JUnit_Platform_Commons_1_3_1() throws Exception {
    analyze("junit-platform-commons-1.3.1");
  }

  @Test
  void analyze_ASM_4_1() throws Exception {
    analyze("asm-4.1");
  }

  private void analyze(String name) throws Exception {
    var path = Paths.get(getClass().getResource("/jars/" + name + ".jar").toURI());
    assertTrue(Files.exists(path));

    var jar = new JarFile(path.toFile());

    var builder = Configuration.builder();
    if (jar.isMultiRelease()) {
      builder.addArgument("--multi-release");
      builder.addArgument(Runtime.version().feature());
    }

    var destination = Paths.get("build", "test-output", getClass().getSimpleName(), name);
    var configuration = builder //
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
            .collect(Collectors.toList());
    Files.write(raw, lines);
  }
}
