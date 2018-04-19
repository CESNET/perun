package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToBlackListOfSecurityTeam {

    private User user;
    private SecurityTeam securityTeam;
    private String description;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserAddedToBlackListOfSecurityTeam() {
    }

    public UserAddedToBlackListOfSecurityTeam(User user, SecurityTeam securityTeam, String description) {
        this.user = user;
        this.securityTeam = securityTeam;
        this.description = description;

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SecurityTeam getSecurityTeam() {
        return securityTeam;
    }

    public void setSecurityTeam(SecurityTeam securityTeam) {
        this.securityTeam = securityTeam;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return user + " add to blacklist of " + securityTeam + " with description '"+ description+"'.";
    }
}
