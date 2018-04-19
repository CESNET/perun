package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class OwnersRemovedFromContactGroupOfFacility {
    private List<Integer> ownersId;
    private ContactGroup contactGroup;
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    private String name = this.getClass().getName();

    public OwnersRemovedFromContactGroupOfFacility(List<Integer> ownersId, ContactGroup contactGroup) {
        this.ownersId = ownersId;
        this.contactGroup = contactGroup;
    }

    public OwnersRemovedFromContactGroupOfFacility() {
    }

    public List<Integer> getOwnersId() {
        return ownersId;
    }

    public void setOwnersId(List<Integer> ownersId) {
        this.ownersId = ownersId;
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
        return "Owners (" + ownersId.toString() + ") successfully removed from contact group " + contactGroup.toString() + ".";
    }
}
