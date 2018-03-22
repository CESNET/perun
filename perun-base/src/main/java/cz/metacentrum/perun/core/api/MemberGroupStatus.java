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
	ACTIVE(0),
	EXPIRED(1);

	private static final Map<Integer, MemberGroupStatus> lookup = new HashMap<>();
	int code;

	static {
		for (MemberGroupStatus status : EnumSet.allOf(MemberGroupStatus.class)) {
			lookup.put(status.code, status);
		}
	}

	MemberGroupStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static MemberGroupStatus getMemberGroupStatus(int code) {
		return lookup.get(code);
	}
}
