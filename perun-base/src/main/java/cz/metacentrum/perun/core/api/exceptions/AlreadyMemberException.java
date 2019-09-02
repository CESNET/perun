package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Checked version of AlreadyMemberException
 * This exception is thrown when the user is already a member.
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
	 * Constructor with a member
	 * @param member who is already a member
	 */
	public AlreadyMemberException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 *
	 * @return member who is already a member
	 */
	public Member getMember() {
		return member;
	}
}
