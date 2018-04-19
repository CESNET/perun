package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;

public class FacilityDeleted {

    private  Facility facility;
    private String name = this.getClass().getName();
    private String message;

    public FacilityDeleted(Facility facility) {
        this.facility = facility;
    }

    public FacilityDeleted() {
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
        return String.format("Facility deleted %s.",facility);
    }
}
