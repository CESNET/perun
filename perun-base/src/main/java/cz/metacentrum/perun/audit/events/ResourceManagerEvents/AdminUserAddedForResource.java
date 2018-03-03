package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

public class AdminUserAddedForResource implements AuditEvent {

	private User user;
	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public AdminUserAddedForResource(User user, Resource resource) {
		this.user = user;
		this.resource = resource;
	}

	public AdminUserAddedForResource() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return user + " was added as admin of " + resource + ".";
	}
}
