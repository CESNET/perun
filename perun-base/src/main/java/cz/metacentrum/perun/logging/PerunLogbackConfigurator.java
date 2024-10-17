package cz.metacentrum.perun.logging;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Configurator for logback logging framework. The loading sequence is as follows:
 * <p>
 * <ol>
 * <li>if <b>-Dlogback.configurationFile=/somedir/logback.xml</b> is set, it is used</li>
 * <li>if <b>logback-test.xml</b> is found in classpath, it is used (this happens only during tests)</li>
 * <li>if system property<b>perun.conf.custom</b> defines a directory with logback.xml, it is used</li>
 * <li>if file /etc/perun/logback.xml exists, it is used</li>
 * <li>if logback.xml is found anywhere on the classpath, it is used (like perun-engine, perun-ldapc, ...)</li>
 * <li>if logback-default.xml is found anywhere on the classpath, it is used (RPC)</li>
 * <li>if everything else fails, logback's BasicConfigurator is used</li>
 * </ol>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunLogbackConfigurator extends ContextAwareBase implements Configurator {

  @Override
  public ExecutionStatus configure(LoggerContext loggerContext) {
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(loggerContext);

    File confFile = null;

    // load logback config from system property passed to the app (overrides everything)
    String envConfig = System.getProperty("logback.configurationFile");
    confFile = (envConfig != null) ? Paths.get(envConfig).toFile() : null;
    if (confFile != null && confFile.exists()) {
      System.out.println("Loading logback config from system property.");
      loadConfig(configurator, confFile);
      return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }

    // load logback config for tests from classpath
    try (InputStream configStream = this.getClass().getResourceAsStream("/logback-test.xml")) {
      System.out.println("Loading logback-test.xml file from classpath.");
      configurator.doConfigure(configStream); // loads logback file
      return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    } catch (IOException | JoranException e) {
      System.out.println("Failed to load logback configuration file for tests (logback-test.xml): " + e.getMessage());
    }

    // load logback config from perun config location
    String confDir = System.getProperty("perun.conf.custom", "/etc/perun/");
    confFile = Paths.get(confDir, "logback.xml").toFile();
    if (confFile.exists()) {
      System.out.println("Loading logback.xml config file from perun conf path: '" + confDir + "'.");
      loadConfig(configurator, confFile);
    } else {

      // load default logback config (logback.xml) from classpath (used only by engine, ldapc, auditlogger)
      try (InputStream configStream = this.getClass().getResourceAsStream("/logback.xml")) {
        System.out.println("Loading logback.xml file from classpath.");
        configurator.doConfigure(configStream); // loads logback file
        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
      } catch (IOException | JoranException e) {
        System.out.println("Failed to load default logback configuration file (logback.xml): " + e.getMessage());
      }

      // load default logback config (logback-default.xml) from classpath (used by core / RPC)
      try (InputStream configStream = this.getClass().getResourceAsStream("/logback-default.xml")) {
        System.out.println("Loading logback-default.xml file from classpath.");
        configurator.doConfigure(configStream); // loads logback file
      } catch (IOException | JoranException e) {
        System.out.println("Failed to load default logback configuration file (logback-default.xml): " +
                e.getMessage());
        System.out.println("Falling back to logback basic configurator.");
        BasicConfigurator basicConfigurator = new BasicConfigurator();
        basicConfigurator.setContext(loggerContext);
        basicConfigurator.configure(loggerContext);
      }
    }

    return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;

  }

  /**
   * Load Logback configuration
   * @param configurator Instance of logback`s JoranConfigurator
   * @param confFile Configuration file to load
   */
  private void loadConfig(JoranConfigurator configurator, File confFile) {
    try {
      configurator.doConfigure(confFile.toString()); // loads logback file
    } catch (JoranException e) {
      System.out.println("Failed to load logback configuration file: " + e.getMessage());
    }
  }

}
