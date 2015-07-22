package cz.metacentrum.perun.dispatcher.parser;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface Parser extends Runnable {

	void stop();

	boolean isRunning();

}
