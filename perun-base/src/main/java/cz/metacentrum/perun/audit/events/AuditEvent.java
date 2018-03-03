package cz.metacentrum.perun.audit.events;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface AuditEvent {

	/**
	 * Get message that should be logged.
	 *
	 * @return message
	 */
	String getMessage();
}
