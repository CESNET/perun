package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamDeleted {
    private SecurityTeam securityTeam;

    public SecurityTeamDeleted(SecurityTeam securityTeam) {
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return securityTeam + " was deleted.";
    }
}
