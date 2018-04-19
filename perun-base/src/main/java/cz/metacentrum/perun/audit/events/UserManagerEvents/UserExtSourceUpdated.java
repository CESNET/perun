package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceUpdated {

    private UserExtSource userExtSource;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserExtSourceUpdated() {
    }

    public UserExtSource getUserExtSource() {
        return userExtSource;
    }

    public void setUserExtSource(UserExtSource userExtSource) {
        this.userExtSource = userExtSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserExtSourceUpdated(UserExtSource userExtSource) {
        this.userExtSource = userExtSource;
    }

    @Override
    public String toString() {
        return userExtSource + " updated.";
    }
}
