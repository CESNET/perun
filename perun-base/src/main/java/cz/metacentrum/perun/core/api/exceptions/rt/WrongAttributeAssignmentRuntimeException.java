package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class WrongAttributeAssignmentRuntimeException extends EntityNotExistsRuntimeException {

	public WrongAttributeAssignmentRuntimeException() {
		super();
	}

	public WrongAttributeAssignmentRuntimeException(Throwable cause) {
		super(cause);
	}

	public WrongAttributeAssignmentRuntimeException(String message) {
		super(message);
	}


}
