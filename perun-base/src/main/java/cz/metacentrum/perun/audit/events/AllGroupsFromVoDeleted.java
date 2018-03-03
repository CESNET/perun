package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Vo;

public class AllGroupsFromVoDeleted {

    private Vo vo;
    public AllGroupsFromVoDeleted(Vo vo) {
        this.vo = vo;
    }

    @Override
    public String toString() {
        return "All group in "+ vo + " deleted.";
    }
}
