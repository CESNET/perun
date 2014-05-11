package cz.metacentrum.perun.notif.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Represents trigger to send notification
 *
 * @author tomas.tunkl
 *
 */
public enum PerunNotifNotifyTrigger {

	// All type of objects must be received
	ALL_REGEX_IDS("all_regex_ids"),
	// Many object of same type, executed by time
	STREAM("stream");

	private PerunNotifNotifyTrigger(String key) {
		this.key = key;
	}

	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static List<PerunNotifNotifyTrigger> getAll() {

		return Arrays.asList(values());
	}

	public static PerunNotifNotifyTrigger resolve(String key) {

		for (PerunNotifNotifyTrigger type : getAll()) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}

		return null;
	}
}
