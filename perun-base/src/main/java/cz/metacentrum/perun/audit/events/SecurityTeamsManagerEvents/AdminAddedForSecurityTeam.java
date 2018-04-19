package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class AdminAddedForSecurityTeam {

    private User user;
    private SecurityTeam securityTeam;


    public AdminAddedForSecurityTeam(User user, SecurityTeam securityTeam) {
        this.user = user;
        this.securityTeam = securityTeam;
    }

    @Override
    public String toString() {
        return user + " was added as security admin of "+ securityTeam +".";
    }
}
