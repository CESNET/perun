package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;

public class FacilityUpdated {
    private Facility facility;

    public FacilityUpdated(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return facility +" updated.";
    }
}
