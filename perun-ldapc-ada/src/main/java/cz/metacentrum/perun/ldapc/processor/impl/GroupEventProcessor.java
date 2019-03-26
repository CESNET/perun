package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import cz.metacentrum.perun.rpclib.Rpc;

public class GroupEventProcessor extends AbstractEventProcessor {

	private final static Logger log = LoggerFactory.getLogger(GroupEventProcessor.class);

	@Override
	public void processEvent(String msg, MessageBeans beans) {
	}

	public void processMemberAdded(String msg, MessageBeans beans) {
		if(beans.getGroup() == null || beans.getMember() == null) {
			return;
		}
		try {
			log.debug("Adding member {} to group {}", beans.getMember(), beans.getGroup());
			perunGroup.addMemberToGroup(beans.getMember(), beans.getGroup());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding member {} to group {}: {}", beans.getMember().getId(), beans.getGroup().getId(), e.getMessage());;
		}
	}

	public void processMemberRemoved(String msg, MessageBeans beans) {
		if(beans.getGroup() == null || beans.getMember() == null) {
			return;
		}
		try {
			log.debug("Removing member {} from group {}", beans.getMember(), beans.getGroup());
			perunGroup.removeMemberFromGroup(beans.getMember(), beans.getGroup());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing member {} from group {}: {}", beans.getMember().getId(), beans.getGroup().getId(), e.getMessage());;
		}
	}

	public void processSubgroupAdded(String msg, MessageBeans beans) {
		if(beans.getGroup() == null || beans.getParentGroup() == null) {
			return;
		}
		try {
			log.debug("Adding subgroup {} to group {}", beans.getGroup(), beans.getParentGroup());
			perunGroup.addGroupAsSubGroup(beans.getGroup(), beans.getParentGroup());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding subgroup {} to group {}: {}", beans.getGroup().getId(), beans.getParentGroup().getId(), e.getMessage());
		}
	}

	public void processResourceAssigned(String msg, MessageBeans beans) {
		if(beans.getGroup() == null || beans.getResource() == null) {
			return;
		}
		try {
			log.debug("Adding resource {} to group {}", beans.getResource(), beans.getGroup());
			perunResource.assignGroup(beans.getResource(), beans.getGroup());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding resource {} to group {}: {}", beans.getResource().getId(), beans.getGroup().getId(), e.getMessage());
		}
	}

	public void processResourceRemoved(String msg, MessageBeans beans) {
		if(beans.getGroup() == null || beans.getResource() == null) {
			return;
		}
		try {
			log.debug("Removing resource {} from group {}", beans.getResource(), beans.getGroup());
			perunResource.removeGroup(beans.getResource(), beans.getGroup());
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing resource {} from group {}: {}", beans.getResource().getId(), beans.getGroup().getId(), e.getMessage());
		}
	}

	public void processGroupMoved(String msg, MessageBeans beans) {
		if(beans.getGroup() == null) {
			return;
		}
		try {
			// TODO move to PerunGroupImpl?
			log.debug("Moving group {}", beans.getGroup());
			perunGroup.modifyEntry(beans.getGroup(), 
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
						PerunAttribute.PerunAttributeNames.ldapAttrPerunUniqueGroupName,
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroup,
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroupId);
		} catch (NamingException | InternalErrorException e) {
			log.error("Error moving group {}: {}", beans.getGroup().getId(), e.getMessage());
		}
		
	}

	public void processMemberValidated(String msg, MessageBeans beans) {
		if(beans.getMember() == null) {
			return;
		}
		List<Group> memberGroups = new ArrayList<Group>();
		Perun perun = ldapcManager.getPerunBl();
		try {
			log.debug("Getting list of groups for member {}", beans.getMember().getId());
			// memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), beans.getMember());
			memberGroups = perun.getGroupsManager().getAllGroupsWhereMemberIsActive(ldapcManager.getPerunSession(), beans.getMember());
			for(Group g: memberGroups) {
				log.debug("Adding validated member {} to group {}", beans.getMember(), g);
				perunGroup.addMemberToGroup(beans.getMember(), g);
			}
		} catch (MemberNotExistsException e) {
			//IMPORTANT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
		} catch (PrivilegeException e) {
			log.warn("There are no privileges for getting member's groups", e);
		} catch (NamingException | InternalErrorException e) {
			log.error("Error adding validated member to group", e);
		}
	}

	public void processMemberInvalidated(String msg, MessageBeans beans) {
		if(beans.getMember() == null) {
			return;
		}
		List<Group> memberGroups = new ArrayList<Group>();
		Perun perun = ldapcManager.getPerunBl();
		try {
			log.debug("Getting list of groups for member {}", beans.getMember().getId());
			// memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), beans.getMember());
			memberGroups = perun.getGroupsManager().getAllMemberGroups(ldapcManager.getPerunSession(), beans.getMember());
			for(Group g: memberGroups) {
				log.debug("Removing invalidated member {} from group {}", beans.getMember(), g);
				perunGroup.removeMemberFromGroup(beans.getMember(), g);
			}
		} catch (MemberNotExistsException e) {
			//IMPORTANT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
		} catch (PrivilegeException e) {
			log.warn("There are no privilegies for getting member's groups", e);
		} catch (NamingException | InternalErrorException e) {
			log.error("Error removing validated member from group", e);
		}
	}

}
