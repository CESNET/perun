package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

import java.util.ArrayList;
import java.util.List;

public class CreationEventProcessor extends AbstractEventProcessor {

	private final static Logger log = LoggerFactory.getLogger(CreationEventProcessor.class);

	@Override
	public void processEvent(String msg, MessageBeans beans) {
		for(int beanFlag: beans.getPresentBeansFlags()) {
			try {
				switch(beanFlag) {
				case MessageBeans.GROUP_F:
					if (beans.getParentGroup() != null) {
						log.debug("Adding subgroup {} to group {}", beans.getGroup(), beans.getParentGroup());
						perunGroup.addGroupAsSubGroup(beans.getGroup(), beans.getParentGroup());
						break;
					}
					log.debug("Adding new group: {}", beans.getGroup());
					perunGroup.addGroup(beans.getGroup());
					break;

				case MessageBeans.RESOURCE_F:
					log.debug("Adding new resource: {}", beans.getResource());
					perunResource.addResource(beans.getResource());
					// push also facility attributes with it using sync
					PerunEntry.SyncOperation op = perunResource.beginSynchronizeEntry(beans.getResource(), getFacilityAttributes(beans.getResource()));
					perunResource.commitSyncOperation(op);
					break;

				case MessageBeans.FACILITY_F:
					log.debug("Adding new facility: {}", beans.getFacility());
					perunFacility.addFacility(beans.getFacility());
					break;

				case MessageBeans.USER_F:
					log.debug("Adding new user: {}", beans.getUser());
					perunUser.addUser(beans.getUser());
					break;

				case MessageBeans.VO_F:
					if (beans.getGroup() != null) {
						break;
					}
					log.debug("Adding new VO: {}", beans.getVo());
					perunVO.addVo(beans.getVo());
					break;

				default:
					break;
				}
			} catch(NamingException | InternalErrorException e) {
				log.error("Error creating new entry: {}", e.getMessage());
			}
		}

	}

	/**
	 * Get facility attributes requested by the resource
	 *
	 * @param resource to get facility from
	 * @return List of facility attributes requested by resource
	 * @throws InternalErrorException if some exception is thrown from RPC
	 */
	private List<Attribute> getFacilityAttributes(Resource resource) throws InternalErrorException {

		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		PerunSession perunSession = ldapcManager.getPerunSession();
		Facility facility = null;
		try {
			facility = perun.getFacilitiesManagerBl().getFacilityById(perunSession, resource.getFacilityId());
		} catch (FacilityNotExistsException ex) {
			//If facility not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
			return new ArrayList<>();
		}
		// get only facility attributes
		List<String> filteredNames = perunResource.getPerunAttributeNames();
		filteredNames.removeIf(attrName-> !attrName.startsWith(AttributesManager.NS_FACILITY_ATTR));
		return perun.getAttributesManagerBl().getAttributes(perunSession, facility, filteredNames);

	}


}
