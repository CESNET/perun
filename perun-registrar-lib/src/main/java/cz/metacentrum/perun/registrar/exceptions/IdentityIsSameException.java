package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when user tries to join one identity with itself.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class IdentityIsSameException extends PerunException {

	private static final long serialVersionUID = 1L;

	private String login = "";
	private String source = "";
	private String sourceType = "";

	public IdentityIsSameException(String message) {
		super(message);
	}

	public IdentityIsSameException(String message, Throwable ex) {
		super(message, ex);
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

}
