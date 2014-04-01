package cz.metacentrum.perun.webgui.json;


/**
 * Interface for status setting callbacks.
 * You must pass the object ID in the constructor.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface JsonStatusSetCallback {

	/**
	 * @param newStatus status to be set
	 */
	void setStatus(String newStatus);

	/**
	 * Sets the new events for the query
	 * @param events
	 */
	void setEvents(JsonCallbackEvents events);
}
