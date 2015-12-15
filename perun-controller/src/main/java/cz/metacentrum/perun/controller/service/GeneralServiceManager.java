package cz.metacentrum.perun.controller.service;

import java.util.List;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * ExecService manager
 *
 * @author Michal Karm Babacek
 */
public interface GeneralServiceManager {

	/**
	 * Deletes the service as well as all the ExecServices bounded to it.
	 *
	 * @param perunSession
	 * @param service service to delete
	 */
	void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException;

	/**
	 * Lists all services
	 *
	 * @param perunSession
	 * @return list of all services
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	public List<Service> listServices(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

	/**
	 * Gets service by id.
	 *
	 * @param perunSession
	 * @param serviceId id of a service to get
	 * @return service with specified id
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public Service getService(PerunSession perunSession, int serviceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Lists all execServices
	 *
	 * @param perunSession
	 * @return list of all execServices
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listExecServices(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List all execServices tied to a certain Service
	 *
	 * @param perunSession
	 * @param serviceId
	 * @return list of all the execServices tied to a certain Service
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listExecServices(PerunSession perunSession, int serviceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Count execServices
	 *
	 * @return amount of execServices stored in the DB
	 */
	public int countExecServices();

	/**
	 * Get execService by ID
	 *
	 * @param perunSession
	 * @param execServiceId id to get execService by
	 * @return execService
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public ExecService getExecService(PerunSession perunSession, int execServiceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Inserts a new ExecService.
	 * If the parent Service doesn't exist, it will be created. If the parent Service already exists, the new service will be bound to it.
	 *
	 * @param perunSession
	 * @param execService execService to insert
	 * @return a new execService id
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceExistsException
	 */
	public int insertExecService(PerunSession perunSession, ExecService execService) throws InternalErrorException, PrivilegeException, ServiceExistsException;

	/**
	 * Updates the ExecService as well as the parent Service.
	 *
	 * @param perunSession
	 * @param execService execService to be updated
	 *
	 * @throws ServiceNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public void updateExecService(PerunSession perunSession, ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Remove execService
	 *
	 * @param execService execService to be removed
	 */
	public void deleteExecService(ExecService execService);

	/**
	 * Bans execService on facility.
	 * It wouldn't be possible to execute the given execService on the whole facility nor on any of its destinations.
	 *
	 * @param perunSession
	 * @param execService The execService to be banned on the facility
	 * @param facility The facility on which we want to ban the execService
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyBannedException
	 */
	public void banExecServiceOnFacility(PerunSession perunSession, ExecService execService, Facility facility) throws InternalErrorException, ServiceAlreadyBannedException;

	/**
	 * Bans execService on destination.
	 * It wouldn't be possible to execute the given execService on this destination, however,
	 * it still can be executed on all the other destinations in the facility.
	 *
	 * @param perunSession
	 * @param execService The execService to be banned on this particular destination
	 * @param destinationId The destination on which we want to ban the execService
	 * @throws InternalErrorException
	 */
	public void banExecServiceOnDestination(PerunSession perunSession, ExecService execService, int destinationId) throws InternalErrorException;

	/**
	 * List all the execServices that are banned on this facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return a list of execServices that are denied on the facility
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listDenialsForFacility(PerunSession perunSession, Facility facility) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List all the execServices that are banned on this destination.
	 *
	 * @param perunSession
	 * @param destinationId
	 * @return a list of execServices that are denied on the destination
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listDenialsForDestination(PerunSession perunSession, int destinationId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Is this execService denied on the facility?
	 *
	 * @param execService The execService, the denial of which we want to examine
	 * @param facility The facility on which we want to look up the denial of the execService
	 * @return true - in case the execService is denied on the facility false - in
	 *         case the execService in NOT denied on the facility
	 */
	public boolean isExecServiceDeniedOnFacility(ExecService execService, Facility facility);

	/**
	 * Is this execService denied on the destination?
	 *
	 * @param execService The execService, the denial of which we want to examine
	 * @param destinationId The destination on which we want to look up the denial of the execService
	 * @return true - in case the execService is denied on the destination false - in case
	 *         the execService in NOT denied on the destination
	 */
	public boolean isExecServiceDeniedOnDestination(ExecService execService, int destinationId);

