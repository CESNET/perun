package cz.metacentrum.perun.engine.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 * 
 */
public interface DependenciesResolver {

    List<ExecService> listDependencies(ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

    List<ExecService> listDependantServices(ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

    List<ExecService> listDependencies(int execServiceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;
}
