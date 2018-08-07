package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupSyncFailed extends AuditEvent {

	private final Group group;
	private final String originalExceptionMessage;
	private final String message;

	public GroupSyncFailed(Group group, String originalExceptionMessage) {
		this.group = group;
		this.originalExceptionMessage = originalExceptionMessage;
		this.message = String.format( "%s synchronization failed because of %s.", group, originalExceptionMessage);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public String getOriginalExceptionMessage() {
		return originalExceptionMessage;
	}

	@Override
	public String toString() {
		return message;
	}
}
