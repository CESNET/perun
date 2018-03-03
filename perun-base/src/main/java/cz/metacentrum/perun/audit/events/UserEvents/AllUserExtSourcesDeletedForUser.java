package cz.metacentrum.perun.audit.events.UserEvents;

import cz.metacentrum.perun.core.api.User;

public class AllUserExtSourcesDeletedForUser {

    private User user;


    public AllUserExtSourcesDeletedForUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "All user ext sources removed for " + user + ".";
    }
}
