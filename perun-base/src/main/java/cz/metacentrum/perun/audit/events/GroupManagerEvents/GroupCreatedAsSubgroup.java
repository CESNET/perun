package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class GroupCreatedAsSubgroup extends AuditEvent {

	private final Group group;
	private final Group parentGroup;
	private final Vo vo;
	private final String message;

	public GroupCreatedAsSubgroup(Group group, Vo vo, Group parentGroup) {
		this.group = group;
		this.parentGroup = parentGroup;
		this.vo = vo;
		this.message = String.format("%s created in %s as subgroup of %s", group, vo, parentGroup);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public Group getParentGroup() {
		return parentGroup;
	}

	public Vo getVo() {
		return vo;
	}

	@Override
	public String toString() {
		return message;
	}
}
