package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerFactory;
import cz.metacentrum.perun.dispatcher.scheduling.impl.TaskScheduled;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import static cz.metacentrum.perun.dispatcher.scheduling.impl.TaskScheduled.*;

/**
 * Schedule Tasks, which are WAITING in DelayQueue and send them to Engine and switch it to PLANNED.
 *
 * @author Michal Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "taskScheduler")
public class TaskScheduler extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(TaskScheduler.class);
	private PerunSession perunSession;

	private SchedulingPool schedulingPool;
	private Perun perun;
	private Properties dispatcherProperties;
	private EngineMessageProducerFactory engineMessageProducerFactory;
	private DelayQueue<TaskSchedule> waitingTasksQueue;
	private DelayQueue<TaskSchedule> waitingForcedTasksQueue;
	private TasksManagerBl tasksManagerBl;

	// ----- setters -------------------------------------

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public Perun getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public EngineMessageProducerFactory getEngineMessageProducerPool() {
		return engineMessageProducerFactory;
	}

	@Autowired
	public void setEngineMessageProducerPool(EngineMessageProducerFactory engineMessageProducerPool) {
		this.engineMessageProducerFactory = engineMessageProducerPool;
	}

	public DelayQueue<TaskSchedule> getWaitingTasksQueue() {
		return waitingTasksQueue;
	}

	@Autowired
	public void setWaitingTasksQueue(DelayQueue<TaskSchedule> waitingTasksQueue) {
		this.waitingTasksQueue = waitingTasksQueue;
	}

	public DelayQueue<TaskSchedule> getWaitingForcedTasksQueue() {
		return waitingForcedTasksQueue;
	}

	@Autowired
	public void setWaitingForcedTasksQueue(DelayQueue<TaskSchedule> waitingForcedTasksQueue) {
		this.waitingForcedTasksQueue = waitingForcedTasksQueue;
	}

	public TasksManagerBl getTasksManagerBl() {
		return tasksManagerBl;
	}

	@Autowired
	public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
		this.tasksManagerBl = tasksManagerBl;
	}

	// ----- methods -------------------------------------


	/**
	 * This method runs in separate thread perpetually trying to take tasks from delay queue, blocking if none are available.
	 * If there is Task ready, we check if it source was updated. If it was, we put the task back to the queue (This
	 * can happen only limited number of times). If on the other hand it was not updated we perform additional checks using
	 * method sendToEngine.
	 */
	@Override
	public void run() {
		try {
			initPerunSession();
		} catch (InternalErrorException e1) {
			String message = "Dispatcher was unable to initialize Perun session.";
			log.error(message, e1);
			throw new RuntimeException(message, e1);
		}
		log.debug("Pool contains {} tasks in total", schedulingPool.getSize());
		TaskSchedule schedule;
		while (!shouldStop()) {
			try {
				if (tasksManagerBl.isSuspendedTasksPropagation()) {
					// do not continue sending tasks to the engine until propagation is set to resume
					waitForResumingPropagation();
				}
				schedule = getWaitingTaskSchedule();
			} catch (InterruptedException e) {
				String message = "Thread was interrupted, cannot continue.";
				log.error(message, e);
				throw new RuntimeException(message, e);
			}
			Task task = schedule.getTask();
			if (task.isSourceUpdated() && schedule.getDelayCount() > 0 && !task.isPropagationForced()) {
				// source data changed before sending, wait for more changes to come -> reschedule
				log.warn("[{}] Task was not allowed to be sent to Engine now: {}.", task.getId(), task);
				schedulingPool.scheduleTask(task, schedule.getDelayCount() - 1);
			} else {
				// send it to engine
				TaskScheduled reason = sendToEngine(task);
				switch (reason) {
					case QUEUE_ERROR:
						log.warn("[{}] Task dispatcherQueue could not be set, so it is rescheduled: {}.", task.getId(), task);
						schedulingPool.scheduleTask(task, -1);
						break;
					case DENIED:
						// Task is lost from waiting queue, since somebody blocked service on facility, all destinations or globally
						log.info("[{}] Execution was denied for Task before sending to Engine: {}.", task.getId(), task);
						break;
					case ERROR:
						log.error("[{}] Unexpected error when scheduling Task, so it is rescheduled: {}.", task.getId(), task);
						schedulingPool.scheduleTask(task, -1);
						break;
					case SUCCESS:
						log.info("[{}] Task was successfully sent to Engine: {}.", task.getId(), task);
						break;
					case DB_ERROR:
						// Task is lost from waiting queue, will be cleared from pool by propagation maintainer
						log.warn("[{}] Facility, Service or Destination could not be found in DB for Task {}.", task.getId(), task);
						break;
				}
				// update task status in DB
				tasksManagerBl.updateTask(perunSession, task);
			}
		}
		log.debug("TaskScheduler has stopped.");
	}

	/**
	 * Internal method which chooses next Task that will be processed, we try to take forced Task first,
	 * and if none is available, then we wait for a normal Task for a few seconds.
	 *
	 * @return Once one of the Queues returns non null TaskSchedule, we return it.
	 * @throws InterruptedException When blocking queue polling was interrupted.
	 */
	private TaskSchedule getWaitingTaskSchedule() throws InterruptedException {
		TaskSchedule taskSchedule = null;
		while (!shouldStop()) {
			log.debug(schedulingPool.getReport());
			log.debug("WaitingTasksQueue has {} normal Tasks and {} forced Tasks.", waitingTasksQueue.size(), waitingForcedTasksQueue.size());
			taskSchedule = waitingForcedTasksQueue.poll();
			if (taskSchedule == null) {
				taskSchedule = waitingTasksQueue.poll(10, TimeUnit.SECONDS);
			}
			if (taskSchedule != null) {
				break;
			}
		}
		log.trace("[{}] Returning Task schedule {}.", taskSchedule.getTask().getId(), taskSchedule);
		return taskSchedule;
	}

	/**
	 * Method waiting for propagation of tasks to the engine to be resumed.
	 * Called when propagation was suspended.
	 * @throws InterruptedException Waiting thread was interrupted.
	 */
	private void waitForResumingPropagation() throws InterruptedException {
		int sleepTime = 10000;
		while (tasksManagerBl.isSuspendedTasksPropagation()) {
			log.debug("Propagation of tasks is suspended.");
			log.debug(schedulingPool.getReport());
			log.debug("WaitingTasksQueue has {} normal Tasks and {} forced Tasks.", waitingTasksQueue.size(), waitingForcedTasksQueue.size());
			Thread.sleep(sleepTime);
		}
		log.debug("Propagation of tasks is resumed.");
	}

	/**
	 * Send Task to Engine. Called when it waited long enough in a waiting queue (listening for other changes).
	 *
	 * @param task Task to be send to Engine
	 * @return Resulting state if Task was sent or denied or any error happened.
	 */
	protected TaskScheduled sendToEngine(Task task) {

		Service service = task.getService();
		Facility facility = task.getFacility();

		try {
			initPerunSession();
			service = perun.getServicesManager().getServiceById(perunSession, service.getId());
			facility = perun.getFacilitiesManager().getFacilityById(perunSession, facility.getId());
			task.setService(service);
			task.setFacility(facility);
		} catch (ServiceNotExistsException e) {
			log.error("[{}] Service for task does not exist...", task.getId());
			task.setEndTime(LocalDateTime.now());
			task.setStatus(TaskStatus.ERROR);
			return DB_ERROR;
		} catch (FacilityNotExistsException e) {
			log.error("[{}] Facility for task does not exist...", task.getId());
			task.setEndTime(LocalDateTime.now());
			task.setStatus(TaskStatus.ERROR);
			return DB_ERROR;
		} catch (PrivilegeException e) {
			log.error("[{}] Privilege error accessing the database: {}", task.getId(), e.getMessage());
			task.setEndTime(LocalDateTime.now());
			task.setStatus(TaskStatus.ERROR);
			return DB_ERROR;
		} catch (InternalErrorException e) {
			log.error("[{}] Internal error: {}", task.getId(), e.getMessage());
			task.setEndTime(LocalDateTime.now());
			task.setStatus(TaskStatus.ERROR);
			return DB_ERROR;
		}

		EngineMessageProducer engineMessageProducer = engineMessageProducerFactory.getProducer();

		log.debug("[{}] Scheduling {}.", task.getId(), task);

		if (engineMessageProducer != null) {
			log.debug("[{}] Assigned queue {} to task.", task.getId(), engineMessageProducer.getQueueName());
		} else {
			log.error("[{}] There are no engines registered.", task.getId());
			return QUEUE_ERROR;
		}

		if (service.isEnabled()) {
			log.debug("[{}] Service {} is enabled globally.", task.getId(), service.getId());
		} else {
			log.debug("[{}] Service {} is disabled globally.", task.getId(), service.getId());
			return DENIED;
		}

		try {
			if (!((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(service, facility)) {
				log.debug("[{}] Service {} is allowed on Facility {}.", task.getId(), service.getId(), facility.getId());
			} else {
				log.debug("[{}] Service {} is blocked on Facility {}.", task.getId(), service.getId(), facility.getId());
				return DENIED;
			}
		} catch (Exception e) {
			log.error("[{}] Error getting disabled status for Service, task will not run now: {}.", task.getId(), e);
			return ERROR;
		}

		// task|[task_id][is_forced][exec_service_id][facility]|[destination_list]|[dependency_list]
		// - the task|[engine_id] part is added by dispatcherQueue
		List<Destination> destinations = task.getDestinations();
		if (task.isSourceUpdated() || destinations == null || destinations.isEmpty()) {
			log.trace("[{}] No destinations for task, trying to query the database.", task.getId());
			try {
				initPerunSession();
				destinations = perun.getServicesManager().getDestinations(perunSession, task.getService(), task.getFacility());
			} catch (ServiceNotExistsException e) {
				log.error("[{}] No destinations found for task. Service not exists...", task.getId());
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				return DB_ERROR;
			} catch (FacilityNotExistsException e) {
				log.error("[{}] No destinations found for task. Facility for task does not exist...", task.getId());
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				return DB_ERROR;
			} catch (PrivilegeException e) {
				log.error("[{}] No destinations found for task. Privilege error accessing the database: {}", task.getId(), e.getMessage());
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				return DB_ERROR;
			} catch (InternalErrorException e) {
				log.error("[{}] No destinations found for task. Internal error: {}", task.getId(), e.getMessage());
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				return DB_ERROR;
			}
		}

		log.debug("[{}] Fetched destinations: {}",  task.getId(), (destinations == null) ? "[]" : destinations.toString());

		if (destinations != null && !destinations.isEmpty()) {
			Iterator<Destination> iter = destinations.iterator();
			while (iter.hasNext()) {
				Destination dest = iter.next();
				if (((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(service, dest.getId())) {

					// create fake task result to let admin know about the block
					TaskResult result = new TaskResult();
					result.setTaskId(task.getId());
					result.setDestination(dest);
					result.setDestinationId(dest.getId());
					result.setService(service);
					result.setStandardMessage("");
					result.setErrorMessage("Destination is blocked in Perun.");
					result.setReturnCode(1);
					result.setId(0);
					result.setTimestamp(new Date(System.currentTimeMillis()));
					result.setStatus(TaskResult.TaskResultStatus.DENIED);
					try {
						schedulingPool.onTaskDestinationComplete(result);
					} catch (Exception ex) {
						log.warn("Couldn't store fake TaskResult about blocked destination.");
					}

					// actually remove from destinations sent to engine
					iter.remove();
					log.debug("[{}] Removed blocked destination: {}",  task.getId(), dest.toString());

				}
			}
			if (destinations.isEmpty()) {
				// All service destinations were blocked -> Task is denied to be sent to engine just like
				// when service is blocked globally in Perun or on facility as a whole.
				return DENIED;
			}
		} else {
			log.debug("[{}] No destination found for task: {}.", task.getId(), task);
			task.setStatus(TaskStatus.ERROR);
			return ERROR;
		}

		task.setDestinations(destinations);

		// construct JMS message for Engine

		StringBuilder destinations_s = new StringBuilder("Destinations [");
		if (destinations != null) {
			for (Destination destination : destinations) {
				destinations_s.append(destination.serializeToString()).append(", ");
			}
		}
		destinations_s.append("]");

		// send message async

		engineMessageProducer.sendMessage("[" + task.getId() + "]["
				+ task.isPropagationForced() + "]|["
				+ fixStringSeparators(task.getService().serializeToString()) + "]|["
				+ fixStringSeparators(task.getFacility().serializeToString()) + "]|["
				+ fixStringSeparators(destinations_s.toString()) + "]");

		// modify task status and reset forced flag

		task.setSentToEngine(LocalDateTime.now());
		task.setStatus(Task.TaskStatus.PLANNED);
		task.setPropagationForced(false);
		return SUCCESS;

	}

	/**
	 * Encode string to base64 when it contains any message divider character "|".
	 *
	 * @param data Data to be checked
	 * @return Base64 encoded string if needed or original string
	 */
	private String fixStringSeparators(String data) {
		if (data.contains("|")) {
			return new String(Base64.encodeBase64(data.getBytes()));
		} else {
			return data;
		}
	}

	protected void initPerunSession() {
		if (perunSession == null) {
			perunSession = perun.getPerunSession(new PerunPrincipal(
					dispatcherProperties.getProperty("perun.principal.name"),
					dispatcherProperties.getProperty("perun.principal.extSourceName"),
					dispatcherProperties.getProperty("perun.principal.extSourceType")),
					new PerunClient());
		}
	}

}
