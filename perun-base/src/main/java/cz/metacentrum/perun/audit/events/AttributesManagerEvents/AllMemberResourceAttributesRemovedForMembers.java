package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Resource;

public class AllMemberResourceAttributesRemovedForMembers {
    private Resource resource;
    private String name = this.getClass().getName();
    private String message = String.format("All non-virtual member-resource attributes removed for all members and %s.",resource);

    public AllMemberResourceAttributesRemovedForMembers(Resource resource) {
        this.resource = resource;
    }

    public AllMemberResourceAttributesRemovedForMembers() {
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
        return "All non-virtual member-resource attributes removed for all members and " + resource;
    }
}
