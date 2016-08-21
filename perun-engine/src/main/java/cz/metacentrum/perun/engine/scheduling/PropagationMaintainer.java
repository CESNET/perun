package cz.metacentrum.perun.engine.scheduling;

/**
 * Implements logic needed to end Tasks that get stuck in Engine for too long.
 *
 * @author Michal Karm Babacek
 */
public interface PropagationMaintainer {

	/**
	 * Switch processing tasks which started too long ago to error and report them back to dispatcher.
	 */
	void endStuckTasks();

}
