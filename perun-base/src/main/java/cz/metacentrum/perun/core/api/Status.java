package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Status {
  VALID(0), INVALID(1),    //just created object, where some information (e.g. attribute)  is missing
  //    SUSPENDED (2),  //security issue
  EXPIRED(3), DISABLED(4);   //use this status instead of deleting the entity

  private static final Map<Integer, Status> LOOKUP = new HashMap<Integer, Status>();

  static {
    for (Status s : EnumSet.allOf(Status.class)) {
      LOOKUP.put(s.getCode(), s);
    }
  }

  int code;

  private Status(int code) {
    this.code = code;
  }

  public static Status getStatus(int code) {
    return LOOKUP.get(code);
  }

  public int getCode() {
    return code;
  }
}
