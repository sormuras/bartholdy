package de.sormuras.bartholdy.tool;

import static java.util.Objects.requireNonNull;

import de.sormuras.bartholdy.AbstractTool;
import de.sormuras.bartholdy.Bartholdy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Collectors;

/** Ant. */
public class Ant extends AbstractTool {

  private static final System.Logger LOG = System.getLogger(Ant.class.getName());

  public static Ant install(String version, Path tools) {
    var host = "https://archive.apache.org/dist/";
    var uri = String.format("%s/ant/binaries/apache-ant-%s-bin.zip", host, version);
    var home = Bartholdy.install(URI.create(uri), tools);
    return new Ant(home);
  }

  private final Path home;
  private final String version;
  private final Path executable;

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
    this.version = getVersion(jar);
    this.executable = getExecutable(home);
  }

  private Path getExecutable(Path home) {
    var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    var name = "ant" + (win ? ".bat" : "");
    var path = home.resolve("bin").resolve(name);
    // set executable flag
    if (!Files.isExecutable(path)) {
      if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
        var program = path.toFile();
        var ok = program.setExecutable(true);
        if (!ok) {
          LOG.log(System.Logger.Level.WARNING, "couldn't set executable flag: " + program);
        }
      }
    }
    return path;
  }

  @Override
  public Path getHome() {
    return home;
  }

  @Override
  protected Path createPathToProgram() {
    return executable;
  }

  @Override
  public String getName() {
    return "ant";
  }

  @Override
  public String getVersion() {
    return version;
  }

  /** Get version from "version.txt" in "lib/ant.jar". */
  private String getVersion(Path jar) {
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
      throw new UncheckedIOException("extract version failed", e);
    }
    return version;
  }
}
