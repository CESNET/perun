package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Auditer;

/**
 * Perun himself.
 *
 * See {@link cz.metacentrum.perun.core.bl.PerunBl#bootstrap()} to find how to get
 * an instance of Perun.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface Perun {

    /**
     * Gets a (possibly cached) Perun session.
     * @throws InternalErrorException raised when session cannot be created.
     * @param actor identification of the actor, who will perform operations.
     * @return perun session
     */
    PerunSession getPerunSession(PerunPrincipal actor) throws InternalErrorException ;

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
     * Gets a AuditMessages manager.
     * @return AuditMessages manager
     */
    public AuditMessagesManager getAuditMessagesManager();

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
     * Gets the Auditer.
     * @return Auditer/
     */
    public Auditer getAuditer();

    /**
     * Gets a Messages manager.
     * @return Messages manager
     */
    public RTMessagesManager getRTMessagesManager();

    /**
     * Gets a Searcher.
     * @return Searcher
     */
    public Searcher getSearcher();
}
