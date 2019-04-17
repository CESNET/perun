package cz.metacentrum.perun.taskslib.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	private LocalDateTime startTime;
	private LocalDateTime sentToEngine;
	private LocalDateTime sendStartTime;
	private LocalDateTime schedule;
	private LocalDateTime genStartTime;
	private LocalDateTime genEndTime;
	private LocalDateTime sendEndTime;
	private LocalDateTime endTime;
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

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public Long getStartTimeAsLong() {
		return (startTime == null) ? null : startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setStartTime(Long startTime) {
		if (startTime != null) {
			this.startTime = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.startTime = null;
		}
	}

	public LocalDateTime getSchedule() {
		return schedule;
	}

	public Long getScheduleAsLong() {
		return (schedule == null) ? null : schedule.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setSchedule(LocalDateTime schedule) {
		this.schedule = schedule;
	}

	public void setSchdule(Long schedule) {
		if (schedule != null) {
			this.schedule = Instant.ofEpochMilli(schedule).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.schedule = null;
		}
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

	public LocalDateTime getGenEndTime() {
		return genEndTime;
	}

	public Long getGenEndTimeAsLong() {
		return (genEndTime == null) ? null : genEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setGenEndTime(LocalDateTime genEndTime) {
		this.genEndTime = genEndTime;
	}

	public void setGenEndTime(Long genEndTime) {
		if (genEndTime != null) {
			this.genEndTime = Instant.ofEpochMilli(genEndTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.genEndTime = null;
		}
	}

	public LocalDateTime getSendEndTime() {
		return sendEndTime;
	}

	public Long getSendEndTimeAsLong() {
		return (sendEndTime == null) ? null : sendEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setSendEndTime(LocalDateTime sendEndTime) {
		this.sendEndTime = sendEndTime;
	}

	public void setSendEndTime(Long sendEndTime) {
		if (sendEndTime != null) {
			this.sendEndTime = Instant.ofEpochMilli(sendEndTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.sendEndTime = null;
		}
	}

	public LocalDateTime getSendStartTime() {
		return sendStartTime;
	}

	public Long getSendStartTimeAsLong() {
		return (sendStartTime == null) ? null : sendStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public LocalDateTime getGenStartTime() {
		return genStartTime;
	}

	public Long getGenStartTimeAsLong() {
		return (genStartTime == null) ? null : genStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setGenStartTime(LocalDateTime genStartTime) {
		this.genStartTime = genStartTime;
	}

	public void setGenStartTime(Long genStartTime) {
		if (genStartTime != null) {
			this.genStartTime = Instant.ofEpochMilli(genStartTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.genStartTime = null;
		}
	}

	public LocalDateTime getSentToEngine() {
		return sentToEngine;
	}

	public Long getSentToEngineAsLong() {
		return (sentToEngine == null) ? null : sentToEngine.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setSentToEngine(LocalDateTime sentToEngine) {
		this.sentToEngine = sentToEngine;
	}

	public void setSentToEngine(Long sentToEngine) {
		if (sentToEngine != null) {
			this.sentToEngine = Instant.ofEpochMilli(sentToEngine).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.sentToEngine = null;
		}
	}

	public void setSendStartTime(LocalDateTime sendStartTime) {
		this.sendStartTime = sendStartTime;
	}

	public void setSendStartTime(Long sendStartTime) {
		if (sendStartTime != null) {
			this.sendStartTime = Instant.ofEpochMilli(sendStartTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.sendStartTime = null;
		}
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public Long getEndTimeAsLong() {
		return (endTime == null) ? null : endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public void setEndTime(Long endTime) {
		if (endTime != null) {
			this.endTime = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
			this.endTime = null;
		}
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
