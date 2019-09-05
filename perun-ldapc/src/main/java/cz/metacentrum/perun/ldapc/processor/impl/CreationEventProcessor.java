package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.metacentrum.perun.ldapc.model.PerunAttribute.PerunAttributeNames.perunAttrClientID;
import static cz.metacentrum.perun.ldapc.model.PerunAttribute.PerunAttributeNames.perunAttrEntityID;

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
					perunResource.addResource(beans.getResource(), getFacilityAttributes(beans.getResource().getFacilityId()));
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
	 * Get facility attributes as map of LDAP attr names to their values. Key in a map is omitted if value is empty
	 *
	 * @param facilityId the facilityId
	 * @return value of facility attributes stored in resource or null, if value is null or facility not exists yet
	 * @throws InternalErrorException if some exception is thrown from RPC
	 */
	private Map<String,String> getFacilityAttributes(int facilityId) throws InternalErrorException {

		Map<String,String> result = new HashMap<>();
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		PerunSession perunSession = ldapcManager.getPerunSession();
		Facility facility = null;
		try {
			facility = perun.getFacilitiesManagerBl().getFacilityById(perunSession, facilityId);
		} catch (FacilityNotExistsException ex) {
			//If facility not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
			return result;
		}

		// FIXME - at least for those two attributes perun and ldap attr name is the same
		// we might include static mapping if new attrs are added

		List<Attribute> attributes = perun.getAttributesManagerBl().getAttributes(perunSession, facility, Arrays.asList(
				AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrEntityID,
				AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrClientID));

		for (Attribute a : attributes) {
			if (a.getValue() != null) {
				result.put(a.getFriendlyName(), a.valueAsString());
			}
		}
		return result;

	}


}
