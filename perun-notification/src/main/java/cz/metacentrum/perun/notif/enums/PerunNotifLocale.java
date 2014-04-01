package cz.metacentrum.perun.notif.enums;

import java.util.Locale;

/**
 * Enum containing usable locale for localization of messages
 *
 * User: tomastunkl
 * Date: 12.11.12
 * Time: 22:21
 */
public enum PerunNotifLocale {

	cs("cs"),
		en("en");

	PerunNotifLocale(String key) {
		this.key = key;
	}

	String key;

	public String toString() {
		return key;
	}

	public String getKey() {
		return key;
	}

	public static PerunNotifLocale resolvePerunNotifLocale(String locale) {

		for (PerunNotifLocale notifLocale : values()) {
			if (notifLocale.getKey().equals(locale)) {
				return notifLocale;
			}
		}

		return null;
	}

	public static PerunNotifLocale resolvePerunNotifLocale(Locale locale) {

		for (PerunNotifLocale notifLocale : values()) {
			if (notifLocale.getKey().equalsIgnoreCase(locale.getLanguage())) {
				return notifLocale;
			}
		}

		return null;
	}
}
