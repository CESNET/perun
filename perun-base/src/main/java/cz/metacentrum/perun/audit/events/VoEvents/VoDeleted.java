package cz.metacentrum.perun.audit.events.VoEvents;

import cz.metacentrum.perun.core.api.Vo;

public class VoDeleted {
    private Vo vo;

    public VoDeleted(Vo vo) {
        this.vo = vo;
    }

    @Override
    public String toString() {
        return vo + " deleted.";
    }
}
