package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceRemovedFromVo extends AuditEvent {

	private final ExtSource source;
	private final Vo vo;
	private final String message;

	public ExtSourceRemovedFromVo(ExtSource source, Vo vo) {
		this.source = source;
		this.vo = vo;
		this.message = String.format("%s removed from %s.", source, vo);
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
