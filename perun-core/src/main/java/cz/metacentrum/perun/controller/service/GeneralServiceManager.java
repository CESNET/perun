package cz.metacentrum.perun.controller.service;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;

import java.util.List;

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
	void blockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, ServiceAlreadyBannedException;

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
	void blockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws InternalErrorException, PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException;

	/**
	 * Block all services currently assigned on this facility.
	 * From this moment on, there are no Services being allowed on this facility.
	 * If you assign a new service to the facility, it will be allowed!
	 *
	 * @param perunSession
	 * @param facility Facility we want to block all services on.
	 *
	 * @throws InternalErrorException
	 */
	void blockAllServicesOnFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;

	/**
	 * Block all services currently assigned on this destination.
	 * From this moment on, there are no Services being allowed on this destination.
	 * If you assign a new service to the destination, it will be allowed!
	 *
	 * @param perunSession
	 * @param destinationId The id of a destination we want to block all services on.
	 *
	 * @throws InternalErrorException
	 */
	void blockAllServicesOnDestination(PerunSession perunSession, int destinationId) throws InternalErrorException, PrivilegeException, DestinationNotExistsException;

	/**
	 * List all the Services that are banned on this facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return a list of Services that are denied on the facility
	 *
	 */
	List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility);

	/**
	 * List all the Services that are banned on this destination.
	 *
	 * @param perunSession
	 * @param destinationId
	 * @return a list of Services that are denied on the destination
	 *
	 */
	List<Service> getServicesBlockedOnDestination(PerunSession perunSession, int destinationId);

	/**
	 * Is this Service denied on the facility?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param facility The facility on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the facility false - in
	 *         case the Service in NOT denied on the facility
	 */
	boolean isServiceBlockedOnFacility(Service service, Facility facility);

	/**
	 * Is this Service denied on the destination?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param destinationId The destination on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the destination false - in case
	 *         the Service in NOT denied on the destination
	 */
	boolean isServiceBlockedOnDestination(Service service, int destinationId);

	/**
	 * Erase all the possible denials on this facility.
	 * From this moment on, there are no Services being denied on this facility.
	 *
	 * @param perunSession
	 * @param facility Facility we want to clear of all the denials.
	 *
	 */
	void unblockAllServicesOnFacility(PerunSession perunSession, Facility facility);

	/**
	 * Erase all the possible denials on this destination.
	 * From this moment on, there are no Services being denied on this destination.
	 *
	 * @param perunSession
	 * @param destinationId The id of a destination we want to clear of all the denials.
	 *
	 */
	void unblockAllServicesOnDestination(PerunSession perunSession, int destinationId);

	/**
	 * Free the denial of the Service on this facility.
	 * If the Service was banned on this facility, it will be freed.
	 * In case the Service was not banned on this facility, nothing will happen.
	 *
	 * @param perunSession
	 * @param service The Service, the denial of which we want to free on this facility.
	 * @param facility The facility on which we want to free the denial of the Service.
	 *
	 */
	void unblockServiceOnFacility(PerunSession perunSession, Service service, Facility facility);

	/**
	 * Free the denial of the Service on this destination.
	 * If the Service was banned on this destination, it will be freed.
	 * In case the Service was not banned on this destination, nothing will happen.
	 *
	 * @param perunSession
	 * @param service The Service, the denial of which we want to free on this destination.
	 * @param destinationId The id of a destination on which we want to free the denial of the Service.
	 *
	 */
	void unblockServiceOnDestination(PerunSession perunSession, Service service, int destinationId);

	/**
	 * Forces service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return true if it is possible, false if not
	 *
	 */
	boolean forceServicePropagation(PerunSession perunSession, Facility facility, Service service);

	/**
	 * Forces service propagation on all facilities where the service is defined on.
	 *
	 * @param perunSession
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean forceServicePropagation(PerunSession perunSession, Service service);

	/**
	 * Plans service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean planServicePropagation(PerunSession perunSession, Facility facility, Service service);

	/**
	 * Forces service propagation on all facilities where the service is defined on.
	 *
	 * @param perunSession
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean planServicePropagation(PerunSession perunSession, Service service);

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
