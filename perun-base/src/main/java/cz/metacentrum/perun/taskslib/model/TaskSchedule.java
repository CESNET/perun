package cz.metacentrum.perun.taskslib.model;

import cz.metacentrum.perun.core.api.BeansUtils;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TaskSchedule implements Delayed {
	private final Task task;
	private long base;
	private long delay;
	private int delayCount;

	public TaskSchedule(long delay, Task task) {
		this.delay = delay;
		this.task = task;
	}

	public void setBase(long base) {
		this.base = base;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public Task getTask() {
		return task;
	}

	public int getDelayCount() {
		return delayCount;
	}

	public void setDelayCount(int delayCount) {
		this.delayCount = delayCount;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay - (System.currentTimeMillis() - base), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed delayed) {
		return Long.valueOf(this.delay).compareTo(delayed.getDelay(TimeUnit.MILLISECONDS));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((task == null) ? 0 : task.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TaskSchedule)) {
			return false;
		}

		final TaskSchedule other = (TaskSchedule) obj;
		if (task == null) {
			if (other.task != null) {
				return false;
			}
		} else if (!(task.equals(other.task))) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName())
				.append(":[base='").append(BeansUtils.getDateFormatter().format(base))
				.append("', delay='").append((delay == 0) ? delay : delay/1000).append("s")
				.append("', remaining='").append(getDelay(TimeUnit.SECONDS)).append("s")
				.append("', delayCount='").append(delayCount)
				.append("', task='").append(task)
				.append("']").toString();
	}

}
