package cz.metacentrum.perun.controller.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDao;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDenialDao;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * @author Michal Karm Babacek
 */
@Transactional
@org.springframework.stereotype.Service(value = "generalServiceManager")
public class GeneralServiceManagerImpl implements GeneralServiceManager {

	private final static Logger log = LoggerFactory.getLogger(GeneralServiceManagerImpl.class);
	// Beginning of the auditer message which triggers service propagation
	public final static String FORCE_PROPAGATION = "force propagation: ";
	public final static String FREE_ALL_DEN = "free all denials: ";
	public final static String FREE_DEN_OF_EXECSERVICE = "free denial: ";
	public final static String BAN_SERVICE = "ban :"; 

	@Autowired
	private ExecServiceDao execServiceDao;
	@Autowired
	private ExecServiceDenialDao execServiceDenialDao;
	@Autowired
	private ExecServiceDependencyDao execServiceDependencyDao;
	@Autowired
	private ServicesManager servicesManager;

	@Override
	public List<ExecService> listExecServices(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
		return execServiceDao.listExecServices();
	}

	@Override
	public List<ExecService> listExecServices(PerunSession perunSession, int serviceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
		return execServiceDao.listExecServices(serviceId);
	}

	@Override
	public int countExecServices() {
		return execServiceDao.countExecServices();
	}

	@Override
	public ExecService getExecService(PerunSession perunSession, int execServiceId) throws InternalErrorException {
		return execServiceDao.getExecService(execServiceId);
	}

	@Override
	public int insertExecService(PerunSession perunSession, ExecService execService) throws InternalErrorException, PrivilegeException, ServiceExistsException {
		Service service = null;
		try {
			service = servicesManager.getServiceByName(perunSession, execService.getService().getName());
		} catch (ServiceNotExistsException e) {
			service = servicesManager.createService(perunSession, execService.getService());
		}
		execService.setService(service);
		return execServiceDao.insertExecService(execService);
	}

