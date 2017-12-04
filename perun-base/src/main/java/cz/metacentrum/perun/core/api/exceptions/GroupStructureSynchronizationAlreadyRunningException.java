package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Service not exists in underlaying data source.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupStructureSynchronizationAlreadyRunningException
 * @author Peter Balcirak peter.balcirak@gmail.com
 */
public class GroupStructureSynchronizationAlreadyRunningException extends PerunException {
    static final long serialVersionUID = 0;

    private Group group;

    public GroupStructureSynchronizationAlreadyRunningException(String message) {
        super(message);
    }

    public GroupStructureSynchronizationAlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupStructureSynchronizationAlreadyRunningException(Throwable cause) {
        super(cause);
    }

    public GroupStructureSynchronizationAlreadyRunningException(Group group) {
        super(group.toString());
        this.group = group;
    }

    public Group getGroup() {
        return this.group;
    }
}
