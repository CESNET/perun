package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class AdminGroupAddedForSecurityTeam {

    private Group group;
    private SecurityTeam securityTeam;

    public AdminGroupAddedForSecurityTeam(Group group, SecurityTeam securityTeam) {
        this.group = group;
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return group + " was added as security admins of "+ securityTeam +".";
    }
}
