package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

public class AdminUserRemovedForResource extends AuditEvent {

	private User user;
	private Resource resource;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminUserRemovedForResource() {
	}

	public AdminUserRemovedForResource(User user, Resource resource) {
		this.user = user;
		this.resource = resource;
		this.message = formatMessage("%s was removed from admins of %s.", user, resource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return message;
	}
}
