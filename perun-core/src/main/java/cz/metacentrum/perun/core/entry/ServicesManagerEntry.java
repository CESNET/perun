package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
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
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class ServicesManagerEntry implements ServicesManager {

	final static Logger log = LoggerFactory.getLogger(ServicesManagerEntry.class);

	private PerunBl perunBl;
	private ServicesManagerBl servicesManagerBl;

	public ServicesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.servicesManagerBl = perunBl.getServicesManagerBl();
	}

	public ServicesManagerEntry() {
	}

	@Override
	public void blockServiceOnFacility(PerunSession sess, Service service, Facility facility) throws ServiceAlreadyBannedException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "blockServiceOnFacility_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "blockServiceOnFacility");
		}
		getServicesManagerBl().blockServiceOnFacility(sess, service, facility);
	}

	@Override
	public void blockServiceOnDestination(PerunSession sess, Service service, int destinationId) throws PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException, FacilityNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "blockServiceOnDestination_Service_int_policy", Arrays.asList(facility, service))) {
				getServicesManagerBl().blockServiceOnDestination(sess, service, destinationId);
				return;
			}
		}
		throw new PrivilegeException(sess, "blockServiceOnDestination");
	}

	@Override
	public void blockAllServicesOnFacility(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "blockAllServicesOnFacility_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "blockAllServicesOnFacility");
		}
		getServicesManagerBl().blockAllServicesOnFacility(sess, facility);
	}

	@Override
	public void blockAllServicesOnDestination(PerunSession sess, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());


		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "blockAllServicesOnDestination_int_policy", facility)) {
				getServicesManagerBl().blockAllServicesOnDestination(sess, destinationId);
				return;
			}
		}
		throw new PrivilegeException(sess, "blockAllServicesOnDestination");
	}

	@Override
	public List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getServicesBlockedOnFacility_Facility_policy", facility)) {
			throw new PrivilegeException(perunSession, "getServicesBlockedOnFacility");
		}
		return getServicesManagerBl().getServicesBlockedOnFacility(perunSession, facility);
	}

	@Override
	public List<Service> getServicesBlockedOnDestination(PerunSession sess, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "getServicesBlockedOnDestination_int_policy", facility)) {
				return getServicesManagerBl().getServicesBlockedOnDestination(sess, destinationId);
			}
		}
		throw new PrivilegeException(sess, "getServicesBlockedOnDestination");
	}

	@Override
	public boolean isServiceBlockedOnFacility(PerunSession sess, Service service, Facility facility) throws PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "isServiceBlockedOnFacility_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "isServiceBlockedOnFacility");
		}
		return getServicesManagerBl().isServiceBlockedOnFacility(service, facility);
	}

	@Override
	public boolean isServiceBlockedOnDestination(PerunSession sess, Service service, int destinationId) throws PrivilegeException, DestinationNotExistsException, FacilityNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "isServiceBlockedOnDestination_Service_int_policy", Arrays.asList(service, facility))) {
				return getServicesManagerBl().isServiceBlockedOnDestination(service, destinationId);
			}
		}
		throw new PrivilegeException(sess, "isServiceBlockedOnDestination");
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
	public void unblockAllServicesOnDestination(PerunSession sess, String destinationName) throws FacilityNotExistsException {
		List<Destination> destinations = getServicesManagerBl().getDestinations(sess);
		for(Destination destination: destinations) {
			if(destination.getDestination().equals(destinationName)) {
				List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());
				boolean servicesUnblocked = false;
				for (Facility facility : destinationFacilities) {
					if (AuthzResolver.authorizedInternal(sess, "unblockAllServicesOnDestination_String_policy", facility)) {
						getServicesManagerBl().unblockAllServicesOnDestination(sess, destination.getId());
						servicesUnblocked = true;
						break;
					}
				}
				if (!servicesUnblocked) {
					log.warn("Trying to unblock services on destination by a user who is neither perunadmin nor admin of the facility");
				}
			}
		}
	}

	@Override
	public void unblockAllServicesOnDestination(PerunSession sess, int destinationId) throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "unblockAllServicesOnDestination_int_policy", facility)) {
				getServicesManagerBl().unblockAllServicesOnDestination(sess, destinationId);
				return;
			}
		}
		throw new PrivilegeException(sess, "unblockAllServicesOnDestination");
	}

	@Override
	public void unblockServiceOnFacility(PerunSession sess, Service service, Facility facility) throws PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "unblockServiceOnFacility_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "unblockServiceOnFacility");
		}
		getServicesManagerBl().unblockServiceOnFacility(sess, service, facility);
	}

	@Override
	public void unblockServiceOnDestination(PerunSession sess, Service service, int destinationId) throws PrivilegeException, FacilityNotExistsException, DestinationNotExistsException {
		Destination destination = getServicesManagerBl().getDestinationById(sess, destinationId);
		List<Facility> destinationFacilities = perunBl.getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getDestination());

		for (Facility facility : destinationFacilities) {
			if (AuthzResolver.authorizedInternal(sess, "unblockServiceOnDestination_Service_int_policy", Arrays.asList(service, facility))) {
				getServicesManagerBl().unblockServiceOnDestination(sess, service, destinationId);
				return;
			}
		}
		throw new PrivilegeException(sess, "unblockServiceOnDestination");
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Facility facility, Service service) throws PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "forceServicePropagation_Facility_Service_policy", Arrays.asList(service, facility))) {
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
	public boolean planServicePropagation(PerunSession sess, Facility facility, Service service) throws PrivilegeException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "planServicePropagation_Facility_Service_policy", Arrays.asList(facility, service))) {
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
	public List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getFacilityAssignedServicesForGUI_Facility_policy", facility)) {
			throw new PrivilegeException(perunSession, "getFacilityAssignedServicesForGUI");
		}
		return getServicesManagerBl().getFacilityAssignedServicesForGUI(perunSession, facility);
	}

	@Override
	public Service createService(PerunSession sess, Service service) throws PrivilegeException, ServiceExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(service, "service");
		Utils.notNull(service.getName(), "service.name");

		if (!service.getName().matches(ServicesManager.SERVICE_NAME_REGEXP)) {
			throw new IllegalArgumentException("Wrong service name, service name must matches " + ServicesManager.SERVICE_NAME_REGEXP + ", but was: " + service.getName());
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createService_Service_policy")) {
			throw new PrivilegeException(sess, "createService");
		}

		return getServicesManagerBl().createService(sess, service);
	}

	@Override
	public void deleteService(PerunSession sess, Service service) throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteService_Service_policy", service)) {
			throw new PrivilegeException(sess, "deleteService");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().deleteService(sess, service);
	}

	@Override
	public void deleteService(PerunSession sess, Service service, boolean forceFlag) throws ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteService_Service_policy", service)) {
			throw new PrivilegeException(sess, "deleteService");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().deleteService(sess, service, forceFlag);
	}

	@Override
	public void updateService(PerunSession sess, Service service) throws ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateService_Service_policy", service)) {
			throw new PrivilegeException(sess, "updateService");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().updateService(sess, service);
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
	public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getServicesByAttributeDefinition_AttributeDefinition_policy")) {
			throw new PrivilegeException(sess, "getServicesByAttributeDefinition");
		}

		return getServicesManagerBl().getServicesByAttributeDefinition(sess, attributeDefinition);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Service service) throws PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getServicesManagerBl().checkServiceExists(sess, service);

		List<Resource> resources = getServicesManagerBl().getAssignedResources(sess, service);

		if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Service_policy")) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		resources.removeIf(resource -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedResources_Service_policy", Arrays.asList(service, resource)));

		return resources;
	}

	@Override
	public ServiceAttributes getHierarchicalData(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getHierarchicalData_Service_Facility_boolean_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "getHierarchicalData");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getHierarchicalData(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public HashedGenData getHashedHierarchicalData(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getHashedHierarchicalData_Service_Facility_boolean_policy", service, facility)) {
			throw new PrivilegeException(sess, "getHashedHierarchicalData");
		}

		return getServicesManagerBl().getHashedHierarchicalData(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public HashedGenData getHashedDataWithGroups(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getHashedDataWithGroups_Service_Facility_boolean_policy", service, facility)) {
			throw new PrivilegeException(sess, "getHashedDataWithGroups");
		}

		return getServicesManagerBl().getHashedDataWithGroups(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public ServiceAttributes getFlatData(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFlatData_Service_Facility_boolean_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "getFlatData");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getFlatData(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public ServiceAttributes getDataWithGroups(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDataWithGroups_Service_Facility_boolean_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "getDataWithGroups");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getDataWithGroups(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public ServiceAttributes getDataWithVos(PerunSession sess, Service service, Facility facility, boolean filterExpiredMembers) throws FacilityNotExistsException, VoNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDataWithVos_Service_Facility_boolean_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "getDataWithVos");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getDataWithVos(sess, service, facility, filterExpiredMembers);
	}

	@Override
	public List<ServicesPackage> getServicesPackages(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getServicesPackages_policy")) {
			throw new PrivilegeException(sess, "getServicesPackages");
		}

		return getServicesManagerBl().getServicesPackages(sess);
	}

	@Override
	public ServicesPackage getServicesPackageById(PerunSession sess, int servicesPackageId) throws ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getServicesPackageById_int_policy")) {
			throw new PrivilegeException(sess, "getServicesPackageById");
				}

		return getServicesManagerBl().getServicesPackageById(sess, servicesPackageId);
	}

	@Override
	public ServicesPackage getServicesPackageByName(PerunSession sess, String servicesPackageName) throws ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(servicesPackageName, "servicesPackageName");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getServicesPackageByName_String_policy")) {
			throw new PrivilegeException(sess, "getServicesPackageByName");
		}

		return getServicesManagerBl().getServicesPackageByName(sess, servicesPackageName);
	}

	@Override
	public ServicesPackage createServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws PrivilegeException, ServicesPackageExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createServicesPackage_ServicesPackage_policy", servicesPackage)) {
			throw new PrivilegeException(sess, "createServicesPackage");
		}

		Utils.notNull(servicesPackage, "servicesPackage");

		return getServicesManagerBl().createServicesPackage(sess, servicesPackage);
	}

	@Override
	public void updateServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateServicesPackage_ServicesPackage_policy", servicesPackage)) {
			throw new PrivilegeException(sess, "updateServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getServicesManagerBl().updateServicesPackage(sess, servicesPackage);
	}

	@Override
	public void deleteServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException, RelationExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteServicesPackage_ServicesPackage_policy", servicesPackage)) {
			throw new PrivilegeException(sess, "deleteServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getServicesManagerBl().deleteServicesPackage(sess, servicesPackage);
	}

	@Override
	public void addServiceToServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addServiceToServicesPackage_ServicesPackage_Service_policy", Arrays.asList(service, servicesPackage))) {
			throw new PrivilegeException(sess, "addServiceToServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);
		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().addServiceToServicesPackage(sess, servicesPackage, service);
	}

	@Override
	public void removeServiceFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyRemovedFromServicePackageException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeServiceFromServicesPackage_ServicesPackage_Service_policy", Arrays.asList(service, servicesPackage))) {
			throw new PrivilegeException(sess, "removeServiceFromServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);
		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().removeServiceFromServicesPackage(sess, servicesPackage, service);
	}

	@Override
	public List<Service> getServicesFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getServicesFromServicesPackage_ServicesPackage_policy", servicesPackage)) {
			throw new PrivilegeException(sess, "getServicesFromServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		return getServicesManagerBl().getServicesFromServicesPackage(sess, servicesPackage);
	}

	@Override
	public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException {
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
	public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addRequiredAttributes_Service_List<AttributeDefinition>_policy", service)) {
			throw new PrivilegeException(sess, "addRequiredAttributes");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

		getServicesManagerBl().addRequiredAttributes(sess, service, attributes);
	}

	@Override
	public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeRequiredAttribute_Service_AttributeDefinition_policy", service)) {
			throw new PrivilegeException(sess, "removeRequiredAttribute");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attribute);

		getServicesManagerBl().removeRequiredAttribute(sess, service, attribute);
	}

	@Override
	public void removeRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeRequiredAttributes_Service_List<AttributeDefinition>_policy", service)) {
			throw new PrivilegeException(sess, "removeRequiredAttributes");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

		getServicesManagerBl().removeRequiredAttributes(sess, service, attributes);
	}

	@Override
	public void removeAllRequiredAttributes(PerunSession sess, Service service) throws PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeAllRequiredAttributes_Service_policy", service)) {
			throw new PrivilegeException(sess, "removeRequiredAttribute");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getServicesManagerBl().removeAllRequiredAttributes(sess, service);
	}

	@Override
	public Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, WrongPatternException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(services, "services");
		Utils.checkDestinationType(destination);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy", facility)) {
			throw new PrivilegeException(perunSession, "addDestination");
		}

		//prepare lists of facilities
		List<Facility> facilitiesByHostname;
		List<Facility> facilitiesByDestination = new ArrayList<>();
		if(destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONWINDOWS) ||
				destination.getType().equals(Destination.DESTINATIONWINDOWSPROXY)) {
			facilitiesByHostname = getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(perunSession, destination.getHostNameFromDestination());
			if(facilitiesByHostname.isEmpty()) facilitiesByDestination = getPerunBl().getFacilitiesManagerBl().getFacilitiesByDestination(perunSession, destination.getHostNameFromDestination());

			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy", facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("addDestination");
			}

			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.authorizedInternal(perunSession, "addDestination_List<Service>_Facility_Destination_policy", facilityByDestination)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("addDestination");
			}
				}

		for(Service s: services) {
			getServicesManagerBl().checkServiceExists(perunSession, s);
		}
		Utils.notNull(destination, "destination");
		Utils.notNull(destination.getDestination(), "destination.destination");
		Utils.notNull(destination.getType(), "destination.type");

		return getServicesManagerBl().addDestination(perunSession, services, facility, destination);
	}

	@Override
	public Destination addDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException {
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
		if(destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONWINDOWS) ||
				destination.getType().equals(Destination.DESTINATIONWINDOWSPROXY)) {
			facilitiesByHostname = getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(sess, destination.getHostNameFromDestination());
			if(facilitiesByHostname.isEmpty()) facilitiesByDestination = getPerunBl().getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getHostNameFromDestination());

			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.authorizedInternal(sess, "addDestination_Service_Facility_Destination_policy", facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("You have no right to add this destination.");
			}

			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.authorizedInternal(sess, "addDestination_Service_Facility_Destination_policy", facilityByDestination)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("You have no right to add this destination.");
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
	public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyRemovedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeDestination_Service_Facility_Destination_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "removeDestination");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		Utils.notNull(destination, "destination");
		Utils.notNull(destination.getDestination(), "destination.destination");
		Utils.notNull(destination.getType(), "destination.type");

		getServicesManagerBl().removeDestination(sess, service, facility, destination);
	}

	@Override
	public Destination getDestinationById(PerunSession sess, int id) throws PrivilegeException, DestinationNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDestinationById_int_policy")) {
			throw new PrivilegeException(sess, "getDestinationById");
				}

		return getServicesManagerBl().getDestinationById(sess, id);
	}

	@Override
	public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDestinations_Service_Facility_policy", Arrays.asList(service, facility))) {
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
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllRichDestinations_Facility_policy", facility)) {
			throw new PrivilegeException(perunSession, "getAllRichDestinations");
				}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, facility);
	}

	@Override
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws PrivilegeException, ServiceNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllRichDestinations_Service_policy", service)) {
			throw new PrivilegeException(perunSession, "getAllRichDestinations");
		}

		getServicesManagerBl().checkServiceExists(perunSession, service);
		return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, service);
	}

	@Override
	public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getRichDestinations_Facility_Service_policy", Arrays.asList(facility, service))) {
			throw new PrivilegeException(perunSession, "getRichDestinations");
				}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		getServicesManagerBl().checkServiceExists(perunSession, service);
		return getPerunBl().getServicesManagerBl().getRichDestinations(perunSession, facility, service);
	}


	@Override
	public void removeAllDestinations(PerunSession sess, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeAllDestinations_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(sess, "removeAllDestinations");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		getServicesManagerBl().removeAllDestinations(sess, service, facility);
	}

	@Override
	public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getServicesManagerBl().getFacilitiesDestinations(sess, vo);
	}

	@Override
	public int getDestinationIdByName(PerunSession sess, String name, String type) throws DestinationNotExistsException {
		return servicesManagerBl.getDestinationIdByName(sess, name, type);
	}

	@Override
	public List<Service> getAssignedServices(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedServices_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAssignedServices");
		}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getAssignedServices(sess, facility);
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the servicesManagerBl for this instance.
	 *
	 * @param servicesManagerBl The servicesManagerBl.
	 */
	public void setServicesManagerBl(ServicesManagerBl servicesManagerBl)
	{
		this.servicesManagerBl = servicesManagerBl;
	}

	public ServicesManagerBl getServicesManagerBl() {
		return this.servicesManagerBl;
	}

	@Override
	public List<Destination> addDestinationsForAllServicesOnFacility(PerunSession sess, Facility facility, Destination destination) throws PrivilegeException,
			FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException {
					 Utils.checkPerunSession(sess);
					 Utils.checkDestinationType(destination);

					 // Authorization
					 if (!AuthzResolver.authorizedInternal(sess, "addDestinationsForAllServicesOnFacility_Facility_Destination_policy", Arrays.asList(facility, destination))) {
						 throw new PrivilegeException(sess, "addDestinationsForAllServices");
					 }

					 getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
					 Utils.notNull(destination, "destination");
					 Utils.notNull(destination.getDestination(), "destination.destination");
					 Utils.notNull(destination.getType(), "destination.type");

					 return getServicesManagerBl().addDestinationsForAllServicesOnFacility(sess, facility, destination);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException {
		Utils.checkPerunSession(perunSession);

		// Auhtorization
		if (!AuthzResolver.authorizedInternal(perunSession, "addDestinationsDefinedByHostsOnFacility_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
		}

		getServicesManagerBl().checkServiceExists(perunSession, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, service, facility);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) throws PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(services, "services");

		for(Service s: services) {
			getServicesManagerBl().checkServiceExists(perunSession, s);
		}
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		// Authorization
		for (Service service: services) {
			if (!AuthzResolver.authorizedInternal(perunSession, "addDestinationsDefinedByHostsOnFacility_List<Services>_Facility_policy", service, facility)) {
				throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
			}
		}

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, services, facility);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);

		// Auhtorization
		if (!AuthzResolver.authorizedInternal(perunSession, "addDestinationsDefinedByHostsOnFacility_Facility_policy", facility)) {
			throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
		}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, facility);
	}

	@Override
	public int getDestinationsCount(PerunSession sess) {
		Utils.checkPerunSession(sess);

		return getServicesManagerBl().getDestinationsCount(sess);
	}
}
