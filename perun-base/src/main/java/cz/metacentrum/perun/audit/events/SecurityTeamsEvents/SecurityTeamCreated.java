package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamCreated {

    private SecurityTeam securityTeam;

    public SecurityTeamCreated(SecurityTeam securityTeam) {
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return securityTeam + " was created.";
    }
}
