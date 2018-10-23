package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupSyncFinishedWithErrors extends AuditEvent {

	private Group group;
	private String originalExceptionMessage;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public GroupSyncFinishedWithErrors() {
	}

	public GroupSyncFinishedWithErrors(Group group, String originalExceptionMessage) {
		this.group = group;
		this.originalExceptionMessage = originalExceptionMessage;
		this.message = formatMessage("%s synchronization finished with errors: %s.", group, originalExceptionMessage);
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
