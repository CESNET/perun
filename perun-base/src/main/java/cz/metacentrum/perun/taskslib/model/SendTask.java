package cz.metacentrum.perun.taskslib.model;


import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Pair;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Sending part of original Task. Task is usually split between multiple SendTasks,
 * each of them associated with one Destination. Results are then grouped and used
 * to determine state of a whole Task.
 *
 * @see cz.metacentrum.perun.taskslib.model.Task
 * @see cz.metacentrum.perun.core.api.Destination
 */
public class SendTask implements Serializable {

	private static final long serialVersionUID = 4795659061486919871L;

	private Pair<Integer, Destination> id;
	private SendTaskStatus status;
	private Date startTime;
	private Date endTime;
	private Task task;
	private Destination destination;
	private Integer returnCode;
	private String stdout;
	private String stderr;

	/**
	 * Create new SendTask for Task and Destination
	 *
	 * @param task
	 * @param destination
	 */
	public SendTask(Task task, Destination destination) {
		this.task = task;
		this.destination = destination;
		setId();
	}

	/**
	 * Get time when sending of Task started.
	 *
	 * @return Time when sending started
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Set time when sending of Task started.
	 *
	 * @param startTime Time when sending started
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Get time when sending of Task ended.
	 *
	 * @return Time when sending ended
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Set time when sending of Task ended.
	 *
	 * @param endTime Time when sending ended
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Get Task associated with this SendTask
	 *
	 * @return Task associated with this SendTask
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Set Task associated with this SendTask
	 *
	 * @param task Task associated with this SendTask
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Get Destination associated with this SendTask
	 *
	 * @return Destination associated with this SendTask
	 */
	public Destination getDestination() {
		return destination;
	}

	/**
	 * Set Destination associated with this SendTask
	 *
	 * @param destination Destination associated with this SendTask
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Get Status of SendTask
	 *
	 * @return Status of SendTask
	 */
	public SendTaskStatus getStatus() {
		return status;
	}

	/**
	 * Set Status of SendTask
	 *
	 * @param status Status of SendTask
	 */
	public void setStatus(SendTaskStatus status) {
		this.status = status;
	}

	/**
	 * Get return code of sending script
	 *
	 * @return Return code of sending script
	 */
	public Integer getReturnCode() {
		return returnCode;
	}

	/**
	 * Set return code of sending script
	 *
	 * @param returnCode Return code of sending script
	 */
	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * Get STDOUT of sending script
	 *
	 * @return STDOUT of sending script
	 */
	public String getStdout() {
		return stdout;
	}

	/**
	 * Set STDOUT of sending script
	 *
	 * @param stdout STDOUT of sending script
	 */
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	/**
	 * Get STDERR of sending script
	 *
	 * @return STDERR of sending script
	 */
	public String getStderr() {
		return stderr;
	}

	/**
	 * Set STDERR of sending script
	 *
	 * @param stderr STDERR of sending script
	 */
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	/**
	 * Set SendTask ID as a pair of Task ID and Destination
	 */
	public void setId() {
		id = new Pair<>(task.getId(), destination);
	}

	/**
	 * Get ID of SendTask as a pair of Task ID and Destination
	 *
	 * @return ID of SendTask
	 */
	public Pair<Integer, Destination> getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SendTask)) return false;
		SendTask sendTask = (SendTask) o;
		return Objects.equals(startTime, sendTask.startTime) &&
				Objects.equals(endTime, sendTask.endTime) &&
				Objects.equals(task, sendTask.task) &&
				Objects.equals(destination, sendTask.destination);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startTime, endTime, task, destination);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName()).append(":[status='").append(status)
				.append("', startTime='").append((startTime!=null) ? BeansUtils.getDateFormatter().format(startTime) : startTime)
				.append("', endTime='").append((endTime!=null) ? BeansUtils.getDateFormatter().format(endTime) : endTime)
				.append("', returnCode='").append(returnCode)
				.append("', task='").append(task)
				.append("', destination='").append(destination)
				.append("', stdout='").append(stdout)
				.append("', stderr='").append(stderr)
				.append("']");
		return str.toString();
	}

	/**
	 * Represent state in which sending task is
	 */
	public static enum SendTaskStatus {
		SENDING, SENT, ERROR
	}
}
