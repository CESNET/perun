package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.User;

public class AllAttributesRemovedForUser {

    private User user;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForUser(User user) {
        this.user = user;
    }

    public AllAttributesRemovedForUser() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",user);
    }
}
