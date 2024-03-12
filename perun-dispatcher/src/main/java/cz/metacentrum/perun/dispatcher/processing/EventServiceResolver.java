package cz.metacentrum.perun.dispatcher.processing;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import java.util.Map;
import java.util.Set;

/**
 * Allow resolving of Facility-Service pairs from Events. It's used by EventProcessor for creating Tasks from Events.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.model.Event
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 */
public interface EventServiceResolver {

  /**
   * Resolve Facilities and set of Services affected by AuditEvent.
   *
   * @param event Event to be resolved
   * @return Affected Facilities and Services on them.
   * @throws InvalidEventMessageException When Event has wrong format.
   * @throws ServiceNotExistsException    When Service no longer exists.
   * @throws InternalErrorException       When implementation fails.
   * @throws PrivilegeException           When dispatcher lack privileges to call core methods.
   */
  Map<Facility, Set<Service>> resolveEvent(AuditEvent event)
      throws InvalidEventMessageException, ServiceNotExistsException, PrivilegeException;

}
