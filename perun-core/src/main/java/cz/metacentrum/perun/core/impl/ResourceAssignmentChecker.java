package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AssignedGroup;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This component is periodically called to search for and fix inconsistencies in automatic group-resource assignments.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class ResourceAssignmentChecker {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceAssignmentChecker.class);

  private final PerunSession sess;
  private PerunBl perunBl;

  public ResourceAssignmentChecker(PerunBl perunBl) {
    this.perunBl = perunBl;
    String synchronizerPrincipal = "perunResourceAssignmentChecker";
    this.sess = perunBl.getPerunSession(
        new PerunPrincipal(synchronizerPrincipal, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
  }

  /**
   * Filter subgroups of source group (with autoassign) which are not assigned and assign them. Runs in transaction.
   *
   * @param resource
   * @param automaticallyAssignedSubgroups
   * @param sourceGroup
   */
  public void assignSubgroupsToResource(Resource resource, List<AssignedGroup> automaticallyAssignedSubgroups,
                                        AssignedGroup sourceGroup) {
    List<Group> sourceGroupSubgroups =
        perunBl.getGroupsManagerBl().getAllSubGroups(sess, sourceGroup.getEnrichedGroup().getGroup());
    sourceGroupSubgroups = sourceGroupSubgroups.stream().filter(
        sourceSubgroup -> automaticallyAssignedSubgroups.stream().noneMatch(assignedSubgroup ->
            assignedSubgroup.getSourceGroupId() == sourceGroup.getEnrichedGroup().getGroup().getId() &&
            assignedSubgroup.getEnrichedGroup().getGroup().equals(sourceSubgroup))).collect(Collectors.toList());

    for (Group subgroup : sourceGroupSubgroups) {
      try {
        perunBl.getResourcesManagerBl()
            .assignAutomaticGroupToResource(sess, sourceGroup.getEnrichedGroup().getGroup(), subgroup, resource);
      } catch (GroupResourceMismatchException e) {
        LOG.error("Cannot activate group (id = " + subgroup.getId() + ") assignment on resource " + resource, e);
      } catch (GroupAlreadyAssignedException | WrongReferenceAttributeValueException | WrongAttributeValueException e) {
        // silently skip
      }
    }
  }

  /**
   * Waits for 10 minutes after Perun startup and then every hour checks, if all group-resource assignments are
   * consistent, e.g. all subgroups are automatically assigned and no automatic subgroups assignments are kept after
   * removing source group.
   */
  @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
  public void fixInconsistentGroupResourceAssignments() {
    if (perunBl.isPerunReadOnly()) {
      LOG.warn("This instance is just read only so skip periodic check of automatic group-resource assignments.");
      return;
    }

    LOG.debug("ResourceAssignmentChecker starting fixing inconsistencies in automatic group-resource assignments.");

    List<Resource> resources = perunBl.getResourcesManagerBl().getResources(sess);

    for (Resource resource : resources) {
      List<AssignedGroup> assignedGroups =
          perunBl.getResourcesManagerBl().getGroupAssignments(sess, resource, List.of());

      List<AssignedGroup> automaticallyAssignedSubgroups =
          assignedGroups.stream().filter(group -> group.getSourceGroupId() != null).collect(Collectors.toList());

      List<AssignedGroup> sourceGroups =
          assignedGroups.stream().filter(a -> a.isAutoAssignSubgroups() && a.getSourceGroupId() == null)
              .collect(Collectors.toList());

      for (AssignedGroup assignedSubgroup : automaticallyAssignedSubgroups) {
        perunBl.getResourceAssignmentChecker().removeSubgroupFromResource(resource, sourceGroups, assignedSubgroup);
      }

      for (AssignedGroup sourceGroup : sourceGroups) {
        perunBl.getResourceAssignmentChecker()
            .assignSubgroupsToResource(resource, automaticallyAssignedSubgroups, sourceGroup);
      }
    }

  }

  public PerunBl getPerunBl() {
    return perunBl;
  }

  /**
   * Remove assigned subgroup which source group is not assigned as source group. Runs in transaction.
   *
   * @param resource
   * @param sourceGroups
   * @param assignedSubgroup
   */
  public void removeSubgroupFromResource(Resource resource, List<AssignedGroup> sourceGroups,
                                         AssignedGroup assignedSubgroup) {
    boolean sourceIsAssigned;

    try {
      Group srcGroup = perunBl.getGroupsManagerBl().getGroupById(sess, assignedSubgroup.getSourceGroupId());
      sourceIsAssigned = sourceGroups.stream().anyMatch(s -> s.getEnrichedGroup().getGroup().equals(srcGroup));
    } catch (GroupNotExistsException e) {
      sourceIsAssigned = false;
    }

    if (!sourceIsAssigned) {
      try {
        perunBl.getResourcesManagerBl()
            .removeAutomaticGroupFromResource(sess, assignedSubgroup.getEnrichedGroup().getGroup(), resource,
                assignedSubgroup.getSourceGroupId());
      } catch (GroupNotDefinedOnResourceException | GroupAlreadyRemovedFromResourceException e) {
        // skip silently, already removed
      }
    }
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

}
