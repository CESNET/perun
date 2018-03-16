package cz.metacentrum.perun.engine.processing.impl;

import cz.metacentrum.perun.engine.exceptions.UnknownCommandException;
import cz.metacentrum.perun.engine.model.Command;
import cz.metacentrum.perun.engine.processing.CommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@org.springframework.stereotype.Service(value = "commandProcessor")
public class CommandProcessorImpl implements CommandProcessor {

	private final static Logger log = LoggerFactory
			.getLogger(CommandProcessorImpl.class);

	@Override
	public void receiveCommand(String command) throws UnknownCommandException {

		String commandString = command.trim().toUpperCase();

		if (commandString.equals(Command.SEND_STATS)) {
			log.debug("Command [{}] [{}] received into CommandProcessor.", Command.SEND_STATS, command);

		} else if (commandString.equals(Command.REFRESH_PROCESSING_RULES)) {
			log.debug("Command [{}] [{}] received into CommandProcessor.", Command.REFRESH_PROCESSING_RULES, command);

		} else if (commandString.equals(Command.SWITCH_OFF)) {
			log.debug("Command [{}] [{}] received into CommandProcessor.", Command.SWITCH_OFF, command);

		} else if (commandString.equals(Command.FORCE_SERVICE_PROPAGATION)) {
			log.debug("Command [{}] [{}] received into CommandProcessor.", Command.FORCE_SERVICE_PROPAGATION, command);

		} else {
			throw new UnknownCommandException(commandString);
		}

	}

}
