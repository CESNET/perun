package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Facility;

public class AllUserFacilityAttributesRemoved {

    private Facility facility;
    private String name = this.getClass().getName();
    private String message;

    public AllUserFacilityAttributesRemoved() {
    }

    public AllUserFacilityAttributesRemoved(Facility facility) {
        this.facility = facility;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
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
        return String.format("All user-facility attributes removed for %s for any user.",facility);
    }
}
