package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Ban;

/**
 * This exception is thrown when the ban on the facility/resource does not exist
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class BanNotExistsException extends EntityExistsException {

	private Ban ban;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public BanNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public BanNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public BanNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with ban that does not exist
	 * @param ban
	 */
	public BanNotExistsException(Ban ban) {
		super("Ban of type: " + ban.getType() + " not exists for subjectID: " + ban.getSubjectId() + " and targetID: " + ban.getTargetId());
		this.ban = ban;
	}

	/**
	 * Getter for the ban
	 * @return ban that does not exist
	 */
	public Ban getBan() {
		return ban;
	}

	/**
	 * Setter for the ban
	 * @param ban that does not exist
	 */
	public void setBan(Ban ban) {
		this.ban = ban;
	}
}
