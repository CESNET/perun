package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "dependenciesResolver")
public class DependenciesResolverImpl implements DependenciesResolver {

	private final static Logger log = LoggerFactory.getLogger(DependenciesResolverImpl.class);

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
}
