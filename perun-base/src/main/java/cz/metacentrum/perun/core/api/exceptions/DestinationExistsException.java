package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Destination;

/**
 * Checked version of DestinationExistsException
 *
 * @author Michal Prochazka
 */
public class DestinationExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;
	private Destination destination;

	public DestinationExistsException(String message) {
		super(message);
	}

	public DestinationExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public DestinationExistsException(Throwable cause) {
		super(cause);
	}

	public DestinationExistsException(Destination destination) {
		super(destination.toString());
		this.destination = destination;
	}

	/**
	 * Gets the destination for this instance.
	 *
	 * @return The destination.
	 */
	public Destination getDestination()
	{
		return this.destination;
	}
}
