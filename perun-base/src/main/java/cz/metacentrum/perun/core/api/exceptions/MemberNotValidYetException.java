package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Member;

/**
 * Operation required complete member. This member is still invalid (his record in perun is not complete)
 *
 * @author Slavek Licehammer
 */
public class MemberNotValidYetException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MemberNotValidYetException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotValidYetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MemberNotValidYetException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the member
	 * @param member the member that is invalid
	 */
	public MemberNotValidYetException(Member member) {
		super(member.toString());
		this.member = member;
	}

	/**
	 * Getter for the member
	 * @return the member that is invalid
	 */
	public Member getMember() {
		return this.member;
	}
}
