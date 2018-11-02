package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Checked version of DestinationAlreadyRemovedException
 *
 * @author Slavek Licehammer
 */
public class DestinationAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	private Destination destination;

	public DestinationAlreadyRemovedException(String message) {
		super(message);
	}

	public DestinationAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DestinationAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

	public DestinationAlreadyRemovedException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	public Destination getDestination() {
		return this.destination;
	}

}
