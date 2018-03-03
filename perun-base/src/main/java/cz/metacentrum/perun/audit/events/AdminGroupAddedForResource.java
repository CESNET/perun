package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AdminGroupAddedForResource {

    private Group group;
    private Resource resource;

    public AdminGroupAddedForResource(Group group, Resource resource) {
        this.group = group;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "Group " + group + " was added as admin of " + resource + ".";
    }
}
