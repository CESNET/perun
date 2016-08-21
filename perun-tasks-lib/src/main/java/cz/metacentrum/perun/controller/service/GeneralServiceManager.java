package cz.metacentrum.perun.controller.service;

import java.util.List;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;

/**
 * Propagation manager allows to plan/force propagation, block/unblock Services on Facilities and Destinations.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal
 */
public interface GeneralServiceManager {

	/**
	 * Bans Service on facility.
	 * It wouldn't be possible to execute the given Service on the whole facility nor on any of its destinations.
	 *
	 * @param perunSession
	 * @param service The Service to be banned on the facility
	 * @param facility The facility on which we want to ban the Service
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyBannedException
	 */
	public void blockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, ServiceAlreadyBannedException;

	/**
	 * Bans Service on destination.
	 * It wouldn't be possible to execute the given Service on this destination, however,
	 * it still can be executed on all the other destinations in the facility.
	 *
	 * @param perunSession
	 * @param service The Service to be banned on this particular destination
	 * @param destinationId The destination on which we want to ban the Service
	 * @throws InternalErrorException
	 */
	public void blockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws InternalErrorException;

	/**
	 * List all the Services that are banned on this facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return a list of Services that are denied on the facility
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * List all the Services that are banned on this destination.
	 *
	 * @param perunSession
	 * @param destinationId
	 * @return a list of Services that are denied on the destination
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	public List<Service> getServicesBlockedOnDestination(PerunSession perunSession, int destinationId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Is this Service denied on the facility?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param facility The facility on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the facility false - in
	 *         case the Service in NOT denied on the facility
	 */
	public boolean isServiceBlockedOnFacility(Service service, Facility facility);

	/**
	 * Is this Service denied on the destination?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param destinationId The destination on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the destination false - in case
	 *         the Service in NOT denied on the destination
	 */
	public boolean isServiceBlockedOnDestination(Service service, int destinationId);

	/**
	 * Erase all the possible denials on this facility.
	 * From this moment on, there are no Services being denied on this facility.
	 *
	 * @param perunSession
	 * @param facility Facility we want to clear of all the denials.
	 *
	 * @throws InternalErrorException
	 */
	public void unblockAllServicesOnFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Erase all the possible denials on this destination.
	 * From this moment on, there are no Services being denied on this destination.
	 *
	 * @param perunSession
	 * @param destinationId The id of a destination we want to clear of all the denials.
	 *
	 * @throws InternalErrorException
	 */
	public void unblockAllServicesOnDestination(PerunSession perunSession, int destinationId) throws InternalErrorException;

	/**
	 * Free the denial of the Service on this facility.
	 * If the Service was banned on this facility, it will be freed.
	 * In case the Service was not banned on this facility, nothing will happen.
	 *
	 * @param perunSession
	 * @param service The Service, the denial of which we want to free on this facility.
	 * @param facility The facility on which we want to free the denial of the Service.
	 *
	 * @throws InternalErrorException
	 */
	public void unblockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Free the denial of the Service on this destination.
	 * If the Service was banned on this destination, it will be freed.
	 * In case the Service was not banned on this destination, nothing will happen.
	 *
	 * @param perunSession
	 * @param service The Service, the denial of which we want to free on this destination.
	 * @param destinationId The id of a destination on which we want to free the denial of the Service.
	 *
	 * @throws InternalErrorException
	 */
	public void unblockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws InternalErrorException;

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
	 * Plans service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	boolean planServicePropagation(PerunSession perunSession, Facility facility, Service service) throws ServiceNotExistsException, FacilityNotExistsException, InternalErrorException, PrivilegeException;

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
	boolean planServicePropagation(PerunSession perunSession, Service service) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Return list of ServiceForGUI assigned on facility, (Service with "allowedOnFacility" property filled).
	 * 1 - allowed / 0 - service is denied).
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

}
