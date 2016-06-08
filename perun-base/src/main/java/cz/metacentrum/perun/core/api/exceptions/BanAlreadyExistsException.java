package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Ban;

/**
 * Checked version of BanAlreadyExistsException
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class BanAlreadyExistsException extends EntityExistsException {

	private Ban ban;

	public BanAlreadyExistsException(String message) {
		super(message);
	}

	public BanAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public BanAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public BanAlreadyExistsException(Ban ban) {
		super("Ban of type: " + ban.getType() + " already exists for subjectID: " + ban.getSubjectId() + " and targetID: " + ban.getTargetId());
		this.ban = ban;
	}

	public Ban getBan() {
		return ban;
	}

	public void setBan(Ban ban) {
		this.ban = ban;
	}
}
