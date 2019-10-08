package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * This exception is thrown when the user is already a member of the Vo/group.
 *
 * @author Martin Kuba
 */
public class AlreadyMemberException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AlreadyMemberException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyMemberException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AlreadyMemberException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member the member
	 */
	public AlreadyMemberException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Getter for the member
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}
}
