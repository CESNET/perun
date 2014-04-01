package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.rt.NotGroupMemberRuntimeException;

/**
 * Checked version of NotGroupMemberException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.NotGroupMemberRuntimeException
 * @author Michal Prochazka
 */
public class NotGroupMemberException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	public NotGroupMemberException(NotGroupMemberRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public NotGroupMemberException(String message) {
		super(message);
	}

	public NotGroupMemberException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotGroupMemberException(Throwable cause) {
		super(cause);
	}

	public NotGroupMemberException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return member;
	}
}
