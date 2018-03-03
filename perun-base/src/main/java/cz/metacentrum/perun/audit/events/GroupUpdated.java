package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;

public class GroupUpdated {
    private Group group;

    public GroupUpdated(Group group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return  group + " updated.";
    }
}
