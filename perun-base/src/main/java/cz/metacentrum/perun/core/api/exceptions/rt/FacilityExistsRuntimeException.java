package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class FacilityExistsRuntimeException extends EntityExistsRuntimeException {
	private String facilityName;

	public FacilityExistsRuntimeException() {
		super();
	}

	public FacilityExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public FacilityExistsRuntimeException(String facilityName, Throwable cause) {
		super(facilityName, cause);
		this.facilityName = facilityName;
	}

	public FacilityExistsRuntimeException(String facilityName) {
		super(facilityName);
		this.facilityName = facilityName;
	}

	public String getFacilityName() {
		return facilityName;
	}
}
