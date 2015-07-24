/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.api.exceptions;

/**
 * Group and resource are not in the same VO
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 5.10.2011
 */
public class GroupResourceMismatchException extends PerunException {

	public GroupResourceMismatchException(Throwable cause) {
		super(cause);
	}

	public GroupResourceMismatchException(String message, Throwable cause) {
		super(message,cause);
	}

	public GroupResourceMismatchException(String message) {
		super(message);
	}

	public GroupResourceMismatchException() {
	}


}
