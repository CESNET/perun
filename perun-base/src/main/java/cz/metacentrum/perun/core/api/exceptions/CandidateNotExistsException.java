package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.MemberNotExistsRuntimeException;

/**
 * Checked version of CandidateNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.CandidateNotExistsRuntimeException
 * @author Martin Kuba
 */
public class CandidateNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private String candidate;

	public CandidateNotExistsException(MemberNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public CandidateNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public CandidateNotExistsException(Throwable cause) {
		super(cause);
	}

	public CandidateNotExistsException(String candidate) {
		super(candidate.toString());
		this.candidate = candidate;
	}

	public String getCandidate() {
		return this.candidate;
	}
}
