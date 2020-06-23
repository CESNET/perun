package cz.metacentrum.perun.dispatcher.processing.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.PerunClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.processing.EventServiceResolver;

/**
 * Implementation of EventServiceResolver.
 *
 * @see cz.metacentrum.perun.dispatcher.processing.EventServiceResolver
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "eventServiceResolver")
public class EventServiceResolverImpl implements EventServiceResolver {

	private static final Logger log = LoggerFactory.getLogger(EventServiceResolverImpl.class);

	private Properties dispatcherProperties;
	private Perun perun;

	private PerunSession perunSession = null;

	// ----- setters -------------------------------------

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@javax.annotation.Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public Perun getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	// ----- methods -------------------------------------

	@Override
	public Map<Facility, Set<Service>> resolveEvent(AuditEvent event) throws InvalidEventMessageException, ServiceNotExistsException, PrivilegeException {

		log.info("Event - I am going to process event: {}", event);

		Map<Facility, Set<Service>> result = new HashMap<Facility, Set<Service>>();

		if (event instanceof EngineIgnoreEvent) {
			log.info("Event ignored {} facilities will be returned", result.size());
			return result;
		}

		// GET All Beans (only PerunBeans) from message
		List<PerunBean> listOfBeans = new ArrayList<PerunBean>();
		listOfBeans = AuditParser.parseLog(event.getMessage());

		// Prepare variables
		AttributeDefinition attributeDefinition = null;
		Facility facility = null;
		Resource resource = null;
		Group group = null;
		User user = null;
		Member member = null;
		Service service = null;
		Host host = null;

		// Recognize every object in List of PerunBeans from eventData
		// TODO: What about more than 1 resources, or more than 1 facilities etc. ?
		for (PerunBean pb : listOfBeans) {
			if (pb instanceof AttributeDefinition) {
				attributeDefinition = (AttributeDefinition) pb;
			} else if (pb instanceof Facility) {
				facility = (Facility) pb;
			} else if (pb instanceof Resource) {
				resource = (Resource) pb;
			} else if (pb instanceof Group) {
				group = (Group) pb;
			} else if (pb instanceof User) {
				user = (User) pb;
			} else if (pb instanceof Member) {
				member = (Member) pb;
			} else if (pb instanceof Service) {
				service = (Service) pb;
			} else if (pb instanceof Host) {
				host = (Host) pb;
			}
		}

		// If there is any attribute, so create AttributeDefinition
		if (attributeDefinition != null) {
			log.debug("Attribute found in event. {}.", attributeDefinition);
		}

		List<Facility> facilitiesResolvedFromEvent = new ArrayList<Facility>();
		List<Resource> resourcesResolvedFromEvent = new ArrayList<Resource>();
		List<Service> servicesResolvedFromEvent = new ArrayList<Service>();

		// =============== Resolve facilities from event======================

		if (perunSession == null) {
			perunSession = perun.getPerunSession(new PerunPrincipal(
							dispatcherProperties.getProperty("perun.principal.name"),
							dispatcherProperties.getProperty("perun.principal.extSourceName"),
							dispatcherProperties.getProperty("perun.principal.extSourceType")),
					new PerunClient());
		}

		// Try to find FACILITY in event
		if (facility != null) {
			try {
				log.debug("Facility found in event. {}.", facility);
				facilitiesResolvedFromEvent.add(facility);
				resourcesResolvedFromEvent.addAll(perun.getFacilitiesManager()
						.getAssignedResources(perunSession, facility));
			} catch (FacilityNotExistsException ex) {
				log.warn("Non-existing facility found while resolving event. id={}", facility.getId());
			}
		} else {
			// Try to find RESOURCE in event
			if (resource != null) {
				resourcesResolvedFromEvent.add(resource);
			} else {
				// Try to find GROUP in event
				if (group != null) {
					try {
						resourcesResolvedFromEvent = perun.getResourcesManager()
								.getAssignedResources(perunSession, group);
					} catch (GroupNotExistsException ex) {
						log.warn("Non-existing group found while resolving event. id={}", group.getId());
					}
				} else {
					// try to find USER in event
					if (user != null) {
						try {
							resourcesResolvedFromEvent = perun.getUsersManager()
									.getAllowedResources(perunSession, user);
						} catch (UserNotExistsException ex) {
							log.warn("Non-existing user found while resolving event. id={}", user.getId());
						}
					} else {
						// try to find MEMBER in event
						if (member != null) {
							try {
								resourcesResolvedFromEvent = perun.getResourcesManager()
										.getAllowedResources(perunSession, member);
							} catch (MemberNotExistsException ex) {
								log.warn("Non-existing member found while resolving event. id={}", member.getId());
							}
						} else {
							// try to find HOST in event
							if (host != null) {
								try {
									log.debug("Host found in event.id= {}.", host.getId());
									facility = perun.getFacilitiesManager().getFacilityForHost(perunSession, host);
									facilitiesResolvedFromEvent.add(facility);
									resourcesResolvedFromEvent.addAll(perun.getFacilitiesManager()
											.getAssignedResources(perunSession, facility));
								} catch (FacilityNotExistsException ex) {
									log.warn(
											"Host on non-existing facility found while resolving event. Host id={}",
											host.getId());
								} catch (HostNotExistsException ex) {
									log.warn("Non-existing host found while resolving event. id={}", host.getId());
								}
							} else {
								log.warn("No match found for this event. Event={}", event);
							}
						}
					}
				}
			}
		}

		// Try to find SERVICE in event
		// TODO resolve more than one service
		if (service != null) {
			servicesResolvedFromEvent.add(service);
		}

		// FIXME - Following code is commented since we don't want to start propagation for messages like "ServiceUpdated".
		// Generally it could clog the propagations, when single service is assigned to the many facilities.
		// It also means, that messages to force/planServicePropagation for service (without facility specified) are ignored.
		/*
		if (servicesResolvedFromEvent.size() == 1 &&
				facilitiesResolvedFromEvent.isEmpty() &&
				resourcesResolvedFromEvent.isEmpty()) {
			// there was no proper sourcing object other than the service
			// so we will append all facilities with such service
			facilitiesResolvedFromEvent.addAll(perun.getFacilitiesManager().getAssignedFacilities(perunSession, service));
			for (Facility fac : facilitiesResolvedFromEvent) {
				try {
					resourcesResolvedFromEvent.addAll(perun.getFacilitiesManager()
							.getAssignedResources(perunSession, fac));
				} catch (FacilityNotExistsException e) {
					log.error("Facility {} was probably deleted, can't get resources for it.", fac, e);
				}
			}
		}
		*/

		for (Resource r : resourcesResolvedFromEvent) {

			Facility facilityResolvedFromEvent;
			List<Service> servicesResolvedFromResource;
			try {
				facilityResolvedFromEvent = perun.getResourcesManager().getFacility(perunSession, r);
				servicesResolvedFromResource = perun.getResourcesManager().getAssignedServices(perunSession, r);
				// process only services resolved from event if any
				if (!servicesResolvedFromEvent.isEmpty())
					servicesResolvedFromResource.retainAll(servicesResolvedFromEvent);
			} catch (ResourceNotExistsException ex) {
				log.error("Non-existing resource found while resolving event. Resource={}", r);
				continue; // skip to next resource
			}

			for (Service s : servicesResolvedFromResource) {

				if (attributeDefinition != null) {
					// remove from future processing services
					// which don't require the found attribute
					// TODO (CHECKME) This method can raise
					// ServiceNotExistsException. Is it ok? Or it must be
					// catch?
					List<AttributeDefinition> serviceRequiredAttributes = perun
							.getAttributesManager()
							.getRequiredAttributesDefinition(perunSession, s);
					if (!serviceRequiredAttributes.contains(attributeDefinition))
						continue;
				}

				if(!result.containsKey(facilityResolvedFromEvent)) {
					Set<Service> servicesToPut = new HashSet<Service>();
					servicesToPut.add(s);
					result.put(facilityResolvedFromEvent, servicesToPut);
				} else {
					result.get(facilityResolvedFromEvent).add(s);
				}
			}
		}

		log.info("{} facilities will be returned", result.size());
		return result;

	}

}
