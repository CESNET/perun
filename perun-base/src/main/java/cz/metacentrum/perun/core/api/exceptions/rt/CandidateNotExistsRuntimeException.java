package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class CandidateNotExistsRuntimeException extends EntityNotExistsRuntimeException {
	private String candidate;

	public CandidateNotExistsRuntimeException() {
		super();
	}

	public CandidateNotExistsRuntimeException(String candidate) {
		super();
		this.candidate = candidate;
	}

	public CandidateNotExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public CandidateNotExistsRuntimeException(Throwable cause, String candidate) {
		super(cause);
		this.candidate = candidate;
	}

	public String getCandidate() {
		return candidate;
	}
}
