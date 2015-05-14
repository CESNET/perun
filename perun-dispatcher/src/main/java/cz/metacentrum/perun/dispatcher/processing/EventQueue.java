package cz.metacentrum.perun.dispatcher.processing;

import cz.metacentrum.perun.dispatcher.model.Event;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface EventQueue {

	void add(Event event);

	Event poll();

	int size();
}
