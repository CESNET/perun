package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class AdminGroupRemovedFromSecurityTeam {

    private Group group;
    private SecurityTeam securityTeam;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdminGroupRemovedFromSecurityTeam() {
    }

    public AdminGroupRemovedFromSecurityTeam(Group group, SecurityTeam securityTeam) {
        this.group = group;
        this.securityTeam = securityTeam;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public SecurityTeam getSecurityTeam() {
        return securityTeam;
    }

    public void setSecurityTeam(SecurityTeam securityTeam) {
        this.securityTeam = securityTeam;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return group + " was removed from security admins of "+ securityTeam + ".";
    }
}
