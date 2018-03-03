package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;

public class ServiceDeleted {

    private Service service;


    public ServiceDeleted(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return service + " deleted.";
    }
}
