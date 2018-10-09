package cz.metacentrum.perun.core.implApi;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface AuditerPublisher {

	/**
	 * Checks if some new messages has been audited and if so
	 * publish them.
	 */
	void checkNewMessages();
}
