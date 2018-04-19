package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Owner;

import java.util.List;

public class OwnerContactsRemovedForFacility {

    private Owner owner;
    private ContactGroup contactGroup;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OwnerContactsRemovedForFacility(Owner owner, ContactGroup contactGroup) {
        this.owner = owner;
        this.contactGroup = contactGroup;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public ContactGroup getContactGroup() {
        return contactGroup;
    }

    public void setContactGroup(ContactGroup contactGroup) {
        this.contactGroup = contactGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Owner (" + owner.getId() + ") successfully removed from contact groups " + contactGroup.toString() + ".";
    }
}
