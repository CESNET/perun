package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

/**
 * EventProcessor handling the more complex events related to Group updates (excluding attributes) - such as the group
 * being set/unset as an admin group, member operations, assignments to resources, etc.
 */
public class GroupEventProcessor extends AbstractEventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(GroupEventProcessor.class);

  public void processAdminAdded(String msg, MessageBeans beans) {
    if (beans.getGroup() == null) {
      return;
    }
    PerunBean admined = null;
    try {
      if (beans.getVo() != null) {
        admined = beans.getVo();
        perunGroup.addAsVoAdmin(beans.getGroup(), beans.getVo());
      } else if (beans.getParentGroup() != null) {
        admined = beans.getParentGroup();
        perunGroup.addAsGroupAdmin(beans.getGroup(), beans.getParentGroup());
      } else if (beans.getFacility() != null) {
        admined = beans.getFacility();
        perunGroup.addAsFacilityAdmin(beans.getGroup(), beans.getFacility());
      }
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error adding group {} as admin of {}", beans.getGroup().getId(), admined.getId());
    }
  }

  public void processAdminRemoved(String msg, MessageBeans beans) {
    if (beans.getGroup() == null) {
      return;
    }
    PerunBean admined = null;
    try {
      if (beans.getVo() != null) {
        admined = beans.getVo();
        perunGroup.removeFromVoAdmins(beans.getGroup(), beans.getVo());
      } else if (beans.getParentGroup() != null) {
        admined = beans.getParentGroup();
        perunGroup.removeFromGroupAdmins(beans.getGroup(), beans.getParentGroup());
      } else if (beans.getFacility() != null) {
        admined = beans.getFacility();
        perunGroup.removeFromFacilityAdmins(beans.getGroup(), beans.getFacility());
      }
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing group {} from admins of {}", beans.getGroup().getId(), admined.getId());
    }

  }

  @Override
  public void processEvent(String msg, MessageBeans beans) {
  }

  /*public void processSubgroupAdded(String msg, MessageBeans beans) {
        if(beans.getGroup() == null || beans.getParentGroup() == null) {
            return;
        }
        try {
            log.debug("Adding subgroup {} to group {}", beans.getGroup(), beans.getParentGroup());
            perunGroup.addGroupAsSubGroup(beans.getGroup(), beans.getParentGroup());
        } catch (NamingException | InternalErrorException e) {
            log.error("Error adding subgroup {} to group {}: {}", beans.getGroup().getId(), beans.getParentGroup()
            .getId(), e.getMessage());
        }
    }*/

  public void processGroupMoved(String msg, MessageBeans beans) {
    if (beans.getGroup() == null) {
      return;
    }
    try {
      // TODO move to PerunGroupImpl?
      LOG.debug("Moving group {}", beans.getGroup());
      perunGroup.modifyEntry(beans.getGroup(), PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME,
          PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_UNIQUE_GROUP_NAME,
          PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_PARENT_GROUP,
          PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_PARENT_GROUP_ID);
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error moving group {}: {}", beans.getGroup().getId(), e.getMessage());
    }

  }

  public void processMemberAdded(String msg, MessageBeans beans) {
    if (beans.getGroup() == null || beans.getMember() == null) {
      return;
    }
    try {
      LOG.debug("Adding member {} to group {}", beans.getMember(), beans.getGroup());
      perunGroup.addMemberToGroup(beans.getMember(), beans.getGroup());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error adding member {} to group {}: {}", beans.getMember().getId(), beans.getGroup().getId(),
          e.getMessage());
    }
  }

  public void processMemberInvalidated(String msg, MessageBeans beans) {
    if (beans.getMember() == null) {
      return;
    }
    List<Group> memberGroups = new ArrayList<Group>();
    Perun perun = ldapcManager.getPerunBl();
    try {
      LOG.debug("Getting list of groups for member {}", beans.getMember().getId());
      // memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), beans.getMember());
      memberGroups = perun.getGroupsManager().getAllMemberGroups(ldapcManager.getPerunSession(), beans.getMember());
      for (Group g : memberGroups) {
        LOG.debug("Removing invalidated member {} from group {}", beans.getMember(), g);
        perunGroup.removeMemberFromGroup(beans.getMember(), g);
      }
    } catch (MemberNotExistsException e) {
      //IMPORTANT this is not problem, if member not exist, we expected that will be deleted in some message after
      // that, in DB is deleted
    } catch (PrivilegeException e) {
      LOG.warn("There are no privilegies for getting member's groups", e);
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing validated member from group", e);
    }
  }

  public void processMemberRemoved(String msg, MessageBeans beans) {
    if (beans.getGroup() == null || beans.getMember() == null) {
      return;
    }
    try {
      LOG.debug("Removing member {} from group {}", beans.getMember(), beans.getGroup());
      perunGroup.removeMemberFromGroup(beans.getMember(), beans.getGroup());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing member {} from group {}: {}", beans.getMember().getId(), beans.getGroup().getId(),
          e.getMessage());
    }
  }

  public void processMemberValidated(String msg, MessageBeans beans) {
    if (beans.getMember() == null) {
      return;
    }
    List<Group> memberGroups = new ArrayList<Group>();
    Perun perun = ldapcManager.getPerunBl();
    try {
      LOG.debug("Getting list of groups for member {}", beans.getMember().getId());
      // memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), beans.getMember());
      memberGroups =
          perun.getGroupsManager().getAllGroupsWhereMemberIsActive(ldapcManager.getPerunSession(), beans.getMember());
      for (Group g : memberGroups) {
        LOG.debug("Adding validated member {} to group {}", beans.getMember(), g);
        perunGroup.addMemberToGroup(beans.getMember(), g);
      }
    } catch (MemberNotExistsException e) {
      //IMPORTANT this is not problem, if member not exist, we expected that will be deleted in some message after
      // that, in DB is deleted
    } catch (PrivilegeException e) {
      LOG.warn("There are no privileges for getting member's groups", e);
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error adding validated member to group", e);
    }
  }

  public void processResourceAssigned(String msg, MessageBeans beans) {
    if (beans.getGroup() == null || beans.getResource() == null) {
      return;
    }
    try {
      LOG.debug("Adding resource {} to group {}", beans.getResource(), beans.getGroup());
      perunResource.assignGroup(beans.getResource(), beans.getGroup());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error adding resource {} to group {}: {}", beans.getResource().getId(), beans.getGroup().getId(),
          e.getMessage());
    }
  }

  public void processResourceRemoved(String msg, MessageBeans beans) {
    if (beans.getGroup() == null || beans.getResource() == null) {
      return;
    }
    try {
      LOG.debug("Removing resource {} from group {}", beans.getResource(), beans.getGroup());
      perunResource.removeGroup(beans.getResource(), beans.getGroup());
    } catch (NamingException | InternalErrorException e) {
      LOG.error("Error removing resource {} from group {}: {}", beans.getResource().getId(), beans.getGroup().getId(),
          e.getMessage());
    }
  }

}
