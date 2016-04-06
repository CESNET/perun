package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

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
	 * Creates new service.
	 *
	 * @param perunSession
	 * @param service
	 * @return new service
	 */
	Service createService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceExistsException;

	/** Deletes the service.
	 *
	 * @param perunSession
	 * @param service
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, RelationExistsException, ServiceAlreadyRemovedException;

	/** Updates the service.
	 *
	 * @param perunSession
	 * @param service
	 */
	void updateService(PerunSession perunSession, Service service) throws InternalErrorException;

	/**
	 * Get service by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return service with specified id
	 *
	 * @throws InternalErrorException
	 */
	Service getServiceById(PerunSession perunSession, int id) throws InternalErrorException, ServiceNotExistsException;

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
	Service getServiceByName(PerunSession perunSession, String name) throws InternalErrorException, ServiceNotExistsException;

	/**
	 * get all services in perun
	 *
	 * @param perunSession
	 * @return all services in perun
	 *
	 * @throws InternalErrorException
	 */
	List<Service> getServices(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get all services with given attribute.
	 *
	 * @param sess perun session
	 * @param attributeDefinition
	 * @return all services with given attribute
	 *
	 * @throws InternalErrorException
	 */
	List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Get all resources where the service is defined.
	 *
	 * @param sess
	 * @param service
	 * @return list of resources where the service is defined
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException;

	/**
	 * Generates the list of attributes per each member associated with the resource.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return attributes in special structure. Facility is in the root, facility children are resources. And resource children are members.
	 *
	 * @throws InternalErrorException
	 */
	ServiceAttributes getHierarchicalData(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Generates the list of attributes per each resource associated with the facility and filtered by service. Next it generates list of attributes
	 * associated with the facility and service.
	 *
	 * @param perunSession
	 * @param service you will get attributes required by this service
	 * @param facility you will get attributes for this facility, resources associated with it and users assigned to the resources
	 * @return attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's children are all resources. 
	 * 				Facility second child is abstract node with no attribute and it's children are all users.
	 *
	 * @throws InternalErrorException
	 */
	ServiceAttributes getFlatData(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * Generate also vo-required attributes for service. Add them to the same structure like resource-required attributes.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first child is abstract structure which children are groups.
	 *         Resource  second chi is abstract structure which children are members.
	 *         Group first child is abstract structure which children are groups.
	 *         Group second chi is abstract structure which children are members.
	 *
	 * @throws InternalErrorException
	 */
	ServiceAttributes getDataWithGroups(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;
	/**
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first child is abstract structure which children are groups.
	 *         Resource  second chi is abstract structure which children are members.
	 *         Group first child is abstract structure which children are groups.
	 *         Group second chi is abstract structure which children are members.
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 */
	ServiceAttributes getDataWithVos(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, VoNotExistsException;

	/**
	 * List packages
	 *
	 * @param perunSession
	 *
	 * @return list of packages in the DB
	 *
	 * @throws InternalErrorException
	 */
	public List<ServicesPackage> getServicesPackages(PerunSession perunSession) throws InternalErrorException;

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
	ServicesPackage getServicesPackageById(PerunSession perunSession, int servicesPackageId) throws InternalErrorException, ServicesPackageNotExistsException;

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
	ServicesPackage getServicesPackageByName(PerunSession perunSession, String name) throws InternalErrorException, ServicesPackageNotExistsException;

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
	ServicesPackage createServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageExistsException;

	/**
	 * Update package
	 *
	 * @param servicesPackage with which is the old one supposed to be updated :-)
	 * @param perunSession
	 *
	 * @throws InternalErrorException
	 */
	void updateServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Remove the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to be removed.
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	void deleteServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, RelationExistsException;

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
	public void addServiceToServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyAssignedException;
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
	public void removeServiceFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyRemovedFromServicePackageException;

	/**
	 * List services stored in the packages
	 *
	 * @param servicesPackage the package from which we want to list the services
	 *
	 * @return list consisting services
	 *
	 * @throws InternalErrorException
	 */
	public List<Service> getServicesFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

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
	void addRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeAlreadyAssignedException;

	/**
	 *  Batch version of addRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#addRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void addRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeAlreadyAssignedException;

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
	void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeNotAssignedException;

	/**
	 *  Batch version of removeRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void removeRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotAssignedException;

	/**
	 * Detate all required attributes from service.
	 *
	 * @param perunSession
	 * @param service
	 *
	 * @throws InternalErrorException
	 */
	void removeAllRequiredAttributes(PerunSession perunSession, Service service) throws InternalErrorException;

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
	Destination addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyAssignedException;

	/**
	 * Adds an destination for the facility and all services. Destination id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param services
	 * @param facility
	 * @param destination (id of this destination doesn't need to be filled.)
	 * @return destination with it's id set
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyAssignedException
	 */
	Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyAssignedException;

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
	List<Destination> addDestinationsForAllServicesOnFacility(PerunSession perunSession, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyAssignedException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, DestinationAlreadyAssignedException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) throws InternalErrorException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;


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
	void removeDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyRemovedException;

	/**
	 * Get destination by id
	 *
	 * @param perunSession
	 * @param id
	 * @return Destination with the id
	 * @throws InternalErrorException
	 * @throws DestinationNotExistsException
	 */
	Destination getDestinationById(PerunSession perunSession, int id) throws InternalErrorException, DestinationNotExistsException;

	/**
	 * Get list of all destinations defined for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return list list of destinations defined for the service and facility
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Get list of all destinations.
	 *
	 * @param perunSession
	 * @return list of all destinations for session
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get lists of all destinations for specific Facility
	 *
	 * @param perunSession
	 * @param facility the facility
	 * @return lists of all destinations for specific Facility
	 * @throws InternalErrorException
	 */
	List<Destination> getDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Get list of all rich destinations defined for the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of rich destinations defined for the facility
	 * @throws InternalErrorException
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Get list of all rich destinations defined for the service.
	 *
	 * @param perunSession
	 * @param service
	 * @return list of rich destinations defined for the service
	 * @throws InternalErrorException
	 */
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws InternalErrorException;

	/**
	 * Get list of all rich destinations defined for the service and the facility
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 * @return list of rich destinations defined for the service and the facility
	 * @throws InternalErrorException
	 */
	List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws InternalErrorException;

	/**
	 * Removes all defined destinations for the service and facility.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @throws InternalErrorException
	 */
	void removeAllDestinations(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Removes all defined destinations for the facility.
	 *
	 * @param perunSession
	 * @param facility the facility
	 * @throws InternalErrorException
	 */
	void removeAllDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Check if the service exits.
	 *
	 * @param sess
	 * @param service
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 */
	void checkServiceExists(PerunSession sess, Service service) throws InternalErrorException, ServiceNotExistsException;

	/**
	 * Check if the service package exists.
	 *
	 * @param sess
	 * @param servicesPackage
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	void checkServicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException;

	int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException;

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
	List<Service> getAssignedServices(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * List all destinations for all facilities which are joined by resources to the VO.
	 *
	 * @param sess
	 * @param vo vo for which we are searching destinations
	 * @return list of destinations
	 *
	 * @throws InternalErrorException
	 */
	List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get count of all destinations.
	 *
	 * @param perunSession
	 *
	 * @return count of all destinations
	 *
	 * @throws InternalErrorException
	 */
	int getDestinationsCount(PerunSession perunSession) throws InternalErrorException;
}
