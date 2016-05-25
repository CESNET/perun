package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Ban;

/**
 * Checked version of BanNotExistsException
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class BanNotExistsException extends EntityExistsException {

	private Ban ban;

	public BanNotExistsException(String message) {
		super(message);
	}

	public BanNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public BanNotExistsException(Throwable cause) {
		super(cause);
	}

	public BanNotExistsException(Ban ban) {
		super("Ban of type: " + ban.getType() + " not exists for subjectID: " + ban.getSubjectId() + " and targetID: " + ban.getTargetId());
		this.ban = ban;
	}

	public Ban getBan() {
		return ban;
	}

	public void setBan(Ban ban) {
		this.ban = ban;
	}
}
