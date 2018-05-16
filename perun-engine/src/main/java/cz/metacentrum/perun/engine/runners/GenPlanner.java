package cz.metacentrum.perun.engine.runners;

import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.engine.scheduling.impl.GenWorkerImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATING;

/**
 * This class represents permanently running thread, which should run in a single instance.
 *
 * It takes all new Tasks received from Dispatcher (from newTasks blocking deque),
 * and put them to BlockingGenExecutorCompletionService. Processing waits on call of blockingSubmit()
 *
 * Expected Task status change PLANNED -> GENERATING is reported to Dispatcher.
 *
 * @see SchedulingPool#getNewTasksQueue()
 * @see BlockingGenExecutorCompletionService
 * @see GenWorkerImpl
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GenPlanner extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(GenPlanner.class);

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private BlockingGenExecutorCompletionService genCompletionService;
	@Autowired
	private JMSQueueManager jmsQueueManager;
	private File directory;

	public GenPlanner() {}

	public GenPlanner(SchedulingPool schedulingPool, BlockingGenExecutorCompletionService genCompletionService, JMSQueueManager jmsQueueManager) {
		this.schedulingPool = schedulingPool;
		this.genCompletionService = genCompletionService;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		BlockingDeque<Task> newTasks = schedulingPool.getNewTasksQueue();
		while (!shouldStop()) {
			try {
				log.debug("Getting new Task in the newTasks BlockingDeque");
				Task task = newTasks.take();
				/*
				!! Change status immediately, so it won't be picked by PropagationMaintainer#endStuckTasks()
				because we might be waiting on blockingSubmit() here !!
				*/
				task.setStatus(GENERATING);
				GenWorker worker = new GenWorkerImpl(task, directory);
				genCompletionService.blockingSubmit(worker);
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getGenStartTime().getTime());
				} catch (JMSException e) {
					log.warn("[{}] Could not send Tasks {} GEN status update: {}", task.getId(), task, e);
				}
			} catch (InterruptedException e) {

				String errorStr = "Thread executing GEN tasks was interrupted.";
				log.error(errorStr, e);
				throw new RuntimeException(errorStr, e);

			} catch (Throwable ex) {
				log.error("Unexpected exception in GenPlanner thread. Stuck Tasks will be cleaned by PropagationMaintainer#endStuckTasks() later {}.", ex);
			}
		}
	}

	@Autowired
	public void setPropertiesBean(Properties propertiesBean) {
		if (propertiesBean != null) {
			directory = new File(propertiesBean.getProperty("engine.genscript.path"));
		}
	}

}
