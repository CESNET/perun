package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ForceServicePropagationDisabledException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAttributesCannotExtend;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceIsNotBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class ServicesManagerEntry implements ServicesManager {

  static final Logger LOG = LoggerFactory.getLogger(ServicesManagerEntry.class);

  private PerunBl perunBl;
  private ServicesManagerBl servicesManagerBl;

  public ServicesManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.servicesManagerBl = perunBl.getServicesManagerBl();
  }

  public ServicesManagerEntry() {
  }

  private ServicesManagerBl getServicesManagerBl() {
    return this.servicesManagerBl;
  }

  @Override
  public Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility,
                                    Destination destination)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, WrongPatternException {
    Utils.checkPerunSession(perunSession);
    Utils.notNull(services, "services");
    Utils.checkDestinationType(destination);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy",
        facility)) {
      throw new PrivilegeException(perunSession, "addDestination");
    }

    //prepare lists of facilities
    List<Facility> facilitiesByHostname;
    List<Facility> facilitiesByDestination = new ArrayList<>();
    if (destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONWINDOWS) ||
            destination.getType().equals(Destination.DESTINATIONWINDOWSPROXY)) {
      facilitiesByHostname = getPerunBl().getFacilitiesManagerBl()
                                 .getFacilitiesByHostName(perunSession, destination.getHostNameFromDestination());
      if (facilitiesByHostname.isEmpty()) {
        facilitiesByDestination = getPerunBl().getFacilitiesManagerBl()
                                      .getFacilitiesByDestination(perunSession,
                                          destination.getHostNameFromDestination());
      }

      if (!facilitiesByHostname.isEmpty()) {
        boolean hasRight = false;
        for (Facility facilityByHostname : facilitiesByHostname) {
          if (AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy",
              facilityByHostname)) {
            hasRight = true;
            break;
          }
        }
        if (!hasRight) {
          throw new PrivilegeException("addDestination");
        }
      }

      if (!facilitiesByDestination.isEmpty()) {
        boolean hasRight = false;
        for (Facility facilityByDestination : facilitiesByDestination) {
          if (AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy",
              facilityByDestination)) {
            hasRight = true;
            break;
          }
        }
        if (!hasRight) {
          throw new PrivilegeException("addDestination");
        }
      }
    }

    for (Service s : services) {
      getServicesManagerBl().checkServiceExists(perunSession, s);
    }
    Utils.notNull(destination, "destination");
    Utils.notNull(destination.getDestination(), "destination.destination");
    Utils.notNull(destination.getType(), "destination.type");

    return getServicesManagerBl().addDestination(perunSession, services, facility, destination);
  }

  @Override
  public Destination addDestination(PerunSession sess, Service service, Facility facility, Destination destination)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException,
                 DestinationAlreadyAssignedException, WrongPatternException {
    Utils.checkPerunSession(sess);
    Utils.checkDestinationType(destination);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addDestination_Service_Facility_Destination_policy", facility)) {
      throw new PrivilegeException(sess, "addDestination");
    }

    //prepare lists of facilities
    List<Facility> facilitiesByHostname;
    List<Facility> facilitiesByDestination = new ArrayList<>();
    if (destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE) ||
            destination.getType().equals(Destination.DESTINATIONWINDOWS) ||
            destination.getType().equals(Destination.DESTINATIONWINDOWSPROXY)) {
      facilitiesByHostname =
          getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(sess, destination.getHostNameFromDestination());
      if (facilitiesByHostname.isEmpty()) {
        facilitiesByDestination = getPerunBl().getFacilitiesManagerBl()
                                      .getFacilitiesByDestination(sess,
                                          destination.getHostNameFromDestination());
      }

      if (!facilitiesByHostname.isEmpty()) {
        boolean hasRight = false;
        for (Facility facilityByHostname : facilitiesByHostname) {
          if (AuthzResolver.authorizedInternal(sess, "addDestination_Service_Facility_Destination_policy",
              facilityByHostname)) {
            hasRight = true;
            break;
          }
        }
        if (!hasRight) {
          throw new PrivilegeException("You have no right to add this destination.");
        }
      }

      if (!facilitiesByDestination.isEmpty()) {
        boolean hasRight = false;
        for (Facility facilityByDestination : facilitiesByDestination) {
          if (AuthzResolver.authorizedInternal(sess, "addDestination_Service_Facility_Destination_policy",
              facilityByDestination)) {
            hasRight = true;
            break;
          }
        }
        if (!hasRight) {
          throw new PrivilegeException("You have no right to add this destination.");
        }
      }
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    Utils.notNull(destination, "destination");
    Utils.notNull(destination.getDestination(), "destination.destination");
    Utils.notNull(destination.getType(), "destination.type");

    return getServicesManagerBl().addDestination(sess, service, facility, destination);
  }

  @Override
  public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service,
                                                                   Facility facility)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException,
                 DestinationAlreadyAssignedException {
    Utils.checkPerunSession(perunSession);

    // Auhtorization
    if (!AuthzResolver.authorizedInternal(perunSession,
        "addDestinationsDefinedByHostsOnFacility_Service_Facility_policy", Arrays.asList(service, facility))) {
      throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
    }

    getServicesManagerBl().checkServiceExists(perunSession, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

    return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, service, facility);
  }

  @Override
  public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services,
                                                                   Facility facility)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
    Utils.checkPerunSession(perunSession);
    Utils.notNull(services, "services");

    for (Service s : services) {
      getServicesManagerBl().checkServiceExists(perunSession, s);
    }
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

    // Authorization
    for (Service service : services) {
      if (!AuthzResolver.authorizedInternal(perunSession,
          "addDestinationsDefinedByHostsOnFacility_List<Services>_Facility_policy", service, facility)) {
        throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
      }
    }

    return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, services, facility);
  }

  @Override
  public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(perunSession);

    // Auhtorization
    if (!AuthzResolver.authorizedInternal(perunSession, "addDestinationsDefinedByHostsOnFacility_Facility_policy",
        facility)) {
      throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

    return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, facility);
  }

  @Override
  public List<Destination> addDestinationsForAllServicesOnFacility(PerunSession sess, Facility facility,
                                               Destination destination) throws PrivilegeException,
                                                                                   FacilityNotExistsException,
                                                                                   DestinationAlreadyAssignedException,
                                                                                   WrongPatternException {
    Utils.checkPerunSession(sess);
    Utils.checkDestinationType(destination);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addDestinationsForAllServicesOnFacility_Facility_Destination_policy",
        Arrays.asList(facility, destination))) {
      throw new PrivilegeException(sess, "addDestinationsForAllServices");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    Utils.notNull(destination, "destination");
    Utils.notNull(destination.getDestination(), "destination.destination");
    Utils.notNull(destination.getType(), "destination.type");

    return getServicesManagerBl().addDestinationsForAllServicesOnFacility(sess, facility, destination);
  }

  @Override
  public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute)
      throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException,
                 AttributeAlreadyAssignedException, ServiceAttributesCannotExtend {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addRequiredAttribute_Service_AttributeDefinition_policy", service)) {
      throw new PrivilegeException(sess, "addRequiredAttribute");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attribute);

    getServicesManagerBl().addRequiredAttribute(sess, service, attribute);
  }

  @Override
  public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes)
      throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException,
                 AttributeAlreadyAssignedException, ServiceAttributesCannotExtend {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addRequiredAttributes_Service_List<AttributeDefinition>_policy",
        service)) {
      throw new PrivilegeException(sess, "addRequiredAttributes");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

    getServicesManagerBl().addRequiredAttributes(sess, service, attributes);
  }

  @Override
  public void blockAllServicesOnDestination(PerunSession sess, int destinationId)
      throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());


    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "blockAllServicesOnDestination_int_policy", facility)) {
        getServicesManagerBl().blockAllServicesOnDestination(sess, destinationId);
        return;
      }
    }
    throw new PrivilegeException(sess, "blockAllServicesOnDestination");
  }

  @Override
  public void blockAllServicesOnFacility(PerunSession sess, Facility facility)
      throws FacilityNotExistsException, PrivilegeException {

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "blockAllServicesOnFacility_Facility_policy", facility)) {
      throw new PrivilegeException(sess, "blockAllServicesOnFacility");
    }
    getServicesManagerBl().blockAllServicesOnFacility(sess, facility);
  }

  @Override
  public void blockServiceOnDestination(PerunSession sess, Service service, int destinationId)
      throws PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException,
                 FacilityNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "blockServiceOnDestination_Service_int_policy",
          Arrays.asList(facility, service))) {
        getServicesManagerBl().blockServiceOnDestination(sess, service, destinationId);
        return;
      }
    }
    throw new PrivilegeException(sess, "blockServiceOnDestination");
  }

  @Override
  public void blockServiceOnFacility(PerunSession sess, Service service, Facility facility)
      throws ServiceAlreadyBannedException, PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "blockServiceOnFacility_Service_Facility_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "blockServiceOnFacility");
    }
    getServicesManagerBl().blockServiceOnFacility(sess, service, facility);
  }

  @Override
  public void blockServicesOnDestinations(PerunSession sess, List<RichDestination> richDestinations)
      throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
    for (RichDestination richDestination : richDestinations) {
      try {
        blockServiceOnDestination(sess, richDestination.getService(), richDestination.getId());
      } catch (ServiceAlreadyBannedException ignored) {
        // ignored
      }
    }
  }

  @Override
  public void blockServicesOnFacility(PerunSession sess, List<Service> services, Facility facility)
      throws ServiceAlreadyBannedException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    for (Service service : services) {
      blockServiceOnFacility(sess, service, facility);
    }
  }

  @Override
  public Service createService(PerunSession sess, Service service) throws PrivilegeException, ServiceExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(service, "service");
    Utils.notNull(service.getName(), "service.name");

    if (!service.getName().matches(ServicesManager.SERVICE_NAME_REGEXP)) {
      throw new IllegalArgumentException(
          "Wrong service name, service name must matches " + ServicesManager.SERVICE_NAME_REGEXP + ", but was: " +
              service.getName());
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createService_Service_policy")) {
      throw new PrivilegeException(sess, "createService");
    }

    return getServicesManagerBl().createService(sess, service);
  }

  @Override
  public void deleteService(PerunSession sess, Service service, boolean forceFlag)
      throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteService_Service_boolean_policy", service)) {
      throw new PrivilegeException(sess, "deleteService");
    }

    getServicesManagerBl().checkServiceExists(sess, service);

    getServicesManagerBl().deleteService(sess, service, forceFlag);
  }

  @Override
  public void deleteServices(PerunSession sess, List<Service> services, boolean forceFlag)
      throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
    for (Service service : services) {
      deleteService(sess, service, forceFlag);
    }
  }

  @Override
  public boolean forceServicePropagation(PerunSession sess, Facility facility, Service service)
      throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "forceServicePropagation_Facility_Service_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "forceServicePropagation");
    }
    return getServicesManagerBl().forceServicePropagation(sess, facility, service);
  }

  @Override
  public boolean forceServicePropagation(PerunSession sess, Service service) throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "forceServicePropagation_Service_policy", service)) {
      throw new PrivilegeException(sess, "forceServicePropagation");
    }
    return getServicesManagerBl().forceServicePropagation(sess, service);
  }

  @Override
  public void forceServicePropagationBulk(PerunSession sess, Facility facility, List<Service> services)
      throws PrivilegeException, FacilityNotExistsException, ForceServicePropagationDisabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    for (Service service : services) {
      if (!forceServicePropagation(sess, facility, service)) {
        throw new ForceServicePropagationDisabledException(
            String.format("It is not possible to force service propagation for service with id %d", service.getId()));
      }
    }
  }

  @Override
  public void forceServicePropagationBulk(PerunSession sess, List<Service> services)
      throws PrivilegeException, ForceServicePropagationDisabledException {
    Utils.checkPerunSession(sess);

    for (Service service : services) {
      if (!forceServicePropagation(sess, service)) {
        throw new ForceServicePropagationDisabledException(
            String.format("It is not possible to force service propagation for service with id %d", service.getId()));
      }
    }
  }

  @Override
  public String forceServicePropagationForHostname(PerunSession sess, String hostname) throws PrivilegeException {
    Utils.checkPerunSession(sess);
    if (StringUtils.isBlank(hostname)) {
      throw new InternalErrorException("Non-empty parameter 'hostname' is required!");
    }

    // Authorization
    List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(sess, hostname);
    facilities.removeIf(
        facility -> !AuthzResolver.authorizedInternal(sess, "forceServicePropagationForHostname_String_policy",
            facility));
    if (facilities.isEmpty()) {
      return "ERROR: No facilities found for '" + hostname + "'.";
    }

    Set<String> forcedServices = new HashSet<>();
    for (Facility facility : facilities) {

      // work only with assigned and allowed services used on destinations matching the hostname
      List<RichDestination> destinations = getPerunBl().getServicesManagerBl().getAllRichDestinations(sess, facility);
      List<Service> assignedServices = getPerunBl().getServicesManagerBl().getAssignedServices(sess, facility);
      List<RichDestination> filteredDestinations = destinations.stream().filter(
          d -> hostname.equalsIgnoreCase(d.getHostNameFromDestination()) &&
                   !d.isBlocked() &&
                   assignedServices.contains(d.getService())
      ).toList();
      Set<Service> services =
          filteredDestinations.stream().map(RichDestination::getService).collect(Collectors.toSet());
      for (Service service : services) {
        if (getPerunBl().getServicesManagerBl().forceServicePropagation(sess, facility, service)) {
          forcedServices.add(service.getName());
        }
      }
    }

    if (!forcedServices.isEmpty()) {
      return String.join(" ", forcedServices);
    }

    return "ERROR: No services found for '" + hostname + "'.";

  }

  @Override
  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(perunSession);

    //Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getAllRichDestinations_Facility_policy", facility)) {
      throw new PrivilegeException(perunSession, "getAllRichDestinations");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
    return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, facility);
  }

  @Override
  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service)
      throws PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(perunSession);

    //Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getAllRichDestinations_Service_policy", service)) {
      throw new PrivilegeException(perunSession, "getAllRichDestinations");
    }

    getServicesManagerBl().checkServiceExists(perunSession, service);
    return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, service);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Service service)
      throws PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(sess);

    getServicesManagerBl().checkServiceExists(sess, service);

    List<Resource> resources = getServicesManagerBl().getAssignedResources(sess, service);

    if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Service_policy")) {
      throw new PrivilegeException(sess, "getAssignedResources");
    }

    resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedResources_Service_policy",
        Arrays.asList(service, resource)));

    return resources;
  }

  @Override
  public List<Service> getAssignedServices(PerunSession sess, Facility facility)
      throws FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAssignedServices_Facility_policy", facility)) {
      throw new PrivilegeException(sess, "getAssignedServices");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getServicesManagerBl().getAssignedServices(sess, facility);
  }

  @Override
  public List<Service> getAssignedServices(PerunSession sess, Facility facility, Vo vo)
      throws FacilityNotExistsException, VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAssignedServices_Facility_Vo_policy", facility)) {
      throw new PrivilegeException(sess, "getAssignedServices");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    return getServicesManagerBl().getAssignedServices(sess, facility, vo);
  }

  @Override
  public Destination getDestinationById(PerunSession sess, int id)
      throws PrivilegeException, DestinationNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getDestinationById_int_policy")) {
      throw new PrivilegeException(sess, "getDestinationById");
    }

    return getServicesManagerBl().getDestinationById(sess, id);
  }

  @Override
  public int getDestinationIdByName(PerunSession sess, String name, String type) throws DestinationNotExistsException {
    return servicesManagerBl.getDestinationIdByName(sess, name, type);
  }

  @Override
  public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getDestinations_Service_Facility_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "getDestinations");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getServicesManagerBl().getDestinations(sess, service, facility);
  }

  @Override
  public List<Destination> getDestinations(PerunSession perunSession) throws PrivilegeException {
    Utils.checkPerunSession(perunSession);

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getDestinations_policy")) {
      throw new PrivilegeException(perunSession, "getDestinations");
    }

    return getServicesManagerBl().getDestinations(perunSession);
  }

  @Override
  public int getDestinationsCount(PerunSession sess) {
    Utils.checkPerunSession(sess);

    return getServicesManagerBl().getDestinationsCount(sess);
  }

  @Override
  public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws VoNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    return getPerunBl().getServicesManagerBl().getFacilitiesDestinations(sess, vo);
  }

  @Override
  public List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getFacilityAssignedServicesForGUI_Facility_policy",
        facility)) {
      throw new PrivilegeException(perunSession, "getFacilityAssignedServicesForGUI");
    }
    return getServicesManagerBl().getFacilityAssignedServicesForGUI(perunSession, facility);
  }

  @Override
  public HashedGenData getHashedDataWithGroups(PerunSession sess, Service service, Facility facility,
                                               boolean consentEval, int taskRunId)
      throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getHashedDataWithGroups_Service_Facility_boolean_policy", service,
        facility)) {
      throw new PrivilegeException(sess, "getHashedDataWithGroups");
    }

    return getServicesManagerBl().getHashedDataWithGroups(sess, service, facility, consentEval, taskRunId);
  }

  @Override
  public HashedGenData getHashedHierarchicalData(PerunSession sess, Service service, Facility facility,
                                                 boolean consentEval, int taskRunId)
      throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getHashedHierarchicalData_Service_Facility_boolean_policy", service,
        facility)) {
      throw new PrivilegeException(sess, "getHashedHierarchicalData");
    }

    return getServicesManagerBl().getHashedHierarchicalData(sess, service, facility, consentEval, taskRunId);
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException {
    Utils.checkPerunSession(perunSession);

    //Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getRichDestinations_Facility_Service_policy",
        Arrays.asList(facility, service))) {
      throw new PrivilegeException(perunSession, "getRichDestinations");
    }

    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
    getServicesManagerBl().checkServiceExists(perunSession, service);
    return getPerunBl().getServicesManagerBl().getRichDestinations(perunSession, facility, service);
  }

  @Override
  public Service getServiceById(PerunSession sess, int id) throws PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getServiceById_int_policy")) {
      throw new PrivilegeException(sess, "getServiceById");
    }

    return getServicesManagerBl().getServiceById(sess, id);
  }

  @Override
  public Service getServiceByName(PerunSession sess, String name) throws PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getServiceByName_String_policy")) {
      throw new PrivilegeException(sess, "getServiceByName");
    }

    Utils.notNull(name, "name");

    return getServicesManagerBl().getServiceByName(sess, name);
  }

  @Override
  public List<Service> getServices(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getServices_policy")) {
      throw new PrivilegeException(sess, "getServices");
    }

    return getServicesManagerBl().getServices(sess);
  }

  @Override
  public List<Service> getServicesBlockedOnDestination(PerunSession sess, int destinationId)
      throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "getServicesBlockedOnDestination_int_policy", facility)) {
        return getServicesManagerBl().getServicesBlockedOnDestination(sess, destinationId);
      }
    }
    throw new PrivilegeException(sess, "getServicesBlockedOnDestination");
  }

  @Override
  public List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility)
      throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getServicesBlockedOnFacility_Facility_policy", facility)) {
      throw new PrivilegeException(perunSession, "getServicesBlockedOnFacility");
    }
    return getServicesManagerBl().getServicesBlockedOnFacility(perunSession, facility);
  }

  @Override
  public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getServicesByAttributeDefinition_AttributeDefinition_policy")) {
      throw new PrivilegeException(sess, "getServicesByAttributeDefinition");
    }

    return getServicesManagerBl().getServicesByAttributeDefinition(sess, attributeDefinition);
  }

  @Override
  public boolean isServiceBlockedOnDestination(PerunSession sess, Service service, int destinationId)
      throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "isServiceBlockedOnDestination_Service_int_policy",
          Arrays.asList(service, facility))) {
        return getServicesManagerBl().isServiceBlockedOnDestination(service, destinationId);
      }
    }
    throw new PrivilegeException(sess, "isServiceBlockedOnDestination");
  }

  @Override
  public boolean isServiceBlockedOnFacility(PerunSession sess, Service service, Facility facility)
      throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "isServiceBlockedOnFacility_Service_Facility_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "isServiceBlockedOnFacility");
    }
    return getServicesManagerBl().isServiceBlockedOnFacility(service, facility);
  }

  @Override
  public boolean planServicePropagation(PerunSession sess, Facility facility, Service service)
      throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "planServicePropagation_Facility_Service_policy",
        Arrays.asList(facility, service))) {
      throw new PrivilegeException(sess, "planServicePropagation");
    }
    return getServicesManagerBl().planServicePropagation(sess, facility, service);
  }

  @Override
  public boolean planServicePropagation(PerunSession perunSession, Service service) throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "planServicePropagation_Service_policy", service)) {
      throw new PrivilegeException(perunSession, "planServicePropagation");
    }
    return getServicesManagerBl().planServicePropagation(perunSession, service);
  }

  @Override
  public void removeAllDestinations(PerunSession sess, Service service, Facility facility)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeAllDestinations_Service_Facility_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "removeAllDestinations");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    getServicesManagerBl().removeAllDestinations(sess, service, facility);
  }

  @Override
  public void removeAllRequiredAttributes(PerunSession sess, Service service)
      throws PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeAllRequiredAttributes_Service_policy", service)) {
      throw new PrivilegeException(sess, "removeRequiredAttribute");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getServicesManagerBl().removeAllRequiredAttributes(sess, service);
  }

  @Override
  public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException,
                 DestinationAlreadyRemovedException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeDestination_Service_Facility_Destination_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "removeDestination");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    Utils.notNull(destination, "destination");
    Utils.notNull(destination.getDestination(), "destination.destination");
    Utils.notNull(destination.getType(), "destination.type");

    getServicesManagerBl().removeDestination(sess, service, facility, destination);
  }

  @Override
  public void removeDestinationsByRichDestinations(PerunSession sess, List<RichDestination> richDestinations)
      throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException,
                 DestinationAlreadyRemovedException {
    for (RichDestination richDestination : richDestinations) {
      removeDestination(sess, richDestination.getService(), richDestination.getFacility(), richDestination);
    }
  }

  @Override
  public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute)
      throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeRequiredAttribute_Service_AttributeDefinition_policy",
        service)) {
      throw new PrivilegeException(sess, "removeRequiredAttribute");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attribute);

    getServicesManagerBl().removeRequiredAttribute(sess, service, attribute);
  }

  @Override
  public void removeRequiredAttributes(PerunSession sess, Service service,
                                       List<? extends AttributeDefinition> attributes)
      throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeRequiredAttributes_Service_List<AttributeDefinition>_policy",
        service)) {
      throw new PrivilegeException(sess, "removeRequiredAttributes");
    }

    getServicesManagerBl().checkServiceExists(sess, service);
    getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

    getServicesManagerBl().removeRequiredAttributes(sess, service, attributes);
  }

  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  /**
   * Sets the servicesManagerBl for this instance.
   *
   * @param servicesManagerBl The servicesManagerBl.
   */
  public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
    this.servicesManagerBl = servicesManagerBl;
  }

  @Override
  public void unblockAllServicesOnDestination(PerunSession sess, String destinationName)
      throws FacilityNotExistsException {
    List<Destination> destinations = getServicesManagerBl().getDestinations(sess);
    for (Destination destination : destinations) {
      if (destination.getDestination().equals(destinationName)) {
        List<Facility> destinationFacilities =
            perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());
        boolean servicesUnblocked = false;
        for (Facility facility : destinationFacilities) {
          if (AuthzResolver.authorizedInternal(sess, "unblockAllServicesOnDestination_String_policy", facility)) {
            getServicesManagerBl().unblockAllServicesOnDestination(sess, destination.getId());
            servicesUnblocked = true;
            break;
          }
        }
        if (!servicesUnblocked) {
          LOG.warn(
              "Trying to unblock services on destination by a user who is neither perunadmin" +
                  " nor admin of the facility");
        }
      }
    }
  }

  @Override
  public void unblockAllServicesOnDestination(PerunSession sess, int destinationId)
      throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "unblockAllServicesOnDestination_int_policy", facility)) {
        getServicesManagerBl().unblockAllServicesOnDestination(sess, destinationId);
        return;
      }
    }
    throw new PrivilegeException(sess, "unblockAllServicesOnDestination");
  }

  @Override
  public void unblockAllServicesOnFacility(PerunSession sess, Facility facility) throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "unblockAllServicesOnDestination_String_policy", facility)) {
      throw new PrivilegeException(sess, "unblockAllServicesOnFacility");
    }
    getServicesManagerBl().unblockAllServicesOnFacility(sess, facility);
  }

  @Override
  public void unblockServiceOnDestination(PerunSession sess, Service service, int destinationId)
      throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException {
    Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
    List<Facility> destinationFacilities =
        perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

    for (Facility facility : destinationFacilities) {
      if (AuthzResolver.authorizedInternal(sess, "unblockServiceOnDestination_Service_int_policy",
          Arrays.asList(service, facility))) {
        getServicesManagerBl().unblockServiceOnDestination(sess, service, destinationId);
        return;
      }
    }
    throw new PrivilegeException(sess, "unblockServiceOnDestination");
  }

  @Override
  public void unblockServiceOnFacility(PerunSession sess, Service service, Facility facility)
      throws PrivilegeException {
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "unblockServiceOnFacility_Service_Facility_policy",
        Arrays.asList(service, facility))) {
      throw new PrivilegeException(sess, "unblockServiceOnFacility");
    }
    getServicesManagerBl().unblockServiceOnFacility(sess, service, facility);
  }

  @Override
  public void unblockServicesOnDestinations(PerunSession sess, List<RichDestination> richDestinations)
      throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException {
    for (RichDestination richDestination : richDestinations) {
      unblockServiceOnDestination(sess, richDestination.getService(), richDestination.getId());
    }
  }

  @Override
  public void unblockServicesOnFacility(PerunSession sess, List<Service> services, Facility facility)
      throws PrivilegeException, FacilityNotExistsException, ServiceIsNotBannedException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    for (Service service : services) {
      if (!getServicesManagerBl().isServiceBlockedOnFacility(service, facility)) {
        throw new ServiceIsNotBannedException(
            String.format("Service with id %d is not banned on the facility with id %d", service.getId(),
                facility.getId()));
      }
      unblockServiceOnFacility(sess, service, facility);
    }
  }

  @Override
  public void updateService(PerunSession sess, Service service) throws ServiceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateService_Service_policy", service)) {
      throw new PrivilegeException(sess, "updateService");
    }

    getServicesManagerBl().checkServiceExists(sess, service);

    if (!service.getName().matches(ServicesManager.SERVICE_NAME_REGEXP)) {
      throw new IllegalArgumentException(
          "Wrong service name, service name must matches " + ServicesManager.SERVICE_NAME_REGEXP + ", but was: " +
              service.getName());
    }
    getServicesManagerBl().updateService(sess, service);
  }
}
