package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.core.api.Group;

public class GroupDeleted {

    private Group group;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GroupDeleted(Group group) {
        this.group = group;
    }

    public GroupDeleted() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return group + "deleted.";
    }
}
