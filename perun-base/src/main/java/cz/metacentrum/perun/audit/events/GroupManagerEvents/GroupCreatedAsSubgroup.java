package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class GroupCreatedAsSubgroup {

    private Group group, parentGroup;
    private Vo vo;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GroupCreatedAsSubgroup(Group group, Vo vo, Group parentGroup) {
        this.group = group;
        this.parentGroup = parentGroup;
        this.vo = vo;
    }

    public GroupCreatedAsSubgroup() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
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
        return group + " created in " + vo + " as subgroup of " + parentGroup;
    }
}
