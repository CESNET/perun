package cz.metacentrum.perun.taskslib.runners;

/**
 * Interface for all Runners (periodic threads used in dispatcher/engine).
 * It handles "stop" flag, so thread should know, when to stop and can be
 * stopped from outside.
 *
 * @author David Šarman
 */
public interface Runner extends Runnable {

	/**
	 * Return TRUE if Runner thread processing should stop.
	 *
	 * @return TRUE if should stop / FALSE otherwise
	 */
	public boolean shouldStop();

	/**
	 * Stop Runner thread (it's not interrupt - thread stops by own implementation in run() method).
	 */
	public void stop();

}
