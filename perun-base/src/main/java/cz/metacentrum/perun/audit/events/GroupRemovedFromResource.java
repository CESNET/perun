package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class GroupRemovedFromResource {

    private Group group;
    private Resource resource;

    public GroupRemovedFromResource(Group group, Resource resource) {
        this.group = group;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return group + " removed from " + resource;
    }
}
