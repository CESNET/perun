package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import java.util.List;
import java.util.regex.Pattern;

public class FacilityAttributeProcessor extends AbstractAttributeProcessor {

	private final static Logger log = LoggerFactory.getLogger(FacilityAttributeProcessor.class);

	private static Pattern facilitySetPattern = Pattern.compile(" set for Facility:\\[(.*)\\]");
	private static Pattern facilityRemovePattern = Pattern.compile(" removed for Facility:\\[(.*)\\]");
	private static Pattern facilityAllAttrsRemovedPattern = Pattern.compile("All attributes removed for Facility:\\[(.*)\\]");
	private static Pattern facilityVirtualChangePattern = Pattern.compile(" changed for Facility:\\[(.*)\\]");

	public FacilityAttributeProcessor() {
		super(MessageBeans.FACILITY_F, facilitySetPattern, facilityRemovePattern, facilityAllAttrsRemovedPattern, facilityVirtualChangePattern);
	}

	public void processAttributeSet(String msg, MessageBeans beans) {
		PerunBl perun = (PerunBl) ldapcManager.getPerunBl();

		// ensure we have the correct beans available
		if (beans.getAttribute() == null || beans.getFacility() == null) {
			return;
		}
		try {
			log.debug("Setting attribute {} for facility {}", beans.getAttribute(), beans.getFacility());
			perunFacility.modifyEntry(beans.getFacility(), beans.getAttribute());
			log.debug("Getting resources assigned to facility {}", beans.getFacility().getId());
			// List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), beans.getFacility());
			List<Resource> resources = perun.getFacilitiesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), beans.getFacility());
			for (Resource resource : resources) {
				log.debug("Setting attribute {} for resource {}", beans.getAttribute(), resource);
				perunResource.modifyEntry(resource, beans.getAttribute());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error setting attribute:", e);
		}
	}

	public void processAttributeRemoved(String msg, MessageBeans beans) {
		PerunBl perun = (PerunBl) ldapcManager.getPerunBl();

		// ensure we have the correct beans available
		if (beans.getAttributeDef() == null || beans.getFacility() == null) {
			return;
		}
		try {
			log.debug("Removing attribute {} from facility {}", beans.getAttributeDef(), beans.getFacility());
			perunFacility.modifyEntry(beans.getFacility(), beans.getAttributeDef());
			log.debug("Getting resources assigned to facility {}", beans.getFacility().getId());
			// List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), beans.getFacility());
			List<Resource> resources = perun.getFacilitiesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), beans.getFacility());
			for (Resource resource : resources) {
				log.debug("Removing attribute {} from resource {}", beans.getAttributeDef(), resource);
				perunResource.modifyEntry(resource, beans.getAttributeDef());
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing attribute:", e);
		}
	}

	public void processAllAttributesRemoved(String msg, MessageBeans beans) {
		PerunBl perun = (PerunBl) ldapcManager.getPerunBl();

		// ensure we have the correct beans available
		if (beans.getFacility() == null) {
			return;
		}
		try {
			log.debug("Removing all attributes from facility {}", beans.getFacility());
			perunFacility.removeAllAttributes(beans.getFacility());
			log.debug("Getting resources assigned to facility {}", beans.getFacility().getId());
			// List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), beans.getFacility());
			List<Resource> resources = perun.getFacilitiesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), beans.getFacility());
			for (Resource resource : resources) {
				log.debug("Removing all attributes from resource {}", resource);
				perunResource.removeAllAttributes(resource);
			}
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing attributes:", e);
		}
	}

	public void processVirtualAttributeChanged(String msg, MessageBeans beans) {
		PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
		if (beans.getAttribute() == null || beans.getFacility() == null) {
			return;
		}
		try {
			log.debug("Getting resources assigned to facility {}", beans.getFacility().getId());
			List<Resource> resources = perun.getFacilitiesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), beans.getFacility());

			Attribute virtAttr = perun.getAttributesManagerBl().
				getAttribute(ldapcManager.getPerunSession(), beans.getFacility(), beans.getAttribute().getName());

			log.debug("Changing virtual attribute {} for facility {}", virtAttr, beans.getFacility());
			perunFacility.modifyEntry(beans.getFacility(), virtAttr);

			for (Resource resource : resources) {
				log.debug("Changing virtual attribute {} for resource {}", virtAttr, resource);
				perunResource.modifyEntry(resource, virtAttr);
			}
		} catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException |
			NamingException e) {
			log.error("Error changing virtual attribute:", e);
		}
	}
}