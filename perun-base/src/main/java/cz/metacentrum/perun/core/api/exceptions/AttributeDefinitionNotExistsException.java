package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.rt.AttributeNotExistsRuntimeException;

/**
 * Attribute not exists in underlaying data source.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributeDefinitionNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	public AttributeDefinitionNotExistsException(AttributeNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public AttributeDefinitionNotExistsException(String message) {
		super(message);
	}

	public AttributeDefinitionNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeDefinitionNotExistsException(Throwable cause) {
		super(cause);
	}

}
