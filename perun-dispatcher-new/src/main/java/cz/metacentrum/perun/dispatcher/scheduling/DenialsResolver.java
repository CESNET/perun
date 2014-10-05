package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 * 
 *         TODO: Whether to base this on isExecServiceDeniedOnFacility or
 *         on a listing of all the Facilities/Destinations the ExecService is based on?
 * 
 */
public interface DenialsResolver {

    boolean isExecServiceDeniedOnFacility(ExecService execService, Facility facility) throws InternalErrorException;

    boolean isExecServiceDeniedOnDestination(ExecService execService, int destination) throws InternalErrorException;
}
