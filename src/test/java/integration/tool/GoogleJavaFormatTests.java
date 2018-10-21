package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.GoogleJavaFormat;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GoogleJavaFormatTests {

  @Test
  void google_java_format_1_5() {
    version("1.5");
  }

  @Test
  void google_java_format_1_6() {
    version("1.6");
  }

  private void version(String version) {
    var destination = Path.of("build", "bartholdy", "tools");
    var tool = GoogleJavaFormat.install(version, destination);
    assertEquals("google-java-format", tool.getName());
    assertEquals("java", tool.getProgram());
    assertEquals("google-java-format-" + version + "-all-deps.jar", tool.getVersion());

    var result = tool.run(Configuration.of("-version"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("out"));
    assertEquals("google-java-format: Version " + version, result.getOutput("err"));
  }
}
