package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.GroupResourceAssignment;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.ResourceAssignmentActivatorApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;

/**
 * Component responsible for activating group-resource assignments in PROCESSING or FAILED state after Perun startup.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ResourceAssignmentActivator
    implements ResourceAssignmentActivatorApi, ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceAssignmentActivator.class);

  private final PerunSession sess;
  private PerunBl perunBl;

  public ResourceAssignmentActivator(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.sess = perunBl.getPerunSession(
        new PerunPrincipal("perunResourceAssignmentActivator", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
  }

  /**
   * Tries to activate all group-resource assignments in PROCESSING or FAILED state. The activations run synchronously
   * in one thread.
   */
  private void activateGroupResourceAssignments() {
    if (perunBl.isPerunReadOnly()) {
      LOG.warn("This instance is just read only so skip activation of group-resource assignments.");
      return;
    }

    try {
      LOG.debug(
          "ResourceAssignmentActivator starting to activate group-resource assignments in PROCESSING or FAILED state.");

      List<GroupResourceAssignment> assignments = perunBl.getResourcesManagerBl()
          .getGroupResourceAssignments(sess, List.of(GroupResourceStatus.PROCESSING, GroupResourceStatus.FAILED));

      for (GroupResourceAssignment assignment : assignments) {
        perunBl.getResourceAssignmentActivator().tryActivateAssignment(assignment);
      }
    } catch (Exception e) {
      LOG.error("Error during activating group-resource assignments: ", e);
    }
  }

  public PerunBl getPerunBl() {
    return perunBl;
  }

  /**
   * Tries to activate all group-resource assignments in PROCESSING or FAILED state after Spring context is refreshed or
   * initialized, e.g. after Perun startup.
   * <p>
   * This method runs asynchronously so it doesn't block other Spring events.
   */
  @Override
  @Async
  public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
    activateGroupResourceAssignments();
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  /**
   * Tries to activate assignment in transaction.
   *
   * @param assignment
   */
  @Override
  public void tryActivateAssignment(GroupResourceAssignment assignment) {
    try {
      perunBl.getResourcesManagerBl()
          .activateGroupResourceAssignment(sess, assignment.getGroup(), assignment.getResource(), false);
    } catch (Exception e) {
      LOG.error("Cannot activate group-resource assignment: " + assignment, e);
    }
  }
}
