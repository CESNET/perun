package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 *
 * Note: ServicesManager is not to be used directly by any client.
 * ServicesManager's functionality is going to be encapsulated in the Controller's
 * GeneralServiceManager.
 *
 */
public interface ServicesManagerImplApi {

	/**
	 * Creates new service.
	 *
	 * @param perunSession
	 * @param service
	 * @return new service
	 */
	Service createService(PerunSession perunSession, Service service) throws InternalErrorException;

	/** Deletes the service.
	 *
	 * @param perunSession
	 * @param service
	 *
	 * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceAlreadyRemovedException;

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
	 * @throws ServiceNotExistsException
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
	 * Get all resources which use this service.
	 *
	 * @param sess
	 * @param service
	 * @return list of resources
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException;

	/**
	 * List packages
	 *
	 * @param perunSession
	 *
	 * @return list of packages in the DB
	 *
	 * @throws InternalErrorException
	 */
	List<ServicesPackage> getServicesPackages(PerunSession perunSession) throws InternalErrorException;

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
	 * Get services package by name.
	 * @param sess
	 * @param name
	 * @return package
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	ServicesPackage getServicesPackageByName(PerunSession sess, String name) throws InternalErrorException, ServicesPackageNotExistsException;
	/**
	 * Insert a new package
	 *
	 * @param servicesPackage package to be inserted
	 * @param perunSession
	 *
	 * @return ServicesPackage object completely filled (including Id)
	 *
	 * @throws InternalErrorException
	 */
	ServicesPackage createServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Update package
	 *
	 * @param servicesPackage with which is the old one supposed to be updated :-)
	 * @param perunSession
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	void updateServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Remove the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to be removed.
	 * @throws ServicesPackageNotExistsException
	 */
	void deleteServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Add the service to the package
	 *
	 * @param perunSession
	 * @param servicesPackage services package to which the service supposed to be added
	 * @param service service to be added to the services package
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws ServiceAlreadyAssignedException
	 */
	void addServiceToServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyAssignedException;

	/**
	 * Remove Service from Services Package
	 *
	 * @param perunSession
	 * @param servicesPackage services package from which the service supposed to be removed
	 * @param service service that will be removed from the services package
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws ServiceAlreadyRemovedFromServicePackageException there are 0 rows affected by removing service from service package in DB
	 */
	void removeServiceFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyRemovedFromServicePackageException;

	/**
	 * List services stored in the packages
	 *
	 * @param servicesPackage the package from which we want to list the services
	 *
	 * @return list consisting services
	 *
	 * @throws InternalErrorException
	 * @throws ServicesPackageNotExistsException
	 */
	List<Service> getServicesFromServicesPackage(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/*
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
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 * @throws AttributeAlreadyAssignedException if the attribute is already added
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
	 * @param perunSession perunSession
	 * @param service service from which the attribute will be removed
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotAssignedException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 */
	void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeNotAssignedException;

	/**
	 *  Batch version of removeRequiredAttribute
	 *  @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession,Service,AttributeDefinition)
	 */
	void removeRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotAssignedException;

	/**
	 * Detate all required attributes from service
	 *
	 * @param perunSession perunSession
	 * @param service service from which the attributes will be removed
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
	 */
	void removeAllRequiredAttributes(PerunSession perunSession, Service service) throws InternalErrorException;

	/**
	 * Check if service exists in underlaying data source.
	 *
	 * @param perunSession perun session
	 * @param service service to check
	 * @return true if service exists in underlaying data source, false otherwise
	 *
	 * @throws InternalErrorException if unexpected error occured
	 */
	boolean serviceExists(PerunSession perunSession, Service service) throws InternalErrorException;

	/**
	 * Check if service exists in underlaying data source.
	 *
	 * @param perunSession perun session
	 * @param service service to check
	 *
	 * @throws InternalErrorException if unexpected error occured
	 * @throws ServiceNotExistsException if service doesn't exists
	 */
	void checkServiceExists(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException;

	/**
	 * Check if services package exists in underlaying data source.
	 *
	 * @param perunSession perun session
	 * @param servicesPackage services package to check
	 * @return true if services package exists in underlaying data source, false otherwise
	 *
	 * @throws InternalErrorException if unexpected error occur
	 */
	boolean servicesPackageExists(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Check if services package exists in underlaying data source.
	 *
	 * @param perunSession perun session
	 * @param servicesPackage services package to check
	 *
	 * @throws InternalErrorException if unexpected error occur
	 * @throws ServicesPackageNotExistsException if service doesn't exists
	 */
	void checkServicesPackageExists(PerunSession perunSession, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException;

	/**
	 * Adds an destination for the facility and service.
	 *
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @param destination string contains destination address (mail, url, hostname, ...)
	 * @throws InternalErrorException
	 * @throws DestinationAlreadyAssignedException
	 */
	void addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination) throws InternalErrorException;

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

	@Deprecated
	int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException;

	/**
	 * Get destination by String destination and type
	 *
	 * @param sess
	 * @param destination Destination string representation
	 * @param type type of destination
	 * @return Destination
	 *
	 * @throws InternalErrorException
	 * @throws DestinationNotExistsException
	 */
	Destination getDestination(PerunSession sess, String destination, String type) throws InternalErrorException, DestinationNotExistsException;

	/**
	 *  Determine if destination exists for specified facility and service.
	 *
	 * @param sess
	 * @param service
	 * @param facility
	 * @param destination
	 * @return true if the destination exists for the facility and the resource
	 *
	 * @throws InternalErrorException
	 */
	boolean destinationExists(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException;

	Destination createDestination(PerunSession sess, Destination destination) throws InternalErrorException;

	boolean destinationExists(PerunSession sess, Destination destination) throws InternalErrorException;

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
