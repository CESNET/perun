package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessagesManager;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.SecurityTeamsManager;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.OwnersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.RTMessagesManagerBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.bl.SecurityTeamsManagerBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.bl.*;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.metacentrum.perun.core.impl.CacheManager;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Perun.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunBlImpl implements PerunBl {

	private CoreConfig coreConfig;
	private VosManager vosManager = null;
	private UsersManager usersManager = null;
	private MembersManager membersManager = null;
	private GroupsManager groupsManager = null;
	private FacilitiesManager facilitiesManager = null;
	private DatabaseManager databaseManager = null;
	private ResourcesManager resourcesManager = null;
	private ExtSourcesManager extSourcesManager = null;
	private AttributesManager attributesManager = null;
	private ServicesManager servicesManager = null;
	private OwnersManager ownersManager = null;
	private AuditMessagesManager auditMessagesManager = null;
	private RTMessagesManager rtMessagesManager = null;
	private SecurityTeamsManager securityTeamsManager = null;
	private Searcher searcher = null;

	private ModulesUtilsBl modulesUtilsBl = null;
	private VosManagerBl vosManagerBl = null;
	private UsersManagerBl usersManagerBl = null;
	private MembersManagerBl membersManagerBl = null;
	private GroupsManagerBl groupsManagerBl = null;
	private FacilitiesManagerBl facilitiesManagerBl = null;
	private DatabaseManagerBl databaseManagerBl = null;
	private ResourcesManagerBl resourcesManagerBl = null;
	private ExtSourcesManagerBl extSourcesManagerBl = null;
	private AttributesManagerBl attributesManagerBl = null;
	private ServicesManagerBl servicesManagerBl = null;
	private OwnersManagerBl ownersManagerBl = null;
	private AuditMessagesManagerBl auditMessagesManagerBl = null;
	private RTMessagesManagerBl rtMessagesManagerBl = null;
	private SecurityTeamsManagerBl securityTeamsManagerBl = null;
	private SearcherBl searcherBl = null;

	private Auditer auditer = null;
	private CacheManager cacheManager = null;
	private AttributesManagerImplApi attributesManagerImpl = null;

	final static Logger log = LoggerFactory.getLogger(PerunBlImpl.class);

	private final static Set<String> dontLookupUsersForLogins = BeansUtils.getCoreConfig().getDontLookupUsers();

	public PerunBlImpl() {

	}

	public PerunBlImpl(Auditer auditer) {
		this.auditer = auditer;
	}

	@Override
	public PerunSession getPerunSession(PerunPrincipal principal, PerunClient client) throws InternalErrorException {
		PerunSessionImpl perunSession = new PerunSessionImpl(this, principal, client);
		log.debug("creating PerunSession for user {}", principal.getActor());
		if (principal.getUser() == null && usersManagerBl != null && !dontLookupUsersForLogins.contains(principal.getActor())) {
			// Get the user if we are completely initialized
			try {
				PerunSession internalSession = getPerunSession();
				User user = usersManagerBl.getUserByExtSourceNameAndExtLogin(internalSession, principal.getExtSourceName(), principal.getActor());
				principal.setUser(user);

				if (client.getType() != PerunClient.Type.OAUTH) {
					// Try to update LoA for userExtSource
					ExtSource es = extSourcesManagerBl.getExtSourceByName(internalSession, principal.getExtSourceName());
					UserExtSource ues = usersManagerBl.getUserExtSourceByExtLogin(internalSession, es, principal.getActor());
					if (ues.getLoa() != principal.getExtSourceLoa()) {
						ues.setLoa(principal.getExtSourceLoa());
						usersManagerBl.updateUserExtSource(internalSession, ues);
					}
					// Update last access for userExtSource
					usersManagerBl.updateUserExtSourceLastAccess(internalSession, ues);

					// update selected attributes for given extsourcetype
					setUserExtSourceAttributes(perunSession, ues, principal.getAdditionalInformations());

				}
			} catch (ExtSourceNotExistsException | UserExtSourceNotExistsException | UserNotExistsException | UserExtSourceExistsException e) {
				// OK - We don't know user yet or we are modifying more than a LoA and we shouldn't !!
			}
		}
		return perunSession;
	}

	/**
	 * Store values from map "additionalAttributes" as UserExtSource attributes to specified UES.
	 * Used internally when session is initialized and when user is self-created through registration.
	 * Only specific map keys are stored, based on Perun config for UES type.
	 *
	 * @param session PerunSession for authorization
	 * @param ues UserExtSource to store attributes for
	 * @param additionalAttributes Map of attribute names=values
	 * @throws InternalErrorException When implementation fails
	 */
	public void setUserExtSourceAttributes(PerunSession session, UserExtSource ues, Map<String, String> additionalAttributes) throws InternalErrorException {

		// update selected attributes for given extsourcetype
		List<AttributeDefinition> attrs = coreConfig.getAttributesForUpdate().get(ues.getExtSource().getType());
		if (attrs != null) {
			for (AttributeDefinition attr : attrs) {
				//get value from authentication
				String attrValue = additionalAttributes.get(attr.getFriendlyName());
				if ("".equals(attrValue)) attrValue = null;
				//save the value to attribute (create the attribute if it does not exist)
				try {
					Attribute attributeWithValue;
					try {
						attributeWithValue = attributesManagerBl.getAttribute(session, ues, attr.getName());
					} catch (AttributeNotExistsException ex) {
						try {
							attributeWithValue = new Attribute(attributesManagerBl.createAttribute(session, attr));
						} catch (AttributeDefinitionExistsException e) {
							attributeWithValue = attributesManagerBl.getAttribute(session, ues, attr.getName());
						}
					}
					attributeWithValue.setValue(attrValue);
					log.debug("storing attribute {}='{}' for user {}", attributeWithValue.getFriendlyName(), attrValue, ues.getLogin());
					attributesManagerBl.setAttribute(session, ues, attributeWithValue);
				} catch (AttributeNotExistsException | WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException e) {
					log.error("Attribute " + attr.getName() + " with value '" + attrValue + "' cannot be saved", e);
				}
			}
		}

	}

	/**
	 * This method is used only internally.
	 */
	private PerunSession getPerunSession() throws InternalErrorException {
		PerunPrincipal principal = new PerunPrincipal(INTERNALPRINCIPAL, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		PerunClient client = new PerunClient();
		return new PerunSessionImpl(this, principal, client);
	}

	public void setCoreConfig(CoreConfig coreConfig) {
		this.coreConfig = coreConfig;
	}

	@Override
	public GroupsManager getGroupsManager() {
		return groupsManager;
	}

	@Override
	public FacilitiesManager getFacilitiesManager() {
		return facilitiesManager;
	}

	@Override
	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	@Override
	public UsersManager getUsersManager() {
		return usersManager;
	}

	@Override
	public MembersManager getMembersManager() {
		return membersManager;
	}

	@Override
	public VosManager getVosManager() {
		return vosManager;
	}

	@Override
	public ResourcesManager getResourcesManager() {
		return resourcesManager;
	}

	@Override
	public RTMessagesManager getRTMessagesManager() {
		return rtMessagesManager;
	}

	public void setRTMessagesManager(RTMessagesManager rtMessagesManager) {
		this.rtMessagesManager = rtMessagesManager;
	}

	@Override
	public SecurityTeamsManager getSecurityTeamsManager() {
		return securityTeamsManager;
	}

	public void setSecurityTeamsManager(SecurityTeamsManager securityTeamsManager) {
		this.securityTeamsManager = securityTeamsManager;
	}

	public void setVosManager(VosManager vosManager) {
		this.vosManager = vosManager;
	}

	public void setUsersManager(UsersManager usersManager) {
		this.usersManager = usersManager;
	}

	public void setAuditMessagesManager(AuditMessagesManager auditMessagesManager) {
		this.auditMessagesManager = auditMessagesManager;
	}

	public void setAuditMessagesManagerBl(AuditMessagesManagerBl auditMessagesManagerBl) {
		this.auditMessagesManagerBl = auditMessagesManagerBl;
	}

	public void setGroupsManager(GroupsManager groupsManager) {
		this.groupsManager = groupsManager;
	}

	public void setFacilitiesManager(FacilitiesManager facilitiesManager) {
		this.facilitiesManager = facilitiesManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setMembersManager(MembersManager membersManager) {
		this.membersManager = membersManager;
	}

	public void setResourcesManager(ResourcesManager resourcesManager) {
		this.resourcesManager = resourcesManager;
	}

	@Override
	public ExtSourcesManager getExtSourcesManager() {
		return extSourcesManager;
	}

	public void setExtSourcesManager(ExtSourcesManager extSourcesManager) {
		this.extSourcesManager = extSourcesManager;
	}

	public void setAttributesManager(AttributesManager attributesManager) {
		this.attributesManager = attributesManager;
	}

	public void setSearcher(Searcher searcher) {
		this.searcher = searcher;
	}

	@Override
	public AttributesManager getAttributesManager() {
		return attributesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	@Override
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setOwnersManager(OwnersManager ownersManager) {
		this.ownersManager = ownersManager;
	}

	@Override
	public OwnersManager getOwnersManager() {
		return ownersManager;
	}

	@Override
	public Searcher getSearcher() {
		return searcher;
	}

	@Override
	public ModulesUtilsBl getModulesUtilsBl() {
		return modulesUtilsBl;
	}

	public void setModulesUtilsBl(ModulesUtilsBl modulesUtilsBl) {
		this.modulesUtilsBl = modulesUtilsBl;
	}

	@Override
	public RTMessagesManagerBl getRTMessagesManagerBl() {
		return rtMessagesManagerBl;
	}

	public void setRTMessagesManagerBl(RTMessagesManagerBl rtMessagesManagerBl) {
		this.rtMessagesManagerBl = rtMessagesManagerBl;
	}

	@Override
	public AuditMessagesManager getAuditMessagesManager() {
		return auditMessagesManager;
	}

	@Override
	public VosManagerBl getVosManagerBl() {
		return vosManagerBl;
	}

	public void setVosManagerBl(VosManagerBl vosManagerBl) {
		this.vosManagerBl = vosManagerBl;
	}

	@Override
	public UsersManagerBl getUsersManagerBl() {
		return usersManagerBl;
	}

	@Override
	public AuditMessagesManagerBl getAuditMessagesManagerBl() {
		return auditMessagesManagerBl;
	}

	public void setUsersManagerBl(UsersManagerBl usersManagerBl) {
		this.usersManagerBl = usersManagerBl;
	}

	@Override
	public MembersManagerBl getMembersManagerBl() {
		return membersManagerBl;
	}

	public void setMembersManagerBl(MembersManagerBl membersManagerBl) {
		this.membersManagerBl = membersManagerBl;
	}

	@Override
	public GroupsManagerBl getGroupsManagerBl() {
		return groupsManagerBl;
	}

	public void setGroupsManagerBl(GroupsManagerBl groupsManagerBl) {
		this.groupsManagerBl = groupsManagerBl;
	}

	@Override
	public FacilitiesManagerBl getFacilitiesManagerBl() {
		return facilitiesManagerBl;
	}

	public void setFacilitiesManagerBl(FacilitiesManagerBl facilitiesManagerBl) {
		this.facilitiesManagerBl = facilitiesManagerBl;
	}

	@Override
	public DatabaseManagerBl getDatabaseManagerBl() {
		return databaseManagerBl;
	}

	public void setDatabaseManagerBl(DatabaseManagerBl databaseManagerBl) {
		this.databaseManagerBl = databaseManagerBl;
	}

	@Override
	public ResourcesManagerBl getResourcesManagerBl() {
		return resourcesManagerBl;
	}

	public void setResourcesManagerBl(ResourcesManagerBl resourcesManagerBl) {
		this.resourcesManagerBl = resourcesManagerBl;
	}

	@Override
	public ExtSourcesManagerBl getExtSourcesManagerBl() {
		return extSourcesManagerBl;
	}

	public void setExtSourcesManagerBl(ExtSourcesManagerBl extSourcesManagerBl) {
		this.extSourcesManagerBl = extSourcesManagerBl;
	}

	@Override
	public AttributesManagerBl getAttributesManagerBl() {
		return attributesManagerBl;
	}

	public void setAttributesManagerBl(AttributesManagerBl attributesManagerBl) {
		this.attributesManagerBl = attributesManagerBl;
	}

	@Override
	public ServicesManagerBl getServicesManagerBl() {
		return servicesManagerBl;
	}

	public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
		this.servicesManagerBl = servicesManagerBl;
	}

	@Override
	public OwnersManagerBl getOwnersManagerBl() {
		return ownersManagerBl;
	}

	public void setOwnersManagerBl(OwnersManagerBl ownersManagerBl) {
		this.ownersManagerBl = ownersManagerBl;
	}

	@Override
	public SecurityTeamsManagerBl getSecurityTeamsManagerBl() {
		return securityTeamsManagerBl;
	}

	public void setSecurityTeamsManagerBl(SecurityTeamsManagerBl securityTeamsManagerBl) {
		this.securityTeamsManagerBl = securityTeamsManagerBl;
	}

	@Override
	public Auditer getAuditer() {
		return this.auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

	@Override
	public SearcherBl getSearcherBl() {
		return searcherBl;
	}

	public void setSearcherBl(SearcherBl searcherBl) {
		this.searcherBl = searcherBl;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setAttributesManagerImpl(AttributesManagerImplApi attributesManagerImpl) {
		this.attributesManagerImpl = attributesManagerImpl;
	}

	public AttributesManagerImplApi getAttributesManagerImpl() {
		return attributesManagerImpl;
	}

	@Override
	public boolean isPerunReadOnly() {
		return BeansUtils.isPerunReadOnly();
	}

	/**
	 * Call managers' initialization methods
	 */
	public void initialize() throws InternalErrorException {
		this.extSourcesManagerBl.initialize(this.getPerunSession());
		this.auditer.initialize();
		if (coreConfig.isCacheEnabled()) {
			this.cacheManager.initialize(getPerunSession(), attributesManagerImpl);
			CacheManager.setCacheDisabled(false);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[" +
				"vosManager='" + vosManager + "', " +
				"usersManager='" + usersManager + "', " +
				"membersManager='" + membersManager + "', " +
				"groupsManager='" + groupsManager + "', " +
				"facilitiesManager='" + facilitiesManager + "', " +
				"databaseManager='" + databaseManager + "', " +
				"auditMessagesManager=" + auditMessagesManager + ", " +
				"resourcesManager='" + resourcesManager + "', " +
				"extSourcesManager='" + extSourcesManager + "', " +
				"attributesManager='" + attributesManager + "', " +
				"rtMessagesManager='" + rtMessagesManager + "', " +
				"securityTeamsManager='" + securityTeamsManager + "', " +
				"searcher='" + searcher + "', " +
				"servicesManager='" + servicesManager + "']";
	}
}
