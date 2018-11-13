package cz.metacentrum.perun.core.api.exceptions;


/**
 * Checked version of CandidateNotExistsException.
 *
 * @author Martin Kuba
 */
public class CandidateNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private String candidate;

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
