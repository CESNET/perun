package cz.metacentrum.perun.dispatcher.main;

import java.awt.HeadlessException;
import java.awt.SplashScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;

import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class DispatcherStarter {

	private final static Logger log = LoggerFactory
			.getLogger(DispatcherStarter.class);

	private DispatcherManager dispatcherManager;
	private AbstractApplicationContext springCtx;

	public DispatcherStarter() {
		springCtx = new ClassPathXmlApplicationContext(
				"/perun-dispatcher-applicationcontext.xml",
				"/perun-dispatcher-applicationcontext-jdbc.xml");
		this.dispatcherManager = springCtx.getBean("dispatcherManager",
				DispatcherManager.class);
	}

	public static void main(String[] args) {
		try {

			DispatcherStarter dispatcherStarter = new DispatcherStarter();
			// Just for the Spring IoC to exit gracefully...
			dispatcherStarter.springCtx.registerShutdownHook();

			// Register into the database
			dispatcherStarter.dispatcherManager.registerDispatcher();
			// Start HornetQ server
			dispatcherStarter.dispatcherManager.startPerunHornetQServer();
			// Start System Queue Processor
			dispatcherStarter.dispatcherManager.startProcessingSystemMessages();
			// Prefetch rules for all the Engnes in the Perun DB and create
			// Dispatcher queues
			dispatcherStarter.dispatcherManager
					.prefetchRulesAndDispatcherQueues();
			// reload tasksk from database
			dispatcherStarter.dispatcherManager.loadSchedulingPool();
			// Start parsers (mining data both from Grouper and PerunDB)
			dispatcherStarter.dispatcherManager.startParsingData();
			// Start Event Processor
			dispatcherStarter.dispatcherManager.startPocessingEvents();
			log.info("Done. Perun-Dispatcher has started.");
			System.out.println("Done. Perun-Dispatcher has started.");
			if (SplashScreen.getSplashScreen() != null) {
				SplashScreen.getSplashScreen().close();
			}
		} catch (HeadlessException e) {
			// Doesn't matter... (We can't show splash screen on a server :-))
		} catch (PerunHornetQServerException e) {
			log.error(e.toString(), e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}
}
