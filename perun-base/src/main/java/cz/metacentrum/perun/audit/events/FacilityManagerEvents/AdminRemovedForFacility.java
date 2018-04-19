package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;

public class AdminRemovedForFacility {
    public AdminRemovedForFacility(User user, Facility facility) {
    }

    public AdminRemovedForFacility(Group group, Facility facility) {
    }


    //getPerunBl().getAuditer().log(sess, "{} was removed from admins of {}.", user, facility);
    //getPerunBl().getAuditer().log(sess, "Group {} was removed from admins of {}.", group, facility);
}
