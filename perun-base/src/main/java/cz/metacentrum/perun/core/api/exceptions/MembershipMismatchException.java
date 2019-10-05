package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * This exception is thrown when trying to add member to the group from other VO
 *
 * @author Michal Stava
 */
public class MembershipMismatchException extends InternalErrorException {
	static final long serialVersionUID = 0;

	private Member member;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MembershipMismatchException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MembershipMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MembershipMismatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member member from the other VO
	 */
	public MembershipMismatchException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Getter for the member
	 * @return member from other VO
	 */
	public Member getMember() {
		return this.member;
	}
}
