package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OwnerType {
  technical(0),
  administrative(1);

  private static final Map<Integer, OwnerType> lookup = new HashMap<Integer, OwnerType>();

  static {
    for (OwnerType o : EnumSet.allOf(OwnerType.class)) {
      lookup.put(o.getCode(), o);
    }
  }

  int code;

  private OwnerType(int code) {
    this.code = code;
  }

  public static OwnerType getOwnerType(int code) {
    return lookup.get(code);
  }

  public int getCode() {
    return code;
  }
}
