package cz.metacentrum.perun.engine.service.impl;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.taskslib.model.ExecService;

import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * @author Michal Karm Babacek JavaDoc coming soon...
 * @author Michal Voců
 * @authro Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "engineManager")
public class EngineManagerImpl implements EngineManager {

	private final static Logger log = LoggerFactory.getLogger(EngineManagerImpl.class);

	@Autowired
	private JMSQueueManager jmsQueueManager;
	@Autowired
	private SchedulingPool schedulingPool;

	@Override
	public void startMessaging() {
		// jmsQueueManager.initiateConnection();
		// jmsQueueManager.registerForReceivingMessages();
		jmsQueueManager.start();
	}

	public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
		this.jmsQueueManager = jmsQueueManager;
	}

	public JMSQueueManager getJmsQueueManager() {
		return jmsQueueManager;
	}

	@Override
	@Deprecated
	public void loadSchedulingPool() {
		/*
		 * log.info("Loading last state of Tasks from local DB.");
		 * // reload all tasks from local DB into pool
		 * schedulingPool.reloadTasks(0);
		 * log.info("Loading last state of Tasks from local DB is done. Pool contains " + schedulingPool.getSize() + " tasks.");
		 */
	}

	@Override
	public void switchUnfinishedTasksToERROR() {
		log.info("I am going to switched all unfinished tasks to ERROR and finished GEN tasks which data wasn't send to ERROR as well");
		/*
		 * for (Task task :
		 * taskManager.listAllTasks(Integer.parseInt(propertiesBean
		 * .getProperty("engine.unique.id")))) {
		 * if(task.getStatus().equals(TaskStatus.DONE)) { ExecService
		 * execService = task.getExecService();
		 * 
		 * if(execService.getExecServiceType().equals(ExecServiceType.GENERATE))
		 * { task.setStatus(TaskStatus.NONE); task.setEndTime(new
		 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * } else { if (!task.getStatus().equals(TaskStatus.ERROR) &&
		 * !task.getStatus().equals(TaskStatus.NONE)) {
		 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
		 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * } }
		 */

		/* we set everything found to error to report it back to dispatcher */
		for (Task task : schedulingPool.getDoneTasks()) {
			ExecService execService = task.getExecService();

			if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
				log.debug("Setting task " + task.toString() + " to ERROR");
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
			}
		}
		for (Task task : schedulingPool.getProcessingTasks()) {
			log.debug("Setting task " + task.toString() + " to ERROR");
			schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
		}
		for (Task task : schedulingPool.getPlannedTasks()) {
			log.debug("Setting task " + task.toString() + " to ERROR");
			schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
		}
		log.info("I'm done with it.");
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}


}
