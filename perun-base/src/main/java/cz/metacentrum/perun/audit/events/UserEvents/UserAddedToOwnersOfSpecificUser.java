package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class UserAddedToOwnersOfSpecificUser {

    private User user;
    private User specificUser;

    public UserAddedToOwnersOfSpecificUser(User user, User specificUser) {
        this.user = user;
        this.specificUser = specificUser;
    }

    @Override
    public String toString() {
        return user + " was added to owners of " + specificUser + ".";
    }
}
