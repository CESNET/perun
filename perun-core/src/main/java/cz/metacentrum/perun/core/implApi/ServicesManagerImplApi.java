package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import java.util.List;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 * <p>
 * Note: ServicesManager is not to be used directly by any client. ServicesManager's functionality is going to be
 * encapsulated in the Controller's GeneralServiceManager.
 */
public interface ServicesManagerImplApi {

  /**
   * Adds an destination for the facility and service.
   *
   * @param perunSession
   * @param service
   * @param facility
   * @param destination  string contains destination address (mail, url, hostname, ...)
   * @throws InternalErrorException
   * @throws DestinationAlreadyAssignedException when the combination already exists
   */
  void addDestination(PerunSession perunSession, Service service, Facility facility, Destination destination)
      throws DestinationAlreadyAssignedException;

  /**
   * Mark the attribute as required for the service. Required attribues are requisite for Service to run. If you add
   * attribute which has a default attribute then this default attribute will be automatically add too.
   *
   * @param perunSession perunSession
   * @param service      service to which the attribute will be added
   * @param attribute    attribute to add
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws AttributeAlreadyAssignedException if the attribute is already added
   */
  void addRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute)
      throws AttributeAlreadyAssignedException;

  /**
   * Batch version of addRequiredAttribute
   *
   * @see cz.metacentrum.perun.core.api.ServicesManager#addRequiredAttribute(PerunSession, Service, AttributeDefinition)
   */
  void addRequiredAttributes(PerunSession perunSession, Service service, List<? extends AttributeDefinition> attributes)
      throws AttributeAlreadyAssignedException;

  /**
   * Block Service on specific Destination. Service still can be propagated to other facility Destinations.
   *
   * @param session
   * @param serviceId     The Service to be blocked on this particular destination
   * @param destinationId The destination on which we want to block the Service
   * @throws InternalErrorException
   */
  void blockServiceOnDestination(PerunSession session, int serviceId, int destinationId)
      throws ServiceAlreadyBannedException;

  /**
   * Block Service on Facility. It won't be possible to propagate service on whole facility or any of its destinations.
   *
   * @param session
   * @param serviceId  The Service to be blocked on the Facility
   * @param facilityId The Facility on which we want to block the Service
   * @throws InternalErrorException
   */
  void blockServiceOnFacility(PerunSession session, int serviceId, int facilityId) throws ServiceAlreadyBannedException;

  /**
   * Check if service exists in underlaying data source.
   *
   * @param perunSession perun session
   * @param service      service to check
   * @throws InternalErrorException    if unexpected error occured
   * @throws ServiceNotExistsException if service doesn't exists
   */
  void checkServiceExists(PerunSession perunSession, Service service) throws ServiceNotExistsException;

  Destination createDestination(PerunSession sess, Destination destination);

  /**
   * Creates new service.
   *
   * @param perunSession
   * @param service
   * @return new service
   */
  Service createService(PerunSession perunSession, Service service);

  /**
   * Deletes destination.
   *
   * @param sess
   * @param destination destination to be deleted
   * @throws InternalErrorException
   * @throws DestinationAlreadyRemovedException if there are 0 rows affected by deleting from DB
   * @throws RelationExistsException            if the destination has some existing relations in DB
   */
  void deleteDestination(PerunSession sess, Destination destination)
      throws DestinationAlreadyRemovedException, RelationExistsException;

  /**
   * Deletes the service.
   *
   * @param perunSession
   * @param service
   * @throws ServiceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void deleteService(PerunSession perunSession, Service service) throws ServiceAlreadyRemovedException;

  /**
   * Determine if destination exists for specified facility and service.
   *
   * @param sess
   * @param service
   * @param facility
   * @param destination
   * @return true if the destination exists for the facility and the resource
   * @throws InternalErrorException
   */
  boolean destinationExists(PerunSession sess, Service service, Facility facility, Destination destination);

  boolean destinationExists(PerunSession sess, Destination destination);

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
   * Get all resources which use this service.
   *
   * @param sess
   * @param service
   * @return list of resources
   * @throws InternalErrorException
   */
  List<Resource> getAssignedResources(PerunSession sess, Service service);

  /**
   * List all services associated with the facility (via resource).
   *
   * @param perunSession
   * @param facility
   * @return list of services assigned  to facility
   * @throws InternalErrorException
   */
  List<Service> getAssignedServices(PerunSession perunSession, Facility facility);

  /**
   * List all services associated with the facility and vo (via resource).
   *
   * @param perunSession
   * @param facility
   * @param vo
   * @return list of services assigned  to facility and vo
   * @throws InternalErrorException
   */
  List<Service> getAssignedServices(PerunSession perunSession, Facility facility, Vo vo);

  /**
   * Get destination by String destination and type
   *
   * @param sess
   * @param destination Destination string representation
   * @param type        type of destination
   * @return Destination
   * @throws InternalErrorException
   * @throws DestinationNotExistsException
   */
  Destination getDestination(PerunSession sess, String destination, String type) throws DestinationNotExistsException;

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
   * @param facility     the facility
   * @return lists of all destinations for specific Facility
   * @throws InternalErrorException
   */
  List<Destination> getDestinations(PerunSession perunSession, Facility facility);

  /**
   * Get count of all destinations.
   *
   * @param perunSession
   * @return count of all destinations
   * @throws InternalErrorException
   */
  int getDestinationsCount(PerunSession perunSession);

  /**
   * List all destinations for all facilities which are joined by resources to the VO.
   *
   * @param sess
   * @param vo   vo for which we are searching destinations
   * @return list of destinations
   * @throws InternalErrorException
   */
  List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo);

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
   * Get service by id.
   *
   * @param perunSession
   * @param id
   * @return service with specified id
   * @throws InternalErrorException
   * @throws ServiceNotExistsException
   */
  Service getServiceById(PerunSession perunSession, int id) throws ServiceNotExistsException;

  /*
         getRequiredAttributes(PerunSession perunSession, Service service);
         */

  /**
   * Get service by name.
   *
   * @param perunSession
   * @param name         name of the service
   * @return service with specified name
   * @throws InternalErrorException
   * @throws ServiceNotExistsException
   */
  Service getServiceByName(PerunSession perunSession, String name) throws ServiceNotExistsException;

  /**
   * get all services in perun
   *
   * @param perunSession
   * @return all services in perun
   * @throws InternalErrorException
   */
  List<Service> getServices(PerunSession perunSession);

  /**
   * Get Services blocked on Destination.
   *
   * @param destinationId ID of Destination to get blocked Services for.
   * @return List of blocked Services.
   */
  List<Service> getServicesBlockedOnDestination(int destinationId);

  /**
   * Get Services blocked on Facility.
   *
   * @param facilityId ID of Facility to get blocked Services for.
   * @return List of blocked Services.
   */
  List<Service> getServicesBlockedOnFacility(int facilityId);

  /**
   * Get all services with given attribute.
   *
   * @param sess                perun session
   * @param attributeDefinition
   * @return all services with given attribute
   * @throws InternalErrorException
   */
  List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition);

  /**
   * Return list of services this destination points to.
   *
   * @param destinationId ID of destination
   * @return Services associated with this destination.
   */
  List<Service> getServicesFromDestination(int destinationId);

  /**
   * Checks whether given service is assigned to given facility (through some resource).
   *
   * @param sess     session
   * @param facility facility
   * @param service  service
   * @return true if service is assigned to given facility, false otherwise
   */
  boolean isServiceAssignedToFacility(PerunSession sess, Facility facility, Service service);

  /**
   * Return TRUE if Service is blocked on Destination.
   *
   * @param serviceId     ID of Service to check on.
   * @param destinationId ID of Destination to check on.
   * @return TRUE if Service is blocked on Destination / FALSE otherwise
   */
  boolean isServiceBlockedOnDestination(int serviceId, int destinationId);

  /**
   * Return TRUE if Service is blocked on Facility.
   *
   * @param serviceId  ID of Service to check on.
   * @param facilityId ID of Facility to check on.
   * @return TRUE if Service is blocked on Facility / FALSE otherwise
   */
  boolean isServiceBlockedOnFacility(int serviceId, int facilityId);

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
   * @param facility     the facility
   * @throws InternalErrorException
   */
  void removeAllDestinations(PerunSession perunSession, Facility facility);

  /**
   * Detate all required attributes from service
   *
   * @param perunSession perunSession
   * @param service      service from which the attributes will be removed
   * @throws InternalErrorException    if an exception raise in concrete implementation, the exception is wrapped in
   *                                   InternalErrorException
   * @throws ServiceNotExistsException if the service doesn't exists in underlaying data source
   */
  void removeAllRequiredAttributes(PerunSession perunSession, Service service);

  /**
   * Removes an destination from the facility and service.
   *
   * @param perunSession
   * @param service
   * @param facility
   * @param destination  string contains destination address (mail, url, hostname, ...)
   * @throws InternalErrorException
   * @throws DestinationAlreadyRemovedException
   */
  void removeDestination(PerunSession perunSession, Service service, Facility facility, Destination destination)
      throws DestinationAlreadyRemovedException;

  /**
   * Remove required attribute from service.
   * TODO If you try to remove attribute which is default for other Required attribute ...
   *
   * @param perunSession perunSession
   * @param service      service from which the attribute will be removed
   * @param attribute    attribute to remove
   * @throws InternalErrorException        if an exception raise in concrete implementation, the exception is wrapped in
   *                                       InternalErrorException
   * @throws AttributeNotAssignedException
   * @throws ServiceNotExistsException     if the service doesn't exists in underlaying data source
   */
  void removeRequiredAttribute(PerunSession perunSession, Service service, AttributeDefinition attribute)
      throws AttributeNotAssignedException;

  /**
   * Batch version of removeRequiredAttribute
   *
   * @see cz.metacentrum.perun.core.api.ServicesManager#removeRequiredAttribute(PerunSession, Service,
   * AttributeDefinition)
   */
  void removeRequiredAttributes(PerunSession perunSession, Service service,
                                List<? extends AttributeDefinition> attributes) throws AttributeNotAssignedException;

  /**
   * Check if service exists in underlaying data source.
   *
   * @param perunSession perun session
   * @param service      service to check
   * @return true if service exists in underlaying data source, false otherwise
   * @throws InternalErrorException if unexpected error occured
   */
  boolean serviceExists(PerunSession perunSession, Service service);

  /**
   * Unblock all blocked Services on specified Destination.
   *
   * @param destination ID of Destination we want to unblock all Services.
   */
  void unblockAllServicesOnDestination(int destination);

  /**
   * Unblock all blocked Services on Facility.
   *
   * @param facility ID of Facility we want to unblock all Services.
   */
  void unblockAllServicesOnFacility(int facility);

  /**
   * Unblock Service everywhere. If was not blocked, nothing happens.
   *
   * @param serviceId ID of Service to unblock.
   */
  void unblockService(int serviceId);

  /**
   * Unblock Service on specific Destination. If was not blocked, nothing happens.
   *
   * @param serviceId     ID of Service to unblock on Destination.
   * @param destinationId ID of Destination to unblock Service on.
   */
  void unblockServiceOnDestination(int serviceId, int destinationId);

  /**
   * Unblock Service on whole Facility. If was not blocked, nothing happens.
   *
   * @param serviceId  ID of Service to unblock on Facility.
   * @param facilityId ID of Facility to unblock Service on.
   */
  void unblockServiceOnFacility(int serviceId, int facilityId);

  /**
   * Updates the service.
   *
   * @param perunSession
   * @param service
   */
  void updateService(PerunSession perunSession, Service service);

}
