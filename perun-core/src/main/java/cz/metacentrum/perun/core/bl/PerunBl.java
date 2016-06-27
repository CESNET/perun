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
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Perun himself.
 *
 * See {@link cz.metacentrum.perun.core.bl.PerunBl#bootstrap()} to find how to get
 * an instance of Perun.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public interface PerunBl extends Perun {

	public static final String PERUNVERSION = "3.0.0";
	public static final String INTERNALPRINCIPAL = "INTERNAL";

	/**
	 * Gets a (possibly cached) Perun session.
	 * @throws InternalErrorException raised when session cannot be created.
	 * @param actor identification of the actor, who will perform operations.
	 * @return perun session
	 */
	PerunSession getPerunSession(PerunPrincipal actor, PerunClient client) throws InternalErrorException ;

	/**
	 * Gets an internal Perun session.
	 * @throws InternalErrorException raised when session cannot be created.
	 * @return perun session
	 */
	PerunSession getPerunSession() throws InternalErrorException ;

	/**
	 * Gets a groups manager.
	 * @return groups manager
	 */
	public GroupsManager getGroupsManager();

	/**
	 * Gets a resource manager.
	 * @return resource manager
	 */
	public FacilitiesManager getFacilitiesManager();

	/**
	 * Gets a database manager.
	 * @return database manager
	 */
	public DatabaseManager getDatabaseManager();
	
	/**
	 * Gets a users manager.
	 * @return users manager
	 */
	public UsersManager getUsersManager();

	/**
	 * Gets a members manager.
	 * @return members manager
	 */
	public MembersManager getMembersManager();

	/**
	 * Gets a VOs manager.
	 * @return VOs manager
	 */
	public VosManager getVosManager();

	/**
	 * Gets a Resources manager.
	 * @return Resources manager
	 */
	public ResourcesManager getResourcesManager();

	/**
	 * Gets a ExtSources manager.
	 * @return ExtSources manager
	 */
	public ExtSourcesManager getExtSourcesManager();

	/**
	 * Gets a Attributes manager.
	 * @return Attributes manager
	 */
	public AttributesManager getAttributesManager();

	/**
	 * Gets a Services manager.
	 * @return Services manager
	 */
	public ServicesManager getServicesManager();

	/**
	 * Gets a Owners manager.
	 * @return Owners manager
	 */
	public OwnersManager getOwnersManager();

	/**
	 * Gets a AuditMessages manager.
	 * @return AuditMessages manager
	 */
	public AuditMessagesManager getAuditMessagesManager();

	/**
	 * Gets a Messages manager.
	 * @return Messages manager
	 */
	public RTMessagesManager getRTMessagesManager();

	/**
	 * Gets a Security Teams manager.
	 * @return Security Teams manager
	 */
	public SecurityTeamsManager getSecurityTeamsManager();

	/**
	 * Gets a Searcher.
	 * @return Searcher
	 */
	public Searcher getSearcher();

	/**
	 * Gets a AuditMessages manager business logic.
	 * @return groups manager
	 */
	public AuditMessagesManagerBl getAuditMessagesManagerBl();

	/**
	 * Gets a groups manager buisness logic.
	 * @return groups manager
	 */
	public GroupsManagerBl getGroupsManagerBl();

	/**
	 * Gets a resource manager buisness logic.
	 * @return resource manager
	 */
	public FacilitiesManagerBl getFacilitiesManagerBl();

	/**
	 * Gets a database manager buisness logic.
	 * @return database manager
	 */
	public DatabaseManagerBl getDatabaseManagerBl();
	
	/**
	 * Gets a users manager buisness logic.
	 * @return users manager
	 */
	public UsersManagerBl getUsersManagerBl();

	/**
	 * Gets a members manager buisness logic.
	 * @return members manager
	 */
	public MembersManagerBl getMembersManagerBl();

	/**
	 * Gets a VOs manager buisness logic.
	 * @return VOs manager
	 */
	public VosManagerBl getVosManagerBl();

	/**
	 * Gets a Resources manager buisness logic.
	 * @return Resources manager
	 */
	public ResourcesManagerBl getResourcesManagerBl();

	/**
	 * Gets a ExtSources manager buisness logic.
	 * @return ExtSources manager
	 */
	public ExtSourcesManagerBl getExtSourcesManagerBl();

	/**
	 * Gets a Attributes manager buisness logic.
	 * @return Attributes manager
	 */
	public AttributesManagerBl getAttributesManagerBl();

	/**
	 * Gets a Services manager buisness logic.
	 * @return Services manager
	 */
	public ServicesManagerBl getServicesManagerBl();

	/**
	 * Gets a Owners manager buisness logic.
	 * @return Owners manager
	 */
	public OwnersManagerBl getOwnersManagerBl();

	/**
	 * Gets a Messages manager.
	 * @return Messages manager
	 */
	public RTMessagesManagerBl getRTMessagesManagerBl();

	/**
	 * Gets a Security Teams manager.
	 * @return Security Teams manager
	 */
	public SecurityTeamsManagerBl getSecurityTeamsManagerBl();

	/**
	 * Gets a AuthzResolver.
	 * @return AuthzResolver
	 */
	public AuthzResolverBl getAuthzResolverBl();

	/**
	 * Gets a SearcherBl
	 * @return SearcherBl
	 */
	public SearcherBl getSearcherBl();

	/**
	 * Gets a ModulesUtilsBl
	 * @return  ModulesUtilsBl
	 */
	public ModulesUtilsBl getModulesUtilsBl();
}
