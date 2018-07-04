package integration.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.process.AbstractProcessTool;
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
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines = List.of(">> header >>", ".+README\\.md.*", ">> footer >>");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void windowsShellWithoutActualCommandFails() {
    // Override default 9 seconds timeout for faster test execution.
    var conf = Configuration.builder().setTimeoutMillis(789);
    var result = new WindowsShellCommand().run(conf);
    assertEquals(1, result.getExitCode());
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void ls() {
    var tool = new ShellCommand();
    var result = tool.run(Configuration.of("-c", "ls", "-l"));
    assertEquals(0, result.getExitCode(), result.toString());
    assertEquals("", result.getOutput("err"));
    assertTrue(result.getOutput("out").contains("README.md"));
  }

  static class WindowsShellCommand extends AbstractProcessTool {

    @Override
    public String getName() {
      return "cmd";
    }

    @Override
    public String getVersion() {
      return System.getProperty("os.name");
    }

    @Override
    protected String createProgram() {
      return "cmd";
    }
  }

  static class ShellCommand extends AbstractProcessTool {

    @Override
    public String getName() {
      return "sh";
    }

    @Override
    public String getVersion() {
      return System.getProperty("os.name");
    }

    @Override
    protected String createProgram() {
      return "sh";
    }
  }
}
