package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.SecurityTeam;

/**
 * Security team is not assigned to facility
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamNotAssignedException extends EntityNotAssignedException {

	private final SecurityTeam securityTeam;

	public SecurityTeamNotAssignedException(String message, SecurityTeam securityTeam) {
		super(message);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamNotAssignedException(String message, Throwable cause, SecurityTeam securityTeam) {
		super(message, cause);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamNotAssignedException(Throwable cause, SecurityTeam securityTeam) {
		super(cause);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamNotAssignedException(SecurityTeam securityTeam) {
		super(securityTeam.toString());
		this.securityTeam = securityTeam;
	}

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}
}
