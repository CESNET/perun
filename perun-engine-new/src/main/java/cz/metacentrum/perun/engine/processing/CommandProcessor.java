package cz.metacentrum.perun.engine.processing;

import cz.metacentrum.perun.engine.exceptions.UnknownCommandException;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface CommandProcessor {

	public void receiveCommand(String command) throws UnknownCommandException;

}
