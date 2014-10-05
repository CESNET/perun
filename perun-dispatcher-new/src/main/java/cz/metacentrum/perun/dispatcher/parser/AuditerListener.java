package cz.metacentrum.perun.dispatcher.parser;

import cz.metacentrum.perun.dispatcher.processing.EventQueue;

public interface AuditerListener {
	EventQueue getEventQueue();
	void setEventQueue(EventQueue eventQueue);
	String getDispatcherName();
	void setDispatcherName(String dispatcherName);
	void init();
}
