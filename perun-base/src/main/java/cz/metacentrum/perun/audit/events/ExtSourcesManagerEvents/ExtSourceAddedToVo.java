package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceAddedToVo extends AuditEvent {

	private final ExtSource source;
	private final Vo vo;
	private final String message;

	public ExtSourceAddedToVo(ExtSource source, Vo vo) {
		this.source = source;
		this.vo = vo;
		this.message = String.format("%s added to %s.", source, vo);
	}

	public ExtSource getSource() {
		return source;
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
