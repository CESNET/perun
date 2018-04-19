package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class ResourceDeleted {

    private Resource resource;
    private Facility facility;
    private List<Service> services;
    public ResourceDeleted(Resource resource, Facility facility, List<Service> services) {
        this.resource = resource;
        this.facility = facility;
        this.services = services;
    }

    @Override
    public String toString() {
        return resource + " deleted.#" + facility + ". Afected services:" + services +".";
    }
}
