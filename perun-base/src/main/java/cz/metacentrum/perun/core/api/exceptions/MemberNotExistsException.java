package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * This exception is thrown when the member doesn't exist or isn't a member of the specific VO
 * or wasn't found using userExtSource
 *
 * @author Martin Kuba
 */
public class MemberNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Member member;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MemberNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member the member that doesn't exist
	 */
	public MemberNotExistsException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Getter for the member
	 * @return member that doesn't exist
	 */
	public Member getMember() {
		return this.member;
	}
}
