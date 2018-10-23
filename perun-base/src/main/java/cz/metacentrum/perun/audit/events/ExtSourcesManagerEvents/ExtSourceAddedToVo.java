package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceAddedToVo implements AuditEvent {
	private ExtSource source;
	private Vo vo;

	private String name = this.getClass().getName();
	private String message;

	public ExtSourceAddedToVo(ExtSource source, Vo vo) {
		this.source = source;
		this.vo = vo;
	}

	public ExtSourceAddedToVo() {
	}

	public ExtSource getSource() {
		return source;
	}

	public void setSource(ExtSource source) {
		this.source = source;
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
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return source + " added to " + vo + ".";
	}
}
