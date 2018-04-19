package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForGroupAndResource {

    private Group group;
    private Resource resource;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForGroupAndResource(Group group, Resource resource) {
        this.group = group;
        this.resource = resource;
    }

    public AllAttributesRemovedForGroupAndResource() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
        return String.format("All attributes removed for %s and %s.",group, resource);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s and %s.",group, resource);
    }
}
