package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.PerunBean;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "name")
public abstract class AuditEvent {

	protected String name = getClass().getName();

	/**
	 * Get message that should be logged.
	 *
	 * @return message
	 */
	public abstract String getMessage();

	/**
	 * Get name of the event class
	 *
	 * @return name of event class
	 */
	public String getName() {
		return name;
	}

	/**
	 * Formats the given args in format that can be audited.
	 *
	 * For PerunBeans calls 'serializeToString, for any other type 'toString'.
	 *
	 * @param formatString format String
	 * @param args arguments
	 * @return formatted message
	 */
	protected String formatMessage(String formatString, Object... args) {
		Object[] formattedObjects = formatObjects(args);

		return String.format(formatString, formattedObjects);
	}

	private Object[] formatObjects(Object... args) {
		Object[] formattedObjects = new Object[args.length];

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			Object formattedObject;
			if (arg instanceof PerunBean) {
				formattedObject = ((PerunBean) arg).serializeToString();
			} else if (arg instanceof List) {
				List<Object> list = new ArrayList<>();
				for (Object listObject : ((List) arg)) {
					list.add(formatObjects(listObject));
				}
				formattedObject = list;
			} else if (arg instanceof Set) {
				Set<Object> set = new HashSet<>();
				for (Object setObject : ((Set) arg)) {
					set.add(formatObjects(setObject));
				}
				formattedObject = set;
			} else {
				formattedObject = arg;
			}

			formattedObjects[i] = formattedObject;
		}

		return formattedObjects;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditEvent that = (AuditEvent) o;
		return Objects.equals(getMessage(), that.getMessage());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getMessage());
	}
}
