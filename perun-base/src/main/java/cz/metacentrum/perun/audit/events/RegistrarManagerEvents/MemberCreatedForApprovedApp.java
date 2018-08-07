package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.registrar.model.Application;

public class MemberCreatedForApprovedApp extends AuditEvent {

	private final Member member;
	private final Application app;
	private final String message;

	public MemberCreatedForApprovedApp(Member member, Application app) {
		this.member = member;
		this.app = app;
		this.message = String.format("%s created for approved %s.", member, app);
	}

	public Member getMember() {
		return member;
	}

	public Application getApp() {
		return app;
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
