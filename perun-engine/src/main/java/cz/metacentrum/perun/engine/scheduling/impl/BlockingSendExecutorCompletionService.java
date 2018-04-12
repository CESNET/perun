package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.BlockingBoundedMap;
import cz.metacentrum.perun.engine.scheduling.BlockingCompletionService;
import cz.metacentrum.perun.engine.scheduling.EngineWorker;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.taskslib.model.SendTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.*;

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
 */
public class BlockingSendExecutorCompletionService implements BlockingCompletionService<SendTask> {

	private final static Logger log = LoggerFactory.getLogger(BlockingSendExecutorCompletionService.class);
	private CompletionService<SendTask> completionService;
	@Autowired
	@Qualifier("sendingSendTasks")
	private BlockingBoundedMap<Pair<Integer, Destination>, SendTask> executingTasks;
	private int limit;

	/**
	 * Create new blocking CompletionService for SEND Tasks with specified limit
	 *
	 * @param limit Limit for processing SEND Tasks
	 */
	public BlockingSendExecutorCompletionService(int limit) {
		this.limit = limit;
		completionService = new ExecutorCompletionService<SendTask>(Executors.newFixedThreadPool(limit), new LinkedBlockingQueue<Future<SendTask>>());
	}

	@Override
	public Future<SendTask> blockingSubmit(EngineWorker<SendTask> taskWorker) throws InterruptedException {
		SendWorker sendWorker = (SendWorker) taskWorker;
		// FIXME - actual debug output differs, since object values are serialized later and might be modified by another thread
		log.debug("Executing SEND tasks before submit: {}/{}", executingTasks.keySet().size(), limit);
		executingTasks.blockingPut(sendWorker.getSendTask().getId(), sendWorker.getSendTask());
		return completionService.submit(sendWorker);
	}

	@Override
	public SendTask blockingTake() throws InterruptedException, TaskExecutionException {
		// FIXME - actual debug output differs, since object values are serialized later and might be modified by another thread
		log.debug("Executing SEND tasks before take: {}/{}", executingTasks.keySet().size(), limit);
		Future<SendTask> taskFuture = completionService.take();
		try {
			SendTask taskResult = taskFuture.get();
			SendTask removed = executingTasks.remove(taskResult.getId());
			if (removed == null) {
				log.warn("Finished {} was not present in executingTasks map. Probably cleaned by endStuckTasks().", taskResult);
			}
			return taskResult;
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause.getClass().equals(TaskExecutionException.class)) {
				TaskExecutionException castedCause = (TaskExecutionException) cause;
				executingTasks.remove(new Pair<>(castedCause.getTask().getId(), castedCause.getDestination()));
				throw castedCause;
			} else {
				String errorMsg = "Unexpected exception occurred during SendTask execution";
				log.error(errorMsg, e);
				throw new RuntimeException(errorMsg, e);
			}
		}
		// TODO - catch cancellation exception ?
	}
}
