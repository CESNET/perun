package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceAddedToGroup {

    private ExtSource source;
    private Group group;

    private String name = this.getClass().getName();
    private String message;

    public ExtSourceAddedToGroup(ExtSource source, Group group) {
        this.source = source;
        this.group = group;
    }

    public ExtSourceAddedToGroup() {
    }

    public ExtSource getSource() {
        return source;
    }

    public void setSource(ExtSource source) {
        this.source = source;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
        return String.format("%s added to %s.",source,group);
    }



}