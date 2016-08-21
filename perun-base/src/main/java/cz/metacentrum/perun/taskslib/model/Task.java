package cz.metacentrum.perun.taskslib.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

/**
 *
 * @author Michal Karm Babacek
 */
public class Task implements Serializable {

	private static final long serialVersionUID = -1809998673612582742L;

	public enum TaskStatus {
		WAITING, PLANNED, GENERATING, GENERROR, GENERATED, SENDING, DONE, SENDERROR, ERROR
	}

	private int id;
	private int delay;
	private int recurrence;
	private Date startTime;
	private Date sentToEngine;
	private Date sendStartTime;
	private Date schedule;
	private Date genStartTime;
	private Date genEndTime;
	private Date sendEndTime;
	private Date endTime;
	private Service service;
	private Facility facility;
	private List<Destination> destinations;
	private TaskStatus status;
	private boolean sourceUpdated;
	private boolean propagationForced;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Task other = (Task) obj;
		if (!service.equals(other.service))
			return false;
		if (!facility.equals(other.facility))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(int recurrence) {
		this.recurrence = recurrence;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getSchedule() {
		return schedule;
	}

	public void setSchedule(Date schedule) {
		this.schedule = schedule;
	}


	public int getServiceId() {
		if (service != null) {
			return service.getId();
		} else {
			return -1;
		}
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Service getService() {
		return service;
	}

	public int getFacilityId() {
		if (facility != null) {
			return facility.getId();
		} else {
			return -1;
		}
	}

	public Date getGenEndTime() {
		return genEndTime;
	}

	public void setGenEndTime(Date genEndTime) {
		this.genEndTime = genEndTime;
	}

	public Date getSendEndTime() {
		return sendEndTime;
	}

	public void setSendEndTime(Date sendEndTime) {
		this.sendEndTime = sendEndTime;
	}

	public Date getSendStartTime() {
		return sendStartTime;
	}

	public Date getGenStartTime() {
		return genStartTime;
	}

	public void setGenStartTime(Date genStartTime) {
		this.genStartTime = genStartTime;
	}

	public Date getSentToEngine() {
		return sentToEngine;
	}

	public void setSentToEngine(Date sentToEngine) {
		this.sentToEngine = sentToEngine;
	}

	public void setSendStartTime(Date sendStartTime) {
		this.sendStartTime = sendStartTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public List<Destination> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<Destination> destinations) {
		this.destinations = destinations;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getBeanName(){
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName())
				.append(":[id='").append(id)
				.append("', status='").append(status)
				.append("', delay='").append(delay)
				.append("', recurrence='").append(recurrence)
				.append("', sourceUpdated='").append(sourceUpdated)
				.append("', forced='").append(propagationForced)
				.append("', schedule='").append(schedule)
				.append("', startTime='").append(startTime)
				.append("', endTime='").append(endTime)
				.append("', genStartTime='").append(genStartTime)
				.append("', genEndTime='").append(genEndTime)
				.append("', sendStartTime='").append(sendStartTime)
				.append("', sendEndTime='").append(sendEndTime)
				.append("', service='").append(service)
				.append("', facility='").append(facility)
				.append("', destinations='").append(destinations)
				.append("']").toString();
	}

	public boolean isSourceUpdated() {
		return sourceUpdated;
	}

	public void setSourceUpdated(boolean sourceUpdated) {
		this.sourceUpdated = sourceUpdated;
	}

	public boolean isPropagationForced() {
		return propagationForced;
	}

	public void setPropagationForced(boolean propagationForced) {
		this.propagationForced = propagationForced;
	}
}
