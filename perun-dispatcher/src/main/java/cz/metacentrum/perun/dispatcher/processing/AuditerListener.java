package cz.metacentrum.perun.dispatcher.processing;

import javax.annotation.Resource;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
	private Properties dispatcherProperties;
	private Perun perun;
	private PerunSession sess;

	// ----- setters -------------------------------------

	public BlockingQueue<Event> getEventQueue() {
		return eventQueue;
	}

	@Resource(name = "eventQueue")
	public void setEventQueue(BlockingQueue<Event> eventQueue) {
		this.eventQueue = eventQueue;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public Perun getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	// ----- methods -------------------------------------

	@Override
	public void run() {

		boolean whichOfTwoRules = false;

		String dispatcherName = dispatcherProperties.getProperty("dispatcher.ip.address") + ":" + dispatcherProperties.getProperty("dispatcher.port");

		try {

			try {
				if (sess == null) {
					sess = perun.getPerunSession(new PerunPrincipal(
									dispatcherProperties.getProperty("perun.principal.name"),
									dispatcherProperties.getProperty("perun.principal.extSourceName"),
									dispatcherProperties.getProperty("perun.principal.extSourceType")),
							new PerunClient());
				}
			} catch (InternalErrorException e1) {
				log.error("Error establishing perun session in AuditerListener.", e1);
				// we can't continue without session
				stop();
			}


			while (!shouldStop()) {
				try {
					for (AuditEvent message : perun.getAuditMessagesManager().pollConsumerEvents(sess, dispatcherName)) {
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
				} catch (InternalErrorException | PrivilegeException ex) {
					log.error("AuditerListener couldn't get AuditEvents.", ex);
					Thread.sleep(1000);
				}
			}
			log.debug("AuditerListener has stopped.");
		} catch (InterruptedException e) {
			log.error("Error in AuditerListener: {}" + e);
			throw new RuntimeException("Somebody has interrupted us...", e);
		}

	}

}
