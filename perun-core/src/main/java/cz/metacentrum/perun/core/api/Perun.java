package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Auditer;

/**
 * Perun himself.
 * <p>
 * See {@link cz.metacentrum.perun.core.bl.PerunBl#bootstrap()} to find how to get
 * an instance of Perun.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface Perun {

  /**
   * True if this instance of perun is read only.
   * False if not.
   *
   * @return true or false (readOnly or not)
   */
  boolean isPerunReadOnly();

  /**
   * Gets a (possibly cached) Perun session.
   *
   * @param actor  identification of the actor, who will perform operations.
   * @param client identification of the client, who will perform operations.
   * @return perun session
   * @throws InternalErrorException raised when session cannot be created.
   */
  PerunSession getPerunSession(PerunPrincipal actor, PerunClient client);

  /**
   * Gets a groups manager.
   *
   * @return groups manager
   */
  GroupsManager getGroupsManager();

  /**
   * Gets a resource manager.
   *
   * @return resource manager
   */
  FacilitiesManager getFacilitiesManager();

  /**
   * Gets a database manager.
   *
   * @return database manager
   */
  DatabaseManager getDatabaseManager();

  /**
   * Gets a users manager.
   *
   * @return users manager
   */
  UsersManager getUsersManager();

  /**
   * Gets a members manager.
   *
   * @return members manager
   */
  MembersManager getMembersManager();

  /**
   * Gets a VOs manager.
   *
   * @return VOs manager
   */
  VosManager getVosManager();

  /**
   * Gets a AuditMessages manager.
   *
   * @return AuditMessages manager
   */
  AuditMessagesManager getAuditMessagesManager();

  /**
   * Gets a Resources manager.
   *
   * @return Resources manager
   */
  ResourcesManager getResourcesManager();

  /**
   * Gets a ExtSources manager.
   *
   * @return ExtSources manager
   */
  ExtSourcesManager getExtSourcesManager();

  /**
   * Gets a Attributes manager.
   *
   * @return Attributes manager
   */
  AttributesManager getAttributesManager();

  /**
   * Gets a Services manager.
   *
   * @return Services manager
   */
  ServicesManager getServicesManager();

  /**
   * Gets a Owners manager.
   *
   * @return Owners manager
   */
  OwnersManager getOwnersManager();

  /**
   * Gets the Auditer.
   *
   * @return Auditer
   */
  Auditer getAuditer();

  /**
   * Gets a Messages manager.
   *
   * @return Messages manager
   */
  RTMessagesManager getRTMessagesManager();

  /**
   * Gets a Security teams manager.
   *
   * @return Security teams manager
   */
  SecurityTeamsManager getSecurityTeamsManager();

  /**
   * Gets a Searcher.
   *
   * @return Searcher
   */
  Searcher getSearcher();

  /**
   * Gets a TasksManager
   *
   * @return TasksManager
   */
  TasksManager getTasksManager();

  /**
   * Gets a ConfigManager
   *
   * @return ConfigManager
   */
  ConfigManager getConfigManager();

  /**
   * Gets a ConsentsManager
   *
   * @return ConsentsManager
   */
  ConsentsManager getConsentsManager();
}
