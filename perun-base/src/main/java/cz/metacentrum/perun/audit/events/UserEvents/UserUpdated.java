package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class UserUpdated {
    private User user;
    public UserUpdated(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return user + " updated.";
    }
}
