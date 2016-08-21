package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;

import java.util.concurrent.Future;

/**
 * This class wraps classic CompletionService, providing extra methods with blocking behaviour.
 * @see java.util.concurrent.CompletionService
 */
public interface BlockingCompletionService<V>{

	/**
	 * Tries to submit the given worker for execution, blocking if the CompletionServices ThreadPool is full.
	 * @param taskWorker The EngineWorker instance which will be executed
	 * @return Returns Future holding the executing Task.
	 * @throws InterruptedException Thrown if the Thread was interrupted while waiting for a place in ThreadPool.
	 */
	Future<V> blockingSubmit(EngineWorker<V> taskWorker) throws InterruptedException;

	V blockingTake() throws InterruptedException, TaskExecutionException;
}
