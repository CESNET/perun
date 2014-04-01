package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.rt.MemberNotExistsRuntimeException;

/**
 * Checked version of MemberNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.MemberNotExistsRuntimeException
 * @author Martin Kuba
 */
public class MemberNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Member member;

	public MemberNotExistsException(MemberNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
