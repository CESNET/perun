package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceUpdated {

    private UserExtSource userExtSource;


    public UserExtSourceUpdated(UserExtSource userExtSource) {
        this.userExtSource = userExtSource;
    }

    @Override
    public String toString() {
        return userExtSource + " updated.";
    }
}
