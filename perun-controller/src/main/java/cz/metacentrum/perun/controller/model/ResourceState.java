package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.taskslib.model.Task;

import java.util.List;

/**
 * Created by Oliver Mrazik a Simona Kruppova on 28. 7. 2014.
 */
public class ResourceState implements Comparable<ResourceState>{

	private Resource resource;
	private List<Task> taskList;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public List<Task> getTaskList() {
		return taskList;
	}

	public void setTaskList(List<Task> taskList) {
		this.taskList = taskList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ResourceState that = (ResourceState) o;

		if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;
		if (taskList != null ? !taskList.equals(that.taskList) : that.taskList != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = resource != null ? resource.hashCode() : 0;
		result = 31 * result + (taskList != null ? taskList.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ResourceState[" + "resource=" + resource + ", taskList=" + taskList +']';
	}

	@Override
	public int compareTo(ResourceState resource) {
		if (resource == null || this.resource == null || this.resource.getName() == null) throw new NullPointerException("Resource or resource name");
		return this.resource.getName().compareTo(resource.getResource().getName());
	}
}
