package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamRemovedFromFacility {
    private SecurityTeam securityTeam;
    private Facility facility;

    public SecurityTeamRemovedFromFacility(SecurityTeam securityTeam, Facility facility) {
        this.securityTeam = securityTeam;
        this.facility = facility;
    }

    @Override
    public String toString() {
        return securityTeam + " was removed from " + facility + ".";
    }
}
