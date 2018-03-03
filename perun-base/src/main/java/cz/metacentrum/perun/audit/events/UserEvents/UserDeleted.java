package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class UserDeleted {
    private User user;
    public UserDeleted(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return user + " deleted.";
    }
}
