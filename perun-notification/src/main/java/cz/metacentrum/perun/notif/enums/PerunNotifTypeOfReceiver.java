package cz.metacentrum.perun.notif.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Represents which type can receiver be
 *
 * @author tomas.tunkl
 *
 */
public enum PerunNotifTypeOfReceiver {

	EMAIL_USER("EMAIL_USER"),
	EMAIL_GROUP("EMAIL_GROUP"),
	JABBER("JABBER");

	private PerunNotifTypeOfReceiver(String key) {
		this.key = key;
	}

	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static List<PerunNotifTypeOfReceiver> getAll() {

		return Arrays.asList(values());
	}

	public static PerunNotifTypeOfReceiver resolve(String key) {

		for (PerunNotifTypeOfReceiver type : getAll()) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}

		return null;
	}
}
