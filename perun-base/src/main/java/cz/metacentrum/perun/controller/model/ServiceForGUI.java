package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Service;

/**
 * Extension of Service (from Perun-Core) to provide more info for GUI
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
public class ServiceForGUI extends Service {

	// contextual info
	private boolean allowedOnFacility;

	public ServiceForGUI(Service service){
		setId(service.getId());
		setName(service.getName());
		setDescription(service.getDescription());
		setDelay(service.getDelay());
		setRecurrence(service.getRecurrence());
		setScript(service.getScript());
		setEnabled(service.isEnabled());
	}

	public void setAllowedOnFacility(boolean allowed){
		allowedOnFacility = allowed;
	}

	public boolean getAllowedOnFacility(){
		return allowedOnFacility;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceForGUI other = (ServiceForGUI) obj;
		if (allowedOnFacility != other.allowedOnFacility)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "ServiceForGUI [allowedOnFacility=" + allowedOnFacility
				+ ", Service=" + super.toString() + "]";
	}

}
