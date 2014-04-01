package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Is not allowed to add member to group of other VO.
 *
 * @author Michal Stava
 */
public class MembershipMismatchException extends InternalErrorException {
	static final long serialVersionUID = 0;

	private Member member;

	public MembershipMismatchException(String message) {
		super(message);
	}

	public MembershipMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public MembershipMismatchException(Throwable cause) {
		super(cause);
	}

	public MembershipMismatchException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return this.member;
	}
}
