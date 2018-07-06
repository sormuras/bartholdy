package de.sormuras.bartholdy.process;

import static java.util.Objects.requireNonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;

/** Ant. */
public class Ant extends AbstractProcessTool {

  private static Path executable(Path home) {
    var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    var executable = "ant" + (win ? ".bat" : "");
    return home.resolve("bin").resolve(executable);
  }

  public static Ant version(Path tools, String version) {
    var zip = String.format("apache-ant-%s-bin.zip", version);
    try {
      // download
      var url = new URL("https://archive.apache.org/dist/ant/binaries/" + zip);
      var zap = tools.resolve(zip);
      if (Files.notExists(zap)) {
        var rbc = Channels.newChannel(url.openStream());
        Files.createDirectories(tools);
        var fos = new FileOutputStream(zap.toFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      }

      // extract
      var jarTool = ToolProvider.findFirst("jar").orElseThrow();
      var listing = new StringWriter();
      var printWriter = new PrintWriter(listing);
      jarTool.run(printWriter, printWriter, "--list", "--file", zap.toString());
      // TODO Find better way to extract root folder name...
      var root = Paths.get(listing.toString().split("\\R")[0]);
      var home = tools.resolve(root);
      if (Files.notExists(home)) {
        jarTool.run(System.out, System.err, "--extract", "--file", zap.toString());
        Files.move(root, home);
        // set executable
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
          executable(home).toFile().setExecutable(true);
        }
      }

      // done
      return new Ant(home.normalize().toAbsolutePath());
    } catch (IOException e) {
      throw new UncheckedIOException("installing failed", e);
    }
  }

  private final Path home;
  private final String version;

  public Ant(Path home) {
    this.home = requireNonNull(home);
    if (!Files.isDirectory(home)) {
      throw new IllegalArgumentException("not a directory: " + home);
    }
    if (!Files.isRegularFile(home.resolve(Paths.get("bin", "ant")))) {
      throw new IllegalArgumentException("`bin/ant` launch script not found in: " + home);
    }
    var jar = home.resolve(Paths.get("lib", "ant.jar"));
    if (!Files.isRegularFile(jar)) {
      throw new IllegalArgumentException("main `lib/ant.jar` not found in: " + home);
    }
    this.version = extractVersion(jar);
  }

  private String extractVersion(Path jar) {
    var version = "?";
    var entry = "/org/apache/tools/ant/version.txt";
    try (var fs = FileSystems.newFileSystem(jar, null)) {
      for (var root : fs.getRootDirectories()) {
        var versionPath = root.resolve(entry);
        if (Files.exists(versionPath)) {
          return Files.readAllLines(versionPath).stream().collect(Collectors.joining());
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("", e);
    }
    return version;
  }

  @Override
  public Path getHome() {
    return home;
  }

  @Override
  protected Path createPathToProgram() {
    return executable(home);
  }

  @Override
  public String getName() {
    return "ant";
  }

  @Override
  public String getVersion() {
    return version;
  }
}
