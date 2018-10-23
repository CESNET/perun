package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpirationInMonthNotification extends AuditEvent {

	private Member member;
	private Vo vo;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public MembershipExpirationInMonthNotification() {
	}

	public MembershipExpirationInMonthNotification(Member member, Vo vo) {
		this.member = member;
		this.vo = vo;
		this.message = formatMessage("%s will expire in a month in %s.", member, vo);
	}

	public Member getMember() {
		return member;
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
