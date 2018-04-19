package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Resource;

public class AllGroupResourceAttributesRemovedForGroups {

    private Resource resource;
    private String name = this.getClass().getName();
    private String message;

    public AllGroupResourceAttributesRemovedForGroups(Resource resource) {
        this.resource = resource;
    }

    public AllGroupResourceAttributesRemovedForGroups() {
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

    @Override
    public String toString() {
        return String.format("All non-virtual group-resource attributes removed for all groups and %s.",resource);
    }
}
