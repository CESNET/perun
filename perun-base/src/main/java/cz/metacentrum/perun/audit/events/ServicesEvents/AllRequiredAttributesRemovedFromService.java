package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;

public class AllRequiredAttributesRemovedFromService {

    private Service service;

    public AllRequiredAttributesRemovedFromService(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "All required attributes removed from " + service + ".";
    }
}
