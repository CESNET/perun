package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception is thrown when there is an attempt to create a group with
 * an invalid name.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class InvalidGroupNameException extends PerunException {
	public InvalidGroupNameException() {
		super();
	}

	public InvalidGroupNameException(String message) {
		super(message);
	}

	public InvalidGroupNameException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidGroupNameException(Throwable cause) {
		super(cause);
	}
}
