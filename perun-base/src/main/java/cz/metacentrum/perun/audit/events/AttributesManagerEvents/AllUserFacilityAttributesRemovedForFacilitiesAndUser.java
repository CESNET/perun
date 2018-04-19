package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.User;

public class AllUserFacilityAttributesRemovedForFacilitiesAndUser {

    private User user;
    private String name = this.getClass().getName();
    private String message = String.format("All non-virtual user-facility attributes removed for all facilities and %s",user);

    public AllUserFacilityAttributesRemovedForFacilitiesAndUser(User user) {
        this.user = user;
    }

    public AllUserFacilityAttributesRemovedForFacilitiesAndUser() {
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
        return "All non-virtual user-facility attributes removed for all facilities and " + user;
    }
}
