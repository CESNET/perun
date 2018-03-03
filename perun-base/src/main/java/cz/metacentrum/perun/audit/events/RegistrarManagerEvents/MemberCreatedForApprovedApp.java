package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.registrar.model.Application;

public class MemberCreatedForApprovedApp implements AuditEvent {

	private Member member;
	private Application app;
	private String name = this.getClass().getName();
	private String message;

	public MemberCreatedForApprovedApp() {
	}

	public MemberCreatedForApprovedApp(Member member, Application app) {
		this.member = member;
		this.app = app;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
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
		return String.format("{} created for approved {}.", member, app);
	}
}
