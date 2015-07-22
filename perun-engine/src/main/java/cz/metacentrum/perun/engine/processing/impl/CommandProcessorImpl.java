package cz.metacentrum.perun.engine.processing.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.engine.exceptions.UnknownCommandException;
import cz.metacentrum.perun.engine.model.Command;
import cz.metacentrum.perun.engine.processing.CommandProcessor;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "commandProcessor")
public class CommandProcessorImpl implements CommandProcessor {

	private final static Logger log = LoggerFactory
			.getLogger(CommandProcessorImpl.class);

	@Override
	public void receiveCommand(String command) throws UnknownCommandException {

		String commandString = command.trim().toUpperCase();

		if (commandString.equals(Command.SEND_STATS)) {
			log.debug("Command [" + Command.SEND_STATS + "] [" + command
					+ "] received into CommandProcessor.");

		} else if (commandString.equals(Command.REFRESH_PROCESSING_RULES)) {
			log.debug("Command [" + Command.REFRESH_PROCESSING_RULES + "] ["
					+ command + "] received into CommandProcessor.");

		} else if (commandString.equals(Command.SWITCH_OFF)) {
			log.debug("Command [" + Command.SWITCH_OFF + "] [" + command
					+ "] received into CommandProcessor.");

		} else if (commandString.equals(Command.FORCE_SERVICE_PROPAGATION)) {
			log.debug("Command [" + Command.FORCE_SERVICE_PROPAGATION + "] ["
					+ command + "] received into CommandProcessor.");

		} else {
			throw new UnknownCommandException(commandString);
		}

	}

}
