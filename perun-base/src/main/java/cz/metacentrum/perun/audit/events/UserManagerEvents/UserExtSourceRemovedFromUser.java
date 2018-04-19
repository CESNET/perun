package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceRemovedFromUser {
    private User user;
    private UserExtSource userExtSource;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserExtSourceRemovedFromUser() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public UserExtSourceRemovedFromUser(UserExtSource userExtSource, User user) {
        this.userExtSource = userExtSource;
        this.user = user;
    }

    @Override
    public String toString() {
        return userExtSource + " removed from " + user + ".";
    }
}
