package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.audit.events.AuditEvent;

/**
 * This is a wrapper around AuditEvent with metadata, like ID, timestamp, and actor.
 * It is used solely for reading purpose (once event is stored in DB).
 *
 * @see cz.metacentrum.perun.audit.events.AuditEvent
 *
 * @author Michal Stava
 * @author Pavel Zlámal
 */
public class AuditMessage {

	protected int id;
	protected AuditEvent event;
	protected String actor;
	protected String createdAt;
	protected Integer createdByUid;

	public AuditMessage() {
	}

	public AuditMessage(int id, AuditEvent event, String actor, String createdAt, Integer createdByUid) {
		this();
		this.id = id;
		this.event = event;
		this.actor = actor;
		this.createdAt = createdAt;
		this.createdByUid = createdByUid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public void setEvent(AuditEvent event) {
		this.event = event;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public Integer getCreatedByUid() {
		return createdByUid;
	}

	public void setCreatedByUid(Integer createdByUid) {
		this.createdByUid = createdByUid;
	}

	public String getUIMessage() {
		if (event != null) {
			return BeansUtils.eraseEscaping(BeansUtils.replaceEscapedNullByStringNull(BeansUtils.replacePointyBracketsByApostrophe(event.getMessage())));
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getClass().getSimpleName());
		ret.append(":[id='");
		ret.append(id);
		ret.append("', event='");
		ret.append(event);
		ret.append("']");

		return ret.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((event == null) ? 0 : event.hashCode());
		result = prime * result + id;
		result = prime * result
			+ ((createdAt == null) ? 0 : createdAt.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuditMessage other = (AuditMessage) obj;
		if (id != other.id)
			return false;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (createdByUid == null) {
			if (other.createdByUid != null)
				return false;
		} else if (other.createdByUid == null) {
			return false;
		} else if (createdByUid != other.createdByUid)
			return false;
		return true;
	}
}
