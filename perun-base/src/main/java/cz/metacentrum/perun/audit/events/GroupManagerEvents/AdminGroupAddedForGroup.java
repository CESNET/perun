package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.core.api.Group;

public class AdminGroupAddedForGroup {

    private Group group, authorizedGroup;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdminGroupAddedForGroup(Group authorizedGroup, Group group) {
        this.authorizedGroup = authorizedGroup;
        this.group = group;
    }

    public AdminGroupAddedForGroup() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getAuthorizedGroup() {
        return authorizedGroup;
    }

    public void setAuthorizedGroup(Group authorizedGroup) {
        this.authorizedGroup = authorizedGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Group " + authorizedGroup + " was added as admin of " + group + ".";
    }
}
