package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class OwnershipDisabledForSpecificUser {

    private User user;
    private User specificUser;

    public OwnershipDisabledForSpecificUser(User user, User specificUser) {
        this.user = user;
        this.specificUser = specificUser;
    }

    @Override
    public String toString() {
        return user + " ownership was disabled for specificUser " + specificUser + ".";
    }
}
