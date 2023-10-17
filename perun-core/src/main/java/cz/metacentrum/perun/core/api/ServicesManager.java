package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ForceServicePropagationDisabledException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAttributesCannotExtend;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceIsNotBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;

import java.util.List;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 *
 * Note: ServicesManager is not to be used directly by any client.
 * ServicesManager's functionality is going to be encapsulated in the Controller's
 * GeneralServiceManager.
 *
 */
public interface ServicesManager {

	String SERVICE_NAME_REGEXP = "^[a-zA-Z0-9_]+$";



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
	void blockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws ServiceAlreadyBannedException, PrivilegeException;

	/**
	 * Bans services on facility.
	 * It wouldn't be possible to execute the given services on the whole facility nor on any of their destinations.
	 *
	 * @param sess perun session
	 * @param services list of services
	 * @param facility The facility on which we want to ban services
	 *
	 * @throws PrivilegeException insufficient permissions
	 * @throws ServiceAlreadyBannedException when service was already banned
	 * @throws FacilityNotExistsException when facility does not exist
	 */
	void blockServicesOnFacility(PerunSession sess, List<Service> services, Facility facility) throws ServiceAlreadyBannedException, PrivilegeException, FacilityNotExistsException;

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
	void blockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException, FacilityNotExistsException;

