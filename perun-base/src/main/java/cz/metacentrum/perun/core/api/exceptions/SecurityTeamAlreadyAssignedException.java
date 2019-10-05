package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.SecurityTeam;

/**
 * This exception is thrown when the security team is already assigned to facility
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamAlreadyAssignedException extends EntityAlreadyAssignedException {

	private final SecurityTeam securityTeam;

	/**
	 * Simple constructor with a message and the securityTeam
	 * @param message message with details about the cause
	 * @param securityTeam securityTeam that has already been assigned to the facility
	 */
	public SecurityTeamAlreadyAssignedException(String message, SecurityTeam securityTeam) {
		super(message);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a message, Throwable object and the securityTeam
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 * @param securityTeam securityTeam that has already been assigned to the facility
	 */
	public SecurityTeamAlreadyAssignedException(String message, Throwable cause, SecurityTeam securityTeam) {
		super(message, cause);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with a Throwable object and the securityTeam
	 * @param cause Throwable that caused throwing of this exception
	 * @param securityTeam securityTeam that has already been assigned to the facility
	 */
	public SecurityTeamAlreadyAssignedException(Throwable cause, SecurityTeam securityTeam) {
		super(cause);
		this.securityTeam = securityTeam;
	}

	/**
	 * Constructor with the securityTeam
	 * @param securityTeam securityTeam that has already been assigned to the facility
	 */
	public SecurityTeamAlreadyAssignedException(SecurityTeam securityTeam) {
		super(securityTeam.toString());
		this.securityTeam = securityTeam;
	}

	/**
	 * Getter for the securityTeam
	 * @return securityTeam that has already been assigned to the facility
	 */
	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}
}
