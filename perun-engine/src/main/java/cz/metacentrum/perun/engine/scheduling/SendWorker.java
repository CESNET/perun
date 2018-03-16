package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.taskslib.model.SendTask;

/**
 * Worker used to execute Tasks SEND script.
 *
 * @author David Šarman
 */
public interface SendWorker extends EngineWorker<SendTask> {

	@Override
	SendTask call() throws Exception;

	SendTask getSendTask();

}
