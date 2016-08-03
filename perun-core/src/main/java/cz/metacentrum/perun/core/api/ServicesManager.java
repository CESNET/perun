package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
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
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;

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

	public static final String SERVICE_NAME_REGEXP = "^[a-zA-Z0-9_]+$";

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
	Service createService(PerunSession perunSession, Service service) throws InternalErrorException, PrivilegeException, ServiceExistsException;

	/**
	 * Deletes the service.
	 *
	 * @param perunSession
	 * @param service
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException;

	/** Updates the service.
	 *
	 * @param perunSession
	 * @param service
	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 */
	void updateService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException;

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
	Service getServiceById(PerunSession perunSession, int id) throws InternalErrorException, PrivilegeException, ServiceNotExistsException;

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
	Service getServiceByName(PerunSession perunSession, String name) throws InternalErrorException, PrivilegeException, ServiceNotExistsException;

	/**
	 * get all services in perun
	 *
	 * @param perunSession
	 * @return all services in perun
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Service> getServices(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

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
	List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, PrivilegeException;

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
	List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceNotExistsException;

	/**
	 * Generates the list of attributes per each member associated with the resource.
	 *
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return attributes in special structure. Facility is in the root, facility children are resources. And resource children are members.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 |             +------Member
	 |             |        +-------Attrs
	 |             +------Member
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 .             +------Member
	 .             |        +-------Attrs
	 .             +------Member
	 |        +-------Attrs
	 +...
	 </pre>
	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws PrivilegeException
	 */
	ServiceAttributes getHierarchicalData(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

	/**
	 * Generates the list of attributes per each user and per each resource. Resources are filtered by service. 
	 * Never return member or member-resource attribute.
	 *
	 * @param perunSession
	 * @param service you will get attributes required by this service
	 * @param facility you will get attributes for this facility, resources associated with it and users assigned to the resources
	 * @return attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's children are all resources. Facility second child is abstract node with no attribute and it's children are all users.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------()
	 |      +---ChildNodes
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------()
	 +---ChildNodes
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +...
	 </pre>

	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws PrivilegeException
	 */
	ServiceAttributes getFlatData(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

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
	 <pre>
	 Facility
	 +---Attrs                              ...................................................
	 +---ChildNodes                         |                                                 .
	 +------Resource                 |                                                 .
	 |       +---Attrs
	 |       +---ChildNodes          |                                                 .
	 |              +------()        V                                                 .
	 |              |       +------Group                                               .
	 |              |       |        +-------Attrs                                     .
	 |              |       |        +-------ChildNodes                                .
	 |              |       |                   +-------()                             .
	 |              |       |                   |        +---ChildNodes                .
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +...
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +------Group
	 |              |       |        +-------Attrs
	 |              |       |        +-------ChildNodes
	 |              |       |                   +-------()
	 |              |       |                   |        +---ChildNodes
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +...
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      |
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      +...
	 |
	 +------Resource
	 |       +---Attrs
	 |       +---ChildNodes
	 |              +------()
	 |              |       +...
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +...
	 .                      +...
	.
		.
		</pre>
		*
		* @throws InternalErrorException
		* @throws ServiceNotExistsException
		* @throws FacilityNotExistsException
		* @throws PrivilegeException
		*/
		ServiceAttributes getDataWithGroups(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

	/**
	 * Generates the list of attributes per each member associated with the resources and groups in vos.
	 * 
	 * @param perunSession
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, vos associated with this facility by resources, resources associated with it and members assigned to the resources
	 * @return attributes in special structure. 
	 *        Facility is in the root, facility children are vos.
	 *        Vo first child is abstract structure which children are resources.
	 *        Resource first child is abstract structure which children are groups.
	 *        Resource  second chi is abstract structure which children are members.
	 *        Group first child is abstract structure which children are groups.
	 *        Group second chi is abstract structure which children are members.
	 <pre>
	 Facility
	 +---Attrs                              
	 +---ChildNodes               
	        +-----Vo
	        |      +---Attrs
	        |      +---ChildNodes
	        |             +-------Resource
	        |             |       +---Attrs               |-------------------------------------------------.
	        |             |       +---ChildNodes          |                                                 .
	        |             |              +------()        V                                                 .
	        |             |              |       +------Group                                               .
	        |             |              |       |        +-------Attrs                                     .
	        |             |              |       |        +-------ChildNodes                                .
	        |             |              |       |                   +-------()                             .
	        |             |              |       |                   |        +---ChildNodes                .
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +------Group
	        |             |              |       |        +-------Attrs
	        |             |              |       |        +-------ChildNodes
	        |             |              |       |                   +-------()
	        |             |              |       |                   |        +---ChildNodes
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      |
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      +...
	        |             |
	        |             +------Resource
	        |             |       +---Attrs
	        |             |       +---ChildNodes
	        |             |              +------()
	        |             |              |       +...
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +...
	        +-----Vo ....
	</pre>
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException 
	 * @throws VoNotExistsException
	 */
	ServiceAttributes getDataWithVos(PerunSession perunSession, Service service, Facility facility) throws InternalErrorException, VoNotExistsException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException;

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
	public List<ServicesPackage> getServicesPackages(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

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
	ServicesPackage getServicesPackageById(PerunSession perunSession, int servicesPackageId) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException;

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
	ServicesPackage getServicesPackageByName(PerunSession perunSession, String name) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException;

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
	ServicesPackage createServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ServicesPackageExistsException;

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
	void updateServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException;

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
	void deleteServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException, RelationExistsException;

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
	public void addServiceToServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServicesPackageNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, PrivilegeException;

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
	public void removeServiceFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyRemovedFromServicePackageException;

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
	public List<Service> getServicesFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException;

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
	 */
	void addRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException;

	/**
	 *  Batch version of addRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#addRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void addRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException;

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
	void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException;

	/**
	 *  Batch version of removeRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void removeRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException;

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
	void removeAllRequiredAttributes(PerunSession perunSession, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException;

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
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 * @throws WrongPatternException
	 */
	Destination addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException;

	/**
	 * Adds an destination for the facility and all services. Destination id doesn't need to be filled. If destination doesn't exist it will be created.
	 *
	 * @param perunSession
	 * @param services
	 * @param facility
	 * @param destination (id of this destination doesn't need to be filled.)
	 * @return destination with it's id set
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 * @throws WrongPatternException
	 */
	Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException;

	/**
	 * Adds destination for all services defined on the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @param destination
	 * @return list of added destinations
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws DestinationAlreadyAssignedException
	 * @throws WrongPatternException
	 */
	List<Destination> addDestinationsForAllServicesOnFacility(PerunSession perunSession, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException;

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
	List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException;

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
	void removeDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyRemovedException;

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
	Destination getDestinationById(PerunSession perunSession, int id) throws PrivilegeException, InternalErrorException, DestinationNotExistsException;

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
	List<Destination> getDestinations(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException;

	/**
	 * Get list of all destinations.
	 *
	 * @param perunSession
	 * @return list of all destinations for session
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Destination> getDestinations(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

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
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException;

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
	List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException;

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
	List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException;

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
	void removeAllDestinations(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException;

	@Deprecated
	int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException;

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
	 * @return list of services assigned  to facility
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;

	/**
	 * List all destinations for all facilities which are joined by resources to the VO.
	 *
	 * @param sess
	 * @param vo vo for which we are searching destinations
	 * @return list of destinations
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get count of all destinations.
	 *
	 * @param sess PerunSession
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 *
	 * @return count of all destinations
	 */
	int getDestinationsCount(PerunSession sess) throws InternalErrorException, PrivilegeException;
}
