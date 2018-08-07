package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;

public class ExtSourceRemovedFromGroup extends AuditEvent {

	private final ExtSource source;
	private final Group group;
	private final String message;

	public ExtSourceRemovedFromGroup(ExtSource source, Group group) {
		this.source = source;
		this.group = group;
		this.message = String.format("%s removed from %s.", source, group);
	}

	public ExtSource getSource() {
		return source;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
