package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamAssignedToFacility implements AuditEvent {
	private SecurityTeam securityTeam;
	private Facility facility;
	private String name = this.getClass().getName();
	private String message;

	public SecurityTeamAssignedToFacility(SecurityTeam securityTeam, Facility facility) {
		this.securityTeam = securityTeam;
		this.facility = facility;
	}

	public SecurityTeamAssignedToFacility() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}

	public void setSecurityTeam(SecurityTeam securityTeam) {
		this.securityTeam = securityTeam;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return securityTeam + " was assigned to " + facility + ".";
	}
}
