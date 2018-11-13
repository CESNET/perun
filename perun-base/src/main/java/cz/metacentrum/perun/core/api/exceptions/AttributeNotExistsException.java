package cz.metacentrum.perun.core.api.exceptions;


/**
 * Attribute not exists in underlaying data source.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributeNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	public AttributeNotExistsException(String message) {
		super(message);
	}

	public AttributeNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeNotExistsException(Throwable cause) {
		super(cause);
	}

}
