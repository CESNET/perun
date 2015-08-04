package cz.metacentrum.perun.engine.service;

/**
 * 
 * @author Michal Karm Babacek
 * @authot Michal Voců
 * @authro Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface EngineManager {

	/**
	 * Start JSM communication with perun-dispatcher component
	 */
	void startMessaging();

	/**
	 * Reload all Tasks from local DB to schedulingPool in order to start
	 * where engine last stopped.
	 */
	void loadSchedulingPool();

	/**
	 * Switch all unfinished Tasks to Error in order to re-start them.
	 */
	void switchUnfinishedTasksToERROR();

}
