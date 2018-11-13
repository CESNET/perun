package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Checked version of DestinationAlreadyAssignedException.
 *
 * @author Slavek Licehammer
 */
public class DestinationAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	private Destination destination;

	public DestinationAlreadyAssignedException(String message) {
		super(message);
	}

	public DestinationAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DestinationAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	public DestinationAlreadyAssignedException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	public Destination getDestination() {
		return this.destination;
	}



}
