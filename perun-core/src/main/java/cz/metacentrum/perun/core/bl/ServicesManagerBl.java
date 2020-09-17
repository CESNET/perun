package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

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
public interface ServicesManagerBl {

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
	void blockServiceOnFacility(PerunSession perunSession, Service service, Facility facility) throws ServiceAlreadyBannedException;

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
	void blockServiceOnDestination(PerunSession perunSession, Service service, int destinationId) throws PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException;

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
	void blockAllServicesOnDestination(PerunSession perunSession, int destinationId) throws PrivilegeException, DestinationNotExistsException;

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
	 * Erase all the possible denials on destinations defined by the destinationName.
	 * From this moment on, there are no Services being denied on these destinations.
	 *
	 * @param sess
	 * @param destinationName The name of destinations we want to clear of all the denials.
	 */
	void unblockAllServicesOnDestination(PerunSession sess, String destinationName);

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
	List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Creates new service.
	 *
	 * @param perunSession
	 * @param service
	 * @return new service
	 */
	Service createService(PerunSession perunSession, Service service) throws ServiceExistsException;

	/** Deletes the service.
	 *
	 * @param perunSession
	 * @param service
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteService(PerunSession perunSession, Service service) throws RelationExistsException, ServiceAlreadyRemovedException;

	/** Updates the service.
	 *
	 * @param perunSession
	 * @param service
	 */
	void updateService(PerunSession perunSession, Service service);

	/**
	 * Get service by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return service with specified id
	 *
	 * @throws InternalErrorException
	 */
	Service getServiceById(PerunSession perunSession, int id) throws ServiceNotExistsException;

	/**
	 * Get service by name.
	 *
	 * @param perunSession
	 * @param name name of the service
	 * @return service with specified name
	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	Service getServiceByName(PerunSession perunSession, String name) throws ServiceNotExistsException;

	/**
	 * get all services in perun
	 *
	 * @param perunSession
	 * @return all services in perun
	 *
	 * @throws InternalErrorException
	 */
	List<Service> getServices(PerunSession perunSession);

