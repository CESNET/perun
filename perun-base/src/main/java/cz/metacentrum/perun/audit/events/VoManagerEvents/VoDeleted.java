package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class VoDeleted extends AuditEvent {

	private final Vo vo;
	private final String message;

	public VoDeleted(Vo vo) {
		this.vo = vo;
		this.message = String.format("%s deleted.", vo);
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
		return vo + " deleted.";
	}
}
