package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Ban;

/**
 * This exception is thrown when the ban on the facility/resource already exists
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class BanAlreadyExistsException extends EntityExistsException {

	private Ban ban;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public BanAlreadyExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public BanAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public BanAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the ban that already exists
	 * @param ban ban that already exists
	 */
	public BanAlreadyExistsException(Ban ban) {
		super("Ban of type: " + ban.getType() + " already exists for subjectID: " + ban.getSubjectId() + " and targetID: " + ban.getTargetId());
		this.ban = ban;
	}

	/**
	 * Getter for the ban
	 * @return the ban that already exists
	 */
	public Ban getBan() {
		return ban;
	}

	/**
	 * Setter for the ban
	 * @param ban the ban that already exists
	 */
	public void setBan(Ban ban) {
		this.ban = ban;
	}
}
