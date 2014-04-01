package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of AlreadyReservedLoginException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AlreadyReservedLoginException
 * @author Michal Šťava
 */
public class AlreadyReservedLoginException extends PerunException {
	static final long serialVersionUID = 0;

	private String login;
	private String namespace;

	public AlreadyReservedLoginException(String message) {
		super(message);
	}

	public AlreadyReservedLoginException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyReservedLoginException(Throwable cause) {
		super(cause);
	}

	public AlreadyReservedLoginException(String namespace, String login) {
		super("Login: " + login + " with namespace: " + namespace + " is already reserved.");
		this.login = login;
		this.namespace = namespace;
	}

	public String getLogin() {
		return login;
	}

	public String getNamespace() {
		return namespace;
	}
}
