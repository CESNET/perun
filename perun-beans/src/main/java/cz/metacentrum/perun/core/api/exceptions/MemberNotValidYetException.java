package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Operation required complete member. This member is still invalid (his record in perun is not complete),
 *
 * @author Slavek Licehammer
 */
public class MemberNotValidYetException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	public MemberNotValidYetException(String message) {
		super(message);
	}

	public MemberNotValidYetException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemberNotValidYetException(Throwable cause) {
		super(cause);
	}

	public MemberNotValidYetException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return this.member;
	}
}
