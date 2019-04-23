package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.BlockingCompletionService;
import cz.metacentrum.perun.engine.scheduling.EngineWorker;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * Implementation of BlockingCompletionService<Task> for generating Tasks in Engine.
 * It provides blocking methods and size limit to javas CompletionService, which itself run GenWorkers.
 * Tasks are managed by separate threads GenPlanner and GenCollector.
 *
 * @see BlockingCompletionService
 * @see GenWorker
 * @see GenWorkerImpl
 * @see cz.metacentrum.perun.engine.runners.GenPlanner
 * @see cz.metacentrum.perun.engine.runners.GenCollector
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class BlockingGenExecutorCompletionService implements BlockingCompletionService<Task> {

	private final static Logger log = LoggerFactory.getLogger(BlockingGenExecutorCompletionService.class);
	private CompletionService<Task> completionService;
	private ConcurrentMap<Future<Task>, Task> executingGenTasks = new ConcurrentHashMap<>();
	/**
	 * Provide blocking-waiting behavior to GEN Tasks, which are not started, until semaphore is acquired.
	 * When job is cancelled or done, semaphore is released. Semaphore shares limit for concurrently running
	 * GEN Tasks with javas ExecutorCompletionService.
	 */
	private Semaphore semaphore;

	/**
	 * Create new blocking CompletionService for GEN Tasks with specified limit
	 *
	 * @param limit Limit for processing GEN Tasks
	 */
	public BlockingGenExecutorCompletionService(int limit) {
		completionService = new ExecutorCompletionService<Task>(Executors.newFixedThreadPool(limit), new LinkedBlockingQueue<Future<Task>>());
		this.semaphore = new Semaphore(limit);
	}

	@Override
	public Future<Task> blockingSubmit(EngineWorker<Task> taskWorker) throws InterruptedException {
		semaphore.acquire();
		Future<Task> future = null;
		try {
			GenWorker genWorker = (GenWorker) taskWorker;
			// We must have start time before adding Task to executingGenTasks
			genWorker.getTask().setGenStartTime(LocalDateTime.now());
			future = completionService.submit(genWorker);
			executingGenTasks.put(future, genWorker.getTask());
		} catch (Exception ex) {
			// release semaphore if submission fails
			semaphore.release();
			throw ex;
		}
		return future;
	}

	@Override
	public Task blockingTake() throws InterruptedException, TaskExecutionException {

		Future<Task> taskFuture = completionService.take();

		try {

			// .get() throws CancellationException if Task processing was cancelled from outside
			Task task = taskFuture.get();
			removeTaskFuture(taskFuture);
			return task;

		} catch (ExecutionException e) {

			Task task = executingGenTasks.get(taskFuture);
			removeTaskFuture(taskFuture);

			Throwable cause = e.getCause();
			if (cause instanceof TaskExecutionException) {
				// GEN Task failed and related Task and results are part of this exception
				throw (TaskExecutionException)cause;
			} else {

				// Unexpected exception during processing, pass stored Task if possible
				if (task == null) {
					log.error("We couldn't get Task for failed Future<Task>: {}", e);
					throw new RuntimeException("We couldn't get Task for failed Future<Task>", e);
				}

				throw new TaskExecutionException(task, "Unexpected exception during GEN Task processing.", e);
			}

		} catch (CancellationException ex) {

			// processing was cancelled
			Task removedTask = executingGenTasks.get(taskFuture);
			removeTaskFuture(taskFuture);
			if (removedTask == null) {
				log.error("Somebody manually removed Future<Task> from executingGenTasks or Task was null: {}", ex);
				throw ex; // we can't do anything about it
			}

			// make sure GenCollector always get related Task
			throw new TaskExecutionException(removedTask, "Processing of Task was cancelled before completion.");

		}

	}

	@Override
	public ConcurrentMap<Future<Task>, Task> getRunningTasks() {
		return executingGenTasks;
	}

	@Override
	public void removeStuckTask(Future<Task> future) {
		removeTaskFuture(future);
	}

	/**
	 * Remove Future<Task> from executingGenTasks and release semaphore.
	 *
	 * @param future to be removed
	 */
	private void removeTaskFuture(Future<Task> future) {
		Task removedTask = executingGenTasks.remove(future);
		if (removedTask != null) {
			// release semaphore only if future was really in a map
			// because it could change during processing
			semaphore.release();
		}
	}

}
