package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Resource;

public class ResourceCreated {

    private Resource resource;


    public ResourceCreated(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return resource + " created.";
    }
}
