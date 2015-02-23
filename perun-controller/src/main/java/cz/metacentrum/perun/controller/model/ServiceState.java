package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceState is object containing information about one service propagated on one facility.
 * 
 * @author Jana Cechackova
 */
public class ServiceState {
    private int id;
    private List<Task> tasks = new ArrayList<>();
    private List<ExecService> execServices = new ArrayList<>();
    private Service service;
    private Facility facility;
    private boolean isBlockedOnFacility;
    private boolean isBlockedGlobally;
        
    public void setId(int id){
	this.id = id;
    }
    
    public int getId(){
	return id;
    }
    
    public boolean isBlockedGlobally(){
	return isBlockedGlobally;
    }
    
    public void setBlockedGlobally(boolean isBlockedGlobally){
	this.isBlockedGlobally = isBlockedGlobally;
    }
    
    public boolean isBlockedOnFacility(){
	return isBlockedOnFacility;
    }
    
    public void setBlockedOnFacility(boolean isBlockedOnFacility){
	this.isBlockedOnFacility = isBlockedOnFacility;
    }
        
    public boolean hasDestinationsOnFacility(){
	for (Task task : tasks){
	    if (!task.getDestinations().isEmpty())  return true;
	}
	return false;	
    }
    
    public Facility getFacility(){
	return facility;
    }
    
    public void setFacility(Facility facility){
	this.facility = facility;
    }
        
    public Service getService(){
	return service;
    }
    
    public void setService(Service service){
	this.service = service;
    }
    
   public List<Task> getAllTasks(){
	return tasks;
    }
    
    public void addTask(Task task){
	tasks.add(task);
    }
    
    public List<ExecService> getAllExecServices(){
	return execServices;
    }
    
    public void addExecService(ExecService execService){
	execServices.add(execService);
    }
    
    public String getBeanName() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String toString(){
	
	StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":["
			).append("id='").append(getId()
			).append("', service='").append(getService().toString()
			).append("', facility='").append(getFacility().toString()
			).append("', has destinations on facility ='").append(hasDestinationsOnFacility()
			).append("', blocked globally='").append(isBlockedGlobally()
			).append("', blocked on facility='").append(isBlockedOnFacility()
			).append("']").toString();
    }
}
