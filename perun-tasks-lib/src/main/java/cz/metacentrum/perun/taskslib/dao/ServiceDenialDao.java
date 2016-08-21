package cz.metacentrum.perun.taskslib.dao;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;

/**
 * Management of Service denials (block/unblock services on facility/destination).
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal
 */
public interface ServiceDenialDao {

	/**
	 * Block Service on Facility. It won't be possible to propagate service on whole facility
	 * or any of its destinations.
	 *
	 * @param serviceId The Service to be blocked on the Facility
	 * @param facilityId The Facility on which we want to block the Service
	 * @throws InternalErrorException
	 */
	void blockServiceOnFacility(int serviceId, int facilityId) throws InternalErrorException;

	/**
	 * Block Service on specific Destination. Service still can be propagated to other facility Destinations.
	 *
	 * @param serviceId The Service to be blocked on this particular destination
	 * @param destinationId The destination on which we want to block the Service
	 * @throws InternalErrorException
	 */
	void blockServiceOnDestination(int serviceId, int destinationId) throws InternalErrorException;

	/**
	 * Unblock Service on whole Facility. If was not blocked, nothing happens.
	 *
	 * @param serviceId ID of Service to unblock on Facility.
	 * @param facilityId ID of Facility to unblock Service on.
	 */
	void unblockServiceOnFacility(int serviceId, int facilityId);

	/**
	 * Unblock Service on specific Destination. If was not blocked, nothing happens.
	 *
	 * @param serviceId ID of Service to unblock on Destination.
	 * @param destinationId ID of Destination to unblock Service on.
	 */
	void unblockServiceOnDestination(int serviceId, int destinationId);

	/**
	 * Unblock all blocked Services on Facility.
	 *
	 * @param facility ID of Facility we want to unblock all Services.
	 */
	void unblockAllServicesOnFacility(int facility);

	/**
	 * Unblock all blocked Services on specified Destination.
	 *
	 * @param destination ID of Destination we want to unblock all Services.
	 */
	void unblockAllServicesOnDestination(int destination);

	/**
	 * Get Services blocked on Facility.
	 *
	 * @param facilityId ID of Facility to get blocked Services for.
	 * @return List of blocked Services.
	 */
	List<Service> getServicesBlockedOnFacility(int facilityId);

	/**
	 * Get Services blocked on Destination.
	 *
	 * @param destinationId ID of Destination to get blocked Services for.
	 * @return List of blocked Services.
	 */
	List<Service> getServicesBlockedOnDestination(int destinationId);

	/**
	 * Return TRUE if Service is blocked on Facility.
	 *
	 * @param serviceId ID of Service to check on.
	 * @param facilityId ID of Facility to check on.
	 * @return TRUE if Service is blocked on Facility / FALSE otherwise
	 */
	boolean isServiceBlockedOnFacility(int serviceId, int facilityId);

	/**
	 * Return TRUE if Service is blocked on Destination.
	 *
	 * @param serviceId ID of Service to check on.
	 * @param destinationId ID of Destination to check on.
	 * @return TRUE if Service is blocked on Destination / FALSE otherwise
	 */
	boolean isServiceBlockedOnDestination(int serviceId, int destinationId);

}
