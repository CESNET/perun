package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.core.api.Vo;

public class VoUpdated {
    private Vo vo;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VoUpdated() {
    }

    public Vo getVo() {
        return vo;
    }

    public void setVo(Vo vo) {
        this.vo = vo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VoUpdated(Vo vo) {
        this.vo = vo;
    }

    @Override
    public String toString() {
        return vo + " updated.";
    }
}
