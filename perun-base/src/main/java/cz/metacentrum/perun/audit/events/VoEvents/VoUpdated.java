package cz.metacentrum.perun.audit.events.VoEvents;

import cz.metacentrum.perun.core.api.Vo;

public class VoUpdated {
    private Vo vo;

    public VoUpdated(Vo vo) {
        this.vo = vo;
    }

    @Override
    public String toString() {
        return vo + " updated.";
    }
}
