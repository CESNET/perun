package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class VoCreated extends AuditEvent {

	private final Vo vo;
	private final String message;

	public VoCreated(Vo vo) {
		this.vo = vo;
		this.message = String.format("%s created.", vo);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Vo getVo() {
		return vo;
	}

	@Override
	public String toString() {
		return message;
	}
}
