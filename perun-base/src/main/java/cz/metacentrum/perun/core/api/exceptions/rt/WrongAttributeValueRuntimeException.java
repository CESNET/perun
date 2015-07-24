package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class WrongAttributeValueRuntimeException extends EntityNotExistsRuntimeException {

	public WrongAttributeValueRuntimeException() {
		super();
	}

	public WrongAttributeValueRuntimeException(Throwable cause) {
		super(cause);
	}


}
