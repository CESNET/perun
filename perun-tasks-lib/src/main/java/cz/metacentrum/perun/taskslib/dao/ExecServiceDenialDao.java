package cz.metacentrum.perun.taskslib.dao;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * ExecService Denial Data Access Object
 *
 * @author Michal Karm Babacek
 */
public interface ExecServiceDenialDao {

	/**
	 * Ban execService on facility
	 * It wouldn't be possible to execute the given execService on the
	 * whole facility nor on any of it's destinations.
	 * @param execServiceId
	 * 				The execService to be banned on the facility
	 * @param facilityId
	 * 				The facility on which we want to ban the execService
	 * @throws InternalErrorException
	 */
	public void banExecServiceOnFacility(int execServiceId, int facilityId) throws InternalErrorException;

	/**
	 * Ban execService on destination
	 * It wouldn't be possible to execute the given execService on this destination,
	 * however, it still can be executed on all the other destinations in the facility.
	 * @param execServiceId
	 * 			The execService to be banned on this particular destination
	 * @param destinationId
	 * 			The destination on which we want to ban the execService
	 * @throws InternalErrorException
	 */
	public void banExecServiceOnDestination(int execServiceId, int destinationId) throws InternalErrorException;

	/**
	 * List denials for facility
	 * List all the execServices that are banned on this facility.
	 * @param facilityId
	 *
	 * @return a list of execServices that are denied on the facility
	 */
	public List<ExecService> listDenialsForFacility(int facilityId);

	/**
	 * List denials for destination
	 * List all the execServices that are banned on this destination.
	 * @param destinationId
	 *
	 * @return a list of execServices that are denied on the destination
	 */
	public List<ExecService> listDenialsForDestination(int destinationId);

	/**
	 * Is this execService denied on the facility?
	 *
	 * @param execServiceId
	 * 			The execService, the denial of which we want to examine
	 * @param facilityId
	 * 			The facility on which we want to look up the denial of the execService
	 * @return	true - in case the execService is denied on the facility
	 * 			false - in case the execService in NOT denied on the facility
	 */

	public boolean isExecServiceDeniedOnFacility(int execServiceId, int facilityId);

	/**
	 * Is this execService denied on the destination?
	 *
	 * @param execServiceId
	 * 			The execService, the denial of which we want to examine
	 * @param destinationId
	 * 			The destination on which we want to look up the denial of the execService
	 * @return	true - in case the execService is denied on the destination
	 * 			false - in case the execService in NOT denied on the destination
	 */
	public boolean isExecServiceDeniedOnDestination(int execServiceId, int destinationId);

	/**
	 * Free all denials on the facility
	 * Erase all the possible denials on this facility. From this moment on, there
	 * are no execServices being denied on this facility.
	 * @param facility
	 * 			Facility we want to clear of all the denials.
	 */
	public void freeAllDenialsOnFacility(int facility);

	/**
	 * Free all denials on the destination
	 * Erase all the possible denials on this destination. From this moment on, there
	 * are no execServices being denied on this destination.
	 * @param destination
	 * 			Destination we want to clear of all the denials.
	 */
	public void freeAllDenialsOnDestination(int destination);

	/**
	 * Free the denial of the execService on this facility.
	 * If the execService was banned on this facility, it will be freed. In case
	 * the execService was not banned on this facility, nothing will happen.
	 *
	 * @param execServiceId
	 * 			The execService, the denial of which we want to free on this facility.
	 * @param facilityId
	 * 			The facility on which we want to free the denial of the execService.
	 */
	public void freeDenialOfExecServiceOnFacility(int execServiceId, int facilityId);

	/**
	 * Free the denial of the execService on this destination.
	 * If the execService was banned on this destination, it will be freed. In case
	 * the execService was not banned on this destination, nothing will happen.
	 *
	 * @param execServiceId
	 * 			The execService, the denial of which we want to free on this destination.
	 * @param destinationId
	 * 			The destination on which we want to free the denial of the execService.
	 */
	public void freeDenialOfExecServiceOnDestination(int execServiceId, int destinationId);

}
