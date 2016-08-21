package cz.metacentrum.perun.engine.service;

/**
 * Interface of EngineManager used to start all the threads and processes necessary for Engine
 * to propagate data from Perun to end services.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface EngineManager {

	/**
	 * Start JSM communication with perun-dispatcher component
	 */
	void startMessaging();

	/**
	 * Starts all threads responsible for moving and executing Tasks
	 */
	void startRunnerThreads();

	/**
	 * Gives indication to all runner Threads that they should stop
	 */
	void stopRunnerThreads();
}
