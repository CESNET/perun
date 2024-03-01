package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum defining member's status in group
 *
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public enum MemberGroupStatus {
  VALID(0), EXPIRED(1);

  private static final Map<Integer, MemberGroupStatus> LOOKUP = new HashMap<>();

  static {
    for (MemberGroupStatus status : EnumSet.allOf(MemberGroupStatus.class)) {
      LOOKUP.put(status.code, status);
    }
  }

  int code;

  MemberGroupStatus(int code) {
    this.code = code;
  }

  public static MemberGroupStatus getMemberGroupStatus(int code) {
    return LOOKUP.get(code);
  }

  public int getCode() {
    return code;
  }
}
