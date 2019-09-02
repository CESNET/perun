package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of AlreadyReservedLoginException.
 *
 * Exception is thrown when the login is already reserved
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AlreadyReservedLoginException
 * @author Michal Šťava
 */
public class AlreadyReservedLoginException extends PerunException {
	static final long serialVersionUID = 0;

	private String login;
	private String namespace;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AlreadyReservedLoginException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyReservedLoginException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyReservedLoginException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the namespace and the login
	 * @param namespace namespace for login
	 * @param login login
	 */
	public AlreadyReservedLoginException(String namespace, String login) {
		super("Login: " + login + " with namespace: " + namespace + " is already reserved.");
		this.login = login;
		this.namespace = namespace;
	}

	/**
	 *
	 * @return login which is reserved
	 */
	public String getLogin() {
		return login;
	}

	/**
	 *
	 * @return namespace for login
	 */
	public String getNamespace() {
		return namespace;
	}
}
