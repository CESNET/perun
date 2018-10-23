package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberSuspended implements AuditEvent {

	private Member member;
	private String engineForceKeyword;
	private String name = this.getClass().getName();
	private String message;

	public MemberSuspended(Member member, String engineForceKeyword) {
		this.member = member;
		this.engineForceKeyword = engineForceKeyword;
	}

	public MemberSuspended() {
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

	public String getEngineForceKeyword() {
		return engineForceKeyword;
	}

	public void setEngineForceKeyword(String engineForceKeyword) {
		this.engineForceKeyword = engineForceKeyword;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return member + " suspended #" + engineForceKeyword + ".";
	}
}
