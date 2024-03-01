package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;
import cz.metacentrum.perun.core.impl.Synchronizer;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;

/**
 * Class handling auto rejection of expired applications for VOs and Groups
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class AppAutoRejectionScheduler {

  private final static Logger log = LoggerFactory.getLogger(Synchronizer.class);
  private static final String A_VO_APP_EXP_RULES = "urn:perun:vo:attribute-def:def:applicationExpirationRules";
  private static final String A_GROUP_APP_EXP_RULES = "urn:perun:group:attribute-def:def:applicationExpirationRules";
  private static final String A_VO_APP_REJECT_MESSAGES = "urn:perun:vo:attribute-def:def:applicationAutoRejectMessages";
  private static final String A_GROUP_APP_REJECT_MESSAGES =
      "urn:perun:group:attribute-def:def:applicationAutoRejectMessages";
  private static final String A_USER_PREFERRED_LANGUAGE = "urn:perun:user:attribute-def:def:preferredLanguage";
  private static final String GROUP_PLACEHOLDER = "%group_name%";
  private static final String VO_PLACEHOLDER = "%vo_name%";
  private static final String ADMIN_IGNORED_KEY = "ignoredByAdmin";
  private static final String EMAIL_VERIFICATION_KEY = "emailVerification";
  private static final String DEFAULT_LANG = "en";
  private static final String DEFAULT_VO_MSG_ADMIN = "Your application to VO " + VO_PLACEHOLDER +
      " was automatically rejected, because admin didn't approve your application in a timely manner.";
  private static final String DEFAULT_GROUP_MSG_ADMIN = "Your application to group " + GROUP_PLACEHOLDER +
      " was automatically rejected, because admin didn't approve your application in a timely manner.";
  private static final String DEFAULT_VO_MSG_MAIL = "Your application to VO " + VO_PLACEHOLDER + " was " +
      "automatically rejected, because you didn't verify your email address.";
  private static final String DEFAULT_GROUP_MSG_MAIL = "Your application to group " + GROUP_PLACEHOLDER +
      " was automatically rejected, because you didn't verify your email address.";
  private final SearcherBl searcherBl;
  private JdbcPerunTemplate jdbc;
  private PerunSession sess;
  private PerunBl perun;
  private RegistrarManager registrarManager;

  /**
   * Constructor for unit tests
   *
   * @param perun PerunBl bean
   */
  public AppAutoRejectionScheduler(PerunBl perun, SearcherBl searcherBl) {
    this.perun = perun;
    this.searcherBl = searcherBl;
    initialize();
  }

  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbc = new JdbcPerunTemplate(dataSource);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  public PerunBl getPerun() {
    return perun;
  }

  @Autowired
  public void setPerun(PerunBl perun) {
    this.perun = perun;
  }

  public RegistrarManager getRegistrarManager() {
    return registrarManager;
  }

  @Autowired
  public void setRegistrarManager(RegistrarManager registrarManager) {
    this.registrarManager = registrarManager;
  }

  public void initialize() {
    this.sess = perun.getPerunSession(
        new PerunPrincipal("perunRegistrar", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL),
        new PerunClient());
  }

  /**
   * Perform check on applications state and expiration attribute and reject them, if it is necessary.
   * Rejection is based on current date and their value of application expiration.
   * <p>
   * Method is triggered by Spring scheduler (at midnight everyday).
   *
   * @throws VoNotExistsException    if vo not exist (it shouldn't happen)
   * @throws GroupNotExistsException if group not exist (it shouldn't happen)
   */
  public void checkApplicationsExpiration() throws VoNotExistsException, GroupNotExistsException {
    //Do not run, when perun is in readOnly mode
    if (perun.isPerunReadOnly()) {
      log.debug("Perun is in readOnly mode, skipping auto-rejection of expired applications.");
      return;
    }

    List<Vo> vos = getAllEligibleVos();
    // check applications expiration in eligible vos
    try {
      voApplicationsAutoRejection(vos);
    } catch (InternalErrorException | PerunException e) {
      log.error("Synchronizer: voApplicationsAutoRejection", e);
    }

    List<Group> groups = getAllEligibleGroups();
    // check applications expiration in eligible groups
    try {
      groupApplicationsAutoRejection(groups);
    } catch (InternalErrorException | PerunException e) {
      log.error("Synchronizer: groupApplicationsAutoRejection", e);
    }
  }

  /**
   * Returns current system time.
   *
   * @return current time.
   */
  public LocalDate getCurrentLocalDate() {
    return LocalDate.now();
  }

  /**
   * Checks all applications for given vos and if some application is expired (according to expiration rules set by
   * VO Manager), then rejects this application.
   *
   * @param vos eligible virtual organizations
   * @throws PerunException perun exception
   */
  private void voApplicationsAutoRejection(List<Vo> vos) throws PerunException {
    List<String> states = new ArrayList<>();
    states.add("NEW");
    states.add("VERIFIED");

    for (Vo vo : vos) {
      Attribute expiration = perun.getAttributesManagerBl().getAttribute(sess, vo, A_VO_APP_EXP_RULES);
      if (expiration.getValue() != null) {
        List<Application> applications = registrarManager.getApplicationsForVo(sess, vo, states, false);
        rejectExpiredApplications(applications, expiration);
      }
    }
  }

  /**
   * Gets all existing groups and then checks all applications for this groups and if some application is expired
   * (according to expiration rules set by VO Manager), then rejects this application.
   *
   * @param groups eligible groups
   * @throws PerunException perun exception
   */
  private void groupApplicationsAutoRejection(List<Group> groups) throws PerunException {
    List<String> states = new ArrayList<>();
    states.add("NEW");
    states.add("VERIFIED");

    for (Group group : groups) {
      Attribute expiration = perun.getAttributesManagerBl().getAttribute(sess, group, A_GROUP_APP_EXP_RULES);
      if (expiration.getValue() != null) {
        List<Application> applications = registrarManager.getApplicationsForGroup(sess, group, states);
        rejectExpiredApplications(applications, expiration);
      }
    }
  }

  /**
   * Compares date of last modification of application to values in expiration attribute and if finds expired application, then
   * rejects it.
   *
   * @param applications applications
   * @param expiration   attribute with number of days to application expiration
   */
  private void rejectExpiredApplications(List<Application> applications, Attribute expiration) {
    Map<String, String> attrValue = expiration.valueAsMap();
    for (Application application : applications) {
      String date = application.getModifiedAt();
      LocalDate modifiedAt = LocalDate.parse(date.substring(0, 10));
      LocalDate now = getCurrentLocalDate();
      if (application.getState() == Application.AppState.NEW && attrValue.containsKey(EMAIL_VERIFICATION_KEY)) {
        int expirationAppWaitingForEmail = Integer.parseInt(attrValue.get(EMAIL_VERIFICATION_KEY));
        if (now.minusDays(expirationAppWaitingForEmail).isAfter(modifiedAt)) {
          if (application.getGroup() != null) {
            rejectApplication(application, EMAIL_VERIFICATION_KEY, DEFAULT_GROUP_MSG_MAIL);
          } else {
            rejectApplication(application, EMAIL_VERIFICATION_KEY, DEFAULT_VO_MSG_MAIL);
          }
        }
      } else if (attrValue.containsKey(ADMIN_IGNORED_KEY)) {
        int expirationAppIgnoredByAdmin = Integer.parseInt(attrValue.get(ADMIN_IGNORED_KEY));
        if (now.minusDays(expirationAppIgnoredByAdmin).isAfter(modifiedAt)) {
          if (application.getGroup() != null) {
            rejectApplication(application, ADMIN_IGNORED_KEY, DEFAULT_GROUP_MSG_ADMIN);
          } else {
            rejectApplication(application, ADMIN_IGNORED_KEY, DEFAULT_VO_MSG_ADMIN);
          }
        }
      }
    }
  }

  /**
   * Rejects the given application with custom message. The 'attrValueKey' represents the
   * key of the message in the applicationAutoRejectMessages attribute. If the vo/group has no
   * such attribute set, the default message will be used.
   *
   * @param application    application to be rejected
   * @param attrValueKey   key of a message in the applicationAutoRejectMessages
   * @param defaultMessage default message if the appropriate vo/group has no
   *                       applicationAutoRejectMessages attribute set
   */
  private void rejectApplication(Application application, String attrValueKey, String defaultMessage) {
    Group group = application.getGroup();
    String lang = getUserPreferredLang(application);
    Vo vo = application.getVo();

    Attribute messagesAttr = null;
    try {
      if (group != null) {
        messagesAttr = perun.getAttributesManagerBl().getAttribute(sess, group, A_GROUP_APP_REJECT_MESSAGES);
      } else {
        messagesAttr = perun.getAttributesManagerBl().getAttribute(sess, vo, A_VO_APP_REJECT_MESSAGES);
      }
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      log.error("Failed to get attribute with reject messages.", e);
    }

    String message;
    if (group != null) {
      message = getRejectMessage(messagesAttr, lang, attrValueKey, defaultMessage)
          .replaceAll(GROUP_PLACEHOLDER, group.getName());
    } else {
      message = getRejectMessage(messagesAttr, lang, attrValueKey, defaultMessage)
          .replaceAll(VO_PLACEHOLDER, vo.getName());
    }

    try {
      registrarManager.rejectApplication(sess, application.getId(), message);
    } catch (PerunException e) {
      log.error("Failed to reject application {}.", application, e);
    }
  }

  /**
   * Returns reject message obtained from the given attribute, using the given key; or returns the default msg,
   * if there is no suitable message in the attribute.
   *
   * @param attribute  attribute of type map
   * @param lang       user preferred language
   * @param key        key used to find the value in the given attribute
   * @param defaultMsg returned if there is no suitable message in the attribute
   * @return reject message, concerning the preferred language, obtained from the given attribute,
   * or the given default message
   */
  private String getRejectMessage(Attribute attribute, String lang, String key, String defaultMsg) {
    String attrKey = key + "-" + lang;
    String attrValue = attribute != null && attribute.getValue() != null
        ? attribute.valueAsMap().get(attrKey)
        : null;

    if (attrValue == null && attribute != null && attribute.getValue() != null) {
      attrValue = attribute.valueAsMap().get(key);
    }

    return attrValue != null ? attrValue : defaultMsg;
  }

  /**
   * Returns preferred language of user from the given application.
   * Preferred language is taken from the submitted application, or from the user's
   * preferred language attribute. If none is present, 'en' is returned.
   *
   * @param application user
   * @return preferred language, 'en' as default
   */
  private String getUserPreferredLang(Application application) {
    User user = application.getUser();
    if (user == null) {
      String appLang = null;
      try {
        appLang = getPreferredLangFromApplication(application);
      } catch (PerunException e) {
        log.error("Failed to read user preferred lang from application.", e);
      }
      if (appLang == null) {
        return DEFAULT_LANG;
      }
      return appLang;
    }
    try {
      Attribute langAttr = perun.getAttributesManagerBl().getAttribute(sess, user, A_USER_PREFERRED_LANGUAGE);
      String attrValue = langAttr.valueAsString();

      if (attrValue != null && !attrValue.isBlank()) {
        return attrValue;
      }
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      log.error("Failed to read user preferred lang attribute.", e);
    }

    return DEFAULT_LANG;
  }

  /**
   * Returns preferred language filled in the application. If there is no item
   * with destination attribute 'urn:perun:user:attribute-def:def:preferredLanguage filled,
   * null is returned.
   *
   * @param application application, from which the preferred language should be returned
   * @return value filled by user, for application item mapping to preferredLanguage attribute,
   * or null if the value is empty of there is no such item
   * @throws PerunException if anything goes wrong
   */
  private String getPreferredLangFromApplication(Application application) throws PerunException {
    List<ApplicationFormItemData> appData = registrarManager.getApplicationDataById(sess, application.getId());
    for (ApplicationFormItemData data : appData) {
      String destAttr = data.getFormItem().getPerunDestinationAttribute();
      if (A_USER_PREFERRED_LANGUAGE.equals(destAttr) && data.getValue() != null && !data.getValue().isBlank()) {
        return data.getValue();
      }
    }
    return null;
  }

  /**
   * Selects all vos from database, in which could be some expired applications acceptable for auto rejection.
   *
   * @return list of vos
   * @throws VoNotExistsException if vo not exist (it shouldn't happen)
   */
  private List<Vo> getAllEligibleVos() throws VoNotExistsException {
    List<Integer> vosIds = searcherBl.getVosIdsForAppAutoRejection();
    List<Vo> vos = new ArrayList<>();
    for (int voId : vosIds) {
      vos.add(perun.getVosManagerBl().getVoById(sess, voId));
    }
    return vos;

  }

  /**
   * Selects all groups from database, in which could be some expired applications acceptable for auto rejection.
   *
   * @return list of groups
   * @throws GroupNotExistsException if group not exist (it shouldn't happen)
   */
  private List<Group> getAllEligibleGroups() throws GroupNotExistsException {
    List<Integer> groupsIds = searcherBl.getGroupsIdsForAppAutoRejection();
    List<Group> groups = new ArrayList<>();
    for (int groupId : groupsIds) {
      groups.add(perun.getGroupsManagerBl().getGroupById(sess, groupId));
    }
    return groups;

  }
}
