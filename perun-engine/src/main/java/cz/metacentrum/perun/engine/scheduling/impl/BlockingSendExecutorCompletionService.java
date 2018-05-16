package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.BlockingCompletionService;
import cz.metacentrum.perun.engine.scheduling.EngineWorker;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.taskslib.model.SendTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENDING;

/**
 * Implementation of BlockingCompletionService<SendTask> for sending Tasks in Engine.
 * (SendTask is inner representation of <Task,Destination>)
 * It provides blocking methods and size limit to javas CompletionService, which itself run SendWorkers.
 * Tasks are managed by separate threads SendPlanner and SendCollector.
 *
 * @see BlockingCompletionService
 * @see SendWorker
 * @see SendWorkerImpl
 * @see cz.metacentrum.perun.engine.runners.SendPlanner
 * @see cz.metacentrum.perun.engine.runners.SendCollector
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class BlockingSendExecutorCompletionService implements BlockingCompletionService<SendTask> {

	private final static Logger log = LoggerFactory.getLogger(BlockingSendExecutorCompletionService.class);
	private CompletionService<SendTask> completionService;
	private ConcurrentMap<Future<SendTask>, SendTask> executingSendTasks = new ConcurrentHashMap<>();
	/**
	 * Provide blocking-waiting behavior to SEND Tasks, which are not started, until semaphore is acquired.
	 * When job is cancelled or done, semaphore is released. Semaphore shares limit for concurrently running
	 * SEND Tasks with javas ExecutorCompletionService.
	 */
	private Semaphore semaphore;

	/**
	 * Create new blocking CompletionService for SEND Tasks with specified limit
	 *
	 * @param limit Limit for processing SEND Tasks
	 */
	public BlockingSendExecutorCompletionService(int limit) {
		this.completionService = new ExecutorCompletionService<SendTask>(Executors.newFixedThreadPool(limit), new LinkedBlockingQueue<Future<SendTask>>());
		this.semaphore = new Semaphore(limit);
	}

	@Override
	public Future<SendTask> blockingSubmit(EngineWorker<SendTask> taskWorker) throws InterruptedException {
		semaphore.acquire();
		Future<SendTask> future = null;
		try {
			SendWorker sendWorker = (SendWorker) taskWorker;
			sendWorker.getSendTask().setStartTime(new Date(System.currentTimeMillis()));
			sendWorker.getSendTask().setStatus(SENDING);
			future = completionService.submit(sendWorker);
			executingSendTasks.put(future, sendWorker.getSendTask());
		} catch (Exception ex) {
			semaphore.release();
			throw ex;
		}
		return future;
	}

	@Override
	public SendTask blockingTake() throws InterruptedException, TaskExecutionException {

		Future<SendTask> taskFuture = completionService.take();

		try {
			// .get() throws CancellationException if Task processing was cancelled from outside
			SendTask sendTask = taskFuture.get();
			removeTaskFuture(taskFuture);
			return sendTask;

		} catch (ExecutionException e) {

			SendTask sendTask = executingSendTasks.get(taskFuture);
			removeTaskFuture(taskFuture);

			Throwable cause = e.getCause();
			if (cause instanceof TaskExecutionException) {
				// SEND Task failed and related Task and results are part of this exception
				throw (TaskExecutionException)cause;
			} else {

				// Unexpected exception during processing, pass stored SendTask if possible
				if (sendTask == null) {
					log.error("We couldn't get SendTask for failed Future<SendTask>: {}", e);
					throw new RuntimeException("We couldn't get SendTask for failed Future<Task>", e);
				}

				throw new TaskExecutionException(sendTask.getTask(), sendTask.getDestination(), "Unexpected exception during SEND Task processing.", e);
			}

		} catch (CancellationException ex) {

			// processing was cancelled
			SendTask removedSendTask = executingSendTasks.get(taskFuture);
			removeTaskFuture(taskFuture);
			if (removedSendTask == null) {
				log.error("Somebody manually removed Future<SendTask> from executingSendTasks or SendTask was null: {}", ex);
				throw ex; // we can't do anything about it
			}

			// make sure SendCollector always get related Task
			throw new TaskExecutionException(removedSendTask.getTask(), removedSendTask.getDestination(), "Processing of Task was cancelled before completion.");

		}

	}

	@Override
	public ConcurrentMap<Future<SendTask>, SendTask> getRunningTasks() {
		return executingSendTasks;
	}

	@Override
	public void removeStuckTask(Future<SendTask> future) {
		removeTaskFuture(future);
	}

	/**
	 * Remove Future<SendTask> from executingSendTasks and release semaphore.
	 *
	 * @param future to be removed
	 */
	private void removeTaskFuture(Future<SendTask> future) {
		SendTask removedTask = executingSendTasks.remove(future);
		if (removedTask != null) {
			// release semaphore only if future was really in a map
			// because it could change during processing
			semaphore.release();
		}
	}

}
