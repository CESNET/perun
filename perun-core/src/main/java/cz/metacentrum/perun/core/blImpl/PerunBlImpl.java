package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.*;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

	final static Logger log = LoggerFactory.getLogger(PerunBlImpl.class);

	private final static Set<String> dontLookupUsersForLogins = BeansUtils.getCoreConfig().getDontLookupUsers();

	public PerunBlImpl() {

	}

	public PerunSession getPerunSession(PerunPrincipal principal, PerunClient client) throws InternalErrorException {
		PerunSessionImpl perunSession = new PerunSessionImpl(this, principal, client);
		log.debug("creating PerunSession for user {}",principal.getActor());
		if (principal.getUser() == null && usersManagerBl!=null && !dontLookupUsersForLogins.contains(principal.getActor())) {
			// Get the user if we are completely initialized
			try {
				PerunSession internalSession = getPerunSession();
				User user = usersManagerBl.getUserByExtSourceNameAndExtLogin(internalSession, principal.getExtSourceName(), principal.getActor());
				principal.setUser(user);
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
				List<AttributeDefinition> attrs = coreConfig.getAttributesForUpdate().get(principal.getExtSourceType());
				if (attrs != null) {
					for (AttributeDefinition attr : attrs) {
						//get value from authentication
						String attrValue = principal.getAdditionalInformations().get(attr.getFriendlyName());
						if ("".equals(attrValue)) attrValue = null;
						//save the value to attribute (create the attribute if it does not exist)
						try {
							Attribute attributeWithValue;
							try {
								attributeWithValue = attributesManagerBl.getAttribute(perunSession, ues, attr.getName());
							} catch (AttributeNotExistsException ex) {
								try {
									attributeWithValue = new Attribute(attributesManagerBl.createAttribute(perunSession, attr));
								} catch (AttributeDefinitionExistsException e) {
									attributeWithValue = attributesManagerBl.getAttribute(perunSession, ues, attr.getName());
								}
							}
							attributeWithValue.setValue(attrValue);
							log.debug("storing attribute {}='{}' for user {}",attributeWithValue.getFriendlyName(),attrValue,principal.getActor());
							attributesManagerBl.setAttribute(perunSession, ues, attributeWithValue);
						} catch (AttributeNotExistsException | WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException | MemberResourceMismatchException | GroupResourceMismatchException e) {
							log.error("Attribute " + attr.getName() + " with value '" + attrValue + "' cannot be saved", e);
						}
					}
				}
			} catch (ExtSourceNotExistsException | UserExtSourceNotExistsException | UserNotExistsException e) {
				// OK - We don't know user yet
			}
		}
		return perunSession;
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

	public GroupsManager getGroupsManager() {
		return groupsManager;
	}

	public FacilitiesManager getFacilitiesManager() {
		return facilitiesManager;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public UsersManager getUsersManager() {
		return usersManager;
	}

	public MembersManager getMembersManager() {
		return membersManager;
	}

	public VosManager getVosManager() {
		return vosManager;
	}

	public ResourcesManager getResourcesManager() {
		return resourcesManager;
	}

	public RTMessagesManager getRTMessagesManager() {
		return rtMessagesManager;
	}

	public void setRTMessagesManager(RTMessagesManager rtMessagesManager) {
		this.rtMessagesManager = rtMessagesManager;
	}

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

	public AttributesManager getAttributesManager() {
		return attributesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setOwnersManager(OwnersManager ownersManager) {
		this.ownersManager = ownersManager;
	}

	public OwnersManager getOwnersManager() {
		return ownersManager;
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public ModulesUtilsBl getModulesUtilsBl() {
		return modulesUtilsBl;
	}

	public void setModulesUtilsBl(ModulesUtilsBl modulesUtilsBl) {
		this.modulesUtilsBl = modulesUtilsBl;
	}

	public RTMessagesManagerBl getRTMessagesManagerBl() {
		return rtMessagesManagerBl;
	}

	public void setRTMessagesManagerBl(RTMessagesManagerBl rtMessagesManagerBl) {
		this.rtMessagesManagerBl = rtMessagesManagerBl;
	}

	public AuditMessagesManager getAuditMessagesManager() {
		return auditMessagesManager;
	}

	public VosManagerBl getVosManagerBl() {
		return vosManagerBl;
	}

	public void setVosManagerBl(VosManagerBl vosManagerBl) {
		this.vosManagerBl = vosManagerBl;
	}

	public UsersManagerBl getUsersManagerBl() {
		return usersManagerBl;
	}

	public AuditMessagesManagerBl getAuditMessagesManagerBl() {
		return auditMessagesManagerBl;
	}

	public void setUsersManagerBl(UsersManagerBl usersManagerBl) {
		this.usersManagerBl = usersManagerBl;
	}

	public MembersManagerBl getMembersManagerBl() {
		return membersManagerBl;
	}

	public void setMembersManagerBl(MembersManagerBl membersManagerBl) {
		this.membersManagerBl = membersManagerBl;
	}

	public GroupsManagerBl getGroupsManagerBl() {
		return groupsManagerBl;
	}

	public void setGroupsManagerBl(GroupsManagerBl groupsManagerBl) {
		this.groupsManagerBl = groupsManagerBl;
	}

	public FacilitiesManagerBl getFacilitiesManagerBl() {
		return facilitiesManagerBl;
	}

	public void setFacilitiesManagerBl(FacilitiesManagerBl facilitiesManagerBl) {
		this.facilitiesManagerBl = facilitiesManagerBl;
	}

	public DatabaseManagerBl getDatabaseManagerBl() {
		return databaseManagerBl;
	}

	public void setDatabaseManagerBl(DatabaseManagerBl databaseManagerBl) {
		this.databaseManagerBl = databaseManagerBl;
	}

	public ResourcesManagerBl getResourcesManagerBl() {
		return resourcesManagerBl;
	}

	public void setResourcesManagerBl(ResourcesManagerBl resourcesManagerBl) {
		this.resourcesManagerBl = resourcesManagerBl;
	}

	public ExtSourcesManagerBl getExtSourcesManagerBl() {
		return extSourcesManagerBl;
	}

	public void setExtSourcesManagerBl(ExtSourcesManagerBl extSourcesManagerBl) {
		this.extSourcesManagerBl = extSourcesManagerBl;
	}

	public AttributesManagerBl getAttributesManagerBl() {
		return attributesManagerBl;
	}

	public void setAttributesManagerBl(AttributesManagerBl attributesManagerBl) {
		this.attributesManagerBl = attributesManagerBl;
	}

	public ServicesManagerBl getServicesManagerBl() {
		return servicesManagerBl;
	}

	public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
		this.servicesManagerBl = servicesManagerBl;
	}

	public OwnersManagerBl getOwnersManagerBl() {
		return ownersManagerBl;
	}

	public void setOwnersManagerBl(OwnersManagerBl ownersManagerBl) {
		this.ownersManagerBl = ownersManagerBl;
	}

	public SecurityTeamsManagerBl getSecurityTeamsManagerBl() {
		return securityTeamsManagerBl;
	}

	public void setSecurityTeamsManagerBl(SecurityTeamsManagerBl securityTeamsManagerBl) {
		this.securityTeamsManagerBl = securityTeamsManagerBl;
	}

	public Auditer getAuditer() {
		return this.auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

	public SearcherBl getSearcherBl() {
		return searcherBl;
	}

	public void setSearcherBl(SearcherBl searcherBl) {
		this.searcherBl = searcherBl;
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
	}

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
