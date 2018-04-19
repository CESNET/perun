package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

public class AdminUserRemovedForResource {
    private User user;
    private Resource resource;

    public AdminUserRemovedForResource(User user, Resource resource) {
        this.user = user;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return  user + " was removed from admins of " + resource + ".";
    }
}
