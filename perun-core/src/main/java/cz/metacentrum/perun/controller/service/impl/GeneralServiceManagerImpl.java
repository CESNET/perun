package cz.metacentrum.perun.controller.service.impl;

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
import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.taskslib.dao.ServiceDenialDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Propagation manager allows to plan/force propagation, block/unblock Services on Facilities and Destinations.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal
 */
@Transactional
@org.springframework.stereotype.Service(value = "generalServiceManager")
public class GeneralServiceManagerImpl implements GeneralServiceManager {

	private final static Logger log = LoggerFactory.getLogger(GeneralServiceManagerImpl.class);
	// Beginning of the auditer message which triggers service propagation

	@Autowired
	private ServiceDenialDao serviceDenialDao;
	@Autowired
	private ServicesManager servicesManager;
	@Autowired
	private GeneralServiceManager generalServiceManager;

	@Override
	@Transactional(rollbackFor = ServiceAlreadyBannedException.class , propagation = Propagation.NESTED)
	public void blockServiceOnFacility(PerunSession sess, Service service, Facility facility) throws InternalErrorException, ServiceAlreadyBannedException {
		try {
			getServiceDenialDao().blockServiceOnFacility(service.getId(), facility.getId());
		} catch (DuplicateKeyException ex) {
			throw new ServiceAlreadyBannedException(service, facility);
		}
		sess.getPerun().getAuditer().log(sess, new BanServiceOnFacility(service, facility));
	}

	@Override
	@Transactional(rollbackFor = ServiceAlreadyBannedException.class , propagation = Propagation.NESTED)
	public void blockServiceOnDestination(PerunSession sess, Service service, int destinationId) throws InternalErrorException, PrivilegeException, DestinationNotExistsException, ServiceAlreadyBannedException {
		Destination destination = servicesManager.getDestinationById(sess, destinationId);
		try {
			getServiceDenialDao().blockServiceOnDestination(service.getId(), destinationId);
		} catch (DuplicateKeyException ex) {
			throw new ServiceAlreadyBannedException(service, destination);
		}
		sess.getPerun().getAuditer().log(sess, new BanServiceOnDestination(service, destinationId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void blockAllServicesOnFacility(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		List<Service> services = servicesManager.getAssignedServices(sess, facility);
		for (Service service : services) {
			try {
				getGeneralServiceManager().blockServiceOnFacility(sess, service, facility);
			} catch (ServiceAlreadyBannedException e) {
				// we ignore, that service was already blocked
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void blockAllServicesOnDestination(PerunSession sess, int destinationId) throws InternalErrorException, PrivilegeException, DestinationNotExistsException {
		List<Service> services = serviceDenialDao.getServicesFromDestination(destinationId);
		for (Service service : services) {
			try {
				getGeneralServiceManager().blockServiceOnDestination(sess, service, destinationId);
			} catch (ServiceAlreadyBannedException e) {
				// we ignore, that service was already blocked
			}
		}
	}

	@Override
	public List<Service> getServicesBlockedOnFacility(PerunSession perunSession, Facility facility) {
		return serviceDenialDao.getServicesBlockedOnFacility(facility.getId());
	}

	@Override
	public List<Service> getServicesBlockedOnDestination(PerunSession perunSession, int destinationId) {
		return serviceDenialDao.getServicesBlockedOnDestination(destinationId);
	}

	@Override
	public boolean isServiceBlockedOnFacility(Service service, Facility facility) {
		return serviceDenialDao.isServiceBlockedOnFacility(service.getId(), facility.getId());
	}

	@Override
	public boolean isServiceBlockedOnDestination(Service service, int destinationId) {
		return serviceDenialDao.isServiceBlockedOnDestination(service.getId(), destinationId);
	}

	@Override
	public void unblockAllServicesOnFacility(PerunSession sess, Facility facility) {
		serviceDenialDao.unblockAllServicesOnFacility(facility.getId());
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
		serviceDenialDao.unblockAllServicesOnDestination(destinationId);
		sess.getPerun().getAuditer().log(sess, new FreeAllDenialsOnDestination(destinationId));
	}

	@Override
	public void unblockServiceOnFacility(PerunSession sess, Service service, Facility facility) {
		serviceDenialDao.unblockServiceOnFacility(service.getId(), facility.getId());
		sess.getPerun().getAuditer().log(sess, new FreeDenialServiceOnFacility(service, facility));
	}

	@Override
	public void unblockServiceOnDestination(PerunSession sess, Service service, int destinationId) {
		serviceDenialDao.unblockServiceOnDestination(service.getId(), destinationId);
		sess.getPerun().getAuditer().log(sess, new FreeDenialServiceOnDestination(service, destinationId));
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Facility facility, Service service) {
		//Global
		if(!service.isEnabled()) return false;
		//Local
		if(serviceDenialDao.isServiceBlockedOnFacility(service.getId(), facility.getId())) return false;
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
		if(serviceDenialDao.isServiceBlockedOnFacility(service.getId(), facility.getId())) return false;
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
	public List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException {

		// result list
		List<ServiceForGUI> result = new ArrayList<>();
		// get assigned services
		List<Service> services = getServicesManager().getAssignedServices(perunSession, facility);
		for (Service service : services){
			// new ServiceForGUI
			ServiceForGUI newService = new ServiceForGUI(service);
			newService.setAllowedOnFacility(!serviceDenialDao.isServiceBlockedOnFacility(service.getId(), facility.getId()));
			result.add(newService);
		}
		return result;

	}

	public ServiceDenialDao getServiceDenialDao() {
		return serviceDenialDao;
	}

	public void setServiceDenialDao(ServiceDenialDao serviceDenialDao) {
		this.serviceDenialDao = serviceDenialDao;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

}
