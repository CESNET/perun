package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.SecurityTeam;

/**
 * Security team is already assigned to facility
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamAlreadyAssignedException extends EntityAlreadyAssignedException {

	private final SecurityTeam securityTeam;

	public SecurityTeamAlreadyAssignedException(String message, SecurityTeam securityTeam) {
		super(message);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamAlreadyAssignedException(String message, Throwable cause, SecurityTeam securityTeam) {
		super(message, cause);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamAlreadyAssignedException(Throwable cause, SecurityTeam securityTeam) {
		super(cause);
		this.securityTeam = securityTeam;
	}

	public SecurityTeamAlreadyAssignedException(SecurityTeam securityTeam) {
		super(securityTeam.toString());
		this.securityTeam = securityTeam;
	}

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}
}
