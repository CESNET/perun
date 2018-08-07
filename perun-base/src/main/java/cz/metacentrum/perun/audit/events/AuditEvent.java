package cz.metacentrum.perun.audit.events;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
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
}
