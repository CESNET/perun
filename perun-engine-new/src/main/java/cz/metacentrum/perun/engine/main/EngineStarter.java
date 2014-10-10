package cz.metacentrum.perun.engine.main;

import java.awt.HeadlessException;
import java.awt.SplashScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.engine.exceptions.DispatcherNotConfiguredException;
import cz.metacentrum.perun.engine.exceptions.EngineNotConfiguredException;
import cz.metacentrum.perun.engine.service.EngineManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class EngineStarter {
	private final static Logger log = LoggerFactory
			.getLogger(EngineStarter.class);

	private EngineManager engineManager;
	private AbstractApplicationContext springCtx;

	public EngineStarter() {
		try {
			springCtx = new ClassPathXmlApplicationContext(
					"classpath:perun-engine-applicationcontext.xml",
					"classpath:perun-engine-applicationcontext-jdbc.xml");
			this.engineManager = springCtx.getBean("engineManager",
					EngineManager.class);
		} catch (Exception e) {
			log.error("Application context loading error.", e);
		}
	}

	public static void main(String[] args) {
		try {
			EngineStarter engineStarter = new EngineStarter();
			// Just for the Spring IoC to exit gracefully...
			engineStarter.springCtx.registerShutdownHook();
			try {
				log.info("Gonna register Engine and load Dispatcher settings...");
				engineStarter.engineManager.registerEngine();
			} catch (EngineNotConfiguredException e) {
				log.error(
						"This engine instance is not properly configured in PerunDB.",
						e);
				System.out
						.print("Engine is not able to proceed. We gonna release reousrces and die. See log for details.");
				System.exit(666);
			} catch (DispatcherNotConfiguredException e) {
				log.error(
						"The Dispatcher instance is not properly configured in PerunDB.",
						e);
			}
			log.info("Gonna loadSchedulingPool from file.");
			// Yes, we do this in the main thread because we do want to carry
			// this initial steps out sequentially.
			engineStarter.engineManager.loadSchedulingPool();
			log.info("Gonna start Messaging...");
			engineStarter.engineManager.startMessaging();
			log.info("Gonna switch all the unfinished Tasks to ERROR...");
			engineStarter.engineManager.switchUnfinishedTasksToERROR();
			log.info("Done. Perun-Engine has started.");
			System.out.println("Done. Perun-Engine has started.");
			if (SplashScreen.getSplashScreen() != null) {
				SplashScreen.getSplashScreen().close();
			}
		} catch (HeadlessException e) {
			// Doesn't matter... (We can't show splash screen on a server :-))
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

}
