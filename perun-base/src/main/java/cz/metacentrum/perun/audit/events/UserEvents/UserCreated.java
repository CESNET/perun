package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class UserCreated {
    private User user;
    public UserCreated(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return user + " created.";
    }
}
