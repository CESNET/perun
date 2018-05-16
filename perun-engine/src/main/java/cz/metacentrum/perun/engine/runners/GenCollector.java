package cz.metacentrum.perun.engine.runners;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.util.concurrent.BlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERROR;

/**
 * This class represents permanently running thread, which should run in a single instance.
 *
 * It takes all done GEN Tasks (both successfully and not) from BlockingGenExecutorCompletionService
 * and report their outcome to the Dispatcher. Successful Tasks are put to the generatedTasks blocking deque
 * for later processing by SendPlanner (waiting to be sent to destinations). Failed Tasks are removed from
 * SchedulingPool (Engine).
 *
 * Expected Task status change is GENERATING -> GENERATED | GENERROR based on GenWorker outcome.
 *
 * @see cz.metacentrum.perun.engine.scheduling.impl.GenWorkerImpl
 * @see BlockingGenExecutorCompletionService
 * @see SchedulingPool#getGeneratedTasksQueue()
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GenCollector extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(GenCollector.class);

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private BlockingGenExecutorCompletionService genCompletionService;
	@Autowired
	private JMSQueueManager jmsQueueManager;

	public GenCollector() {
	}

	public GenCollector(SchedulingPool schedulingPool, BlockingGenExecutorCompletionService genCompletionService, JMSQueueManager jmsQueueManager) {
		this.schedulingPool = schedulingPool;
		this.genCompletionService = genCompletionService;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		BlockingDeque<Task> generatedTasks = schedulingPool.getGeneratedTasksQueue();
		while (!shouldStop()) {
			try {

				Task task = genCompletionService.blockingTake();
				// set ok status immediately
				task.setStatus(Task.TaskStatus.GENERATED);
				// report to Dispatcher
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getGenEndTime().getTime());
				} catch (JMSException e) {
					jmsErrorLog(task.getId(), task.getStatus());
				}

				// push Task to generated
				if (task.isPropagationForced()) {
					generatedTasks.putFirst(task);
				} else {
					generatedTasks.put(task);
				}

			} catch (InterruptedException e) {

				String errorStr = "Thread collecting generated Tasks was interrupted.";
				log.error(errorStr);
				throw new RuntimeException(errorStr, e);

			} catch (TaskExecutionException e) {

				// GEN Task failed
				Task task = e.getTask();

				if (task == null) {
					log.error("GEN Task failed, but TaskExecutionException doesn't contained Task object! Tasks will be cleaned by PropagationMaintainer#endStuckTasks()");
				} else {

					task.setStatus(GENERROR);

					try {
						jmsQueueManager.reportTaskStatus(task.getId(), GENERROR, System.currentTimeMillis());
					} catch (JMSException e1) {
						jmsErrorLog(task.getId(), task.getStatus());
					}
					try {
						schedulingPool.removeTask(task.getId());
					} catch (TaskStoreException e1) {
						log.error("[{}] Could not remove error GEN Task from SchedulingPool: {}", task.getId(), e1);
					}

				}

			} catch (Throwable ex) {
				log.error("Unexpected exception in GenCollector thread. Stuck Tasks will be cleaned by PropagationMaintainer#endStuckTasks() later. {}", ex);
			}
		}
	}

	private void jmsErrorLog(Integer id, Task.TaskStatus status) {
		log.warn("[{}] Could not send GEN status update to {} to Dispatcher.", id, status);
	}

}
