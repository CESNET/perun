package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Group;

public class GroupContactsRemovedForFacility {

    private Group group;
    private ContactGroup contactGroup;

    private String name = this.getClass().getName();
    private String message;

    public GroupContactsRemovedForFacility(Group group, ContactGroup contactGroup) {
        this.group = group;
        this.contactGroup = contactGroup;
    }

    public GroupContactsRemovedForFacility() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Group (%d) successfully removed from contact groups %s.",group.getId(),contactGroup.toString());
    }
}
