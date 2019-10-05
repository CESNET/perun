package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.SecurityTeam;

/**
 * This exception is thrown when the security team is not assigned to facility
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamNotAssignedException extends EntityNotAssignedException {

	private final SecurityTeam securityTeam;

	/**
	 * Simple constructor with a message and the securityTeam
	 * @param message message with details about the cause
	 * @param securityTeam securityTeam that is not assigned to the facility
	 */
	public SecurityTeamNotAssignedException(String message, SecurityTeam securityTeam) {
		super(message);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a message, Throwable object and the securityTeam
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param securityTeam securityTeam that is not assigned to the facility
	 */
	public SecurityTeamNotAssignedException(String message, Throwable cause, SecurityTeam securityTeam) {
		super(message, cause);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a Throwable object and the securityTeam
	 * @param cause Throwable that caused throwing of this exception
	 * @param securityTeam securityTeam that is not assigned to the facility
	 */
	public SecurityTeamNotAssignedException(Throwable cause, SecurityTeam securityTeam) {
		super(cause);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with the securityTeam
	 * @param securityTeam securityTeam that is not assigned to the facility
	 */
	public SecurityTeamNotAssignedException(SecurityTeam securityTeam) {
		super(securityTeam.toString());
		this.securityTeam = securityTeam;
	}

	/**
	 * Getter for the securityTeam
	 * @return securityTeam that is not assigned to the facility
	 */
	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}
}
