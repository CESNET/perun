package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Thrown when application form for VO
 * or Group does not exists.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FormNotExistsException extends PerunException {

	private static final long serialVersionUID = 1L;

	public FormNotExistsException(String message) {
		super(message);
	}

}
