package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;

import java.util.concurrent.ConcurrentMap;
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

	/**
	 * Return map of currently running Tasks. This map must NEVER be modified outside BlockingCompletionService !!
	 * Its only for inspection reasons !!
	 *
	 * @return map of running Futures and Tasks
	 */
	ConcurrentMap<Future<V>, V> getRunningTasks();

	/**
	 * Remove Future from running tasks in completion service and release blocking semaphore.
	 * This should be called only if we are sure, that Future is either stuck (running for more than
	 * rescheduleTime) or GenCollector / SendCollector is not running and finished and failed Tasks are kept
	 * in completion service.
	 *
	 * @param future to be removed
	 */
	void removeStuckTask(Future<V> future);

}
