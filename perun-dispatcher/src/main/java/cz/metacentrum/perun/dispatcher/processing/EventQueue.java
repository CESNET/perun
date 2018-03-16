package cz.metacentrum.perun.dispatcher.processing;

import cz.metacentrum.perun.dispatcher.model.Event;

/**
 * Wrapper on ConcurrentLinkedQueue (FIFO) used to pass Events between two processes.
 * Queue is filled by AuditerListener. Events are taken by EventProcessor.
 *
 * @see cz.metacentrum.perun.dispatcher.processing.AuditerListener
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface EventQueue {

	/**
	 * Add Event to the queue (FIFO).
	 *
	 * @param event Event to be added
	 */
	void add(Event event);

	/**
	 * Takes Event from a queue (FIFO).
	 *
	 * @return Event
	 */
	Event poll();

	/**
	 * Get size of Event queue (Events to be processed).
	 *
	 * @return Current size of EventQueue
	 */
	int size();

}
