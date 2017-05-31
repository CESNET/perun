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
 * <li>if logback.xml is found anywhere on the classpath, it is used (like perun-engine)</li>
 * <li>if system property<b>perun.conf.custom</b> defines a directory with logback.xml, it is used</li>
 * <li>if file /etc/perun/logback.xml exists, it is used</li>
 * <li>file logback-default.xml from perun-base is loaded</li>
 * <li>if everything else fails, logback's BasicConfigurator is used</li>
 * </ol>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunLogbackConfigurator extends ContextAwareBase implements Configurator {

	@Override
	public void configure(LoggerContext loggerContext) {
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);

		String confDir = System.getProperty("perun.conf.custom", "/etc/perun/");
		File confFile = Paths.get(confDir, "logback.xml").toFile();
		if (confFile.exists()) {
			System.out.println("Loading logback config file " + confFile);
			try {
				configurator.doConfigure(confFile.toString()); // loads logback file
			} catch (JoranException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Loading logback-default.xml file from classpath");
			try (InputStream configStream = this.getClass().getResourceAsStream("/logback-default.xml")) {
				configurator.doConfigure(configStream); // loads logback file
				configStream.close();
			} catch (IOException | JoranException e) {
				e.printStackTrace();
				System.out.println("Falling back to logback basic configurator");
				BasicConfigurator basicConfigurator = new BasicConfigurator();
				basicConfigurator.setContext(loggerContext);
				basicConfigurator.configure(loggerContext);
			}
		}
	}
}
