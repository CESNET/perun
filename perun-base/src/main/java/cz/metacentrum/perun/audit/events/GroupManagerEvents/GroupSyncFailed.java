package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.core.api.Group;

public class GroupSyncFailed {

    private Group group;
    private String originalExceptionMessage;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GroupSyncFailed(Group group, String originalExceptionMessage) {
        this.group = group;
        this.originalExceptionMessage = originalExceptionMessage;
    }

    public GroupSyncFailed() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getOriginalExceptionMessage() {
        return originalExceptionMessage;
    }

    public void setOriginalExceptionMessage(String originalExceptionMessage) {
        this.originalExceptionMessage = originalExceptionMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return  group + " synchronization failed because of " + originalExceptionMessage + ".";
    }
}
