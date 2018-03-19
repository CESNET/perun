package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;

import java.util.concurrent.Future;

/**
 * This class wraps classic CompletionService, providing extra methods with blocking behaviour for Engine purpose
 *
 * @see java.util.concurrent.CompletionService
 *
 * @see cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService
 * @see cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService
 *
 * Makes any thread wait until there is space for starting new worker and wait to get completed/error worker.
 *
 * @author David Šarman
 */
public interface BlockingCompletionService<V>{

	/**
	 * Tries to submit the given worker for execution, blocking if the CompletionServices ThreadPool is full
	 * (backed by BlockingBoundedMap of currently executing tasks)
	 *
	 * @see BlockingBoundedMap
	 *
	 * @param taskWorker The EngineWorker instance which will be executed
	 * @return Returns Future holding the executing Task.
	 * @throws InterruptedException Thrown if the Thread was interrupted while waiting for a place in ThreadPool.
	 */
	Future<V> blockingSubmit(EngineWorker<V> taskWorker) throws InterruptedException;

	/**
	 * Tries to get any completed worker, blocking if non worker is done/or error.
	 * (backed by BlockingBoundedMap of currently executing tasks)
	 *
	 * @return Gen or Send worker (EngineWorker)
	 * @throws InterruptedException if canceled
	 * @throws TaskExecutionException when worker failed
	 */
	V blockingTake() throws InterruptedException, TaskExecutionException;

}
