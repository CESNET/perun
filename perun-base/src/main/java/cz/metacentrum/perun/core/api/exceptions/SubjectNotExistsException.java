package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.MemberNotExistsRuntimeException;

/**
 * Checked version of SubjectNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.SubjectNotExistsRuntimeException
 * @author Martin Kuba
 */
public class SubjectNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private String subject;

	public SubjectNotExistsException(MemberNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public SubjectNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubjectNotExistsException(Throwable cause) {
		super(cause);
	}

	public SubjectNotExistsException(String subject) {
		super(subject.toString());
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}
}
