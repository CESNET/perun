package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OwnerType {
	technical  (0),
						 administrative (1);

	private static final Map<Integer,OwnerType> lookup = new HashMap<Integer,OwnerType>();
	int code;

	static {
		for(OwnerType o : EnumSet.allOf(OwnerType.class))
			lookup.put(o.getCode(), o);
	}

	private OwnerType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static OwnerType getOwnerType(int code) {
		return lookup.get(code);
	}
}
