package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;

import java.util.Date;

/**
 * ServiceState is object containing information about one service propagated on one facility.
 *
 * @author Jana Cechackova
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ServiceState {

	private Service service;
	private Facility facility;
	private Task genTask;
	private Task sendTask;
	private boolean isBlockedGlobally;
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

	public Task getGenTask() {
		return genTask;
	}

	public void setGenTask(Task genTask) {
		this.genTask = genTask;
	}

	public Task getSendTask() {
		return sendTask;
	}

	public void setSendTask(Task sendTask) {
		this.sendTask = sendTask;
	}

	public boolean isBlockedGlobally() {
		return isBlockedGlobally;
	}

	public void setBlockedGlobally(boolean isBlockedGlobally) {
		this.isBlockedGlobally = isBlockedGlobally;
	}

	public boolean isBlockedOnFacility() {
		return isBlockedOnFacility;
	}

	public void setBlockedOnFacility(boolean isBlockedOnFacility) {
		this.isBlockedOnFacility = isBlockedOnFacility;
	}

	public boolean hasDestinations() {
		return hasDestinations;
	}

	public void setHasDestinations(boolean hasDestinations) {
		this.hasDestinations = hasDestinations;
	}

	/**
	 * Return ID of GEN Task if present or 0
	 *
	 * @return ID of GEN Task
	 */
	public int getGenTaskId() {
		return (genTask != null) ? genTask.getId() : 0;
	}

	/**
	 * Return ID of SEND Task if present or 0
	 *
	 * @return ID of SEND Task
	 */
	public int getSendTaskId() {
		return (sendTask != null) ? sendTask.getId() : 0;
	}

	/**
	 * Return ID of Task. If present SEND Task takes precedence.
	 * If not GEN Task is used or 0 is returned if none Task is present.
	 *
	 * @return ID of Task
	 */
	public int getTaskId() {
		return (getSendTaskId() != 0) ? getSendTaskId() : getGenTaskId();
	}

	/**
	 * Return status of GEN Task or TaskStatus.NONE if gen task is not present.
	 *
	 * @return status of GEN Task
	 */
	public Task.TaskStatus getGenStatus() {
		return (genTask != null) ? genTask.getStatus() : Task.TaskStatus.NONE;
	}

	/**
	 * Return status of SEND Task or TaskStatus.NONE if send task is not present.
	 *
	 * @return status of SEND Task
	 */
	public Task.TaskStatus getSendStatus() {
		return (sendTask != null) ? sendTask.getStatus() : Task.TaskStatus.NONE;
	}

	/**
	 * Return time, when was GEN Task scheduled or null if was never scheduled.
	 *
	 * @return time when was GEN task scheduled
	 */
	public Date getGenScheduled() {
		return (genTask != null) ? genTask.getSchedule() : null;
	}

	/**
	 * Return time, when was GEN Task scheduled or null if was never scheduled.
	 *
	 * @return time when was GEN task scheduled
	 */
	public Date getSendScheduled() {
		return (sendTask != null) ? sendTask.getSchedule() : null;
	}

	/**
	 * Return time, when GEN Task started or null if never.
	 *
	 * @return time when GEN task started
	 */
	public Date getGenStartTime() {
		return (genTask != null) ? genTask.getStartTime() : null;
	}

	/**
	 * Return time, when SEND Task started or null if never.
	 *
	 * @return time when SEND task started
	 */
	public Date getSendStartTime() {
		return (sendTask != null) ? sendTask.getStartTime() : null;
	}

	/**
	 * Return time, when GEN Task ended or null if never.
	 *
	 * @return time when GEN task ended
	 */
	public Date getGenEndTime() {
		return (genTask != null) ? genTask.getEndTime() : null;
	}

	/**
	 * Return time, when SEND Task ended or null if never.
	 *
	 * @return time when SEND task ended
	 */
	public Date getSendEndTime() {
		return (sendTask != null) ? sendTask.getEndTime() : null;
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Date getScheduled() {
		if (ExecService.ExecServiceType.GENERATE.equals(getLastScheduled())) {
			return getGenScheduled();
		} else {
			return getSendScheduled();
		}
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Date getStartTime() {
		if (ExecService.ExecServiceType.GENERATE.equals(getLastScheduled())) {
			return getGenStartTime();
		} else {
			return getSendStartTime();
		}
	}

	/**
	 * Return time when was Task on Facility scheduled if ever for the last time.
	 *
	 * @return Time when was last task scheduled.
	 */
	public Date getEndTime() {
		if (ExecService.ExecServiceType.GENERATE.equals(getLastScheduled())) {
			return getGenEndTime();
		} else {
			return getSendEndTime();
		}
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
		if (ExecService.ExecServiceType.GENERATE.equals(getLastScheduled())) {
			return getGenStatus();
		} else {
			return getSendStatus();
		}
	}

	/**
	 * Return type of Task which was last scheduled for facility.
	 * If none GEN is returned.
	 *
	 * @return Return type of Task, which was last scheduled
	 */
	public ExecService.ExecServiceType getLastScheduled() {
		if (getGenScheduled() != null && getSendScheduled() != null) {
			if (getGenScheduled().after(getSendScheduled())) {
				// gen was last scheduled
				return ExecService.ExecServiceType.GENERATE;
			} else {
				// send was last scheduled
				return ExecService.ExecServiceType.SEND;
			}
		}
		// gen was only scheduled
		if (getGenScheduled() != null) return ExecService.ExecServiceType.GENERATE;
		// send was only scheduled (but it shouldn't occur !)
		if (getSendScheduled() != null) return ExecService.ExecServiceType.SEND;
		// no task was ever scheduled - make it generate
		return ExecService.ExecServiceType.GENERATE;
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
				.append("', genTask='").append(getGenTask())
				.append("', sendTask='").append(getSendTask())
				.append("']").toString();
	}

}
