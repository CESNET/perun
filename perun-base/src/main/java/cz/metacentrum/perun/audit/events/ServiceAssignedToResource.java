package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

public class ServiceAssignedToResource {

    private Service service;
    private Resource resource;

    public ServiceAssignedToResource(Service service, Resource resource) {
        this.service = service;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return service + " asigned to " + resource;
    }
}
