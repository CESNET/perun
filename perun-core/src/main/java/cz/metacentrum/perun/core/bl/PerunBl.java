package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessagesManager;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.SecurityTeamsManager;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.TasksManager;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Perun himself.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public interface PerunBl extends Perun {

	String INTERNALPRINCIPAL = "INTERNAL";

	/**
	 * Gets a (possibly cached) Perun session.
	 * @throws InternalErrorException raised when session cannot be created.
	 * @param actor identification of the actor, who will perform operations.
	 * @return perun session
	 */
	PerunSession getPerunSession(PerunPrincipal actor, PerunClient client);

	/**
	 * Gets a groups manager.
	 * @return groups manager
	 */
	GroupsManager getGroupsManager();

	/**
	 * Gets a resource manager.
	 * @return resource manager
	 */
	FacilitiesManager getFacilitiesManager();

	/**
	 * Gets a database manager.
	 * @return database manager
	 */
	DatabaseManager getDatabaseManager();
	
	/**
	 * Gets a users manager.
	 * @return users manager
	 */
	UsersManager getUsersManager();

	/**
	 * Gets a members manager.
	 * @return members manager
	 */
	MembersManager getMembersManager();

	/**
	 * Gets a VOs manager.
	 * @return VOs manager
	 */
	VosManager getVosManager();

	/**
	 * Gets a Resources manager.
	 * @return Resources manager
	 */
	ResourcesManager getResourcesManager();

	/**
	 * Gets a ExtSources manager.
	 * @return ExtSources manager
	 */
	ExtSourcesManager getExtSourcesManager();

	/**
	 * Gets a Attributes manager.
	 * @return Attributes manager
	 */
	AttributesManager getAttributesManager();

	/**
	 * Gets a Services manager.
	 * @return Services manager
	 */
	ServicesManager getServicesManager();

	/**
	 * Gets a Owners manager.
	 * @return Owners manager
	 */
	OwnersManager getOwnersManager();

	/**
	 * Gets a AuditMessages manager.
	 * @return AuditMessages manager
	 */
	AuditMessagesManager getAuditMessagesManager();

	/**
	 * Gets a Messages manager.
	 * @return Messages manager
	 */
	RTMessagesManager getRTMessagesManager();

	/**
	 * Gets a Security Teams manager.
	 * @return Security Teams manager
	 */
	SecurityTeamsManager getSecurityTeamsManager();

	/**
	 * Gets a Searcher.
	 * @return Searcher
	 */
	Searcher getSearcher();

	/**
	 * Gets a TasksManager
	 * @return TasksManager
	 */
	TasksManager getTasksManager();

	/**
	 * Gets a AuditMessages manager business logic.
	 * @return groups manager
	 */
	AuditMessagesManagerBl getAuditMessagesManagerBl();

	/**
	 * Gets a groups manager buisness logic.
	 * @return groups manager
	 */
	GroupsManagerBl getGroupsManagerBl();

	/**
	 * Gets a resource manager buisness logic.
	 * @return resource manager
	 */
	FacilitiesManagerBl getFacilitiesManagerBl();

	/**
	 * Gets a database manager buisness logic.
	 * @return database manager
	 */
	DatabaseManagerBl getDatabaseManagerBl();
	
	/**
	 * Gets a users manager buisness logic.
	 * @return users manager
	 */
	UsersManagerBl getUsersManagerBl();

	/**
	 * Gets a members manager buisness logic.
	 * @return members manager
	 */
	MembersManagerBl getMembersManagerBl();

	/**
	 * Gets a VOs manager buisness logic.
	 * @return VOs manager
	 */
	VosManagerBl getVosManagerBl();

	/**
	 * Gets a Resources manager buisness logic.
	 * @return Resources manager
	 */
	ResourcesManagerBl getResourcesManagerBl();

	/**
	 * Gets a ExtSources manager buisness logic.
	 * @return ExtSources manager
	 */
	ExtSourcesManagerBl getExtSourcesManagerBl();

	/**
	 * Gets a Attributes manager buisness logic.
	 * @return Attributes manager
	 */
	AttributesManagerBl getAttributesManagerBl();

	/**
	 * Gets a Services manager buisness logic.
	 * @return Services manager
	 */
	ServicesManagerBl getServicesManagerBl();

	/**
	 * Gets a Owners manager buisness logic.
	 * @return Owners manager
	 */
	OwnersManagerBl getOwnersManagerBl();

	/**
	 * Gets a Messages manager.
	 * @return Messages manager
	 */
	RTMessagesManagerBl getRTMessagesManagerBl();

	/**
	 * Gets a Security Teams manager.
	 * @return Security Teams manager
	 */
	SecurityTeamsManagerBl getSecurityTeamsManagerBl();

	/**
	 * Gets a SearcherBl
	 * @return SearcherBl
	 */
	SearcherBl getSearcherBl();

	/**
	 * Gets a ModulesUtilsBl
	 * @return  ModulesUtilsBl
	 */
	ModulesUtilsBl getModulesUtilsBl();

	/**
	 * Gets a TasksManagerBl
	 * @return TasksManagerBl
	 */
	TasksManagerBl getTasksManagerBl();
}
