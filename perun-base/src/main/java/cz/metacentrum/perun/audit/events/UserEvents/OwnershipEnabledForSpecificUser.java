package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class OwnershipEnabledForSpecificUser {


    private User user;
    private User specificUser;

    public OwnershipEnabledForSpecificUser(User user, User specificUser) {
        this.user = user;
        this.specificUser = specificUser;
    }

    @Override
    public String toString() {
        return user + " ownership was enabled for specificUser " + specificUser + ".";
    }
}
