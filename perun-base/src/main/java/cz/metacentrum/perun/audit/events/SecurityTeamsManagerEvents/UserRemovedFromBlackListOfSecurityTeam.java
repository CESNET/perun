package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlackListOfSecurityTeam {

    private User user;
    private SecurityTeam securityTeam;


    public UserRemovedFromBlackListOfSecurityTeam(User user, SecurityTeam securityTeam) {

        this.user = user;
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return user + " remove from blacklist of " + securityTeam + ".";
    }
}
