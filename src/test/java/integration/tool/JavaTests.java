package integration.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.Java;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class JavaTests {

  @Test
  void version() {
    var tool = new Java();
    assertEquals(Runtime.version().toString(), tool.getVersion());
    var result = tool.run(Configuration.of("--version"));
    assertEquals(0, result.getExitCode());
    assertEquals("", result.getOutput("err"));
    assertTrue(result.getOutput("out").contains(Runtime.version().toString()));
  }

  @Test
  void runJavaWithLongCommandLine() {
    var configuration = longCommandLineConfigurationBuilder(4000).addArgument("end.").build();
    var result = new Java().run(configuration);
    var expected = List.of("--dry-run", "--version", ">> many args >>", "end.");
    assertLinesMatch(expected, configuration.getArguments());
    assertTrue(result.getOutput("out").contains(Runtime.version().toString()));
  }

  @Test
  void creatingArgumentsFileFailsForNonExistingTemporaryDirectory() {
    var configuration =
        longCommandLineConfigurationBuilder(4001)
            .setTemporaryDirectory(Paths.get("folder-that-does-not-exist"))
            .build();
    var e = assertThrows(UncheckedIOException.class, () -> new Java().run(configuration));
    assertEquals("creating temporary arguments file failed", e.getMessage());
    assertEquals(NoSuchFileException.class, e.getCause().getClass());
    assertTrue(e.getCause().getMessage().contains("folder-that-does-not-exist"));
  }

  private static Configuration.Builder longCommandLineConfigurationBuilder(int args) {
    var builder = Configuration.builder();
    builder.addArgument("--dry-run");
    builder.addArgument("--version");
    for (var i = 0; i <= args; i++) {
      builder.addArgument(String.format("arg-%04d", i));
    }
    return builder;
  }
}
