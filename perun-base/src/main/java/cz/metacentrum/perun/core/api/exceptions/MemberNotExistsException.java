package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Checked version of MemberNotExistsException.
 *
 * @author Martin Kuba
 */
public class MemberNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Member member;

	public MemberNotExistsException(String message) {
		super(message);
	}

	public MemberNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemberNotExistsException(Throwable cause) {
		super(cause);
	}

	public MemberNotExistsException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return this.member;
	}
}
