package cz.metacentrum.perun.dispatcher.processing.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.dispatcher.dao.ProcessingRuleDao;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.dispatcher.model.ProcessingRule;
import cz.metacentrum.perun.dispatcher.processing.EventExecServiceResolver;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventExecServiceResolver")
public class EventExecServiceResolverImpl implements EventExecServiceResolver {
	private static final Logger log = LoggerFactory
			.getLogger(EventExecServiceResolverImpl.class);
	private volatile boolean rulesLoaded = false;
	private Map<ProcessingRule, List<ExecService>> rules = new HashMap<ProcessingRule, List<ExecService>>();
	@Autowired
	private ProcessingRuleDao processingRuleDao;
	@Autowired
	private Properties propertiesBean;
	@Autowired
	private Perun perun;
	@Autowired
	private GeneralServiceManager generalServiceManager;

	@Override
	public List<Pair<List<ExecService>, Facility>> parseEvent(String event)
			throws InvalidEventMessageException, ServiceNotExistsException,
			InternalErrorException, PrivilegeException {
		log.info("I am going to process event:" + event);
		/**
		 * Applied on the first run (the first time it's needed...lazy
		 * initialization)
		 */
		if (!rulesLoaded) {
			refreshProcessingRules();
			rulesLoaded = true;
		}

		/**
		 * Expected string format as on:
		 * https://projekty.ics.muni.cz/perunv3/trac
		 * /wiki/PerunEngineDispatcherController event|x|[timestamp][Event
		 * header][Event data]
		 */
		String eventParsingPattern = "^\\[([a-zA-Z0-9: ]+)\\]\\[([^\\]]+)\\]\\[(.*)\\]$";
		Pattern pattern = Pattern.compile(eventParsingPattern);
		Matcher matcher = pattern.matcher(event);
		boolean matchFound = matcher.find();

		if (matchFound) {
			log.debug("Message format matched ok...");
			// NOT USED ANYMORE: not applicable in dispatcher
			// String thisEngineID = matcher.group(1);
			// // This should indeed match the current Engine instance ID, so
			// let's compare it...
			// if (Integer.parseInt(thisEngineID) != Integer.parseInt((String)
			// propertiesBean.get("engine.unique.id"))) {
			// throw new InvalidEventMessageException("Wrong Engine ID. Was:" +
			// thisEngineID + ", Expected:" +
			// propertiesBean.get("engine.unique.id"));
			// }
			// // Not being used at the moment.
			// String timeStamp = matcher.group(2);
			// Header should provide information regarding the target facility.
			String eventHeader = matcher.group(2);
			// We expect the string to contain something like this:
			// facility.id=2 ???
			// String headerParsingPattern = ".*facility.id\\=([0-9]+).*";
			// Pattern headerPattern = Pattern.compile(headerParsingPattern);
			// Matcher headerMatcher = headerPattern.matcher(eventHeader);
			/*
			 * boolean headerMatchFound = headerMatcher.find();
			 * if(!headerMatchFound) { throw new InvalidEventMessageException(
			 * "Invalid event header. It does not contain the expected facility.id=value..."
			 * ); } int facilityId = Integer.parseInt(matcher.group(1));
			 * PerunSession perunSession =
			 * engineManager.getPerunSession(propertiesBean
			 * .getProperty("perun.principal")); Facility facility = null; try {
			 * facility = facilitiesManager.getFacilityById(perunSession,
			 * facilityId); } catch (FacilityNotExistsException e) { throw new
			 * InvalidEventMessageException
			 * ("Facility with ID "+facilityId+"does not exist.", e); } catch
			 * (InternalErrorException e) { throw new
			 * InvalidEventMessageException("Unknown error...", e); } catch
			 * (PrivilegeException e) { throw new
			 * InvalidEventMessageException("Principal "
			 * +propertiesBean.getProperty
			 * ("perun.principal")+" is not allowed to access that facility. ",
			 * e); }
			 */

			// Data should provide information regarding the target ExecService
			// (Processing rule).
			String eventData = matcher.group(3);

			log.debug("Event data to be parsed:" + eventData);

			// GET All Beans (only PerunBeans) from message
			List<PerunBean> listOfBeans = new ArrayList<PerunBean>();
			listOfBeans = AuditParser.parseLog(eventData);

			// Prepare variables
			AttributeDefinition attributeDefinition = null;
			Attribute attribute = null;
			Facility facility = null;
			Resource resource = null;
			Group group = null;
			User user = null;
			Member member = null;
			Service service = null;
			Host host = null;

			// Recognize every object in List of PerunBeans from eventData
			// TODO: What about more than 1 resources, or more than 1 facilities
			// etc. ?
			for (PerunBean pb : listOfBeans) {
				if (pb instanceof AttributeDefinition
						&& pb instanceof Attribute) {
					attribute = (Attribute) pb;
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
			if (attribute != null) {
				attributeDefinition = new AttributeDefinition(attribute);
				log.debug("Attribute found in event. {}.", attributeDefinition);
			}

			List<Facility> facilitiesResolvedFromEvent = new ArrayList<Facility>();
			List<Resource> resourcesResolvedFromEvent = new ArrayList<Resource>();
			List<Service> servicesResolvedFromEvent = new ArrayList<Service>();

			// =============== Resolve facilities from
			// event======================

			PerunSession perunSession = perun
					.getPerunSession(new PerunPrincipal(
							propertiesBean.getProperty("perun.principal.name"),
							propertiesBean
									.getProperty("perun.principal.extSourceName"),
							propertiesBean
									.getProperty("perun.principal.extSourceType")));
			// Try to find FACILITY in event
			if (facility != null) {
				try {
					log.debug("Facility found in event. {}.", facility);
					facilitiesResolvedFromEvent.add(facility);
					resourcesResolvedFromEvent.addAll(perun
							.getFacilitiesManager().getAssignedResources(
									perunSession, facility));
				} catch (FacilityNotExistsException ex) {
					log.debug(
							"Non-existing facility found while resolving event. id={}",
							facility.getId());
				}
			} else {
				// Try to find RESOURCE in event
				if (resource != null) {
					resourcesResolvedFromEvent.add(resource);
				} else {
					// Try to find GROUP in event
					if (group != null) {
						try {
							resourcesResolvedFromEvent = perun
									.getResourcesManager()
									.getAssignedResources(perunSession, group);
						} catch (GroupNotExistsException ex) {
							log.debug(
									"Non-existing group found while resolving event. id={}",
									group.getId());
						}
					} else {
						// try to find USER in event
						if (user != null) {
							try {
								resourcesResolvedFromEvent = perun
										.getUsersManager().getAllowedResources(
												perunSession, user);
							} catch (UserNotExistsException ex) {
								log.debug(
										"Non-existing user found while resolving event. id={}",
										user.getId());
							}
						} else {
							// try to find MEMBER in event
							if (member != null) {
								try {
									resourcesResolvedFromEvent = perun
											.getResourcesManager()
											.getAllowedResources(perunSession,
													member);
								} catch (MemberNotExistsException ex) {
									log.debug(
											"Non-existing member found while resolving event. id={}",
											member.getId());
								}
							} else {
								// try to find HOST in event
								if (host != null) {
									try {
										log.debug(
												"Host found in event.id= {}.",
												host.getId());
										facility = perun.getFacilitiesManager()
												.getFacilityForHost(
														perunSession, host);
										facilitiesResolvedFromEvent
												.add(facility);
										resourcesResolvedFromEvent
												.addAll(perun
														.getFacilitiesManager()
														.getAssignedResources(
																perunSession,
																facility));
									} catch (FacilityNotExistsException ex) {
										log.debug(
												"Host on non-existing facility found while resolving event. Host id={}",
												host.getId());
									} catch (HostNotExistsException ex) {
										log.debug(
												"Non-existing host found while resolving event. id={}",
												host.getId());
									}
								} else {
									log.warn(
											"No match found for this event. Event={}",
											event);
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

			List<Pair<List<ExecService>, Facility>> pairs = new ArrayList<Pair<List<ExecService>, Facility>>();
			for (Resource r : resourcesResolvedFromEvent) {

				Facility facilityResolvedFromEvent;
				List<Service> servicesResolvedFromResource;
				try {
					facilityResolvedFromEvent = perun.getResourcesManager()
							.getFacility(perunSession, r);
					servicesResolvedFromResource = perun.getResourcesManager()
							.getAssignedServices(perunSession, r);
					// process only services resolved from event if any
					if (!servicesResolvedFromEvent.isEmpty())
						servicesResolvedFromResource
								.retainAll(servicesResolvedFromEvent);
				} catch (ResourceNotExistsException ex) {
					log.debug(
							"Non-existing resource found while resolving event. Resource={}",
							r);
					continue; // skip to next resource
				}

				for (Service s : servicesResolvedFromResource) {
					// TODO: Optimize with an SQL query...
					List<ExecService> execServicesGenAndSend = generalServiceManager
							.listExecServices(perunSession, s.getId());
					List<ExecService> execServices = new ArrayList<ExecService>();
					for (ExecService execService : execServicesGenAndSend) {
						if (execService.getExecServiceType().equals(
								ExecServiceType.SEND)) {
							execServices.add(execService);
						}
					}

					if (attributeDefinition != null) { // remove from future
														// processing services
														// which don't require
														// the found attribute
						// TODO (CHECKME) This method can raise
						// ServiceNotExistsException. Is it ok? Or it must be
						// catch?
						List<AttributeDefinition> serviceRequiredAttributes = perun
								.getAttributesManager()
								.getRequiredAttributesDefinition(perunSession,
										s);
						if (!serviceRequiredAttributes
								.contains(attributeDefinition))
							continue;
					}

					pairs.add(new Pair<List<ExecService>, Facility>(
							execServices, facilityResolvedFromEvent));
				}
			}

			log.info("I am going to return " + pairs.size()
					+ " Pair<List<ExecService>, Facility>> pairs.");
			return pairs;

		} else {
			throw new InvalidEventMessageException("Message[" + event + "]");
		}
	}

	@Override
	public void refreshProcessingRules() throws ServiceNotExistsException,
			InternalErrorException, PrivilegeException {
		PerunSession perunSession = perun.getPerunSession(new PerunPrincipal(
				propertiesBean.getProperty("perun.principal.name"),
				propertiesBean.getProperty("perun.principal.extSourceName"),
				propertiesBean.getProperty("perun.principal.extSourceType")));
		rules.clear();
		rules.putAll(processingRuleDao.getRules(perunSession));
	}

	@Override
	public String toString() {
		return rules.toString();
	}

	public ProcessingRuleDao getProcessingRuleDao() {
		return processingRuleDao;
	}

	public void setProcessingRuleDao(ProcessingRuleDao processingRuleDao) {
		this.processingRuleDao = processingRuleDao;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

	public Perun getPerun() {
		return perun;
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public void setGeneralServiceManager(
			GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

}
