package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToBlackListOfSecurityTeam {

    private User user;
    private SecurityTeam securityTeam;
    private String description;

    public UserAddedToBlackListOfSecurityTeam(User user, SecurityTeam securityTeam, String description) {
        this.user = user;
        this.securityTeam = securityTeam;
        this.description = description;

    }

    @Override
    public String toString() {
        return user + " add to blacklist of " + securityTeam + " with description '"+ description+"'.";
    }
}
