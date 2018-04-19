package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;

public class AdminGroupRemovedForFacility {
    private Group group;
    private Facility facility;

    private String name = this.getClass().getName();
    private String message;

    public AdminGroupRemovedForFacility(Group group, Facility facility) {
        this.group = group;
        this.facility = facility;
    }

    public AdminGroupRemovedForFacility() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
        return String.format("Group %s was removed from admins of %s.",group,facility);
    }
}
