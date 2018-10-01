package integration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.AbstractTool;
import de.sormuras.bartholdy.Configuration;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class DirectoryListingTests {

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void dir() {
    var tool = new WindowsShellCommand();
    var result = tool.run(Configuration.of("/c", "dir"));
    assertFalse(result.isTimedOut());
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines = List.of(">> header >>", ".+README\\.md.*", ">> footer >>");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void windowsShellWithoutActualCommandFails() {
    var conf = Configuration.builder().setTimeoutMillis(789).build();
    var result = new WindowsShellCommand().run(conf);
    assertAll(
        "Checking -> " + result,
        () -> assertTrue(result.isTimedOut()),
        () -> assertEquals(1, result.getExitCode()),
        () -> assertFalse(result.getOutput("out").isEmpty()),
        () -> assertEquals("", result.getOutput("err")));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void windowsShellCallingPauseTimesOut() {
    var conf = Configuration.builder().setTimeoutMillis(789).setArguments("/c", "pause").build();
    var result = new WindowsShellCommand().run(conf);
    assertTrue(result.isTimedOut());
    assertEquals(1, result.getExitCode());
    assertFalse(result.getOutput("out").isEmpty());
    assertEquals("", result.getOutput("err"));
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void ls() {
    var tool = new ShellCommand();
    var result = tool.run(Configuration.of("-c", "ls", "-l"));
    assertEquals(0, result.getExitCode(), result.toString());
    assertEquals("", result.getOutput("err"));
    assertTrue(result.getOutput("out").contains("README.md"), "result=" + result);
  }

  static class WindowsShellCommand extends AbstractTool {

    @Override
    public String getName() {
      return "cmd";
    }

    @Override
    public String getVersion() {
      return System.getProperty("os.name");
    }

    @Override
    protected String createProgram(Path __) {
      return "cmd";
    }
  }

  static class ShellCommand extends AbstractTool {

    @Override
    public String getName() {
      return "sh";
    }

    @Override
    public String getVersion() {
      return System.getProperty("os.name");
    }

    @Override
    protected String createProgram(Path __) {
      return "sh";
    }
  }
}
