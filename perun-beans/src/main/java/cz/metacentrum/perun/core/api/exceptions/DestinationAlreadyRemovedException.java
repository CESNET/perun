package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.rt.DestinationAlreadyRemovedRuntimeException;

/**
 * Checked version of DestinationAlreadyRemovedException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.DestinationAlreadyRemovedException
 * @author Slavek Licehammer
 */
public class DestinationAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	private Destination destination;

	public DestinationAlreadyRemovedException(DestinationAlreadyRemovedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
