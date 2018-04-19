package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;

public class FacilityCreated {

    private Facility facility;

    public FacilityCreated(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "Facility created " + facility + ".";
    }
}
