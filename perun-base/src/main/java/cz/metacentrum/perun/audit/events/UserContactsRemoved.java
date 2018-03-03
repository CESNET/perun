package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ContactGroup;

public class UserContactsRemoved {

    private int userId;
    private ContactGroup contactGroup;

    public UserContactsRemoved(int id, ContactGroup contactGroup) {
        userId = id;
        this.contactGroup = contactGroup;
    }

    @Override
    public String toString() {
        return "User (" + userId + ") successfully removed from contact groups " + contactGroup.toString() + ".";
    }
}
