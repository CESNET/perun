package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 *
 * @see cz.metacentrum.perun.core.api.exceptions.EntityNotAssignedException
 * @author Slavek Licehammer
 *
 */
public class EntityNotAssignedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public EntityNotAssignedRuntimeException() {
		super();
	}

	public EntityNotAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public EntityNotAssignedRuntimeException(String err) {
		super(err);
	}

	public EntityNotAssignedRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