	/**
	 * Get all services with given attribute.
	 *
	 * @param sess perun session
	 * @param attributeDefinition
	 * @return all services with given attribute
	 *
	 * @throws InternalErrorException
	 */
	List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition);

	/**
	 * Get all resources where the service is defined.
	 *
	 * @param sess
	 * @param service
	 * @return list of resources where the service is defined
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Service service);

	/**
	 * Generates the list of attributes per each member associated with the resource.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take expired members into account
	 * @return attributes in special structure. Facility is in the root, facility children are resources. And resource children are members.
	 *
	 * @throws InternalErrorException
	 */
	ServiceAttributes getHierarchicalData(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers);

	/**
	 * Generates hashed hierarchical data structure for given service and resource.
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *    ** facility **
	 *    hashes: [...hashes...]
	 *    members: []
	 *    children: [
	 *      {
	 *        ** resource1 **
	 *        hashes: [...hashes...]
	 *        children: []
	 *        members: [
	 *          {
	 *            ** member 1 **
	 *            hashes: [...hashes...]
	 *          },
	 *          {
	 *            ** member 2 **
	 *            ...
	 *          }
	 *        ]
	 *      },
	 *      {
	 *        ** resource2 **
	 *        ...
	 *      }
	 *    ]
	 * }
	 *
	 * @param perunSession perun session
	 * @param service service
	 * @param facility facility
	 * @param filterExpiredMembers if the generator should filter expired members
	 * @return generated hashed data structure
	 */
	HashedGenData getHashedHierarchicalData(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers);

	/**
	 * Generates hashed data with group structure for given service and resource.
	 *
	 * Generates data in format:
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *    ** facility **
	 *    hashes: [...hashes...]
	 *    members: []
	 *    children: [
	 *      {
	 *        ** resource1 **
	 *        hashes: [...hashes...]
	 *        children: [
	 *          {
	 *            ** group A **
	 *            hashes: [...hashes...]
	 *            members: [...group members...]
	 *            children: []
	 *          },
	 *          {
	 *            ** group B **
	 *            ...
	 *          }
	 *        ]
	 *        members: [
	 *          {
	 *            ** member 1 **
	 *            hashes: [...hashes...]
	 *          },
	 *          {
	 *            ** member 2 **
	 *            ...
	 *          }
	 *        ]
	 *      },
	 *      {
	 *        ** resource2 **
	 *        ...
	 *      }
	 *    ]
	 * }
	 *
	 * @param perunSession perun session
	 * @param service service
	 * @param facility facility
	 * @param filterExpiredMembers if the generator should filter expired members
	 * @return generated hashed data structure
	 */
	HashedGenData getHashedDataWithGroups(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers);

	/**
	 * Generates the list of attributes per each resource associated with the facility and filtered by service. Next it generates list of attributes
	 * associated with the facility and service.
	 *
	 * @param perunSession
	 * @param service you will get attributes required by this service
	 * @param facility you will get attributes for this facility, resources associated with it and users assigned to the resources
	 * @param filterExpiredMembers if true the method does not take expired members into account
	 * @return attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's children are all resources.
	 * 				Facility second child is abstract node with no attribute and it's children are all users.
	 *
	 * @throws InternalErrorException
	 */
	ServiceAttributes getFlatData(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers);

	/**
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * Generate also vo-required attributes for service. Add them to the same structure like resource-required attributes.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take expired members into account
	 * @return attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first child is abstract structure which children are groups.
	 *         Resource  second child is abstract structure which children are members.
	 *         Group first child is empty structure (services expect members to be second child, here used to be subgroups).
	 *         Group second child is abstract structure which children are members.
	 */
	ServiceAttributes getDataWithGroups(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers);
	/**
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take expired members into account
	 * @return attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first child is abstract structure which children are groups.
	 *         Resource  second chi is abstract structure which children are members.
	 *         Group first child is abstract structure which children are groups.
	 *         Group second chi is abstract structure which children are members.
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 */
	ServiceAttributes getDataWithVos(PerunSession perunSession, Service service, Facility facility, boolean filterExpiredMembers) throws VoNotExistsException;

	/**
	 * List packages
	 *
	 * @param perunSession
	 *
	 * @return list of packages in the DB
	 *
	 * @throws InternalErrorException
	 */
	List<ServicesPackage> getServicesPackages(PerunSession perunSession);

	/**
	 * Get package by Id
	 *
	 * @param servicesPackageId id of the package we want to retrieve
	 * @param perunSession
	 *
	 * @return package
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	ServicesPackage getServicesPackageById(PerunSession perunSession, int servicesPackageId) throws ServicesPackageNotExistsException;

	/**
	 * Get package by name
	 *
	 * @param name name of the services package
	 * @param perunSession
	 *
	 * @return package
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	ServicesPackage getServicesPackageByName(PerunSession perunSession, String name) throws ServicesPackageNotExistsException;

	/**
	 * Insert a new package
	 *
	 * @param servicesPackage package to be inserted
	 * @param perunSession
	 *
	 * @return ServicesPackage object completely filled (including Id)
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageExistsException
	 */
	ServicesPackage createServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws ServicesPackageExistsException;

	/**
	 * Update package
	 *
	 * @param servicesPackage with which is the old one supposed to be updated :-)
	 * @param perunSession
	 *
	 * @throws InternalErrorException
	 */
	void updateServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage);

	/**
	 * Remove the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to be removed.
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	void deleteServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws RelationExistsException;

	/**
	 * Add the service to the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to which the service supposed to be added
	 * @param service service to be added to the services package
	 *
	 * @throws ServiceAlreadyAssignedException
	 * @throws InternalErrorException
	 */
	void addServiceToServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws ServiceAlreadyAssignedException;
	/**
	 * Remove Service from Services Package
	 *
	 * @param perunSession
	 * @param servicesPackage services package from which the service supposed to be removed
	 * @param service service that will be removed from the services package
	 *
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyRemovedFromServicePackageException there are 0 rows affected by removing service from servicePackage in DB
	 */
	void removeServiceFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws ServiceAlreadyRemovedFromServicePackageException;

	/**
	 * List services stored in the packages
	 *
	 * @param servicesPackage the package from which we want to list the services
	 *
	 * @return list consisting services
	 *
	 * @throws InternalErrorException
	 */
	List<Service> getServicesFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage);

	/*
		 getRequiredAttributes(PerunSession perunSession, Service service);
		 */

	/**
	 * Mark the attribute as required for the service. Required attributes are requisite for Service to run.
	 * If you add attribute which has a default attribute then this default attribute will be automatically add too.
	 *
	 * @param perunSession
	 * @param service
	 * @param attribute
	 *
	 * @throws InternalErrorException
	 * @throws AttributeAlreadyAssignedException
	 */
	void addRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws AttributeAlreadyAssignedException;

	/**
	 *  Batch version of addRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#addRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void addRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws AttributeAlreadyAssignedException;

	/**
	 * Remove required attribute from service.
	 * TODO If you try to remove attribute which is default for other Required attribute ...
	 *
	 * @param perunSession
	 * @param service
	 * @param attribute
	 *
	 * @throws InternalErrorException
	 * @throws AttributeNotAssignedException
	 */
	void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws AttributeNotAssignedException;

	/**
	 *  Batch version of removeRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void removeRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws AttributeNotAssignedException;

	/**
	 * Detate all required attributes from service.
	 *
	 * @param perunSession
	 * @param service
	 *
	 * @throws InternalErrorException
	 */
	void removeAllRequiredAttributes(PerunSession perunSession, Service service);

	/**
	 * Adds an destination for the facility and service. Destination.id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @param destination (Id of this destination doesn't need to be filled.)
	 * @return destination with it's id set.
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyAssignedException
	 */
	Destination addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws DestinationAlreadyAssignedException;

	/**
	 * Adds an destination for the facility and all services. Destination id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param services
	 * @param facility
	 * @param destination (id of this destination doesn't need to be filled.)
	 * @return destination with it's id set
	 * @throws InternalErrorException
	 */
	Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination);

	/**
	 * Adds destination for all services defined on the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param destination
	 * @return list of added destinations
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyAssignedException
	 */
	List<Destination> addDestinationsForAllServicesOnFacility(PerunSession perunSession, Facility facility, Destination destination) throws DestinationAlreadyAssignedException;

	/**
	 * Defines service destination for all hosts using theirs hostnames.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return list of added destinations
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyAssignedException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws DestinationAlreadyAssignedException;

	/**
	 * Defines services destination for all hosts using their hostnames.
	 * Do it for all services in List.
	 *
	 * If some destination for service and facility already exist, do not create it but still return back in the list.
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of added destinations (even if they already was added before)
	 * @throws InternalErrorException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility);

	/**
	 * Defines services destination for all hosts using their hostnames.
	 * Use all assigned services to resources for the facility.
	 *
	 * If some destination for service and facility already exist, do not create it but still return back in the list.
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of added destinations (even if they already was added before)
	 * @throws InternalErrorException
	 */
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility);


	/**
	 * Removes an destination from the facility and service.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @param destination string contains destination address (mail, url, hostname, ...)
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyRemovedException
	 */
	void removeDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws DestinationAlreadyRemovedException;

	/**
	 * Get destination by id
	 *
	 * @param perunSession
	 * @param id
	 * @return Destination with the id
	 * @throws InternalErrorException
	 * @throws DestinationNotExistsException
	 */
	Destination getDestinationById(PerunSession perunSession, int id) throws DestinationNotExistsException;

	/**
	 * Get list of all destinations defined for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return list list of destinations defined for the service and facility
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession, Service service, Facility facility);

	/**
	 * Get list of all destinations.
	 *
	 * @param perunSession
	 * @return list of all destinations for session
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession);

	/**
	 * Get lists of all destinations for specific Facility
	 *
	 * @param perunSession
	 * @param facility the facility
	 * @return lists of all destinations for specific Facility
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession, Facility facility);

	/**
	 * Get list of all rich destinations defined for the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of rich destinations defined for the facility
	 * @throws InternalErrorException
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility);

	/**
	 * Get list of all rich destinations defined for the service.
	 *
	 * @param perunSession
	 * @param service
	 * @return list of rich destinations defined for the service
	 * @throws InternalErrorException
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service);

	/**
	 * Get list of all rich destinations defined for the service and the facility
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return list of rich destinations defined for the service and the facility
	 * @throws InternalErrorException
	 */
	List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service);

	/**
	 * Removes all defined destinations for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @throws InternalErrorException
	 */
	void removeAllDestinations(PerunSession perunSession, Service service, Facility facility);

	/**
	 * Removes all defined destinations for the facility.
	 *
	 * @param perunSession
	 * @param facility the facility
	 * @throws InternalErrorException
	 */
	void removeAllDestinations(PerunSession perunSession, Facility facility);

	/**
	 * Check if the service exits.
	 *
	 * @param sess
	 * @param service
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	void checkServiceExists(PerunSession sess, Service service) throws ServiceNotExistsException;

	/**
	 * Check if the service package exists.
	 *
	 * @param sess
	 * @param servicesPackage
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	void checkServicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException;

	/**
	 * Returns Destinations ID based on destination name and type.
	 *
	 * @param sess
	 * @param name Name (value) of destination
	 * @param type type of destination
	 * @return
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
	 *
	 * @return list of services assigned  to facility
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Facility facility);

	/**
	 * List all destinations for all facilities which are joined by resources to the VO.
	 *
	 * @param sess
	 * @param vo vo for which we are searching destinations
	 * @return list of destinations
	 *
	 * @throws InternalErrorException
	 */
	List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo);

	/**
	 * Get count of all destinations.
	 *
	 * @param perunSession
	 *
	 * @return count of all destinations
	 *
	 * @throws InternalErrorException
	 */
	int getDestinationsCount(PerunSession perunSession);
}
