package cz.metacentrum.perun.dispatcher.processing;

import javax.annotation.Resource;
import javax.sql.DataSource;

import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.dispatcher.model.Event;

import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 * This class wraps AuditerConsumer for Dispatcher.
 *
 * It ensure continuous reading of audit messages and convert them to Events,
 * which are then pushed to EventQueue for further processing by EventProcessor.
 *
 * Its started by DispatcherManager when Spring context is initialized.
 *
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 * @see cz.metacentrum.perun.dispatcher.service.DispatcherManager
 *
 * @author Michal Babacek
 * @author Michal Vocu
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Service(value = "auditerListener")
public class AuditerListener extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(AuditerListener.class);

	private BlockingQueue<Event> eventQueue;
	private DataSource dataSource;
	private Properties dispatcherProperties;

	// ----- setters -------------------------------------

	public BlockingQueue<Event> getEventQueue() {
		return eventQueue;
	}

	@Resource(name = "eventQueue")
	public void setEventQueue(BlockingQueue<Event> eventQueue) {
		this.eventQueue = eventQueue;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	// ----- methods -------------------------------------

	@Override
	public void run() {

		boolean whichOfTwoRules = false;

		String dispatcherName = dispatcherProperties.getProperty("dispatcher.ip.address") + ":" + dispatcherProperties.getProperty("dispatcher.port");

		try {
			AuditerConsumer auditerConsumer;
			while(!shouldStop()) {
				try {
					auditerConsumer = new AuditerConsumer(dispatcherName, dataSource);
					while (!shouldStop()) {
						for (String message : auditerConsumer.getMessagesForParser()) {
							// create event for each message
							Event event = new Event();
							event.setTimeStamp(System.currentTimeMillis());
							if (whichOfTwoRules) {
								event.setHeader("portishead");
								whichOfTwoRules = false;
							} else {
								event.setHeader("clockworkorange");
								whichOfTwoRules = true;
							}
							event.setData(message);
							// pass event to queue for further processing
							eventQueue.put(event);
						}
						Thread.sleep(1000);
					}
				} catch (InternalErrorException e) {
					log.error("Error in AuditerConsumer: " + e.getMessage() + ", trying to recover by getting a new one.");
					auditerConsumer = null;
				}
				Thread.sleep(10000);
			}
			log.debug("AuditerListener has stopped.");
		} catch (InterruptedException e) {
			log.error("Error in AuditerListener: {}" + e);
			throw new RuntimeException("Somebody has interrupted us...", e);
		}

	}

}
