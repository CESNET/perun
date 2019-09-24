/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the group and resource are not in the same VO
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 5.10.2011
 */
public class GroupResourceMismatchException extends PerunException {

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupResourceMismatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupResourceMismatchException(String message, Throwable cause) {
		super(message,cause);
	}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupResourceMismatchException(String message) {
		super(message);
	}

	/**
	 * Constructor without arguments
	 */
	public GroupResourceMismatchException() {
	}


}
