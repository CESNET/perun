package cz.metacentrum.perun.core.entry;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Role;
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
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;

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
	public Service createService(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(service, "service");
		Utils.notNull(service.getName(), "service.name");

		if (!service.getName().matches(ServicesManager.SERVICE_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong service name, service name must matches " + ServicesManager.SERVICE_NAME_REGEXP + ", but was: " + service.getName()));
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createService");
		}

		return getServicesManagerBl().createService(sess, service);
	}

	@Override
	public void deleteService(PerunSession sess, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteService");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().deleteService(sess, service);
	}

	@Override
	public void updateService(PerunSession sess, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "updateService");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().updateService(sess, service);
	}

	@Override
	public Service getServiceById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getServiceById");
				}

		return getServicesManagerBl().getServiceById(sess, id);
	}

	@Override
	public Service getServiceByName(PerunSession sess, String name) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getServiceByName");
				}

		Utils.notNull(name, "name");

		return getServicesManagerBl().getServiceByName(sess, name);
	}

	@Override
	public List<Service> getServices(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			throw new PrivilegeException(sess, "getServices");
				}

		return getServicesManagerBl().getServices(sess);
	}

	@Override
	public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getServicesByAttributeDefinition");
		}

		return getServicesManagerBl().getServicesByAttributeDefinition(sess, attributeDefinition);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) && !AuthzResolver.isAuthorized(sess, Role.ENGINE) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {

			throw new PrivilegeException(sess, "getAssignedResources");
		}

		getServicesManagerBl().checkServiceExists(sess, service);

		return getServicesManagerBl().getAssignedResources(sess, service);
	}

	@Override
	public ServiceAttributes getHierarchicalData(PerunSession sess, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
		    !AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getHierarchicalData");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getHierarchicalData(sess, service, facility);
	}

	@Override
	public ServiceAttributes getFlatData(PerunSession sess, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
		    !AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getFlatData");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getFlatData(sess, service, facility);
	}

	@Override
	public ServiceAttributes getDataWithGroups(PerunSession sess, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
		    !AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getDataWithGroups");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getDataWithGroups(sess, service, facility);
	}

	@Override
	public ServiceAttributes getDataWithVos(PerunSession sess, Service service, Facility facility) throws InternalErrorException, FacilityNotExistsException, VoNotExistsException, ServiceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
		    !AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getDataWithVos");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getDataWithVos(sess, service, facility);
	}

	@Override
	public List<ServicesPackage> getServicesPackages(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {
			throw new PrivilegeException(sess, "getServicesPackages");
		}

		return getServicesManagerBl().getServicesPackages(sess);
	}

	@Override
	public ServicesPackage getServicesPackageById(PerunSession sess, int servicesPackageId) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getServicesPackageById");
				}

		return getServicesManagerBl().getServicesPackageById(sess, servicesPackageId);
	}

	@Override
	public ServicesPackage getServicesPackageByName(PerunSession sess, String servicesPackageName) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(servicesPackageName, "servicesPackageName");

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {
			throw new PrivilegeException(sess, "getServicesPackageByName");
		}

		return getServicesManagerBl().getServicesPackageByName(sess, servicesPackageName);
	}

	@Override
	public ServicesPackage createServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ServicesPackageExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createServicesPackage");
		}

		Utils.notNull(servicesPackage, "servicesPackage");

		return getServicesManagerBl().createServicesPackage(sess, servicesPackage);
	}

	@Override
	public void updateServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "updateServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getServicesManagerBl().updateServicesPackage(sess, servicesPackage);
	}

	@Override
	public void deleteServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException, RelationExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		getServicesManagerBl().deleteServicesPackage(sess, servicesPackage);
	}

	@Override
	public void addServiceToServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "addServiceToServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);
		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().addServiceToServicesPackage(sess, servicesPackage, service);
	}

	@Override
	public void removeServiceFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServicesPackageNotExistsException, ServiceNotExistsException, PrivilegeException, ServiceAlreadyRemovedFromServicePackageException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeServiceFromServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);
		getServicesManagerBl().checkServiceExists(sess, service);

		getServicesManagerBl().removeServiceFromServicesPackage(sess, servicesPackage, service);
	}

	@Override
	public List<Service> getServicesFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {
			throw new PrivilegeException(sess, "getServicesFromServicesPackage");
		}

		getServicesManagerBl().checkServicesPackageExists(sess, servicesPackage);

		return getServicesManagerBl().getServicesFromServicesPackage(sess, servicesPackage);
	}

	@Override
	public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "addRequiredAttribute");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attribute);

		getServicesManagerBl().addRequiredAttribute(sess, service, attribute);
	}

	@Override
	public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "addRequiredAttributes");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

		getServicesManagerBl().addRequiredAttributes(sess, service, attributes);
	}

	@Override
	public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeRequiredAttribute");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attribute);

		getServicesManagerBl().removeRequiredAttribute(sess, service, attribute);
	}

	@Override
	public void removeRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ServiceNotExistsException, AttributeNotAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeRequiredAttributes");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getAttributesManagerBl().checkAttributesExists(sess, attributes);

		getServicesManagerBl().removeRequiredAttributes(sess, service, attributes);
	}

	@Override
	public void removeAllRequiredAttributes(PerunSession sess, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeRequiredAttribute");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getServicesManagerBl().removeAllRequiredAttributes(sess, service);
	}

	@Override
	public Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(services, "services");
		Utils.checkDestinationType(destination);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(perunSession, "addDestination");
		}

		//prepare lists of facilities
		List<Facility> facilitiesByHostname = new ArrayList<Facility>();
		List<Facility> facilitiesByDestination = new ArrayList<Facility>();
		if(destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE)) {
			facilitiesByHostname = getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(perunSession, destination.getHostNameFromDestination());
			if(facilitiesByHostname.isEmpty()) facilitiesByDestination = getPerunBl().getFacilitiesManagerBl().getFacilitiesByDestination(perunSession, destination.getHostNameFromDestination());

			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("You have no right to add this destination.");
			}

			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facilityByDestination)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("You have no right to add this destination.");
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
	public Destination addDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException, PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException {
		Utils.checkPerunSession(sess);
		Utils.checkDestinationType(destination);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addDestination");
		}

		//prepare lists of facilities
		List<Facility> facilitiesByHostname = new ArrayList<Facility>();
		List<Facility> facilitiesByDestination = new ArrayList<Facility>();
		if(destination.getType().equals(Destination.DESTINATIONHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTTYPE) ||
				destination.getType().equals(Destination.DESTINATIONUSERHOSTPORTTYPE)) {
			facilitiesByHostname = getPerunBl().getFacilitiesManagerBl().getFacilitiesByHostName(sess, destination.getHostNameFromDestination());
			if(facilitiesByHostname.isEmpty()) facilitiesByDestination = getPerunBl().getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination.getHostNameFromDestination());

			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(!hasRight) throw new PrivilegeException("You have no right to add this destination.");
			}

			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByDestination)) {
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
	public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException, PrivilegeException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyRemovedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "removeDestination");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		Utils.notNull(destination, "destination");
		Utils.notNull(destination.getDestination(), "destination.destination");
		Utils.notNull(destination.getType(), "destination.type");

		getServicesManagerBl().removeDestination(sess, service, facility, destination);
	}

	@Override
	public Destination getDestinationById(PerunSession sess, int id) throws PrivilegeException, InternalErrorException, DestinationNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getDestinationById");
				}

		return getServicesManagerBl().getDestinationById(sess, id);
	}

	@Override
	public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException, PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getDestinations");
				}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getServicesManagerBl().getDestinations(sess, service, facility);
	}

	@Override
	public List<Destination> getDestinations(PerunSession perunSession) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(perunSession);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "getDestinations");
		}

		return getServicesManagerBl().getDestinations(perunSession);
	}

	@Override
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(perunSession, Role.ENGINE)) {
			throw new PrivilegeException(perunSession, "getAllRichDestinations");
				}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, facility);
	}

	@Override
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) throw new PrivilegeException(perunSession, "getAllRichDestinations");

		getServicesManagerBl().checkServiceExists(perunSession, service);
		return getPerunBl().getServicesManagerBl().getAllRichDestinations(perunSession, service);
	}

	@Override
	public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException{
		Utils.checkPerunSession(perunSession);

		//Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(perunSession, Role.ENGINE)) {
			throw new PrivilegeException(perunSession, "getRichDestinations");
				}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		getServicesManagerBl().checkServiceExists(perunSession, service);
		return getPerunBl().getServicesManagerBl().getRichDestinations(perunSession, facility, service);
	}


	@Override
	public void removeAllDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException, PrivilegeException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeAllDestinations");
		}

		getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		getServicesManagerBl().removeAllDestinations(sess, service, facility);
	}

	@Override
	public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getServicesManagerBl().getFacilitiesDestinations(sess, vo);
	}

	@Override
	public int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException {
		return servicesManagerBl.getDestinationIdByName(sess, name);
	}

	@Override
	public List<Service> getAssignedServices(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
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
			InternalErrorException, FacilityNotExistsException, DestinationAlreadyAssignedException, WrongPatternException {
					 Utils.checkPerunSession(sess);
					 Utils.checkDestinationType(destination);

					 // Authorization
					 if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
						 throw new PrivilegeException(sess, "addDestinationsForAllServices");
					 }

					 getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
					 Utils.notNull(destination, "destination");
					 Utils.notNull(destination.getDestination(), "destination.destination");
					 Utils.notNull(destination.getType(), "destination.type");

					 return getServicesManagerBl().addDestinationsForAllServicesOnFacility(sess, facility, destination);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException {
		Utils.checkPerunSession(perunSession);

		// Auhtorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
		}

		getServicesManagerBl().checkServiceExists(perunSession, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, service, facility);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(services, "services");

		// Auhtorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
		}

		for(Service s: services) {
			getServicesManagerBl().checkServiceExists(perunSession, s);
		}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, services, facility);
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);

		// Auhtorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(perunSession, "addDestinationsDefinedByHostsOnFacility");
		}

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		return getServicesManagerBl().addDestinationsDefinedByHostsOnFacility(perunSession, facility);
	}

	@Override
	public int getDestinationsCount(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		return getServicesManagerBl().getDestinationsCount(sess);
	}
}
