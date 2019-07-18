package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when new application can't be created (stored) in Perun.
 * This is usually caused by broken login/pass reservation i external system.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ApplicationNotCreatedException extends PerunException {

	private static final long serialVersionUID = 1L;

	private final String login;
	private final String namespace;

	public ApplicationNotCreatedException(String message, String login, String namespace) {
		super(message);
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
