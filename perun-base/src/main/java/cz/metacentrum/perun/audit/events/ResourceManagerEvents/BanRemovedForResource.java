package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnResource;

public class BanRemovedForResource implements AuditEvent {
	private BanOnResource banOnResource;
	private int memberId;
	private int resourceId;
	private String name = this.getClass().getName();
	private String message;

	public BanRemovedForResource(BanOnResource ban, int memberId, int resourceId) {
		this.banOnResource = ban;
		this.memberId = memberId;
		this.resourceId = resourceId;
	}

	public BanRemovedForResource() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BanOnResource getBanOnResource() {
		return banOnResource;
	}

	public void setBanOnResource(BanOnResource banOnResource) {
		this.banOnResource = banOnResource;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Ban " + banOnResource + " was removed for memberId " + memberId + " on resourceId " + resourceId + ".";
	}
}
