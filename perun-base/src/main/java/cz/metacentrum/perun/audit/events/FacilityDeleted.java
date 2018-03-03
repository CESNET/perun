package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;

public class FacilityDeleted {

    private  Facility facility;

    public FacilityDeleted(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "Facility deleted " + facility + ".";
    }
}
