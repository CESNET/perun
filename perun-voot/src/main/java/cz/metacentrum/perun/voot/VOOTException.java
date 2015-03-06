package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Checked version of VOOTException.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTException extends PerunException {
	static final long serialVersionUID = 0;

	String error;
	String error_description;

	public VOOTException(String message){
		super(message);
		error = message;
	}

	public VOOTException(String error, String errorDescription){
		super(error);
		this.error = error;
		this.error_description = errorDescription;
	}

	public VOOTException(Throwable cause){
		super(cause);
	}

	public VOOTException(String message, Throwable cause){
		super(message, cause);
	}

	public String getError() {
		return error;
	}

	public String getErrorDescription() {
		return error_description;
	}

	@Override
	public String toString(){
		return new StringBuilder(getClass().getName()).append(":[")
				.append("error='").append(getError()).append("', ")
				.append("error_description='").append(getErrorDescription()).append("']").toString();
	}
}
