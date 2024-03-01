package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupSynchronizer extends AbstractSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(GroupSynchronizer.class);

  @Autowired
  protected PerunGroup perunGroup;

  public void synchronizeGroups() {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    boolean shouldWriteExceptionLog = true;
    try {

      LOG.debug("Group synchronization - getting list of VOs");
      List<Vo> vos = perun.getVosManagerBl().getVos(ldapcManager.getPerunSession());
      Set<Name> presentGroups = new HashSet<Name>();

      for (Vo vo : vos) {

        try {

          LOG.debug("Getting list of groups for VO {}", vo);

          List<Group> groups = perun.getGroupsManagerBl().getAllGroups(ldapcManager.getPerunSession(), vo);

          for (Group group : groups) {

            presentGroups.add(perunGroup.getEntryDN(String.valueOf(vo.getId()), String.valueOf(group.getId())));

            LOG.debug("Synchronizing group {}", group);

            LOG.debug("Getting list of attributes for group {}", group.getId());
            List<Attribute> attrs = new ArrayList<Attribute>();
            List<String> attrNames = fillPerunAttributeNames(perunGroup.getPerunAttributeNames());
            try {
              attrs.addAll(
                  perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), group, attrNames));
            } catch (PerunRuntimeException e) {
              LOG.warn("Couldn't get attributes {} for group {}: {}", attrNames, group.getId(), e.getMessage());
              shouldWriteExceptionLog = false;
              throw new InternalErrorException(e);
            }
            LOG.debug("Got attributes {}", attrNames.toString());

            try {

              LOG.debug("Getting list of members for group {}", group.getId());
              // List<Member> members = ldapcManager.getRpcCaller().call("groupsManager",  "getGroupMembers", params)
              // .readList(Member.class);
              List<Member> members =
                  perun.getGroupsManagerBl().getActiveGroupMembers(ldapcManager.getPerunSession(), group, Status.VALID);
              LOG.debug("Synchronizing {} members of group {}", members.size(), group.getId());
              //perunGroup.synchronizeMembers(group, members);

              LOG.debug("Getting list of resources assigned to group {}", group.getId());
              // List<Resource> resources = Rpc.ResourcesManager.getAssignedResources(ldapcManager.getRpcCaller(),
              // group);
              List<Resource> resources =
                  perun.getResourcesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), group);
              LOG.debug("Synchronizing {} resources assigned to group {}", resources.size(), group.getId());
              //perunGroup.synchronizeResources(group, resources);

              GroupsManagerBl groupsManager = perun.getGroupsManagerBl();
              List<Group> adminGroups =
                  groupsManager.getGroupsWhereGroupIsAdmin(ldapcManager.getPerunSession(), group);
              List<Vo> adminVos = groupsManager.getVosWhereGroupIsAdmin(ldapcManager.getPerunSession(), group);
              List<Facility> adminFacilities =
                  groupsManager.getFacilitiesWhereGroupIsAdmin(ldapcManager.getPerunSession(), group);

              LOG.debug("Synchronizing group {} as admin of {} groups, {} VOs and {} facilities", group.getId(),
                  adminGroups.size(), adminVos.size(), adminFacilities.size());

              perunGroup.synchronizeGroup(group, attrs, members, resources, adminGroups, adminVos, adminFacilities);

            } catch (PerunRuntimeException e) {
              LOG.error("Error synchronizing group", e);
              shouldWriteExceptionLog = false;
              throw new InternalErrorException(e);
            }
          }

        } catch (PerunRuntimeException e) {
          if (shouldWriteExceptionLog) {
            LOG.error("Error synchronizing groups", e);
          }
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
      }

      try {
        removeOldEntries(perunGroup, presentGroups, LOG);
      } catch (InternalErrorException e) {
        LOG.error("Error removing old group entries", e);
        shouldWriteExceptionLog = false;
        throw new InternalErrorException(e);
      }

    } catch (InternalErrorException e) {
      if (shouldWriteExceptionLog) {
        LOG.error("Error reading list of VOs", e);
      }
      throw new InternalErrorException(e);
    }

  }

}
