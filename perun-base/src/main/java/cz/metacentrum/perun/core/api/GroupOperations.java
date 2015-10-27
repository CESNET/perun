package cz.metacentrum.perun.core.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum class for group operation types.
 * Created on 15. 10. 2015.
 *
 * @author Oliver Mrázik
 */
public enum GroupOperations {
	UNION(1),
	DIFFERENCE(2);
	
	public static final Map<Integer, GroupOperations> lookup = new HashMap<>();
	int code;
	
	static {
		for (GroupOperations g : EnumSet.allOf(GroupOperations.class))
			lookup.put(g.getCode(), g);
	}
	
	GroupOperations(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static GroupOperations getGroupOperations(int code) {
		return lookup.get(code);
	}
}
