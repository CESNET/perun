package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Custom exception thrown by Registrar.
 * It's processed in GUI and shows raw message text.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class RegistrarException extends PerunException {

	private static final long serialVersionUID = 1L;

	public RegistrarException(String message) {
        super(message);
    }

    public RegistrarException(String message, Throwable ex) {
        super(message, ex);
    }

}