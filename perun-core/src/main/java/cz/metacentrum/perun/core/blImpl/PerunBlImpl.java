package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.PerunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessagesManager;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.SecurityTeamsManager;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
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
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of Perun.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunBlImpl implements PerunBl {
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
	private AuthzResolverBl authzResolverBl = null;
	private SearcherBl searcherBl = null;

	private Auditer auditer = null;

	final static Logger log = LoggerFactory.getLogger(PerunBlImpl.class);

	private final static Set<String> dontLookupUsersForLogins = BeansUtils.getCoreConfig().getDontLookupUsers();

	public PerunBlImpl() {

	}

	public PerunSession getPerunSession(PerunPrincipal principal, PerunClient client) throws InternalErrorException {
		if (principal.getUser() == null &&
				this.getUsersManagerBl() != null &&
				!dontLookupUsersForLogins.contains(principal.getActor())) {
			// Get the user if we are completely initialized
			try {
				principal.setUser(this.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(getPerunSession(), principal.getExtSourceName(), principal.getActor()));

				// Try to update LoA for userExtSource
				ExtSource es = this.getExtSourcesManagerBl().getExtSourceByName(getPerunSession(), principal.getExtSourceName());
				UserExtSource ues = this.getUsersManagerBl().getUserExtSourceByExtLogin(getPerunSession(), es, principal.getActor());
				if (ues.getLoa() != principal.getExtSourceLoa()) {
					ues.setLoa(principal.getExtSourceLoa());
					this.getUsersManagerBl().updateUserExtSource(getPerunSession(), ues);
				}

				// Update last access for userExtSource
				this.getUsersManagerBl().updateUserExtSourceLastAccess(getPerunSession(), ues);

			} catch (ExtSourceNotExistsException | UserExtSourceNotExistsException | UserNotExistsException e) {
				// OK - We don't know user yet
			}
		}
		return new PerunSessionImpl(this, principal, client);
	}

	/**
	 * This method is used only internally.
	 *
	 */
	public PerunSession getPerunSession() throws InternalErrorException {
		PerunPrincipal principal = new PerunPrincipal(INTERNALPRINCIPAL, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		PerunClient client = new PerunClient();
		return new PerunSessionImpl(this, principal, client);
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

	public AuthzResolverBl getAuthzResolverBl() {
		return authzResolverBl;
	}

	public void setAuthzResolverBl(AuthzResolverBl authzResolverBl) {
		this.authzResolverBl = authzResolverBl;
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

	/**
	 * Creates a Perun instance.
	 * <p/>
	 * Uses {@link org.springframework.context.support.ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String...)}
	 * to load files perun-core.xml and  perun-core-jdbc.xml from CLASSPATH.
	 * <p/>
	 * <h3>Web applications</h3>
	 * <p>In web applications, use {@link org.springframework.web.context.WebApplicationContext} to either load
	 * the same files, or load just  perun-core.xml and provide your own definition of {@link javax.sql.DataSource}
	 * with id dataSource.</p>
	 * <p>The use {link org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext}
	 * to retrieve the context, i.e. add to web.xml the following:</p>
	 * <pre>
	 * &lt;listener&gt;
	 *   &lt;listener-class&gt;org.springframework.web.context.ContextLoaderListener&lt;/listener-class&gt;
	 * &lt;/listener&gt;
	 * &lt;context-param&gt;
	 *   &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
	 *   &lt;param-value&gt;classpath:perun-core.xml,classpath:perun-core-jdbc.xml&lt;/param-value&gt;
	 * &lt;/context-param&gt;
	 * </pre>
	 * and in servlets use this code:
	 * <pre>
	 *  Perun perun = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("perun",Perun.class);
	 * </pre>
	 * <p>The code gets always the same instance of Perun, as the getWebApplicationContext() only gets application context
	 * from ServletContext attribute, and getBean() then gets the same singleton.</p>
	 *
	 * @deprecated It is discouraged to use the Perun bootstrap in order
	 * to obtain a new Perun instance. Use Spring dependency injection please (both in the application code and in tests).
	 * Perun bootstrap is going to be removed in one of the future versions.
	 *
	 * E.g:
	 *
	 * Put following code in your Spring Application Context xml file:
	 *
	 *     <import resource="classpath:perun-core.xml"/>
	 *
	 *
	 * @return Perun instance
	 */
	@Deprecated
	public static Perun bootstrap() {
		ApplicationContext springCtx = new ClassPathXmlApplicationContext("perun-beans.xml", "perun-datasources.xml");
		return springCtx.getBean("perun",Perun.class);
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
