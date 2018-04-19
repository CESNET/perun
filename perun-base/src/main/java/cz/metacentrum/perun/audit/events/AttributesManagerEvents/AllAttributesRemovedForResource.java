package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResource {

    private Resource resource;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForResource() {
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AllAttributesRemovedForResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",resource);
    }
}
