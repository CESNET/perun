package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceAdded {

    public ExtSourceAdded(ExtSource source, Vo vo) {

    }


    public ExtSourceAdded(ExtSource source, Group group) {
    }

    //getPerunBl().getAuditer().log(sess, "{} added to {}.", source, group);
    //getPerunBl().getAuditer().log(sess, "{} added to {}.", source, vo);
}