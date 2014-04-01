package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Exception thrown when VO is not found.
 *
 */
public class VoNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	static final long serialVersionUID = 0;

	public VoNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}
	/**
	 * Constructor.
	 * @param err error message identifying the VO, perhaps by name or id
	 */
	public VoNotExistsRuntimeException(String err) {
		super(err);
	}

	/**
	 * Constructor.
	 * @param err error message identifying the VO, perhaps by name or id
	 * @param cause the throwable to be wrapped in this exception
	 */
	public VoNotExistsRuntimeException(String err, Throwable cause) {
		super(err, cause);
	}
}
