package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Resource;

public class ResourceUpdated {

    private Resource resource;

    public ResourceUpdated(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return resource + " updated.";
    }
}
