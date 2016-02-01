package cz.metacentrum.perun.core.api;

public enum SpecificUserType {

	NORMAL ("normal"),
	SERVICE ("service"),
	SPONSORED ("sponsored");

	private final String specificUserType;

	SpecificUserType(String specificUserType) {
		this.specificUserType = specificUserType;
	}

	public String getSpecificUserType() {
		return specificUserType;
	}
}
