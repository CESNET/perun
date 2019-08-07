package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class UserSyncFinishedWithErrors extends AuditEvent implements EngineIgnoreEvent {

	private User user;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public UserSyncFinishedWithErrors() {
	}

	public UserSyncFinishedWithErrors(User user) {
		this.user = user;
		this.message = formatMessage( "%s synchronization finished with errors.", user);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return message;
	}
}