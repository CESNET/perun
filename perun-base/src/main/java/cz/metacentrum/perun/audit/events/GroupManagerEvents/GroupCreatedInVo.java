package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class GroupCreatedInVo implements AuditEvent {

	private Group group;
	private Vo vo;
	private String name = this.getClass().getName();
	private String message;

	public GroupCreatedInVo(Group group, Vo vo) {
		this.group = group;
		this.vo = vo;
	}

	public GroupCreatedInVo() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
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
		return group + " created in " + vo + ".";
	}
}
