package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

public class AdminUserAddedForResource {

    private User user;
    private Resource resource;

    public AdminUserAddedForResource(User user, Resource resource) {
        this.user =user;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return user + " was added as admin of " +resource + ".";
    }
}
