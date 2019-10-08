package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.PerunBean;

/**
 * Checked version of PerunBeanNotSupportedException.
 *
 * This exception is thrown when somewhere in code is object PerunBean but
 * this one is not supported there for some reason.
 *
 * @author Michal Stava
 */
public class PerunBeanNotSupportedException extends PerunException {
	static final long serialVersionUID = 0;

	private PerunBean complementaryObject;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PerunBeanNotSupportedException(String message) {
		super(message);
	}

	/**
	 *
	 * @param message message with details about the cause
	 * @param complementaryObject PerunBean object which is not supported
	 */
	public PerunBeanNotSupportedException(String message, PerunBean complementaryObject) {
		super(message);
		this.complementaryObject = complementaryObject;
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PerunBeanNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with the cause, the PerunBean object and the message
	 * @param message message with details about the cause
	 * @param complementaryObject PerunBean object which is not supported
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PerunBeanNotSupportedException(String message, PerunBean complementaryObject, Throwable cause) {
		super(message, cause);
		this.complementaryObject = complementaryObject;
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PerunBeanNotSupportedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the cause and the PerunBean object
	 * @param cause Throwable that caused throwing of this exception
	 * @param complementaryObject PerunBean object which is not supported
	 */
	public PerunBeanNotSupportedException(Throwable cause, PerunBean complementaryObject) {
		super(cause);
		this.complementaryObject = complementaryObject;
	}

	/**
	 * Getter for the PerunBean object
	 * @return PerunBean object which is not supported
	 */
	public PerunBean getComplementaryObject() {
		return this.complementaryObject;
	}
}
