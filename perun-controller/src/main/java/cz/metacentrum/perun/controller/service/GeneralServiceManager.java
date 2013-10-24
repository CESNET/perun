package cz.metacentrum.perun.controller.service;

import java.util.List;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * ExecService manager
 * 
 * @author Michal Karm Babacek
 * @version first draft
 */
public interface GeneralServiceManager {
    
    /** 
     * Delete the service.
     * 
     * Deletes the service as well as all the 
     * ExecServices bounded to it. 
     * 
     * @param perunSession
     * @param service 
     */
    void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException;

    /**
	 * List Services
	 * 
	 * @param perunSession
	 * @return list of services
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 */
	public List<Service> listServices(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

	/**
	 * Get service
	 * 
	 * @param perunSession
	 * @param serviceId
	 * @return service by given ID
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public Service getService(PerunSession perunSession, int serviceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;
	
	
	
	/**
	 * List all execServices
	 * 
	 * @return list of all the execServices
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public List<ExecService> listExecServices(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List all execServices tied to a certain Service
	 * 
	 * @return list of all the execServices tied to a certain Service
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
	 * @param execServiceId
	 * @return execService
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public ExecService getExecService(PerunSession perunSession, int execServiceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Insert a new ExecService
	 * 
	 * Inserts a new ExecService. If the parent Service doesn't exist, 
	 * it will be created as well. If the parent Service already exists,
	 * it will be just binded to it.
	 * 
	 * @param execService
	 * @return a new execService
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 */
	public int insertExecService(PerunSession perunSession, ExecService execService, Owner owner) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, ServiceExistsException;

	
	/**
	 * Update execService
	 * Updates the ExecService as well as the parent Service. 
	 * @param execService
	 *            to by updated
	 */
	public void updateExecService(PerunSession perunSession, ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Remove execService
	 * 
	 * @param execServiceId
	 * 					id of the execService that is to be removed
	 */
	public void deleteExecService(ExecService execService);

	/**
	 * Ban execService on facility It woun't be possible to execute the given execService
	 * on the whole facility nor on any of it's destinations.
	 * 
	 * @param execService
	 *            The execService to be banned on the facility
	 * @param facility
	 *            The facility on which we want to ban the execService
	 * @throws InternalErrorException 
	 */
	public void banExecServiceOnFacility(ExecService execService, Facility facility) throws InternalErrorException;

	/**
	 * Ban execService on destination It woun't be possible to execute the given execService on
	 * this destination, however, it still can be executed on all the other destinations in
	 * the facility.
	 * 
	 * @param execService
	 *            The execService to be banned on this particular destination
	 * @param destination
	 *            The destination on which we want to ban the execService
	 * @throws InternalErrorException 
	 */
	public void banExecServiceOnDestination(ExecService execService, int destinationId) throws InternalErrorException;

	/**
	 * List denials for facility List all the execServices that are banned on this
	 * facility.
	 * 
	 * @param facility
	 * 
	 * @return a list of execServices that are denied on the facility
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public List<ExecService> listDenialsForFacility(PerunSession perunSession, Facility facility) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List denials for destination List all the execServices that are banned on this destination.
	 * 
	 * @param destination
	 * 
	 * @return a list of execServices that are denied on the destination
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public List<ExecService> listDenialsForDestination(PerunSession perunSession, int destinationId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Is this execService denied on the facility?
	 * 
	 * @param execService
	 *            The execService, the denial of which we want to examine
	 * @param facility
	 *            The facility on which we want to look up the denial of the
	 *            execService
	 * @return true - in case the execService is denied on the facility false - in
	 *         case the execService in NOT denied on the facility
	 */
	public boolean isExecServiceDeniedOnFacility(ExecService execService, Facility facility);

	/**
	 * Is this execService denied on the destination?
	 * 
	 * @param execService
	 *            The execService, the denial of which we want to examine
	 * @param destination
	 *            The destination on which we want to look up the denial of the execService
	 * @return true - in case the execService is denied on the destination false - in case
	 *         the execService in NOT denied on the destination
	 */
	public boolean isExecServiceDeniedOnDestination(ExecService execService, int destinationId);

	/**
	 * Free all denials on the facility Erase all the possible denials on this
	 * facility. From this moment on, there are no execServices being denied on this
	 * facility.
	 * 
	 * @param facility
	 *            Facility we want to clear of all the denials.
	 */
	public void freeAllDenialsOnFacility(Facility facility);

	/**
	 * Free all denials on the destination Erase all the possible denials on this destination.
	 * From this moment on, there are no execServices being denied on this destination.
	 * 
	 * @param destination
	 *            Destination we want to clear of all the denials.
	 */
	public void freeAllDenialsOnDestination(int destinationId);

	/**
	 * Free the denial of the execService on this facility. If the execService was banned
	 * on this facility, it will be freed. In case the execService was not banned on
	 * this facility, nothing will happen.
	 * 
	 * @param execService
	 *            The execService, the denial of which we want to free on this
	 *            facility.
	 * @param facility
	 *            The facility on which we want to free the denial of the
	 *            execService.
	 */
	public void freeDenialOfExecServiceOnFacility(ExecService execService, Facility facility);

	/**
	 * Free the denial of the execService on this destination. If the execService was banned on
	 * this destination, it will be freed. In case the execService was not banned on this
	 * destination, nothing will happen.
	 * 
	 * @param execService
	 *            The execService, the denial of which we want to free on this destination.
	 * @param destination
	 *            The destination on which we want to free the denial of the execService.
	 */
	public void freeDenialOfExecServiceOnDestination(ExecService execService, int destinationId);

	/**
	 * Create dependency The execService can not be executed if any of the execServices
	 * it depends on is in an unstable (not terminal) state.
	 * 
	 * @param dependantExecService
	 *            The execService depending on the other execService.
	 * @param execService
	 *            The execService the other execService depends on.
	 */
	public void createDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * Remove dependency Removes the dependency...
	 * 
	 * @param dependantExecService
	 *            The execService depending on the other execService.
	 * @param execService
	 *            The execService the other execService depends on.
	 */
	public void removeDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * Is there a dependency? Checks whether there the one execService depends on
	 * the other.
	 * 
	 * @param dependantExecServiceId
	 *            The execService depending on the other execService.
	 * @param execServiceId
	 *            The execService the other execService depends on.
	 * @return true - yes, there is such a dependency false - no, there is not
	 *         such a dependency
	 */
	public boolean isThereDependency(ExecService dependantExecService, ExecService execService);

	/**
	 * List execServices depending on the given execService
	 * 
	 * @param execServiceId
	 *            The execService which dependent execServices we want to look up.
	 * @return A list of execServices that are depending on the given execService.
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public List<ExecService> listExecServicesDependingOn(PerunSession perunSession, ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List execServices this execService depends on
	 * 
	 * @param dependantExecServiceId
	 *            The execService which dependencies we want to look up.
	 * @return A list of execServices this execService depends on.
	 * @throws PrivilegeException 
	 * @throws InternalErrorException 
	 * @throws ServiceNotExistsException 
	 */
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;
	
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService, ExecServiceType execServiceType) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Forces service propagation on defined facility.
	 * 
	 * @param perunSession
	 * @param service
	 * @param facility
         * @return true if it is possible, return false if not
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
         * @return true if it is possible, return false if not
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
	 */
	List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException ;


}
