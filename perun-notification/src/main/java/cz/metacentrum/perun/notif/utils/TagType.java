package cz.metacentrum.perun.notif.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: tomastunkl Date: 29.09.12 Time: 23:48 To
 * change this template use File | Settings | File Templates.
 */
public enum TagType {

	FOR("for"),
	IF("if");

	private TagType(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static List<TagType> getAll() {
		return Arrays.asList(values());
	}

	public static TagType resolve(String value) {

		if (value == null || value.isEmpty()) {
			return null;
		}

		value = value.trim();
		for (TagType type : getAll()) {
			if (type.getValue().equalsIgnoreCase(value)) {
				return type;
			}
		}

		return null;
	}
}
