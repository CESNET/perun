package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 *
 * @see cz.metacentrum.perun.core.api.exceptions.EntityExistsException
 * @author Slavek Licehammer
 *
 */
public class EntityExistsRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public EntityExistsRuntimeException() {
		super();
	}

	public EntityExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public EntityExistsRuntimeException(String err) {
		super(err);
	}

	public EntityExistsRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
