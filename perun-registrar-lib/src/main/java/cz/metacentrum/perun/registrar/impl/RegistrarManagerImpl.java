package cz.metacentrum.perun.registrar.impl;

import static cz.metacentrum.perun.core.api.AttributeAction.READ;
import static cz.metacentrum.perun.core.api.AttributeAction.WRITE;
import static cz.metacentrum.perun.core.api.GroupsManager.GROUPSYNCHROENABLED_ATTRNAME;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.AUTO_SUBMIT_BUTTON;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.CHECKBOX;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.HEADING;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.HTML_COMMENT;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.PASSWORD;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.SUBMIT_BUTTON;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.USERNAME;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.VALIDATED_EMAIL;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.ApplicationApproved;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.ApplicationCreated;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.ApplicationDeleted;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.ApplicationRejected;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.ApplicationVerified;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.FormItemAdded;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.FormItemDeleted;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.FormItemUpdated;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.FormItemsUpdated;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.FormUpdated;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.MemberCreatedForApprovedApp;
import cz.metacentrum.perun.audit.events.RegistrarManagerEvents.MembershipExtendedForMemberInApprovedApp;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicy;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleObject;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.EmbeddedGroupApplicationSubmissionError;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.FormItemNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupIsNotASubgroupException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAllowedToAutoRegistrationException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotEmbeddedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidHtmlInputException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MfaPrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.MfaRoleTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.MfaTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.MissingSubmitButtonException;
import cz.metacentrum.perun.core.api.exceptions.MultipleApplicationFormItemsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.OpenApplicationExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.HTMLParser;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_group_attribute_def_def_htmlMailFooter;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_vo_attribute_def_def_htmlMailFooter;
import cz.metacentrum.perun.registrar.ConsolidatorManager;
import cz.metacentrum.perun.registrar.MailManager;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.blImpl.InvitationsManagerBlImpl;
import cz.metacentrum.perun.registrar.exceptions.AlreadyProcessingException;
import cz.metacentrum.perun.registrar.exceptions.AlreadyRegisteredException;
import cz.metacentrum.perun.registrar.exceptions.ApplicationNotCreatedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.exceptions.DuplicateRegistrationAttemptException;
import cz.metacentrum.perun.registrar.exceptions.FormItemSetupException;
import cz.metacentrum.perun.registrar.exceptions.FormNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.MissingRequiredDataCertException;
import cz.metacentrum.perun.registrar.exceptions.MissingRequiredDataException;
import cz.metacentrum.perun.registrar.exceptions.NoPrefilledUneditableRequiredDataException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Application.AppState;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem.ItemTexts;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;
import cz.metacentrum.perun.registrar.model.ApplicationOperationResult;
import cz.metacentrum.perun.registrar.model.ApplicationsPageQuery;
import cz.metacentrum.perun.registrar.model.Identity;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.RichApplication;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Implementation of RegistrarManager. Provides methods for:
 * <ul>
 * <li>preparing application form by a VO administrator</li>
 * <li>getting an application form by a user</li>
 * <li>submitting an application by a user</li>
 * <li>listing applications by a VO administrator</li>
 * </ul>
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RegistrarManagerImpl implements RegistrarManager {

  static final String URN_USER_LAST_NAME = "urn:perun:user:attribute-def:core:lastName";
  static final String URN_USER_DISPLAY_NAME = "urn:perun:user:attribute-def:core:displayName";
  // FIXME - we are retrieving GROUP name using only "short_name" so it's not same as getGroupById()
  static final String APP_SELECT =
      "select a.id as id, a.vo_id as vo_id, a.group_id as group_id,a.apptype as apptype,a.fed_info as fed_info,a" +
      ".state as state," +
      "a.user_id as user_id, a.auto_approve_error as auto_approve_error, a.extsourcename as extsourcename, a" +
      ".extsourcetype as extsourcetype, a.extsourceloa as extsourceloa, a.user_id as user_id, a.created_at as " +
      "app_created_at, a.created_by as app_created_by, a.modified_at as app_modified_at, a.modified_by as " +
      "app_modified_by, " +
      "v.name as vo_name, v.short_name as vo_short_name, v.created_by as vo_created_by, v.created_at as " +
      "vo_created_at, v.created_by_uid as vo_created_by_uid, v.modified_by as vo_modified_by, " +
      "v.modified_at as vo_modified_at, v.modified_by_uid as vo_modified_by_uid, g.name as group_name, g.dsc as " +
      "group_description, g.created_by as group_created_by, g.created_at as group_created_at, g.modified_by as " +
      "group_modified_by, g.created_by_uid as group_created_by_uid, g.modified_by_uid as group_modified_by_uid," +
      "g.modified_at as group_modified_at, g.vo_id as group_vo_id, g.parent_group_id as group_parent_group_id, g" +
      ".uu_id as group_uu_id, u.first_name as user_first_name, u.last_name as user_last_name, u.middle_name as " +
      "user_middle_name, " +
      "u.title_before as user_title_before, u.title_after as user_title_after, u.service_acc as user_service_acc, u" +
      ".sponsored_acc as user_sponsored_acc , u.uu_id as user_uu_id from application a left outer join vos v on a" +
      ".vo_id = v.id left outer join groups g on a.group_id = g.id left outer join users u on a.user_id = u.id";
  static final String APP_SELECT_PAGE =
      "select a.id as id, a.vo_id as vo_id, a.group_id as group_id, a.apptype as apptype, a.fed_info as fed_info,a" +
      ".state as state," +
      "a.auto_approve_error as auto_approve_error, a.user_id as user_id,a.extsourcename as extsourcename, a" +
      ".extsourcetype as extsourcetype, a.extsourceloa as extsourceloa, a.user_id as user_id, a.created_at as " +
      "app_created_at, a.created_by as app_created_by, a.modified_at as app_modified_at, a.modified_by as " +
         "app_modified_by, " +
      "v.name as vo_name, v.short_name as vo_short_name, v.created_by as vo_created_by, v.created_at as " +
      "vo_created_at, v.created_by_uid as vo_created_by_uid, v.modified_by as vo_modified_by, " +
      "v.modified_at as vo_modified_at, v.modified_by_uid as vo_modified_by_uid, g.name as group_name, g.dsc as " +
       "group_description, g.created_by as group_created_by, g.created_at as group_created_at, g.modified_by as " +
      "group_modified_by, g.created_by_uid as group_created_by_uid, g.modified_by_uid as group_modified_by_uid," +
      "g.modified_at as group_modified_at, g.vo_id as group_vo_id, g.parent_group_id as group_parent_group_id, g" +
      ".uu_id as group_uu_id, u.first_name as user_first_name, u.last_name as user_last_name, u.middle_name as " +
        "user_middle_name, " +
      "u.title_before as user_title_before, u.title_after as user_title_after, u.service_acc as user_service_acc, u" +
      ".sponsored_acc as user_sponsored_acc , u.uu_id as user_uu_id, count(*) OVER() AS total_count from application" +
      " a" +
      " left outer join vos v on a.vo_id = v.id left outer join groups g on a.group_id = g.id left outer join users u" +
       " on a.user_id = u.id left outer join application_data d on a.id = d.app_id";
  static final String APP_PAGE_GROUP_BY =
      " GROUP BY a.id, a.vo_id, a.group_id, a.apptype, a.fed_info, a.state, a.user_id, a.extsourcename, a" +
       ".extsourcetype, a.extsourceloa, a.user_id, a.created_at, a.created_by, a.modified_at, a.modified_by," +
      " v.name, v.short_name, v.created_by, v.created_at, v.created_by_uid, v.modified_by, v.modified_at, v" +
       ".modified_by_uid, g.name, g.dsc, g.created_by, g.created_at, g.modified_by, g.created_by_uid, g" +
        ".modified_by_uid, g.modified_at, g.vo_id, " +
      "g.parent_group_id, g.uu_id, u.first_name, u.last_name, u.middle_name, u.title_before, u.title_after, u" +
      ".service_acc, u.sponsored_acc, u.uu_id";
  static final RowMapper<Application> APP_MAPPER = (resultSet, i) -> {

    Application app = new Application(resultSet.getInt("id"),
        new Vo(resultSet.getInt("vo_id"), resultSet.getString("vo_name"), resultSet.getString("vo_short_name"),
            resultSet.getString("vo_created_at"), resultSet.getString("vo_created_by"),
            resultSet.getString("vo_modified_at"), resultSet.getString("vo_modified_by"),
            resultSet.getInt("vo_created_by_uid"), resultSet.getInt("vo_modified_by_uid")), null,
        AppType.valueOf(resultSet.getString("apptype")), resultSet.getString("fed_info"),
        AppState.valueOf(resultSet.getString("state")), resultSet.getString("extsourcename"),
        resultSet.getString("extsourcetype"), resultSet.getInt("extsourceloa"), null);

    // if group present
    if (resultSet.getInt("group_id") != 0) {
      app.setGroup(new Group(resultSet.getInt("group_id"), resultSet.getString("group_name"),
          resultSet.getString("group_description"), resultSet.getString("group_created_at"),
          resultSet.getString("group_created_by"), resultSet.getString("group_modified_at"),
          resultSet.getString("group_modified_by"), resultSet.getInt("group_created_by_uid"),
          resultSet.getInt("group_modified_by_uid")));
      app.getGroup().setVoId(resultSet.getInt("vo_id"));
      app.getGroup().setUuid(resultSet.getObject("group_uu_id", UUID.class));

      if (resultSet.getInt("group_parent_group_id") != 0) {
        app.getGroup().setParentGroupId(resultSet.getInt("group_parent_group_id"));
      }

    }

    // if user present
    if (resultSet.getInt("user_id") != 0) {
      app.setUser(new User(resultSet.getInt("user_id"), resultSet.getString("user_first_name"),
          resultSet.getString("user_last_name"), resultSet.getString("user_middle_name"),
          resultSet.getString("user_title_before"), resultSet.getString("user_title_after"),
          resultSet.getBoolean("user_service_acc"), resultSet.getBoolean("user_sponsored_acc")));
      app.getUser().setUuid(resultSet.getObject("user_uu_id", UUID.class));
    }

    app.setCreatedAt(resultSet.getString("app_created_at"));
    app.setCreatedBy(resultSet.getString("app_created_by"));
    app.setModifiedAt(resultSet.getString("app_modified_at"));
    app.setModifiedBy(resultSet.getString("app_modified_by"));
    app.setAutoApproveError(resultSet.getString("auto_approve_error"));

    return app;

  };
  private static final Logger LOG = LoggerFactory.getLogger(RegistrarManagerImpl.class);
  private static final Set<String> EXT_SOURCES_MULTIPLE_IDENTIFIERS =
      BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();
  private static final boolean FIND_SIMILAR_USERS_DISABLED = BeansUtils.getCoreConfig().isFindSimilarUsersDisabled();
  // identifiers for selected attributes
  private static final String URN_USER_TITLE_BEFORE = "urn:perun:user:attribute-def:core:titleBefore";
  private static final String URN_USER_TITLE_AFTER = "urn:perun:user:attribute-def:core:titleAfter";
  private static final String URN_USER_FIRST_NAME = "urn:perun:user:attribute-def:core:firstName";
  private static final String URN_USER_MIDDLE_NAME = "urn:perun:user:attribute-def:core:middleName";
  private static final String DISPLAY_NAME_VO_FROM_EMAIL = "\"From\" email address";
  private static final String FRIENDLY_NAME_VO_FROM_EMAIL = "fromEmail";
  private static final String NAMESPACE_VO_FROM_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_FROM_EMAIL = NAMESPACE_VO_FROM_EMAIL + ":" + FRIENDLY_NAME_VO_FROM_EMAIL;
  private static final String DISPLAY_NAME_VO_TO_EMAIL = "\"To\" email addresses";
  private static final String FRIENDLY_NAME_VO_TO_EMAIL = "toEmail";
  private static final String NAMESPACE_VO_TO_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_TO_EMAIL = NAMESPACE_VO_TO_EMAIL + ":" + FRIENDLY_NAME_VO_TO_EMAIL;
  private static final String DISPLAY_NAME_GROUP_TO_EMAIL = "\"To\" email addresses";
  private static final String FRIENDLY_NAME_GROUP_TO_EMAIL = "toEmail";
  private static final String NAMESPACE_GROUP_TO_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_TO_EMAIL = NAMESPACE_GROUP_TO_EMAIL + ":" + FRIENDLY_NAME_GROUP_TO_EMAIL;
  private static final String DISPLAY_NAME_GROUP_FROM_EMAIL = "\"From\" email address";
  private static final String FRIENDLY_NAME_GROUP_FROM_EMAIL = "fromEmail";
  private static final String NAMESPACE_GROUP_FROM_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_FROM_EMAIL = NAMESPACE_GROUP_FROM_EMAIL + ":" + FRIENDLY_NAME_GROUP_FROM_EMAIL;
  private static final String DISPLAY_NAME_GROUP_FROM_NAME_EMAIL = "\"From\" name";
  private static final String FRIENDLY_NAME_GROUP_FROM_NAME_EMAIL = "fromNameEmail";
  static final String URN_GROUP_FROM_NAME_EMAIL =
      NAMESPACE_GROUP_FROM_EMAIL + ":" + FRIENDLY_NAME_GROUP_FROM_NAME_EMAIL;
  private static final String NAMESPACE_GROUP_FROM_NAME_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
  private static final String DISPLAY_NAME_VO_FROM_NAME_EMAIL = "\"From\" name";
  private static final String FRIENDLY_NAME_VO_FROM_NAME_EMAIL = "fromNameEmail";
  static final String URN_VO_FROM_NAME_EMAIL = NAMESPACE_VO_FROM_EMAIL + ":" + FRIENDLY_NAME_VO_FROM_NAME_EMAIL;
  private static final String NAMESPACE_VO_FROM_NAME_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
  private static final String DISPLAY_NAME_VO_LANGUAGE_EMAIL = "Notification default language";
  private static final String FRIENDLY_NAME_VO_LANGUAGE_EMAIL = "notificationsDefLang";
  private static final String NAMESPACE_VO_LANGUAGE_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_LANGUAGE_EMAIL = NAMESPACE_VO_LANGUAGE_EMAIL + ":" + FRIENDLY_NAME_VO_LANGUAGE_EMAIL;
  private static final String DISPLAY_NAME_GROUP_LANGUAGE_EMAIL = "Notification default language";
  private static final String FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL = "notificationsDefLang";
  private static final String NAMESPACE_GROUP_LANGUAGE_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_LANGUAGE_EMAIL =
      NAMESPACE_GROUP_LANGUAGE_EMAIL + ":" + FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL;
  private static final String DISPLAY_NAME_VO_APPLICATION_URL = "Application form URL";
  private static final String FRIENDLY_NAME_VO_APPLICATION_URL = "applicationURL";
  private static final String NAMESPACE_VO_APPLICATION_URL = AttributesManager.NS_VO_ATTR_DEF;
  private static final String URN_VO_APPLICATION_URL =
      NAMESPACE_VO_APPLICATION_URL + ":" + FRIENDLY_NAME_VO_APPLICATION_URL;
  private static final String DISPLAY_NAME_GROUP_APPLICATION_URL = "Application form URL";
  private static final String FRIENDLY_NAME_GROUP_APPLICATION_URL = "applicationURL";
  private static final String NAMESPACE_GROUP_APPLICATION_URL = AttributesManager.NS_GROUP_ATTR_DEF;
  private static final String URN_GROUP_APPLICATION_URL =
      NAMESPACE_GROUP_APPLICATION_URL + ":" + FRIENDLY_NAME_GROUP_APPLICATION_URL;
  private static final String DISPLAY_NAME_VO_REGISTRAR_URL = "Registrar URL";
  private static final String FRIENDLY_NAME_VO_REGISTRAR_URL = "registrarURL";
  private static final String NAMESPACE_VO_REGISTRAR_URL = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_REGISTRAR_URL = NAMESPACE_VO_REGISTRAR_URL + ":" + FRIENDLY_NAME_VO_REGISTRAR_URL;
  private static final String DISPLAY_NAME_GROUP_REGISTRAR_URL = "Registrar URL";
  private static final String FRIENDLY_NAME_GROUP_REGISTRAR_URL = "registrarURL";
  private static final String NAMESPACE_GROUP_REGISTRAR_URL = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_REGISTRAR_URL = NAMESPACE_GROUP_REGISTRAR_URL + ":" + FRIENDLY_NAME_GROUP_REGISTRAR_URL;
  private static final String DISPLAY_NAME_VO_MAIL_FOOTER = "Mail Footer";
  private static final String FRIENDLY_NAME_VO_MAIL_FOOTER = "mailFooter";
  private static final String NAMESPACE_VO_MAIL_FOOTER = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_MAIL_FOOTER = NAMESPACE_VO_MAIL_FOOTER + ":" + FRIENDLY_NAME_VO_MAIL_FOOTER;
  private static final String FRIENDLY_NAME_VO_HTML_MAIL_FOOTER = "htmlMailFooter";
  private static final String NAMESPACE_VO_HTML_MAIL_FOOTER = AttributesManager.NS_VO_ATTR_DEF;
  static final String URN_VO_HTML_MAIL_FOOTER = NAMESPACE_VO_HTML_MAIL_FOOTER + ":" + FRIENDLY_NAME_VO_HTML_MAIL_FOOTER;
  private static final String DISPLAY_NAME_GROUP_MAIL_FOOTER = "Mail Footer";
  private static final String FRIENDLY_NAME_GROUP_MAIL_FOOTER = "mailFooter";
  private static final String NAMESPACE_GROUP_MAIL_FOOTER = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_MAIL_FOOTER = NAMESPACE_GROUP_MAIL_FOOTER + ":" + FRIENDLY_NAME_GROUP_MAIL_FOOTER;
  private static final String FRIENDLY_NAME_GROUP_HTML_MAIL_FOOTER = "htmlMailFooter";
  private static final String NAMESPACE_GROUP_HTML_MAIL_FOOTER = AttributesManager.NS_GROUP_ATTR_DEF;
  static final String URN_GROUP_HTML_MAIL_FOOTER =
      NAMESPACE_GROUP_HTML_MAIL_FOOTER + ":" + FRIENDLY_NAME_GROUP_HTML_MAIL_FOOTER;
  private static final String DEFAULT_GROUP_MSG_VO =
      "Your application to group {groupName} was automatically rejected," +
      " because the application for organization {voName} was rejected.";
  private static final String MODULE_PACKAGE_PATH = "cz.metacentrum.perun.registrar.modules.";
  // federation attribute name constants
  private static final String SHIB_DISPLAY_NAME_VAR = "displayName";
  private static final String SHIB_COMMON_NAME_VAR = "cn";
  private static final String SHIB_FIRST_NAME_VAR = "givenName";
  private static final String SHIB_LAST_NAME_VAR = "sn";
  // regular expression to match alfanumeric contents
  private static final Pattern ALNUM_PATTERN = Pattern.compile(".*\\p{Alnum}+.*", Pattern.UNICODE_CHARACTER_CLASS);
  private static final String APP_TYPE_SELECT = "select apptype from application_form_item_apptypes";
  private static final String FORM_SELECT =
      "select id,vo_id,group_id,automatic_approval,automatic_approval_extension,automatic_approval_embedded," +
      "module_names from application_form";
  private static final String FORM_ITEM_SELECT =
      "select id,ordnum,shortname,required,type,fed_attr,src_attr,dst_attr,regex,hidden,disabled," +
       "hidden_dependency_item_id,disabled_dependency_item_id,updatable from application_form_items";
  private static final String FORM_ITEM_TEXTS_SELECT =
      "select locale,label,options,help,error_message from application_form_item_texts";
  private static final RowMapper<Application> IDENTITY_APP_MAPPER = (resultSet, i) -> {
    Application app = new Application();
    app.setId(resultSet.getInt("id"));
    app.setUser(new User(resultSet.getInt("user_id"), "", "", "", "", ""));
    app.setCreatedBy(resultSet.getString("created_by"));
    app.setExtSourceName(resultSet.getString("extsourcename"));
    app.setFedInfo(resultSet.getString("fed_info"));
    return app;
  };
  private static final RowMapper<AppType> APP_TYPE_MAPPER = (resultSet, i) -> AppType.valueOf(resultSet.getString(1));
  private static final RowMapper<ApplicationFormItem> ITEM_MAPPER = (resultSet, i) -> {
    ApplicationFormItem app = new ApplicationFormItem(resultSet.getInt("id"), resultSet.getString("shortname"),
        resultSet.getBoolean("required"), Type.valueOf(resultSet.getString("type")), resultSet.getString("fed_attr"),
        resultSet.getString("src_attr"), resultSet.getString("dst_attr"), resultSet.getString("regex"));
    app.setOrdnum(resultSet.getInt("ordnum"));
    app.setHidden(ApplicationFormItem.Hidden.valueOf(resultSet.getString("hidden")));
    app.setDisabled(ApplicationFormItem.Disabled.valueOf(resultSet.getString("disabled")));
    app.setUpdatable(resultSet.getBoolean("updatable"));

    if (resultSet.getInt("hidden_dependency_item_id") != 0) {
      app.setHiddenDependencyItemId(resultSet.getInt("hidden_dependency_item_id"));
    }
    if (resultSet.getInt("disabled_dependency_item_id") != 0) {
      app.setDisabledDependencyItemId(resultSet.getInt("disabled_dependency_item_id"));
    }
    return app;
  };
  private static final RowMapper<ApplicationFormItem.ItemTexts> ITEM_TEXTS_MAPPER =
      (resultSet, i) -> new ItemTexts(new Locale(resultSet.getString("locale")), resultSet.getString("label"),
          resultSet.getString("options"), resultSet.getString("help"), resultSet.getString("error_message"));
  private static final Set<ApplicationFormItem.Type> NON_INPUT_FORM_ITEM_TYPES =
          Set.of(HTML_COMMENT, HEADING, SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);
  private static final Set<ApplicationFormItem.Type> SUBMIT_FORM_ITEM_TYPES = Set.of(SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);
  @Autowired
  PerunBl perun;
  @Autowired
  MailManager mailManager;
  @Autowired
  ConsolidatorManager consolidatorManager;
  @Autowired
  InvitationsManagerImpl invitationsManager;
  @Autowired
  InvitationsManagerBlImpl invitationsManagerBl;
  private final Set<UUID> processingInvitation = new HashSet<>();
  private final Set<String> runningCreateApplication = new HashSet<>();
  private final Set<Integer> runningApproveApplication = new HashSet<>();
  private final Set<Integer> runningRejectApplication = new HashSet<>();
  private final Set<Integer> runningDeleteApplication = new HashSet<>();
  private RegistrarManager registrarManager;
  private PerunSession registrarSession;
  private JdbcPerunTemplate jdbc;
  private NamedParameterJdbcTemplate namedJdbc;
  private AttributesManagerBl attrManager;
  private MembersManagerBl membersManager;
  private GroupsManagerBl groupsManager;
  private UsersManagerBl usersManager;
  private VosManagerBl vosManager;

  /**
   * Returns ResultSetExtractor that can be used to extract returned paginated applications from db.
   *
   * @param query query data
   * @return extractor, that can be used to extract returned paginated applications from db
   */
  private static ResultSetExtractor<Paginated<RichApplication>> getPaginatedApplicationsExtractor(
      ApplicationsPageQuery query) {
    return resultSet -> {
      List<RichApplication> applications = new ArrayList<>();
      int totalCount = 0;
      int row = 0;
      while (resultSet.next()) {
        totalCount = resultSet.getInt("total_count");
        Application app = APP_MAPPER.mapRow(resultSet, row);
        if (app != null) {
          applications.add(new RichApplication(app));
        }
        row++;
      }
      return new Paginated<>(applications, query.getOffset(), query.getPageSize(), totalCount);
    };
  }

  @Transactional
  @Override
  public ApplicationFormItem addFormItem(PerunSession user, ApplicationForm form, ApplicationFormItem item)
      throws PerunException {

    //Authorization
    if (form.getGroup() == null) {
      // VO application
      if (!AuthzResolver.authorizedInternal(user, "vo-addFormItem_ApplicationForm_ApplicationFormItem_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(user, "addFormItem");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(user, "group-addFormItem_ApplicationForm_ApplicationFormItem_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(user, "addFormItem");
      }
    }

    if (item.getType() == EMBEDDED_GROUP_APPLICATION &&
        countEmbeddedGroupFormItems(registrarManager.getFormItems(user, form)) == 1) {
      throw new MultipleApplicationFormItemsException(
          "Multiple definitions of embedded groups. Only one definition is allowed.");
    }

    // find the ordinal number of the next item
    int ordnum = 0;
    if (item.getOrdnum() == null || item.getOrdnum() < 0) {
      if (jdbc.queryForInt("select count(*) from application_form_items where form_id=?", form.getId()) > 0) {
        ordnum = jdbc.queryForInt("select max(ordnum)+1 from application_form_items where form_id=?", form.getId());
      }
    } else {
      // use predefined ordnum
      ordnum = item.getOrdnum();
    }

    int itemId = Utils.getNewId(jdbc, "APPLICATION_FORM_ITEMS_ID_SEQ");
    jdbc.update("insert into application_form_items(id,form_id,ordnum,shortname,required,type,fed_attr,src_attr," +
                "dst_attr,regex,updatable,hidden,disabled,hidden_dependency_item_id,disabled_dependency_item_id) " +
        "values (?,?,?,?,?,?,?,?,?,?,?,?::app_item_hidden,?::app_item_disabled,?,?)",
        itemId, form.getId(), ordnum, item.getShortname(), item.isRequired(), item.getType().name(),
        item.getFederationAttribute(), item.getPerunSourceAttribute(), item.getPerunDestinationAttribute(),
        item.getRegex(), item.isUpdatable(), item.getHidden().toString(), item.getDisabled().toString(),
        item.getHiddenDependencyItemId(), item.getDisabledDependencyItemId());

    // create texts
    for (Locale locale : item.getI18n().keySet()) {
      ItemTexts itemTexts = item.getTexts(locale);
      jdbc.update(
          "insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?," +
           "?,?)",
          itemId, locale.getLanguage(), itemTexts.getLabel(), itemTexts.getOptions(), itemTexts.getHelp(),
          itemTexts.getErrorMessage());
    }

    if (item.getApplicationTypes().contains(AppType.EMBEDDED)) {
      throw new InternalErrorException("It is not possible to add form items to EMBEDDED application type.");
    }
    for (AppType appType : item.getApplicationTypes()) {
      jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)", itemId,
          appType.toString());
    }

    // set new properties back to object & return
    item.setOrdnum(ordnum);
    item.setId(itemId);
    perun.getAuditer().log(user, new FormItemAdded(form));
    return item;

  }

  @Override
  public void addGroupsToAutoRegistration(PerunSession sess, List<Group> groups)
      throws GroupNotExistsException, PrivilegeException, GroupNotAllowedToAutoRegistrationException {
    Utils.checkPerunSession(sess);

    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
    }

    //Authorization
    for (Group group : groups) {
      if (!AuthzResolver.authorizedInternal(sess, "addGroupsToAutoRegistration_List<Group>_policy", group)) {
        throw new PrivilegeException(sess, "addGroupsToAutoRegistration");
      }
    }

    // Create application form if non exists
    for (Group group : groups) {
      try {
        getFormForGroup(group);
      } catch (FormNotExistsException e) {
        createApplicationFormInGroup(sess, group);
      }
    }

    perun.getGroupsManagerBl().addGroupsToAutoRegistration(sess, groups);
  }

  @Override
  public void addGroupsToAutoRegistration(PerunSession sess, List<Group> groups, ApplicationFormItem formItem)
      throws GroupNotExistsException, PrivilegeException, GroupNotAllowedToAutoRegistrationException,
      FormItemNotExistsException {
    Utils.checkPerunSession(sess);

    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
    }

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addGroupsToAutoRegistration_List<Group>_ApplicationFormItem_policy",
        new ArrayList<>(groups))) {
      throw new PrivilegeException(sess, "addGroupsToAutoRegistration");
    }

    // Create application form if non exists
    for (Group group : groups) {
      try {
        getFormForGroup(group);
      } catch (FormNotExistsException e) {
        createApplicationFormInGroup(sess, group);
      }
    }

    perun.getGroupsManagerBl().addGroupsToAutoRegistration(sess, groups, formItem);
  }

  @Override
  public void addGroupsToAutoRegistration(PerunSession sess, List<Group> groups, Group registrationGroup,
                                          ApplicationFormItem formItem)
      throws GroupNotExistsException, PrivilegeException, GroupNotAllowedToAutoRegistrationException,
      GroupIsNotASubgroupException, FormItemNotExistsException {
    Utils.checkPerunSession(sess);

    List<Group> subGroups = perun.getGroupsManagerBl().getAllSubGroups(sess, registrationGroup);

    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
      if (!subGroups.contains(group)) {
        throw new GroupIsNotASubgroupException();
      }
    }

    //Authorization
    List<PerunBean> allGroupsToCheck = new ArrayList<>(groups);
    allGroupsToCheck.add(registrationGroup);
    if (!AuthzResolver.authorizedInternal(sess,
        "addGroupsToAutoRegistration_List<Group>_Group_ApplicationFormItem_policy", allGroupsToCheck)) {
      throw new PrivilegeException(sess, "addGroupsToAutoRegistration");
    }

    // Create application form if non exists
    for (Group group : groups) {
      try {
        getFormForGroup(group);
      } catch (FormNotExistsException e) {
        createApplicationFormInGroup(sess, group);
      }
    }

    perun.getGroupsManagerBl().addGroupsToAutoRegistration(sess, groups, formItem);
  }

  @Override
  public Application approveApplication(PerunSession sess, int appId) throws PerunException {
    return approveApplication(sess, appId, sess.getPerunPrincipal().getActor());
  }

  private Application approveApplication(PerunSession sess, int appId, String approver)
      throws PerunException {
    synchronized (runningApproveApplication) {
      if (runningApproveApplication.contains(appId)) {
        throw new AlreadyProcessingException("Application " + appId + " approval is already processing.");
      } else {
        runningApproveApplication.add(appId);
      }
    }

    Application app;
    try {
      app = registrarManager.approveApplicationInternal(sess, appId, approver, true);
    } catch (AlreadyMemberException ex) {
      // case when user joined identity after sending initial application and former user was already member of VO
      throw new RegistrarException("User is already member (with ID: " + ex.getMember().getId() +
                                   ") of your VO/group. (user joined his identities after sending new application). " +
                                    "You can reject this application " +
                                   appId + " and re-validate old member to keep old data (e.g. login,email).", ex);
    } catch (MemberNotExistsException ex) {
      throw new RegistrarException("To approve application " + appId + " user must already be member of VO.", ex);
    } catch (NotGroupMemberException ex) {
      throw new RegistrarException("To approve application " + appId + " user must already be member of Group.", ex);
    } catch (UserNotExistsException | UserExtSourceNotExistsException | ExtSourceNotExistsException ex) {
      throw new RegistrarException("User specified by the data in application " + appId +
                                   " was not found. If you tried to approve application for the Group, try to check, " +
                  "if user already has approved application in the VO. Application to the VO must " +
                                     "be approved first.",
          ex);
    } finally {
      synchronized (runningApproveApplication) {
        runningApproveApplication.remove(appId);
      }
    }

    perun.getAuditer().log(sess, new ApplicationApproved(app));

    synchronized (runningApproveApplication) {
      runningApproveApplication.remove(appId);
    }

    return app;
  }

  /**
   * Process application approval in 1 transaction. If validateMember is true performs also an asynchronous member
   * validation after the transaction commits.
   *
   * @param sess  session for authz
   * @param appId application ID to approve
   * @param validateMember whether to perform also a member validation
   * @return updated application
   * @throws PerunException
   */
  @Transactional(rollbackFor = Exception.class)
  public Application approveApplicationInternal(PerunSession sess, int appId, String approver, boolean validateMember)
      throws PerunException {

    Application app = getApplicationById(appId);
    if (app == null) {
      throw new RegistrarException("Application with ID " + appId + " doesn't exists.");
    }
    Member member;

    //Authorization
    if (app.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-approveApplicationInternal_int_policy",
          Collections.singletonList(app.getVo()))) {
        throw new PrivilegeException(sess, "approveApplicationInternal");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-approveApplicationInternal_int_policy",
          Arrays.asList(app.getVo(), app.getGroup()))) {
        throw new PrivilegeException(sess, "approveApplicationInternal");
      }
    }

    // only VERIFIED applications can be approved
    if (!AppState.VERIFIED.equals(app.getState())) {
      if (AppState.APPROVED.equals(app.getState())) {
        throw new RegistrarException(
            "Application " + appId + " is already approved. Try to refresh the view to see changes.");
      }
      if (AppState.REJECTED.equals(app.getState())) {
        throw new RegistrarException(
            "Rejected application " + appId + " cant' be approved. Try to refresh the view to see changes.");
      }
      throw new RegistrarException("User didn't verify his email address yet. Please wait until application " + appId +
                                   " will be in a 'Submitted' state. You can send mail verification notification to " +
                                    "user again if you wish.");
    }

    LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
    PerunPrincipal applicationPrincipal =
        new PerunPrincipal(app.getCreatedBy(), app.getExtSourceName(), app.getExtSourceType(), app.getExtSourceLoa(),
            additionalAttributes);

    // get registrar module
    Set<RegistrarModule> modules;
    if (app.getGroup() != null) {
      modules = getRegistrarModules(getFormForGroup(app.getGroup()));
    } else {
      modules = getRegistrarModules(getFormForVo(app.getVo()));
    }
    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        module.beforeApprove(sess, app);
      }
    }

    // mark as APPROVED
    int result = jdbc.update("update application set state=?, modified_by=?, modified_at=?" +
                                 " where id=?", AppState.APPROVED.toString(), approver, new Date(), appId);
    if (result == 0) {
      throw new RegistrarException("Application with ID=" + appId + " not found.");
    } else if (result > 1) {
      throw new ConsistencyErrorException("More than one application is stored under ID=" + appId + ".");
    }
    // set back as approved
    app.setState(AppState.APPROVED);
    LOG.info("Application {} marked as APPROVED", appId);

    // Try to get reservedLogin and reservedNamespace before deletion, it will be used for creating userExtSources
    List<Pair<String, String>> logins = usersManager.getReservedLoginsByApp(sess, app.getId());

    // FOR INITIAL / EMBEDDED APPLICATION
    if (AppType.INITIAL.equals(app.getType()) || AppType.EMBEDDED.equals(app.getType())) {

      if (app.getGroup() != null) {
        // group application

        // free reserved logins so they can be set as attributes
        for (Pair<String, String> login : logins) {
          jdbc.update("delete from application_reserved_logins where namespace=? and login=?", login.getLeft(),
              login.getRight());
        }

        if (app.getUser() == null) {

          // application for group doesn't have user set, but it can exists in perun (joined identities after
          // submission)
          User u = perun.getUsersManagerBl().getUserByExtSourceInformation(registrarSession, applicationPrincipal);

          // put user back to application
          app.setUser(u);
          // store user_id in DB
          int result2 = jdbc.update("update application set user_id=? where id=?", u.getId(), appId);
          if (result2 == 0) {
            throw new RegistrarException("Application with ID=" + appId + " not found.");
          } else if (result2 > 1) {
            throw new ConsistencyErrorException("More than one application is stored under ID=" + appId + ".");
          }

        }

        // add new member of VO as member of group (for group applications)
        // !! MUST BE MEMBER OF VO !!
        member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

        // MEMBER must be in a VALID or INVALID state since approval starts validation !!
        // and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
        // meaning, user should submit membership extension application first !!
        if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
          throw new CantBeApprovedException(
              "Application " + appId + " of member with membership status: " + member.getStatus() +
              " can't be approved. Please wait until member extends/re-validate own membership in a VO.");
        }

        // store all attributes (but not logins)
        storeApplicationAttributes(app);

        // cancel reservation of new duplicate logins and get purely new logins back
        logins = unreserveNewLoginsFromSameNamespace(logins, app.getUser());

        // store purely new logins to user
        storeApplicationLoginAttributes(app);

        for (Pair<String, String> pair : logins) {
          // LOGIN IN NAMESPACE IS PURELY NEW => VALIDATE ENTRY IN KDC
          // left = namespace, right = login
          usersManager.validatePassword(registrarSession, app.getUser(), pair.getLeft());
        }

        // update titles before/after users name if part of application !! USER MUST EXISTS !!
        updateUserNameTitles(app);

        // Perform checks since we moved from entry to BL
        // Check if the group is externally synchronized
        Attribute attrSynchronizeEnabled = attrManager.getAttribute(sess, app.getGroup(), GROUPSYNCHROENABLED_ATTRNAME);
        if ("true".equals(attrSynchronizeEnabled.getValue()) ||
            groupsManager.isGroupInStructureSynchronizationTree(sess, app.getGroup())) {
          throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
        }

        groupsManager.addMember(registrarSession, app.getGroup(), member);

        LOG.debug("[REGISTRAR] Member {} added to Group {}.", member, app.getGroup());

      } else {

        // VO application

        if (AppType.EMBEDDED.equals(app.getType())) {
          throw new InternalErrorException("Only group application can have EMBEDDED type.");
        }

        // free reserved logins so they can be set as attributes
        for (Pair<String, String> login : logins) {
          jdbc.update("delete from application_reserved_logins where namespace=? and login=?", login.getLeft(),
              login.getRight());
        }

        User u;
        if (app.getUser() != null) {
          u = app.getUser();
          LOG.debug("[REGISTRAR] Trying to make member from user {}", u);
          member = membersManager.createMember(sess, app.getVo(), u);
          // store all attributes (but not logins)
          storeApplicationAttributes(app);
          // if user was already known to perun, createMember() will set attributes
          // via setAttributes() method so core attributes are skipped
          // ==> updateNameTitles() in case of change in appForm.
          updateUserNameTitles(app);
        } else {
          try {
            u = perun.getUsersManagerBl().getUserByExtSourceInformation(registrarSession, applicationPrincipal);
            LOG.debug("[REGISTRAR] Trying to make member from user {}", u);
            member = membersManager.createMember(sess, app.getVo(), u);
            // set NEW user id back to application
            app.setUser(u);
            // store all attributes (but not logins)
            storeApplicationAttributes(app);
            // if user was already known to perun, createMember() will set attributes
            // via setAttributes() method so core attributes are skipped
            // ==> updateNameTitles() in case of change in appForm.
            updateUserNameTitles(app);
          } catch (UserExtSourceNotExistsException | UserNotExistsException | ExtSourceNotExistsException ex) {
            Candidate candidate = createCandidateFromApplicationData(app);
            // create member and user
            LOG.debug("[REGISTRAR] Trying to make member from candidate {}", candidate);

            // added duplicit check, since we switched from entry to bl call of createMember()
            Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
            Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

            member = membersManager.createMember(sess, app.getVo(), app.getExtSourceName(), app.getExtSourceType(),
                app.getExtSourceLoa(), app.getCreatedBy(), candidate);
            u = usersManager.getUserById(registrarSession, member.getUserId());
            // set NEW user id back to application
            app.setUser(u);
          }
          // user originally not known -> set UserExtSource attributes from source identity for new User and UES
          ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, app.getExtSourceName());
          UserExtSource ues = usersManager.getUserExtSourceByExtLogin(sess, es, app.getCreatedBy());
          // we have historical data in "fedInfo" item, hence we must safely ignore any parsing errors.
          try {
            ((PerunBlImpl) perun).setUserExtSourceAttributes(sess, ues, additionalAttributes);
          } catch (Exception ex) {
            LOG.error("Unable to store UES attributes from application ID: {}, attributes: {}, with exception: {}",
                appId, app.getFedInfo(), ex);
          }
        }

        result = jdbc.update("update application set user_id=? where id=?", member.getUserId(), appId);
        if (result == 0) {
          throw new RegistrarException("User ID hasn't been associated with the application " + appId +
                                       ", because the application was not found!");
        } else if (result > 1) {
          throw new ConsistencyErrorException("User ID hasn't been associated with the application " + appId +
                                              ", because more than one application exists under the same ID.");
        }
        LOG.info("Member {} created for: {} / {}", member.getId(), app.getCreatedBy(), app.getExtSourceName());

        // unreserve new login if user already have login in same namespace
        // also get back purely new logins
        logins = unreserveNewLoginsFromSameNamespace(logins, u);

        // store purely new logins to user
        storeApplicationLoginAttributes(app);

        for (Pair<String, String> pair : logins) {
          // LOGIN IN NAMESPACE IS PURELY NEW => VALIDATE ENTRY IN KDC
          // left = namespace, right = login
          usersManager.validatePassword(registrarSession, u, pair.getLeft());
        }

        // log
        perun.getAuditer().log(sess, new MemberCreatedForApprovedApp(member, app));

      }

      // FOR EXTENSION APPLICATION
    } else if (AppType.EXTENSION.equals(app.getType())) {

      // free reserved logins so they can be set as attributes
      for (Pair<String, String> login : logins) {
        jdbc.update("delete from application_reserved_logins where namespace=? and login=?", login.getLeft(),
            login.getRight());
      }

      member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

      if (app.getGroup() != null) {

        // MEMBER must be in a VALID or INVALID state since approval starts validation !!
        // and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
        // meaning, user should submit membership extension application first !!
        if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
          throw new CantBeApprovedException(
              "Application " + appId + " of member with membership status: " + member.getStatus() +
              " can't be approved. Please wait until member extends/re-validate own membership in a VO.");
        }

        // overwrite member with group context
        member = groupsManager.getGroupMemberById(registrarSession, app.getGroup(), member.getId());

      }

      storeApplicationAttributes(app);

      if (app.getGroup() != null) {
        // extends users Group membership
        groupsManager.extendMembershipInGroup(sess, member, app.getGroup());
      } else {
        // extend users VO membership
        // if VO is hierarchical, add it to MemberOrganizations
        if (perun.getVosManagerBl().getMemberVos(sess, app.getVo().getId()).size() > 0) {
          perun.getMembersManagerBl().updateOrganizationsAttributes(sess, app.getVo(), member);
        }
        membersManager.extendMembership(registrarSession, member);
      }

      // unreserve new logins, if user already have login in same namespace
      // also get back logins, which are purely new
      logins = unreserveNewLoginsFromSameNamespace(logins, app.getUser());

      // store purely new logins from application
      storeApplicationLoginAttributes(app);

      // validate purely new logins in KDC
      for (Pair<String, String> pair : logins) {
        // left = namespace, right = login
        usersManager.validatePassword(registrarSession, app.getUser(), pair.getLeft());
      }

      // update titles before/after users name if part of application !! USER MUST EXISTS !!
      updateUserNameTitles(app);

      // log
      perun.getAuditer().log(sess, new MembershipExtendedForMemberInApprovedApp(member, app, app.getVo()));

    }

    // CONTINUE FOR BOTH APP TYPES
    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        module.approveApplication(sess, app);
      }
    }


    // for application with any embedded groups, submit applications also to these embedded groups
    List<Group> embeddedGroups = getEmbeddedGroups(sess, appId);
    if (!embeddedGroups.isEmpty()) {
      submitEmbeddedGroupApplications(sess, embeddedGroups, app);
    }

    getMailManager().sendMessage(app, MailType.APP_APPROVED_USER, null, null);

    Member newMember = membersManager.getMemberByUser(sess, app.getVo(), app.getUser());
    // register afterCommit action for validating a member
    if (validateMember) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          // logic extracted to a separate method to clear the still existing commited transaction just to be safe
          registrarManager.approveApplicationAfterCommitValidation(sess, newMember, app);
        }
      });
    }

    // return updated application
    return app;

  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void approveApplicationAfterCommitValidation(PerunSession sess, Member member, Application app) {
    try {
      // validate member async when all changes are committed
      // we can't use existing core method, since we want to approve auto-approval waiting group applications
      // once member is validated
      new Thread(() -> {
        try {
          membersManager.validateMember(registrarSession, member);
        } catch (InternalErrorException | WrongAttributeValueException |
                 WrongReferenceAttributeValueException e) {
          LOG.error("[REGISTRAR] Exception when validating {} after approving application {}.", member, app);
        }

        try {
          // get user's group apps with auto-approve and approve them and set them user id
          handleUsersGroupApplications(sess, app.getVo(), app.getUser());
        } catch (PerunException ex) {
          LOG.error(
              "[REGISTRAR] Exception when auto-approving waiting group applications for {} after approving " +
                  "application {}.",
              member, app);
        }

      }).start();

    } catch (Exception ex) {
      // we skip any exception thrown from here
      LOG.error("[REGISTRAR] Exception when validating {} after approving application {}.", member, app);
    }
  }

  @Override
  public List<ApplicationOperationResult> approveApplications(PerunSession sess, List<Integer> applicationIds)
      throws PerunException {
    checkMFAForApplications(sess, applicationIds, "approveApplicationInternal_int_policy");

    Collections.sort(applicationIds);
    List<ApplicationOperationResult> approveApplicationsResult = new ArrayList<>();
    for (Integer id : applicationIds) {
      try {
        registrarManager.canBeApproved(sess, registrarManager.getApplicationById(sess, id));
        registrarManager.approveApplication(sess, id);
        approveApplicationsResult.add(new ApplicationOperationResult(id, null));
      } catch (Exception e) {
        approveApplicationsResult.add(new ApplicationOperationResult(id, e));
      }
    }
    return approveApplicationsResult;
  }

  /**
   * Try to approve group applications if automatic approval is enabled.
   *
   * @param sess        perun session
   * @param application which we try to auto approve
   * @throws PerunException
   */
  private void autoApproveGroupApplication(PerunSession sess, Application application) throws PerunException {
    // if new => skipp user will approve automatically by verifying email
    if (application.getState().equals(AppState.NEW)) {
      return;
    }

    // check whether the application is tied to a pre-approved invitation
    Invitation invitation = null;
    try {
      invitation = invitationsManager.getInvitationByApplication(sess, application);
    } catch (InvitationNotExistsException ignored) {
      // this just means that no invitation is tied to this application
    }

    // approve applications only for auto-approve forms
    if (!forceAutoApprove(sess, application) && invitation == null) {
      if (!getFormForGroup(application.getGroup()).isAutomaticApproval() &&
              AppType.INITIAL.equals(application.getType())) {
        return;
      }
      if (!getFormForGroup(application.getGroup()).isAutomaticApprovalExtension() &&
              AppType.EXTENSION.equals(application.getType())) {
        return;
      }
      if (!getFormForGroup(application.getGroup()).isAutomaticApprovalEmbedded() &&
              AppType.EMBEDDED.equals(application.getType())) {
        return;
      }
    }

    String approver = invitation != null ? invitation.getCreatedBy() : sess.getPerunPrincipal().getActor();
    Application processedApplication;
    try {
      processedApplication = registrarManager.approveApplicationInternal(sess, application.getId(), approver, false);
    } catch (RegistrarException ex) {
      // case when user have UNVERIFIED group application
      // will be approved when user verify his email
      LOG.error("[REGISTRAR] Can't auto-approve group application after vo app approval because of exception.", ex);
    }
  }

  @Override
  public void canBeApproved(PerunSession session, Application application) throws PerunException {

    //Authorization
    if (application.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(session, "vo-canBeApproved_Application_policy",
          Collections.singletonList(application.getVo()))) {
        throw new PrivilegeException(session, "canBeApproved");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(session, "group-canBeApproved_Application_policy",
          Arrays.asList(application.getVo(), application.getGroup()))) {
        throw new PrivilegeException(session, "canBeApproved");
      }
    }

    // get registrar module
    Set<RegistrarModule> modules;
    if (application.getGroup() != null) {
      modules = getRegistrarModules(getFormForGroup(application.getGroup()));
    } else {
      modules = getRegistrarModules(getFormForVo(application.getVo()));
    }
    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        module.canBeApproved(session, application);
      }
    }

    // generally for Group applications:

    // submitter, must be MEMBER of VO and in VALID or INVALID state since approval starts validation !!
    // and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
    // meaning, user should submit membership extension application first !!
    if (application.getGroup() != null) {
      try {
        User u = application.getUser();
        if (u == null) {
          LinkedHashMap<String, String> additionalAttributes =
              BeansUtils.stringToMapOfAttributes(application.getFedInfo());
          PerunPrincipal applicationPrincipal =
              new PerunPrincipal(application.getCreatedBy(), application.getExtSourceName(),
                  application.getExtSourceType(), application.getExtSourceLoa(), additionalAttributes);
          u = perun.getUsersManagerBl().getUserByExtSourceInformation(registrarSession, applicationPrincipal);
        }
        Member member = membersManager.getMemberByUser(registrarSession, application.getVo(), u);
        if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
          throw new CantBeApprovedException("Application of member with membership status: " + member.getStatus() +
                                            " can't be approved. Please wait until member extends/re-validate own " +
                                             "membership in a VO.");
        }
      } catch (MemberNotExistsException | UserNotExistsException | ExtSourceNotExistsException |
               UserExtSourceNotExistsException ex) {
        throw new RegistrarException("To approve application user must be a member of VO.", ex);
      }

    }

  }

  @Override
  public void checkCheckboxHtml(PerunSession sess, String html) throws InvalidHtmlInputException {
    Utils.checkPerunSession(sess);

    boolean isSafe = new HTMLParser().isCheckboxLabelSafe(html);
    if (!isSafe) {
      throw new InvalidHtmlInputException(
          "Input contains unsafe elements. Remove them and try again. Only <a> elements with 'href' and 'target' " +
"attributes are allowed");
    }
  }

  /**
   * Check if user can submit application for specified form and type. Performs check on VO/Group membership, VO/Group
   * expiration rules, form modules and duplicate (already submitted) applications.
   *
   * @param sess    PerunSession for authz
   * @param appType Type of application form
   * @param form    Application form
   */
  private void checkDuplicateRegistrationAttempt(PerunSession sess, AppType appType, ApplicationForm form)
      throws DuplicateRegistrationAttemptException, AlreadyRegisteredException, PrivilegeException,
      ExtendMembershipException, RegistrarException, MemberNotExistsException, CantBeSubmittedException,
      NotGroupMemberException {

    Vo vo = form.getVo();
    Group group = form.getGroup();

    // get necessary params from session
    User user = sess.getPerunPrincipal().getUser();
    int extSourceLoa = sess.getPerunPrincipal().getExtSourceLoa();

    if (AppType.INITIAL.equals(appType) || AppType.EMBEDDED.equals(appType)) {
      if (AppType.EMBEDDED.equals(appType) && group == null) {
        throw new InternalErrorException("Only group application can have EMBEDDED type.");
      }
      if (user != null) {
        //user is known
        try {
          Member m = membersManager.getMemberByUser(registrarSession, vo, user);
          if (group != null) {
            // get members groups
            List<Group> g = groupsManager.getMemberGroups(registrarSession, m);
            if (g.contains(group)) {
              // user is member of group - can't post more initial applications
              throw new AlreadyRegisteredException("You are already member of group " + group.getName() + ".");
            } else {
              checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
              checkDupplicateGroupApplications(sess, vo, group, AppType.EMBEDDED);
              // pass if have approved or rejected app
            }
          } else {
            // user is member of vo, can't post more initial applications
            throw new AlreadyRegisteredException("You are already member of VO: " + vo.getName());
          }
        } catch (MemberNotExistsException ex) {
          // user is not member of vo
          if (group != null) {
            checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
            checkDupplicateGroupApplications(sess, vo, group, AppType.EMBEDDED);
            //throw new InternalErrorException("You must be member of vo: "+vo.getName()+" to apply for membership in
            // group: "+group.getName());
          } else {
            checkDupplicateVoApplications(sess, vo, AppType.INITIAL);
            // pass not member and have only approved or rejected apps
          }
        }
      } else {
        // user is not known
        if (group != null) {
          checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
          checkDupplicateGroupApplications(sess, vo, group, AppType.EMBEDDED);
          //throw new InternalErrorException("You must be member of vo: "+vo.getName()+" to apply for membership in
          // group: "+group.getName());
        } else {
          checkDupplicateVoApplications(sess, vo, AppType.INITIAL);
          // pass not member and have only approved or rejected apps
        }
      }
      // check for embedded applications was already done in original app, this would always fail because of
      // registrar session
      if (!AppType.EMBEDDED.equals(appType)) {
        // if false, throws exception with reason for GUI
        membersManager.canBeMemberWithReason(sess, vo, user, String.valueOf(extSourceLoa));
      }
    }
    // if extension, user != null !!
    if (AppType.EXTENSION.equals(appType)) {
      if (user == null) {
        throw new RegistrarException(
            "Trying to get extension application for non-existing user. Try to log-in with different identity.");
      }
      // check for submitted registrations
      Member member = membersManager.getMemberByUser(sess, vo, user);
      if (group != null) {
        member = groupsManager.getGroupMemberById(registrarSession, group, member.getId());
        checkDupplicateGroupApplications(sess, vo, group, AppType.EXTENSION);
        // if false, throws exception with reason for GUI
        groupsManager.canExtendMembershipInGroupWithReason(sess, member, group);
        // vo sponsored members can extend in a group
      } else {
        checkDupplicateVoApplications(sess, vo, AppType.EXTENSION);
        // if false, throws exception with reason for GUI
        membersManager.canExtendMembershipWithReason(sess, member);
        // sponsored vo members cannot be extended in this way
        if (member.isSponsored()) {
          throw new CantBeSubmittedException(
              "Sponsored member cannot apply for membership extension, it must be extended by the sponsor.");
        }
        checkExtensionWithNeverExpiration(sess, form, vo, member);
      }

    }

  }

  /**
   * Check whether a principal in perun session has already created application in group
   *
   * @param sess            perun session containing principal
   * @param vo              for application
   * @param group           for application
   * @param applicationType type of application
   * @throws DuplicateRegistrationAttemptException if the principal has already created application
   * @throws RegistrarException
   * @throws PrivilegeException
   */
  private void checkDupplicateGroupApplications(PerunSession sess, Vo vo, Group group, AppType applicationType)
      throws DuplicateRegistrationAttemptException, RegistrarException, PrivilegeException {
    // select neccessary information from already existing Group applications
    List<Application> applications = new ArrayList<>(jdbc.query(
        "select id, user_id, created_by, extSourceName, fed_info from application where apptype=? and vo_id=? and " +
         "group_id=? and (state=? or state=?)",
        IDENTITY_APP_MAPPER, applicationType.toString(), vo.getId(), group.getId(), AppState.NEW.toString(),
        AppState.VERIFIED.toString()));
    // not member of VO - check for unprocessed applications to Group
    List<Application> filteredApplications = filterPrincipalApplications(sess, applications);
    if (!filteredApplications.isEmpty()) {
      // user have unprocessed application for group
      throw new DuplicateRegistrationAttemptException("Application for Group: " + group.getName() + " already exists.",
          getApplicationById(filteredApplications.get(0).getId()),
          getApplicationDataById(registrarSession, filteredApplications.get(0).getId()));
    }
  }

  /**
   * Check whether a principal in perun session has already created application in vo
   *
   * @param sess            perun session containing principal
   * @param vo              for application
   * @param applicationType type of application
   * @throws DuplicateRegistrationAttemptException if the principal has already created application
   * @throws RegistrarException
   * @throws PrivilegeException
   */
  private void checkDupplicateVoApplications(PerunSession sess, Vo vo, AppType applicationType)
      throws DuplicateRegistrationAttemptException, RegistrarException, PrivilegeException {
    // select neccessary information from already existing Vo applications
    List<Application> applications = jdbc.query(
        "select id, user_id, created_by, extSourceName, fed_info from application where apptype=? and vo_id=? and " +
         "group_id is null and (state=? or state=?)",
        IDENTITY_APP_MAPPER, applicationType.toString(), vo.getId(), AppState.NEW.toString(),
        AppState.VERIFIED.toString());
    // not member of VO - check for unprocessed applications
    List<Application> filteredApplications = filterPrincipalApplications(sess, applications);
    if (!filteredApplications.isEmpty()) {
      // user have unprocessed application for VO - can't post more
      throw new DuplicateRegistrationAttemptException("Application for VO: " + vo.getName() + " already exists.",
          getApplicationById(filteredApplications.get(0).getId()),
          getApplicationDataById(registrarSession, filteredApplications.get(0).getId()));
    }
  }

  /**
   * Checks that members without expiration attribute (it is NEVER) can't submit extension forms in VOs with defined
   * expiration rules. Throws CantBeSubmittedException in such a case. The only exception is if the form doesn't contain
   * any submit button.
   *
   * @param sess
   * @param form
   * @param vo
   * @param member
   * @throws CantBeSubmittedException
   */
  private void checkExtensionWithNeverExpiration(PerunSession sess, ApplicationForm form, Vo vo, Member member)
      throws CantBeSubmittedException {
    if (member.getStatus() == Status.EXPIRED) {
      return; // expired member can always submit extension form
    }

    try {
      Attribute membershipExpirationRules =
          attrManager.getAttribute(sess, vo, MembersManager.MEMBERSHIP_EXPIRATION_RULES_ATTRIBUTE_NAME);
      Attribute memberExpiration =
          attrManager.getAttribute(sess, member, MembersManager.MEMBERSHIP_EXPIRATION_ATTRIBUTE_NAME);

      if (membershipExpirationRules.getValue() != null && memberExpiration.getValue() == null) {
        List<ApplicationFormItem> formItems;
        try {
          formItems = getFormItems(registrarSession, form, AppType.EXTENSION);
        } catch (PerunException e) {
          throw new InternalErrorException(e);
        }
        boolean hasSubmitButton = formItems.stream()
            .anyMatch(item -> item.getType() == SUBMIT_BUTTON || item.getType() == AUTO_SUBMIT_BUTTON);
        if (hasSubmitButton) {
          throw new CantBeSubmittedException("Members with expiration set to NEVER cannot apply for " +
                                             "membership extension in VOs with defined expiration rules.",
              "NEVER_EXPIRATION", null, null);
        }
      }
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
      // ignore, shouldn't happen
    }
  }

  @Override
  public String checkHtmlInput(PerunSession sess, String html) throws InvalidHtmlInputException {
    Utils.checkPerunSession(sess);

    HTMLParser parser = new HTMLParser().sanitizeHTML(html).checkEscapedHTML();
    if (!parser.isInputValid()) {
      throw new InvalidHtmlInputException(
          "HTML input contains unsafe HTML tags, attributes, styles or links. Remove them and try again.",
          parser.getEscaped());
    }

    // check if after sanitization is the output still valid
    parser = new HTMLParser().sanitizeHTML(parser.getEscapedHTML()).checkEscapedHTML();
    if (!parser.isInputValid()) {
      throw new InvalidHtmlInputException(
          "HTML will be autocompleted during the sanitization and the result will be an invalid HTML (e.g. incorrect " +
           "link in the href attribute). Fix the HTML input and try again.",
          parser.getEscaped());
    }


    // check if the sanitization will change the HTML input
    String responseMessage = "";
    if (!parser.getEscapedHTML().equals(html)) {
      responseMessage =
          "HTML will be autocompleted/changed during the sanitization so the result will be different than the input " +
           "HTML.";
    }

    return responseMessage;
  }

  /**
   * Checks if form item belongs to form with given id
   *
   * @param sess
   * @param formId id of application form
   * @param itemId id of form item
   * @return true if item belongs to the form, false otherwise
   */
  private boolean checkItemBelongsToForm(PerunSession sess, int formId, int itemId) {
    int storedFormId;
    try {
      storedFormId = jdbc.queryForInt("select form_id from application_form_items where id=?", itemId);
    } catch (EmptyResultDataAccessException e) {
      return false;
    }
    return storedFormId == formId;
  }

  /**
   * Returns true if the given map contains a non-empty value for given key.
   *
   * @param map map
   * @param key key
   * @return true if the given map contains a non-empty value for given key, false otherwise
   */
  private boolean containsNonEmptyValue(Map<String, String> map, String key) {
    return map.containsKey(key) && map.get(key) != null && !map.get(key).isEmpty();
  }

  /**
   * Copies configured groups for item with type EMBEDDED_GROUP_APPLICATION. Also limits the configured groups to only
   * valid groups/subgroups. Groups are matched according to group name. Expects the new copied item to be already
   * stored.
   */
  private void copyEmbeddedGroups(PerunSession sess, ApplicationForm fromForm, ApplicationForm toForm, int oldItemId,
                                  ApplicationFormItem newItem)
      throws PrivilegeException, GroupNotAllowedToAutoRegistrationException, FormItemNotExistsException {
    ApplicationFormItem oldItem = getFormItemById(sess, oldItemId);
    List<Group> validGroups;

    if (toForm.getGroup() != null) {
      validGroups = perun.getGroupsManagerBl().getAllSubGroups(sess, toForm.getGroup());
    } else {
      validGroups = perun.getGroupsManagerBl().getAllGroups(sess, toForm.getVo());
    }

    Group group = fromForm.getGroup();
    List<Group> configuredGroups =
        group != null ? perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, group, oldItem) :
            perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, fromForm.getVo(), oldItem);

    List<Group> copyGroups = validGroups.stream().filter(validGroup -> configuredGroups.stream()
        .anyMatch(configuredGroup -> configuredGroup.getName().equals(validGroup.getName()))).toList();
    perun.getGroupsManagerBl().addGroupsToAutoRegistration(sess, copyGroups, newItem);
  }

  @Override
  public void clearVoForm(PerunSession sess, Vo vo)
      throws PrivilegeException, VoNotExistsException, FormNotExistsException {
    //Authorization
    perun.getVosManagerBl().checkVoExists(sess, vo);
    if (!AuthzResolver.authorizedInternal(sess, "clearVoForm_Vo_policy",
        Collections.singletonList(vo))) {
      throw new PrivilegeException(sess, "clearVoForm");
    }
    ApplicationForm form = getFormForVo(vo);
    // reset approval styles and modules
    form.setAutomaticApproval(false);
    form.setAutomaticApprovalExtension(false);
    form.setAutomaticApprovalEmbedded(false);
    form.setModuleClassNames(new ArrayList<>());
    // auth policies are the same for now so should be fine
    try {
      updateForm(sess, form);
    } catch (GroupNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

    List<ApplicationFormItem> items;
    try {
      items = getFormItems(sess, form);
    } catch (PerunException e) {
      throw new InternalErrorException(e);
    }
    // delete all form items
    for (ApplicationFormItem item : items) {
      item.setForDelete(true);
    }
    try {
      updateFormItems(sess, form, items);
    } catch (PerunException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void clearGroupForm(PerunSession sess, Group group)
      throws PrivilegeException, FormNotExistsException, GroupNotExistsException, VoNotExistsException {
    //Authorization
    perun.getGroupsManagerBl().checkGroupExists(sess, group);
    if (!AuthzResolver.authorizedInternal(sess, "clearGroupForm_Group_policy",
        group)) {
      throw new PrivilegeException(sess, "clearGroupForm");
    }
    ApplicationForm form = getFormForGroup(group);
    // reset approval styles and modules
    form.setAutomaticApproval(false);
    form.setAutomaticApprovalExtension(false);
    form.setAutomaticApprovalEmbedded(false);
    form.setModuleClassNames(new ArrayList<>());
    // auth policies are the same for now so should be fine
    updateForm(sess, form);

    List<ApplicationFormItem> items;
    try {
      items = getFormItems(sess, form);
    } catch (PerunException e) {
      throw new InternalErrorException(e);
    }
    // delete all form items
    for (ApplicationFormItem item : items) {
      deleteFormItem(sess, form, item.getOrdnum());
    }
  }

  @Override
  public void copyFormFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup, boolean idempotent)
      throws PerunException {
    groupsManager.checkGroupExists(sess, fromGroup);
    groupsManager.checkGroupExists(sess, toGroup);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromGroupToGroup_Group_Group_policy",
        Collections.singletonList(fromGroup)) ||
        !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromGroupToGroup_Group_Group_policy",
            Collections.singletonList(toGroup))) {
      throw new PrivilegeException(sess, "copyFormFromGroupToGroup");
    }

    try {
      getFormForGroup(toGroup);
    } catch (FormNotExistsException ignored) {
      // we need empty form to copy the items to
      createApplicationFormInGroup(sess, toGroup);
    }
    copyItems(sess, getFormForGroup(fromGroup), getFormForGroup(toGroup), idempotent);
  }

  @Override
  public void copyFormFromVoToGroup(PerunSession sess, Vo fromVo, Group toGroup, boolean reverse, boolean idempotent)
      throws PerunException {
    vosManager.checkVoExists(sess, fromVo);
    groupsManager.checkGroupExists(sess, toGroup);

    if (reverse) {
      //Authorization
      if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromVoToGroup_Vo_Group_Policy",
          Collections.singletonList(toGroup)) ||
          !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromVoToGroup_Vo_Group_Policy",
              Collections.singletonList(fromVo))) {
        throw new PrivilegeException(sess, "copyFormFromVoToGroup");
      }

      copyItems(sess, getFormForGroup(toGroup), getFormForVo(fromVo), idempotent);
    } else {
      //Authorization
      if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromVoToGroup_Vo_Group_Policy",
          Collections.singletonList(fromVo)) ||
          !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromVoToGroup_Vo_Group_Policy",
              Collections.singletonList(toGroup))) {
        throw new PrivilegeException(sess, "copyFormFromVoToGroup");
      }

      try {
        getFormForGroup(toGroup);
      } catch (FormNotExistsException ignored) {
        // we need empty form to copy the items to
        createApplicationFormInGroup(sess, toGroup);
      }
      copyItems(sess, getFormForVo(fromVo), getFormForGroup(toGroup), idempotent);
    }
  }

  @Override
  public void copyFormFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo, boolean idempotent) throws PerunException {
    vosManager.checkVoExists(sess, fromVo);
    vosManager.checkVoExists(sess, toVo);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromVoToVo_Vo_Vo_policy", fromVo) ||
        !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromVoToVo_Vo_Vo_policy", toVo)) {
      throw new PrivilegeException(sess, "copyFormFromVoToVo");
    }

    copyItems(sess, getFormForVo(fromVo), getFormForVo(toVo), idempotent);
  }

  /**
   * Copy items from one form to another.
   *
   * @param sess     session
   * @param fromForm the form from which the items are taken
   * @param toForm   the form where the items are added
   * @param idempotent delete existing target form items if true
   */
  private void copyItems(PerunSession sess, ApplicationForm fromForm, ApplicationForm toForm, boolean idempotent)
      throws PerunException {
    List<ApplicationFormItem> items = getFormItems(sess, fromForm);
    Map<Integer, Integer> oldToNewIDs = new HashMap<>();

    if (idempotent) {
      if (!validateSubmitButtonPresence(items)) {
        throw new MissingSubmitButtonException(
                "Application form contains at least one input field, but no submit or auto-submit button.");
      }
      List<ApplicationFormItem> oldItems = getFormItems(sess, toForm);
      for (ApplicationFormItem item : oldItems) {
        item.setForDelete(true);
      }
      updateFormItems(sess, toForm, oldItems);
    } else {
      List<ApplicationFormItem> bothItems = new ArrayList<>(items);
      bothItems.addAll(registrarManager.getFormItems(sess, toForm));
      if (!validateSubmitButtonPresence(bothItems)) {
        throw new MissingSubmitButtonException(
                "Application form contains at least one input field, but no submit or auto-submit button.");
      }
      if (countEmbeddedGroupFormItems(bothItems) > 1) {
        throw new MultipleApplicationFormItemsException(
            "Multiple definitions of embedded groups. Only one definition is allowed.");
      }
    }

    for (ApplicationFormItem item : items) {
      int oldId = item.getId();
      item.setOrdnum(null); // reset order, id is always new inside add method
      item = addFormItem(sess, toForm, item);
      if (item.getType() == EMBEDDED_GROUP_APPLICATION) {
        copyEmbeddedGroups(registrarSession, fromForm, toForm, oldId, item);
      }
      oldToNewIDs.put(oldId, item.getId());
    }

    for (ApplicationFormItem item : items) {
      if (item.getHiddenDependencyItemId() != null || item.getDisabledDependencyItemId() != null) {
        item.setHiddenDependencyItemId(oldToNewIDs.get(item.getHiddenDependencyItemId()));
        item.setDisabledDependencyItemId(oldToNewIDs.get(item.getDisabledDependencyItemId()));
        updateFormItem(sess, item);
      }
    }
  }

  /**
   * Returns number of embedded groups form items.
   */
  private int countEmbeddedGroupFormItems(List<ApplicationFormItem> items) {
    int counter = 0;
    for (ApplicationFormItem item : items) {
      if (item.getType() == EMBEDDED_GROUP_APPLICATION && !item.isForDelete()) {
        counter++;
      }
    }

    return counter;
  }

  /**
   * Checks if submit or auto-submit button are either not required, or present if required.
   *
   * @param items list of ApplicationFormItems being checked
   * @return true if submit or auto-submit button is either not required, or present. False otherwise
   */
  private boolean validateSubmitButtonPresence(List<ApplicationFormItem> items) {
    boolean containsInputTypeItems = false;

    for (ApplicationFormItem item : items) {
      if (item.isForDelete() || (item.getDisabled() == ApplicationFormItem.Disabled.ALWAYS)) {
        continue;
      }
      ApplicationFormItem.Type itemType = item.getType();
      if (!NON_INPUT_FORM_ITEM_TYPES.contains(itemType)) {
        containsInputTypeItems = true;
      }
      if (SUBMIT_FORM_ITEM_TYPES.contains(itemType) && (item.getHidden() != ApplicationFormItem.Hidden.ALWAYS)) {
        return true;
      }
    }

    return !containsInputTypeItems;
  }

  @Override
  @Deprecated
  public List<ApplicationFormItemData> createApplication(PerunSession session, Application application,
                                                         List<ApplicationFormItemData> data) throws PerunException {
    int appId = processApplication(session, application, data);
    return getApplicationDataById(session, appId);
  }

  @Override
  public void createApplicationFormInGroup(PerunSession sess, Group group)
      throws PrivilegeException, GroupNotExistsException {
    groupsManager.checkGroupExists(sess, group);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createApplicationFormInGroup_Group_policy",
        Collections.singletonList(group))) {
      throw new PrivilegeException(sess, "createApplicationFormInGroup");
    }

    int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
    try {
      jdbc.update("insert into application_form(id, vo_id, group_id) values (?,?,?)", id, group.getVoId(),
          group.getId());
    } catch (DuplicateKeyException ex) {
      throw new ConsistencyErrorException("Group can have defined only one application form. Can't insert another.",
          ex);
    }

  }

  @Override
  public void createApplicationFormInVo(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
    vosManager.checkVoExists(sess, vo);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createApplicationFormInVo_Vo_policy", Collections.singletonList(vo))) {
      throw new PrivilegeException(sess, "createApplicationFormInVo");
    }

    int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
    try {
      jdbc.update("insert into application_form(id, vo_id) values (?,?)", id, vo.getId());
    } catch (DuplicateKeyException ex) {
      throw new ConsistencyErrorException("VO can have defined only one application form. Can't insert another.", ex);
    }

  }

  @Override
  @Transactional(rollbackFor = ApplicationNotCreatedException.class)
  public Application createApplicationInternal(PerunSession session, Application application,
                                               List<ApplicationFormItemData> data) throws PerunException {

    // exceptions to send to vo admin with new app created email
    List<Exception> exceptions = new ArrayList<>();
    boolean applicationNotCreated = false;

    try {

      // 1) create application
      int appId = Utils.getNewId(jdbc, "APPLICATION_ID_SEQ");
      application.setId(appId);

      application.setState(AppState.NEW);

      // optional group
      Integer groupId = null;
      Integer userId = null;
      if (application.getGroup() != null) {
        groupId = application.getGroup().getId();
      }
      if (application.getUser() != null) {
        userId = application.getUser().getId();
      }

      jdbc.update(
          "insert into application(id,vo_id,group_id,user_id,apptype,fed_info,extSourceName,extSourceType," +
                          "extSourceLoa,state,created_by,modified_by) values (?,?,?,?,?,?,?,?,?,?,?,?)",
          appId, application.getVo().getId(), groupId, userId, application.getType().toString(),
          application.getFedInfo(), application.getExtSourceName(), application.getExtSourceType(),
          application.getExtSourceLoa(), application.getState().toString(), application.getCreatedBy(),
          application.getCreatedBy());

      // 2) process & store app data
      for (ApplicationFormItemData itemData : data) {

        Type itemType = itemData.getFormItem().getType();
        if (itemType == HTML_COMMENT || itemType == SUBMIT_BUTTON || itemType == AUTO_SUBMIT_BUTTON ||
            itemType == PASSWORD || itemType == HEADING) {
          continue;
        }

        // Check if mails needs to be validated
        if (itemType == VALIDATED_EMAIL) {
          handleLoaForValidatedMail(session, itemData);
        }

        try {
          itemData.setId(Utils.getNewId(jdbc, "APPLICATION_DATA_ID_SEQ"));
          jdbc.update(
              "insert into application_data(id,app_id,item_id,shortname,value,assurance_level) values (?,?,?,?,?,?)",
              itemData.getId(), appId, itemData.getFormItem().getId(), itemData.getFormItem().getShortname(),
              itemData.getValue(), ((isBlank(itemData.getAssuranceLevel())) ? null : itemData.getAssuranceLevel()));
        } catch (Exception ex) {
          // log and store exception so vo manager could see error in notification.
          LOG.error("[REGISTRAR] Storing form item {} caused exception {}", itemData, ex);
          exceptions.add(ex);
        }

      }

      // 3) process all logins and passwords

      // create list of logins and passwords to process
      List<ApplicationFormItemData> logins = new ArrayList<>();
      for (ApplicationFormItemData itemData : data) {

        Type itemType = itemData.getFormItem().getType();
        if (itemType == USERNAME || itemType == PASSWORD) {
          // skip logins with empty/null value
          if (itemData.getValue() == null || itemData.getValue().isEmpty() || itemData.getValue().equals("null")) {
            continue;
          }
          // skip unchanged pre-filled logins, since they must have been handled last time
          if (itemType == USERNAME && Objects.equals(itemData.getValue(), itemData.getPrefilledValue())) {
            continue;
          }
          logins.add(itemData);
        }
      }

      for (ApplicationFormItemData loginItem : logins) {
        if (loginItem.getFormItem().getType() == USERNAME) {
          // values to store
          String login = loginItem.getValue();
          String pass; // filled later
          // Get login namespace
          String dstAttr = loginItem.getFormItem().getPerunDestinationAttribute();
          AttributeDefinition loginAttribute = attrManager.getAttributeDefinition(session, dstAttr);
          String loginNamespace = loginAttribute.getFriendlyNameParameter();

          boolean loginAvailable = false;
          try {
            loginAvailable = usersManager.isLoginAvailable(session, loginNamespace, login);
          } catch (InvalidLoginException ex) {
            LOG.error("[REGISTRAR] Unable to store login: {} in namespace: {} due to {}", login, loginNamespace, ex);
            throw new ApplicationNotCreatedException(
                "Application was not created. Reason: Login: " + login + " in namespace: " + loginNamespace +
                " is not allowed. Please choose different login.", login, loginNamespace);
          }

          // try to book new login in namespace if the application hasn't been approved yet
          if (loginAvailable) {
            try {
              // Reserve login
              jdbc.update(
                  "insert into application_reserved_logins(login,namespace,user_id,extsourcename,created_by," +
                   "created_at) values(?,?,?,?,?,?)",
                  login, loginNamespace, userId, session.getPerunPrincipal().getExtSourceName(),
                  session.getPerunPrincipal().getActor(), new Date());
              LOG.debug("[REGISTRAR] Added login reservation for login: {} in namespace: {}.", login, loginNamespace);

              // process password for this login
              for (ApplicationFormItemData passItem : logins) {
                ApplicationFormItem item = passItem.getFormItem();
                if (item.getType() == PASSWORD && item.getPerunDestinationAttribute() != null) {
                  if (item.getPerunDestinationAttribute().equals(dstAttr)) {
                    pass = passItem.getValue();
                    try {
                      // reserve password
                      usersManager.reservePassword(registrarSession, login, loginNamespace, pass);
                      LOG.debug(
                          "[REGISTRAR] Password for login: {} in namespace: {} successfully reserved in external " +
                           "system.",
                          login, loginNamespace);
                    } catch (Exception ex) {
                      // login reservation fail must cause rollback !!
                      LOG.error(
                          "[REGISTRAR] Unable to reserve password for login: {} in namespace: {} in external system. " +
                           "Exception: {}",
                          login, loginNamespace, ex);
                      throw new ApplicationNotCreatedException(
                          "Application was not created. Reason: Unable to reserve password for login: " + login +
                          " in namespace: " + loginNamespace +
                          " in external system. Please contact support to fix this issue before new application " +
"submission.",
                          login, loginNamespace);
                    }
                    break; // use first pass with correct namespace
                  }
                }
              }
            } catch (ApplicationNotCreatedException ex) {
              throw ex; // re-throw
            } catch (Exception ex) {
              // unable to book login
              LOG.error("[REGISTRAR] Unable to reserve login: {} in namespace: {}. Exception: ", login, loginNamespace,
                  ex);
              exceptions.add(ex);
            }
          } else {
            // login is not available
            LOG.error("[REGISTRAR] Login: {} in namespace: {} is already occupied but it shouldn't (race condition).",
                login, loginNamespace);
            exceptions.add(new InternalErrorException(
                "Login: " + login + " in namespace: " + loginNamespace + " is already occupied but it shouldn't."));
          }
        }
      }

      // call registrar module before auto validation so createAction is trigerred first
      Set<RegistrarModule> modules;
      if (application.getGroup() != null) {
        modules = getRegistrarModules(getFormForGroup(application.getGroup()));
      } else {
        modules = getRegistrarModules(getFormForVo(application.getVo()));
      }
      if (!modules.isEmpty()) {
        for (RegistrarModule module : modules) {
          module.createApplication(session, application, data);
        }
      }

    } catch (ApplicationNotCreatedException ex) {
      applicationNotCreated = true; // prevent action in finally block
      throw ex; // re-throw
    } catch (Exception ex) {
      // any exception during app creation process => add it to list
      // exceptions when handling logins are catched before
      LOG.error("Unexpected exception when creating application.", ex);
      exceptions.add(ex);
    } finally {

      // process rest only if it was not exception related to PASSWORDS creation
      if (!applicationNotCreated) {
        processNotificationsAfterCreation(session, application, exceptions);
        // if there were exceptions, throw some to let know GUI about it
        if (!exceptions.isEmpty()) {
          RegistrarException ex = new RegistrarException(
              "Your application (ID=" + application.getId() + ") has been created with errors. Administrator of " +
              application.getVo().getName() +
              " has been notified. If you want, you can use \"Send report to RT\" button to send this information to " +
               "administrators directly.");
          LOG.error("[REGISTRAR] New application {} created with errors {}. This is case of PerunException {}",
              application, exceptions, ex.getErrorId());
          throw ex;
        }
        LOG.info("New application {} created.", application);
        perun.getAuditer().log(session, new ApplicationCreated(application));

      }
    }

    // return stored data
    return application;

  }

  /**
   * Method creates a candidate object from application according to the application id.
   *
   * @param app the application
   * @return Candidate
   */
  private Candidate createCandidateFromApplicationData(Application app) {
    // put application data into Candidate
    final Map<String, String> attributes = new HashMap<>();
    jdbc.query("select dst_attr,value from application_data d, application_form_items i where d.item_id=i.id " +
               "and i.dst_attr is not null and i.dst_attr <> '' and d.value is not null and app_id=?",
        (resultSet, i) -> {
          attributes.put(resultSet.getString("dst_attr"), resultSet.getString("value"));
          return null;
        }, app.getId());
    Map<String, String> fedData = BeansUtils.stringToMapOfAttributes(app.getFedInfo());

    // DO NOT STORE LOGINS THROUGH CANDIDATE
    // we do not set logins by candidate object to prevent accidental overwrite while joining identities in process
    attributes.entrySet()
        .removeIf(entry -> entry.getKey().contains("urn:perun:user:attribute-def:def:login-namespace:"));

    // NORMALIZE SSH KEYS VALUE - same as we do for existing users in #storeApplicationAttributes()
    attributes.entrySet().forEach(entry -> {
      if (Objects.equals(AttributesManager.NS_USER_ATTR_DEF + ":sshPublicKey", entry.getKey())) {
        entry.setValue(
            BeansUtils.attributeValueToString(handleArrayValue(null, entry.getValue()), ArrayList.class.getName()));
      }
    });

    Candidate candidate = new Candidate();
    candidate.setAttributes(attributes);

    LOG.debug("[REGISTRAR] Retrieved candidate from DB {}", candidate);

    // first try to parse display_name if not null and not empty
    parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    // if names are separated, used them after
    for (String attrName : attributes.keySet()) {
      // if value not null or empty - set to candidate
      if (attributes.get(attrName) != null && !attributes.get(attrName).isEmpty()) {
        if (URN_USER_TITLE_BEFORE.equals(attrName)) {
          candidate.setTitleBefore(attributes.get(attrName));
        } else if (URN_USER_TITLE_AFTER.equals(attrName)) {
          candidate.setTitleAfter(attributes.get(attrName));
        } else if (URN_USER_FIRST_NAME.equals(attrName)) {
          candidate.setFirstName(attributes.get(attrName));
        } else if (URN_USER_LAST_NAME.equals(attrName)) {
          candidate.setLastName(attributes.get(attrName));
        } else if (URN_USER_MIDDLE_NAME.equals(attrName)) {
          candidate.setMiddleName(attributes.get(attrName));
        }
      }
    }

    return candidate;
  }

  private List<AttributePolicyCollection> createInitialPolicyCollections(
      List<Triple<String, AttributeAction, RoleObject>> policies, int attrId) {
    List<AttributePolicyCollection> policyCollections = new ArrayList<>();
    int collectionId = 0;
    for (Triple<String, AttributeAction, RoleObject> policy : policies) {
      collectionId++;

      AttributePolicy newPolicy = new AttributePolicy(-1, policy.getLeft(), policy.getRight(), collectionId);
      // attributeId is set after definitions are created
      policyCollections.add(
          new AttributePolicyCollection(collectionId, attrId, policy.getMiddle(), List.of(newPolicy)));
    }
    return policyCollections;
  }

  @Override
  public void deleteApplication(PerunSession sess, Application app) throws PerunException {

    //Authorization
    if (app.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-deleteApplication_Application_policy",
          Collections.singletonList(app.getVo()))) {
        throw new PrivilegeException(sess, "deleteApplication");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-deleteApplication_Application_policy",
          Arrays.asList(app.getVo(), app.getGroup()))) {
        throw new PrivilegeException(sess, "deleteApplication");
      }
    }

    // lock to prevent concurrent runs
    synchronized (runningDeleteApplication) {
      if (runningDeleteApplication.contains(app.getId())) {
        throw new AlreadyProcessingException("Application " + app.getId() + " deletion is already processing.");
      } else {
        runningDeleteApplication.add(app.getId());
      }
    }

    try {

      // TODO do we update a potentional linked invitation?
      if (AppState.NEW.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) {

        deleteApplicationReservedLogins(sess, app);

        // delete application and data on cascade
        jdbc.update("delete from application where id=?", app.getId());

      } else {
        if (AppState.VERIFIED.equals(app.getState())) {
          throw new RegistrarException(
              "Submitted application " + app.getId() + " can't be deleted. Please reject the application first.");
        }
        if (AppState.APPROVED.equals(app.getState())) {
          throw new RegistrarException(
              "Approved application " + app.getId() + " can't be deleted. Try to refresh the view to see changes.");
        }
      }
      perun.getAuditer().log(sess, new ApplicationDeleted(app));
      LOG.info("Application {} deleted.", app.getId());

    } finally {
      synchronized (runningDeleteApplication) {
        runningDeleteApplication.remove(app.getId());
      }
    }

  }

  /**
   * Deletes reserved logins which are used only by the given application. Deletes them from both KDC and DB.
   *
   * @param sess
   * @param app
   * @throws PasswordDeletionFailedException
   * @throws PasswordOperationTimeoutException
   * @throws InvalidLoginException
   */
  private void deleteApplicationReservedLogins(PerunSession sess, Application app)
      throws PasswordDeletionFailedException, PasswordOperationTimeoutException, InvalidLoginException {
    List<Pair<String, String>> logins = usersManager.getReservedLoginsOnlyByGivenApp(sess, app.getId());

    for (Pair<String, String> login : logins) {
      try {
        // delete passwords in KDC
        usersManager.deletePassword(registrarSession, login.getRight(), login.getLeft());
      } catch (LoginNotExistsException ex) {
        LOG.error("[REGISTRAR] Login: {} not exists while deleting passwords in application: {}", login.getLeft(),
            app.getId());
      }
      // delete reserved logins in DB
      jdbc.update("delete from application_reserved_logins where namespace=? and login=?", login.getLeft(),
          login.getRight());
    }
  }

  @Override
  public List<ApplicationOperationResult> deleteApplications(PerunSession sess, List<Integer> applicationIds) {
    checkMFAForApplications(sess, applicationIds, "deleteApplication_Application_policy");

    List<ApplicationOperationResult> deleteApplicationsResult = new ArrayList<>();

    for (Integer id : applicationIds) {
      try {
        Application app = getApplicationById(sess, id);
        deleteApplication(sess, app);
        deleteApplicationsResult.add(new ApplicationOperationResult(app.getId(), null));
      } catch (Exception e) {
        deleteApplicationsResult.add(new ApplicationOperationResult(id, e));
      }
    }
    return deleteApplicationsResult;
  }

  @Transactional
  @Override
  public void deleteFormItem(PerunSession user, ApplicationForm form, int ordnum) throws PrivilegeException {

    //Authorization
    if (!AuthzResolver.authorizedInternal(user, "deleteFormItem_ApplicationForm_int_policy",
        Collections.singletonList(form.getVo()))) {
      throw new PrivilegeException(user, "deleteFormItem");
    }

    jdbc.update("delete from application_form_items where form_id=? and ordnum=?", form.getId(), ordnum);
    jdbc.update("update application_form_items set ordnum=ordnum-1 where form_id=? and ordnum>?", form.getId(), ordnum);

    perun.getAuditer().log(user, new FormItemDeleted(form));

  }

  @Override
  public void deleteGroupsFromAutoRegistration(PerunSession sess, List<Group> groups)
      throws GroupNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
    }

    //Authorization
    for (Group group : groups) {
      if (!AuthzResolver.authorizedInternal(sess, "deleteGroupsFromAutoRegistration_List<Group>_policy", group)) {
        throw new PrivilegeException(sess, "deleteGroupsFromAutoRegistration");
      }
    }

    perun.getGroupsManagerBl().deleteGroupsFromAutoRegistration(sess, groups);
  }

  @Override
  public void deleteGroupsFromAutoRegistration(PerunSession sess, List<Group> groups, ApplicationFormItem formItem)
      throws GroupNotExistsException, PrivilegeException, FormItemNotExistsException {
    Utils.checkPerunSession(sess);

    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
    }

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess,
        "deleteGroupsFromAutoRegistration_List<Group>_ApplicationFormItem_policy", new ArrayList<>(groups))) {
      throw new PrivilegeException(sess, "deleteGroupsFromAutoRegistration");
    }

    perun.getGroupsManagerBl().deleteGroupsFromAutoRegistration(sess, groups, formItem);
  }

  @Override
  public void deleteGroupsFromAutoRegistration(PerunSession sess, List<Group> groups, Group registrationGroup,
                                               ApplicationFormItem formItem)
      throws GroupNotExistsException, PrivilegeException, GroupIsNotASubgroupException, FormItemNotExistsException {
    Utils.checkPerunSession(sess);

    List<Group> subGroups = perun.getGroupsManagerBl().getAllSubGroups(sess, registrationGroup);
    for (Group group : groups) {
      perun.getGroupsManagerBl().checkGroupExists(sess, group);
      if (!subGroups.contains(group)) {
        throw new GroupIsNotASubgroupException();
      }
    }

    List<PerunBean> allGroupsToCheck = new ArrayList<>(groups);
    allGroupsToCheck.add(registrationGroup);
    //Authorization
    if (!AuthzResolver.authorizedInternal(sess,
        "deleteGroupsFromAutoRegistration_List<Group>_Group_ApplicationFormItem_policy", allGroupsToCheck)) {
      throw new PrivilegeException(sess, "deleteGroupsFromAutoRegistration");
    }

    perun.getGroupsManagerBl().deleteGroupsFromAutoRegistration(sess, groups, formItem);
  }

  /**
   * Extract names for User from his federation attributes
   *
   * @param federValues map of federation attribute names to their value
   * @return map with exctracted names
   */
  private Map<String, String> extractNames(Map<String, String> federValues) {

    String commonName = federValues.get(SHIB_COMMON_NAME_VAR);
    String displayName = federValues.get(SHIB_DISPLAY_NAME_VAR);

    Map<String, String> parsedName;
    if (displayName != null && !displayName.isEmpty()) {
      parsedName = Utils.parseCommonName(displayName);
    } else if (commonName != null && !commonName.isEmpty()) {
      parsedName = Utils.parseCommonName(commonName);
    } else {
      parsedName = new HashMap<>();
    }
    // if the idp provided first name or last name, always use it
    String fedFirstName = federValues.get(SHIB_FIRST_NAME_VAR);
    String fedLastName = federValues.get(SHIB_LAST_NAME_VAR);

    setIfNotEmpty(parsedName, fedFirstName, "firstName");
    setIfNotEmpty(parsedName, fedLastName, "lastName");

    // do new parsing heuristic
    Candidate candidate = new Candidate();
    if (displayName != null && !displayName.isEmpty() && fedFirstName != null && !fedFirstName.isEmpty() &&
        fedLastName != null && !fedLastName.isEmpty()) {
      parseTitlesAndMiddleName(candidate, displayName, fedFirstName, fedLastName);
    }

    setIfNotEmpty(parsedName, candidate.getMiddleName(), "middleName");
    setIfNotEmpty(parsedName, candidate.getTitleBefore(), "titleBefore");
    setIfNotEmpty(parsedName, candidate.getTitleAfter(), "titleAfter");

    return parsedName;

  }

  @Override
  public List<Application> filterPrincipalApplications(PerunSession sess, List<Application> applications) {
    // get necessary params from session
    PerunPrincipal principal = sess.getPerunPrincipal();
    User user = principal.getUser();
    String actor = principal.getActor();
    String extSourceName = principal.getExtSourceName();
    Map<String, String> additionalInformation = principal.getAdditionalInformations();

    List<Application> filteredApplications = new ArrayList<>();

    // filter principal's applications
    for (Application application : applications) {
      // check existing application by user
      if (user != null && application.getUser() != null && user.getId() == application.getUser().getId()) {
        filteredApplications.add(application);
      } else {
        //check existing application by additional identifiers
        String shibIdentityProvider = additionalInformation.get(UsersManagerBl.ORIGIN_IDENTITY_PROVIDER_KEY);
        if (shibIdentityProvider != null && EXT_SOURCES_MULTIPLE_IDENTIFIERS.contains(shibIdentityProvider)) {
          String principalAdditionalIdentifiers =
              principal.getAdditionalInformations().get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
          if (principalAdditionalIdentifiers == null) {
            //This should not happen
            throw new InternalErrorException("Entry " + UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME +
                                             " is not defined in the principal's additional information. Either it " +
                                              "was not provided by external source used for sign-in or the mapping " +
                                               "configuration is wrong.");
          }
          LinkedHashMap<String, String> additionalFedAttributes;
          try {
            additionalFedAttributes = BeansUtils.stringToMapOfAttributes(application.getFedInfo());
          } catch (Exception ex) {
            // Some very old applications don't have FED_INFO in parseable format
            // fallback to actor/extSourceName match or skip them if it fails
            if (extSourceName.equals(application.getExtSourceName()) && actor.equals(application.getCreatedBy())) {
              filteredApplications.add(application);
            }
            continue;
          }
          String applicationAdditionalIdentifiers =
              additionalFedAttributes.get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
          List<String> identifiersInIntersection =
              BeansUtils.additionalIdentifiersIntersection(principalAdditionalIdentifiers,
                  applicationAdditionalIdentifiers);
          if (!identifiersInIntersection.isEmpty()) {
            filteredApplications.add(application);
          }
        } else if (extSourceName.equals(application.getExtSourceName()) && actor.equals(application.getCreatedBy())) {
          //check existing application by extSourceName and extSource login
          filteredApplications.add(application);
        }
      }
    }
    return filteredApplications;
  }

  @Override
  public List<Application> filterUserApplications(PerunSession sess, User user, List<Application> applications) {

    List<UserExtSource> userExtSources = usersManager.getUserExtSources(registrarSession, user);

    List<Application> resultApps = new ArrayList<>();

    for (Application application : applications) {
      //check based on user id
      if (application.getUser() != null && application.getUser().getId() == user.getId()) {
        resultApps.add(application);
        //check based on user extSources
      } else {
        for (UserExtSource ues : userExtSources) {
          if (ues.getExtSource().getName().equals(application.getExtSourceName()) &&
              ues.getExtSource().getType().equals(application.getExtSourceType())) {
            //login check
            if (ues.getLogin().equals(application.getCreatedBy())) {
              resultApps.add(application);
              break;
            }
            //additional identifiers check
            try {
              Attribute attribute =
                  attrManager.getAttribute(sess, ues, UsersManagerBl.ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
              if (attribute.getValue() != null) {
                List<String> userIdentifiers = attribute.valueAsList();
                // Create Arrays from principal and application identifiers and makes intersection between them.
                LinkedHashMap<String, String> additionalFedAttributes;
                try {
                  additionalFedAttributes = BeansUtils.stringToMapOfAttributes(application.getFedInfo());
                } catch (Exception ex) {
                  // Some very old applications don't have FED_INFO in parseable format
                  // skip them if parsing fails
                  continue;
                }
                String applicationAdditionalIdentifiers =
                    additionalFedAttributes.get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
                String[] applicationIdentifiersArray = {};
                if (applicationAdditionalIdentifiers != null) {
                  applicationIdentifiersArray =
                      applicationAdditionalIdentifiers.split(UsersManagerBl.MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX);
                }
                HashSet<String> principalIdentifiersSet = new HashSet<>(userIdentifiers);
                principalIdentifiersSet.retainAll(Arrays.asList(applicationIdentifiersArray));
                if (!principalIdentifiersSet.isEmpty()) {
                  resultApps.add(application);
                  break;
                }
              }
            } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
              //We can ignore that
            }
          }
        }
      }
    }
    return resultApps;
  }

  /**
   * Places removed dependencies back.
   */
  private void fixDependencies(ApplicationForm form, Map<Integer, Integer> idsTranslation,
                               Map<Integer, Integer> toDisabled, Map<Integer, Integer> toHidden) {
    List<ApplicationFormItem> items = jdbc.query(FORM_ITEM_SELECT + " where form_id=?", ITEM_MAPPER, form.getId());

    for (Integer tempId : toDisabled.keySet()) {
      jdbc.update("update application_form_items set disabled_dependency_item_id=? where id=?",
          idsTranslation.get(toDisabled.get(tempId)), idsTranslation.get(tempId));
    }

    for (Integer tempId : toHidden.keySet()) {
      jdbc.update("update application_form_items set hidden_dependency_item_id=? where id=?",
          idsTranslation.get(toHidden.get(tempId)), idsTranslation.get(tempId));
    }
  }

  /**
   * Check if application meets some condition for forced auto approval.
   *
   * @param sess perun session
   * @param app  application
   * @return true if some condition for forced auto approval is met
   * @throws PerunException
   */
  private boolean forceAutoApprove(PerunSession sess, Application app) throws PerunException {
    Set<RegistrarModule> modules = app.getGroup() != null ? getRegistrarModules(getFormForGroup(app.getGroup())) :
        getRegistrarModules(getFormForVo(app.getVo()));
    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        if (module.autoApproveShouldBeForce(sess, app)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<Group> getAllGroupsForAutoRegistration(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllGroupsForAutoRegistration_policy")) {
      throw new PrivilegeException(sess, "getAllGroupsForAutoRegistration");
    }

    return perun.getGroupsManagerBl().getAllGroupsForAutoRegistration(sess);
  }

  @Override
  public Application getApplicationById(PerunSession sess, int appId) throws RegistrarException, PrivilegeException {

    // get application
    Application app = getApplicationById(appId);
    if (app == null) {
      throw new RegistrarException("Application with ID=" + appId + " doesn't exists.");
    }

    //Authorization
    if (app.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-getApplicationById_int_policy",
          Collections.singletonList(app.getVo())) && !AuthzResolver.selfAuthorizedForApplication(sess, app)) {
        throw new PrivilegeException(sess, "getApplicationById");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-getApplicationById_int_policy",
          Arrays.asList(app.getVo(), app.getGroup())) && !AuthzResolver.selfAuthorizedForApplication(sess, app)) {
        throw new PrivilegeException(sess, "getApplicationById");
      }
    }

    return app;

  }

  /**
   * Retrieves whole application object from DB (authz in parent methods)
   *
   * @param appId ID of application to get
   * @return application object / null if not exists
   */
  private Application getApplicationById(int appId) {
    try {
      return jdbc.queryForObject(APP_SELECT + " where a.id=?", APP_MAPPER, appId);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }

  @Override
  public List<ApplicationFormItemData> getApplicationDataById(PerunSession sess, int appId)
      throws PrivilegeException, RegistrarException {

    // this ensure authorization of user on application
    try {
      getApplicationById(sess, appId);
    } catch (PrivilegeException ex) {
      throw new PrivilegeException(sess, "getApplicationDataById");
    }

    return jdbc.query("select id,item_id,shortname,value,assurance_level from application_data where app_id=?",
        (resultSet, rowNum) -> {
          ApplicationFormItemData data = new ApplicationFormItemData();
          data.setId(resultSet.getInt("id"));
          data.setFormItem(getFormItemById(resultSet.getInt("item_id")));
          data.setShortname(resultSet.getString("shortname"));
          data.setValue(resultSet.getString("value"));
          data.setAssuranceLevel(resultSet.getString("assurance_level"));
          return data;
        }, appId);

  }

  @Override
  public List<Application> getApplicationsForGroup(PerunSession userSession, Group group, List<String> state)
      throws PerunException {
    groupsManager.checkGroupExists(userSession, group);

    //Authorization
    if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForGroup_Group_List<String>_policy",
        Collections.singletonList(group))) {
      throw new PrivilegeException(userSession, "getApplicationsForGroup");
    }
    if (state == null || state.isEmpty()) {
      // list all
      try {
        return jdbc.query(APP_SELECT + " where a.group_id=? order by a.id desc", APP_MAPPER, group.getId());
      } catch (EmptyResultDataAccessException ex) {
        return new ArrayList<>();
      }
    } else {
      // filter by state
      try {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("groupId", group.getId());
        sqlParameterSource.addValue("states", state);
        return namedJdbc.query(APP_SELECT + " where a.group_id=:groupId and state in ( :states ) order by a.id desc",
            sqlParameterSource, APP_MAPPER);
      } catch (EmptyResultDataAccessException ex) {
        return new ArrayList<>();
      }
    }

  }

  @Override
  public List<Application> getApplicationsForGroup(PerunSession userSession, Group group, List<String> state,
                                                   LocalDate dateFrom, LocalDate dateTo) throws PerunException {
    groupsManager.checkGroupExists(userSession, group);

    //Authorization
    if (!AuthzResolver.authorizedInternal(userSession,
        "getApplicationsForGroup_Group_List<String>_LocalDate_LocalDate_policy", Collections.singletonList(group))) {
      throw new PrivilegeException(userSession, "getApplicationsForGroup");
    }
    try {
      MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
      StringBuilder query = new StringBuilder(APP_SELECT);
      query.append(" where a.group_id=:groupId");
      sqlParameterSource.addValue("groupId", group.getId());
      if (state != null && !state.isEmpty()) {
        // list all
        sqlParameterSource.addValue("states", state);
        query.append(" and state in ( :states )");
      }
      if (dateFrom != null) {
        sqlParameterSource.addValue("from", dateFrom);
        query.append(" and a.created_at::date >= :from");
      }
      if (dateTo != null) {
        sqlParameterSource.addValue("to", dateTo);
        query.append(" and a.created_at::date <= :to");
      }
      query.append(" order by a.id desc");
      return namedJdbc.query(query.toString(), sqlParameterSource, APP_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getApplicationsForMember(PerunSession sess, Group group, Member member)
      throws PerunException {
    membersManager.checkMemberExists(sess, member);

    //Authorization
    if (group == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-getApplicationsForMember_Group_Member_policy",
          Collections.singletonList(member))) {
        throw new PrivilegeException(sess, "getApplicationsForMember");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-getApplicationsForMember_Group_Member_policy",
          Arrays.asList(member, group))) {
        throw new PrivilegeException(sess, "getApplicationsForMember");
      }
    }

    try {
      if (group == null) {
        return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? order by a.id desc", APP_MAPPER,
            member.getUserId(), member.getVoId());
      } else {
        return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? and a.group_id=? order by a.id desc", APP_MAPPER,
            member.getUserId(), member.getVoId(), group.getId());
      }
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getApplicationsForUser(User user) {

    try {
      // sort by ID which respect latest applications
      return jdbc.query(APP_SELECT + " where user_id=? order by a.id desc", APP_MAPPER, user.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getApplicationsForUser(PerunSession sess) {

    try {
      List<Application> allApplications = jdbc.query(APP_SELECT + " order by a.id desc", APP_MAPPER);
      return filterPrincipalApplications(sess, allApplications);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getApplicationsForVo(PerunSession userSession, Vo vo, List<String> state,
                                                Boolean includeGroupApplications) throws PerunException {
    vosManager.checkVoExists(userSession, vo);

    //Authorization
    if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForVo_Vo_List<String>_Boolean_policy",
        Collections.singletonList(vo))) {
      throw new PrivilegeException(userSession, "getApplicationsForVo");
    }
    if (state == null || state.isEmpty()) {
      // list all
      try {
        return jdbc.query(
            APP_SELECT + " where a.vo_id=? " + (includeGroupApplications ? "" : " and a.group_id is null ") +
            " order by a.id desc", APP_MAPPER, vo.getId());
      } catch (EmptyResultDataAccessException ex) {
        return new ArrayList<>();
      }
    } else {
      // filter by state
      try {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("voId", vo.getId());
        sqlParameterSource.addValue("states", state);
        return namedJdbc.query(APP_SELECT + " where a.vo_id=:voId and state in ( :states ) " +
                               (includeGroupApplications ? "" : " and a.group_id is null ") + " order by a.id desc",
            sqlParameterSource, APP_MAPPER);
      } catch (EmptyResultDataAccessException ex) {
        return new ArrayList<>();
      }
    }

  }

  @Override
  public List<Application> getApplicationsForVo(PerunSession userSession, Vo vo, List<String> state, LocalDate dateFrom,
                                                LocalDate dateTo, Boolean includeGroupApplications)
      throws PerunException {
    vosManager.checkVoExists(userSession, vo);

    //Authorization
    if (!AuthzResolver.authorizedInternal(userSession,
        "getApplicationsForVo_Vo_List<String>_LocalDate_LocalDate_Boolean_policy", Collections.singletonList(vo))) {
      throw new PrivilegeException(userSession, "getApplicationsForVo");
    }
    try {
      MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
      StringBuilder query = new StringBuilder(APP_SELECT);
      query.append(" where a.vo_id=:voId");
      sqlParameterSource.addValue("voId", vo.getId());
      if (state != null && !state.isEmpty()) {
        // list all
        sqlParameterSource.addValue("states", state);
        query.append(" and state in ( :states )");
      }
      if (dateFrom != null) {
        sqlParameterSource.addValue("from", dateFrom);
        query.append(" and a.created_at::date >= :from");
      }
      if (dateTo != null) {
        sqlParameterSource.addValue("to", dateTo);
        query.append(" and a.created_at::date <= :to");
      }
      if (!includeGroupApplications) {
        query.append(" and a.group_id is null");
      }
      query.append(" order by a.id desc");
      return namedJdbc.query(query.toString(), sqlParameterSource, APP_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public Paginated<RichApplication> getApplicationsPage(PerunSession userSession, Vo vo, ApplicationsPageQuery query)
      throws PerunException {
    if (vo == null) {
      User user = new User();
      if (query.getUserId() == null) {
        throw new IllegalArgumentException("User has to be included in the query when not providing VO");
      }
      user.setId(query.getUserId());
      if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForUser_User_policy",
          Collections.singletonList(user))) {
        throw new PrivilegeException(userSession, "getApplicationsPage");
      }
    } else {
      vosManager.checkVoExists(userSession, vo);

      //Authorization
      if (query.getGroupId() != null) {
        if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForGroup_Group_List<String>_policy",
            Collections.singletonList(groupsManager.getGroupById(userSession, query.getGroupId())))) {
          throw new PrivilegeException(userSession, "getApplicationsPage");
        }
      } else {
        if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForVo_Vo_List<String>_Boolean_policy",
            Collections.singletonList(vo))) {
          throw new PrivilegeException(userSession, "getApplicationsPage");
        }
      }
    }

    MapSqlParameterSource namedParams = new MapSqlParameterSource();
    if (vo != null) {
      namedParams.addValue("voId", vo.getId());
    }
    if (query.getStates() != null) {
      namedParams.addValue("states", query.getStates().stream().map(Enum::toString).toList());
    }
    namedParams.addValue("dateFrom", query.getDateFrom());
    namedParams.addValue("dateTo", query.getDateTo());
    namedParams.addValue("offset", query.getOffset());
    namedParams.addValue("limit", query.getPageSize());
    namedParams.addValue("userId", query.getUserId());
    namedParams.addValue("groupId", query.getGroupId());

    String searchQuery = getSQLWhereForApplicationsPage(query, namedParams);

    String subgroups = " OR a.group_id in (WITH RECURSIVE subgroups AS (" +
                           "    SELECT id, parent_group_id FROM groups WHERE parent_group_id = (:groupId)" +
                           "    UNION ALL" +
                           "    SELECT g.id, g.parent_group_id FROM groups g" +
                           "    INNER JOIN subgroups sg ON g.parent_group_id = sg.id" +
                           ")" +
                           " SELECT id FROM subgroups))";

    Paginated<RichApplication> applications = namedJdbc.query(APP_SELECT_PAGE + " WHERE a.id IS NOT NULL " +
                                                                  (vo != null ? " AND a.vo_id=(:voId) " : "") +
                                                                  (query.getStates() == null ||
                                                                       query.getStates().isEmpty() ? "" :
                                                                  " AND a.state IN (:states) ") +
                                                              (query.getIncludeGroupApplications() != null &&
                                                               query.getIncludeGroupApplications() ? "" :
                                                                  " AND a.group_id is null") +
                                                              (query.getUserId() == null ? "" :
                                                                  "  AND a.user_id=(:userId)") +
                                                              (query.getGroupId() == null ? "" :
                                                                  ("  AND (a.group_id=(:groupId)" +
                                                                    (query.getIncludeSubGroupApplications() != null &&
                                                                     query.getIncludeSubGroupApplications() ? subgroups
                                                                     : ")"))) +
                                                              " AND (:dateFrom) <= a.created_at::date AND a" +
                                                               ".created_at::date <= (:dateTo)" +
                                                              searchQuery +
                                                              // group by to remove duplicates from application_data
                                                              // join
                                                              APP_PAGE_GROUP_BY + " ORDER BY " +
                                                              query.getSortColumn().getSqlOrderBy(query) +
                                                              " OFFSET (:offset)" + " LIMIT (:limit)", namedParams,
        getPaginatedApplicationsExtractor(query));

    if (applications == null) {
      return new Paginated<>(new ArrayList<>(), query.getOffset(), query.getPageSize(), 0);
    }

    for (RichApplication app : applications.getData()) {
      List<ApplicationFormItemData> appData = new ArrayList<>();
      if (query.getGetDetails()) {
        appData = getApplicationDataById(userSession, app.getId());
      }
      app.setFormData(appData);
    }

    return applications;
  }

  /**
   * Gets email for candidate.
   *
   * @param candidate candidate
   * @return email
   */
  private String getCandidateEmail(Candidate candidate) {
    if (candidate.getAttributes().containsKey("urn:perun:member:attribute-def:def:mail")) {
      return candidate.getAttributes().get("urn:perun:member:attribute-def:def:mail");
    } else if (candidate.getAttributes().containsKey("urn:perun:user:attribute-def:def:preferredMail")) {
      return candidate.getAttributes().get("urn:perun:user:attribute-def:def:preferredMail");
    }
    return "";
  }

  @Override
  public ConsolidatorManager getConsolidatorManager() {
    return this.consolidatorManager;
  }

  /**
   * Returns list of all groups embedded to target vo/group of application
   *
   * @param sess
   * @param appId
   * @return
   * @throws PrivilegeException
   * @throws RegistrarException
   */
  public List<Group> getEmbeddedGroups(PerunSession sess, int appId)
      throws PrivilegeException, RegistrarException, GroupNotExistsException {
    List<ApplicationFormItemData> appFormData = getApplicationDataById(sess, appId);
    List<Group> embeddedGroups = new ArrayList<>();
    String groupsPipe = null;
    for (ApplicationFormItemData item : appFormData) {
      if (item.getFormItem().getType() == EMBEDDED_GROUP_APPLICATION) {
        groupsPipe = item.getValue();
      }
    }

    if (groupsPipe != null) {
      //Format is: "Group A#124|Group B#1212|Group C#1212"
      String[] arr = groupsPipe.split("\\|");
      for (String el : arr) {
        String[] subelements = el.split("#");
        int groupId = Integer.parseInt(subelements[subelements.length - 1]);
        embeddedGroups.add(groupsManager.getGroupById(sess, groupId));
      }
    }

    return embeddedGroups;
  }

  @Override
  public ApplicationForm getFormById(PerunSession sess, int id) throws PrivilegeException, FormNotExistsException {

    try {
      ApplicationForm form = jdbc.queryForObject(FORM_SELECT + " where id=?", (resultSet, arg1) -> {
        ApplicationForm form1 = new ApplicationForm();
        form1.setId(resultSet.getInt("id"));
        form1.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
        form1.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
        form1.setAutomaticApprovalEmbedded(resultSet.getBoolean("automatic_approval_embedded"));
        if (resultSet.getString("module_names") != null) {
          form1.setModuleClassNames(Arrays.asList(resultSet.getString("module_names").split(",")));
        }
        try {
          form1.setVo(vosManager.getVoById(sess, resultSet.getInt("vo_id")));
        } catch (Exception ex) {
          // we don't care, shouldn't happen for internal identity.
        }
        try {
          if (resultSet.getInt("group_id") != 0) {
            form1.setGroup(groupsManager.getGroupById(sess, resultSet.getInt("group_id")));
          }
        } catch (Exception ex) {
          // we don't care, shouldn't happen for internal identity.
        }
        return form1;
      }, id);

      if (form == null) {
        throw new FormNotExistsException("Form with ID: " + id + " doesn't exists.");
      }

      //Authorization
      if (Objects.isNull(form.getGroup())) {
        // VO application
        if (!AuthzResolver.authorizedInternal(sess, "vo-getFormById_int_policy",
            Collections.singletonList(form.getVo()))) {
          throw new PrivilegeException(sess, "getFormById");
        }
      } else {
        if (!AuthzResolver.authorizedInternal(sess, "group-getFormById_int_policy",
            Arrays.asList(form.getVo(), form.getGroup()))) {
          throw new PrivilegeException(sess, "getFormById");
        }
      }

      return form;

    } catch (EmptyResultDataAccessException ex) {
      throw new FormNotExistsException("Form with ID: " + id + " doesn't exists.");
    }

  }

  @Override
  public ApplicationForm getFormByItemId(PerunSession sess, int id) throws PrivilegeException, FormNotExistsException {

    try {
      ApplicationForm form =
          jdbc.queryForObject(FORM_SELECT + " where id=(select form_id from application_form_items where id=?)",
              (resultSet, arg1) -> {
                ApplicationForm form1 = new ApplicationForm();
                form1.setId(resultSet.getInt("id"));
                form1.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
                form1.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
                form1.setAutomaticApprovalEmbedded(resultSet.getBoolean("automatic_approval_embedded"));
                if (resultSet.getString("module_names") != null) {
                  form1.setModuleClassNames(Arrays.asList(resultSet.getString("module_names").split(",")));
                }
                try {
                  form1.setVo(vosManager.getVoById(sess, resultSet.getInt("vo_id")));
                } catch (Exception ex) {
                  // we don't care, shouldn't happen for internal identity.
                }
                try {
                  if (resultSet.getInt("group_id") != 0) {
                    form1.setGroup(groupsManager.getGroupById(sess, resultSet.getInt("group_id")));
                  }
                } catch (Exception ex) {
                  // we don't care, shouldn't happen for internal identity.
                }
                return form1;
              }, id);

      if (Objects.isNull(form)) {
        throw new FormNotExistsException("Form with ID: " + id + " doesn't exists.");
      }

      //Authorization
      if (form.getGroup() == null) {
        // VO application
        if (!AuthzResolver.authorizedInternal(sess, "vo-getFormByItemId_int_policy",
            Collections.singletonList(form.getVo()))) {
          throw new PrivilegeException(sess, "getFormByItemId");
        }
      } else {
        if (!AuthzResolver.authorizedInternal(sess, "group-getFormByItemId_int_policy",
            Arrays.asList(form.getVo(), form.getGroup()))) {
          throw new PrivilegeException(sess, "getFormByItemId");
        }
      }

      return form;

    } catch (EmptyResultDataAccessException ex) {
      throw new FormNotExistsException("Form with ID: " + id + " doesn't exists.");
    }

  }

  @Override
  public ApplicationForm getFormForGroup(final Group group) throws FormNotExistsException {

    if (group == null) {
      throw new FormNotExistsException("Group can't be null");
    }

    try {
      return jdbc.queryForObject(FORM_SELECT + " where vo_id=? and group_id=?", (resultSet, arg1) -> {
        ApplicationForm form = new ApplicationForm();
        form.setId(resultSet.getInt("id"));
        form.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
        form.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
        form.setAutomaticApprovalEmbedded(resultSet.getBoolean("automatic_approval_embedded"));
        if (resultSet.getString("module_names") != null) {
          form.setModuleClassNames(Arrays.asList(resultSet.getString("module_names").split(",")));
        }
        form.setGroup(group);
        try {
          form.setVo(vosManager.getVoById(registrarSession, group.getVoId()));
        } catch (Exception ex) {
          // we don't care, shouldn't happen for internal identity.
        }
        return form;
      }, group.getVoId(), group.getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new FormNotExistsException("Form for Group: " + group.getName() + " doesn't exists.");
    } catch (Exception ex) {
      throw new InternalErrorException(ex.getMessage(), ex);
    }

  }

  @Override
  public ApplicationForm getFormForVo(final Vo vo) throws FormNotExistsException {

    if (vo == null) {
      throw new FormNotExistsException("VO can't be null");
    }

    try {
      return jdbc.queryForObject(FORM_SELECT + " where vo_id=? and group_id is null", (resultSet, arg1) -> {
        ApplicationForm form = new ApplicationForm();
        form.setId(resultSet.getInt("id"));
        form.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
        form.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
        form.setAutomaticApprovalEmbedded(resultSet.getBoolean("automatic_approval_embedded"));
        if (resultSet.getString("module_names") != null) {
          form.setModuleClassNames(Arrays.asList(resultSet.getString("module_names").split(",")));
        }
        form.setVo(vo);
        return form;
      }, vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new FormNotExistsException("Form for VO: " + vo.getName() + " doesn't exists.");
    } catch (Exception ex) {
      throw new InternalErrorException(ex.getMessage(), ex);
    }

  }

  @Override
  public ApplicationFormItem getFormItemById(PerunSession session, int id) throws PrivilegeException {

    // authz - can read form -> can get item
    try {
      getFormByItemId(session, id);
    } catch (PrivilegeException ex) {
      throw new PrivilegeException("getFormItemById");
    } catch (PerunException ex) {
      // shouldn't happen
    }

    return getFormItemById(id);

  }

  @Override
  public ApplicationFormItem getFormItemById(int id) {

    ApplicationFormItem item;
    item = jdbc.queryForObject(FORM_ITEM_SELECT + " where id=?", ITEM_MAPPER, id);
    if (item != null) {
      List<ItemTexts> texts = jdbc.query(FORM_ITEM_TEXTS_SELECT + " where item_id=?", ITEM_TEXTS_MAPPER, item.getId());
      for (ItemTexts itemTexts : texts) {
        item.getI18n().put(itemTexts.getLocale(), itemTexts);
      }
      List<AppType> appTypes = jdbc.query(APP_TYPE_SELECT + " where item_id=?", APP_TYPE_MAPPER, item.getId());
      item.setApplicationTypes(appTypes);
    }

    return item;

  }

  /**
   * Retrieve form item data by its ID or NULL if not exists. It also expect, that item belongs to the passed
   * application ID, if not, NULL is returned.
   *
   * @param formItemDataId ID of form item data entry
   * @param applicationId  ID of application this item belongs to
   * @return Form item with data submitted by the User.
   * @throws InternalErrorException When implementation fails
   */
  private ApplicationFormItemData getFormItemDataById(int formItemDataId, int applicationId) {

    try {
      return jdbc.queryForObject(
          "select id,item_id,shortname,value,assurance_level from application_data where id=? and app_id=?",
          (resultSet, rowNum) -> {
            ApplicationFormItemData data = new ApplicationFormItemData();
            data.setId(resultSet.getInt("id"));
            data.setFormItem(getFormItemById(resultSet.getInt("item_id")));
            data.setShortname(resultSet.getString("shortname"));
            data.setValue(resultSet.getString("value"));
            data.setAssuranceLevel(resultSet.getString("assurance_level"));
            return data;
          }, formItemDataId, applicationId);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(
          "Unable to get form item data by its ID:" + formItemDataId + " and application ID: " + applicationId, ex);
    }

  }

  @Override
  public List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm form, AppType appType)
      throws PerunException {

    //Authorization
    if (form.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-getFormItems_ApplicationForm_AppType_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException("getFormItems");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-getFormItems_ApplicationForm_AppType_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException("getFormItems");
      }
    }

    List<ApplicationFormItem> items;
    if (appType == null) {
      items = jdbc.query(FORM_ITEM_SELECT + " where form_id=? order by ordnum asc", ITEM_MAPPER, form.getId());
    } else {
      items = jdbc.query(FORM_ITEM_SELECT +
                         " i,application_form_item_apptypes t where form_id=? and i.id=t.item_id and t.apptype=? " +
                          "order by ordnum asc",
          ITEM_MAPPER, form.getId(), appType.toString());
    }
    for (ApplicationFormItem item : items) {
      List<ItemTexts> texts = jdbc.query(FORM_ITEM_TEXTS_SELECT + " where item_id=?", ITEM_TEXTS_MAPPER, item.getId());
      for (ItemTexts itemTexts : texts) {
        item.getI18n().put(itemTexts.getLocale(), itemTexts);
      }
      List<AppType> appTypes = jdbc.query(APP_TYPE_SELECT + " where item_id=?", APP_TYPE_MAPPER, item.getId());
      item.setApplicationTypes(appTypes);
    }

    return items;
  }

  @Override
  public List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm form) throws PerunException {
    return getFormItems(sess, form, null);
  }

  @Override
  public List<ApplicationFormItemWithPrefilledValue> getFormItemsWithPrefilledValues(PerunSession sess, AppType appType,
                                                                                     ApplicationForm form,
                                                                                     Map<String, List<String>>
                                                                                               externalParams)
      throws PerunException {

    Vo vo = form.getVo();
    Group group = form.getGroup();

    // refresh session (user) to get correct data
    AuthzResolverBlImpl.refreshSession(sess);

    // get necessary params from session
    User user = sess.getPerunPrincipal().getUser();
    String actor = sess.getPerunPrincipal().getActor();
    String extSourceName = sess.getPerunPrincipal().getExtSourceName();
    String extSourceType = sess.getPerunPrincipal().getExtSourceType();
    int extSourceLoa = sess.getPerunPrincipal().getExtSourceLoa();
    Map<String, String> federValues = sess.getPerunPrincipal().getAdditionalInformations();

    // throws exception if user couldn't submit application - no reason to get form
    checkDuplicateRegistrationAttempt(sess, appType, form);

    Set<RegistrarModule> modules = getRegistrarModules(form);
    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        module.canBeSubmitted(sess, appType, federValues, externalParams);
      }
    }

    // PROCEED
    Map<String, String> parsedName = extractNames(federValues);
    List<ApplicationFormItem> formItems = getFormItems(registrarSession, form, appType);

    List<ApplicationFormItemWithPrefilledValue> itemsWithValues = new ArrayList<>();
    for (ApplicationFormItem item : formItems) {
      itemsWithValues.add(new ApplicationFormItemWithPrefilledValue(item, null));
    }

    List<Pair<String, String>> reservedLogins = getPrincipalsReservedLogins(sess); // used to prefill USERNAME items

    // data from pending app to group's VO, use values from attributes which destination in VO application matches
    List<ApplicationFormItemData> pendingVoApplicationData = new ArrayList<>();

    // storage for all prefilled values from perun
    Map<String, Attribute> map = new HashMap<>();

    // get user and member attributes from DB for existing users
    if (user != null) {

      // process user attributes
      List<Attribute> userAttributes = attrManager.getAttributes(sess, user);
      for (Attribute att : userAttributes) {
        map.put(att.getName(), att);
      }
      // process member attributes
      try {
        Member member = membersManager.getMemberByUser(sess, vo, user);
        List<Attribute> memberAttributes = attrManager.getAttributes(sess, member);
        for (Attribute att : memberAttributes) {
          map.put(att.getName(), att);
        }
      } catch (MemberNotExistsException ex) {
        // we don't care that user is not yet member
      }
    }

    // get also vo/group attributes for extended pre-fill !!
    List<Attribute> voAttributes = attrManager.getAttributes(sess, vo);
    for (Attribute att : voAttributes) {
      map.put(att.getName(), att);
    }
    if (group != null) {
      List<Attribute> groupAttributes = attrManager.getAttributes(sess, group);
      for (Attribute att : groupAttributes) {
        map.put(att.getName(), att);
      }
    }

    // fill prepared values from Perun to the form
    Iterator<ApplicationFormItemWithPrefilledValue> it =
            ((Collection<ApplicationFormItemWithPrefilledValue>) itemsWithValues).iterator();
    while (it.hasNext()) {
      ApplicationFormItemWithPrefilledValue itemW = it.next();
      String sourceAttribute = itemW.getFormItem().getPerunSourceAttribute();
      // skip items without perun attr reference
      if (sourceAttribute == null || sourceAttribute.equals("")) {
        continue;
      }
      // if attr exist and value != null
      if (map.get(sourceAttribute) != null && map.get(sourceAttribute).getValue() != null) {
        if (itemW.getFormItem().getType() == PASSWORD && user != null) {
          // if login in namespace exists (for existing users), do not return password field
          // because application form is not place to change login or password
          it.remove();
        } else {
          // else set value
          itemW.setPrefilledValue(BeansUtils.attributeValueToString(map.get(sourceAttribute)));
        }
      }
    }

    // pending vo data for current user (if known to Perun) or session principal
    if (group != null) {
      Optional<Application> pendingVoApplication =
          getOpenApplicationsForUserInVo(sess, vo).stream().filter(voApp -> voApp.getGroup() == null).findFirst();
      if (pendingVoApplication.isPresent()) {
        pendingVoApplicationData = getApplicationDataById(sess, pendingVoApplication.get().getId());
      }
    }

    List<ApplicationFormItemWithPrefilledValue> itemsWithMissingData = new ArrayList<>();

    // get user attributes from federation
    Iterator<ApplicationFormItemWithPrefilledValue> it2 = (itemsWithValues).iterator();
    while (it2.hasNext()) {
      ApplicationFormItemWithPrefilledValue itemW = it2.next();
      String fa = itemW.getFormItem().getFederationAttribute();
      if (fa != null && !fa.isEmpty()) {

        // FILL VALUE FROM FEDERATION
        String s = federValues.get(fa);
        if (s != null && !s.isEmpty()) {
          // In case of email, value from the federation can contain more than one entries, entries are separated by
          // semi-colon
          if (itemW.getFormItem().getType().equals(ApplicationFormItem.Type.VALIDATED_EMAIL)) {
            if (itemW.getPrefilledValue() != null && !itemW.getPrefilledValue().isEmpty()) {
              s = itemW.getPrefilledValue() + ";" + s;
            }
          }
          // remove password field if (login) prefilled from federation
          if (itemW.getFormItem().getType() == PASSWORD) {
            it2.remove();
            continue;
          }
          itemW.setPrefilledValue(s);
          itemW.setAssuranceLevel(String.valueOf(extSourceLoa));
        }

        // TRY TO CONSTRUCT THE VALUE FROM PARTIAL FED-INFO

        ApplicationFormItem item = itemW.getFormItem();
        String sourceAttribute = item.getPerunSourceAttribute();
        if (URN_USER_TITLE_BEFORE.equals(sourceAttribute)) {
          String titleBefore = parsedName.get("titleBefore");
          if (titleBefore != null && !titleBefore.trim().isEmpty()) {
            itemW.setPrefilledValue(titleBefore);
          }
        } else if (URN_USER_TITLE_AFTER.equals(sourceAttribute)) {
          String titleAfter = parsedName.get("titleAfter");
          if (titleAfter != null && !titleAfter.trim().isEmpty()) {
            itemW.setPrefilledValue(titleAfter);
          }
        } else if (URN_USER_FIRST_NAME.equals(sourceAttribute)) {
          String firstName = parsedName.get("firstName");
          if (firstName != null && !firstName.trim().isEmpty()) {
            itemW.setPrefilledValue(firstName);
          }
        } else if (URN_USER_MIDDLE_NAME.equals(sourceAttribute)) {
          String middleName = parsedName.get("middleName");
          if (middleName != null && !middleName.trim().isEmpty()) {
            itemW.setPrefilledValue(middleName);
          } else {
            itemW.setPrefilledValue("");
          }
        } else if (URN_USER_LAST_NAME.equals(sourceAttribute)) {
          String lastName = parsedName.get("lastName");
          if (lastName != null && !lastName.trim().isEmpty()) {
            itemW.setPrefilledValue(lastName);
          }
        } else if (URN_USER_DISPLAY_NAME.equals(sourceAttribute)) {

          // overwrite only if not filled by Perun
          if (itemW.getPrefilledValue() == null || itemW.getPrefilledValue().isEmpty()) {

            String displayName = "";

            if (parsedName.get("titleBefore") != null && !parsedName.get("titleBefore").isEmpty()) {
              displayName += parsedName.get("titleBefore");
            }

            if (parsedName.get("firstName") != null && !parsedName.get("firstName").isEmpty()) {
              if (!displayName.isEmpty()) {
                displayName += " ";
              }
              displayName += parsedName.get("firstName");
            }
            if (parsedName.get("lastName") != null && !parsedName.get("lastName").isEmpty()) {
              if (!displayName.isEmpty()) {
                displayName += " ";
              }
              displayName += parsedName.get("lastName");
            }
            if (parsedName.get("titleAfter") != null && !parsedName.get("titleAfter").isEmpty()) {
              if (!displayName.isEmpty()) {
                displayName += " ";
              }
              displayName += parsedName.get("titleAfter");
            }

            itemW.setPrefilledValue(displayName);

          }

        }

      }

      // prefill USERNAME items with reserved logins
      if (itemW.getFormItem().getType() == USERNAME || itemW.getFormItem().getType() == PASSWORD) {
        String destAttribute = itemW.getFormItem().getPerunDestinationAttribute();
        if (destAttribute != null && !destAttribute.isEmpty()) {
          for (Pair<String, String> login : reservedLogins) {
            String loginAttribute =
                AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + login.getLeft();
            if (destAttribute.equals(loginAttribute)) {
              if (itemW.getFormItem().getType() == USERNAME) {
                itemW.setPrefilledValue(login.getRight());
              } else {
                it2.remove(); // remove password field if login is prefilled from reserved logins
              }
              break;
            }
          }
        }
      }

      // if pending vo application data can be used for group application, overwrite value with it
      for (ApplicationFormItemData appFormItemData : pendingVoApplicationData) {
        String vosAppDestinationAttribute = appFormItemData.getFormItem().getPerunDestinationAttribute();
        if (vosAppDestinationAttribute != null && !vosAppDestinationAttribute.isEmpty() &&
            appFormItemData.getValue() != null &&
            vosAppDestinationAttribute.equals(itemW.getFormItem().getPerunSourceAttribute())) {
          itemW.setPrefilledValue(appFormItemData.getValue());
        }
      }
    }

    Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds =
        itemsWithValues.stream().collect(toMap(item -> item.getFormItem().getId(), Function.identity()));

    List<ApplicationFormItemWithPrefilledValue> noPrefilledUneditableRequiredItems = new ArrayList<>();

    for (ApplicationFormItemWithPrefilledValue itemW : itemsWithValues) {
      // We do require value from IDP (federation) if attribute is supposed to be pre-filled and item is required and
      // not editable to users
      if (isEmpty(itemW.getPrefilledValue()) && itemW.getFormItem().isRequired() &&
          (isItemHidden(itemW, allItemsByIds) || isItemDisabled(itemW, allItemsByIds))) {
        if (URN_USER_DISPLAY_NAME.equals(itemW.getFormItem().getPerunDestinationAttribute())) {
          LOG.error("Couldn't resolve displayName from: {}, parsedNames were: {}", federValues, parsedName);
        }
        // Required uneditable items with no source or federation attribute
        if (isEmpty(itemW.getFormItem().getFederationAttribute()) &&
            isEmpty(itemW.getFormItem().getPerunSourceAttribute())) {
          noPrefilledUneditableRequiredItems.add(itemW);
        } else {
          itemsWithMissingData.add(itemW);
        }
      }
    }


    if (!modules.isEmpty()) {
      for (RegistrarModule module : modules) {
        module.processFormItemsWithData(sess, appType, form, externalParams, itemsWithValues);
      }
    }

    if (!noPrefilledUneditableRequiredItems.isEmpty()) {
      LOG.error("[REGISTRAR] Uneditable (hidden or disabled) required items with no prefilled data: {}",
          noPrefilledUneditableRequiredItems);
      throw new NoPrefilledUneditableRequiredDataException(
          "The administrator sets these required items as hidden or disabled without any prefilled data.",
          noPrefilledUneditableRequiredItems);
    }

    if (!itemsWithMissingData.isEmpty()) {
      LOG.error(
          "[REGISTRAR] Unable to prefill following disabled/hidden form items from their respective Perun/Federation " +
              "attributes: {}", itemsWithMissingData);
      // throw exception only if user is logged-in by Federation IDP/certificate
      if (extSourceType.equals(ExtSourcesManager.EXTSOURCE_IDP)) {
        throw new MissingRequiredDataException(
            "Some form items that are disabled or hidden could not be prefilled from their source Perun/Federation " +
                "attributes:",
            itemsWithMissingData);
      } else if (extSourceType.equals(ExtSourcesManager.EXTSOURCE_X509)) {
        throw new MissingRequiredDataCertException(
            "Some form items that are disabled or hidden could not be prefilled from their source Perun/Certificate " +
                "attributes:",
            itemsWithMissingData);
      }
    }

    Iterator<ApplicationFormItemWithPrefilledValue> itemsIt = itemsWithValues.iterator();
    while (itemsIt.hasNext()) {
      ApplicationFormItemWithPrefilledValue item = itemsIt.next();
      // Process only EMBEDDED_GROUP_APPLICATION items in this block
      if (item.getFormItem().getType() != EMBEDDED_GROUP_APPLICATION) {
        continue;
      }
      // Generate options for EMBEDDED_GROUP_APPLICATION items.
      setGroupsToCheckBoxForGroups(sess, item, user, vo, group);
      // If the item has no options for the user to offer (bcs user is already member in all possible options,
      // remove it from the form completely
      if (StringUtils.isBlank(item.getFormItem().getI18n().get(ApplicationFormItem.EN).getOptions())) {
        itemsIt.remove();
      }
    }

    // return prefilled form
    return itemsWithValues;
  }

  @Override
  public List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo)
      throws VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    perun.getVosManagerBl().checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getGroupsForAutoRegistration_Vo_policy", vo)) {
      throw new PrivilegeException(sess, "getGroupsForAutoRegistration");
    }

    return perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, vo);
  }

  @Override
  public List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo, ApplicationFormItem formItem)
      throws VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    perun.getVosManagerBl().checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getGroupsForAutoRegistration_Vo_ApplicationFormItem_policy", vo)) {
      throw new PrivilegeException(sess, "getGroupsForAutoRegistration");
    }

    return perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, vo, formItem);
  }

  @Override
  public List<Group> getGroupsForAutoRegistration(PerunSession sess, Group group, ApplicationFormItem formItem)
      throws GroupNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    perun.getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getGroupsForAutoRegistration_Group_ApplicationFormItem_policy",
        group)) {
      throw new PrivilegeException(sess, "getGroupsForAutoRegistration");
    }

    return perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, group, formItem);
  }

  /**
   * Return string representation (key) of application used for locking main operations like
   * "create/verify/approve/reject".
   *
   * @param application Application to get key for
   * @return Key for Application
   */
  private String getLockKeyForApplication(Application application) {

    return application.getType().toString() + application.getVo().getShortName() +
           ((application.getGroup() != null) ? application.getGroup().getName() : "nogroup") +
           application.getCreatedBy() + application.getExtSourceName() + application.getExtSourceType();

  }

  @Override
  public MailManager getMailManager() {
    return this.mailManager;
  }

  /**
   * Generates pattern for parsing titles and middle name from given values.
   * <p>
   * The pattern is of format: ^(.*){firstName}(.*){lastName}(.*)$
   *
   * @param firstName first name
   * @param lastName  last name
   * @return pattern for parsing titles and middle name
   */
  private Pattern getNamesPattern(String firstName, String lastName) {
    return Pattern.compile("^(.*)" + firstName + "(.*)" + lastName + "(.*)$");
  }

  @Override
  public List<Application> getOpenApplicationsForUser(User user) {

    try {
      return jdbc.query(APP_SELECT + " where user_id=? and state in (?,?) order by a.id desc", APP_MAPPER, user.getId(),
          AppState.VERIFIED.toString(), AppState.NEW.toString());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getOpenApplicationsForUser(PerunSession sess) {

    try {
      List<Application> applications =
          jdbc.query(APP_SELECT + " where state in (?,?) order by a.id desc", APP_MAPPER, AppState.VERIFIED.toString(),
              AppState.NEW.toString());
      return filterPrincipalApplications(sess, applications);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getOpenApplicationsForUserInVo(User user, Vo vo) {

    try {
      return jdbc.query(APP_SELECT + " where user_id=? and state in (?,?) and a.vo_id=? order by a.id desc", APP_MAPPER,
          user.getId(), AppState.VERIFIED.toString(), AppState.NEW.toString(), vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public List<Application> getOpenApplicationsForUserInVo(PerunSession sess, Vo vo) {

    try {
      List<Application> allApplications =
          jdbc.query(APP_SELECT + " where state in (?,?) and a.vo_id=? order by a.id desc", APP_MAPPER,
              AppState.VERIFIED.toString(), AppState.NEW.toString(), vo.getId());
      return filterPrincipalApplications(sess, allApplications);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  private List<Pair<String, String>> getPrincipalsReservedLogins(PerunSession sess) {
    User user = sess.getPerunPrincipal().getUser();
    List<Pair<String, String>> logins =
        user == null ? new ArrayList<>() : usersManager.getUsersReservedLogins(sess, user);

    // search reserved logins by principals extsourcename and actor
    logins.addAll(jdbc.query(
        "SELECT namespace,login " + " FROM application_reserved_logins " + " WHERE extsourcename=? and created_by=? ",
        (resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")),
        sess.getPerunPrincipal().getExtSourceName(), sess.getPerunPrincipal().getActor()));

    return logins;
  }

  /**
   * Return RegistrarModule for specific application form (VO or Group) so it can be used for more actions.
   *
   * @param form application form
   * @return RegistrarModule if present or null
   */
  private Set<RegistrarModule> getRegistrarModules(ApplicationForm form) {
    if (form == null) {
      // wrong input
      LOG.error("[REGISTRAR] Application form is null when getting it's registrar module.");
      throw new NullPointerException("Application form is null when getting it's registrar module.");
    }

    Set<RegistrarModule> modules = new LinkedHashSet<>();
    if (!form.getModuleClassNames().isEmpty()) {

      RegistrarModule module = null;

      for (String regModule : form.getModuleClassNames()) {
        try {
          LOG.debug("[REGISTRAR] Attempting to instantiate class: {}{}", MODULE_PACKAGE_PATH, regModule);
          module =
              (RegistrarModule) Class.forName(MODULE_PACKAGE_PATH + regModule).getDeclaredConstructor().newInstance();
          module.setRegistrar(registrarManager);
        } catch (Exception ex) {
          LOG.error("[REGISTRAR] Exception when instantiating module. Skipping module {}{}", MODULE_PACKAGE_PATH,
              regModule, ex);
          continue;
        }
        LOG.debug("[REGISTRAR] Class {}{} successfully created.", MODULE_PACKAGE_PATH, regModule);
        modules.add(module);
      }
    }
    return modules;
  }

  private String getSQLWhereForApplicationsPage(ApplicationsPageQuery query, MapSqlParameterSource namedParams) {
    if (isEmpty(query.getSearchString())) {
      return "";
    }
    return " AND " + Utils.prepareSqlWhereForApplicationsSearch(query.getSearchString(), namedParams);
  }

  /**
   * Normalize input value from registration form for array values and correctly merge them with the originalValue from
   * User.
   *
   * @param originalValue Value from Users attribute currently stored in Perun
   * @param newValue      Value provided by registration form
   * @return List of array attribute values after merge
   */
  private ArrayList<String> handleArrayValue(ArrayList<String> originalValue, String newValue) {
    // blank input means no change to original attribute
    if (StringUtils.isBlank(newValue)) {
      return originalValue;
    }

    if (!newValue.endsWith(",")) {
      newValue += ",";
    }

    List<String> newVals = BeansUtils.parseEscapedListValue(newValue);
    if (originalValue == null) {
      return new ArrayList<>(newVals);
    } else {
      for (String s : newVals) {
        if (!originalValue.contains(s)) {
          originalValue.add(s);
        }
      }
      return originalValue;
    }

  }

  /**
   * If user provided value is the same as was pre-filled from Perun, then we set LOA=2 If user provided value is
   * between those provided by Federation, then we keep provided LOA (0 will require mail validation, >0 will skip it).
   * If user provided value is not between any of pre-filled values, then we set LOA=0 to require validation.
   *
   * @param session
   * @param itemData
   */
  private void handleLoaForValidatedMail(PerunSession session, ApplicationFormItemData itemData) {

    // all mails from federation (lowercased)
    List<String> mailsFromFed = new ArrayList<>();
    String mailsFed = session.getPerunPrincipal().getAdditionalInformations().get("mail");
    if (isNotBlank(mailsFed)) {
      mailsFromFed.addAll(Arrays.stream(mailsFed.split(";")).map(String::toLowerCase).collect(Collectors.toList()));
    }

    // all prefilled mails (lowercased)
    List<String> prefilledValues = new ArrayList<>();
    if (isNotBlank(itemData.getPrefilledValue())) {
      prefilledValues.addAll(
          Arrays.stream(itemData.getPrefilledValue().split(";")).map(String::toLowerCase).collect(Collectors.toList()));
    }

    // value(s) pre-filled from perun
    List<String> valuesFromPerun = new ArrayList<>(prefilledValues);
    for (String fromFed : mailsFromFed) {
      valuesFromPerun.remove(fromFed);
    }

    String actualValue = (isNotBlank(itemData.getValue())) ? itemData.getValue().toLowerCase() : null;

    if (valuesFromPerun.contains(actualValue)) {
      // override incoming LOA, since it was from perun
      itemData.setAssuranceLevel("2");
    } else if (!prefilledValues.contains(actualValue)) {
      // clearing LoA to 0, since value is a new
      itemData.setAssuranceLevel("0");
    }

    // or else keep incoming LoA since it was one of pre-filled values from Federation.

    // normalize empty value
    if (isBlank(itemData.getValue())) {
      itemData.setValue(null);
    }

  }

  /**
   * Merge input value from registration form for map values with the originalValue from User.
   *
   * @param originalValue Value from Users attribute currently stored in Perun
   * @param newValue      Value provided by registration form
   * @return Map of attribute keys, values after merge
   */
  private LinkedHashMap<String, String> handleMapValue(LinkedHashMap<String, String> originalValue, String newValue) {
    // blank input means no change to original attribute
    if (StringUtils.isBlank(newValue)) {
      return originalValue;
    }

    LinkedHashMap<String, String> newVals = BeansUtils.stringToMapOfAttributes(newValue);
    if (originalValue == null) {
      return new LinkedHashMap<>(newVals);
    } else {
      for (Map.Entry<String, String> entry : newVals.entrySet()) {
        if (!originalValue.containsKey(entry.getKey())) {
          originalValue.put(entry.getKey(), entry.getValue());
        }
      }
      return originalValue;
    }

  }

  @Override
  public void handleUsersGroupApplications(PerunSession sess, Vo vo, User user) throws PerunException {
    // get group apps based on the vo
    List<Application> apps =
        jdbc.query(APP_SELECT + " where a.vo_id=? and a.group_id is not null and a.state in (?,?)", APP_MAPPER,
            vo.getId(), AppState.VERIFIED.toString(), AppState.NEW.toString());

    //filter only user's apps
    List<Application> applications = filterUserApplications(sess, user, apps);

    for (Application a : applications) {
      setUserForApplication(a, user);
      processDelayedGroupNotifications(sess, a);
      autoApproveGroupApplication(sess, a);
    }
  }

  @Override
  public Map<String, Object> initRegistrar(PerunSession sess, String voShortName, String groupName,
                                           Map<String, List<String>> externalParams)
      throws PerunException {

    Map<String, Object> result = new HashMap<>();
    Vo vo;
    Group group;

    try {

      // GET VO
      vo = vosManager.getVoByShortName(sess, voShortName);
      List<Attribute> list = attrManager.getAttributes(sess, vo,
          Arrays.asList(AttributesManager.NS_VO_ATTR_DEF + ":contactEmail",
              AttributesManager.NS_VO_ATTR_DEF + ":voLogoURL"));

      result.put("vo", vo);
      result.put("voAttributes", list);
      result.put("voForm", getFormForVo(vo));

      // GET INITIAL APPLICATION IF POSSIBLE
      try {

        result.put("voFormInitial",
            getFormItemsWithPrefilledValues(sess, AppType.INITIAL, (ApplicationForm) result.get("voForm"),
                    externalParams));

      } catch (DuplicateRegistrationAttemptException ex) {
        // has submitted application
        result.put("voFormInitialException", ex);
      } catch (AlreadyRegisteredException ex) {
        // is already member of VO
        result.put("voFormInitialException", ex);
      } catch (ExtendMembershipException ex) {
        // can't become member of VO
        result.put("voFormInitialException", ex);
      } catch (NoPrefilledUneditableRequiredDataException ex) {
        // can't display form
        result.put("voFormInitialException", ex);
      } catch (MissingRequiredDataException ex) {
        // can't display form
        result.put("voFormInitialException", ex);
      } catch (MissingRequiredDataCertException ex) {
        // can't display form
        result.put("voFormInitialException", ex);
      } catch (CantBeSubmittedException ex) {
        // can't display form / become member by some custom rules
        result.put("voFormInitialException", ex);
      }

      // ONLY EXISTING USERS CAN EXTEND VO MEMBERSHIP
      if (sess.getPerunPrincipal().getUser() != null) {

        try {
          result.put("voFormExtension",
              getFormItemsWithPrefilledValues(sess, AppType.EXTENSION, (ApplicationForm) result.get("voForm"),
                      externalParams));
        } catch (DuplicateRegistrationAttemptException ex) {
          // has submitted application
          result.put("voFormExtensionException", ex);
        } catch (RegistrarException ex) {
          // more severe exception like bad input/inconsistency
          result.put("voFormExtensionException", ex);
        } catch (ExtendMembershipException ex) {
          // can't extend membership in VO
          result.put("voFormExtensionException", ex);
        } catch (MemberNotExistsException ex) {
          // is not member -> can't extend
          result.put("voFormExtensionException", ex);
        } catch (NoPrefilledUneditableRequiredDataException ex) {
          // can't display form
          result.put("voFormExtensionException", ex);
        } catch (MissingRequiredDataException ex) {
          // can't display form
          result.put("voFormExtensionException", ex);
        } catch (MissingRequiredDataCertException ex) {
          // can't display form
          result.put("voFormExtensionException", ex);
        } catch (CantBeSubmittedException ex) {
          // can't display form / extend membership by some custom rules
          result.put("voFormExtensionException", ex);
        }

      }

      // GET GROUP IF RELEVANT
      if (groupName != null && !groupName.isEmpty()) {

        group = groupsManager.getGroupByName(sess, vo, groupName);
        result.put("group", group);
        result.put("groupForm", getFormForGroup(group));

        try {
          result.put("groupFormInitial",
              getFormItemsWithPrefilledValues(sess, AppType.INITIAL, (ApplicationForm) result.get("groupForm"),
                      externalParams));
        } catch (DuplicateRegistrationAttemptException ex) {
          // has submitted application
          result.put("groupFormInitialException", ex);
        } catch (AlreadyRegisteredException ex) {
          // is already member of group
          result.put("groupFormInitialException", ex);
        } catch (RegistrarException ex) {
          // more severe exception like bad input/inconsistency
          result.put("groupFormInitialException", ex);
        } catch (ExtendMembershipException ex) {
          // can't become member of VO -> then can't be member of group either
          result.put("groupFormInitialException", ex);
        } catch (NoPrefilledUneditableRequiredDataException ex) {
          // can't display form
          result.put("groupFormInitialException", ex);
        } catch (MissingRequiredDataException ex) {
          // can't display form
          result.put("groupFormInitialException", ex);
        } catch (MissingRequiredDataCertException ex) {
          // can't display form
          result.put("groupFormInitialException", ex);
        } catch (CantBeSubmittedException ex) {
          // can't display form / become member by some custom rules
          result.put("groupFormInitialException", ex);
        }

      }

      // ONLY EXISTING USERS CAN EXTEND GROUP MEMBERSHIP
      if (sess.getPerunPrincipal().getUser() != null && groupName != null && !groupName.isEmpty()) {

        try {
          result.put("groupFormExtension",
              getFormItemsWithPrefilledValues(sess, AppType.EXTENSION, (ApplicationForm) result.get("groupForm"),
                      externalParams));
        } catch (DuplicateRegistrationAttemptException ex) {
          // has submitted application
          result.put("groupFormExtensionException", ex);
        } catch (RegistrarException ex) {
          // more severe exception like bad input/inconsistency
          result.put("groupFormExtensionException", ex);
        } catch (ExtendMembershipException ex) {
          // can't extend membership in Group
          result.put("groupFormExtensionException", ex);
        } catch (MemberNotExistsException ex) {
          // is not member -> can't extend
          result.put("groupFormExtensionException", ex);
        } catch (NotGroupMemberException ex) {
          // is not member of Group -> can't extend
          result.put("groupFormExtensionException", ex);
        } catch (NoPrefilledUneditableRequiredDataException ex) {
          // can't display form
          result.put("groupFormExtensionException", ex);
        } catch (MissingRequiredDataException ex) {
          // can't display form
          result.put("groupFormExtensionException", ex);
        } catch (MissingRequiredDataCertException ex) {
          // can't display form
          result.put("groupFormExtensionException", ex);
        } catch (CantBeSubmittedException ex) {
          // can't display form / extend membership by some custom rules
          result.put("groupFormExtensionException", ex);
        }

      }

      // FIND SIMILAR USERS IF IT IS NOT DISABLED
      if (!FIND_SIMILAR_USERS_DISABLED) {
        try {
          List<Identity> similarUsers = getConsolidatorManager().checkForSimilarUsers(sess);
          if (similarUsers != null && !similarUsers.isEmpty()) {
            LOG.debug("Similar users found for {} / {}: {}", sess.getPerunPrincipal().getActor(),
                sess.getPerunPrincipal().getExtSourceName(), similarUsers);
          }
          result.put("similarUsers", similarUsers);
        } catch (Exception ex) {
          // not relevant exception in this use-case
          LOG.error("[REGISTRAR] Exception when searching for similar users.", ex);
        }
      }

    } catch (Exception ex) {

      // we don't have to try any more, return exception
      result.put("exception", ex);
      return result;

    }

    return result;

  }

  protected void initialize() throws PerunException {

    // gets session for a system principal "perunRegistrar"
    final PerunPrincipal pp = new PerunPrincipal("perunRegistrar", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL);
    registrarSession = perun.getPerunSession(pp, new PerunClient());

    // set managers
    this.attrManager = perun.getAttributesManagerBl();
    this.membersManager = perun.getMembersManagerBl();
    this.groupsManager = perun.getGroupsManagerBl();
    this.usersManager = perun.getUsersManagerBl();
    this.vosManager = perun.getVosManagerBl();

    // check necessary attributes
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_FROM_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_FROM_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_FROM_EMAIL);
      attrDef.setNamespace(NAMESPACE_VO_FROM_EMAIL);
      attrDef.setDescription("Email address used as \"from\" in mail notifications.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_TO_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_TO_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_TO_EMAIL);
      attrDef.setNamespace(NAMESPACE_VO_TO_EMAIL);
      attrDef.setDescription("Email addresses (of VO administrators) used as \"to\" in mail notifications.");
      attrDef.setType("java.util.ArrayList");
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_TO_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_TO_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_TO_EMAIL);
      attrDef.setNamespace(NAMESPACE_GROUP_TO_EMAIL);
      attrDef.setDescription("Email addresses (of Group administrators) used as \"to\" in mail notifications.");
      attrDef.setType("java.util.ArrayList");
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_FROM_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_FROM_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_FROM_EMAIL);
      attrDef.setNamespace(NAMESPACE_GROUP_FROM_EMAIL);
      attrDef.setDescription("Email address used as \"from\" in mail notifications.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_FROM_NAME_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_FROM_NAME_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_FROM_NAME_EMAIL);
      attrDef.setNamespace(NAMESPACE_GROUP_FROM_NAME_EMAIL);
      attrDef.setDescription("Name of the sender used as \"from\" in mail notifications.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_FROM_NAME_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_FROM_NAME_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_FROM_NAME_EMAIL);
      attrDef.setNamespace(NAMESPACE_VO_FROM_NAME_EMAIL);
      attrDef.setDescription("Name of the sender used as \"from\" in mail notifications.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_LANGUAGE_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_LANGUAGE_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_LANGUAGE_EMAIL);
      attrDef.setNamespace(NAMESPACE_VO_LANGUAGE_EMAIL);
      attrDef.setDescription("Default language used for application notifications to VO administrators.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_LANGUAGE_EMAIL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_LANGUAGE_EMAIL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL);
      attrDef.setNamespace(NAMESPACE_GROUP_LANGUAGE_EMAIL);
      attrDef.setDescription("Default language used for application notifications to Group administrators.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_APPLICATION_URL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_APPLICATION_URL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_APPLICATION_URL);
      attrDef.setNamespace(NAMESPACE_VO_APPLICATION_URL);
      attrDef.setDescription("Custom link to VO's application form used in e-mail invitations.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_APPLICATION_URL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_APPLICATION_URL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_APPLICATION_URL);
      attrDef.setNamespace(NAMESPACE_GROUP_APPLICATION_URL);
      attrDef.setDescription("Custom link to group's application form used in e-mail invitations.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_REGISTRAR_URL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_REGISTRAR_URL);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_REGISTRAR_URL);
      attrDef.setNamespace(NAMESPACE_VO_REGISTRAR_URL);
      attrDef.setDescription(
          "Custom URL used in registration notifications (hostname without any parameters like: https://hostname" +
           ".domain/). If not set, default hostname of Perun instance is used.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_REGISTRAR_URL);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_REGISTRAR_URL);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_REGISTRAR_URL);
      attrDef.setNamespace(NAMESPACE_GROUP_REGISTRAR_URL);
      attrDef.setDescription(
          "Custom URL used in registration notifications (hostname without any parameters like: https://hostname" +
          ".domain/). This value override same VO setting. If not set, default hostname of Perun instance is used.");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_MAIL_FOOTER);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_VO_MAIL_FOOTER);
      attrDef.setFriendlyName(FRIENDLY_NAME_VO_MAIL_FOOTER);
      attrDef.setNamespace(NAMESPACE_VO_MAIL_FOOTER);
      attrDef.setDescription(
          "Email footer used in mail notifications by tag {mailFooter}. To edit text whithout loose of formatting, " +
          "please use notification's GUI!!");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_VO_HTML_MAIL_FOOTER);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new urn_perun_vo_attribute_def_def_htmlMailFooter().getAttributeDefinition();
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      // only PERUN ADMIN can set HTML notifications for now
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_MAIL_FOOTER);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName(DISPLAY_NAME_GROUP_MAIL_FOOTER);
      attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_MAIL_FOOTER);
      attrDef.setNamespace(NAMESPACE_GROUP_MAIL_FOOTER);
      attrDef.setDescription(
          "Email footer used in mail notifications by tag {mailFooter}. To edit text whithout loose of formatting, " +
           "please use notification's GUI!!");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      policies.add(Triple.of(Role.GROUPADMIN, WRITE, RoleObject.Group));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, URN_GROUP_HTML_MAIL_FOOTER);
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new urn_perun_group_attribute_def_def_htmlMailFooter().getAttributeDefinition();
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.GROUPADMIN, READ, RoleObject.Group));
      // only PERUN ADMIN can set HTML notifications for now
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
    try {
      attrManager.getAttributeDefinition(registrarSession, "urn:perun:vo:attribute-def:def:voLogoURL");
    } catch (AttributeNotExistsException ex) {
      // create attr if not exists
      AttributeDefinition attrDef = new AttributeDefinition();
      attrDef.setDisplayName("VO logo's URL");
      attrDef.setFriendlyName("voLogoURL");
      attrDef.setNamespace("urn:perun:vo:attribute-def:def");
      attrDef.setDescription(
          "Full URL of the VO's logo image (including https://) or base64 encoded data like: 'data:image/png;base64,." +
           "...'");
      attrDef.setType(String.class.getName());
      attrDef = attrManager.createAttribute(registrarSession, attrDef);
      // set attribute rights
      List<Triple<String, AttributeAction, RoleObject>> policies = new ArrayList<>();
      policies.add(Triple.of(Role.VOADMIN, READ, RoleObject.Vo));
      policies.add(Triple.of(Role.VOADMIN, WRITE, RoleObject.Vo));
      perun.getAttributesManager()
          .setAttributePolicyCollections(registrarSession, createInitialPolicyCollections(policies, attrDef.getId()));
    }
  }

  @Override
  public List<Attribute> initialize(String voShortName, String groupName) throws PerunException {

    Vo vo = vosManager.getVoByShortName(registrarSession, voShortName);
    List<Attribute> list = attrManager.getAttributes(registrarSession, vo);
    // load group info if needed
    if (groupName != null && !groupName.isEmpty()) {
      Group group = groupsManager.getGroupByName(registrarSession, vo, groupName);
      list.addAll(attrManager.getAttributes(registrarSession, group));
    }
    return list;

  }

  /**
   * Sets a flag in DB for the given application, which means that a notification of the given type was already sent.
   */
  private void insertAppNotificationSent(PerunSession sess, int appId, MailType notificationType) {
    jdbc.update(
        "INSERT INTO app_notifications_sent(app_id, notification_type, created_by) " + "values (?,?::mail_type,?)",
        appId, notificationType.toString(), sess.getPerunPrincipal().getActor());
  }

  @Override
  public void inviteMemberCandidates(PerunSession sess, Vo vo, Group group, String lang,
                                     List<MemberCandidate> candidates) throws PerunException {
    Utils.checkPerunSession(sess);

    for (MemberCandidate candidate : candidates) {
      if (candidate.getRichUser() != null) {
        mailManager.sendInvitation(sess, vo, group,
            perun.getUsersManager().getUserById(sess, candidate.getRichUser().getId()));
      } else if (candidate.getCandidate() != null) {
        mailManager.sendInvitation(sess, vo, group, null, getCandidateEmail(candidate.getCandidate()), lang);
      }
    }
  }

  /**
   * Returns true if a notification of the given type was already sent for the given application.
   */
  private boolean isAppNotificationAlreadySent(int appId, MailType notificationType) {
    return jdbc.queryForInt(
        "SELECT count(*) FROM app_notifications_sent " + "WHERE app_id=? and notification_type=?::mail_type", appId,
        notificationType.toString()) > 0;
  }

  /**
   * Check, if the checkbox will be checked automatically, in the form.
   *
   * @param formItemWithValue check form item
   * @return true, if the checkbox will be checked
   */
  private boolean isCheckBoxPrefilled(ApplicationFormItemWithPrefilledValue formItemWithValue) {
    if (isBlank(formItemWithValue.getPrefilledValue())) {
      return false;
    }
    // This should probably be more sophisticated, but if the cs and en options have the same options, it should work
    ItemTexts enTexts = formItemWithValue.getFormItem().getI18n().get(Locale.ENGLISH);
    if (enTexts == null) {
      return false;
    }
    return Arrays.stream(enTexts.getOptions().split("\\|")).map(option -> option.split("#")[0])
        .anyMatch(value -> formItemWithValue.getPrefilledValue().equals(value));
  }

  /**
   * Checks, if the given item will be disabled during the submission.
   *
   * @param formItemWithValue item that is checked
   * @param allItemsByIds     all items from the same form by their ids
   * @return true, if the item will be disabled in the submission form
   */
  private boolean isItemDisabled(ApplicationFormItemWithPrefilledValue formItemWithValue,
                                 Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds) {
    ApplicationFormItem formItem = formItemWithValue.getFormItem();

    return switch (formItem.getDisabled()) {
      case NEVER -> false;
      case ALWAYS -> true;
      case IF_EMPTY -> isItemOrDependencyDisabled(formItemWithValue, allItemsByIds, this::isItemEmpty);
      case IF_PREFILLED -> isItemOrDependencyDisabled(formItemWithValue, allItemsByIds, this::isItemPrefilled);
    };
  }

  /**
   * Check, if the item will not be prefilled.
   *
   * @param formItemWithValue item that is checked
   * @return true, if the item is empty - was not prefilled
   */
  private boolean isItemEmpty(ApplicationFormItemWithPrefilledValue formItemWithValue) {
    return !isItemPrefilled(formItemWithValue);
  }

  /**
   * Checks, if the given item will be displayed during the submission.
   *
   * @param formItemWithValue item that is checked
   * @param allItemsByIds     all items from the same form by their ids
   * @return true, if the item will be hidden in the submission form
   */
  private boolean isItemHidden(ApplicationFormItemWithPrefilledValue formItemWithValue,
                               Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds) {
    ApplicationFormItem formItem = formItemWithValue.getFormItem();

    return switch (formItem.getHidden()) {
      case NEVER -> false;
      case ALWAYS -> true;
      case IF_EMPTY -> isItemOrDependencyHidden(formItemWithValue, allItemsByIds, this::isItemEmpty);
      case IF_PREFILLED -> isItemOrDependencyHidden(formItemWithValue, allItemsByIds, this::isItemPrefilled);
    };
  }

  /**
   * Checks, if the given item matches the given validator, or its disabled dependency item, if specified.
   *
   * @param formItemWithValue the base item that is checked
   * @param allItemsByIds     all items by their ids
   * @param validator         validator that is used to check the given item, or its dependency item
   * @return true, if the given item matches the given validator, or its disabled dependency item, if specified.
   */
  private boolean isItemOrDependencyDisabled(ApplicationFormItemWithPrefilledValue formItemWithValue,
                                             Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds,
                                             Function<ApplicationFormItemWithPrefilledValue, Boolean> validator) {
    ApplicationFormItem formItem = formItemWithValue.getFormItem();

    if (formItem.getDisabledDependencyItemId() == null) {
      return validator.apply(formItemWithValue);
    }
    var dependencyItem = allItemsByIds.get(formItem.getDisabledDependencyItemId());
    if (dependencyItem == null) {
      LOG.error("Application item has a dependency on item, which is not part of the same form. Item: {}", formItem);
      throw new InternalErrorException(
          "Application item has a dependency on item, which is not part of the same form. Item: " + formItem);
    } else {
      return validator.apply(dependencyItem);
    }
  }

  /**
   * Checks, if the given item matches the given validator, or its hidden dependency item, if specified.
   *
   * @param formItemWithValue the base item that is checked
   * @param allItemsByIds     all items by their ids
   * @param validator         validator that is used to check the given item, or its dependency item
   * @return true, if the given item matches the given validator, or its hidden dependency item, if specified.
   */
  private boolean isItemOrDependencyHidden(ApplicationFormItemWithPrefilledValue formItemWithValue,
                                           Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds,
                                           Function<ApplicationFormItemWithPrefilledValue, Boolean> validator) {
    ApplicationFormItem formItem = formItemWithValue.getFormItem();

    if (formItem.getHiddenDependencyItemId() == null) {
      return validator.apply(formItemWithValue);
    }
    var dependencyItem = allItemsByIds.get(formItem.getHiddenDependencyItemId());
    if (dependencyItem == null) {
      LOG.error("Application item has a dependency on item, which is not part of the same form. Item: {}", formItem);
      throw new InternalErrorException(
          "Application item has a dependency on item, which is not part of the same. form. Item: " + formItem);
    }
    return validator.apply(dependencyItem);
  }

  /**
   * Checks, if the item will be prefilled in the gui.
   *
   * @param formItemWithValue item, that is checked
   * @return true, if the item is prefilled
   */
  private boolean isItemPrefilled(ApplicationFormItemWithPrefilledValue formItemWithValue) {
    // For now, we support a special behaviour only for checkboxes
    if (formItemWithValue.getFormItem().getType() == CHECKBOX) {
      return isCheckBoxPrefilled(formItemWithValue);
    }
    return isNotBlank(formItemWithValue.getPrefilledValue());
  }

  /**
   * Forcefully marks application as VERIFIED (only if was in NEW state before)
   *
   * @param sess  session info to use for modified_by
   * @param appId ID of application to verify.
   */
  private void markApplicationVerified(PerunSession sess, int appId) {

    try {
      if (jdbc.update("update application set state=?, modified_at=" + Compatibility.getSysdate() +
                      ", modified_by=? where id=? and state=?", AppState.VERIFIED.toString(),
          sess.getPerunPrincipal().getActor(), appId, AppState.NEW.toString()) > 0) {
        LOG.info("Application {} marked as VERIFIED", appId);
      } else {
        LOG.info("Application {} not marked VERIFIED, was not in state NEW", appId);
      }
    } catch (InternalErrorException ex) {
      LOG.error("Application {} NOT marked as VERIFIED due to error {}", appId, ex);
    }

  }

  @Transactional
  @Override
  public void moveFormItem(PerunSession user, ApplicationForm form, int ordnum, boolean up) throws PrivilegeException {

    //Authorization
    if (!AuthzResolver.authorizedInternal(user, "moveFormItem_ApplicationForm_int_boolean_policy",
        Collections.singletonList(form.getVo()))) {
      throw new PrivilegeException(user, "moveFormItem");
    }

    if (up && ordnum == 0) {
      throw new InternalErrorException("cannot move topmost item up");
    }

    int numItems = jdbc.queryForInt("select count(*) from application_form_items where form_id=?", form.getId());

    if (!up && ordnum == numItems - 1) {
      throw new InternalErrorException("cannot move lowest item down");
    }

    int id1 = jdbc.queryForInt("select id from application_form_items where form_id=? and ordnum=?", form.getId(),
        (up ? ordnum - 1 : ordnum));
    int id2 = jdbc.queryForInt("select id from application_form_items where form_id=? and ordnum=?", form.getId(),
        (up ? ordnum : ordnum + 1));
    jdbc.update("update application_form_items set ordnum=ordnum+1 where id=?", id1);
    jdbc.update("update application_form_items set ordnum=ordnum-1 where id=?", id2);

  }

  /**
   * To given candidate, sets middle name from trim of given value.
   *
   * @param candidate candidate
   * @param value     value
   */
  private void parseMiddleName(Candidate candidate, String value) {
    candidate.setMiddleName(ALNUM_PATTERN.matcher(value).matches() ? value : null);
  }

  public void parseNamesFromDisplayName(Candidate candidate, Map<String, String> attributes) {
    if (containsNonEmptyValue(attributes, URN_USER_DISPLAY_NAME)) {
      // parse
      Map<String, String> commonName = Utils.parseCommonName(attributes.get(URN_USER_DISPLAY_NAME));
      if (commonName.get("titleBefore") != null && !commonName.get("titleBefore").isEmpty()) {
        candidate.setTitleBefore(commonName.get("titleBefore"));
      }
      if (commonName.get("firstName") != null && !commonName.get("firstName").isEmpty()) {
        candidate.setFirstName(commonName.get("firstName"));
      }
      // FIXME - ? there is no middleName in Utils.parseCommonName() implementation
      if (commonName.get("middleName") != null && !commonName.get("middleName").isEmpty()) {
        candidate.setMiddleName(commonName.get("middleName"));
      }
      if (commonName.get("lastName") != null && !commonName.get("lastName").isEmpty()) {
        candidate.setLastName(commonName.get("lastName"));
      }
      if (commonName.get("titleAfter") != null && !commonName.get("titleAfter").isEmpty()) {
        candidate.setTitleAfter(commonName.get("titleAfter"));
      }
    }
  }

  /**
   * Check if the given fed info contains givenName and sn (surname). If so, it sets it to the candidate and tries to
   * match titles and middle name from display name.
   *
   * @param candidate  candidate
   * @param attributes attributes with values
   * @param fedInfo    key-value info from idp
   */
  public void parseNamesFromDisplayNameAndFedInfo(Candidate candidate, Map<String, String> attributes,
                                                  Map<String, String> fedInfo) {
    if (fedInfo != null && containsNonEmptyValue(fedInfo, SHIB_FIRST_NAME_VAR) &&
        containsNonEmptyValue(fedInfo, SHIB_LAST_NAME_VAR)) {
      String firstName = fedInfo.get(SHIB_FIRST_NAME_VAR);
      String lastName = fedInfo.get(SHIB_LAST_NAME_VAR);

      candidate.setFirstName(firstName);
      candidate.setLastName(lastName);

      tryToParseTitlesAndMiddleName(candidate, attributes, firstName, lastName);
    } else {
      parseNamesFromDisplayName(candidate, attributes);
    }
  }

  /**
   * To given candidate, sets titleAfter from trim of given value, or null if empty.
   *
   * @param candidate candidate
   * @param value     value
   */
  private void parseTitlesAfter(Candidate candidate, String value) {
    candidate.setTitleAfter(ALNUM_PATTERN.matcher(value).matches() ? value : null);
  }

  private void parseTitlesAndMiddleName(Candidate candidate, String displayName, String firstName, String lastName) {
    Pattern pattern = getNamesPattern(firstName, lastName);
    if (!tryToParseTitlesAndMiddleNameFromPattern(candidate, displayName, pattern, firstName)) {
      Pattern reversePattern = getNamesPattern(lastName, firstName);
      tryToParseTitlesAndMiddleNameFromPattern(candidate, displayName, reversePattern, lastName);
    }
  }

  /**
   * To given candidate, sets titleBefore from trim of given value, or null if empty.
   *
   * @param candidate candidate
   * @param value     value
   */
  private void parseTitlesBefore(Candidate candidate, String value) {
    candidate.setTitleBefore(ALNUM_PATTERN.matcher(value).matches() ? value : null);
  }

  /**
   * Prepare map tempId -> savedId, save tempId -> hiddenDependencyId and tempId -> disabledDependencyId and reset the
   * dependencyId attributes for unsaved items
   *
   * @param items      Updated items
   * @param ids        tempId -> savedId map
   * @param toDisabled tempId -> disabledDependencyId
   * @param toHidden   tempId -> hiddenDependencyId
   */
  private void prepareIdsMapping(List<ApplicationFormItem> items, Map<Integer, Integer> ids,
                                 Map<Integer, Integer> toDisabled, Map<Integer, Integer> toHidden) {
    for (ApplicationFormItem item : items) {
      //save all updated items' ids
      ids.put(item.getId(), item.getId());

      //save all dependencies, which will be temporarily removed
      if (item.getDisabledDependencyItemId() != null && item.getDisabledDependencyItemId() < 0) {
        toDisabled.put(item.getId(), item.getDisabledDependencyItemId());
        item.setDisabledDependencyItemId(null);
      }
      if (item.getHiddenDependencyItemId() != null && item.getHiddenDependencyItemId() < 0) {
        toHidden.put(item.getId(), item.getHiddenDependencyItemId());
        item.setHiddenDependencyItemId(null);
      }
    }
  }

  private int processApplication(PerunSession session, Application application, List<ApplicationFormItemData> data)
      throws PerunException {
    return processApplication(session, application, data, null);
  }

  private int processApplication(PerunSession session, Application application,
                                 List<ApplicationFormItemData> data, Invitation invitation)
      throws PerunException {

    // If user is known in Perun but unknown in GUI (user joined identity by consolidator)
    if (application.getUser() == null && session.getPerunPrincipal().getUser() != null) {
      application.setUser(session.getPerunPrincipal().getUser());
    }

    // lock to prevent multiple submission of same application on server side
    String key = getLockKeyForApplication(application);

    synchronized (runningCreateApplication) {
      if (runningCreateApplication.contains(key)) {
        throw new AlreadyProcessingException("Your application submission is being processed already.");
      } else {
        runningCreateApplication.add(key);
      }
    }

    // store user-ext-source attributes and redirectURL from session to application object
    if (application.getType() != AppType.EMBEDDED) {
      LinkedHashMap<String, String> map = new LinkedHashMap<>(session.getPerunPrincipal().getAdditionalInformations());
      if (application.getFedInfo() != null && application.getFedInfo().contains("redirectURL")) {
        String redirectURL = StringUtils.substringBetween(
            application.getFedInfo().substring(application.getFedInfo().indexOf("redirectURL")), "\"", "\"");
        if (redirectURL != null && !redirectURL.equals("null")) {
          map.put("redirectURL", redirectURL);
        }
      }
      String additionalAttrs = BeansUtils.attributeValueToString(map, LinkedHashMap.class.getName());
      application.setFedInfo(additionalAttrs);
    }

    Application processedApplication;
    try {

      // throws exception if user already submitted application or is already a member or can't submit it by VO/Group
      // expiration rules.
      checkDuplicateRegistrationAttempt(session, application.getType(),
          (application.getGroup() != null) ? getFormForGroup(application.getGroup()) :
              getFormForVo(application.getVo()));

      // using this to init inner transaction
      // all minor exceptions inside are catched, if not, it's ok to throw them out
      processedApplication = this.registrarManager.createApplicationInternal(session, application, data);
    } catch (Exception ex) {
      // clear flag and re-throw exception, since application was processed with exception
      synchronized (runningCreateApplication) {
        runningCreateApplication.remove(key);
      }
      throw ex;
    }

    if (invitation != null) {
      invitationsManager.setInvitationApplicationId(session, invitation, processedApplication.getId());
      invitationsManager.setInvitationStatus(session, invitation, InvitationStatus.ACCEPTED);
      tryToVerifyApplicationEmailsFromInvitation(session, processedApplication, invitation);

      LOG.debug("Invitation {} was assigned to application {} and marked as ACCEPTED",
          invitation.getId(), processedApplication.getId());
    }

    // try to verify (or even auto-approve) application
    try {
      boolean verified = tryToVerifyApplication(session, processedApplication);
      if (verified) {
        // try to APPROVE if auto approve
        tryToAutoApproveApplication(session, processedApplication);
      } else {
        // send request validation notification
        getMailManager().sendMessage(processedApplication, MailType.MAIL_VALIDATION, null, null);
      }
      // refresh current session, if submission was successful,
      // since user might have been created.
      AuthzResolverBlImpl.refreshSession(session);
    } catch (Exception ex) {
      LOG.error("[REGISTRAR] Unable to verify or auto-approve application {}, because of exception {}",
          processedApplication, ex);
      // clear flag and re-throw exception, since application was processed with exception
      synchronized (runningCreateApplication) {
        runningCreateApplication.remove(key);
      }
      throw ex;
    }

    // clear flag, since application was processed
    synchronized (runningCreateApplication) {
      runningCreateApplication.remove(key);
    }

    return processedApplication.getId();

  }

  /**
   * Checks if any mails from the given application match the email from the linked pre-approved invitation.
   * If so the email from the application will be marked as verified.
   *
   * @param session perun session
   * @param application the application from which to try and verify the mails
   * @param invitation the invitation linked with the application
   */
  private void tryToVerifyApplicationEmailsFromInvitation(PerunSession session, Application application,
                                                          Invitation invitation) {
    try {
      int rowsAffected = jdbc.update("update application_data AS d SET assurance_level=1" +
                                         " from application a, application_form_items i" +
                                         " where d.app_id=a.id and d.item_id=i.id and a.id=? and i.type=?" +
                                         " and d.value=?",
          application.getId(), VALIDATED_EMAIL.toString(), invitation.getReceiverEmail());
      if (rowsAffected > 0) {
        LOG.debug("Email {} from application {} was verified based on invitation {}.", invitation.getReceiverEmail(),
            application.getId(), invitation.getId());
      }
    } catch (Exception ignored) {
      // we do not care about this failling, user can verify the mail manually
    }
  }

  /**
   * Processes delayed group notifications.
   *
   * @param application for which will be notifications processed
   */
  private void processDelayedGroupNotifications(PerunSession sess, Application application)
      throws MemberNotExistsException {

    Member member = perun.getMembersManagerBl().getMemberByUser(sess, application.getVo(), application.getUser());
    if ((member.getStatus().equals(Status.VALID) || member.getStatus().equals(Status.INVALID)) &&
        !isAppNotificationAlreadySent(application.getId(), MailType.APP_CREATED_VO_ADMIN)) {
      getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, null);
      getMailManager().sendMessage(application, MailType.APPROVABLE_GROUP_APP_USER, null, null);
      insertAppNotificationSent(sess, application.getId(), MailType.APP_CREATED_VO_ADMIN);
    }
  }

  /**
   * Processes notifications for created application
   *
   * @param session     Perun session
   * @param application for which notifications are processed
   * @param exceptions  which occurred during the application creation
   */
  private void processNotificationsAfterCreation(PerunSession session, Application application,
                                                 List<Exception> exceptions) {
    getMailManager().sendMessage(application, MailType.APP_CREATED_USER, null, null);

    // Send APPROVABLE_GROUP_APP_USER notifications if possible (if user is already VO member)
    if (application.getUser() != null && application.getGroup() != null) {
      try {
        Member member =
            perun.getMembersManagerBl().getMemberByUser(session, application.getVo(), application.getUser());
        if (member.getStatus().equals(Status.VALID) || member.getStatus().equals(Status.INVALID)) {
          getMailManager().sendMessage(application, MailType.APPROVABLE_GROUP_APP_USER, null, null);
        }
      } catch (MemberNotExistsException e) {
        // Means that we do not send notification to user yet
      }
    }

    if (!exceptions.isEmpty()) {
      // If there were errors, send the notification immediately to admins
      getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, exceptions);
    } else if (application.getGroup() == null) {
      // If it is VO app, send the notification immediately to admins
      getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, null);
    } else {
      // If it is GROUP app, the member does not exist yet or the member is DISABLED or EXPIRED,
      // do not send the notification to admins yet.
      // It will be sent after the corresponding VO app will be approved.
      if (application.getUser() != null) {
        try {
          Member member =
              perun.getMembersManagerBl().getMemberByUser(session, application.getVo(), application.getUser());
          if (member.getStatus().equals(Status.VALID) || member.getStatus().equals(Status.INVALID)) {
            getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, null);
            insertAppNotificationSent(session, application.getId(), MailType.APP_CREATED_VO_ADMIN);
          }
        } catch (MemberNotExistsException e) {
          // Means that we do not send notification to admins yet
        }
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Application rejectApplication(PerunSession sess, int appId, String reason) throws PerunException {

    Application app = getApplicationById(appId);
    if (app == null) {
      throw new RegistrarException("Application with ID=" + appId + " doesn't exists.");
    }

    //Authorization
    if (app.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-rejectApplication_int_String_policy",
          Collections.singletonList(app.getVo()))) {
        throw new PrivilegeException(sess, "rejectApplication");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-rejectApplication_int_String_policy",
          Arrays.asList(app.getVo(), app.getGroup()))) {
        throw new PrivilegeException(sess, "rejectApplication");
      }
    }

    // only VERIFIED applications can be rejected
    if (AppState.APPROVED.equals(app.getState())) {
      throw new RegistrarException(
          "Approved application " + appId + " can't be rejected ! Try to refresh the view to see changes.");
    } else if (AppState.REJECTED.equals(app.getState())) {
      throw new RegistrarException(
          "Application " + appId + " is already rejected. Try to refresh the view to see changes.");
    }

    // lock to prevent concurrent runs
    synchronized (runningRejectApplication) {
      if (runningRejectApplication.contains(appId)) {
        throw new AlreadyProcessingException("Application " + appId + " rejection is already processing.");
      } else {
        runningRejectApplication.add(appId);
      }
    }

    try {

      // mark as rejected
      int result = jdbc.update("update application set state=?, modified_by=?, modified_at=? where id=?",
          AppState.REJECTED.toString(), sess.getPerunPrincipal().getActor(), new Date(), appId);
      if (result == 0) {
        throw new RegistrarException("Application with ID=" + appId + " not found.");
      } else if (result > 1) {
        throw new ConsistencyErrorException("More than one application is stored under ID=" + appId + ".");
      }
      // set back as rejected
      app.setState(AppState.REJECTED);
      LOG.info("Application {} marked as REJECTED.", appId);

      deleteApplicationReservedLogins(sess, app);
      // reject all of user's other group applications to the VO
      if (app.getGroup() == null && app.getType().equals(AppType.INITIAL)) {
        // create dummy principal and session using users information to pass it into getOpenApplicationsForUserInVo
        PerunPrincipal userPrincipal = new PerunPrincipal(app.getCreatedBy(), "", "", app.getUser());
        userPrincipal.setExtSourceName(app.getExtSourceName());
        userPrincipal.setExtSourceType(app.getExtSourceType());
        userPrincipal.setAdditionalInformations(BeansUtils.stringToMapOfAttributes(app.getFedInfo()));
        PerunSession helperSess = new PerunSessionImpl(sess.getPerun(), userPrincipal, sess.getPerunClient());
        List<Application> otherApps = registrarManager.getOpenApplicationsForUserInVo(helperSess, app.getVo());
        for (Application otherApp : otherApps) {
          registrarManager.rejectApplication(sess, otherApp.getId(), DEFAULT_GROUP_MSG_VO);
        }
      }

      // log
      perun.getAuditer().log(sess, new ApplicationRejected(app));

      // call registrar module
      Set<RegistrarModule> modules;
      if (app.getGroup() != null) {
        modules = getRegistrarModules(getFormForGroup(app.getGroup()));
      } else {
        modules = getRegistrarModules(getFormForVo(app.getVo()));
      }
      if (!modules.isEmpty()) {
        for (RegistrarModule module : modules) {
          module.rejectApplication(sess, app, reason);
        }
      }

      // send mail
      getMailManager().sendMessage(app, MailType.APP_REJECTED_USER, reason, null);

      perun.getAuditer().log(sess, new ApplicationRejected(app));

      // return updated application
      return app;

    } finally {

      // always release lock
      synchronized (runningRejectApplication) {
        runningRejectApplication.remove(appId);
      }

    }

  }

  @Override
  public List<ApplicationOperationResult> rejectApplications(PerunSession sess, List<Integer> applicationIds,
                                                             String reason) {
    checkMFAForApplications(sess, applicationIds, "rejectApplication_int_String_policy");

    Collections.sort(applicationIds, Collections.reverseOrder());
    List<ApplicationOperationResult> rejectApplicationsResult = new ArrayList<>();
    for (Integer id : applicationIds) {
      try {
        registrarManager.rejectApplication(sess, id, reason);
        rejectApplicationsResult.add(new ApplicationOperationResult(id, null));
      } catch (Exception e) {
        rejectApplicationsResult.add(new ApplicationOperationResult(id, e));
      }
    }
    return rejectApplicationsResult;
  }


  /**
   * Method to raise MFA exceptions in the given applications before starting the bulk operations.
   *
   * @param sess PerunSession
   * @param applicationsIds ids of the applications to check for MFA access
   * @param policy the policy name part that matches ^(?:vo-|group-)(.*), tldr whatever follows vo- or group- prefix
   */
  private void checkMFAForApplications(PerunSession sess, List<Integer> applicationsIds, String policy) {
    try {
      for (Integer id : applicationsIds) {
        Application application = getApplicationById(id);
        if (application == null) {
          continue;
        }

        if (application.getGroup() == null) {
          AuthzResolver.authorizedInternal(sess, "vo-" + policy,
              Collections.singletonList(application.getVo()));
        } else {
          AuthzResolver.authorizedInternal(sess, "group-" + policy,
              Arrays.asList(application.getVo(), application.getGroup()));
        }
      }
    } catch (MfaPrivilegeException | MfaTimeoutException | MfaRoleTimeoutException e) {
      throw e;
    } catch (Exception ignored) {
      // deal with this exception later in the bulk call
    }
  }

  /**
   * Forcefully set application its state (NEW/VERIFIED/...)
   *
   * @param sess     PerunSession
   * @param appId    ID of application
   * @param appState AppState to be set
   */
  private void setApplicationState(PerunSession sess, int appId, AppState appState) {
    try {
      jdbc.update(
          "update application set state=?, modified_at=" + Compatibility.getSysdate() + ", modified_by=? where id=?",
          appState.toString(), sess.getPerunPrincipal().getActor(), appId);
    } catch (RuntimeException ex) {
      LOG.error("Unable to set application state: {}, to application ID: {}", appState, appId, ex);
      throw new InternalErrorException("Unable to set application state: " + appState + " to application: " + appId,
          ex);
    }
  }

  @Override
  public void setAutoApproveErrorToApplication(Application application, String error) {
    jdbc.update("UPDATE application SET auto_approve_error=? WHERE id=?", error, application.getId());
  }

  public void setConsolidatorManager(ConsolidatorManager consolidatorManager) {
    this.consolidatorManager = consolidatorManager;
  }

  public void setDataSource(DataSource dataSource) {
    this.jdbc = new JdbcPerunTemplate(dataSource);
    this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    this.namedJdbc.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  /**
   * To the given EMBEDDED_GROUP_APPLICATION item, sets options of allowed groups. Example format:
   * 111#GroupA#ENABLED|222#GroupB#DISABLED
   *
   * @param sess              session
   * @param item              item, to which the group options will be set, only EMBEDDED_GROUP_APPLICATION is
   *                          supported
   * @param user
   * @param vo                vo, from which the groups for auto registration are taken
   * @param registrationGroup group, from which the subgroups for auto registration are taken - if not null, then it is
   *                          a group application
   */
  private void setGroupsToCheckBoxForGroups(PerunSession sess, ApplicationFormItemWithPrefilledValue item, User user,
                                            Vo vo, Group registrationGroup) {
    if (item.getFormItem().getType() != EMBEDDED_GROUP_APPLICATION) {
      throw new InternalErrorException("Group options can be set only to the EMBEDDED_GROUP_APPLICATION item.");
    }
    List<Group> groups;
    if (registrationGroup != null) {
      groups = perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, registrationGroup, item.getFormItem());
    } else {
      groups = perun.getGroupsManagerBl().getGroupsForAutoRegistration(sess, vo, item.getFormItem());
    }

    List<Group> userGroups;
    if (user != null) {
      userGroups = groupsManager.getGroupsWhereUserIsActiveMember(sess, user, vo);
    } else {
      userGroups = new ArrayList<>();
    }

    String groupOptions = null;
    if (!groups.isEmpty()) {
      // append DISABLED flag to indicate that user is already a member of this group for frontend purposes
      groupOptions = groups.stream().sorted(Comparator.comparing(Group::getName))
          .map(group -> group.getId() + "#" + group.getName() + (userGroups.contains(group) ? "#DISABLED" : "#ENABLED"))
          .collect(Collectors.joining("|"));
    }

    if (ApplicationFormItem.CS != null) {
      item.getFormItem().getI18n().get(ApplicationFormItem.CS).setOptions(groupOptions);
    }
    item.getFormItem().getI18n().get(ApplicationFormItem.EN).setOptions(groupOptions);
  }

  /**
   * If the given value is not null and not empty, put it in the given map with the given key.
   *
   * @param map   map
   * @param value value which is checked
   * @param key   key
   */
  private void setIfNotEmpty(Map<String, String> map, String value, String key) {
    if (value != null && !value.isEmpty()) {
      map.put(key, value);
    }
  }

  public void setRegistrarManager(RegistrarManager registrarManager) {
    this.registrarManager = registrarManager;
  }

  /**
   * Set user id to the application if it is not set already
   *
   * @param application
   * @param user
   */
  private void setUserForApplication(Application application, User user) {
    if (application.getUser() == null) {
      application.setUser(user);
      jdbc.update("update application set user_id=? where id=?", user.getId(), application.getId());
    }
  }

  /**
   * Store values from application data as user/member attributes
   * <p>
   * New values are set if old are empty, or merged if not empty. Empty new values are skipped (not even merged) as well
   * as core attributes.
   * <p>
   * User and Member must already exists !!
   * <p>
   * !! LOGIN ATTRIBUTES ARE SKIPPED BY THIS METHOD AND MUST BE SET LATER BY storeApplicationLoginAttributes() METHOD !!
   * !! USE unreserveNewLoginsFromSameNamespace() BEFORE DOING SO !!
   *
   * @param app Application to process attributes for
   * @throws UserNotExistsException                When User present in Application not exists
   * @throws InternalErrorException                When implementation fails
   * @throws PrivilegeException                    When caller is not authorized for some action
   * @throws MemberNotExistsException              When Member resolved from VO/User from Application doesn't exist
   * @throws VoNotExistsException                  When VO resolved from application doesn't exist
   * @throws RegistrarException                    When implementation fails
   * @throws AttributeNotExistsException           When expected attribute doesn't exists
   * @throws WrongAttributeAssignmentException     When attribute can't be stored because of wrongly passed params
   * @throws WrongAttributeValueException          When attribute can't be stored because of wrong value
   * @throws WrongReferenceAttributeValueException When attribute can't be stored because of some specific dynamic
   *                                               constraint (from attribute module)
   */
  private void storeApplicationAttributes(Application app)
      throws UserNotExistsException, PrivilegeException, MemberNotExistsException, VoNotExistsException,
      RegistrarException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {

    // user and member must exists if it's extension !!
    User user = usersManager.getUserById(registrarSession, app.getUser().getId());
    Member member = membersManager.getMemberByUser(registrarSession, app.getVo(), user);

    // get all app items
    List<ApplicationFormItemData> items = getApplicationDataById(registrarSession, app.getId());

    // attributes to set
    List<Attribute> attributes = new ArrayList<>();
    for (ApplicationFormItemData item : items) {
      String destAttr = item.getFormItem().getPerunDestinationAttribute();
      String newValue = item.getValue();

      // if correct destination attribute
      if (destAttr != null && !destAttr.isEmpty()) {
        // get attribute (for user and member only)
        Attribute a;
        if (destAttr.contains("urn:perun:user:")) {
          a = attrManager.getAttribute(registrarSession, user, destAttr);
        } else if (destAttr.contains("urn:perun:member:")) {
          a = attrManager.getAttribute(registrarSession, member, destAttr);
        } else {
          continue;
        }

        // do not store null or empty values at all with exception to boolean type
        if ((newValue == null || newValue.isEmpty()) && !a.getType().equalsIgnoreCase(Boolean.class.getName())) {
          continue;
        }

        // NEVER STORE LOGINS THIS WAY TO PREVENT ACCIDENTAL OVERWRITE
        if (a != null && "login-namespace".equals(a.getBaseFriendlyName())) {
          continue;
        }

        // if attribute exists
        if (a != null) {
          if (a.getType().equalsIgnoreCase(LinkedHashMap.class.getName())) {

            // we expect that map contains string keys and values
            LinkedHashMap<String, String> value = a.valueAsMap();
            value = handleMapValue(value, newValue);

            a.setValue(value);
            attributes.add(a);
          } else if (a.getType().equalsIgnoreCase(Boolean.class.getName())) {
            a.setValue(BeansUtils.stringToAttributeValue(newValue, Boolean.class.getName()));
            attributes.add(a);
          } else if (a.getType().equalsIgnoreCase(ArrayList.class.getName())) {

            // we expect that list contains strings
            ArrayList<String> value = a.valueAsList();
            value = handleArrayValue(value, newValue);

            a.setValue(value);
            attributes.add(a);
          } else {
            // other attributes are handled like strings
            a.setValue(newValue);
            attributes.add(a);
          }
        }
      }
    }

    // set attributes
    if (!attributes.isEmpty()) {
      // set them if not empty (member+user)
      attrManager.setAttributes(registrarSession, member, attributes, true);
    }

  }

  /**
   * Store only login attributes from application to user.
   * <p>
   * New values are set only if old are empty to prevent overwrite when joining identities. Empty new values are
   * skipped.
   * <p>
   * User must already exists !!
   *
   * @param app Application to process attributes for
   * @throws UserNotExistsException                When User present in Application not exists
   * @throws InternalErrorException                When implementation fails
   * @throws PrivilegeException                    When caller is not authorized for some action
   * @throws RegistrarException                    When implementation fails
   * @throws AttributeNotExistsException           When expected attribute doesn't exists
   * @throws WrongAttributeAssignmentException     When login can't be stored because of wrongly passed params
   * @throws WrongAttributeValueException          When login can't be stored because of wrong value
   * @throws WrongReferenceAttributeValueException When login can't be stored because of some specific dynamic
   *                                               constraint (from attribute module)
   */
  private void storeApplicationLoginAttributes(Application app)
      throws UserNotExistsException, PrivilegeException, RegistrarException, AttributeNotExistsException,
      WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {

    // user must exists
    User user = usersManager.getUserById(registrarSession, app.getUser().getId());

    // get all app items
    List<ApplicationFormItemData> items = getApplicationDataById(registrarSession, app.getId());

    // attributes to set
    List<Attribute> attributes = new ArrayList<>();
    for (ApplicationFormItemData item : items) {
      String destAttr = item.getFormItem().getPerunDestinationAttribute();
      String newValue = item.getValue();
      // do not store null or empty values at all
      if (newValue == null || newValue.isEmpty()) {
        continue;
      }
      // if correct destination attribute
      if (destAttr != null && !destAttr.isEmpty()) {
        // get login attribute (for user only)
        Attribute a;
        if (destAttr.contains(AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:")) {
          a = attrManager.getAttribute(registrarSession, user, destAttr);
        } else {
          continue;
        }

        // if attribute exists
        if (a != null) {
          if (isBlank(a.valueAsString())) {
            // set login attribute if initial (new) value
            a.setValue(newValue);
            attributes.add(a);
          }
          // skip if login already existed continue
        }
      }
    }

    // set attributes
    if (!attributes.isEmpty()) {
      // set them if not empty (user)
      attrManager.setAttributes(registrarSession, user, attributes);
    }

  }

  @Override
  public Application submitApplication(PerunSession session, Application application,
                                       List<ApplicationFormItemData> data) throws PerunException {
    int appId = processApplication(session, application, data);
    return getApplicationById(appId);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public Application submitApplication(PerunSession session, Application application,
                                       List<ApplicationFormItemData> data, UUID invitationToken) throws PerunException {
    // lock this invitation
    synchronized (processingInvitation) {
      if (processingInvitation.contains(invitationToken)) {
        throw new AlreadyProcessingException("Invitation identified by token " +
                                                 invitationToken + " is already being processed.");
      } else {
        processingInvitation.add(invitationToken);
      }
    }

    int appId;
    try {
      invitationsManagerBl.canInvitationBeAccepted(session, invitationToken, application.getGroup());
      Invitation invitation = invitationsManager.getInvitationByToken(session, invitationToken);

      appId = processApplication(session, application, data, invitation);
    } catch (Exception ex) {
      synchronized (processingInvitation) {
        processingInvitation.remove(invitationToken);
      }
      throw ex;
    }
    synchronized (processingInvitation) {
      processingInvitation.remove(invitationToken);
    }

    return getApplicationById(appId);
  }

  /**
   * Submits applications to all embedded groups
   *
   * @param sess
   * @param groups embedded groups
   * @param app
   * @throws PerunException
   */
  private void submitEmbeddedGroupApplications(PerunSession sess, List<Group> groups, Application app)
      throws PerunException {
    List<Integer> notEmbeddedGroupsIds = new ArrayList<>();
    User applicant = app.getUser();
    List<Group> filteredGroups = new ArrayList<>();
    List<ApplicationFormItemData> appFormData = getApplicationDataById(sess, app.getId());

    List<ApplicationFormItemData> formItems = new ArrayList<>();
    for (ApplicationFormItemData item : appFormData) {
      if (item.getFormItem().getType() == EMBEDDED_GROUP_APPLICATION) {
        formItems.add(item);
      }
    }

    List<Integer> formItemIds =
        formItems.stream().map(ApplicationFormItemData::getFormItem).map(ApplicationFormItem::getId)
            .collect(Collectors.toList());

    for (Group group : groups) {
      //skip if already member of group
      if (groupsManager.isUserMemberOfGroup(sess, applicant, group)) {
        continue;
      }

      //skip if another application to this group from user is waiting for approval
      List<Application> groupApplications =
          getApplicationsForGroup(sess, group, List.of(AppState.NEW.name(), AppState.VERIFIED.name()));
      if (groupApplications.stream().anyMatch(application -> applicant.equals(application.getUser()))) {
        continue;
      }

      //check if group is still embedded in application form
      if (!groupsManager.isGroupForAutoRegistration(sess, group, formItemIds)) {
        notEmbeddedGroupsIds.add(group.getId());
        continue;
      }

      filteredGroups.add(group);
    }

    if (!notEmbeddedGroupsIds.isEmpty()) {
      String groupStr = app.getGroup() != null ? ", group: " + app.getGroup().getId() : "";
      throw new GroupNotEmbeddedException(
          "Group(s) with ID(s): " + notEmbeddedGroupsIds + " not embedded for app items " + formItemIds +
          " (app - vo: " + app.getVo().getId() + groupStr + ")");
    }

    Map<Integer, String> failedGroups = new HashMap<>();

    for (Group group : filteredGroups) {
      try {
        Application groupApplication = new Application();
        groupApplication.setUser(applicant);
        groupApplication.setType(AppType.EMBEDDED);
        groupApplication.setVo(app.getVo());
        groupApplication.setGroup(group);
        groupApplication.setFedInfo(app.getFedInfo());
        groupApplication.setExtSourceName(app.getExtSourceName());
        groupApplication.setExtSourceType(app.getExtSourceType());
        groupApplication.setCreatedBy("Automatically generated");

        submitApplication(registrarSession, groupApplication, new ArrayList<>());
      } catch (Exception e) {
        LOG.error("Error submitting embedded application {}", e);
        failedGroups.put(group.getId(), e.getMessage());
      }
    }

    if (!failedGroups.isEmpty()) {
      throw new EmbeddedGroupApplicationSubmissionError(failedGroups);
    }
  }

  /**
   * Try to approve application if auto-approve is possible
   *
   * @param sess user who tries to approve application
   * @param app  application to be approved
   * @throws InternalErrorException
   */
  private void tryToAutoApproveApplication(PerunSession sess, Application app) throws PerunException {
    ApplicationForm form;

    if (app.getGroup() != null) {
      // group application
      form = getFormForGroup(app.getGroup());
    } else {
      // vo application
      form = getFormForVo(app.getVo());
    }

    AppType type = app.getType();

    // check whether the application is tied to a pre-approved invitation
    Invitation invitation = null;
    try {
      invitation = invitationsManager.getInvitationByApplication(sess, app);
    } catch (InvitationNotExistsException ignored) {
      // this just means that no invitation is tied to this application
    }

    if (!forceAutoApprove(sess, app) && invitation == null) {
      if (AppType.INITIAL.equals(type) && !form.isAutomaticApproval()) {
        return;
      }
      if (AppType.EXTENSION.equals(type) && !form.isAutomaticApprovalExtension()) {
        return;
      }
      if (AppType.EMBEDDED.equals(type) && !form.isAutomaticApprovalEmbedded()) {
        return;
      }
    }

    // do not auto-approve Group applications, if user is not member of VO
    if (app.getGroup() != null && app.getVo() != null) {
      try {
        if (app.getUser() == null) {
          LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
          PerunPrincipal applicationPrincipal =
              new PerunPrincipal(app.getCreatedBy(), app.getExtSourceName(), app.getExtSourceType(),
                  app.getExtSourceLoa(), additionalAttributes);
          User u = perun.getUsersManagerBl().getUserByExtSourceInformation(sess, applicationPrincipal);
          if (u != null) {
            membersManager.getMemberByUser(sess, app.getVo(), u);
          } else {
            // user not found or null, hence can't be member of VO -> do not approve.
            setAutoApproveErrorToApplication(app,
                "This application is waiting for approval of the VO application, which must be approved by the VO " +
                 "manager first. After that, this application will be automatically approved.");
            return;
          }
        } else {
          // user known, but maybe not member of a vo
          membersManager.getMemberByUser(sess, app.getVo(), app.getUser());
        }
      } catch (MemberNotExistsException | UserNotExistsException | UserExtSourceNotExistsException |
               ExtSourceNotExistsException ex) {
        setAutoApproveErrorToApplication(app,
            "This application is waiting for approval of the VO application, which must be approved by the VO manager" +
             " first. After that, this application will be automatically approved.");
        return;
      }
    }

    try {
      if (AppState.VERIFIED.equals(app.getState())) {
        // with registrar session, since only VO admin can approve application

        // check if can be approved (we normally call this manually from GUI before calling approve)
        canBeApproved(registrarSession, app);

        /*

                FIXME - temporarily disabled checking

                if (app.getUser() == null && !app.getExtSourceName().equalsIgnoreCase("LOCAL")) {
                    List<RichUser> list = checkForSimilarUsers(registrarSession, app.getId());
                    if (!list.isEmpty()) {
                        // found similar
                        throw new RegistrarException("Similar users are already registered in system. Automatic
                        approval of application was canceled to prevent creation of duplicate user entry. Please check
                         and approve application manually.");
                    } else {
                        // similar NOT found - continue
                        approveApplication(registrarSession, app.getId());
                    }
                } else { }

                */

        // other types of application doesn't create new user - continue
        String approver = invitation != null ? invitation.getCreatedBy() : sess.getPerunPrincipal().getActor();
        approveApplication(registrarSession, app.getId(), approver);
      }
    } catch (Exception ex) {
      setAutoApproveErrorToApplication(app, ex.getMessage());
      getMailManager().sendMessage(app, MailType.APP_ERROR_VO_ADMIN, null, List.of(ex));
      throw ex;
    }
  }

  /**
   * If the given map of attributes contains a user display name, it tries to match the given firstName and lastName and
   * find titles and middle name.
   *
   * @param candidate  candidate
   * @param attributes map of attributes with values
   * @param firstName  first name to match
   * @param lastName   last name to match
   */
  private void tryToParseTitlesAndMiddleName(Candidate candidate, Map<String, String> attributes, String firstName,
                                             String lastName) {
    if (containsNonEmptyValue(attributes, URN_USER_DISPLAY_NAME)) {
      String displayName = attributes.get(URN_USER_DISPLAY_NAME);
      parseTitlesAndMiddleName(candidate, displayName, firstName, lastName);
    }
  }

  /**
   * Tries to match the given pattern to the given display name. If it matches, its sets titles and middle name from
   * matcher of the given pattern to the given candidate.
   * <p>
   * This method expects the pattern to define 3 groups in order - 1. Titles before, 2. Middle name, 3. Titles after
   *
   * @param candidate   candidate
   * @param displayName display name
   * @param pattern     pattern with 3 matching groups
   * @return true, if the matcher matched
   */
  private boolean tryToParseTitlesAndMiddleNameFromPattern(Candidate candidate, String displayName, Pattern pattern,
                                                           String firstName) {
    Matcher matcher = pattern.matcher(displayName);
    if (!matcher.matches()) {
      return false;
    }
    if (matcher.groupCount() != 3) {
      throw new InternalErrorException(
          "Expected pattern with 3 groups to match - titles before, middle name and " + "titles after, but get " +
          matcher.groupCount() + " groups.");
    }

    // if the middle name equals to the first name placed in displayName (it can be firstName or lastName too)
    if (matcher.group(1).contains(firstName)) {
      parseTitlesBefore(candidate, matcher.group(1).split(firstName)[0].trim());
      parseMiddleName(candidate, firstName);
    } else {
      parseTitlesBefore(candidate, matcher.group(1).trim());
      parseMiddleName(candidate, matcher.group(2).trim());
    }
    parseTitlesAfter(candidate, matcher.group(3).trim());

    return true;
  }

  /**
   * Set application to VERIFIED state if all it's mails (VALIDATED_EMAIL) have assuranceLevel >= 1 and have non-empty
   * value (there is anything to validate) or if application includes only one email
   * associated with an accepted invitation. Returns TRUE if succeeded, FALSE if some mail still waits for verification.
   *
   * @param sess user who try to verify application
   * @param app  application to verify
   * @return TRUE if verified / FALSE if not verified
   * @throws InternalErrorException
   */
  private boolean tryToVerifyApplication(PerunSession sess, Application app) throws PerunException {

    // test all fields that may need to be validated and are not empty !!
    List<Integer> loas = jdbc.query("select d.assurance_level" + Compatibility.castToInteger() +
                                    " from application a, application_form_items i, application_data d " +
                                    "where d.app_id=a.id and d.item_id=i.id and a.id=? and i.type=? and d.value is " +
                                     "not null",
        new SingleColumnRowMapper<>(Integer.class), app.getId(), Type.VALIDATED_EMAIL.toString());

    boolean allValidated = true;
    for (Integer loa : loas) {
      // check on null only for backward compatibility, we now always set some value
      if (loa == null || loa < 1) {
        allValidated = false;
        break;
      }
    }

    if (allValidated) {
      // mark VERIFIED
      markApplicationVerified(sess, app.getId());
      app.setState(AppState.VERIFIED);
    }

    return allValidated;

  }

  // ------------------ MAPPERS AND SELECTS -------------------------------------

  /**
   * Unreserve new login/password from KDC if user already have login in same namespace
   * <p>
   * !! must be called before setting new attributes from application !!
   *
   * @param logins list of all new logins/namespaces pairs passed by application
   * @param user   user to check logins for
   * @return List of login/namespace pairs which are purely new and can be set to user and validated in KDC
   */
  private List<Pair<String, String>> unreserveNewLoginsFromSameNamespace(List<Pair<String, String>> logins, User user)
      throws PasswordDeletionFailedException, PasswordOperationTimeoutException, LoginNotExistsException,
      InvalidLoginException {

    List<Pair<String, String>> result = new ArrayList<>();

    List<Attribute> loginAttrs = perun.getAttributesManagerBl().getLogins(registrarSession, user);

    for (Pair<String, String> pair : logins) {
      boolean found = false;
      for (Attribute a : loginAttrs) {
        if (pair.getLeft().equals(a.getFriendlyNameParameter())) {
          // old login found in same namespace => unreserve new login from KDC
          usersManager.deletePassword(registrarSession, pair.getRight(), pair.getLeft());
          LOG.debug(
              "[REGISTRAR] Unreserving new login: {} in namespace: {} since user already have login: {} in same " +
               "namespace.",
              pair.getRight(), pair.getLeft(), a.getValue());
          found = true;
          break;
        }
      }
      if (!found) {
        // login is purely new
        result.add(pair);
      }
    }

    return result;

  }

  @Override
  public void updateApplicationType(PerunSession session, Application application) {

    // TODO - add authorization (and add to rpc)

    if (jdbc.update("update application set apptype=? where id=?", application.getType().toString(),
        application.getId()) > 0) {
      LOG.debug("Application type changed to + " + application.getType());
    }
  }

  public void updateApplicationUser(PerunSession sess, Application app) {

    jdbc.update(
        "update application set user_id=?, modified_at=" + Compatibility.getSysdate() + ", modified_by=? where id=?",
        (app.getUser() != null) ? app.getUser().getId() : null, sess.getPerunPrincipal().getActor(), app.getId());

  }

  @Override
  public int updateForm(PerunSession user, ApplicationForm form)
      throws PrivilegeException, VoNotExistsException, GroupNotExistsException {

    form.setVo(this.vosManager.getVoById(user, form.getVo().getId()));
    //Authorization
    if (form.getGroup() == null) {
      // VO application
      if (!AuthzResolver.authorizedInternal(user, "vo-updateForm_ApplicationForm_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(user, "updateForm");
      }
    } else {
      form.setGroup(this.groupsManager.getGroupById(user, form.getGroup().getId()));
      if (!AuthzResolver.authorizedInternal(user, "group-updateForm_ApplicationForm_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(user, "updateForm");
      }
    }

    perun.getAuditer().log(user, new FormUpdated((form)));
    return jdbc.update(
        "update application_form set automatic_approval=?, automatic_approval_extension=?, " +
         "automatic_approval_embedded=?, module_names=? where id=?",
        form.isAutomaticApproval(), form.isAutomaticApprovalExtension(), form.isAutomaticApprovalEmbedded(),
        String.join(",", form.getModuleClassNames()), form.getId());
  }

  @Override
  public void updateFormItem(PerunSession sess, ApplicationFormItem item)
      throws PrivilegeException, FormNotExistsException {

    ApplicationForm form;

    // check authz on form
    try {
      form = getFormByItemId(sess, item.getId());
    } catch (PrivilegeException ex) {
      throw new PrivilegeException(sess, "updateFormItemById");
    }

    //Authorization
    if (form.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItem_ApplicationFormItem_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItem_ApplicationFormItem_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    }

    // else update form item

    int result = jdbc.update("update application_form_items set ordnum=?,shortname=?,required=?,type=?,fed_attr=?," +
                             "src_attr=?,dst_attr=?,regex=?,updatable=?,hidden=?::app_item_hidden," +
                              "disabled=?::app_item_disabled,hidden_dependency_item_id=?," +
                               "disabled_dependency_item_id=? where id=?",
        item.getOrdnum(), item.getShortname(), item.isRequired(), item.getType().toString(),
        item.getFederationAttribute(), item.getPerunSourceAttribute(), item.getPerunDestinationAttribute(),
        item.getRegex(), item.isUpdatable(), item.getHidden().toString(), item.getDisabled().toString(),
        item.getHiddenDependencyItemId(), item.getDisabledDependencyItemId(), item.getId());

    // update form item texts (easy way = delete and new insert)

    // delete
    jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
    // insert new
    for (Locale locale : item.getI18n().keySet()) {
      ItemTexts itemTexts = item.getTexts(locale);
      jdbc.update(
          "insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?," +
           "?,?)",
          item.getId(), locale.getLanguage(), itemTexts.getLabel(), itemTexts.getOptions(), itemTexts.getHelp(),
          itemTexts.getErrorMessage());
    }

    // update form item app types (easy way = delete and new insert)
    // first check if there is EMBEDDED application type
    if (item.getApplicationTypes().contains(AppType.EMBEDDED)) {
      throw new InternalErrorException("It is not possible to add form items to EMBEDDED application type.");
    }
    // delete
    jdbc.update("delete from application_form_item_apptypes where item_id=?", item.getId());
    // insert new
    for (AppType appType : item.getApplicationTypes()) {
      jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)", item.getId(),
          appType.toString());
    }

    perun.getAuditer().log(sess, new FormItemUpdated(form, item));

  }

  public void updateFormItemData(PerunSession sess, int appId, ApplicationFormItemData data)
      throws RegistrarException, PrivilegeException {


    Application app = getApplicationById(sess, appId);
    if (AppState.APPROVED.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) {
      throw new RegistrarException("Form items of once approved or rejected applications can't be modified.");
    }

    //Authorization
    if (app.getGroup() == null) {
      if ((!AuthzResolver.authorizedInternal(sess, "updateFormItemData_int_ApplicationFormItemData_policy",
          app.getVo()))) {
        throw new PrivilegeException(sess, "updateFormItemData");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "updateFormItemData_int_ApplicationFormItemData_policy",
          app.getGroup())) {
        throw new PrivilegeException(sess, "updateFormItemData");
      }
    }

    ApplicationFormItemData existingData = getFormItemDataById(data.getId(), appId);
    if (existingData == null) {
      throw new RegistrarException(
          "Form item data specified by ID: " + data.getId() + " not found or doesn't belong to the application " +
          appId);
    }

    List<Type> notAllowed = Arrays.asList(USERNAME, PASSWORD, HEADING, HTML_COMMENT, SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);

    if (notAllowed.contains(existingData.getFormItem().getType())) {
      throw new RegistrarException(
          "You are not allowed to modify " + existingData.getFormItem().getType() + " type of form items.");
    }

    if (!existingData.getFormItem().isUpdatable()) {
      throw new RegistrarException(
          "The item " + existingData.getFormItem().getShortname() + " is not allowed to be updated.");
    }
    updateFormItemData(sess, data);

  }

  private void updateFormItemData(PerunSession session, ApplicationFormItemData dataItem) {
    try {
      if (VALIDATED_EMAIL.equals(dataItem.getFormItem().getType())) {
        handleLoaForValidatedMail(session, dataItem);
      }
      int result =
          jdbc.update("update application_data set value=? , assurance_level=? where id=?", dataItem.getValue(),
              ((isBlank(dataItem.getAssuranceLevel())) ? null : dataItem.getAssuranceLevel()), dataItem.getId());
      LOG.info("{} manually updated form item data {}", session.getPerunPrincipal(), dataItem);
      if (result != 1) {
        throw new InternalErrorException("Unable to update form item data");
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void updateFormItemTexts(PerunSession sess, ApplicationFormItem item, Locale locale)
      throws PrivilegeException, FormNotExistsException {

    ApplicationForm form;

    try {
      form = getFormByItemId(sess, item.getId());
    } catch (PrivilegeException ex) {
      throw new PrivilegeException(sess, "updateFormItemTexts");
    }

    //Authorization
    if (form.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItemTexts_ApplicationFormItem_Locale_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItemTexts_ApplicationFormItem_Locale_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    }

    ItemTexts texts = item.getTexts(locale);
    jdbc.update(
        "update application_form_item_texts set label=?,options=?,help=?,error_message=? where item_id=? and locale=?",
        texts.getLabel(), texts.getOptions(), texts.getHelp(), texts.getErrorMessage(), item.getId(),
        locale.getLanguage());

  }

  @Override
  public void updateFormItemTexts(PerunSession sess, ApplicationFormItem item)
      throws PrivilegeException, FormNotExistsException {

    ApplicationForm form;

    // check authz on form
    try {
      form = getFormByItemId(sess, item.getId());
    } catch (PrivilegeException ex) {
      throw new PrivilegeException(sess, "updateFormItemById");
    }

    //Authorization
    if (form.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItemTexts_ApplicationFormItem_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItemTexts_ApplicationFormItem_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(sess, "updateFormItemById");
      }
    }

    // update form item texts (easy way = delete and new insert)

    // delete
    jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
    // insert new
    for (Locale locale : item.getI18n().keySet()) {
      ItemTexts itemTexts = item.getTexts(locale);
      jdbc.update(
          "insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?," +
           "?,?)",
          item.getId(), locale.getLanguage(), itemTexts.getLabel(), itemTexts.getOptions(), itemTexts.getHelp(),
          itemTexts.getErrorMessage());
    }

  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int updateFormItems(PerunSession sess, ApplicationForm form, List<ApplicationFormItem> items)
      throws PerunException {
    //Authorization
    if (form.getGroup() == null) {
      // VO application
      if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItems_ApplicationForm_List<ApplicationFormItem>_policy",
          Collections.singletonList(form.getVo()))) {
        throw new PrivilegeException(sess, "updateFormItems");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess,
          "group-updateFormItems_ApplicationForm_List<ApplicationFormItem>_policy",
          Arrays.asList(form.getVo(), form.getGroup()))) {
        throw new PrivilegeException(sess, "updateFormItems");
      }
    }

    if (items == null) {
      throw new NullPointerException("ApplicationFormItems to update can't be null");
    }

    if (countEmbeddedGroupFormItems(items) > 1) {
      throw new MultipleApplicationFormItemsException(
          "Multiple definitions of embedded groups. Only one definition is allowed.");
    }

    if (!validateSubmitButtonPresence(items)) {
      throw new MissingSubmitButtonException(
          "Application form contains at least one input field, but no submit or auto-submit button.");
    }

    if (items.stream().filter(i -> i.getId() > 0)
        .anyMatch(i -> !checkItemBelongsToForm(sess, form.getId(), i.getId()))) {
      throw new IllegalArgumentException(
          String.format("Form items do not belong to %s's application form", form.getGroup() == null ? "vo" : "group"));
    }

    //map storing [temporaryId : savedId] to enable fixing invalid dependencies on temporary ids
    Map<Integer, Integer> temporaryToSaved = new HashMap<>();
    //map storing [temporaryId : disabledDependencyTemporaryId]
    Map<Integer, Integer> temporaryToDisabled = new HashMap<>();
    //map storing [temporaryId : hiddenDependencyTemporaryId]
    Map<Integer, Integer> temporaryToHidden = new HashMap<>();

    prepareIdsMapping(items, temporaryToSaved, temporaryToDisabled, temporaryToHidden);

    List<String> openStates = List.of(AppState.NEW.toString(), AppState.VERIFIED.toString());
    List<Application> existingOpenApplications =
        form.getGroup() == null ? getApplicationsForVo(sess, form.getVo(), openStates, false) :
            getApplicationsForGroup(sess, form.getGroup(), openStates);

    int finalResult = 0;
    for (ApplicationFormItem item : items) {
      // if creating or updating password item check that it has destination attribute set
      if (item.getType() == PASSWORD && !item.isForDelete()) {
        if (item.getPerunDestinationAttribute() == null || item.getPerunDestinationAttribute().isEmpty()) {
          throw new FormItemSetupException("The form item of type PASSWORD needs to have destination attribute " +
              "assigned.");
        }

        AttributeDefinition destinationAttrDef;
        try {
          destinationAttrDef = perun.getAttributesManagerBl().getAttributeDefinition(
              sess, item.getPerunDestinationAttribute());
        } catch (AttributeNotExistsException e) {
          throw new FormItemSetupException("The destination attribute used: " + item.getPerunDestinationAttribute() +
              " does not exist. Please create it or use another.");
        }

        if (!(perun.getAttributesManagerBl().isDefAttribute(sess, destinationAttrDef) ||
            perun.getAttributesManagerBl().isOptAttribute(sess, destinationAttrDef))) {
          throw new FormItemSetupException("The destination attribute used: " + item.getPerunDestinationAttribute() +
              " needs to be of def or opt type. Please modify the attribute accordingly.");
        }
      }

      // is item to create ? => create
      if (item.getId() <= 0 && !item.isForDelete()) {
        int temporaryId = item.getId();
        // Check if newly created item contains allowed tags (in case of Header and HTML Comment
        if (item.getType() == HTML_COMMENT || item.getType() == HEADING || item.getType() == CHECKBOX) {
          Map<Locale, ItemTexts> i18n = item.getI18n();
          for (Locale locale : item.getI18n().keySet()) {
            ItemTexts itemTexts = item.getTexts(locale);
            if (item.getType() == CHECKBOX) {
              // Labels for each checkbox option are stored in options, but allow html for classic labels as well
              boolean isSafe = new HTMLParser().isCheckboxLabelSafe(itemTexts.getOptions()) &&
                               new HTMLParser().isCheckboxLabelSafe(itemTexts.getLabel());
              if (!isSafe) {
                throw new InvalidHtmlInputException("HTML content in '" + item.getShortname() +
                                                    "' checkbox label contains unsafe HTML tags or styles. Remove " +
                                                     "them and try again. Only <a> elements with 'href' and 'target' " +
                                                      "attributes are allowed");
              }
            } else {
              HTMLParser parser = new HTMLParser().sanitizeHTML(itemTexts.getLabel()).checkEscapedHTML();
              if (!parser.isInputValid()) {
                throw new InvalidHtmlInputException("HTML content in '" + item.getShortname() +
                                                    "' contains unsafe HTML tags or styles. Remove them and try again.",
                    parser.getEscaped());
              }
              itemTexts.setLabel(parser.getEscapedHTML());
            }
            i18n.put(locale, itemTexts);
          }
          item.setI18n(i18n);
        }

        ApplicationFormItem savedItem = addFormItem(sess, form, item);
        if (savedItem != null) {
          finalResult++;
          temporaryToSaved.put(temporaryId, savedItem.getId()); // override with saved id
        }
        continue;
      }

      // is item for deletion ? => delete on cascade
      if (item.isForDelete()) {
        finalResult += jdbc.update("delete from application_form_items where id=?", item.getId());
        continue; // continue to next item
      }

      String oldname =
          jdbc.queryForObject("select shortname from application_form_items where id=" + item.getId(), String.class);

      // else update form item

      // Check that if the destination attribute was changed then there are no open applications
      if (!Objects.equals(getFormItemById(sess, item.getId()).getPerunDestinationAttribute(),
          item.getPerunDestinationAttribute()) && !existingOpenApplications.isEmpty()) {
        throw new OpenApplicationExistsException(
            "It is impossible to change the destination attribute of the form item while some open applications still" +
             " exist. First, please resolve these applications.");
      }

      int result = jdbc.update("update application_form_items set ordnum=?,shortname=?,required=?,type=?," +
                               "fed_attr=?,src_attr=?,dst_attr=?,regex=?,updatable=?,hidden=?::app_item_hidden," +
                                "disabled=?::app_item_disabled,hidden_dependency_item_id=?," +
                               "disabled_dependency_item_id=? where id=?", item.getOrdnum(), item.getShortname(),
          item.isRequired(), item.getType().toString(), item.getFederationAttribute(), item.getPerunSourceAttribute(),
          item.getPerunDestinationAttribute(), item.getRegex(), item.isUpdatable(), item.getHidden().toString(),
          item.getDisabled().toString(), item.getHiddenDependencyItemId(), item.getDisabledDependencyItemId(),
          item.getId());
      finalResult += result;
      if (result == 0) {
        // skip whole set if not found for update
        continue;
      }

      // did shortname change? => update open applications' data
      if (oldname != null && !oldname.equals(item.getShortname())) {
        jdbc.update("update application_data set shortname=? where item_id=? and shortname=? and app_id in (" +
                    "select app_id from application where application.state in (?,?))", item.getShortname(),
            item.getId(), oldname, AppState.VERIFIED.toString(), AppState.NEW.toString());
      }

      // update form item texts (easy way = delete and new insert)

      // delete
      jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
      // insert new
      for (Locale locale : item.getI18n().keySet()) {
        ItemTexts itemTexts = item.getTexts(locale);
        // Escape HTML_COMMENT and HEADING attributes
        if (item.getType() == HTML_COMMENT || item.getType() == HEADING) {
          // Check if html text contains invalid tags (subject and text)
          HTMLParser parser = new HTMLParser().sanitizeHTML(itemTexts.getLabel()).checkEscapedHTML();
          if (!parser.isInputValid()) {
            throw new InvalidHtmlInputException("HTML content in '" + item.getShortname() +
                                                "' contains unsafe HTML tags or styles. Remove them and try again.",
                parser.getEscaped());
          }
          itemTexts.setLabel(parser.getEscapedHTML());
        }
        if (item.getType() == CHECKBOX) {
          // Labels for each checkbox option are stored in options, but allow html for classic labels as well
          boolean isSafe = new HTMLParser().isCheckboxLabelSafe(itemTexts.getOptions()) &&
                           new HTMLParser().isCheckboxLabelSafe(itemTexts.getLabel());
          if (!isSafe) {
            throw new InvalidHtmlInputException("HTML content in '" + item.getShortname() +
                                                "' checkbox label contains unsafe HTML tags or styles. Remove them " +
"and try again. Only <a> elements with 'href' and 'target' " +
                                                  "attributes are allowed");
          }
        }

        jdbc.update(
            "insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?," +
             "?,?,?)",
            item.getId(), locale.getLanguage(), itemTexts.getLabel(), itemTexts.getOptions(), itemTexts.getHelp(),
            itemTexts.getErrorMessage());
      }

      // update form item app types (easy way = delete and new insert)
      // first check if there is EMBEDDED application type
      if (item.getApplicationTypes().contains(AppType.EMBEDDED)) {
        throw new InternalErrorException("It is not possible to add form items to EMBEDDED application type.");
      }
      // delete
      jdbc.update("delete from application_form_item_apptypes where item_id=?", item.getId());
      // insert new
      for (AppType appType : item.getApplicationTypes()) {
        jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)", item.getId(),
            appType.toString());
      }
    }

    fixDependencies(form, temporaryToSaved, temporaryToDisabled, temporaryToHidden);

    perun.getAuditer().log(sess, new FormItemsUpdated(form));
    // return number of updated rows
    return finalResult;

  }

  @Transactional(rollbackFor = Exception.class)
  public void updateFormItemsData(PerunSession sess, int appId, List<ApplicationFormItemData> data)
      throws PerunException {

    Application app = getApplicationById(appId);

    if (app == null) {
      throw new InternalErrorException("Application with ID=" + appId + " doesn't exist.");
    }

    if (!AuthzResolver.selfAuthorizedForApplication(sess, app)) {
      throw new PrivilegeException(sess, "updateFormItemsData");
    }

    if (AppState.APPROVED.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) {
      throw new RegistrarException("Form items of once approved or rejected applications can't be modified.");
    }

    // no data to change
    if (data == null || data.isEmpty()) {
      return;
    }

    for (ApplicationFormItemData dataItem : data) {

      ApplicationFormItemData existingData = getFormItemDataById(dataItem.getId(), appId);
      if (existingData == null) {
        throw new RegistrarException(
            "Form item data specified by ID: " + dataItem.getId() + " not found or doesn't belong to the application " +
            appId);
      }

      List<Type> notAllowed =
          Arrays.asList(USERNAME, PASSWORD, HEADING, HTML_COMMENT, SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);

      if (notAllowed.contains(existingData.getFormItem().getType())) {
        throw new RegistrarException(
            "You are not allowed to modify " + existingData.getFormItem().getType() + " type of form items.");
      }
      if (!existingData.getFormItem().isUpdatable()) {
        throw new RegistrarException(
            "The item " + existingData.getFormItem().getShortname() + " is not allowed to be updated.");
      }
      updateFormItemData(sess, dataItem);

    }

    // forcefully mark application as NEW and perform verification
    setApplicationState(sess, app.getId(), AppState.NEW);

    // in case that user fixed own form, it should be possible to verify and approve it for auto-approval cases
    boolean verified = tryToVerifyApplication(sess, app);
    if (verified) {
      // try to APPROVE if auto approve
      tryToAutoApproveApplication(sess, app);
    } else {
      // send request validation notification
      getMailManager().sendMessage(app, MailType.MAIL_VALIDATION, null, null);
    }

  }

  /**
   * If titles before / after name are part of application form and User exists, update titles for user according to
   * application.
   * <p>
   * This method doesn't clear titles from users name if sent empty in order to prevent accidental removal when user
   * log-in with different IDP without titles provided.
   *
   * @param app Application to update user's titles for.
   */
  private void updateUserNameTitles(Application app) {

    try {

      User user = usersManager.getUserById(registrarSession, app.getUser().getId());
      List<ApplicationFormItemData> data = registrarManager.getApplicationDataById(registrarSession, app.getId());
      boolean found = false;

      // first check for display name
      for (ApplicationFormItemData item : data) {
        if (URN_USER_DISPLAY_NAME.equals(item.getFormItem().getPerunDestinationAttribute())) {
          if (item.getValue() != null && !item.getValue().isEmpty()) {
            Map<String, String> commonName = Utils.parseCommonName(item.getValue());
            if (commonName.get("titleBefore") != null && !commonName.get("titleBefore").isEmpty()) {
              user.setTitleBefore(commonName.get("titleBefore"));
              found = true;
            }
            if (commonName.get("titleAfter") != null && !commonName.get("titleAfter").isEmpty()) {
              user.setTitleAfter(commonName.get("titleAfter"));
              found = true;
            }
          }
          break;
        }
      }

      // overwrite by specific before/after name title
      for (ApplicationFormItemData item : data) {
        if (URN_USER_TITLE_BEFORE.equals(item.getFormItem().getPerunDestinationAttribute())) {
          if (item.getValue() != null && !item.getValue().isEmpty()) {
            user.setTitleBefore(item.getValue());
            found = true;
          }
        }
        if (URN_USER_TITLE_AFTER.equals(item.getFormItem().getPerunDestinationAttribute())) {
          if (item.getValue() != null && !item.getValue().isEmpty()) {
            user.setTitleAfter(item.getValue());
            found = true;
          }
        }
      }

      // titles were part of application form
      if (found) {
        LOG.debug("[REGISTRAR] User to update titles: {}", user);
        usersManager.updateNameTitles(registrarSession, user);
      }

    } catch (Exception ex) {
      LOG.error("[REGISTRAR] Exception when updating titles.", ex);
    }

  }

  @Override
  public boolean validateEmailFromLink(Map<String, String> urlParameters) throws PerunException {

    String idStr = urlParameters.get("i");
    byte[] mac = mailManager.getMessageAuthenticationCode(idStr).getBytes(StandardCharsets.UTF_8);
    byte[] m = urlParameters.get("m").getBytes(StandardCharsets.UTF_8);
    if (MessageDigest.isEqual(mac, m)) {
      int appDataId = Integer.parseInt(idStr, Character.MAX_RADIX);
      // validate mail
      jdbc.update("update application_data set assurance_level=1 where id = ?", appDataId);
      Application app =
          getApplicationById(jdbc.queryForInt("select app_id from application_data where id = ?", appDataId));
      if (app == null) {
        LOG.warn("Application for FormItemData ID: {} doesn't exists and therefore mail can't be verified.", appDataId);
        throw new RegistrarException("Application doesn't exists and therefore mail can't be verified.");
      }

      // if application is already approved or rejected, fake OK on mail validation and do nothing
      if (Arrays.asList(AppState.APPROVED, AppState.REJECTED).contains(app.getState())) {
        return true;
      }

      boolean verified = AppState.VERIFIED.equals(app.getState());
      if (AppState.NEW.equals(app.getState())) {
        // try to verify only new applications
        verified = tryToVerifyApplication(registrarSession, app);
      }
      if (verified) {
        // try to APPROVE only verified and only if auto approve
        try {
          tryToAutoApproveApplication(registrarSession, app);
        } catch (PerunException ex) {
          // when approval fails, we want this to be silently skipped, since for "user" called method did verified
          // his mail address.
          LOG.warn("We couldn't auto-approve application {}, because of error: {}", app, ex);
        }
      }
      return true;
    }
    return false;

  }

  @Override
  public Application verifyApplication(PerunSession sess, int appId) throws PerunException {

    Application app = getApplicationById(appId);
    if (app == null) {
      throw new RegistrarException("Application with ID=" + appId + " doesn't exists.");
    }

    //Authorization
    if (app.getGroup() == null) {
      if (!AuthzResolver.authorizedInternal(sess, "vo-verifyApplication_int_policy",
          Collections.singletonList(app.getVo()))) {
        throw new PrivilegeException(sess, "verifyApplication");
      }
    } else {
      if (!AuthzResolver.authorizedInternal(sess, "group-verifyApplication_int_policy",
          Arrays.asList(app.getVo(), app.getGroup()))) {
        throw new PrivilegeException(sess, "verifyApplication");
      }
    }

    // proceed
    markApplicationVerified(sess, appId);
    perun.getAuditer().log(sess, new ApplicationVerified(app));
    // return updated application
    return getApplicationById(appId);

  }

}
