package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceRemoved {

    private ExtSource source;
    private Vo vo;
    private Group group;
    public ExtSourceRemoved(ExtSource source, Vo vo) {
        this.source = source;
        this.vo = vo;
    }

    public ExtSourceRemoved(ExtSource source, Group group) {
        this.source = source;
        this.group = group;
    }

}
