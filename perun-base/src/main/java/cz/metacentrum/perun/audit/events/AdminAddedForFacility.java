package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;

public class AdminAddedForFacility {
    public AdminAddedForFacility(Group group, Facility facility) {
    }

    public AdminAddedForFacility(User user, Facility facility) {
    }

    //getPerunBl().getAuditer().log(sess, "Group {} was added as admin of {}.", group, facility);
    //getPerunBl().getAuditer().log(sess, "{} was added as admin of {}.", user, facility);
}
