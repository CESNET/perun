package cz.metacentrum.perun.ldapc.beans;

/**
 * Representation of a single regex substitution, which is applied by {@link RegexpValueTransformer} on the values
 * before they are pushed to the LDAP.
 */
public class RegexpSubst {

  /**
   * Regex of what should be "found" in the value
   */
  private String find;

  /**
   * Replacement value for the "found" part of the attribute value
   */
  private String replace;

  public String getFind() {
    return find;
  }

  public String getReplace() {
    return replace;
  }

  public void setFind(String find) {
    this.find = find;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

}
