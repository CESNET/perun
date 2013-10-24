package cz.metacentrum.perun.core.api;

public enum Role {

  PERUNADMIN  ("perunadmin"),  
  VOADMIN ("voadmin"),
  GROUPADMIN ("groupadmin"),
  SELF ("self"),
  FACILITYADMIN ("facilityadmin"),
  SERVICE	("service"),
  SYNCHRONIZER ("synchronizer"),
  REGISTRAR ("registrar"),
  ENGINE ("engine"),
  RPC ("rpc"),
  NOTIFICATIONS ("notifications"),
  SERVICEUSER ("serviceuser"),
  UNKNOWNROLENAME ("unknown");

  private final String roleName;
  
  Role(String roleName) {
    this.roleName = roleName;
  }
  
  public String getRoleName() {
    return roleName;
  }
}
