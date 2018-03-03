package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class OwnerContactsRemoved {

    private int ownerId;
    private ContactGroup contactGroup;

    public OwnerContactsRemoved(int id, ContactGroup contactGroup) {
        ownerId = id;
        this.contactGroup = contactGroup;
    }

    @Override
    public String toString() {
        return "Owner (" + ownerId + ") successfully removed from contact groups " + contactGroup.toString() + ".";
    }
}
