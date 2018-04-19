package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Facility;

public class DenialsOnFacilityFreed {
    private String freeAllDen;
    private Facility facility;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public DenialsOnFacilityFreed(String freeAllDen, Facility facility) {
        this.freeAllDen = freeAllDen;
        this.facility = facility;
    }

    public DenialsOnFacilityFreed() {
    }

    public String getFreeAllDen() {
        return freeAllDen;
    }

    public void setFreeAllDen(String freeAllDen) {
        this.freeAllDen = freeAllDen;
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

    @Override
    public String toString() {
        return freeAllDen + " on " + facility;
    }

}
