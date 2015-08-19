package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class WrongReferenceAttributeValueRuntimeException extends EntityNotExistsRuntimeException {

	public WrongReferenceAttributeValueRuntimeException() {
		super();
	}

	public WrongReferenceAttributeValueRuntimeException(Throwable cause) {
		super(cause);
	}


}
