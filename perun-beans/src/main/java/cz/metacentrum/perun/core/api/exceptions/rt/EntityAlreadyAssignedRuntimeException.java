package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 *
 * @see cz.metacentrum.perun.core.api.exceptions.EntityAlreadyAssignedException
 * @author Slavek Licehammer
 *
 */
public class EntityAlreadyAssignedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public EntityAlreadyAssignedRuntimeException() {
		super();
	}

	public EntityAlreadyAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public EntityAlreadyAssignedRuntimeException(String err) {
		super(err);
	}

	public EntityAlreadyAssignedRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
