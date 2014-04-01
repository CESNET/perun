package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 *
 * @see cz.metacentrum.perun.core.api.exceptions.EntityNotExistsException
 * @author Slavek Licehammer
 *
 */
public class EntityNotExistsRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public EntityNotExistsRuntimeException() {
		super();
	}

	public EntityNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public EntityNotExistsRuntimeException(String err) {
		super(err);
	}

	public EntityNotExistsRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
