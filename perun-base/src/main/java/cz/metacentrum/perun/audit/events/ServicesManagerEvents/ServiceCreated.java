package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;

public class ServiceCreated {

    private Service service;


    public ServiceCreated(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return service + " created.";
    }
}
