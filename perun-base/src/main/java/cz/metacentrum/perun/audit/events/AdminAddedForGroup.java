package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;

public class AdminAddedForGroup {

    private User user;
    private Group group;

    public AdminAddedForGroup(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    @Override
    public String toString() {
        return user + " was added as admin of "+ group +".";
    }
}
