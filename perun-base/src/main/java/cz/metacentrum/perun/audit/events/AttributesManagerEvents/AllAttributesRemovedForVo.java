package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class AllAttributesRemovedForVo extends AuditEvent {

	private final Vo vo;
	private final String message;

	public AllAttributesRemovedForVo(Vo vo) {
		this.vo = vo;
		this.message = String.format("All attributes removed for %s.", vo);
	}

	public Vo getVo() {
		return vo;
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
