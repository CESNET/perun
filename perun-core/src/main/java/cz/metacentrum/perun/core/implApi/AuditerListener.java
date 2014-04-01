package cz.metacentrum.perun.core.implApi;

/**
 * This interface represdents classes which can be registered as listeners to Auditer.
 *
 * @author Slavek Licehammer
 * @author
 */
@Deprecated
public interface AuditerListener {

	/**
	 * This method process received message.
	 * Auditer sends messages via this method.
	 *
	 *
	 * @param message received message (from auditer)
	 */
	void notifyWith(String message);
}
