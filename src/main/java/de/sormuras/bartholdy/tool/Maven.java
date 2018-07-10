package de.sormuras.bartholdy.tool;

import static java.util.Objects.requireNonNull;

import de.sormuras.bartholdy.AbstractTool;
import de.sormuras.bartholdy.Bartholdy;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/** Maven. */
public class Maven extends AbstractTool {

  private static final System.Logger LOG = System.getLogger(Maven.class.getName());

  public static Maven install(String version, Path tools) {
    var host = "https://archive.apache.org/dist/maven/maven-3/" + version;
    var uri = String.format("%s/binaries/apache-maven-%s-bin.zip", host, version);
    var home = Bartholdy.install(URI.create(uri), tools);
    return new Maven(home);
  }

  private final Path home;
  private final String version;
  private final Path executable;

  public Maven(Path home) {
    this.home = requireNonNull(home);
    if (!Files.isDirectory(home)) {
      throw new IllegalArgumentException("not a directory: " + home);
    }
    if (!Files.isRegularFile(home.resolve(Paths.get("bin", "mvn")))) {
      throw new IllegalArgumentException("`bin/mvn` launch script not found in: " + home);
    }
    this.version = "TODO";
    this.executable = getExecutable(home);
  }

  private Path getExecutable(Path home) {
    var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    var name = "mvn" + (win ? ".cmd" : "");
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
    return "maven";
  }

  @Override
  public String getVersion() {
    return version;
  }
}
