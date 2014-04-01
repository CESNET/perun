package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.rt.AlreadyMemberRuntimeException;

/**
 * Checked version of AlreadyMemberException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AlreadyMemberRuntimeException
 * @author Martin Kuba
 */
public class AlreadyMemberException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	public AlreadyMemberException(AlreadyMemberRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public AlreadyMemberException(String message) {
		super(message);
	}

	public AlreadyMemberException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyMemberException(Throwable cause) {
		super(cause);
	}

	public AlreadyMemberException(Member member) {
		super(member.toString());
		this.member = member;
	}

	public Member getMember() {
		return member;
	}
}
