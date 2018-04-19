package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;

public class AdminAddedForFacility {

    private User user;
    private Facility facility;

    private String name = this.getClass().getName();
    private String message;

    public AdminAddedForFacility(User user, Facility facility) {
        this.user = user;
        this.facility = facility;
    }

    public AdminAddedForFacility() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
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
        return String.format("%s was added as admin of %s.",user,facility);
    }
}
