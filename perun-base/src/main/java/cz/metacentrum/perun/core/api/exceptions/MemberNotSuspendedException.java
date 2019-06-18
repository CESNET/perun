package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Operation required suspended member, but this member is not suspended at all.
 *
 * @author Michal Stava &lt;Michal.Stava@cesnet.cz&gt;
 */
public class MemberNotSuspendedException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	public MemberNotSuspendedException(String message) {
		super(message);
	}

	public MemberNotSuspendedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemberNotSuspendedException(Throwable cause) {
		super(cause);
	}

	public MemberNotSuspendedException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return this.member;
	}
}
