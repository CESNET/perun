package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when user tries to join two identities, by neither is known to Perun
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class IdentityUnknownException extends PerunException {

	private static final long serialVersionUID = 1L;

	private String login = "";
	private String source = "";
	private String sourceType = "";
	private String login2 = "";
	private String source2 = "";
	private String sourceType2 = "";

	public IdentityUnknownException(String message) {
		super(message);
	}

	public IdentityUnknownException(String message, Throwable ex) {
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

	public String getLogin2() {
		return login2;
	}

	public void setLogin2(String login2) {
		this.login2 = login2;
	}

	public String getSource2() {
		return source2;
	}

	public void setSource2(String source2) {
		this.source2 = source2;
	}

	public String getSourceType2() {
		return sourceType2;
	}

	public void setSourceType2(String sourceType2) {
		this.sourceType2 = sourceType2;
	}

}
