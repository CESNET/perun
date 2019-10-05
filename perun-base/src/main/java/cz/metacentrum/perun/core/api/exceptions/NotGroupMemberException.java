package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

/**
 * This exception is thrown when the member is not a member of the group or does not exist at all
 *
 * @author Michal Prochazka
 */
public class NotGroupMemberException extends PerunException {
	static final long serialVersionUID = 0;

	private Member member;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public NotGroupMemberException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NotGroupMemberException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NotGroupMemberException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group and the member
	 * @param group group that the member is not in
	 * @param member member who is not in the group
	 */
	public NotGroupMemberException(Group group, Member member) {
		super((group == null ? "null" : group) +
				", " + (member == null ? "null" : member));
		this.member = member;
		this.group = group;
	}

	/**
	 * Getter for the member
	 * @return the member that isn't in the group
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * Getter for the group
	 * @return the group that the member is not in
	 */
	public Group getGroup() {
		return group;
	}
}
