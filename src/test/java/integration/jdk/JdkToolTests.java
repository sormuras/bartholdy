package integration.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.jdk.Jar;
import de.sormuras.bartholdy.jdk.Javac;
import de.sormuras.bartholdy.jdk.Javadoc;
import de.sormuras.bartholdy.jdk.Javap;
import de.sormuras.bartholdy.jdk.Jdeps;
import de.sormuras.bartholdy.jdk.Jlink;
import de.sormuras.bartholdy.jdk.Jmod;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class JdkToolTests {

  private Stream<Tool> jdkToolStream() {
    return Stream.of(
        new Jar(), new Javac(), new Javadoc(), new Javap(), new Jdeps(), new Jlink(), new Jmod());
  }

  @TestFactory
  Stream<DynamicTest> help() {
    return jdkToolStream().map(tool -> dynamicTest(tool.getName(), () -> help(tool)));
  }

  private void help(Tool tool) {
    var configuration = Configuration.of("--help");
    assertRunReturnsZeroAndIsFast(tool, configuration);
  }

  @TestFactory
  Stream<DynamicTest> version() {
    return jdkToolStream().map(tool -> dynamicTest(tool.getName(), () -> version(tool)));
  }

  private void version(Tool tool) {
    var version = Runtime.version();
    assertEquals(version.toString(), tool.getVersion());
    var oneDashSet = List.of("javap");
    var arg = (oneDashSet.contains(tool.getName()) ? "-" : "--") + "version";
    var configuration = Configuration.of(arg);
    assertRunReturnsZeroAndIsFast(tool, configuration);
  }

  private void assertRunReturnsZeroAndIsFast(Tool tool, Configuration configuration) {
    var result = tool.run(configuration);
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    assertFalse(result.getOutput("out").isEmpty());
    assertTrue(Duration.ofSeconds(1).compareTo(result.getDuration()) >= 0);
  }
}
