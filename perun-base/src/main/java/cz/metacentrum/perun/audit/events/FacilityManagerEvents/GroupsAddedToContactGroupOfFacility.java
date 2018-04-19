package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class GroupsAddedToContactGroupOfFacility {

    private List<Integer> groupsId;
    private ContactGroup contactGroup;

    private String name = this.getClass().getName();
    private String message;


    public GroupsAddedToContactGroupOfFacility(List<Integer> groupsId, ContactGroup contactGroup) {
        this.groupsId = groupsId;
        this.contactGroup = contactGroup;
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
        return String.format("Groups (%s) successfully added from contact groups %s.",groupsId.toString(),contactGroup.toString());
    }
}
