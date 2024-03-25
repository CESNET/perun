package cz.metacentrum.perun.ldapc.beans;

/**
 * LdapProperties data from properties file.
 */
public class LdapProperties {

  private String ldapConsumerName;
  private String ldapBase;
  private String ldapLoginNamespace;
  private String ldapStateFile;
  private boolean isReplica = false;

  public LdapProperties(String ldapConsumerName, String ldapBase, String ldapLoginNamespace, String ldapStateFile,
                        String isReplica) {
    this.ldapConsumerName = ldapConsumerName;
    this.ldapBase = ldapBase;
    this.ldapLoginNamespace = ldapLoginNamespace;
    this.ldapStateFile = ldapStateFile;
    this.isReplica = Boolean.parseBoolean(isReplica);
  }

  public String getLdapBase() {
    return ldapBase;
  }

  public String getLdapConsumerName() {
    return ldapConsumerName;
  }

  public String getLdapLoginNamespace() {
    return ldapLoginNamespace;
  }

  public String getLdapStateFile() {
    return ldapStateFile;
  }

  public boolean isReplica() {
    return isReplica;
  }

  public boolean propsLoaded() {
    return ldapConsumerName != null && ldapBase != null && ldapLoginNamespace != null;
  }

}
