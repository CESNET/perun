package cz.metacentrum.perun.taskslib.model;

import java.io.Serializable;

import cz.metacentrum.perun.core.api.Service;

/**
 * @author Michal Karm Babacek
 */
public class ExecService implements Serializable {
	private static final long serialVersionUID = 3257568390917667126L;

	public static enum ExecServiceType {
		GENERATE, SEND
	}

	private int id;
	private int defaultDelay;
	private int defaultRecurrence = 5;
	private boolean enabled;
	private Service service;
	private String script;
	private ExecServiceType execServiceType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDefaultDelay() {
		return defaultDelay;
	}

	public void setDefaultDelay(int defaultDelay) {
		this.defaultDelay = defaultDelay;
	}

	public int getDefaultRecurrence() {
		return defaultRecurrence;
	}

	public void setDefaultRecurrence(int defaultRecurrence) {
		this.defaultRecurrence = defaultRecurrence;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public ExecServiceType getExecServiceType() {
		return execServiceType;
	}

	public void setExecServiceType(ExecServiceType execServiceType) {
		this.execServiceType = execServiceType;
	}

    public String getBeanName(){
        return this.getClass().getSimpleName();
    }

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
		ExecService other = (ExecService) obj;
		if (this.getId() != other.getId())
			return false;
		return true;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		String toBeReturned = null;
		if(service != null && execServiceType != null) {
			toBeReturned = "ExecService:[id:"+id+", name:"+service.getName()+", type:"+execServiceType.toString()+"]";
		} else {
			toBeReturned = "ExecService:[id:"+id+", name:null, type:null]";
		}
		return toBeReturned;
	}
	
	

}
