package cz.metacentrum.perun.engine.scheduling.impl;

/**
 * Simple set of Worker's statuses.
 * <p/>
 * WAITING      Worker is waiting in a queue to be executed.
 * RUNNING      Worker is actually running.
 * FINISHED     Worker is finished and saved for statistical information.
 *
 * @author Jana Cechackova
 * @version 3
 */
public enum WorkerStatus {
	WAITING, RUNNING, FINISHED
}