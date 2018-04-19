package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.UserExtSource;

public class AllAttributesRemovedForUserExtSource {

    private UserExtSource userExtSource;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForUserExtSource(UserExtSource userExtSource) {
        this.userExtSource = userExtSource;
    }

    public AllAttributesRemovedForUserExtSource() {
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

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",userExtSource);
    }
}
