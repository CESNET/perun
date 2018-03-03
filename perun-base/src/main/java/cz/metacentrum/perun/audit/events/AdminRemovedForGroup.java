package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;

public class AdminRemovedForGroup {

    private User user;
    private Group group;
    public AdminRemovedForGroup(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    @Override
    public String toString() {
        return user + " was removed from admins of "+ group +".";
    }
}
