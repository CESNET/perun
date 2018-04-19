package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class GroupsRemovedFromContactGroupOfFacility {
    private List<Integer> groupsId;
    private ContactGroup contactGroup;

    private String name = this.getClass().getName();
    private String message;

    public GroupsRemovedFromContactGroupOfFacility(List<Integer> groupsId, ContactGroup contactGroup) {
        this.groupsId = groupsId;
        this.contactGroup = contactGroup;
    }

    public GroupsRemovedFromContactGroupOfFacility() {
    }

    public List<Integer> getGroupsId() {
        return groupsId;
    }

    public void setGroupsId(List<Integer> groupsId) {
        this.groupsId = groupsId;
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
        return String.format("Groups (%d) successfully removed from contact groups %s.",groupsId.toString(),contactGroup.toString());
    }
}
