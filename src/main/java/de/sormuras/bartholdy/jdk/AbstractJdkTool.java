package de.sormuras.bartholdy.jdk;

import static java.lang.System.Logger.Level.DEBUG;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.spi.ToolProvider;

/**
 * Covers provided JDK foundation tools.
 *
 * <ul>
 *   <li>jar
 *   <li>javac
 *   <li>javadoc
 *   <li>javap
 *   <li>jdeps
 *   <li>jlink
 *   <li>jmod
 * </ul>
 *
 * @see ToolProvider#findFirst(String)
 */
abstract class AbstractJdkTool implements Tool {

  private final System.Logger logger;
  private final String name;

  AbstractJdkTool() {
    this.name = getClass().getSimpleName().toLowerCase();
    this.logger = System.getLogger(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getVersion() {
    return Runtime.version().toString();
  }

  @Override
  public Result run(Configuration configuration) {
    logger.log(DEBUG, "Running...");
    var provider = ToolProvider.findFirst(getName()).orElseThrow();
    logger.log(DEBUG, "Found %s", provider);
    var start = Instant.now();
    var out = new StringWriter();
    var err = new StringWriter();
    var args = configuration.getArguments().toArray(new String[0]);
    var code = provider.run(new PrintWriter(out), new PrintWriter(err), args);
    var duration = Duration.between(start, Instant.now());
    logger.log(DEBUG, "Took %s", duration);
    return Result.builder()
        .setExitCode(code)
        .setDuration(duration)
        .setOutput("out", out.toString())
        .setOutput("err", err.toString())
        .build();
  }
}