	@Override
	public void updateExecService(PerunSession perunSession, ExecService execService) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
		servicesManager.updateService(perunSession, execService.getService());
		execServiceDao.updateExecService(execService);
	}

	@Override
	public void deleteExecService(ExecService execService) {
		execServiceDao.deleteExecService(execService.getId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void banExecServiceOnFacility(PerunSession sess, ExecService execService, Facility facility) throws InternalErrorException, ServiceAlreadyBannedException {
		try {
			execServiceDenialDao.banExecServiceOnFacility(execService.getId(), facility.getId());
		} catch (DuplicateKeyException ex) {
			throw new ServiceAlreadyBannedException(execService.getService(), facility);
		}
		sess.getPerun().getAuditer().log(sess, "{} {} on {}", BAN_SERVICE, execService, facility);
	}

	@Override
	public void banExecServiceOnDestination(PerunSession sess, ExecService execService, int destinationId) throws InternalErrorException {
		execServiceDenialDao.banExecServiceOnDestination(execService.getId(), destinationId);
		sess.getPerun().getAuditer().log(sess, "{} {} on {}", BAN_SERVICE, execService, destinationId);
	}

	@Override
	public List<ExecService> listDenialsForFacility(PerunSession perunSession, Facility facility) throws InternalErrorException {
		return execServiceDenialDao.listDenialsForFacility(facility.getId());
	}

	@Override
	public List<ExecService> listDenialsForDestination(PerunSession perunSession, int destinationId) throws InternalErrorException {
		return execServiceDenialDao.listDenialsForDestination(destinationId);
	}

	@Override
	public boolean isExecServiceDeniedOnFacility(ExecService execService, Facility facility) {
		return execServiceDenialDao.isExecServiceDeniedOnFacility(execService.getId(), facility.getId());
	}

	@Override
	public boolean isExecServiceDeniedOnDestination(ExecService execService, int destinationId) {
		return execServiceDenialDao.isExecServiceDeniedOnDestination(execService.getId(), destinationId);
	}
	@Override
	public void freeAllDenialsOnFacility(PerunSession sess, Facility facility) throws InternalErrorException{
		execServiceDenialDao.freeAllDenialsOnFacility(facility.getId());
		sess.getPerun().getAuditer().log(sess, "{} on {}" ,FREE_ALL_DEN, facility);
	}

	@Override
	public void freeAllDenialsOnDestination(PerunSession sess, int destinationId) throws InternalErrorException {
		execServiceDenialDao.freeAllDenialsOnDestination(destinationId);
		sess.getPerun().getAuditer().log(sess, "{} on {}", FREE_ALL_DEN, destinationId);
	}

	@Override
	public void freeDenialOfExecServiceOnFacility(PerunSession sess, ExecService execService, Facility facility) throws InternalErrorException {
		execServiceDenialDao.freeDenialOfExecServiceOnFacility(execService.getId(), facility.getId());
		sess.getPerun().getAuditer().log(sess, "{} {} on {}", FREE_DEN_OF_EXECSERVICE, execService, facility);
	}

	@Override
	public void freeDenialOfExecServiceOnDestination(PerunSession sess, ExecService execService, int destinationId) throws InternalErrorException {
		execServiceDenialDao.freeDenialOfExecServiceOnDestination(execService.getId(), destinationId);
		sess.getPerun().getAuditer().log(sess, "{} {} on {}", FREE_DEN_OF_EXECSERVICE, execService, destinationId);
	}

	@Override
	public void createDependency(ExecService dependantExecService, ExecService execService) {
		execServiceDependencyDao.createDependency(dependantExecService.getId(), execService.getId());
	}

	@Override
	public void removeDependency(ExecService dependantExecService, ExecService execService) {
		execServiceDependencyDao.removeDependency(dependantExecService.getId(), execService.getId());
	}

	@Override
	public boolean isThereDependency(ExecService dependantExecService, ExecService execService) {
		return execServiceDependencyDao.isThereDependency(dependantExecService.getId(), execService.getId());
	}

	@Override
	public List<ExecService> listExecServicesDependingOn(PerunSession perunSession, ExecService execService) throws InternalErrorException {
		return execServiceDependencyDao.listExecServicesDependingOn(execService.getId());
	}

	@Override
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService) throws InternalErrorException {
		return execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(dependantExecService.getId());
	}

	@Override
	public List<ExecService> listExecServicesThisExecServiceDependsOn(PerunSession perunSession, ExecService dependantExecService, ExecServiceType execServiceType) throws InternalErrorException {
		return execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(dependantExecService.getId(), execServiceType);
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Facility facility, Service service) throws ServiceNotExistsException, FacilityNotExistsException, InternalErrorException, PrivilegeException {
		List<ExecService> listOfExecServices = listExecServices(sess, service.getId());
		for(ExecService es: listOfExecServices) {
			//Global
			if(!es.isEnabled()) return false;
			//Local
			if(execServiceDenialDao.isExecServiceDeniedOnFacility(es.getId(), facility.getId())) return false;
		}
		//Call log method out of transaction
		sess.getPerun().getAuditer().log(sess, FORCE_PROPAGATION + "On {} and {}", facility, service);
		return true;
	}

	@Override
	public boolean forceServicePropagation(PerunSession sess, Service service) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
		List<ExecService> listOfExecServices = listExecServices(sess, service.getId());
		for(ExecService es: listOfExecServices) {
			//Global
			if(!es.isEnabled()) return false;
		}
		//Call log method out of transaction
		sess.getPerun().getAuditer().log(sess, FORCE_PROPAGATION + "On {} ", service);
		return true;
	}

	@Override
	public void deleteService(PerunSession perunSession, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException, ServiceAlreadyRemovedException {
		execServiceDao.deleteAllExecServicesByService(service.getId());
		servicesManager.deleteService(perunSession, service);
	}

	@Override
	public List<Service> listServices(PerunSession perunSession) throws InternalErrorException, PrivilegeException {
		return servicesManager.getServices(perunSession);
	}

	@Override
	public Service getService(PerunSession perunSession, int serviceId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
		return servicesManager.getServiceById(perunSession, serviceId);
	}

	@Override
	public List<ServiceForGUI> getFacilityAssignedServicesForGUI(PerunSession perunSession, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException {

		// result list
		List<ServiceForGUI> result = new ArrayList<ServiceForGUI>();
		// get assigned services
		List<Service> services = getServicesManager().getAssignedServices(perunSession, facility);
		for (Service service : services){
			// flag
			boolean allowed = true;
			// new ServiceForGUI
			ServiceForGUI newService = new ServiceForGUI(service);
			// get their exec services
			List<ExecService> execs = execServiceDao.listExecServices(service.getId());
			for (ExecService exec : execs){
				// if generate
				if (exec.getExecServiceType().equals(ExecService.ExecServiceType.GENERATE)){
					if (execServiceDenialDao.isExecServiceDeniedOnFacility(exec.getId(), facility.getId()) == true) {
						newService.setGenAllowedOnFacility(false);
						allowed = false;
					} else {
						newService.setGenAllowedOnFacility(true);
					}
					newService.setGenExecService(exec);
				} else {
					// if send
					if (execServiceDenialDao.isExecServiceDeniedOnFacility(exec.getId(), facility.getId()) == true) {
						newService.setSendAllowedOnFacility(false);
						allowed = false;
					} else {
						newService.setSendAllowedOnFacility(true);
					}
					newService.setSendExecService(exec);
				}
			}
			newService.setAllowedOnFacility(allowed);
			result.add(newService);
		}

		return result;

	}

	public ExecServiceDao getExecServiceDao() {
		return execServiceDao;
	}

	public void setExecServiceDao(ExecServiceDao execServiceDao) {
		this.execServiceDao = execServiceDao;
	}

	public ExecServiceDenialDao getExecServiceDenialDao() {
		return execServiceDenialDao;
	}

	public void setExecServiceDenialDao(ExecServiceDenialDao execServiceDenialDao) {
		this.execServiceDenialDao = execServiceDenialDao;
	}

	public ExecServiceDependencyDao getExecServiceDependencyDao() {
		return execServiceDependencyDao;
	}

	public void setExecServiceDependencyDao(ExecServiceDependencyDao execServiceDependencyDao) {
		this.execServiceDependencyDao = execServiceDependencyDao;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Service createCompleteService(PerunSession perunSession, String serviceName, String scriptPath, int defaultDelay, boolean enabled) throws InternalErrorException, PrivilegeException, ServiceExistsException {

		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "createCompleteService");
		}

		Service service = null;

		try {
			service = servicesManager.getServiceByName(perunSession, serviceName);
			if (service != null) {
				throw new ServiceExistsException(service);
			}
		} catch (ServiceNotExistsException e) {
			service = new Service();
			service.setName(serviceName);
			service = servicesManager.createService(perunSession, service);
		}

		ExecService genExecService = new ExecService();
		genExecService.setService(service);
		genExecService.setDefaultDelay(defaultDelay);
		genExecService.setEnabled(enabled);
		genExecService.setScript(scriptPath);
		genExecService.setExecServiceType(ExecServiceType.GENERATE);
		genExecService.setId(execServiceDao.insertExecService(genExecService));

		ExecService sendExecService = new ExecService();
		sendExecService.setService(service);
		sendExecService.setDefaultDelay(defaultDelay);
		sendExecService.setEnabled(enabled);
		sendExecService.setScript(scriptPath);
		sendExecService.setExecServiceType(ExecServiceType.SEND);
		sendExecService.setId(execServiceDao.insertExecService(sendExecService));

		this.createDependency(sendExecService, genExecService);

		return service;

	}
}
