package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.core.api.Resource;

public class ResourceUpdated {

	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public ResourceUpdated() {
	}

	public ResourceUpdated(Resource resource) {
		this.resource = resource;
	}

	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
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
		return resource + " updated.";
	}
}
