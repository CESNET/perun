package cz.metacentrum.perun.dispatcher.processing.impl;

import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation of EventQueue.
 *
 * @see cz.metacentrum.perun.dispatcher.processing.EventQueue
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "eventQueue")
public class EventQueueImpl implements EventQueue {

	private ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<Event>();

	public EventQueueImpl() {
	}

	@Override
	public void add(Event event) {
		eventQueue.add(event);
	}

	@Override
	public Event poll() {
		return eventQueue.poll();
	}

	@Override
	public int size() {
		return eventQueue.size();
	}

}
