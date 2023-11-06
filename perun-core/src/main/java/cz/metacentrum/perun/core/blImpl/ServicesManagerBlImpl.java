package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.BanServiceOnDestination;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.BanServiceOnFacility;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.ForcePropagationOnFacilityAndService;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.ForcePropagationOnService;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.FreeAllDenialsOnDestination;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.FreeAllDenialsOnFacility;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.FreeDenialServiceOnDestination;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.FreeDenialServiceOnFacility;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.PropagationPlannedOnFacilityAndService;
import cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents.PropagationPlannedOnService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.AllRequiredAttributesRemovedFromService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.AttributeAddedAsRequiredToService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.AttributesAddedAsRequiredToService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationAddedToServiceAndFacility;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationCreated;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationRemovedFromService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationsRemovedFromAllServices;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.DestinationsRemovedFromService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.RequiredAttributeRemovedFromService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.RequiredAttributesRemovedFromService;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServiceAddedToServicePackage;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServiceCreated;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServiceDeleted;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServiceRemovedFromServicesPackage;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServiceUpdated;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServicesPackageCreated;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServicesPackageDeleted;
import cz.metacentrum.perun.audit.events.ServicesManagerEvents.ServicesPackageUpdated;
import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAttributesCannotExtend;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ServicesManagerImplApi;
import cz.metacentrum.perun.core.provisioning.GroupsHashedDataGenerator;
import cz.metacentrum.perun.core.provisioning.HashedDataGenerator;
import cz.metacentrum.perun.core.provisioning.HierarchicalHashedDataGenerator;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class ServicesManagerBlImpl implements ServicesManagerBl {

	final static Logger log = LoggerFactory.getLogger(ServicesManagerBlImpl.class);

	private final ServicesManagerImplApi servicesManagerImpl;
	private PerunBl perunBl;

	public ServicesManagerBlImpl(ServicesManagerImplApi servicesManagerImpl) {
		this.servicesManagerImpl = servicesManagerImpl;
	}

	@Override
	public void blockServiceOnFacility(PerunSession sess, Service service, Facility facility) throws ServiceAlreadyBannedException {
		getServicesManagerImpl().blockServiceOnFacility(sess, service.getId(), facility.getId());
		sess.getPerun().getAuditer().log(sess, new BanServiceOnFacility(service, facility));
	}

	@Override
	public void blockServiceOnDestination(PerunSession sess, Service service, int destinationId) throws ServiceAlreadyBannedException {
		getServicesManagerImpl().blockServiceOnDestination(sess, service.getId(), destinationId);
		sess.getPerun().getAuditer().log(sess, new BanServiceOnDestination(service, destinationId));
	}

	@Override
	public void blockAllServicesOnFacility(PerunSession sess, Facility facility) {
		List<Service> services = getAssignedServices(sess, facility);
		for (Service service : services) {
			try {
				getServicesManagerImpl().blockServiceOnFacility(sess, service.getId(), facility.getId());
				sess.getPerun().getAuditer().log(sess, new BanServiceOnFacility(service, facility));
			} catch (ServiceAlreadyBannedException e) {
				// we ignore, that service was already blocked
			}
		}
	}

	@Override
	public void blockAllServicesOnDestination(PerunSession sess, int destinationId) throws PrivilegeException, DestinationNotExistsException {
		List<Service> services = getServicesManagerImpl().getServicesFromDestination(destinationId);
		for (Service service : services) {
			try {
				getServicesManagerImpl().blockServiceOnDestination(sess, service.getId(), destinationId);
				sess.getPerun().getAuditer().log(sess, new BanServiceOnDestination(service, destinationId));
			} catch (ServiceAlreadyBannedException e) {
				// we ignore, that service was already blocked
			}
		}
	}

	@Override
	public List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility) {
		return getServicesManagerImpl().getServicesBlockedOnFacility(facility.getId());
	}

	@Override
	public List<Service> getServicesBlockedOnDestination(PerunSession perunSession, int destinationId) {
		return getServicesManagerImpl().getServicesBlockedOnDestination(destinationId);
	}

	@Override
	public boolean isServiceBlockedOnFacility(Service service, Facility facility) {
		return getServicesManagerImpl().isServiceBlockedOnFacility(service.getId(), facility.getId());
	}

	@Override
	public boolean isServiceBlockedOnDestination(Service service, int destinationId) {
		return getServicesManagerImpl().isServiceBlockedOnDestination(service.getId(), destinationId);
	}

	@Override
	public void unblockAllServicesOnFacility(PerunSession sess, Facility facility) {
		getServicesManagerImpl().unblockAllServicesOnFacility(facility.getId());
		sess.getPerun().getAuditer().log(sess, new FreeAllDenialsOnFacility(facility));
	}

	@Override
	public void unblockAllServicesOnDestination(PerunSession sess, String destinationName) {
		List<Destination> destinations = ((PerunBlImpl) sess.getPerun()).getServicesManagerBl().getDestinations(sess);
		for(Destination destination: destinations) {
			if(destination.getDestination().equals(destinationName)) this.unblockAllServicesOnDestination(sess, destination.getId());
		}
	}

	@Override
	public void unblockAllServicesOnDestination(PerunSession sess, int destinationId) {
		getServicesManagerImpl().unblockAllServicesOnDestination(destinationId);
		sess.getPerun().getAuditer().log(sess, new FreeAllDenialsOnDestination(destinationId));
	}

	@Override
	public void unblockServiceOnFacility(PerunSession sess, Service service, Facility facility) {
		getServicesManagerImpl().unblockServiceOnFacility(service.getId(), facility.getId());
		sess.getPerun().getAuditer().log(sess, new FreeDenialServiceOnFacility(service, facility));
	}

	@Override
	public void unblockServiceOnDestination(PerunSession sess, Service service, int destinationId) {
		getServicesManagerImpl().unblockServiceOnDestination(service.getId(), destinationId);
		sess.getPerun().getAuditer().log(sess, new FreeDenialServiceOnDestination(service, destinationId));
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Facility facility, Service service) {
		//Global
		if(!service.isEnabled()) return false;
		//Local
		if(getServicesManagerImpl().isServiceBlockedOnFacility(service.getId(), facility.getId())) return false;
		//Call log method out of transaction
		sess.getPerun().getAuditer().log(sess, new ForcePropagationOnFacilityAndService(facility, service));
		return true;
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Service service) {
		//Global
		if(!service.isEnabled()) return false;
		//Call log method out of transaction
		sess.getPerun().getAuditer().log(sess, new ForcePropagationOnService(service));
		return true;
	}

	@Override
	public boolean planServicePropagation(PerunSession perunSession, Facility facility, Service service) {
		//Global
		if(!service.isEnabled()) return false;
		//Local
		if(getServicesManagerImpl().isServiceBlockedOnFacility(service.getId(), facility.getId())) return false;
		//Call log method out of transaction
		perunSession.getPerun().getAuditer().log(perunSession, new PropagationPlannedOnFacilityAndService(facility, service));
		return true;
	}

	@Override
	public boolean planServicePropagation(PerunSession perunSession, Service service) {
		//Global
		if(!service.isEnabled()) return false;
		//Call log method out of transaction
		perunSession.getPerun().getAuditer().log(perunSession, new PropagationPlannedOnService(service));
		return true;
	}

	@Override
	public List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) {
		// result list
		List<ServiceForGUI> result = new ArrayList<>();
		// get assigned services
		List<Service> services = getAssignedServices(perunSession, facility);
		for (Service service : services){
			// new ServiceForGUI
			ServiceForGUI newService = new ServiceForGUI(service);
			newService.setAllowedOnFacility(!getServicesManagerImpl().isServiceBlockedOnFacility(service.getId(), facility.getId()));
			result.add(newService);
		}
		return result;
	}

	@Override
	public Service createService(PerunSession sess, Service service) throws ServiceExistsException {
		//check if service with same name exists in perun
		try {
			Service s = getServicesManagerImpl().getServiceByName(sess, service.getName());
			throw new ServiceExistsException(s);
		} catch(ServiceNotExistsException ex) { /* OK */ }

		getPerunBl().getAuditer().log(sess, new ServiceCreated(service));
		return getServicesManagerImpl().createService(sess, service);
	}

	/*
	 * Tables with reference to service:
	 *   - service_required_attrs
	 *   - service_denials
	 *   - resource_services
	 *   - facility_service_destinations
	 *   - service_service_packages
	 *   - tasks
	 *   - authz
	 */
	@Override
	public void deleteService(PerunSession perunSession, Service service, boolean forceFlag) throws RelationExistsException, ServiceAlreadyRemovedException {

		List<Resource> assignedResources = this.getAssignedResources(perunSession, service);

		if(forceFlag) {

			// Remove all denials for this service
			getServicesManagerImpl().unblockService(service.getId());

			// Remove from assigned resources
			ResourcesManagerBl resourcesManager = getPerunBl().getResourcesManagerBl();
			for(Resource resource: assignedResources) {
				try {
					resourcesManager.removeService(perunSession, resource, service);
					// Remove from facility_service_destinations
					Facility facility = getPerunBl().getFacilitiesManagerBl()
							.getFacilityById(perunSession, resource.getFacilityId());
					removeAllDestinations(perunSession, service, facility);
				} catch (ServiceNotAssignedException | FacilityNotExistsException e) {
					// should not happen
					throw new InternalErrorException("Error removing service", e);
				}
			}

			// Remove from service packages
			getServicesManagerImpl().removeServiceFromAllServicesPackages(perunSession, service);

			// Remove all related tasks
			getPerunBl().getTasksManagerBl().removeAllTasksForService(perunSession, service);

		} else {
			if (assignedResources.size() > 0) {
				throw new RelationExistsException("Service is defined on some resource");
			}
		}

		getServicesManagerImpl().removeAllRequiredAttributes(perunSession, service);
		getServicesManagerImpl().deleteService(perunSession, service);
		getPerunBl().getAuditer().log(perunSession, new ServiceDeleted(service));
	}

	@Override
	public void updateService(PerunSession sess, Service service) {
		Utils.notNull(service.getName(), "service.name");
		getServicesManagerImpl().updateService(sess, service);
		getPerunBl().getAuditer().log(sess, new ServiceUpdated(service));
	}

	@Override
	public Service getServiceById(PerunSession sess, int id) throws ServiceNotExistsException {
		return getServicesManagerImpl().getServiceById(sess, id);
	}

	@Override
	public Service getServiceByName(PerunSession sess, String name) throws ServiceNotExistsException {
		return getServicesManagerImpl().getServiceByName(sess, name);
	}

	@Override
	public List<Service> getServices(PerunSession sess) {
		return getServicesManagerImpl().getServices(sess);
	}

	@Override
	public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) {
		return getServicesManagerImpl().getServicesByAttributeDefinition(sess, attributeDefinition);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Service service) {
		return getServicesManagerImpl().getAssignedResources(sess, service);
	}

	@Override
	public HashedGenData getHashedHierarchicalData(PerunSession sess, Service service, Facility facility, boolean consentEval) {
		HashedDataGenerator hashedDataGenerator = new HierarchicalHashedDataGenerator.Builder()
				.sess((PerunSessionImpl) sess)
				.service(service)
				.facility(facility)
				.filterExpiredMembers(!service.isUseExpiredMembers())
				.consentEval(consentEval)
				.build();

		return hashedDataGenerator.generateData();
	}

	@Override
	public HashedGenData getHashedDataWithGroups(PerunSession sess, Service service, Facility facility, boolean consentEval) {
		HashedDataGenerator hashedDataGenerator = new GroupsHashedDataGenerator.Builder()
				.sess((PerunSessionImpl) sess)
				.service(service)
				.facility(facility)
				.filterExpiredMembers(!service.isUseExpiredMembers())
				.consentEval(consentEval)
				.build();

		return hashedDataGenerator.generateData();
	}

	@Override
	public List<ServicesPackage> getServicesPackages(PerunSession sess) {
		return getServicesManagerImpl().getServicesPackages(sess);
	}

	@Override
	public ServicesPackage getServicesPackageById(PerunSession sess, int servicesPackageId) throws ServicesPackageNotExistsException {
		return getServicesManagerImpl().getServicesPackageById(sess, servicesPackageId);
	}

	@Override
	public ServicesPackage getServicesPackageByName(PerunSession sess, String name) throws ServicesPackageNotExistsException {
		return getServicesManagerImpl().getServicesPackageByName(sess, name);
	}

	@Override
	public ServicesPackage createServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageExistsException {
		Utils.notNull(servicesPackage.getDescription(), "servicesPackage.getDescription()");
		Utils.notNull(servicesPackage.getName(), "servicesPackage.getName()");

		//check if servicesPackage with same name exists in perun
		try {
			ServicesPackage s = getServicesManagerImpl().getServicesPackageByName(sess, servicesPackage.getName());
			throw new ServicesPackageExistsException(s);
		} catch(ServicesPackageNotExistsException ex) { /* OK */ }

		getPerunBl().getAuditer().log(sess, new ServicesPackageCreated(servicesPackage));
		return getServicesManagerImpl().createServicesPackage(sess, servicesPackage);
	}

	@Override
	public void updateServicesPackage(PerunSession sess, ServicesPackage servicesPackage) {
		Utils.notNull(servicesPackage.getDescription(), "servicesPackage.getDescription()");
		Utils.notNull(servicesPackage.getName(), "servicesPackage.getName()");
		getServicesManagerImpl().updateServicesPackage(sess, servicesPackage);
		getPerunBl().getAuditer().log(sess, new ServicesPackageUpdated(servicesPackage));
	}

	@Override
	public void deleteServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws RelationExistsException {
		if(getServicesFromServicesPackage(sess, servicesPackage).isEmpty()) {
			getServicesManagerImpl().deleteServicesPackage(sess, servicesPackage);
			getPerunBl().getAuditer().log(sess, new ServicesPackageDeleted(servicesPackage));
		} else {
			throw new RelationExistsException("There is one or more services in the services package. ServicesPackage=\"" + servicesPackage + "\"");
		}
	}

	@Override
	public void addServiceToServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws ServiceAlreadyAssignedException {
		getServicesManagerImpl().addServiceToServicesPackage(sess, servicesPackage, service);
		getPerunBl().getAuditer().log(sess, new ServiceAddedToServicePackage(service, servicesPackage));
	}

	@Override
	public void removeServiceFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws ServiceAlreadyRemovedFromServicePackageException {
		getServicesManagerImpl().removeServiceFromServicesPackage(sess, servicesPackage, service);
		getPerunBl().getAuditer().log(sess, new ServiceRemovedFromServicesPackage(service, servicesPackage));
	}

	@Override
	public List<Service> getServicesFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage) {
		return getServicesManagerImpl().getServicesFromServicesPackage(sess, servicesPackage);
	}

	@Override
	public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws AttributeAlreadyAssignedException, ServiceAttributesCannotExtend {
		//check if attribute isn't already added
		List<AttributeDefinition> requiredAttributes = getPerunBl().getAttributesManagerBl().getRequiredAttributesDefinition(sess, service);
		if(requiredAttributes.contains(attribute)) throw new AttributeAlreadyAssignedException(attribute);

		checkCanAddAttribute(sess, service, attribute);

		getServicesManagerImpl().addRequiredAttribute(sess, service, attribute);
		getPerunBl().getAuditer().log(sess,new AttributeAddedAsRequiredToService(attribute, service));
	}

	@Override
	public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws AttributeAlreadyAssignedException, ServiceAttributesCannotExtend {
		for (AttributeDefinition attrDef : attributes) {
			checkCanAddAttribute(sess, service, attrDef);
		}
		getServicesManagerImpl().addRequiredAttributes(sess, service, attributes);
		getPerunBl().getAuditer().log(sess, new AttributesAddedAsRequiredToService(attributes, service));
	}

	@Override
	public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws AttributeNotAssignedException {
		getServicesManagerImpl().removeRequiredAttribute(sess, service, attribute);
		getPerunBl().getAuditer().log(sess, new RequiredAttributeRemovedFromService(attribute, service));
	}

	@Override
	public void removeRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws AttributeNotAssignedException {
		getServicesManagerImpl().removeRequiredAttributes(sess, service, attributes);
		getPerunBl().getAuditer().log(sess, new RequiredAttributesRemovedFromService(attributes, service));
	}

	@Override
	public void removeAllRequiredAttributes(PerunSession sess, Service service) {
		getServicesManagerImpl().removeAllRequiredAttributes(sess, service);
		getPerunBl().getAuditer().log(sess, new AllRequiredAttributesRemovedFromService(service));
	}

	@Override
	public Destination addDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws DestinationAlreadyAssignedException {
		if(!getServicesManagerImpl().destinationExists(sess, destination)) {
			try {
				//Try to get the destination without id
				Destination existingDestination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
				// pass new propagation type from API to object retrieved from DB,
				// since it always contains PARALLEL type
				existingDestination.setPropagationType(destination.getPropagationType());
				destination = existingDestination;
			} catch(DestinationNotExistsException ex) {
				try {
					destination = createDestination(sess, destination);
				} catch(DestinationExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		getServicesManagerImpl().addDestination(sess, service, facility, destination);
		getPerunBl().getAuditer().log(sess, new DestinationAddedToServiceAndFacility(destination, service, facility));
		return destination;
	}

	@Override
	public Destination addDestination(PerunSession perunSession, List<Service> services, Facility facility, Destination destination) {
		if(!getServicesManagerImpl().destinationExists(perunSession, destination)) {
			try {
				//Try to get the destination without id
				Destination existingDestination = getServicesManagerImpl().getDestination(perunSession, destination.getDestination(), destination.getType());
				// pass new propagation type from API to object retrieved from DB,
				// since it always contains PARALLEL type
				existingDestination.setPropagationType(destination.getPropagationType());
				destination = existingDestination;
			} catch(DestinationNotExistsException ex) {
				try {
					destination = createDestination(perunSession, destination);
				} catch(DestinationExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		for(Service s: services) {
			try {
				getServicesManagerImpl().addDestination(perunSession, s, facility, destination);
				getPerunBl().getAuditer().log(perunSession, new DestinationAddedToServiceAndFacility(destination, s, facility));
			} catch (DestinationAlreadyAssignedException ex) {
				// skip service
			}
		}

		return destination;
	}

	private Destination addDestinationEvenIfAlreadyExists(PerunSession sess, Service service, Facility facility, Destination destination) {
		if(!getServicesManagerImpl().destinationExists(sess, destination)) {
			try {
				//Try to get the destination without id
				destination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
			} catch(DestinationNotExistsException ex) {
				try {
					destination = createDestination(sess, destination);
				} catch(DestinationExistsException e) {
					//This is ok, destination already exists so take it from DB
					try {
						destination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
					} catch (DestinationNotExistsException exep) {
						throw new ConsistencyErrorException("Destination seems to exists and not exists in the same time. There is some other problem." + exep);
					}
				}
			}
		}
		//if destination is already assigned, do not add message to the log and only return it back

		try {
			getServicesManagerImpl().addDestination(sess, service, facility, destination);
		} catch (DestinationAlreadyAssignedException ex) {
			return destination;
		}
		getPerunBl().getAuditer().log(sess, new DestinationAddedToServiceAndFacility(destination, service, facility));
		return destination;
	}

	@Override
	public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination) {
		if(!getServicesManagerImpl().destinationExists(sess, destination)) {
			try {
				//Try to get the destination without id
				destination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
			} catch(DestinationNotExistsException ex) {
				// ignore
			}
		}

		getServicesManagerImpl().removeDestination(sess, service, facility, destination);

		// remove destination from destination table if it is not used anymore
		try {
			this.deleteDestination(sess, destination);
		} catch (RelationExistsException ex) {
			// destination is used by some services and facilities, dont delete it
		}

		getPerunBl().getAuditer().log(sess, new DestinationRemovedFromService(destination, service, facility));
	}

	@Override
	public Destination getDestinationById(PerunSession sess, int id) throws DestinationNotExistsException {
		return getServicesManagerImpl().getDestinationById(sess, id);
	}

	@Override
	public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) {
		List<Destination> destinations = getServicesManagerImpl().getDestinations(sess, service, facility);

		return destinations;
	}

	@Override
	public List<Destination> getDestinations(PerunSession perunSession) {
		List<Destination> destinations = getServicesManagerImpl().getDestinations(perunSession);

		return destinations;
	}

	@Override
	public List<Destination> getDestinations(PerunSession perunSession, Facility facility) {
		return getServicesManagerImpl().getDestinations(perunSession, facility);
	}

	@Override
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) {
		return getServicesManagerImpl().getAllRichDestinations(perunSession, facility);
	}

	@Override
	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) {
		return getServicesManagerImpl().getAllRichDestinations(perunSession, service);
	}

	@Override
	public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) {
		return getServicesManagerImpl().getRichDestinations(perunSession, facility, service);
	}

	@Override
	public void removeAllDestinations(PerunSession sess, Service service, Facility facility) {
		List<Destination> destinations = getDestinations(sess, service, facility);
		getServicesManagerImpl().removeAllDestinations(sess, service, facility);

		// remove destinations from destination table if they are not used anymore
		for (Destination destination : destinations) {
			try {
				this.deleteDestination(sess, destination);
			} catch (RelationExistsException ex) {
				// destination is used by some services and facilities or it is already removed, skip the destination
			}
		}

		getPerunBl().getAuditer().log(sess, new DestinationsRemovedFromService(service, facility));
	}

	@Override
	public void removeAllDestinations(PerunSession perunSession, Facility facility) {
		List<Destination> destinations = getDestinations(perunSession, facility);
		getServicesManagerImpl().removeAllDestinations(perunSession, facility);

		// remove destinations from destination table if they are not used anymore
		for (Destination destination : destinations) {
			try {
				this.deleteDestination(perunSession, destination);
			} catch (RelationExistsException ex) {
				// destination is used by some services and facilities or it is already removed, skip the destination
			}
		}

		getPerunBl().getAuditer().log(perunSession, new DestinationsRemovedFromAllServices(facility));
	}

	@Override
	public void checkServiceExists(PerunSession sess, Service service) throws ServiceNotExistsException {
		getServicesManagerImpl().checkServiceExists(sess, service);
	}

	@Override
	public void checkServicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws ServicesPackageNotExistsException {
		getServicesManagerImpl().checkServicesPackageExists(sess, servicesPackage);
	}

	@Override
	public int getDestinationIdByName(PerunSession sess, String name, String type) throws DestinationNotExistsException {
		return servicesManagerImpl.getDestination(sess, name, type).getId();
	}

	@Override
	public List<Service> getAssignedServices(PerunSession sess, Facility facility) {
		return servicesManagerImpl.getAssignedServices(sess, facility);
	}
	@Override
	public List<Service> getAssignedServices(PerunSession sess, Facility facility, Vo vo) {
		return servicesManagerImpl.getAssignedServices(sess, facility, vo);
	}

	public Destination createDestination(PerunSession sess, Destination destination) throws DestinationExistsException {
		if(getServicesManagerImpl().destinationExists(sess, destination)) throw new DestinationExistsException(destination);
		destination = getServicesManagerImpl().createDestination(sess, destination);
		getPerunBl().getAuditer().log(sess, new DestinationCreated(destination));
		return destination;
	}


	/**
	 * Gets the servicesManagerImpl.
	 *
	 * @return The servicesManagerImpl.
	 */
	private ServicesManagerImplApi getServicesManagerImpl() {
		return this.servicesManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}



	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public List<Destination> addDestinationsForAllServicesOnFacility(PerunSession sess, Facility facility, Destination destination)
		throws DestinationAlreadyAssignedException {
	List<Service> services = this.getAssignedServices(sess, facility);
	List<Destination> destinations = new ArrayList<>();

	for (Service service: services) {
		destinations.add(this.addDestination(sess, service, facility, destination));
	}

	return destinations;
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Service service, Facility facility) throws DestinationAlreadyAssignedException {
		// Get all hosts
		List<Host> hosts = getPerunBl().getFacilitiesManagerBl().getHosts(perunSession, facility);
		List<Destination> destinations = new ArrayList<>();

		for (Host host: hosts) {
			if (host.getHostname() != null && !host.getHostname().isEmpty()) {
				Destination destination = new Destination();
				destination.setDestination(host.getHostname());
				destination.setType(Destination.DESTINATIONHOSTTYPE);
				try {
					destinations.add(this.addDestination(perunSession, service, facility, destination));
				} catch (IllegalArgumentException e) {
					throw new ConsistencyErrorException("Destination created out of hostname has invalid name.", e);
				}
			}
		}

		return destinations;
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, List<Service> services, Facility facility) {
		List<Host> hosts = getPerunBl().getFacilitiesManagerBl().getHosts(perunSession, facility);
		List<Destination> destinations = new ArrayList<>();

		for (Service service: services) {
			for (Host host: hosts) {
				if (host.getHostname() != null && !host.getHostname().isEmpty()) {
					Destination destination = new Destination();
					destination.setDestination(host.getHostname());
					destination.setType(Destination.DESTINATIONHOSTTYPE);
					try {
						destinations.add(this.addDestinationEvenIfAlreadyExists(perunSession, service, facility, destination));
					} catch (IllegalArgumentException e) {
						throw new ConsistencyErrorException("Destination created out of hostname has invalid name.", e);
					}
				}
			}
		}
		return destinations;
	}

	@Override
	public List<Destination> addDestinationsDefinedByHostsOnFacility(PerunSession perunSession, Facility facility) {
		//First generate services
		List<Service> services = getPerunBl().getServicesManagerBl().getAssignedServices(perunSession, facility);
		return this.addDestinationsDefinedByHostsOnFacility(perunSession, services, facility);
	}


	@Override
	public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) {
		List<Destination> destinations = getServicesManagerImpl().getFacilitiesDestinations(sess, vo);
		return destinations;
	}

	@Override
	public int getDestinationsCount(PerunSession sess) {
		return getServicesManagerImpl().getDestinationsCount(sess);
	}

	@Override
	public void deleteDestination(PerunSession sess, Destination destination) throws RelationExistsException {
		List<Service> services = getServicesManagerImpl().getServicesFromDestination(destination.getId());
		if (!services.isEmpty()) {
			throw new RelationExistsException("Destination is used by some services and facilities.");
		}

		// remove task results of destination
		List<TaskResult> taskResults = getPerunBl().getTasksManagerBl().getTaskResultsByDestinations(sess, Collections.singletonList(destination.getDestination()));
		for (TaskResult taskResult : taskResults) {
			getPerunBl().getTasksManagerBl().deleteTaskResultById(sess, taskResult.getId());
		}

		// remove all service denials on destination
		this.unblockAllServicesOnDestination(sess, destination.getId());

		getServicesManagerImpl().deleteDestination(sess, destination);
	}

	/**
	 * Checks, that adding new required attribute to service which would require renewing user consents
	 * (user-related attributes on consents-requiring instances) is done for globally disabled service.
	 * Does not throw exception, if all facilities with given service are excluded from consents logic.
	 * @throws ServiceAttributesCannotExtend if trying to add attribute which would invalidate consents
	 */
	private void checkCanAddAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws ServiceAttributesCannotExtend {
		if (!Utils.isUserRelatedAttribute(attribute)) {
			return;
		}

		if (!service.isEnabled()) {
			return;
		}

		if (!BeansUtils.getCoreConfig().getForceConsents()) {
			return;
		}

		List<Integer> facilitiesIds = perunBl.getServicesManagerBl().getAssignedResources(sess, service).stream()
			.map(Resource::getFacilityId)
			.distinct()
			.toList();
		for (int facilityId : facilitiesIds) {
			try {
				ConsentHub hub = perunBl.getConsentsManagerBl().getConsentHubByFacility(sess, facilityId);
				if (hub.isEnforceConsents()) {
					throw new ServiceAttributesCannotExtend(service, facilityId);
				}
			} catch (ConsentHubNotExistsException e) {
				throw new ConsistencyErrorException("Consent hub not found for facility with id " + facilityId, e);
			}
		}
	}

	@Override
	public boolean isServiceAssignedToFacility(PerunSession sess, Facility facility, Service service) {
		return getServicesManagerImpl().isServiceAssignedToFacility(sess, facility, service);
	}
}
