package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesForResource {

    private Resource resource;

    public AllAttributesForResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "All attributes removed for " + resource + ".";
    }
}
