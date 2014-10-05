package cz.metacentrum.perun.dispatcher.scheduling.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.scheduling.DependenciesResolver;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao.DependencyScope;
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
    private ExecServiceDependencyDao execServiceDependencyDao;

    // screw GeneralServiceManager, go straight for the DAO
    // (the manager just proxies, but requires perun session as paramater - which it does not use in this case)
    
    @Override
    public List<ExecService> listDependencies(ExecService execService) {
        List<ExecService> dependencies = execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(execService.getId());
        log.debug("listDependencies #DependenciesResolver:" + dependencies);
        return dependencies;
    }

    @Override
    public List<ExecService> listDependencies(int execServiceId) {
        List<ExecService> dependencies = execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(execServiceId);
        log.debug("listDependencies #DependenciesResolver:" + dependencies);
        return dependencies;
    }

    @Override
    public List<ExecService> listDependantServices(ExecService execService) {
        return execServiceDependencyDao.listExecServicesDependingOn(execService.getId());
    }

	@Override
	public List<Pair<ExecService, DependencyScope>> listDependenciesAndScope(
			ExecService execService) {
		return execServiceDependencyDao.listExecServicesAndScopeThisExecServiceDependsOn(execService.getId());
	}

	@Override
	public List<Pair<ExecService, DependencyScope>> listDependenciesAndScope(
			int execServiceId) {
		return execServiceDependencyDao.listExecServicesAndScopeThisExecServiceDependsOn(execServiceId);
	}

}
