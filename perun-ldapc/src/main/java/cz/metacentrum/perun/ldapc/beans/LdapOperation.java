package cz.metacentrum.perun.ldapc.beans;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Ldap Operations (remove, modify, add)
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public enum LdapOperation {
  ADD_ATTRIBUTE(1),    //just created object, where some information (e.g. attribute)  is missing
  REPLACE_ATTRIBUTE(2),  //security issue
  REMOVE_ATTRIBUTE(3);

  private static final Map<Integer, LdapOperation> LOOKUP = new HashMap<Integer, LdapOperation>();

  static {
    for (LdapOperation s : EnumSet.allOf(LdapOperation.class)) {
      LOOKUP.put(s.getCode(), s);
    }
  }

  int code;

  private LdapOperation(int code) {
    this.code = code;
  }

  public static LdapOperation getLdapOperation(int code) {
    return LOOKUP.get(code);
  }

  public int getCode() {
    return code;
  }
}
