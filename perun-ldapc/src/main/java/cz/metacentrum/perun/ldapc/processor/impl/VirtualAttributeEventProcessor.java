package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.processor.VirtualAttributeManager;

public class VirtualAttributeEventProcessor extends AbstractEventProcessor implements EventProcessor {

	private final static Logger log = LoggerFactory.getLogger(VirtualAttributeEventProcessor.class);

	@Autowired
	protected VirtualAttributeManager<User> dependencyManager; 
	
	@Override
	public void processEvent(String msg, MessageBeans beans) {
	}

	public void processGroupMemberChange(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getMember() == null || beans.getGroup() == null) {
			return;
		}
		try {
			PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
			User user = perun.getUsersManagerBl().getUserByMember(ldapcManager.getPerunSession(), beans.getMember());
			List<String> registeredAttrs = dependencyManager.getRegisteredAttributes();
			List<Attribute> groupAttrs = perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), beans.getGroup(), registeredAttrs);
			log.debug("Processing member {} status change in group {} with attributes {}", user, beans.getGroup(), groupAttrs);
			if(!groupAttrs.isEmpty()) {
				List<PerunVirtualAttribute<User>> attrDefs = new ArrayList<>();
				groupAttrs.forEach(attr -> attrDefs.addAll(dependencyManager.getAttributeDependants(attr.getName())));
				log.debug("Found attributes: {}", attrDefs);
				Collection<Pair<PerunAttribute<User>, AttributeDefinition>> userAttrs = this.getAttributeValues(perun, user, attrDefs);
				log.debug("Modifyng user {} entry with the new values {}", user.getId(), userAttrs);
				perunUser.modifyEntry(user, userAttrs);
			}
		} catch (InternalErrorException e) {
			log.error("Error processing member {} change in group {}: {}", beans.getMember(), beans.getGroup(), e.getMessage());
		}
	}

	public void processMemberChange(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getMember() == null) {
			return;
		}
		try {
			PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
			User user = perun.getUsersManagerBl().getUserByMember(ldapcManager.getPerunSession(), beans.getMember());
			Collection<PerunVirtualAttribute<User>> attrDefs = dependencyManager.getAllAttributeDependants();
			log.debug("Processing member {} status change with attributes {}", user, attrDefs);
			Collection<Pair<PerunAttribute<User>, AttributeDefinition>> userAttrs = this.getAttributeValues(perun, user, attrDefs);
			log.debug("Modifyng user {} entry with the new values {}", user.getId(), userAttrs);
			perunUser.modifyEntry(user, userAttrs);
		} catch (InternalErrorException e) {
			log.error("Error processing member {} status change: {}", beans.getMember(), e.getMessage());
		}
	}
	
	public void processGroupAttributeChange(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getAttribute() == null || beans.getGroup() == null) {
			return;
		}
		try {
			PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
			/* get list of dependent attributes */
			Collection<PerunVirtualAttribute<User>> attrDefs = dependencyManager.getAttributeDependants(beans.getAttribute().getName());
			log.debug("Processing group {} attribute {} change, affected attributes {}", beans.getGroup(), beans.getAttribute(), attrDefs);
			if(attrDefs != null) {
				/* for each concerned user */
				for(User user : perun.getGroupsManagerBl().getGroupUsers(
						ldapcManager.getPerunSession(), 
						beans.getGroup())) {
					/* get attribute value for each dependent attribute */
					Collection<Pair<PerunAttribute<User>, AttributeDefinition>> userAttrs = this.getAttributeValues(perun, user, attrDefs); 
					log.debug("Modifyng user {} entry with the new values {}", user.getId(), userAttrs);
					perunUser.modifyEntry(user, userAttrs);
				}
			}
		} catch (InternalErrorException e) {
			log.error("Error processing group {} attribute {} change: {}", beans.getGroup(), beans.getAttribute(), e.getMessage());
		}
	}

	public void processGroupAttributeRemoval(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getGroup() == null || beans.getAttributeDef() == null) {
			return;
		}
		try {
			PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
			Collection<PerunVirtualAttribute<User>> attrDefs = dependencyManager.getAttributeDependants(beans.getAttributeDef().getName());
			log.debug("Processing group {} attribute {} removal, affected attributes {}", beans.getGroup(), beans.getAttributeDef(), attrDefs);
			if(attrDefs != null) {
				for(User user : perun.getGroupsManagerBl().getGroupUsers(
						ldapcManager.getPerunSession(), 
						beans.getGroup())) {
					/* get attribute value for each dependent attribute */
					Collection<Pair<PerunAttribute<User>, AttributeDefinition>> userAttrs = this.getAttributeValues(perun, user, attrDefs);
					log.debug("Modifyng user {} entry with the new values {}", user.getId(), userAttrs);
					perunUser.modifyEntry(user, userAttrs);
				}
			}
		} catch (InternalErrorException e) {
			log.error("Error processing group {} attribute {} removal: {}", beans.getGroup(), beans.getAttributeDef(), e.getMessage());
		}
	}

	public void processAllGroupAttributesRemoval(String msg, MessageBeans beans) {
		// ensure we have the correct beans available
		if (beans.getGroup() == null) {
			return;
		}
		try {
			PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
			Collection<PerunVirtualAttribute<User>> attrDefs = dependencyManager.getAllAttributeDependants();
			log.debug("Processing group {} all attribute removal, affected attributes {}", beans.getGroup(),  attrDefs);
			if(attrDefs != null) {
				for(User user : perun.getGroupsManagerBl().getGroupUsers(
						ldapcManager.getPerunSession(), 
						beans.getGroup())) {
					/* get attribute value for each dependent attribute */
					Collection<Pair<PerunAttribute<User>, AttributeDefinition>> userAttrs = this.getAttributeValues(perun, user, attrDefs);
					log.debug("Modifyng user {} entry with the new values {}", user.getId(), userAttrs);
					perunUser.modifyEntry(user, userAttrs);
				}
			}
		} catch (InternalErrorException e) {
			log.error("Error processing group {} attributes removal: {}", beans.getGroup(), e.getMessage());
		}
	}

	protected List<Pair<PerunAttribute<User>, AttributeDefinition>> getAttributeValues(
			PerunBl perun, 
			User user, 
			Collection<PerunVirtualAttribute<User>> attrDefs) 
	{
		return attrDefs.stream()
				.map(attrDef -> {
					try {
						return new Pair<PerunAttribute<User>, AttributeDefinition>(
								attrDef,
								perun.getAttributesManagerBl().getAttribute(
										ldapcManager.getPerunSession(),
										user,
										attrDef.getPerunAttributeName()));
					} catch(AttributeNotExistsException | WrongAttributeAssignmentException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
}
