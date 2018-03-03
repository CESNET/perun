package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;

public class ServiceUpdated {

    private Service service;


    public ServiceUpdated(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return service + " updated.";
    }
}
