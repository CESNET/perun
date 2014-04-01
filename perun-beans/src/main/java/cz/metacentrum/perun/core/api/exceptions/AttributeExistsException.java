package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.AttributeExistsRuntimeException;


/**
 * Attribute already exists in underlaying data source. Thrown where creating atribute thats already exists.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributeExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	public AttributeExistsException(AttributeExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public AttributeExistsException(String message) {
		super(message);
	}

	public AttributeExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeExistsException(Throwable cause) {
		super(cause);
	}
}
