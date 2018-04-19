package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Group;


public class AllAttributesRemovedForGroup {

    private Group group;

    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForGroup(Group group) {
        this.group = group;
    }

    public AllAttributesRemovedForGroup() {
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

    public String getMessage() {
        return String.format("All attributes removed for %s",group);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s",group);
    }
}
