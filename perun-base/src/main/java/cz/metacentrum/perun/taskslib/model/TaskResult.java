package cz.metacentrum.perun.taskslib.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Service;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
public class TaskResult extends PerunBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5656828750714418582L;

	public static enum TaskResultStatus {
		DONE, ERROR, FATAL_ERROR, DENIED, WARN
	}

	private int id;
	private int taskId;
	private int destinationId;
	private String errorMessage;
	private String standardMessage;
	private int returnCode;
	private Date timestamp;
	private TaskResultStatus status;
	private Destination destination;
	private Service service;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destinationId;
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + id;
		result = prime * result + returnCode;
		result = prime * result + ((standardMessage == null) ? 0 : standardMessage.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + taskId;
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskResult other = (TaskResult) obj;
		if (destinationId != other.destinationId)
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (id != other.id)
			return false;
		if (returnCode != other.returnCode)
			return false;
		if (standardMessage == null) {
			if (other.standardMessage != null)
				return false;
		} else if (!standardMessage.equals(other.standardMessage))
			return false;
		if (status != other.status)
			return false;
		if (taskId != other.taskId)
			return false;
		if (!Objects.equals(service, other.service))
			return false;
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String dateString;
		if(timestamp != null) dateString = BeansUtils.getDateFormatter().format(timestamp);
		else dateString = "\\0";

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", taskId=<").append(taskId).append(">").append(
			", destinationId=<").append(destinationId).append(">").append(
			", errorMessage=<").append(errorMessage== null ? "\\0" : BeansUtils.createEscaping(errorMessage.toString())).append(">").append(
			", standardMessage=<").append(standardMessage== null ? "\\0" : BeansUtils.createEscaping(standardMessage.toString())).append(">").append(
			", returnCode=<").append(returnCode).append(">").append(
			", timestamp=<").append(dateString).append(">").append(
			", status=<").append(status == null ? "\\0" : BeansUtils.createEscaping(status.toString())).append(">").append(
			", service=<").append(service == null ? "\\0" : service.serializeToString()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		return "TaskResult:[id='" + id + "', taskId='" + taskId + "', destinationId='" + destinationId + "', errorMessage='" + errorMessage + "', "
				+ "standardMessage='" + standardMessage + "', returnCode='"
			+ returnCode + "', timestamp='" + BeansUtils.getDateFormatter().format(timestamp) + "', status='" + status + "', service='" + service + "']";
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStandardMessage() {
		return standardMessage;
	}

	public void setStandardMessage(String standardMessage) {
		this.standardMessage = standardMessage;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public TaskResultStatus getStatus() {
		return status;
	}

	public void setStatus(TaskResultStatus status) {
		this.status = status;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public String getBeanName(){
		return this.getClass().getSimpleName();
	}

}
