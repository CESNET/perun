package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.core.api.Vo;

public class AllGroupsFromVoDeleted {

    private Vo vo;

    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public AllGroupsFromVoDeleted(Vo vo) {
        this.vo = vo;
    }

    public AllGroupsFromVoDeleted() {
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

    @Override
    public String toString() {
        return "All group in "+ vo + " deleted.";
    }
}
