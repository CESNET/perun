package cz.metacentrum.perun.dispatcher.processing.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventQueue")
public class EventQueueImpl implements EventQueue {

	private ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<Event>();

	public EventQueueImpl() {
	}

	@Override
	public void add(Event event) {
		// /THIS WAS NOT AN ATOMIC OPERATION
		// if (log.isDebugEnabled()) {
		// log.debug("(Queue size:" + eventQueue.size() + "); Adding event:" +
		// event.toString());
		// }
		eventQueue.add(event);
	}

	@Override
	public Event poll() {
		// /THIS WAS NOT AN ATOMIC OPERATION
		// Event event = eventQueue.poll();
		// if (log.isDebugEnabled()) {
		// log.debug("(Queue size:" + eventQueue.size() + "); Polling event:" +
		// event.toString());
		// }
		return eventQueue.poll();
	}

	@Override
	public int size() {
		return eventQueue.size();
	}

}
