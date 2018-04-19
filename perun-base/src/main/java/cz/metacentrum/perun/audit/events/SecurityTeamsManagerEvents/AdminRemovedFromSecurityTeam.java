package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class AdminRemovedFromSecurityTeam {

    private User user;
    private SecurityTeam securityTeam;

    public AdminRemovedFromSecurityTeam(User user, SecurityTeam securityTeam) {
        this.user = user;
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return user + " was removed from security admins of " + securityTeam + ".";
    }
}
