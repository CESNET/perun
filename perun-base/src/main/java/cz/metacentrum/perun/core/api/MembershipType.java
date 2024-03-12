package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Papperwing
 */
public enum MembershipType {
  NOT_DEFINED(0), DIRECT(1), INDIRECT(2);

  private static final Map<Integer, MembershipType> LOOKUP = new HashMap<>();

  static {
    for (MembershipType o : EnumSet.allOf(MembershipType.class)) {
      LOOKUP.put(o.getCode(), o);
    }
  }

  int code;

  MembershipType(int code) {
    this.code = code;
  }

  public static MembershipType getMembershipType(int code) {
    return LOOKUP.get(code);
  }

  public int getCode() {
    return code;
  }


}