	/**
	 * Erase all the possible denials on this facility.
	 * From this moment on, there are no execServices being denied on this facility.
	 *
	 * @param perunSession
	 * @param facility Facility we want to clear of all the denials.
	 * 
	 * @throws InternalErrorException
	 */
	public void freeAllDenialsOnFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Erase all the possible denials on this destination.
	 * From this moment on, there are no execServices being denied on this destination.
	 *
	 * @param perunSession
	 * @param destinationId The id of a destination we want to clear of all the denials.
	 * 
	 * @throws InternalErrorException
	 */
	public void freeAllDenialsOnDestination(PerunSession perunSession, int destinationId) throws InternalErrorException;

	/**
	 * Free the denial of the execService on this facility.
	 * If the execService was banned on this facility, it will be freed.
	 * In case the execService was not banned on this facility, nothing will happen.
	 *
	 * @param perunSession
	 * @param execService The execService, the denial of which we want to free on this facility.
	 * @param facility The facility on which we want to free the denial of the execService.
	 * 
	 * @throws InternalErrorException
	 */
	public void freeDenialOfExecServiceOnFacility(PerunSession perunSession, ExecService execService, Facility facility) throws InternalErrorException;

	/**
	 * Free the denial of the execService on this destination.
	 * If the execService was banned on this destination, it will be freed.
	 * In case the execService was not banned on this destination, nothing will happen.
	 *
	 * @param perunSession
	 * @param execService The execService, the denial of which we want to free on this destination.
	 * @param destinationId The id of a destination on which we want to free the denial of the execService.
	 * 
	 * @throws InternalErrorException
	 */
	public void freeDenialOfExecServiceOnDestination(PerunSession perunSession, ExecService execService, int destinationId) throws InternalErrorException;

	/**
	 * Create a dependency
	 * The execService can not be executed if any of the execServices it depends on is in an unstable (not terminal) state.
	 *
	 * @param dependantExecService The execService depending on the other execService.
	 * @param execService The execService the other execService depends on.
	 */
	public void createDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * Removes a dependency.
	 *
	 * @param dependantExecService The execService depending on the other execService.
	 * @param execService The execService the other execService depends on.
	 */
	public void removeDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * Checks whether one execService depends on the other.
	 *
	 * @param dependantExecService The execService depending on the other execService.
	 * @param execService The execService the other execService depends on.
	 * @return true - yes, there is such a dependency false - no, there is not such a dependency
	 */
	public boolean isThereDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * List execServices depending on the given execService
	 *
	 * @param perunSession
	 * @param execService The execService which dependent execServices we want to look up.
	 * @return A list of execServices that are depending on the given execService.
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listExecServicesDependingOn(PerunSession perunSession, ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List execServices this execService depends on
	 *
	 * @param perunSession
	 * @param dependantExecService execService which dependencies we want to look up
	 * @return A list of execServices this execService depends on.
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List execServices this execService depends on which are of specified execServiceType
	 *
	 * @param perunSession
	 * @param dependantExecService execService which dependencies we want to look up
	 * @param execServiceType type of execServices to list
	 * @return list of exec services this execService depends on
	 *
	 * @throws ServiceNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService, ExecServiceType execServiceType) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Forces service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return true if it is possible, false if not
	 *
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	boolean forceServicePropagation(PerunSession perunSession, Facility facility, Service service) throws ServiceNotExistsException, FacilityNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Forces service propagation on all facilities where the service is defined on.
	 *
	 * @param perunSession
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 * @throws ServiceNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	boolean forceServicePropagation(PerunSession perunSession, Service service) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Return list of ServiceForGUI assigned on facility, (Service with "allowedOnFacility" property filled).
	 * 1 - allowed / 0 - one of service exec services is denied on this facility (=> service is denied).
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of assigned services with allowed property
	 *
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws InternalErrorException
	 */
	List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException;

	/**
	 * Creates 2 ExecServices, first one has type GENERATE, second one has type SEND. 
	 * Method also creates dependency of SEND service on GENERATE service.
	 *
	 * @param perunSession
	 * @param serviceName name of the service
	 * @param scriptPath path to the gen/send script
	 * @param defaultDelay
	 * @param enabled
	 * @throws InternalErrorException
	 * @throws PrivilegeException The method can be executed only by 
	 * PERUNADMIN user, otherwise the PrivilegeException is thrown.
	 * @throws ServiceExistsException Exception is thrown when you're trying
	 * to create a service that already exists
	 * @return Service Created service with <code>id</code> set.
	 */
	Service createCompleteService(PerunSession perunSession, String serviceName, String scriptPath, int defaultDelay, boolean enabled) throws InternalErrorException, PrivilegeException, ServiceExistsException;

}
