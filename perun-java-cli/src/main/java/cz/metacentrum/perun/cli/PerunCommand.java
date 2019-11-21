package cz.metacentrum.perun.cli;

import org.apache.commons.cli.Options;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

/**
 * Empty command.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunCommand {

	public String getName() {
		String s = getClass().getSimpleName();
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public abstract String getCommandDescription();

	/**
	 * Adds command-line options.
	 *
	 * @param options options specific to this command
	 */
	public void addOptions(Options options) {
	}

	public abstract void executeCommand(PerunCLI.CommandContext ctx) throws RestClientException;

}
