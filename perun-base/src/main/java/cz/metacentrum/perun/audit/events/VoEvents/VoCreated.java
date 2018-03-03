package cz.metacentrum.perun.audit.events.VoEvents;

import cz.metacentrum.perun.core.api.Vo;

public class VoCreated {
    private Vo vo;

    public VoCreated(Vo vo) {
        this.vo = vo;
    }

    @Override
    public String toString() {
        return vo + " created.";
    }
}
