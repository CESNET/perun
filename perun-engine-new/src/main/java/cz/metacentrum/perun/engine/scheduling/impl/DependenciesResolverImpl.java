package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "dependenciesResolver")
public class DependenciesResolverImpl implements DependenciesResolver {

    private final static Logger log = LoggerFactory.getLogger(DependenciesResolverImpl.class);

    private Map<Task, List<Integer>> taskDependencies = new ConcurrentHashMap<Task, List<Integer>>();
    
    @Autowired
    private SchedulingPool schedulingPool;
    
/*
    @Autowired
    private EngineManager engineManager;

    @Override
    public List<ExecService> listDependencies(ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
        List<ExecService> dependencies = Rpc.GeneralServiceManager.listExecServicesThisExecServiceDependsOn(engineManager.getRpcCaller(), execService);
        log.debug("listDependencies #DependenciesResolver:" + dependencies);
        return dependencies;
    }

    @Override
    public List<ExecService> listDependencies(int execServiceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
        //TODO: Remove this nasty hack! RPC to be updated...
        ExecService execService = new ExecService();
        execService.setId(execServiceId);
        List<ExecService> dependencies = Rpc.GeneralServiceManager.listExecServicesThisExecServiceDependsOn(engineManager.getRpcCaller(), execService);
        log.debug("listDependencies #DependenciesResolver:" + dependencies);
        return dependencies;
    }

    @Override
    public List<ExecService> listDependantServices(ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
        return Rpc.GeneralServiceManager.listExecServicesThisExecServiceDependsOn(engineManager.getRpcCaller(), execService);
    }

    public EngineManager getEngineManager() {
        return engineManager;
    }

    public void setEngineManager(EngineManager engineManager) {
        this.engineManager = engineManager;
    }
*/
    
	@Override
	public List<Task> getDependencies(Task task) {
		if(!taskDependencies.containsKey(task)) {
			return new ArrayList<Task>();
		}
		List<Task> results = new ArrayList<Task>(taskDependencies.get(task).size());
		for(int id : taskDependencies.get(task)) {
			Task dependant = schedulingPool.getTaskById(id);
			if(dependant != null) { 
				results.add(dependant);
			}
		}
		return results;
	}

	@Override
	public List<Task> getDependants(Task task) {
		// now this is a bit harder than simply getting the value out of hash map
		List<Task> results = new ArrayList<Task> ();
		for(Map.Entry<Task, List<Integer>> entry : taskDependencies.entrySet()) {
			if(entry.getValue().contains(task.getId())) {
				results.add(entry.getKey());
			}
		}
		return results;
	}

	@Override
	public void addDependency(Task task, int dependency) {
		if(taskDependencies.containsKey(task)) {
			taskDependencies.get(task).add(dependency);
		} else {
			List<Integer> dependencies = new ArrayList<Integer>();
			dependencies.add(dependency);
			taskDependencies.put(task, dependencies);
		}
	}
}
