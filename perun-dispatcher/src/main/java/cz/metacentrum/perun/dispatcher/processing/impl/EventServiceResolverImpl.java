package cz.metacentrum.perun.dispatcher.processing.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.PerunClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.Attribute;
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

	/**
	 * Expected string format as on: event|x|[timestamp][Event header][Event data]
	 */
	private final static String eventParsingPattern = "^\\[([a-zA-Z0-9+: ]+)\\]\\[([^\\]]+)\\]\\[(.*)\\]$";
	private final static Pattern pattern = Pattern.compile(eventParsingPattern);

	private Properties dispatcherProperties;
	private Perun perun;
	private GeneralServiceManager generalServiceManager;

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

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	@Autowired
	public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

	// ----- methods -------------------------------------

	@Override
	public Map<Facility, Set<Service>> parseEvent(String event) throws InvalidEventMessageException, ServiceNotExistsException, InternalErrorException, PrivilegeException {

		log.info("Event - I am going to process event:" + event);

		Matcher matcher = pattern.matcher(event);
		boolean matchFound = matcher.find();

		if (matchFound) {

			// TODO: will we ever user clockworkorange/portishead headers ?
			//String eventHeader = matcher.group(2);

			String eventData = matcher.group(3);

			log.debug("Event - Data to be parsed: {}", eventData);

			// GET All Beans (only PerunBeans) from message
			List<PerunBean> listOfBeans = new ArrayList<PerunBean>();
			listOfBeans = AuditParser.parseLog(eventData);

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
					log.debug("Non-existing facility found while resolving event. id={}", facility.getId());
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
							log.debug("Non-existing group found while resolving event. id={}", group.getId());
						}
					} else {
						// try to find USER in event
						if (user != null) {
							try {
								resourcesResolvedFromEvent = perun.getUsersManager()
										.getAllowedResources(perunSession, user);
							} catch (UserNotExistsException ex) {
								log.debug("Non-existing user found while resolving event. id={}", user.getId());
							}
						} else {
							// try to find MEMBER in event
							if (member != null) {
								try {
									resourcesResolvedFromEvent = perun.getResourcesManager()
											.getAllowedResources(perunSession, member);
								} catch (MemberNotExistsException ex) {
									log.debug("Non-existing member found while resolving event. id={}", member.getId());
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
										log.debug(
												"Host on non-existing facility found while resolving event. Host id={}",
												host.getId());
									} catch (HostNotExistsException ex) {
										log.debug("Non-existing host found while resolving event. id={}", host.getId());
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

			Map<Facility, Set<Service>> result = new HashMap<Facility, Set<Service>>();
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
					log.debug("Non-existing resource found while resolving event. Resource={}", r);
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

		} else {
			throw new InvalidEventMessageException("Message[" + event + "]");
		}
	}

}
