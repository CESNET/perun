package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;

public class FacilitySetForResource {
    private Facility facility;
    private Resource resource;


    public FacilitySetForResource(Facility facility, Resource resource) {
        this.facility = facility;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return facility + " set for " + resource;
    }
}
