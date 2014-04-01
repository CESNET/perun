package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * Extension of Service (from Perun-Core) to provide more info for GUI
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */

public class ServiceForGUI extends Service {

	// global service info calculated by GeneralServiceManager
	// (based on allowed status of exec services)
	private boolean allowedOnFacility;
	// Allowed on Facility state for each exec service
	private boolean genAllowedOnFacility;
	private boolean sendAllowedOnFacility;
	// Exec services (with global allowed info about them)
	private ExecService generate;
	private ExecService send;

	public ServiceForGUI(Service service){

		setId(service.getId());
		setName(service.getName());

	}

	public void setAllowedOnFacility(boolean allowed){
		allowedOnFacility = allowed;
	}

	public boolean getAllowedOnFacility(){
		return allowedOnFacility;
	}

	public void setGenAllowedOnFacility(boolean allowed){
		genAllowedOnFacility = allowed;
	}

	public boolean getGenAllowedOnFacility(){
		return genAllowedOnFacility;
	}

	public void setSendAllowedOnFacility(boolean allowed){
		sendAllowedOnFacility = allowed;
	}

	public boolean getSendAllowedOnFacility(){
		return sendAllowedOnFacility;
	}

	public void setGenExecService(ExecService gen){
		generate = gen;
	}

	public ExecService getGenExecService(){
		return generate;
	}

	public void setSendExecService(ExecService send){
		this.send = send;
	}

	public ExecService getSendExecService(){
		return this.send;
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
		if (genAllowedOnFacility != other.genAllowedOnFacility)
			return false;
		if (sendAllowedOnFacility != other.sendAllowedOnFacility)
			return false;
		if (send != other.send)
			return false;
		if (generate != other.generate)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "ServiceForGUI [allowedOnFacility=" + allowedOnFacility
			+ ", genAllowedOnFacility = " + genAllowedOnFacility
			+ ", sendAllowedOnFacility = " + sendAllowedOnFacility
			+ ", generate = " + generate
			+ ", send = " + send
			+ ", Service=" + super.toString() + "]";
	}

}
