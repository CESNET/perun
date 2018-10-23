package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class VoDeleted implements AuditEvent {
	private Vo vo;
	private String name = this.getClass().getName();
	private String message;

	public VoDeleted() {
	}

	public VoDeleted(Vo vo) {
		this.vo = vo;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Vo getVo() {
		return vo;
	}

	public void setVo(Vo vo) {
		this.vo = vo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return vo + " deleted.";
	}
}
