package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.model.Task;

import java.time.LocalDateTime;

/**
 * ServiceState is object containing information about one service propagated on one facility.
 *
 * @author Jana Cechackova
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ServiceState {

	private Service service;
	private Facility facility;
	private Task task;
	private boolean isBlockedOnFacility;
	private boolean hasDestinations;

	/**
	 * Create new instance of ServiceState.
	 */
	public ServiceState() {
	}

	public ServiceState(Service service, Facility facility) {
		this.service = service;
		this.facility = facility;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public boolean isBlockedGlobally() {
		return (service != null) && !service.isEnabled();
	}

	public boolean isBlockedOnFacility() {
		return isBlockedOnFacility;
	}

	public void setBlockedOnFacility(boolean isBlockedOnFacility) {
		this.isBlockedOnFacility = isBlockedOnFacility;
	}

	public boolean getHasDestinations() {
		return hasDestinations;
	}

	public void setHasDestinations(boolean hasDestinations) {
		this.hasDestinations = hasDestinations;
	}

	/**
	 * Return ID of Task. If present SEND Task takes precedence.
	 * If not GEN Task is used or 0 is returned if none Task is present.
	 *
	 * @return ID of Task
	 */
	public int getTaskId() {
		return (task != null) ? task.getId() : 0;
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Long getScheduled() {
		return (task != null) ? task.getScheduleAsLong() : null;
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Long getStartTime() {
		return (task != null) ? task.getStartTimeAsLong() : null;
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Long getEndTime() {
		return (task != null) ? task.getEndTimeAsLong() : null;
	}

	/**
	 * Return status of service propagation (task status) based on current tasks states.
	 * Method compares "scheduled" dates of tasks in order to determine, which is more relevant.
	 *
	 * If can't determine, TaskStatus.NONE is returned.
	 *
	 * @return TaskStatus of service on facility
	 */
	public Task.TaskStatus getStatus() {
		return (task != null) ? task.getStatus() : Task.TaskStatus.WAITING;
	}

	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getBeanName()).append(":[")
				.append("service='").append(getService().toString())
				.append("', facility='").append(getFacility().toString())
				.append("', task='").append(getTask())
				.append("']").toString();
	}

}
