package cz.metacentrum.perun.engine.main;

import java.awt.HeadlessException;
import java.awt.SplashScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.engine.service.EngineManager;

/**
 * Starting class for perun-engine component.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @author Slávek Licehammer <glory@ics.muni.cz>
 */
public class EngineStarter {

	private final static Logger log = LoggerFactory.getLogger(EngineStarter.class);

	private EngineManager engineManager;
	private AbstractApplicationContext springCtx;

	public EngineStarter() {
		try {
			springCtx = new ClassPathXmlApplicationContext( "classpath:perun-engine.xml", "classpath:perun-engine-scheduler.xml");
			this.engineManager = springCtx.getBean("engineManager", EngineManager.class);
		} catch (Exception e) {
			log.error("Application context loading error.", e);
		}
	}

	public static void main(String[] args) {
		try {
			EngineStarter engineStarter = new EngineStarter();
			// Just for the Spring IoC to exit gracefully...
			engineStarter.springCtx.registerShutdownHook();
			// Yes, we do this in the main thread because we do want to carry
			// this initial steps out sequentially.
			// FIXME - We can't reload pool from file since ExecService, Service and Facility are missing
			// FIXME in local DB but are required by mapper when selecting Task.
			// engineStarter.engineManager.loadSchedulingPool();
			//log.info("Gonna loadSchedulingPool from file.");
			log.info("Gonna start Messaging...");
			engineStarter.engineManager.startMessaging();
			// FIXME - There is not reason to switch Tasks in local pool because of above FIXME.
			//log.info("Gonna switch all the unfinished Tasks to ERROR...");
			//engineStarter.engineManager.switchUnfinishedTasksToERROR();
			log.info("Done. Perun-Engine has started.");
			System.out.println("Done. Perun-Engine has started.");
			if (SplashScreen.getSplashScreen() != null) {
				SplashScreen.getSplashScreen().close();
			}
		} catch (HeadlessException e) {
			// Doesn't matter... (We can't show splash screen on a server :-))
		} catch (Exception e) {
			log.error("Error: {}" , e);
		}
	}

}
