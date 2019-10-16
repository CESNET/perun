package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class AdminGroupAddedForVo extends AuditEvent implements EngineIgnoreEvent {

	private Vo vo;
	private Group group;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminGroupAddedForVo() {
	}

	public AdminGroupAddedForVo(Group group, Vo vo) {
		this.group = group;
		this.vo = vo;
		this.message = formatMessage("%s was added as admin of %s.", group, vo);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public Vo getVo() {
		return vo;
	}

	@Override
	public String toString() {
		return message;
	}
}
