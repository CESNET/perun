package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when trying tp send mail verification for an application that is no longer in the state "NEW"
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class ApplicationNotNewException extends PerunException {



	private static final long serialVersionUID = 1L;


	private final String state;

	public ApplicationNotNewException(String mess, String state) {
		super(mess);
		this.state = state;
	}

	public String getState() {
		return state;
	}

}
