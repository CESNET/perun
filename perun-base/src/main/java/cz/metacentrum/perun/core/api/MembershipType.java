package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Papperwing
 */
public enum MembershipType {
	NOT_DEFINED(0),
		DIRECT(1),
		INDIRECT(2),
		INCLUDED(3),
		EXCLUDED(4);


	private static final Map<Integer,MembershipType> lookup = new HashMap<Integer,MembershipType>();
	int code;

	static {
		for(MembershipType o : EnumSet.allOf(MembershipType.class))
			lookup.put(o.getCode(), o);
	}

	private MembershipType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static MembershipType getMembershipType(int code) {
		return lookup.get(code);
	}


}
