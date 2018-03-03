package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceRemovedFromUser {
    private User user;
    private UserExtSource userExtSource;

    public UserExtSourceRemovedFromUser(UserExtSource userExtSource, User user) {
        this.userExtSource = userExtSource;
        this.user = user;
    }

    @Override
    public String toString() {
        return userExtSource + " removed from " + user + ".";
    }
}