	/**
	 * Bans the Service on the destination - each pair defined by the rich destination.
	 * It wouldn't be possible to execute the given Service on this destination, however,
	 * it still can be executed on all the other destinations in the facility.
	 *
	 * @param perunSession
	 * @param richDestinations the list of rich destinations
	 *
	 */
	void blockServicesOnDestinations(PerunSession perunSession, List<RichDestination> richDestinations) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException;

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
	void blockAllServicesOnFacility(PerunSession perunSession, Facility facility) throws FacilityNotExistsException, PrivilegeException;

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
	void blockAllServicesOnDestination(PerunSession perunSession, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException;

	/**
	 * List all the Services that are banned on this facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return a list of Services that are denied on the facility
	 *
	 */
	List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException;

	/**
	 * List all the Services that are banned on this destination.
	 *
	 * @param perunSession
	 * @param destinationId
	 * @return a list of Services that are denied on the destination
	 *
	 */
	List<Service> getServicesBlockedOnDestination(PerunSession perunSession, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException;

	/**
	 * Is this Service denied on the facility?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param facility The facility on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the facility false - in
	 *         case the Service in NOT denied on the facility
	 */
	boolean isServiceBlockedOnFacility(PerunSession sess, Service service, Facility facility) throws PrivilegeException;

	/**
	 * Is this Service denied on the destination?
	 *
	 * @param service The Service, the denial of which we want to examine
	 * @param destinationId The destination on which we want to look up the denial of the Service
	 * @return true - in case the Service is denied on the destination false - in case
	 *         the Service in NOT denied on the destination
	 */
	boolean isServiceBlockedOnDestination(PerunSession sess, Service service, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException;

	/**
	 * Erase all the possible denials on this facility.
	 * From this moment on, there are no Services being denied on this facility.
	 *
	 * @param perunSession
	 * @param facility Facility we want to clear of all the denials.
	 *
	 */
	void unblockAllServicesOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException;

	/**
	 * Erase all the possible denials on destinations defined by the destinationName.
	 * From this moment on, there are no Services being denied on these destinations.
	 *
	 * @param sess
	 * @param destinationName The name of destinations we want to clear of all the denials.
	 */
	void unblockAllServicesOnDestination(PerunSession sess, String destinationName) throws FacilityNotExistsException, PrivilegeException;

	/**
	 * Erase all the possible denials on this destination.
	 * From this moment on, there are no Services being denied on this destination.
	 *
	 * @param perunSession
	 * @param destinationId The id of a destination we want to clear of all the denials.
	 *
	 */
	void unblockAllServicesOnDestination(PerunSession perunSession, int destinationId) throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException;

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
	void unblockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException;

	/**
	 * Free the denial of the services on this facility.
	 *
	 * @param sess perun session
	 * @param services list of services
	 * @param facility facility
	 *
	 * @throws PrivilegeException insufficient permissions
	 * @throws FacilityNotExistsException when facility does not exist
	 * @throws ServiceIsNotBannedException when unblocking service which is not blocked
	 */
	void unblockServicesOnFacility(PerunSession sess, List<Service> services, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceIsNotBannedException;

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
	void unblockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException;

	/**
	 * Free the denial of the Service on the destination - each pair defined by the rich destination.
	 * If the Service was banned on the destination, it will be freed.
	 * In case the Service was not banned on the destination, nothing will happen.
	 *
	 * @param perunSession
	 * @param richDestinations the list of rich destinations
	 *
	 */
	void unblockServicesOnDestinations(PerunSession perunSession, List<RichDestination> richDestinations) throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException;

	/**
	 * Forces service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return true if it is possible, false if not
	 *
	 */
	boolean forceServicePropagation(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException;

	/**
	 * Forces services propagation on defined facility.
	 *
	 * @param sess perun session
	 * @param services list of services
	 * @param facility facility
	 *
	 * @throws FacilityNotExistsException when facility does not exist
	 * @throws PrivilegeException insufficient permissions
	 * @throws ForceServicePropagationDisabledException when forcing propagation is not possible
	 */
	void forceServicePropagationBulk(PerunSession sess, Facility facility, List<Service> services) throws PrivilegeException, FacilityNotExistsException, ForceServicePropagationDisabledException;

	/**
	 * Forces service propagation on all facilities where the service is defined on.
	 *
	 * @param perunSession
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean forceServicePropagation(PerunSession perunSession, Service service) throws PrivilegeException;

	/**
	 * Forces services propagation on all facilities where the services are defined on.
	 *
	 * @param sess session
	 * @param services list of services
	 *
	 * @throws PrivilegeException insufficient permissions
	 * @throws ForceServicePropagationDisabledException when forcing propagation is not possible
	 */
	void forceServicePropagationBulk(PerunSession sess, List<Service> services) throws PrivilegeException, ForceServicePropagationDisabledException;

	/**
	 * Plans service propagation on defined facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean planServicePropagation(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException;

	/**
	 * Forces service propagation on all facilities where the service is defined on.
	 *
	 * @param perunSession
	 * @param service
	 * @return true if it is possible, false if not
	 *
	 */
	boolean planServicePropagation(PerunSession perunSession, Service service) throws PrivilegeException;

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
	List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException;



	/**
	 * Creates new service.
	 *
	 * @param perunSession
	 * @param service
	 * @return new service
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServiceExistsException
	 */
	Service createService(PerunSession perunSession, Service service) throws PrivilegeException, ServiceExistsException;

	/**
	 * Deletes the service.
	 *
	 * @param perunSession
	 * @param service
	 * @param forceFlag if true, removes all dependant objects from database instead of raising exception
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteService(PerunSession perunSession, Service service, boolean forceFlag) throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException;

	/**
	 * Deletes given services.
	 *
	 * @param perunSession
	 * @param services list of services
	 * @param forceFlag if true, removes all dependant objects from database instead of raising exception
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteServices(PerunSession perunSession, List<Service> services, boolean forceFlag) throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException;

	/** Updates the service.
	 *
	 * @param perunSession
	 * @param service
	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 */
	void updateService(PerunSession perunSession, Service service) throws ServiceNotExistsException, PrivilegeException;

	/**
	 * Get service by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return service with specified id
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 */
	Service getServiceById(PerunSession perunSession, int id) throws PrivilegeException, ServiceNotExistsException;

	/**
	 * Get service by name.
	 *
	 * @param perunSession
	 * @param name name of the service
	 * @return service with specified name
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 */
	Service getServiceByName(PerunSession perunSession, String name) throws PrivilegeException, ServiceNotExistsException;

	/**
	 * get all services in perun
	 *
	 * @param perunSession
	 * @return all services in perun
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Service> getServices(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Get all services with given attribute.
	 *
	 * @param sess perun session
	 * @param attributeDefinition
	 * @return all services with given attribute
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws PrivilegeException;

	/**
	 * Get all resources where the service is defined.
	 *
	 * @param sess
	 * @param service
	 * @return list of resources where the service is defined
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Service service) throws PrivilegeException, ServiceNotExistsException;

	/**
	 * Generates hashed hierarchical data structure for given service and facility.
	 * If enforcing consents is turned on on the instance and on the resource's consent hub,
	 * generates only the users that granted a consent to all the service required attributes.
	 * New UNSIGNED consents are created to users that don't have a consent containing all the
	 * service required attributes.
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         children: [],
	 *         voId: 99,
	 *         members: {    ** all members on the resource with id 2 **
	 *           "4" : 5    ** member id : user id **
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 *
	 * @param perunSession perun session
	 * @param service service
	 * @param facility facility
	 * @param consentEval if the generator should force evaluation of consents
	 * @return generated hashed data structure
	 * @throws FacilityNotExistsException if there is no such facility
	 * @throws ServiceNotExistsException if there is no such service
	 * @throws PrivilegeException insufficient permissions
	 */
	HashedGenData getHashedHierarchicalData(PerunSession perunSession, Service service, Facility facility, boolean consentEval) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

	/**
	 * Generates hashed data with group structure for given service and facility.
	 * If enforcing consents is turned on on the instance and on the resource's consent hub,
	 * generates only the users that granted a consent to all the service required attributes.
	 * New UNSIGNED consents are created to users that don't have a consent containing all the
	 * service required attributes.
	 *
	 *  Generates data in format:
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         voId: 99,
	 *         children: [
	 *           "89": {    ** group id **
	 *              "children": {},
	 *              "members": {
	 *                  "91328": 57986,
	 *                  "91330": 60838
	 *              }
	 *           }
	 *         ],
	 *         "members": {    ** all members on the resource with id 2 **
	 *             "91328": 57986,
	 *             "91330": 60838
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 * @param perunSession perun session
	 * @param service service
	 * @param facility facility
	 * @param consentEval if the generator should force evaluation of consents
	 * @return generated hashed data structure
	 * @throws FacilityNotExistsException if there is no such facility
	 * @throws ServiceNotExistsException if there is no such service
	 * @throws PrivilegeException insufficient permissions
	 */
	HashedGenData getHashedDataWithGroups(PerunSession perunSession, Service service, Facility facility, boolean consentEval) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

	/**
	 * List packages
	 *
	 * @param perunSession
	 *
	 * @return list of packages in the DB
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<ServicesPackage> getServicesPackages(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Get package by Id
	 *
	 * @param servicesPackageId id of the package we want to retrieve
	 * @param perunSession
	 *
	 * @return package
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 */
	ServicesPackage getServicesPackageById(PerunSession perunSession, int servicesPackageId) throws ServicesPackageNotExistsException, PrivilegeException;

	/**
	 * Get package by name
	 *
	 * @param name name of the services package
	 * @param perunSession
	 *
	 * @return package
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 */
	ServicesPackage getServicesPackageByName(PerunSession perunSession, String name) throws ServicesPackageNotExistsException, PrivilegeException;

	/**
	 * Insert a new package
	 *
	 * @param servicesPackage package to be inserted
	 * @param perunSession
	 *
	 * @return ServicesPackage object completely filled (including Id)
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageExistsException
	 */
	ServicesPackage createServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws PrivilegeException, ServicesPackageExistsException;

	/**
	 * Update package
	 *
	 * @param servicesPackage with which is the old one supposed to be updated :-)
	 * @param perunSession
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 */
	void updateServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException;

	/**
	 * Remove the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to be removed.
	 * @throws ServicesPackageNotExistsException
	 * @throws RelationExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	void deleteServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException, RelationExistsException;

	/**
	 * Add the service to the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to which the service supposed to be added
	 * @param service service to be added to the services package
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws ServiceAlreadyAssignedException
	 */
	void addServiceToServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws ServicesPackageNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, PrivilegeException;

	/**
	 * Remove Service from Services Package
	 *
	 * @param perunSession
	 * @param servicesPackage services package from which the service supposed to be removed
	 * @param service service that will be removed from the services package
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 * @throws ServiceAlreadyRemovedFromServicePackageException there are 0 rows affected by removing service from servicePackage in DB
	 * @throws ServiceNotExistsException
	 */
	void removeServiceFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyRemovedFromServicePackageException;

	/**
	 * List services stored in the packages
	 *
	 * @param servicesPackage the package from which we want to list the services
	 *
	 * @return list consisting services
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 */
	List<Service> getServicesFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException;

	/* TODO createPackage
		 getRequiredAttributes(PerunSession perunSession, Service service);
		 */

	/**
	 * Mark the attribute as required for the service. Required attribues are requisite for Service to run.
	 * If you add attribute which has a default attribute then this default attribute will be automatically add too.
	 *
	 * @param perunSession perunSession
	 * @param service service to which the attribute will be added
	 * @param attribute attribute to add
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlaying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 * @throws AttributeAlreadyAssignedException if the attribute is already added
	 * @throws ServiceAttributesCannotExtend if trying to add user-related attribute that could invalidate consents
	 */
	void addRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException, ServiceAttributesCannotExtend;

	/**
	 *  Batch version of addRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#addRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void addRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException, ServiceAttributesCannotExtend;

	/**
	 * Remove required attribute from service.
	 * TODO If you try to remove attribute which is default for other Required attribute ...
	 *
	 * @param perunSession perunSession
	 * @param service service from which the attribute will be removed
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException
	 * @throws AttributeNotAssignedException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 */
	void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException;

	/**
	 *  Batch version of removeRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void removeRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException;

	/**
	 * Detate all required attributes from service
	 *
	 * @param perunSession perunSession
	 * @param service service from which the attributes will be removed
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 */
	void removeAllRequiredAttributes(PerunSession perunSession, Service service) throws PrivilegeException, ServiceNotExistsException;

	/**
	 * Adds an destination for the facility and service. Destination.id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @param destination (Id of this destination doesn't need to be filled.)
	 * @return destination with it's id set.
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 * @throws WrongPatternException
	 */
	Destination addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException;

	/**
	 * Adds an destination for the facility and all services. Destination id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param services
	 * @param facility
	 * @param destination (id of this destination doesn't need to be filled.)
	 * @return destination with it's id set
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws WrongPatternException
	 */
	Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, WrongPatternException;

	/**
	 * Adds destination for all services defined on the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param destination
	 * @return list of added destinations
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 * @throws WrongPatternException
	 */
	List<Destination> addDestinationsForAllServicesOnFacility(PerunSession perunSession, Facility facility, Destination destination) throws PrivilegeException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException;

	/**
	 * Defines service destination for all hosts using theirs hostnames.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return list of added destinations
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException;

	/**
	 * Defines services destination for all hosts using their hostnames.
	 * Do it for all services in List.
	 *
	 * If some destination for service and facility already exist, do not create it but still return back in the list.
	 *
	 * @param perunSession
	 * @param services
	 * @param facility
	 * @return list of added destinations (even if they already was added before)
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException;

	/**
	 * Defines services destination for all hosts using their hostnames.
	 * Use all assigned services to resources for the facility.
	 *
	 * If some destination for service and facility already exist, do not create it but still return back in the list.
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of added destinations (even if they already was added before)
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Removes an destination from the facility and service.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @param destination string contains destination address (mail, url, hostname, ...)
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyRemovedException
	 */
	void removeDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyRemovedException;

	/**
	 * Removes destinations defined by list of rich destinations.
	 * Each destination is removed from the rich destination's facility and service.
	 *
	 * @param perunSession
	 * @param richDestinations list of rich destinations
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyRemovedException
	 */
	void removeDestinationsByRichDestinations(PerunSession perunSession, List<RichDestination> richDestinations) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyRemovedException;

	/**
	 * Get destination by id
	 *
	 * @param perunSession
	 * @param id
	 * @return Destination with the id
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws DestinationNotExistsException
	 */
	Destination getDestinationById(PerunSession perunSession, int id) throws PrivilegeException, DestinationNotExistsException;

	/**
	 * Get list of all destinations defined for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return list list of destinations defined for the service and facility
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 */
	List<Destination> getDestinations(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException;

	/**
	 * Get list of all destinations.
	 *
	 * @param perunSession
	 * @return list of all destinations for session
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Destination> getDestinations(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Get list of all rich destinations defined for the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws FacilityNotExistsException if facility which we get from service and destination not exist
	 * @throws ServiceNotExistsException if the service not exist
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Get list of all rich destinations defined for the service.
	 *
	 * @param perunSession
	 * @param service
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws FacilityNotExistsException if the facility not exist
	 * @throws ServiceNotExistsException  if service which we get from facility and destination not exist
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws PrivilegeException, ServiceNotExistsException;

	/**
	 * Get list of all rich destinations defined for the facility and the service.
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws FacilityNotExistsException if the facility not exist
	 * @throws ServiceNotExistsException if the service not exist
	 */
	List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

	/**
	 * Removes all defined destinations for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 */
	void removeAllDestinations(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException;

	/**
	 * Return ID of Destination by its value (name) and type.
	 *
	 * @param sess
	 * @param name Name (value) of Destination
	 * @param type Type of destination
	 * @return ID of Destination
	 * @throws InternalErrorException
	 * @throws DestinationNotExistsException
	 */
	int getDestinationIdByName(PerunSession sess, String name, String type) throws DestinationNotExistsException;

	/**
	 * List all services associated with the facility (via resource).
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws PrivilegeException
	 *
	 * @return list of services assigned to facility
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Facility facility) throws FacilityNotExistsException, PrivilegeException;

	/**
	 * List all services associated with the facility and vo (via resource).
	 *
	 * @param perunSession
	 * @param facility
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 *
	 * @return list of services assigned to facility and vo
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Facility facility, Vo vo) throws FacilityNotExistsException, VoNotExistsException, PrivilegeException;

	/**
	 * List all destinations for all facilities which are joined by resources to the VO.
	 *
	 * @param sess
	 * @param vo vo for which we are searching destinations
	 * @return list of destinations
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 */
	List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws VoNotExistsException;

	/**
	 * Get count of all destinations.
	 *
	 * @param sess PerunSession
	 *
	 * @throws InternalErrorException
	 * @return count of all destinations
	 */
	int getDestinationsCount(PerunSession sess);
}
