package cz.metacentrum.perun.dispatcher.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao.DependencyScope;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 * 
 */
public interface DependenciesResolver {

    List<ExecService> listDependencies(ExecService execService);

    List<ExecService> listDependantServices(ExecService execService);

    List<ExecService> listDependencies(int execServiceId);
    
    List<Pair<ExecService, DependencyScope>> listDependenciesAndScope(ExecService execService);

    List<Pair<ExecService, DependencyScope>> listDependenciesAndScope(int execServiceId);
}
