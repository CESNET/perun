package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpired extends AuditEvent {

	private final Member member;
	private final int daysAfterExpiration;
	private final Vo vo;
	private final String message;

	public MembershipExpired(Member member, int daysAfterExpiration, Vo vo) {
		this.member = member;
		this.daysAfterExpiration = daysAfterExpiration;
		this.vo = vo;
		this.message = String.format("%s has expired %d days ago in %s.", member, daysAfterExpiration, vo);
	}

	public Member getMember() {
		return member;
	}

	public int getDaysAfterExpiration() {
		return daysAfterExpiration;
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
