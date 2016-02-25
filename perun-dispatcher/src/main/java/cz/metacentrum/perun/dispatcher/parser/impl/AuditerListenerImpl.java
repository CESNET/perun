package cz.metacentrum.perun.dispatcher.parser.impl;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.parser.AuditerListener;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;

@org.springframework.stereotype.Service(value = "auditerListener")
public class AuditerListenerImpl implements AuditerListener {

	private final static Logger log = LoggerFactory.getLogger(ParserGrouper.class);
	private EventQueue eventQueue;
	private AuditerConsumer auditerConsumer;
	private String dispatcherName;
	private boolean running = true;
	@Autowired
	private DataSource dataSource;

	boolean whichOfTwoRules = false;

	@Override
	public void init() {
		try {
			while(running) {
				try {
					this.auditerConsumer = new AuditerConsumer(dispatcherName, dataSource);
					while (running) {
						for (String message : auditerConsumer.getMessagesForParser()) {
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
							eventQueue.add(event);
							// Thread.sleep(100);
						}
					}
				} catch (InternalErrorException e) {
					log.error("Error in AuditerConsumer: " + e.getMessage() + ", trying to recover by getting a new one.");
					this.auditerConsumer = null;
				}
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			log.error("Error in AuditerListerImpl: {}" + e);
			throw new RuntimeException("Somebody has interrupted us...", e);
		}
	}

	@Override
	public EventQueue getEventQueue() {
		return eventQueue;
	}

	@Override
	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	@Override
	public String getDispatcherName() {
		return dispatcherName;
	}

	@Override
	public void setDispatcherName(String dispatcherName) {
		this.dispatcherName = dispatcherName;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}
