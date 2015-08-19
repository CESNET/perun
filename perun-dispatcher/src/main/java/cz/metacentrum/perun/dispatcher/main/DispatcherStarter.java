package cz.metacentrum.perun.dispatcher.main;

import java.util.Properties;

import javax.annotation.PreDestroy;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

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
	@Autowired
	private Properties dispatcherPropertiesBean;
	@Autowired
	@Qualifier("perunScheduler")
	private SchedulerFactoryBean perunScheduler;
	
	public static void main(String[] arg) {
		DispatcherStarter starter = new DispatcherStarter();
		
                starter.springCtx = new ClassPathXmlApplicationContext("/perun-dispatcher.xml", "/perun-dispatcher-scheduler.xml");
                // no need to call init explicitly, gets called by spring when initializing this bean
	}
	
	/**
	 * Initialize integrated dispatcher.
	 */
	public final void init() {

		String dispatcherEnabled = dispatcherPropertiesBean.getProperty("dispatcher.enabled");
		if(dispatcherEnabled != null && !Boolean.parseBoolean(dispatcherEnabled)) {
			try {
				// stop scheduler
				perunScheduler.stop();
				// stop all triggers
				perunScheduler.getScheduler().pauseAll();
				log.debug("Dispatcher startup disabled by configuration.");
			} catch (SchedulerException ex) {
				log.error("Unable to stop dispatcher scheduler: {}", ex);
			}
			// skip start of HornetQ and other dispatcher jobs
			return;
		}

		try {

			dispatcherManager = springCtx.getBean("dispatcherManager", DispatcherManager.class);
			if(springCtx instanceof WebApplicationContext) {
				// do nothing here
			} else {
				springCtx.registerShutdownHook();
			}
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
			dispatcherManager.startProcessingEvents();
			log.info("Done. Perun-Dispatcher has started.");
		} catch (PerunHornetQServerException e) {
			log.error(e.toString(), e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

	}

	@PreDestroy
	public void destroy() {
		try {
			// stop current scheduler
			perunScheduler.stop();
			// stop job triggers
			perunScheduler.getScheduler().pauseAll();
		} catch (SchedulerException ex) {
			log.error("Unable to stop dispatcher scheduler: {}", ex);
		}
		// stop currently running jobs
		dispatcherManager.stopProcessingEvents();
		dispatcherManager.stopParsingData();
		dispatcherManager.stopProcessingSystemMessages();
		dispatcherManager.stopPerunHornetQServer();
	}
}
