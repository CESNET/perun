package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamUpdated {

    private SecurityTeam securityTeam;

    public SecurityTeamUpdated(SecurityTeam securityTeam) {
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return securityTeam + " was updated.";
    }
}
