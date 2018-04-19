package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ContactGroup;

public class GroupContactsRemoved {

    private int groupId;
    private ContactGroup contactGroup;
    public GroupContactsRemoved(int id, ContactGroup contactGroup) {
        groupId = id;
        this.contactGroup = contactGroup;
    }

    @Override
    public String toString() {
        return "Group (" + groupId + ") successfully removed from contact groups " + contactGroup.toString() + ".";
    }
}
