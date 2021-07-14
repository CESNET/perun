package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.GroupResourceAssignment;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * Component responsible for activating group-resource assignments in PROCESSING or
 * FAILED state after Perun startup.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ResourceAssignmentActivator implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(ResourceAssignmentActivator.class);

	private final PerunSession sess;
	private PerunBl perunBl;

	public ResourceAssignmentActivator(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.sess = perunBl.getPerunSession(
			new PerunPrincipal("perunResourceAssignmentActivator", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
			new PerunClient());
	}

	public PerunBl getPerunBl() {
		return perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	/**
	 * Tries to activate all group-resource assignments in PROCESSING or FAILED state
	 * after Spring context is refreshed or initialized, e.g. after Perun startup.
	 *
	 * This method runs asynchronously so it doesn't block other Spring events.
	 */
	@Override
	@Async
	public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
		activateGroupResourceAssignments();
	}

	/**
	 * Tries to activate all group-resource assignments in PROCESSING or FAILED state.
	 * The activations run synchronously in one thread.
	 */
	private void activateGroupResourceAssignments() {
		try {
			log.debug("ResourceAssignmentActivator starting to activate group-resource assignments in PROCESSING or FAILED state.");

			List<GroupResourceAssignment> assignments = perunBl.getResourcesManagerBl()
				.getGroupResourceAssignments(sess, List.of(GroupResourceStatus.PROCESSING, GroupResourceStatus.FAILED));

			for (GroupResourceAssignment assignment : assignments) {
				try {
					perunBl.getResourcesManagerBl()
						.activateGroupResourceAssignment(sess, assignment.getGroup(), assignment.getResource(), false);
				} catch (Exception e) {
					log.error("Cannot activate group-resource assignment: " + assignment, e);
				}
			}
		} catch (Exception e) {
			log.error("Error during activating group-resource assignments: ", e);
		}
	}
}
