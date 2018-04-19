package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamRemovedFromFacility {
    private SecurityTeam securityTeam;
    private Facility facility;
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    private String name = this.getClass().getName();

    public SecurityTeamRemovedFromFacility(SecurityTeam securityTeam, Facility facility) {
        this.securityTeam = securityTeam;
        this.facility = facility;
    }

    public SecurityTeamRemovedFromFacility() {
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
        return securityTeam + " was removed from " + facility + ".";
    }
}
