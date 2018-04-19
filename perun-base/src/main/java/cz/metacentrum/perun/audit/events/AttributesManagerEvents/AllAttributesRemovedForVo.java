package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Vo;

public class AllAttributesRemovedForVo {

    private Vo vo;
    private String name = this.getClass().getName();
    private String message;


    public AllAttributesRemovedForVo(Vo vo) {
        this.vo = vo;
    }

    public AllAttributesRemovedForVo() {
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

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",vo);
    }
}
