package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceAddedToUser {

    private UserExtSource userExtSource;
    private User user;

    public UserExtSourceAddedToUser(UserExtSource userExtSource, User user) {
        this.user = user;
        this.userExtSource = userExtSource;
    }

    @Override
    public String toString() {
        return userExtSource + " added to " + user + ".";
    }
}
