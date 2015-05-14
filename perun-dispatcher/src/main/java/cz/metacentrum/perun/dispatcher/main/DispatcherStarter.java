package cz.metacentrum.perun.dispatcher.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class DispatcherStarter {

	private final static Logger log = LoggerFactory.getLogger(DispatcherStarter.class);

	private DispatcherManager dispatcherManager;
	@Autowired
	private AbstractApplicationContext springCtx;

	public static void main(String[] arg) {
		DispatcherStarter starter = new DispatcherStarter();
		
                starter.springCtx = new ClassPathXmlApplicationContext("/perun-dispatcher-applicationcontext.xml", "/perun-dispatcher-applicationcontext-jdbc.xml");
                // no need to call init explicitely, gets called by spring when initializing this bean
	}
	
	/**
	 * Initialize integrated dispatcher.
	 */
	public final void init() {
		try {

			dispatcherManager = springCtx.getBean("dispatcherManager", DispatcherManager.class);
			springCtx.registerShutdownHook();

			// Register into the database
			// DO NOT: dispatcherStarter.dispatcherManager.registerDispatcher();
			// Start HornetQ server
			dispatcherManager.startPerunHornetQServer();
			// Start System Queue Processor
			dispatcherManager.startProcessingSystemMessages();
			// Prefetch rules for all the Engines in the Perun DB and create
			// Dispatcher queues
			dispatcherManager.prefetchRulesAndDispatcherQueues();
			// reload tasks from database
			dispatcherManager.loadSchedulingPool();
			// Start parsers (mining data both from Grouper and PerunDB)
			dispatcherManager.startParsingData();
			// Start Event Processor
			dispatcherManager.startPocessingEvents();
			log.info("Done. Perun-Dispatcher has started.");
		} catch (PerunHornetQServerException e) {
			log.error(e.toString(), e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

	}

}
