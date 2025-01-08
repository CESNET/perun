package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConfigManager;
import cz.metacentrum.perun.core.api.ConsentsManager;
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
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.TasksManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
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
import cz.metacentrum.perun.core.bl.ConfigManagerBl;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
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
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.ResourceAssignmentChecker;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import cz.metacentrum.perun.core.implApi.ResourceAssignmentActivatorApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * Implementation of Perun.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunBlImpl implements PerunBl {

  static final Logger LOG = LoggerFactory.getLogger(PerunBlImpl.class);
  private static final Set<String> DONT_LOOKUP_USERS_FOR_LOGINS = BeansUtils.getCoreConfig().getDontLookupUsers();
  private static final Set<String> EXT_SOURCES_WITH_MULTIPLE_IDENTIFIERS =
      BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();
  private static final boolean LOOKUP_USER_BY_IDENTIFIERS_AND_EXT_SOURCE_LOGIN =
      BeansUtils.getCoreConfig().getLookupUserByIdentifiersAndExtSourceLogin();
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
  private Searcher searcher = null;
  private TasksManager tasksManager = null;
  private ResourceAssignmentChecker resourceAssignmentChecker = null;
  private ConfigManager configManager = null;
  private ConsentsManager consentsManager = null;
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
  private SearcherBl searcherBl = null;
  private TasksManagerBl tasksManagerBl = null;
  private ConfigManagerBl configManagerBl = null;
  private ConsentsManagerBl consentsManagerBl = null;
  private Auditer auditer = null;
  private AttributesManagerImplApi attributesManagerImpl = null;
  private ResourceAssignmentActivatorApi resourceAssignmentActivator = null;

  public PerunBlImpl() {

  }

  public PerunBlImpl(Auditer auditer) {
    this.auditer = auditer;
  }

  private void checkExternalProgramDependencies() {
    List<String> programNames = coreConfig.getExternalProgramsDependencies();
    for (String programName : programNames) {
      int exitCode;
      try {
        Process process = new ProcessBuilder("which", programName).start();
        exitCode = process.waitFor();
      } catch (Exception e) {
        exitCode = 1;
      }
      if (exitCode != 0) {
        LOG.error("Required external program {} was not found. Abandoning perun initialization.", programName);
        throw new BeanInitializationException("Required external program " + programName + " was not found.");
      }
    }
  }

  /**
   * Called when Perun is shutting down to clean up opened resources.
   */
  public void destroy() {
    LOG.debug("destroying");
    this.extSourcesManagerBl.destroy();
  }

  @Override
  public AttributesManager getAttributesManager() {
    return attributesManager;
  }

  @Override
  public AttributesManagerBl getAttributesManagerBl() {
    return attributesManagerBl;
  }

  public AttributesManagerImplApi getAttributesManagerImpl() {
    return attributesManagerImpl;
  }

  @Override
  public AuditMessagesManager getAuditMessagesManager() {
    return auditMessagesManager;
  }

  @Override
  public AuditMessagesManagerBl getAuditMessagesManagerBl() {
    return auditMessagesManagerBl;
  }

  @Override
  public Auditer getAuditer() {
    return this.auditer;
  }

  @Override
  public ConfigManager getConfigManager() {
    return configManager;
  }

  @Override
  public ConfigManagerBl getConfigManagerBl() {
    return configManagerBl;
  }

  @Override
  public ConsentsManager getConsentsManager() {
    return consentsManager;
  }

  @Override
  public ConsentsManagerBl getConsentsManagerBl() {
    return consentsManagerBl;
  }

  @Override
  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  @Override
  public DatabaseManagerBl getDatabaseManagerBl() {
    return databaseManagerBl;
  }

  @Override
  public ExtSourcesManager getExtSourcesManager() {
    return extSourcesManager;
  }

  @Override
  public ExtSourcesManagerBl getExtSourcesManagerBl() {
    return extSourcesManagerBl;
  }

  @Override
  public FacilitiesManager getFacilitiesManager() {
    return facilitiesManager;
  }

  @Override
  public FacilitiesManagerBl getFacilitiesManagerBl() {
    return facilitiesManagerBl;
  }

  @Override
  public GroupsManager getGroupsManager() {
    return groupsManager;
  }

  @Override
  public GroupsManagerBl getGroupsManagerBl() {
    return groupsManagerBl;
  }

  @Override
  public MembersManager getMembersManager() {
    return membersManager;
  }

  @Override
  public MembersManagerBl getMembersManagerBl() {
    return membersManagerBl;
  }

  @Override
  public ModulesUtilsBl getModulesUtilsBl() {
    return modulesUtilsBl;
  }

  @Deprecated
  @Override
  public OwnersManager getOwnersManager() {
    return ownersManager;
  }

  @Deprecated
  @Override
  public OwnersManagerBl getOwnersManagerBl() {
    return ownersManagerBl;
  }

  @Override
  public PerunSession getPerunSession(PerunPrincipal principal, PerunClient client) {
    PerunSessionImpl perunSession = new PerunSessionImpl(this, principal, client);
    LOG.debug("creating PerunSession for user {}", principal.getActor());
    if (principal.getUser() == null && usersManagerBl != null &&
        !DONT_LOOKUP_USERS_FOR_LOGINS.contains(principal.getActor())) {
      // Get the user if we are completely initialized
      try {
        PerunSession internalSession = getPerunSession();
        User user = usersManagerBl.getUserByExtSourceInformation(internalSession, principal);
        principal.setUser(user);

        if (client.getType() != PerunClient.Type.OAUTH || BeansUtils.getCoreConfig().getRequestUserInfoEndpoint()) {
          // Try to update LoA for userExtSource
          UserExtSource ues = null;
          String shibIdentityProvider =
              principal.getAdditionalInformations().get(UsersManagerBl.ORIGIN_IDENTITY_PROVIDER_KEY);
          boolean findByExtSourceNameAndLogin = true;
          if (shibIdentityProvider != null && EXT_SOURCES_WITH_MULTIPLE_IDENTIFIERS.contains(shibIdentityProvider)) {
            try {
              findByExtSourceNameAndLogin = false;
              ues = usersManagerBl.getUserExtSourceFromMultipleIdentifiers(internalSession, principal);
            } catch (UserExtSourceNotExistsException ex) {
              findByExtSourceNameAndLogin = LOOKUP_USER_BY_IDENTIFIERS_AND_EXT_SOURCE_LOGIN;
            }
          }
          if (findByExtSourceNameAndLogin) {
            ExtSource es = extSourcesManagerBl.getExtSourceByName(internalSession, principal.getExtSourceName());
            ues = usersManagerBl.getUserExtSourceByExtLogin(internalSession, es, principal.getActor());
          }
          if (!BeansUtils.isPerunReadOnly()) {
            if (ues != null && ues.getLoa() != principal.getExtSourceLoa()) {
              ues.setLoa(principal.getExtSourceLoa());
              usersManagerBl.updateUserExtSource(internalSession, ues);
            }

            // Update last access for userExtSource
            usersManagerBl.updateUserExtSourceLastAccess(internalSession, ues);

            // update selected attributes for given extsourcetype
            setUserExtSourceAttributes(perunSession, ues, principal.getAdditionalInformations());
          }

        }
      } catch (ExtSourceNotExistsException | UserExtSourceNotExistsException | UserNotExistsException |
               UserExtSourceExistsException e) {
        // OK - We don't know user yet or we are modifying more than a LoA and we shouldn't !!
      }
    }
    return perunSession;
  }

  /**
   * This method is used only internally.
   */
  private PerunSession getPerunSession() {
    PerunPrincipal principal = new PerunPrincipal(INTERNALPRINCIPAL, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    PerunClient client = new PerunClient();
    return new PerunSessionImpl(this, principal, client);
  }

  @Override
  public RTMessagesManager getRTMessagesManager() {
    return rtMessagesManager;
  }

  @Override
  public RTMessagesManagerBl getRTMessagesManagerBl() {
    return rtMessagesManagerBl;
  }

  @Override
  public ResourceAssignmentActivatorApi getResourceAssignmentActivator() {
    return this.resourceAssignmentActivator;
  }

  @Override
  public ResourceAssignmentChecker getResourceAssignmentChecker() {
    return resourceAssignmentChecker;
  }

  @Override
  public ResourcesManager getResourcesManager() {
    return resourcesManager;
  }

  @Override
  public ResourcesManagerBl getResourcesManagerBl() {
    return resourcesManagerBl;
  }

  @Override
  public Searcher getSearcher() {
    return searcher;
  }

  @Override
  public SearcherBl getSearcherBl() {
    return searcherBl;
  }

  @Override
  public ServicesManager getServicesManager() {
    return servicesManager;
  }

  @Override
  public ServicesManagerBl getServicesManagerBl() {
    return servicesManagerBl;
  }

  @Override
  public TasksManager getTasksManager() {
    return tasksManager;
  }

  @Override
  public TasksManagerBl getTasksManagerBl() {
    return tasksManagerBl;
  }

  @Override
  public UsersManager getUsersManager() {
    return usersManager;
  }

  @Override
  public UsersManagerBl getUsersManagerBl() {
    return usersManagerBl;
  }

  @Override
  public VosManager getVosManager() {
    return vosManager;
  }

  @Override
  public VosManagerBl getVosManagerBl() {
    return vosManagerBl;
  }

  /**
   * Call managers' initialization methods
   */
  public void initialize() {
    checkExternalProgramDependencies();
    this.extSourcesManagerBl.initialize(this.getPerunSession());
    this.auditer.initialize();
  }

  @Override
  public boolean isPerunReadOnly() {
    return BeansUtils.isPerunReadOnly();
  }

  public void setAttributesManager(AttributesManager attributesManager) {
    this.attributesManager = attributesManager;
  }

  public void setAttributesManagerBl(AttributesManagerBl attributesManagerBl) {
    this.attributesManagerBl = attributesManagerBl;
  }

  public void setAttributesManagerImpl(AttributesManagerImplApi attributesManagerImpl) {
    this.attributesManagerImpl = attributesManagerImpl;
  }

  public void setAuditMessagesManager(AuditMessagesManager auditMessagesManager) {
    this.auditMessagesManager = auditMessagesManager;
  }

  public void setAuditMessagesManagerBl(AuditMessagesManagerBl auditMessagesManagerBl) {
    this.auditMessagesManagerBl = auditMessagesManagerBl;
  }

  public void setAuditer(Auditer auditer) {
    this.auditer = auditer;
  }

  public void setConfigManager(ConfigManager configManager) {
    this.configManager = configManager;
  }

  public void setConfigManagerBl(ConfigManagerBl configManagerBl) {
    this.configManagerBl = configManagerBl;
  }

  public void setConsentsManager(ConsentsManager consentsManager) {
    this.consentsManager = consentsManager;
  }

  public void setConsentsManagerBl(ConsentsManagerBl consentsManagerBl) {
    this.consentsManagerBl = consentsManagerBl;
  }

  public void setCoreConfig(CoreConfig coreConfig) {
    this.coreConfig = coreConfig;
  }

  public void setDatabaseManager(DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setDatabaseManagerBl(DatabaseManagerBl databaseManagerBl) {
    this.databaseManagerBl = databaseManagerBl;
  }

  public void setExtSourcesManager(ExtSourcesManager extSourcesManager) {
    this.extSourcesManager = extSourcesManager;
  }

  public void setExtSourcesManagerBl(ExtSourcesManagerBl extSourcesManagerBl) {
    this.extSourcesManagerBl = extSourcesManagerBl;
  }

  public void setFacilitiesManager(FacilitiesManager facilitiesManager) {
    this.facilitiesManager = facilitiesManager;
  }

  public void setFacilitiesManagerBl(FacilitiesManagerBl facilitiesManagerBl) {
    this.facilitiesManagerBl = facilitiesManagerBl;
  }

  public void setGroupsManager(GroupsManager groupsManager) {
    this.groupsManager = groupsManager;
  }

  public void setGroupsManagerBl(GroupsManagerBl groupsManagerBl) {
    this.groupsManagerBl = groupsManagerBl;
  }

  public void setMembersManager(MembersManager membersManager) {
    this.membersManager = membersManager;
  }

  public void setMembersManagerBl(MembersManagerBl membersManagerBl) {
    this.membersManagerBl = membersManagerBl;
  }

  public void setModulesUtilsBl(ModulesUtilsBl modulesUtilsBl) {
    this.modulesUtilsBl = modulesUtilsBl;
  }

  public void setOwnersManager(OwnersManager ownersManager) {
    this.ownersManager = ownersManager;
  }

  public void setOwnersManagerBl(OwnersManagerBl ownersManagerBl) {
    this.ownersManagerBl = ownersManagerBl;
  }

  public void setRTMessagesManager(RTMessagesManager rtMessagesManager) {
    this.rtMessagesManager = rtMessagesManager;
  }

  public void setRTMessagesManagerBl(RTMessagesManagerBl rtMessagesManagerBl) {
    this.rtMessagesManagerBl = rtMessagesManagerBl;
  }

  public void setResourceAssignmentActivator(ResourceAssignmentActivatorApi resourceAssignmentActivator) {
    this.resourceAssignmentActivator = resourceAssignmentActivator;
  }

  public void setResourceAssignmentChecker(ResourceAssignmentChecker resourceAssignmentChecker) {
    this.resourceAssignmentChecker = resourceAssignmentChecker;
  }

  public void setResourcesManager(ResourcesManager resourcesManager) {
    this.resourcesManager = resourcesManager;
  }

  public void setResourcesManagerBl(ResourcesManagerBl resourcesManagerBl) {
    this.resourcesManagerBl = resourcesManagerBl;
  }

  public void setSearcher(Searcher searcher) {
    this.searcher = searcher;
  }

  public void setSearcherBl(SearcherBl searcherBl) {
    this.searcherBl = searcherBl;
  }

  public void setServicesManager(ServicesManager servicesManager) {
    this.servicesManager = servicesManager;
  }

  public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
    this.servicesManagerBl = servicesManagerBl;
  }

  public void setTasksManager(TasksManager tasksManager) {
    this.tasksManager = tasksManager;
  }

  public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
    this.tasksManagerBl = tasksManagerBl;
  }

  /**
   * Store values from map "additionalAttributes" as UserExtSource attributes to specified UES. Used internally when
   * session is initialized and when user is self-created through registration. Only specific map keys are stored, based
   * on Perun config for UES type.
   *
   * @param session              PerunSession for authorization
   * @param ues                  UserExtSource to store attributes for
   * @param additionalAttributes Map of attribute names=values
   * @throws InternalErrorException When implementation fails
   */
  public void setUserExtSourceAttributes(PerunSession session, UserExtSource ues,
                                         Map<String, String> additionalAttributes) {

    // update selected attributes for given extsourcetype
    List<AttributeDefinition> attrs = coreConfig.getAttributesForUpdate().get(ues.getExtSource().getType());
    if (attrs != null) {

      for (AttributeDefinition attr : attrs) {
        //get value from authentication
        String attrValue = additionalAttributes.get(attr.getFriendlyName());
        if ("".equals(attrValue)) {
          attrValue = null;
        }
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

          //for Array list attributes try to parse string value into individual fields
          if (attributeWithValue.getType().equals(ArrayList.class.getName())) {
            List<String> value = new ArrayList<>();
            if (attrValue != null) {
              value =
                  new ArrayList<>(Arrays.asList(attrValue.split(UsersManagerBl.MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX)));
            }
            attributeWithValue.setValue(value);
          } else {
            attributeWithValue.setValue(attrValue);
          }

          LOG.debug("storing attribute {}='{}' for user {}", attributeWithValue.getFriendlyName(), attrValue,
              ues.getLogin());
          attributesManagerBl.setAttributeInNestedTransaction(session, ues, attributeWithValue);
        } catch (AttributeNotExistsException | WrongAttributeAssignmentException | WrongAttributeValueException |
                 WrongReferenceAttributeValueException e) {
          LOG.error("Attribute " + attr.getName() + " with value '" + attrValue + "' cannot be saved", e);
        }
      }
    }

  }

  public void setUsersManager(UsersManager usersManager) {
    this.usersManager = usersManager;
  }

  public void setUsersManagerBl(UsersManagerBl usersManagerBl) {
    this.usersManagerBl = usersManagerBl;
  }

  public void setVosManager(VosManager vosManager) {
    this.vosManager = vosManager;
  }

  public void setVosManagerBl(VosManagerBl vosManagerBl) {
    this.vosManagerBl = vosManagerBl;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":[" + "vosManager='" + vosManager + "', " + "usersManager='" + usersManager +
           "', " + "membersManager='" + membersManager + "', " + "groupsManager='" + groupsManager + "', " +
           "facilitiesManager='" + facilitiesManager + "', " + "configManager='" + configManager + "', " +
           "consentsManager='" + consentsManager + "', " + "databaseManager='" + databaseManager + "', " +
           "auditMessagesManager=" + auditMessagesManager + ", " + "resourcesManager='" + resourcesManager + "', " +
           "extSourcesManager='" + extSourcesManager + "', " + "attributesManager='" + attributesManager + "', " +
           "rtMessagesManager='" + rtMessagesManager + "', " + "searcher='" + searcher + "', " + "servicesManager='" +
        servicesManager + "']";
  }
}
