package cz.metacentrum.perun.core.api;

public enum Role {

	PERUNADMIN  ("perunadmin"),
	PERUNOBSERVER ("perunobserver"),
	VOADMIN ("voadmin"),
	GROUPADMIN ("groupadmin"),
	SELF ("self"),
	FACILITYADMIN ("facilityadmin"),
	RESOURCEADMIN ("resourceadmin"),
	RESOURCESELFSERVICE("resourceselfservice"),
	REGISTRAR ("registrar"),
	ENGINE ("engine"),
	RPC ("rpc"),
	NOTIFICATIONS ("notifications"),
	SERVICEUSER ("serviceuser"),
	SPONSOR ("sponsor"),
	VOOBSERVER ("voobserver"),
	TOPGROUPCREATOR ("topgroupcreator"),
	SECURITYADMIN ("securityadmin"),
	CABINETADMIN ("cabinetadmin"),
	UNKNOWNROLENAME ("unknown");

	private final String roleName;

	Role(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleName() {
		return roleName;
	}
}
