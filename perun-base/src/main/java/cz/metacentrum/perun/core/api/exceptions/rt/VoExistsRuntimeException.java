package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class VoExistsRuntimeException extends EntityExistsRuntimeException {
	private String voName;

	public VoExistsRuntimeException() {
		super();
	}

	public VoExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public VoExistsRuntimeException(String voName, Throwable cause) {
		super(voName, cause);
		this.voName = voName;
	}

	public VoExistsRuntimeException(String voName) {
		super(voName);
		this.voName = voName;
	}

	public String getVoName() {
		return voName;
	}
}
