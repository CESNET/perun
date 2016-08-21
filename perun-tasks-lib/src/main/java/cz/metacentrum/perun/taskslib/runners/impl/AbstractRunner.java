package cz.metacentrum.perun.taskslib.runners.impl;

import cz.metacentrum.perun.taskslib.runners.Runner;

/**
 * Extensible stub for all Runners (periodic threads used in dispatcher).
 * It handles "stop" flag, so thread should know, when to stop and can be
 * stopped from outside.
 *
 * @author David Šarman
 */
public abstract class AbstractRunner implements Runner {

	volatile private boolean stop = false;

	@Override
	public boolean shouldStop() {
		return stop;
	}

	@Override
	public void stop() {
		stop = true;
	}

}
