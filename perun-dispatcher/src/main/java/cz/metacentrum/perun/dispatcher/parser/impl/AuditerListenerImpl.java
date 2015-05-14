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
	private final static Logger log = LoggerFactory
			.getLogger(ParserGrouper.class);
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
			this.auditerConsumer = new AuditerConsumer(dispatcherName,
					dataSource);
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
				}
				Thread.sleep(1000);
			}
		} catch (InternalErrorException e) {
			throw new RuntimeException("AuditerConsumer failed...");
		} catch (InterruptedException e) {
			throw new RuntimeException("Somebody has interrupted us...");
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
