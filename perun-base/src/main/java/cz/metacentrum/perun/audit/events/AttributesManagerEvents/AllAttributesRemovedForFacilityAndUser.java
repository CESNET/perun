package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;

public class AllAttributesRemovedForFacilityAndUser {


    private String name = this.getClass().getName();
    private Facility facility;
    private User user;
    private String message;

    public AllAttributesRemovedForFacilityAndUser(Facility facility, User user) {
        this.facility = facility;
        this.user = user;
    }

    public AllAttributesRemovedForFacilityAndUser() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return String.format("All attributes removed for %s and %s .", facility , user);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "All attributes removed for " + facility + " and " + user + ".";
    }
}
