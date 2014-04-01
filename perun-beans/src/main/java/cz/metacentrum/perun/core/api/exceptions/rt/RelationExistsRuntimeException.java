package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 *
 * @see cz.metacentrum.perun.core.api.exceptions.RelationExistsException
 * @author Slavek Licehammer
 *
 */
public class RelationExistsRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	public RelationExistsRuntimeException() {
		super();
	}

	public RelationExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public RelationExistsRuntimeException(String err) {
		super(err);
	}

	public RelationExistsRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
