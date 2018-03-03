package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;

public class MemberValidatedFailed implements AuditEvent {

	private Member member;
	private Status status;
	private String name = this.getClass().getName();
	private String message;

	public MemberValidatedFailed(Member member, Status oldStatus) {
		this.member = member;
		this.status = oldStatus;
	}

	public MemberValidatedFailed() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Validation of " + member + " failed. He stays in " + status + " state.";
	}
}
