package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;

public class FacilityAllAttributesRemoved {
    private Facility facility;

    public FacilityAllAttributesRemoved(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "All attributes removed for "+ facility +".";
    }
}
