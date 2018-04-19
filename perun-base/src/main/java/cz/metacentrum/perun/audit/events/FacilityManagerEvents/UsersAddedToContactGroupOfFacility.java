package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class UsersAddedToContactGroupOfFacility {


    private List<Integer> usersId;
    private ContactGroup contactGroup;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public UsersAddedToContactGroupOfFacility(List<Integer> usersId, ContactGroup contactGroup) {
        this.usersId = usersId;
        this.contactGroup = contactGroup;
    }

    public UsersAddedToContactGroupOfFacility() {
    }

    public List<Integer> getUsersId() {
        return usersId;
    }

    public void setUsersId(List<Integer> usersId) {
        this.usersId = usersId;
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
        return  "Users (" + usersId + ") successfully added to contact group " + contactGroup + ".";
    }
}
