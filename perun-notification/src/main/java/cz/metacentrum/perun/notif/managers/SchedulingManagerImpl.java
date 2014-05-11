package cz.metacentrum.perun.notif.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Scheduling manager for firing sending emails from db to users. Main method is
 * doNotification.
 *
 * @author tomas.tunkl
 *
 */
@Service("schedulingManager")
public class SchedulingManagerImpl {

	private static final Logger logger = LoggerFactory.getLogger(SchedulingManagerImpl.class);

	private static Boolean doNotif = false;

	@Autowired
	private PerunNotifPoolMessageManager perunNotifPoolMessageManager;

	/**
	 * Method starts processing poolMessages from db and starts sending
	 * notifications to users.
	 */
	public void doNotification() {
		logger.info("Starting doNotification");
		synchronized (doNotif) {
			if (doNotif) {
				logger.warn("DoNotification is still running.");
				return;
			} else {
				doNotif = true;
			}
		}

		logger.info("Getting poolMessages from db.");

		try {
			perunNotifPoolMessageManager.processPerunNotifPoolMessagesFromDb();
		} catch (Exception ex) {
			logger.error("Exception thrown during processing poolMessages:", ex);
		}

		synchronized (doNotif) {
			doNotif = false;

		}
	}
}
