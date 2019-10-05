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

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MemberNotSuspendedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotSuspendedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotSuspendedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member the member that is not suspended
	 */
	public MemberNotSuspendedException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Getter for the member
	 * @return the member that is not suspended
	 */
	public Member getMember() {
		return this.member;
	}
}
