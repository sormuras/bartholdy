package de.sormuras.bartholdy;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.spi.ToolProvider;

public final class Bartholdy {

  private static final System.Logger LOG = System.getLogger(Bartholdy.class.getName());

  public static void main(String[] args) {
    System.out.println("Bartholdy " + version());
  }

  public static Path currentJdkHome() {
    var executable = ProcessHandle.current().info().command().map(Paths::get).orElseThrow();
    // path element count is 3 or higher: "<JAVA_HOME>/bin/java[.exe]"
    return executable.getParent().getParent().toAbsolutePath();
  }

  /** Return the file name of the uri. */
  static String fileName(URI uri) {
    var urlString = uri.getPath();
    var begin = urlString.lastIndexOf('/') + 1;
    return urlString.substring(begin).split("\\?")[0].split("#")[0];
  }

  public static Path download(URI uri, Path tools) {
    return download(uri, fileName(uri), tools);
  }

  public static Path download(URI uri, String fileName, Path tools) {
    var localPath = tools.resolve(fileName);
    if (Files.exists(localPath)) {
      return localPath;
    }
    try {
      var rbc = Channels.newChannel(uri.toURL().openStream());
      Files.createDirectories(tools);
      var fos = new FileOutputStream(localPath.toFile());
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      return localPath;
    } catch (IOException e) {
      throw new UncheckedIOException("download failed", e);
    }
  }

  public static Path install(URI uri, Path tools) {
    return install(uri, fileName(uri), tools);
  }

  public static Path install(URI uri, String zip, Path tools) {
    // uri = "https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.4-bin.zip"
    // zip = "apache-ant-1.10.4-bin.zip"
    var localZip = download(uri, zip, tools);
    try {
      // extract
      var jarTool = ToolProvider.findFirst("jar").orElseThrow();
      var listing = new StringWriter();
      var printWriter = new PrintWriter(listing);
      jarTool.run(printWriter, printWriter, "--list", "--file", localZip.toString());
      // TODO Find better way to extract root folder name...
      var root = Paths.get(listing.toString().split("\\R")[0]);
      var home = tools.resolve(root);
      if (Files.notExists(home)) {
        jarTool.run(System.out, System.err, "--extract", "--file", localZip.toString());
        Files.move(root, home);
      }
      // done
      return home.normalize().toAbsolutePath();
    } catch (IOException e) {
      throw new UncheckedIOException("install failed", e);
    }
  }

  static String read(InputStream inputStream) {
    var reader = new BufferedReader(new InputStreamReader(inputStream));
    var joiner = new StringJoiner(System.lineSeparator());
    reader.lines().iterator().forEachRemaining(joiner::add);
    return joiner.toString();
  }

  public static Path setExecutable(Path path) {
    if (Files.isExecutable(path)) {
      return path;
    }
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      LOG.log(System.Logger.Level.DEBUG, "default file system doesn't support posix");
      return path;
    }
    var program = path.toFile();
    var ok = program.setExecutable(true);
    if (!ok) {
      LOG.log(System.Logger.Level.WARNING, "couldn't set executable flag: " + program);
    }
    return path;
  }

  public static String version() {
    var loader = Bartholdy.class.getClassLoader();
    try (var is = loader.getResourceAsStream("de/sormuras/bartholdy/version.properties")) {
      if (is == null) {
        return "DEVELOPMENT";
      }
      var properties = new Properties();
      properties.load(is);
      return properties.getProperty("version", "UNKNOWN");
    } catch (IOException e) {
      throw new UncheckedIOException("read version failed", e);
    }
  }

  private Bartholdy() {
    throw new UnsupportedOperationException();
  }
}
