package integration.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.process.AbstractProcessTool;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class DirectoryListingTests {

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void dir() {
    var tool = new WindowsShellCommand();
    // TODO Configuration.of() ... "hangs" in APT::run() "out = read(inputStream)"
    // 1. read in parallel
    // 2. kill if input is required (or send "exit" after timeout?)
    // https://stackoverflow.com/questions/13008526/runtime-getruntime-execcmd-hanging
    var result = tool.run(Configuration.of("/C", "dir"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    var expectedLines = List.of(">> header >>", ".+README\\.md.*", ">> footer >>");
    assertLinesMatch(expectedLines, result.getOutputLines("out"));
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
}
