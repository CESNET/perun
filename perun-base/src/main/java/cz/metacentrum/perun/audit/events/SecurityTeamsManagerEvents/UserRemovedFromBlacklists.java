package cz.metacentrum.perun.audit.events.SecurityTeamsEvents;

import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlacklists {

    private User user;

    public UserRemovedFromBlacklists(User user) {
        this.user =user;
    }

    @Override
    public String toString() {
        return user + " remove from all blacklists.";
    }
}
