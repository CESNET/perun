package cz.metacentrum.perun.registrar.impl;

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
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.registrar.ConsolidatorManager;
import cz.metacentrum.perun.registrar.exceptions.*;
import cz.metacentrum.perun.registrar.model.Identity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.bl.PerunBl;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.Application.AppState;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem.ItemTexts;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;
import cz.metacentrum.perun.registrar.MailManager;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;

import static cz.metacentrum.perun.core.api.GroupsManager.GROUPSYNCHROENABLED_ATTRNAME;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.*;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

	private final static Logger log = LoggerFactory.getLogger(RegistrarManagerImpl.class);
	private final static Set<String> extSourcesWithMultipleIdentifiers = BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();

	// identifiers for selected attributes
	private static final String URN_USER_TITLE_BEFORE = "urn:perun:user:attribute-def:core:titleBefore";
	private static final String URN_USER_TITLE_AFTER = "urn:perun:user:attribute-def:core:titleAfter";
	private static final String URN_USER_FIRST_NAME = "urn:perun:user:attribute-def:core:firstName";
	static final String URN_USER_LAST_NAME = "urn:perun:user:attribute-def:core:lastName";
	private static final String URN_USER_MIDDLE_NAME = "urn:perun:user:attribute-def:core:middleName";
	static final String URN_USER_DISPLAY_NAME = "urn:perun:user:attribute-def:core:displayName";

	private static final String DISPLAY_NAME_VO_FROM_EMAIL = "\"From\" email address";
	private static final String FRIENDLY_NAME_VO_FROM_EMAIL = "fromEmail";
	private static final String NAMESPACE_VO_FROM_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_FROM_EMAIL = NAMESPACE_VO_FROM_EMAIL  + ":" + FRIENDLY_NAME_VO_FROM_EMAIL;

	private static final String DISPLAY_NAME_VO_TO_EMAIL = "\"To\" email addresses";
	private static final String FRIENDLY_NAME_VO_TO_EMAIL = "toEmail";
	private static final String NAMESPACE_VO_TO_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_TO_EMAIL = NAMESPACE_VO_TO_EMAIL + ":" +  FRIENDLY_NAME_VO_TO_EMAIL;

	private static final String DISPLAY_NAME_GROUP_TO_EMAIL = "\"To\" email addresses";
	private static final String FRIENDLY_NAME_GROUP_TO_EMAIL = "toEmail";
	private static final String NAMESPACE_GROUP_TO_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_TO_EMAIL = NAMESPACE_GROUP_TO_EMAIL + ":" +  FRIENDLY_NAME_GROUP_TO_EMAIL;

	private static final String DISPLAY_NAME_GROUP_FROM_EMAIL = "\"From\" email address";
	private static final String FRIENDLY_NAME_GROUP_FROM_EMAIL = "fromEmail";
	private static final String NAMESPACE_GROUP_FROM_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_FROM_EMAIL = NAMESPACE_GROUP_FROM_EMAIL + ":" +  FRIENDLY_NAME_GROUP_FROM_EMAIL;

	private static final String DISPLAY_NAME_GROUP_FROM_NAME_EMAIL = "\"From\" name";
	private static final String FRIENDLY_NAME_GROUP_FROM_NAME_EMAIL = "fromNameEmail";
	private static final String NAMESPACE_GROUP_FROM_NAME_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_FROM_NAME_EMAIL = NAMESPACE_GROUP_FROM_EMAIL + ":" +  FRIENDLY_NAME_GROUP_FROM_NAME_EMAIL;

	private static final String DISPLAY_NAME_VO_FROM_NAME_EMAIL = "\"From\" name";
	private static final String FRIENDLY_NAME_VO_FROM_NAME_EMAIL = "fromNameEmail";
	private static final String NAMESPACE_VO_FROM_NAME_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_FROM_NAME_EMAIL = NAMESPACE_VO_FROM_EMAIL + ":" +  FRIENDLY_NAME_VO_FROM_NAME_EMAIL;

	private static final String DISPLAY_NAME_VO_LANGUAGE_EMAIL = "Notification default language";
	private static final String FRIENDLY_NAME_VO_LANGUAGE_EMAIL = "notificationsDefLang";
	private static final String NAMESPACE_VO_LANGUAGE_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_LANGUAGE_EMAIL = NAMESPACE_VO_LANGUAGE_EMAIL  + ":" + FRIENDLY_NAME_VO_LANGUAGE_EMAIL;

	private static final String DISPLAY_NAME_GROUP_LANGUAGE_EMAIL = "Notification default language";
	private static final String FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL = "notificationsDefLang";
	private static final String NAMESPACE_GROUP_LANGUAGE_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_LANGUAGE_EMAIL = NAMESPACE_GROUP_LANGUAGE_EMAIL + ":" +  FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL;

	private static final String DISPLAY_NAME_VO_APPLICATION_URL = "Application form URL";
	private static final String FRIENDLY_NAME_VO_APPLICATION_URL = "applicationURL";
	private static final String NAMESPACE_VO_APPLICATION_URL = AttributesManager.NS_VO_ATTR_DEF;
	private static final String URN_VO_APPLICATION_URL = NAMESPACE_VO_APPLICATION_URL  + ":" + FRIENDLY_NAME_VO_APPLICATION_URL;

	private static final String DISPLAY_NAME_GROUP_APPLICATION_URL = "Application form URL";
	private static final String FRIENDLY_NAME_GROUP_APPLICATION_URL = "applicationURL";
	private static final String NAMESPACE_GROUP_APPLICATION_URL = AttributesManager.NS_GROUP_ATTR_DEF;
	private static final String URN_GROUP_APPLICATION_URL = NAMESPACE_GROUP_APPLICATION_URL + ":" +  FRIENDLY_NAME_GROUP_APPLICATION_URL;

	private static final String DISPLAY_NAME_VO_REGISTRAR_URL = "Registrar URL";
	private static final String FRIENDLY_NAME_VO_REGISTRAR_URL = "registrarURL";
	private static final String NAMESPACE_VO_REGISTRAR_URL = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_REGISTRAR_URL = NAMESPACE_VO_REGISTRAR_URL  + ":" + FRIENDLY_NAME_VO_REGISTRAR_URL;

	private static final String DISPLAY_NAME_GROUP_REGISTRAR_URL = "Registrar URL";
	private static final String FRIENDLY_NAME_GROUP_REGISTRAR_URL = "registrarURL";
	private static final String NAMESPACE_GROUP_REGISTRAR_URL = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_REGISTRAR_URL = NAMESPACE_GROUP_REGISTRAR_URL + ":" +  FRIENDLY_NAME_GROUP_REGISTRAR_URL;

	private static final String DISPLAY_NAME_VO_MAIL_FOOTER = "Mail Footer";
	private static final String FRIENDLY_NAME_VO_MAIL_FOOTER = "mailFooter";
	private static final String NAMESPACE_VO_MAIL_FOOTER = AttributesManager.NS_VO_ATTR_DEF;
	static final String URN_VO_MAIL_FOOTER = NAMESPACE_VO_MAIL_FOOTER + ":" + FRIENDLY_NAME_VO_MAIL_FOOTER;

	private static final String DISPLAY_NAME_GROUP_MAIL_FOOTER = "Mail Footer";
	private static final String FRIENDLY_NAME_GROUP_MAIL_FOOTER = "mailFooter";
	private static final String NAMESPACE_GROUP_MAIL_FOOTER = AttributesManager.NS_GROUP_ATTR_DEF;
	static final String URN_GROUP_MAIL_FOOTER = NAMESPACE_GROUP_MAIL_FOOTER + ":" + FRIENDLY_NAME_GROUP_MAIL_FOOTER;

	private static final String MODULE_PACKAGE_PATH = "cz.metacentrum.perun.registrar.modules.";

	@Autowired PerunBl perun;
	@Autowired MailManager mailManager;
	@Autowired ConsolidatorManager consolidatorManager;
	private RegistrarManager registrarManager;
	private PerunSession registrarSession;
	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate namedJdbc;
	private AttributesManagerBl attrManager;
	private MembersManagerBl membersManager;
	private GroupsManagerBl groupsManager;
	private UsersManagerBl usersManager;
	private VosManagerBl vosManager;

	// federation attribute name constants
	private static final String shibDisplayNameVar = "displayName";
	private static final String shibCommonNameVar = "cn";
	private static final String shibFirstNameVar = "givenName";
	private static final String shibLastNameVar = "sn";

	// regular expression to match alfanumeric contents
	private static final Pattern alnumPattern = Pattern.compile(".*\\p{Alnum}+.*", Pattern.UNICODE_CHARACTER_CLASS);
	
	private final Set<String> runningCreateApplication = new HashSet<>();
	private final Set<Integer> runningApproveApplication = new HashSet<>();
	private final Set<Integer> runningRejectApplication = new HashSet<>();
	private final Set<Integer> runningDeleteApplication = new HashSet<>();

	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
		this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		this.namedJdbc.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	public void setRegistrarManager(RegistrarManager registrarManager) {
		this.registrarManager = registrarManager;
	}

	public void setConsolidatorManager(ConsolidatorManager consolidatorManager) {
		this.consolidatorManager = consolidatorManager;
	}

	protected void initialize() throws PerunException {

		// gets session for a system principal "perunRegistrar"
		final PerunPrincipal pp = new PerunPrincipal("perunRegistrar",
				ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
		}
		try {
			attrManager.getAttributeDefinition(registrarSession, URN_VO_REGISTRAR_URL);
		} catch (AttributeNotExistsException ex) {
			// create attr if not exists
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setDisplayName(DISPLAY_NAME_VO_REGISTRAR_URL);
			attrDef.setFriendlyName(FRIENDLY_NAME_VO_REGISTRAR_URL);
			attrDef.setNamespace(NAMESPACE_VO_REGISTRAR_URL);
			attrDef.setDescription("Custom URL used in registration notifications (hostname without any parameters like: https://hostname.domain/). If not set, default hostname of Perun instance is used.");
			attrDef.setType(String.class.getName());
			attrDef = attrManager.createAttribute(registrarSession, attrDef);
			// set attribute rights
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
		}
		try {
			attrManager.getAttributeDefinition(registrarSession, URN_GROUP_REGISTRAR_URL);
		} catch (AttributeNotExistsException ex) {
			// create attr if not exists
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setDisplayName(DISPLAY_NAME_GROUP_REGISTRAR_URL);
			attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_REGISTRAR_URL);
			attrDef.setNamespace(NAMESPACE_GROUP_REGISTRAR_URL);
			attrDef.setDescription("Custom URL used in registration notifications (hostname without any parameters like: https://hostname.domain/). This value override same VO setting. If not set, default hostname of Perun instance is used.");
			attrDef.setType(String.class.getName());
			attrDef = attrManager.createAttribute(registrarSession, attrDef);
			// set attribute rights
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
		}
		try {
			attrManager.getAttributeDefinition(registrarSession, URN_VO_MAIL_FOOTER);
		} catch (AttributeNotExistsException ex) {
			// create attr if not exists
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setDisplayName(DISPLAY_NAME_VO_MAIL_FOOTER);
			attrDef.setFriendlyName(FRIENDLY_NAME_VO_MAIL_FOOTER);
			attrDef.setNamespace(NAMESPACE_VO_MAIL_FOOTER);
			attrDef.setDescription("Email footer used in mail notifications by tag {mailFooter}. To edit text whithout loose of formatting, please use notification's GUI!!");
			attrDef.setType(String.class.getName());
			attrDef = attrManager.createAttribute(registrarSession, attrDef);
			// set attribute rights
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
		}
		try {
			attrManager.getAttributeDefinition(registrarSession, URN_GROUP_MAIL_FOOTER);
		} catch (AttributeNotExistsException ex) {
			// create attr if not exists
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setDisplayName(DISPLAY_NAME_GROUP_MAIL_FOOTER);
			attrDef.setFriendlyName(FRIENDLY_NAME_GROUP_MAIL_FOOTER);
			attrDef.setNamespace(NAMESPACE_GROUP_MAIL_FOOTER);
			attrDef.setDescription("Email footer used in mail notifications by tag {mailFooter}. To edit text whithout loose of formatting, please use notification's GUI!!");
			attrDef.setType(String.class.getName());
			attrDef = attrManager.createAttribute(registrarSession, attrDef);
			// set attribute rights
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			rights.add(new AttributeRights(attrDef.getId(), Role.GROUPADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
		}
		try {
			attrManager.getAttributeDefinition(registrarSession, "urn:perun:vo:attribute-def:def:voLogoURL");
		} catch (AttributeNotExistsException ex) {
			// create attr if not exists
			AttributeDefinition attrDef = new AttributeDefinition();
			attrDef.setDisplayName("VO logo's URL");
			attrDef.setFriendlyName("voLogoURL");
			attrDef.setNamespace("urn:perun:vo:attribute-def:def");
			attrDef.setDescription("Full URL of the VO's logo image (including https://) or base64 encoded data like: 'data:image/png;base64,....'");
			attrDef.setType(String.class.getName());
			attrDef = attrManager.createAttribute(registrarSession, attrDef);
			// set attribute rights
			List<AttributeRights> rights = new ArrayList<>();
			rights.add(new AttributeRights(attrDef.getId(), Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
			perun.getAttributesManager().setAttributeRights(registrarSession, rights);
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

	@Override
	public Map<String, Object> initRegistrar(PerunSession sess, String voShortName, String groupName) throws PerunException {

		Map<String, Object> result = new HashMap<>();
		Vo vo;
		Group group;

		try {

			// GET VO
			vo = vosManager.getVoByShortName(sess, voShortName);
			List<Attribute> list = attrManager.getAttributes(sess, vo,
					Arrays.asList(AttributesManager.NS_VO_ATTR_DEF+":contactEmail",
							AttributesManager.NS_VO_ATTR_DEF+":voLogoURL"));

			result.put("vo", vo);
			result.put("voAttributes", list);
			result.put("voForm", getFormForVo(vo));

			// GET INITIAL APPLICATION IF POSSIBLE
			try {

				result.put("voFormInitial", getFormItemsWithPrefilledValues(sess, AppType.INITIAL, (ApplicationForm) result.get("voForm")));

			} catch (DuplicateRegistrationAttemptException ex) {
				// has submitted application
				result.put("voFormInitialException", ex);
			} catch (AlreadyRegisteredException ex) {
				// is already member of VO
				result.put("voFormInitialException", ex);
			} catch (ExtendMembershipException ex) {
				// can't become member of VO
				result.put("voFormInitialException", ex);
			} catch (MissingRequiredDataException ex) {
				// can't display form
				result.put("voFormInitialException", ex);
			} catch (CantBeSubmittedException ex) {
				// can't display form / become member by some custom rules
				result.put("voFormInitialException", ex);
			}

			// ONLY EXISTING USERS CAN EXTEND VO MEMBERSHIP
			if (sess.getPerunPrincipal().getUser() != null) {

				try {
					result.put("voFormExtension", getFormItemsWithPrefilledValues(sess, AppType.EXTENSION, (ApplicationForm) result.get("voForm")));
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
				} catch (MissingRequiredDataException ex) {
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
					result.put("groupFormInitial", getFormItemsWithPrefilledValues(sess, AppType.INITIAL, (ApplicationForm) result.get("groupForm")));
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
				}  catch (MissingRequiredDataException ex) {
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
					result.put("groupFormExtension", getFormItemsWithPrefilledValues(sess, AppType.EXTENSION, (ApplicationForm) result.get("groupForm")));
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
				} catch (MissingRequiredDataException ex) {
					// can't display form
					result.put("groupFormExtensionException", ex);
				} catch (CantBeSubmittedException ex) {
					// can't display form / extend membership by some custom rules
					result.put("groupFormExtensionException", ex);
				}

			}

			// FIND SIMILAR USERS
			try {
				List<Identity> similarUsers = getConsolidatorManager().checkForSimilarUsers(sess);
				if (similarUsers != null && !similarUsers.isEmpty()) {
					log.debug("Similar users found for {} / {}: {}", sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getExtSourceName(), similarUsers);
				}
				result.put("similarUsers", similarUsers);
			} catch (Exception ex) {
				// not relevant exception in this use-case
				log.error("[REGISTRAR] Exception when searching for similar users.", ex);
			}

		} catch (Exception ex) {

			// we don't have to try any more, return exception
			result.put("exception", ex);
			return result;

		}

		return result;

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
	public void createApplicationFormInGroup(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		groupsManager.checkGroupExists(sess, group);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createApplicationFormInGroup_Group_policy", Collections.singletonList(group))) {
			throw new PrivilegeException(sess, "createApplicationFormInGroup");
		}

		int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
		try {
			jdbc.update("insert into application_form(id, vo_id, group_id) values (?,?,?)", id, group.getVoId(), group.getId());
		} catch (DuplicateKeyException ex) {
			throw new ConsistencyErrorException("Group can have defined only one application form. Can't insert another.", ex);
		}

	}

	@Override
	public ApplicationForm getFormForVo(final Vo vo) throws FormNotExistsException {

		if (vo == null) throw new FormNotExistsException("VO can't be null");

		try {
			return jdbc.queryForObject(FORM_SELECT + " where vo_id=? and group_id is null", (resultSet, arg1) -> {
				ApplicationForm form = new ApplicationForm();
				form.setId(resultSet.getInt("id"));
				form.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
				form.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
				form.setModuleClassName(resultSet.getString("module_name"));
				form.setVo(vo);
				return form;
			}, vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new FormNotExistsException("Form for VO: "+vo.getName()+" doesn't exists.");
		} catch (Exception ex) {
			throw new InternalErrorException(ex.getMessage(), ex);
		}

	}

	@Override
	public ApplicationForm getFormForGroup(final Group group) throws FormNotExistsException {

		if (group == null) throw new FormNotExistsException("Group can't be null");

		try {
			return jdbc.queryForObject(FORM_SELECT + " where vo_id=? and group_id=?", (resultSet, arg1) -> {
				ApplicationForm form = new ApplicationForm();
				form.setId(resultSet.getInt("id"));
				form.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
				form.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
				form.setModuleClassName(resultSet.getString("module_name"));
				form.setGroup(group);
				try {
					form.setVo(vosManager.getVoById(registrarSession, group.getVoId()));
				} catch (Exception ex) {
					// we don't care, shouldn't happen for internal identity.
				}
				return form;
			}, group.getVoId(), group.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new FormNotExistsException("Form for Group: "+group.getName()+" doesn't exists.");
		} catch (Exception ex) {
			throw new InternalErrorException(ex.getMessage(), ex);
		}

	}

	@Override
	public ApplicationForm getFormById(PerunSession sess, int id) throws PrivilegeException, FormNotExistsException {

		try {
			ApplicationForm form = jdbc.queryForObject(FORM_SELECT + " where id=?", (resultSet, arg1) -> {
				ApplicationForm form1 = new ApplicationForm();
				form1.setId(resultSet.getInt("id"));
				form1.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
				form1.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
				form1.setModuleClassName(resultSet.getString("module_name"));
				try {
					form1.setVo(vosManager.getVoById(sess, resultSet.getInt("vo_id")));
				} catch (Exception ex) {
					// we don't care, shouldn't happen for internal identity.
				}
				try {
					if (resultSet.getInt("group_id") != 0)
						form1.setGroup(groupsManager.getGroupById(sess, resultSet.getInt("group_id")));
				} catch (Exception ex) {
					// we don't care, shouldn't happen for internal identity.
				}
				return form1;
			}, id);

			if (form == null) throw new FormNotExistsException("Form with ID: "+id+" doesn't exists.");

			//Authorization
			if (Objects.isNull(form.getGroup())) {
				// VO application
				if (!AuthzResolver.authorizedInternal(sess, "vo-getFormById_int_policy", Collections.singletonList(form.getVo()))) {
					throw new PrivilegeException(sess, "getFormById");
				}
			} else {
				if (!AuthzResolver.authorizedInternal(sess, "group-getFormById_int_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
					throw new PrivilegeException(sess, "getFormById");
				}
			}

			return form;

		} catch (EmptyResultDataAccessException ex) {
			throw new FormNotExistsException("Form with ID: "+id+" doesn't exists.");
		}

	}

	@Override
	public ApplicationForm getFormByItemId(PerunSession sess, int id) throws PrivilegeException, FormNotExistsException {

		try {
			ApplicationForm form = jdbc.queryForObject(FORM_SELECT + " where id=(select form_id from application_form_items where id=?)", (resultSet, arg1) -> {
				ApplicationForm form1 = new ApplicationForm();
				form1.setId(resultSet.getInt("id"));
				form1.setAutomaticApproval(resultSet.getBoolean("automatic_approval"));
				form1.setAutomaticApprovalExtension(resultSet.getBoolean("automatic_approval_extension"));
				form1.setModuleClassName(resultSet.getString("module_name"));
				try {
					form1.setVo(vosManager.getVoById(sess, resultSet.getInt("vo_id")));
				} catch (Exception ex) {
					// we don't care, shouldn't happen for internal identity.
				}
				try {
					if (resultSet.getInt("group_id") != 0)
						form1.setGroup(groupsManager.getGroupById(sess, resultSet.getInt("group_id")));
				} catch (Exception ex) {
					// we don't care, shouldn't happen for internal identity.
				}
				return form1;
			}, id);

			if (Objects.isNull(form)) throw new FormNotExistsException("Form with ID: "+id+" doesn't exists.");

			//Authorization
			if (form.getGroup() == null) {
				// VO application
				if (!AuthzResolver.authorizedInternal(sess, "vo-getFormByItemId_int_policy", Collections.singletonList(form.getVo()))) {
					throw new PrivilegeException(sess, "getFormByItemId");
				}
			} else {
				if (!AuthzResolver.authorizedInternal(sess, "group-getFormByItemId_int_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
					throw new PrivilegeException(sess, "getFormByItemId");
				}
			}

			return form;

		} catch (EmptyResultDataAccessException ex) {
			throw new FormNotExistsException("Form with ID: "+id+" doesn't exists.");
		}

	}

	@Transactional
	@Override
	public ApplicationFormItem addFormItem(PerunSession user, ApplicationForm form, ApplicationFormItem item) throws PrivilegeException {

		//Authorization
		if (form.getGroup() == null) {
			// VO application
			if (!AuthzResolver.authorizedInternal(user, "vo-addFormItem_ApplicationForm_ApplicationFormItem_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException(user, "addFormItem");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(user, "group-addFormItem_ApplicationForm_ApplicationFormItem_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException(user, "addFormItem");
			}
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
		jdbc.update(
				"insert into application_form_items(id,form_id,ordnum,shortname,required,type,fed_attr,src_attr," +
						"dst_attr,regex,updatable,hidden,disabled,hidden_dependency_item_id,disabled_dependency_item_id) values (?,?,?,?,?,?,?,?,?,?,?,?::app_item_hidden,?::app_item_disabled,?,?)",
				itemId,
				form.getId(),
				ordnum,
				item.getShortname(),
				item.isRequired(),
				item.getType().name(),
				item.getFederationAttribute(),
				item.getPerunSourceAttribute(),
				item.getPerunDestinationAttribute(),
				item.getRegex(),
				item.isUpdatable(),
				item.getHidden().toString(),
				item.getDisabled().toString(),
				item.getHiddenDependencyItemId(),
				item.getDisabledDependencyItemId());

		// create texts
		for (Locale locale : item.getI18n().keySet()) {
			ItemTexts itemTexts = item.getTexts(locale);
			jdbc.update("insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?,?,?)",
					itemId, locale.getLanguage(), itemTexts.getLabel(),
					itemTexts.getOptions(), itemTexts.getHelp(),
					itemTexts.getErrorMessage());
		}
		for (AppType appType : item.getApplicationTypes()) {
			jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)",
					itemId, appType.toString());
		}

		// set new properties back to object & return
		item.setOrdnum(ordnum);
		item.setId(itemId);
		perun.getAuditer().log(user, new FormItemAdded(form));
		return item;

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateFormItems(PerunSession sess, ApplicationForm form, List<ApplicationFormItem> items) throws PrivilegeException {

		//Authorization
		if (form.getGroup() == null) {
			// VO application
			if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItems_ApplicationForm_List<ApplicationFormItem>_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException(sess, "updateFormItems");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItems_ApplicationForm_List<ApplicationFormItem>_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException(sess, "updateFormItems");
			}
		}

		if (items == null) {
			throw new NullPointerException("ApplicationFormItems to update can't be null");
		}
		int finalResult = 0;
		for (ApplicationFormItem item : items) {

			// is item to create ? => create
			if (item.getId() == 0 && !item.isForDelete()) {
				if (addFormItem(sess, form, item) != null) {
					finalResult++;
				}
				continue;
			}

			// is item for deletion ? => delete on cascade
			if (item.isForDelete()) {
				finalResult += jdbc.update("delete from application_form_items where id=?", item.getId());
				continue; // continue to next item
			}

			// else update form item

			int result = jdbc.update("update application_form_items set ordnum=?,shortname=?,required=?,type=?," +
							"fed_attr=?,src_attr=?,dst_attr=?,regex=?,updatable=?,hidden=?::app_item_hidden,disabled=?::app_item_disabled,hidden_dependency_item_id=?," +
							"disabled_dependency_item_id=? where id=?",
					item.getOrdnum(), item.getShortname(), item.isRequired(),
					item.getType().toString(), item.getFederationAttribute(),
					item.getPerunSourceAttribute(), item.getPerunDestinationAttribute(),
					item.getRegex(),
					item.isUpdatable(),
					item.getHidden().toString(),
					item.getDisabled().toString(),
					item.getHiddenDependencyItemId(),
					item.getDisabledDependencyItemId(),
					item.getId());
			finalResult += result;
			if (result == 0) {
				// skip whole set if not found for update
				continue;
			}

			// update form item texts (easy way = delete and new insert)

			// delete
			jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
			// insert new
			for (Locale locale : item.getI18n().keySet()) {
				ItemTexts itemTexts = item.getTexts(locale);
				jdbc.update("insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?,?,?)",
						item.getId(), locale.getLanguage(), itemTexts.getLabel(),
						itemTexts.getOptions(), itemTexts.getHelp(),
						itemTexts.getErrorMessage());
			}

			// update form item app types (easy way = delete and new insert)

			// delete
			jdbc.update("delete from application_form_item_apptypes where item_id=?", item.getId());
			// insert new
			for (AppType appType : item.getApplicationTypes()) {
				jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)",
						item.getId(), appType.toString());
			}
		}

		perun.getAuditer().log(sess, new FormItemsUpdated(form));
		// return number of updated rows
		return finalResult;

	}

	@Override
	public int updateForm(PerunSession user, ApplicationForm form) throws PrivilegeException {

		//Authorization
		if (form.getGroup() == null) {
			// VO application
			if (!AuthzResolver.authorizedInternal(user, "vo-updateForm_ApplicationForm_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException(user, "updateForm");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(user, "group-updateForm_ApplicationForm_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException(user, "updateForm");
			}
		}

		perun.getAuditer().log(user, new FormUpdated((form)));
		return jdbc.update(
				"update application_form set automatic_approval=?, automatic_approval_extension=?, module_name=? where id=?",
				form.isAutomaticApproval(), form.isAutomaticApprovalExtension(), form.getModuleClassName(), form.getId());
	}

	@Transactional
	@Override
	public void deleteFormItem(PerunSession user, ApplicationForm form, int ordnum) throws PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(user, "deleteFormItem_ApplicationForm_int_policy", Collections.singletonList(form.getVo()))) {
			throw new PrivilegeException(user, "deleteFormItem");
		}

		jdbc.update("delete from application_form_items where form_id=? and ordnum=?", form.getId(), ordnum);
		jdbc.update("update application_form_items set ordnum=ordnum-1 where form_id=? and ordnum>?", form.getId(), ordnum);

		perun.getAuditer().log(user, new FormItemDeleted(form));

	}

	@Transactional
	@Override
	public void moveFormItem(PerunSession user, ApplicationForm form, int ordnum, boolean up) throws PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(user, "moveFormItem_ApplicationForm_int_boolean_policy", Collections.singletonList(form.getVo()))) {
			throw new PrivilegeException(user, "moveFormItem");
		}

		if (up && ordnum == 0) throw new InternalErrorException("cannot move topmost item up");

		int numItems = jdbc.queryForInt("select count(*) from application_form_items where form_id=?", form.getId());

		if (!up && ordnum == numItems - 1) throw new InternalErrorException("cannot move lowest item down");

		int id1 = jdbc.queryForInt(
				"select id from application_form_items where form_id=? and ordnum=?",
				form.getId(), (up ? ordnum - 1 : ordnum));
		int id2 = jdbc.queryForInt(
				"select id from application_form_items where form_id=? and ordnum=?",
				form.getId(), (up ? ordnum : ordnum + 1));
		jdbc.update("update application_form_items set ordnum=ordnum+1 where id=?",
				id1);
		jdbc.update("update application_form_items set ordnum=ordnum-1 where id=?",
				id2);

	}

	@Override
	public void updateFormItemTexts(PerunSession sess, ApplicationFormItem item, Locale locale) throws PrivilegeException, FormNotExistsException {

		try {
			getFormByItemId(sess, item.getId());
		} catch (PrivilegeException ex) {
			throw new PrivilegeException(sess, "updateFormItemTexts");
		}

		ItemTexts texts = item.getTexts(locale);
		jdbc.update("update application_form_item_texts set label=?,options=?,help=?,error_message=? where item_id=? and locale=?",
				texts.getLabel(), texts.getOptions(), texts.getHelp(),
				texts.getErrorMessage(), item.getId(), locale.getLanguage());

	}

	@Override
	public void updateFormItemTexts(PerunSession sess, ApplicationFormItem item) throws PrivilegeException, FormNotExistsException {

		ApplicationForm form;

		// check authz on form
		try {
			form = getFormByItemId(sess, item.getId());
		} catch (PrivilegeException ex) {
			throw new PrivilegeException(sess, "updateFormItemById");
		}

		//Authorization
		if (form.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItemTexts_ApplicationFormItem_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException(sess, "updateFormItemById");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItemTexts_ApplicationFormItem_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException(sess, "updateFormItemById");
			}
		}

		// update form item texts (easy way = delete and new insert)

		// delete
		jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
		// insert new
		for (Locale locale : item.getI18n().keySet()) {
			ItemTexts itemTexts = item.getTexts(locale);
			jdbc.update("insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?,?,?)",
					item.getId(), locale.getLanguage(), itemTexts.getLabel(),
					itemTexts.getOptions(), itemTexts.getHelp(),
					itemTexts.getErrorMessage());
		}

	}

	@Override
	@Deprecated
	public List<ApplicationFormItemData> createApplication(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {
		int appId = processApplication(session, application, data);
		return getApplicationDataById(session, appId);
	}

	@Override
	public Application submitApplication(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {
		int appId = processApplication(session, application, data);
		return getApplicationById(appId);
	}

	@Override
	@Transactional(rollbackFor = ApplicationNotCreatedException.class)
	public Application createApplicationInternal(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {

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

			jdbc.update("insert into application(id,vo_id,group_id,user_id,apptype,fed_info,extSourceName,extSourceType,extSourceLoa,state,created_by,modified_by) values (?,?,?,?,?,?,?,?,?,?,?,?)",
					appId, application.getVo().getId(), groupId, userId,
					application.getType().toString(), application.getFedInfo(),
					application.getExtSourceName(), application.getExtSourceType(),
					application.getExtSourceLoa(), application.getState().toString(),
					application.getCreatedBy(),application.getCreatedBy());

			// 2) process & store app data
			for (ApplicationFormItemData itemData : data) {

				Type itemType = itemData.getFormItem().getType();
				if (itemType == HTML_COMMENT || itemType == SUBMIT_BUTTON || itemType == AUTO_SUBMIT_BUTTON || itemType == PASSWORD || itemType == HEADING) continue;

				// Check if mails needs to be validated
				if (itemType == VALIDATED_EMAIL) {
					handleLoaForValidatedMail(session, itemData);
				}

				try {
					itemData.setId(Utils.getNewId(jdbc, "APPLICATION_DATA_ID_SEQ"));
					jdbc.update("insert into application_data(id,app_id,item_id,shortname,value,assurance_level) values (?,?,?,?,?,?)",
							itemData.getId(), appId, itemData.getFormItem().getId(), itemData
									.getFormItem().getShortname(), itemData.getValue(), ((isBlank(itemData
									.getAssuranceLevel())) ? null : itemData.getAssuranceLevel()));
				} catch (Exception ex) {
					// log and store exception so vo manager could see error in notification.
					log.error("[REGISTRAR] Storing form item {} caused exception {}", itemData, ex);
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
					if (itemData.getValue() == null || itemData.getValue().isEmpty() || itemData.getValue().equals("null")) continue;
					// skip unchanged pre-filled logins, since they must have been handled last time
					if (itemType == USERNAME && Objects.equals(itemData.getValue(), itemData.getPrefilledValue())) continue;
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
						log.error("[REGISTRAR] Unable to store login: {} in namespace: {} due to {}", login, loginNamespace, ex);
						throw new ApplicationNotCreatedException("Application was not created. Reason: Login: "+login+" in namespace: "+loginNamespace+" is not allowed. Please choose different login.", login, loginNamespace);
					}

					// try to book new login in namespace if the application hasn't been approved yet
					if (loginAvailable) {
						try {
							// Reserve login
							jdbc.update("insert into application_reserved_logins(login,namespace,app_id,created_by,created_at) values(?,?,?,?,?)",
									login, loginNamespace, appId, application.getCreatedBy(), new Date());
							log.debug("[REGISTRAR] Added login reservation for login: {} in namespace: {}.", login, loginNamespace);

							// process password for this login
							for (ApplicationFormItemData passItem : logins) {
								ApplicationFormItem item = passItem.getFormItem();
								if (item.getType() == PASSWORD && item.getPerunDestinationAttribute() != null) {
									if (item.getPerunDestinationAttribute().equals(dstAttr)) {
										pass = passItem.getValue();
										try {
											// reserve password
											usersManager.reservePassword(registrarSession, login, loginNamespace, pass);
											log.debug("[REGISTRAR] Password for login: {} in namespace: {} successfully reserved in external system.", login, loginNamespace);
										} catch (Exception ex) {
											// login reservation fail must cause rollback !!
											log.error("[REGISTRAR] Unable to reserve password for login: {} in namespace: {} in external system. Exception: {}", login, loginNamespace, ex);
											throw new ApplicationNotCreatedException("Application was not created. Reason: Unable to reserve password for login: "+login+" in namespace: "+loginNamespace+" in external system. Please contact support to fix this issue before new application submission.", login, loginNamespace);
										}
										break; // use first pass with correct namespace
									}
								}
							}
						} catch (ApplicationNotCreatedException ex) {
							throw ex; // re-throw
						} catch (Exception ex) {
							// unable to book login
							log.error("[REGISTRAR] Unable to reserve login: {} in namespace: {}. Exception: ", login, loginNamespace, ex);
							exceptions.add(ex);
						}
					} else {
						// login is not available
						log.error("[REGISTRAR] Login: {} in namespace: {} is already occupied but it shouldn't (race condition).", login, loginNamespace);
						exceptions.add(new InternalErrorException("Login: " + login  + " in namespace: " + loginNamespace + " is already occupied but it shouldn't."));
					}
				}
			}

			// call registrar module before auto validation so createAction is trigerred first
			RegistrarModule module;
			if (application.getGroup() != null) {
				module = getRegistrarModule(getFormForGroup(application.getGroup()));
			} else {
				module = getRegistrarModule(getFormForVo(application.getVo()));
			}
			if (module != null) {
				module.createApplication(session, application, data);
			}

		} catch (ApplicationNotCreatedException ex) {
			applicationNotCreated = true; // prevent action in finally block
			throw ex; // re-throw
		} catch (Exception ex) {
			// any exception during app creation process => add it to list
			// exceptions when handling logins are catched before
			log.error("Unexpected exception when creating application.", ex);
			exceptions.add(ex);
		} finally {

			// process rest only if it was not exception related to PASSWORDS creation
			if (!applicationNotCreated) {

				getMailManager().sendMessage(application, MailType.APP_CREATED_USER, null, null);
				getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, exceptions);
				// if there were exceptions, throw some to let know GUI about it
				if (!exceptions.isEmpty()) {
					RegistrarException ex = new RegistrarException("Your application (ID="+ application.getId()+
							") has been created with errors. Administrator of " + application.getVo().getName() + " has been notified. If you want, you can use \"Send report to RT\" button to send this information to administrators directly.");
					log.error("[REGISTRAR] New application {} created with errors {}. This is case of PerunException {}",application, exceptions, ex.getErrorId());
					throw ex;
				}
				log.info("New application {} created.", application);
				perun.getAuditer().log(session, new ApplicationCreated(application));

			}
		}

		// return stored data
		return application;

	}

	@Override
	public void deleteApplication(PerunSession sess, Application app) throws PerunException {

		//Authorization
		if (app.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-deleteApplication_Application_policy", Collections.singletonList(app.getVo()))) {
				throw new PrivilegeException(sess, "deleteApplication");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-deleteApplication_Application_policy", Arrays.asList(app.getVo(), app.getGroup()))) {
				throw new PrivilegeException(sess, "deleteApplication");
			}
		}

		// lock to prevent concurrent runs
		synchronized(runningDeleteApplication) {
			if (runningDeleteApplication.contains(app.getId())) {
				throw new AlreadyProcessingException("Application deletion is already processing.");
			} else {
				runningDeleteApplication.add(app.getId());
			}
		}

		try {

			if (AppState.NEW.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) {

				// Try to get reservedLogin and reservedNamespace before deletion
				List<Pair<String, String>> logins;
				try {
					logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?", (resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), app.getId());
				} catch (EmptyResultDataAccessException e) {
					// set empty logins
					logins = new ArrayList<>();
				}
				// delete passwords in KDC
				for (Pair<String, String> login : logins) {
					// delete LOGIN in NAMESPACE
					usersManager.deletePassword(sess, login.getRight(), login.getLeft());
				}

				// free any login from reservation when application is rejected
				jdbc.update("delete from application_reserved_logins where app_id=?", app.getId());

				// delete application and data on cascade
				jdbc.update("delete from application where id=?", app.getId());

			} else {
				if (AppState.VERIFIED.equals(app.getState()))
					throw new RegistrarException("Submitted application can't be deleted. Please reject the application first.");
				if (AppState.APPROVED.equals(app.getState()))
					throw new RegistrarException("Approved application can't be deleted. Try to refresh the view to see changes.");
			}
			perun.getAuditer().log(sess, new ApplicationDeleted(app));

		} finally {
			synchronized (runningDeleteApplication) {
				runningDeleteApplication.remove(app.getId());
			}
		}

	}

	@Override
	public Application verifyApplication(PerunSession sess, int appId) throws PerunException {

		Application app = getApplicationById(appId);
		if (app == null) throw new RegistrarException("Application with ID="+appId+" doesn't exists.");

		//Authorization
		if (app.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-verifyApplication_int_policy", Collections.singletonList(app.getVo()))) {
				throw new PrivilegeException(sess, "verifyApplication");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-verifyApplication_int_policy", Arrays.asList(app.getVo(), app.getGroup()))) {
				throw new PrivilegeException(sess, "verifyApplication");
			}
		}

		// proceed
		markApplicationVerified(sess, appId);
		perun.getAuditer().log(sess, new ApplicationVerified(app));
		// return updated application
		return getApplicationById(appId);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Application rejectApplication(PerunSession sess, int appId, String reason) throws PerunException {

		Application app = getApplicationById(appId);
		if (app == null) throw new RegistrarException("Application with ID="+appId+" doesn't exists.");

		//Authorization
		if (app.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-rejectApplication_int_String_policy", Collections.singletonList(app.getVo()))) {
				throw new PrivilegeException(sess, "rejectApplication");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-rejectApplication_int_String_policy", Arrays.asList(app.getVo(), app.getGroup()))) {
				throw new PrivilegeException(sess, "rejectApplication");
			}
		}

		// only VERIFIED applications can be rejected
		if (AppState.APPROVED.equals(app.getState())) {
			throw new RegistrarException("Approved application can't be rejected ! Try to refresh the view to see changes.");
		} else if (AppState.REJECTED.equals(app.getState())) {
			throw new RegistrarException("Application is already rejected. Try to refresh the view to see changes.");
		}

		// lock to prevent concurrent runs
		synchronized(runningRejectApplication) {
			if (runningRejectApplication.contains(appId)) {
				throw new AlreadyProcessingException("Application rejection is already processing.");
			} else {
				runningRejectApplication.add(appId);
			}
		}

		try {

			// mark as rejected
			int result = jdbc.update("update application set state=?, modified_by=?, modified_at=? where id=?", AppState.REJECTED.toString(), sess.getPerunPrincipal().getActor(), new Date(), appId);
			if (result == 0) {
				throw new RegistrarException("Application with ID=" + appId + " not found.");
			} else if (result > 1) {
				throw new ConsistencyErrorException("More than one application is stored under ID=" + appId + ".");
			}
			// set back as rejected
			app.setState(AppState.REJECTED);
			log.info("Application {} marked as REJECTED.", appId);

			// get all reserved logins
			List<Pair<String, String>> logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?",
					(resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), appId);

			// delete passwords for reserved logins
			for (Pair<String, String> login : logins) {
				try {
					// left = namespace / right = login
					usersManager.deletePassword(registrarSession, login.getRight(), login.getLeft());
				} catch (LoginNotExistsException ex) {
					log.error("[REGISTRAR] Login: {} not exists while deleting passwords in rejected application: {}", login.getLeft(), appId);
				}
			}
			// free any login from reservation when application is rejected
			jdbc.update("delete from application_reserved_logins where app_id=?", appId);

			// log
			perun.getAuditer().log(sess, new ApplicationRejected(app));

			// call registrar module
			RegistrarModule module;
			if (app.getGroup() != null) {
				module = getRegistrarModule(getFormForGroup(app.getGroup()));
			} else {
				module = getRegistrarModule(getFormForVo(app.getVo()));
			}
			if (module != null) {
				module.rejectApplication(sess, app, reason);
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
	public Application approveApplication(PerunSession sess, int appId) throws PerunException {

		synchronized(runningApproveApplication) {
			if (runningApproveApplication.contains(appId)) {
				throw new AlreadyProcessingException("Application approval is already processing.");
			} else {
				runningApproveApplication.add(appId);
			}
		}

		Application app;
		try {
			app = registrarManager.approveApplicationInternal(sess, appId);
		} catch (AlreadyMemberException ex) {
			// case when user joined identity after sending initial application and former user was already member of VO
			throw new RegistrarException("User is already member (with ID: "+ex.getMember().getId()+") of your VO/group. (user joined his identities after sending new application). You can reject this application and re-validate old member to keep old data (e.g. login,email).", ex);
		} catch (MemberNotExistsException ex) {
			throw new RegistrarException("To approve application user must already be member of VO.", ex);
		} catch (NotGroupMemberException ex) {
			throw new RegistrarException("To approve application user must already be member of Group.", ex);
		} catch (UserNotExistsException | UserExtSourceNotExistsException | ExtSourceNotExistsException ex) {
			throw new RegistrarException("User specified by the data in application was not found. If you tried to approve application for the Group, try to check, if user already has approved application in the VO. Application to the VO must be approved first.", ex);
		} finally {
			synchronized (runningApproveApplication) {
				runningApproveApplication.remove(appId);
			}
		}

		Member member = membersManager.getMemberByUser(sess, app.getVo(), app.getUser());

		try {

			// validate member async when all changes are committed
			// we can't use existing core method, since we want to approve auto-approval waiting group applications
			// once member is validated
			new Thread(() -> {

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					membersManager.validateMember(registrarSession, member);
				} catch (InternalErrorException | WrongAttributeValueException | WrongReferenceAttributeValueException e) {
					log.error("[REGISTRAR] Exception when validating {} after approving application {}.", member, app);
				}

				try {
					// get user's group apps with auto-approve and approve them
					autoApproveUsersGroupApplications(sess, app.getVo(), app.getUser());
				} catch (PerunException ex) {
					log.error("[REGISTRAR] Exception when auto-approving waiting group applications for {} after approving application {}.", member, app);
				}

			}).start();

		} catch (Exception ex) {
			// we skip any exception thrown from here
			log.error("[REGISTRAR] Exception when validating {} after approving application {}.", member, app);
		}
		perun.getAuditer().log(sess, new ApplicationApproved(app));

		synchronized (runningApproveApplication) {
			runningApproveApplication.remove(appId);
		}

		return app;
	}

	/**
	 * Process application approval in 1 transaction
	 * !! WITHOUT members validation !!
	 *
	 * @param sess session for authz
	 * @param appId application ID to approve
	 * @return updated application
	 * @throws PerunException
	 */
	@Transactional(rollbackFor = Exception.class)
	public Application approveApplicationInternal(PerunSession sess, int appId) throws PrivilegeException, RegistrarException, FormNotExistsException, UserNotExistsException, ExtSourceNotExistsException, UserExtSourceNotExistsException, LoginNotExistsException, PasswordCreationFailedException, WrongReferenceAttributeValueException, WrongAttributeValueException, MemberNotExistsException, VoNotExistsException, CantBeApprovedException, GroupNotExistsException, NotGroupMemberException, ExternallyManagedException, WrongAttributeAssignmentException, AttributeNotExistsException, AlreadyMemberException, ExtendMembershipException, PasswordDeletionFailedException, PasswordOperationTimeoutException, AlreadyAdminException, InvalidLoginException {

		Application app = getApplicationById(appId);
		if (app == null) throw new RegistrarException("Application with ID "+appId+" doesn't exists.");
		Member member;

		//Authorization
		if (app.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-approveApplicationInternal_int_policy", Collections.singletonList(app.getVo()))) {
				throw new PrivilegeException(sess, "approveApplicationInternal");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-approveApplicationInternal_int_policy", Arrays.asList(app.getVo(), app.getGroup()))) {
				throw new PrivilegeException(sess, "approveApplicationInternal");
			}
		}

		// only VERIFIED applications can be approved
		if (!AppState.VERIFIED.equals(app.getState())) {
			if (AppState.APPROVED.equals(app.getState())) throw new RegistrarException("Application is already approved. Try to refresh the view to see changes.");
			if (AppState.REJECTED.equals(app.getState())) throw new RegistrarException("Rejected application cant' be approved. Try to refresh the view to see changes.");
			throw new RegistrarException("User didn't verify his email address yet. Please wait until application will be in a 'Submitted' state. You can send mail verification notification to user again if you wish.");
		}

		LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
		PerunPrincipal applicationPrincipal = new PerunPrincipal(app.getCreatedBy(), app.getExtSourceName(), app.getExtSourceType(), app.getExtSourceLoa(), additionalAttributes);

		// get registrar module
		RegistrarModule module;
		if (app.getGroup() != null) {
			module = getRegistrarModule(getFormForGroup(app.getGroup()));
		} else {
			module = getRegistrarModule(getFormForVo(app.getVo()));
		}

		if (module != null) {
			// call custom logic before approving
			module.beforeApprove(sess, app);
		}

		// mark as APPROVED
		int result = jdbc.update("update application set state=?, modified_by=?, modified_at=? where id=?", AppState.APPROVED.toString(), sess.getPerunPrincipal().getActor(), new Date(), appId);
		if (result == 0) {
			throw new RegistrarException("Application with ID="+appId+" not found.");
		} else if (result > 1) {
			throw new ConsistencyErrorException("More than one application is stored under ID="+appId+".");
		}
		// set back as approved
		app.setState(AppState.APPROVED);
		log.info("Application {} marked as APPROVED", appId);

		// Try to get reservedLogin and reservedNamespace before deletion, it will be used for creating userExtSources
		List<Pair<String, String>> logins;
		try {
			logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?", (resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), appId);
		} catch (EmptyResultDataAccessException e) {
			// set empty logins
			logins = new ArrayList<>();
		}

		// FOR INITIAL APPLICATION
		if (AppType.INITIAL.equals(app.getType())) {

			if (app.getGroup() != null) {

				// free reserved logins so they can be set as attributes
				jdbc.update("delete from application_reserved_logins where app_id=?", appId);

				if (app.getUser() == null) {

					// application for group doesn't have user set, but it can exists in perun (joined identities after submission)
					User u = perun.getUsersManagerBl().getUserByExtSourceInformation(registrarSession, applicationPrincipal);

					// put user back to application
					app.setUser(u);
					// store user_id in DB
					int result2 = jdbc.update("update application set user_id=? where id=?", u.getId(), appId);
					if (result2 == 0) {
						throw new RegistrarException("Application with ID="+appId+" not found.");
					} else if (result2 > 1) {
						throw new ConsistencyErrorException("More than one application is stored under ID="+appId+".");
					}

				}

				// add new member of VO as member of group (for group applications)
				// !! MUST BE MEMBER OF VO !!
				member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

				// MEMBER must be in a VALID or INVALID state since approval starts validation !!
				// and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
				// meaning, user should submit membership extension application first !!
				if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
					throw new CantBeApprovedException("Application of member with membership status: "+member.getStatus()+" can't be approved. Please wait until member extends/re-validate own membership in a VO.");
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
				if ("true".equals(attrSynchronizeEnabled.getValue()) || groupsManager.isGroupInStructureSynchronizationTree(sess, app.getGroup())) {
					throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
				}

				groupsManager.addMember(registrarSession, app.getGroup(), member);

				log.debug("[REGISTRAR] Member {} added to Group {}.",member, app.getGroup());

			} else {

				// free reserved logins so they can be set as attributes
				jdbc.update("delete from application_reserved_logins where app_id=?", appId);

				User u;
				if (app.getUser() != null) {
					u = app.getUser();
					log.debug("[REGISTRAR] Trying to make member from user {}", u);
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
						log.debug("[REGISTRAR] Trying to make member from user {}", u);
						member = membersManager.createMember(sess, app.getVo(), u);
						// set NEW user id back to application
						app.setUser(u);
						// store all attributes (but not logins)
						storeApplicationAttributes(app);
						// if user was already known to perun, createMember() will set attributes
						// via setAttributes() method so core attributes are skipped
						// ==> updateNameTitles() in case of change in appForm.
						updateUserNameTitles(app);
					} catch (UserExtSourceNotExistsException | UserNotExistsException | ExtSourceNotExistsException  ex) {
						Candidate candidate = createCandidateFromApplicationData(app);
						// create member and user
						log.debug("[REGISTRAR] Trying to make member from candidate {}", candidate);

						// added duplicit check, since we switched from entry to bl call of createMember()
						Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
						Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

						member = membersManager.createMember(sess, app.getVo(), app.getExtSourceName(), app.getExtSourceType(), app.getExtSourceLoa(), app.getCreatedBy(), candidate);
						u = usersManager.getUserById(registrarSession, member.getUserId());
						// set NEW user id back to application
						app.setUser(u);
					}
					// user originally not known -> set UserExtSource attributes from source identity for new User and UES
					ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, app.getExtSourceName());
					UserExtSource ues = usersManager.getUserExtSourceByExtLogin(sess, es, app.getCreatedBy());
					// we have historical data in "fedInfo" item, hence we must safely ignore any parsing errors.
					try {
						((PerunBlImpl)perun).setUserExtSourceAttributes(sess, ues, additionalAttributes);
					} catch (Exception ex) {
						log.error("Unable to store UES attributes from application ID: {}, attributes: {}, with exception: {}", appId, app.getFedInfo(), ex);
					}
				}

				result = jdbc.update("update application set user_id=? where id=?", member.getUserId(), appId);
				if (result == 0) {
					throw new RegistrarException("User ID hasn't been associated with the application " + appId + ", because the application was not found!");
				} else if (result > 1) {
					throw new ConsistencyErrorException("User ID hasn't been associated with the application " + appId + ", because more than one application exists under the same ID.");
				}
				log.info("Member {} created for: {} / {}", member.getId(), app.getCreatedBy(), app.getExtSourceName());

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
				perun.getAuditer().log(sess, new MemberCreatedForApprovedApp(member,app));

			}

			// FOR EXTENSION APPLICATION
		} else if (AppType.EXTENSION.equals(app.getType())) {

			// free reserved logins so they can be set as attributes
			jdbc.update("delete from application_reserved_logins where app_id=?", app.getId());

			member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

			if (app.getGroup() != null) {

				// MEMBER must be in a VALID or INVALID state since approval starts validation !!
				// and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
				// meaning, user should submit membership extension application first !!
				if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
					throw new CantBeApprovedException("Application of member with membership status: "+member.getStatus()+" can't be approved. Please wait until member extends/re-validate own membership in a VO.");
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
			perun.getAuditer().log(sess, new MembershipExtendedForMemberInApprovedApp(member,app,app.getVo()));

		}

		// CONTINUE FOR BOTH APP TYPES

		if (module != null) {
			module.approveApplication(sess, app);
		}

		getMailManager().sendMessage(app, MailType.APP_APPROVED_USER, null, null);

		// return updated application
		return app;

	}

	@Override
	public void canBeApproved(PerunSession session, Application application) throws PerunException {

		//Authorization
		if (application.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(session, "vo-canBeApproved_Application_policy", Collections.singletonList(application.getVo()))) {
				throw new PrivilegeException(session, "canBeApproved");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(session, "group-canBeApproved_Application_policy", Arrays.asList(application.getVo(), application.getGroup()))) {
				throw new PrivilegeException(session, "canBeApproved");
			}
		}

		// get registrar module
		RegistrarModule module;
		if (application.getGroup() != null) {
			module = getRegistrarModule(getFormForGroup(application.getGroup()));
		} else {
			module = getRegistrarModule(getFormForVo(application.getVo()));
		}

		if (module != null) {
			// call custom logic before approving
			module.canBeApproved(session, application);
		}

		// generally for Group applications:

		// submitter, must be MEMBER of VO and in VALID or INVALID state since approval starts validation !!
		// and we don't want to validate expired, suspended or disabled users without VO admin owns action !!
		// meaning, user should submit membership extension application first !!
		if (application.getGroup() != null) {
			try {
				User u = application.getUser();
				if (u == null) {
					LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(application.getFedInfo());
					PerunPrincipal applicationPrincipal = new PerunPrincipal(application.getCreatedBy(), application.getExtSourceName(), application.getExtSourceType(), application.getExtSourceLoa(), additionalAttributes);
					u = perun.getUsersManagerBl().getUserByExtSourceInformation(registrarSession, applicationPrincipal);
				}
				Member member = membersManager.getMemberByUser(registrarSession, application.getVo(), u);
				if (!Arrays.asList(Status.VALID, Status.INVALID).contains(member.getStatus())) {
					throw new CantBeApprovedException("Application of member with membership status: " + member.getStatus() + " can't be approved. Please wait until member extends/re-validate own membership in a VO.");
				}
			} catch (MemberNotExistsException | UserNotExistsException | ExtSourceNotExistsException | UserExtSourceNotExistsException ex) {
				throw new RegistrarException("To approve application user must be a member of VO.", ex);
			}

		}

	}

	@Override
	public Application getApplicationById(PerunSession sess, int appId) throws RegistrarException, PrivilegeException {

		// get application
		Application app = getApplicationById(appId);
		if (app == null) throw new RegistrarException("Application with ID="+appId+" doesn't exists.");

		//Authorization
		if (app.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-getApplicationById_int_policy", Collections.singletonList(app.getVo()))
				&& !AuthzResolver.selfAuthorizedForApplication(sess, app)) {
				throw new PrivilegeException(sess, "getApplicationById");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-getApplicationById_int_policy", Arrays.asList(app.getVo(), app.getGroup()))
				&& !AuthzResolver.selfAuthorizedForApplication(sess, app)) {
				throw new PrivilegeException(sess, "getApplicationById");
			}
		}

		return app;

	}

	@Override
	public List<Application> getApplicationsForVo(PerunSession userSession, Vo vo, List<String> state, Boolean includeGroupApplications) throws PerunException {
		vosManager.checkVoExists(userSession, vo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForVo_Vo_List<String>_Boolean_policy", Collections.singletonList(vo))) {
			throw new PrivilegeException(userSession, "getApplicationsForVo");
		}
		if (state == null || state.isEmpty()) {
			// list all
			try {
				return jdbc.query(APP_SELECT + " where a.vo_id=? " 
						+ (includeGroupApplications ? "" : " and a.group_id is null ")
						+ " order by a.id desc", APP_MAPPER, vo.getId());
			} catch (EmptyResultDataAccessException ex) {
				return new ArrayList<>();
			}
		} else {
			// filter by state
			try {
				MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
				sqlParameterSource.addValue("voId", vo.getId());
				sqlParameterSource.addValue("states", state);
				return namedJdbc.query(APP_SELECT + " where a.vo_id=:voId and state in ( :states ) " 
						+ (includeGroupApplications ? "" : " and a.group_id is null ")
						+ " order by a.id desc", sqlParameterSource, APP_MAPPER);
			} catch (EmptyResultDataAccessException ex) {
				return new ArrayList<>();
			}
		}

	}

	@Override
	public List<Application> getApplicationsForVo(PerunSession userSession, Vo vo, List<String> state, LocalDate dateFrom, LocalDate dateTo, Boolean includeGroupApplications) throws PerunException {
		vosManager.checkVoExists(userSession, vo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForVo_Vo_List<String>_LocalDate_LocalDate_Boolean_policy", Collections.singletonList(vo))) {
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
			if(dateFrom != null) {
				sqlParameterSource.addValue("from", dateFrom);
				query.append(" and a.created_at::date >= :from");
			}
			if(dateTo != null) {
				sqlParameterSource.addValue("to", dateTo);
				query.append(" and a.created_at::date <= :to");
			}
			if(!includeGroupApplications) {
				query.append(" and a.group_id is null");
			}
			query.append(" order by a.id desc");
			return namedJdbc.query(query.toString(), sqlParameterSource, APP_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}

	}

	@Override
	public List<Application> getApplicationsForGroup(PerunSession userSession, Group group, List<String> state) throws PerunException {
		groupsManager.checkGroupExists(userSession, group);

		//Authorization
		if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForGroup_Group_List<String>_policy", Collections.singletonList(group))) {
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
				return namedJdbc.query(APP_SELECT + " where a.group_id=:groupId and state in ( :states ) order by a.id desc", sqlParameterSource, APP_MAPPER);
			} catch (EmptyResultDataAccessException ex) {
				return new ArrayList<>();
			}
		}

	}

	@Override
	public List<Application> getApplicationsForGroup(PerunSession userSession, Group group, List<String> state, LocalDate dateFrom, LocalDate dateTo) throws PerunException {
		groupsManager.checkGroupExists(userSession, group);

		//Authorization
		if (!AuthzResolver.authorizedInternal(userSession, "getApplicationsForGroup_Group_List<String>_LocalDate_LocalDate_policy", Collections.singletonList(group))) {
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
			if(dateFrom != null) {
				sqlParameterSource.addValue("from", dateFrom);
				query.append(" and a.created_at::date >= :from");
			}
			if(dateTo != null) {
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
	public List<Application> getApplicationsForMember(PerunSession sess, Group group, Member member) throws PerunException {
		membersManager.checkMemberExists(sess, member);

		//Authorization
		if (group == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-getApplicationsForMember_Group_Member_policy", Collections.singletonList(member))) {
				throw new PrivilegeException(sess, "getApplicationsForMember");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-getApplicationsForMember_Group_Member_policy", Arrays.asList(member, group))) {
				throw new PrivilegeException(sess, "getApplicationsForMember");
			}
		}

		try {
			if (group == null) {
				return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? order by a.id desc", APP_MAPPER, member.getUserId(), member.getVoId());
			} else {
				return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? and a.group_id=? order by a.id desc", APP_MAPPER, member.getUserId(), member.getVoId(), group.getId());
			}
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}

	}

	@Override
	public List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm form, AppType appType) throws PerunException {

		//Authorization
		if (form.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-getFormItems_ApplicationForm_AppType_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException("getFormItems");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-getFormItems_ApplicationForm_AppType_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException("getFormItems");
			}
		}

		List<ApplicationFormItem> items;
		if (appType == null) {
			items = jdbc.query(FORM_ITEM_SELECT+" where form_id=? order by ordnum asc", ITEM_MAPPER, form.getId());
		} else {
			items = jdbc.query(FORM_ITEM_SELECT+" i,application_form_item_apptypes t where form_id=? and i.id=t.item_id and t.apptype=? order by ordnum asc",
					ITEM_MAPPER, form.getId(), appType.toString());
		}
		for (ApplicationFormItem item : items) {
			List<ItemTexts> texts = jdbc.query(FORM_ITEM_TEXTS_SELECT + " where item_id=?", ITEM_TEXTS_MAPPER, item.getId());
			for (ItemTexts itemTexts : texts) {
				item.getI18n().put(itemTexts.getLocale(), itemTexts);
			}
			List<AppType> appTypes = jdbc.query(APP_TYPE_SELECT+" where item_id=?", APP_TYPE_MAPPER, item.getId());
			item.setApplicationTypes(appTypes);
		}

		return items;
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
		item = jdbc.queryForObject(FORM_ITEM_SELECT+" where id=?", ITEM_MAPPER, id);
		if (item != null) {
			List<ItemTexts> texts = jdbc.query(FORM_ITEM_TEXTS_SELECT+" where item_id=?", ITEM_TEXTS_MAPPER, item.getId());
			for (ItemTexts itemTexts : texts) {
				item.getI18n().put(itemTexts.getLocale(), itemTexts);
			}
			List<AppType> appTypes = jdbc.query(APP_TYPE_SELECT+" where item_id=?", APP_TYPE_MAPPER, item.getId());
			item.setApplicationTypes(appTypes);
		}

		return item;

	}

	@Override
	public void updateFormItem(PerunSession sess, ApplicationFormItem item) throws PrivilegeException, FormNotExistsException {

		ApplicationForm form;

		// check authz on form
		try {
			form = getFormByItemId(sess, item.getId());
		} catch (PrivilegeException ex) {
			throw new PrivilegeException(sess, "updateFormItemById");
		}

		//Authorization
		if (form.getGroup() == null) {
			if (!AuthzResolver.authorizedInternal(sess, "vo-updateFormItem_ApplicationFormItem_policy", Collections.singletonList(form.getVo()))) {
				throw new PrivilegeException(sess, "updateFormItemById");
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "group-updateFormItem_ApplicationFormItem_policy", Arrays.asList(form.getVo(), form.getGroup()))) {
				throw new PrivilegeException(sess, "updateFormItemById");
			}
		}

		// else update form item

		int result = jdbc.update("update application_form_items set ordnum=?,shortname=?,required=?,type=?,fed_attr=?," +
						"src_attr=?,dst_attr=?,regex=?,updatable=?,hidden=?::app_item_hidden,disabled=?::app_item_disabled,hidden_dependency_item_id=?,disabled_dependency_item_id=? where id=?",
				item.getOrdnum(),
				item.getShortname(),
				item.isRequired(),
				item.getType().toString(),
				item.getFederationAttribute(),
				item.getPerunSourceAttribute(),
				item.getPerunDestinationAttribute(),
				item.getRegex(),
				item.isUpdatable(),
				item.getHidden().toString(),
				item.getDisabled().toString(),
				item.getHiddenDependencyItemId(),
				item.getDisabledDependencyItemId(),
				item.getId());

		// update form item texts (easy way = delete and new insert)

		// delete
		jdbc.update("delete from application_form_item_texts where item_id=?", item.getId());
		// insert new
		for (Locale locale : item.getI18n().keySet()) {
			ItemTexts itemTexts = item.getTexts(locale);
			jdbc.update("insert into application_form_item_texts(item_id,locale,label,options,help,error_message) values (?,?,?,?,?,?)",
					item.getId(), locale.getLanguage(), itemTexts.getLabel(),
					itemTexts.getOptions(), itemTexts.getHelp(),
					itemTexts.getErrorMessage());
		}

		// update form item app types (easy way = delete and new insert)

		// delete
		jdbc.update("delete from application_form_item_apptypes where item_id=?", item.getId());
		// insert new
		for (AppType appType : item.getApplicationTypes()) {
			jdbc.update("insert into application_form_item_apptypes (item_id,apptype) values (?,?)",
					item.getId(), appType.toString());
		}

		perun.getAuditer().log(sess, new FormItemUpdated(form,item));

	}

	@Override
	public List<ApplicationFormItemWithPrefilledValue> getFormItemsWithPrefilledValues(PerunSession sess, AppType appType, ApplicationForm form) throws PerunException {

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

		RegistrarModule module = getRegistrarModule(form);
		if (module != null) module.canBeSubmitted(sess, appType, federValues);

		// throws exception if user couldn't submit application - no reason to get form
		checkDuplicateRegistrationAttempt(sess, appType, form);

		// PROCEED
		Map<String, String> parsedName = extractNames(federValues);
		List<ApplicationFormItem> formItems = getFormItems(registrarSession, form, appType);

		List<ApplicationFormItemWithPrefilledValue> itemsWithValues = new ArrayList<>();
		for (ApplicationFormItem item : formItems) {
			itemsWithValues.add(new ApplicationFormItemWithPrefilledValue(item, null));
		}

		// get user and member attributes from DB for existing users
		if (user != null) {

			Map<String, Attribute> map = new HashMap<>();

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

			Iterator<ApplicationFormItemWithPrefilledValue> it = ((Collection<ApplicationFormItemWithPrefilledValue>) itemsWithValues).iterator();
			while (it.hasNext()) {
				ApplicationFormItemWithPrefilledValue itemW = it.next();
				String sourceAttribute = itemW.getFormItem().getPerunSourceAttribute();
				// skip items without perun attr reference
				if (sourceAttribute == null || sourceAttribute.equals(""))
					continue;
				// if attr exist and value != null
				if (map.get(sourceAttribute) != null && map.get(sourceAttribute).getValue() != null) {
					if (itemW.getFormItem().getType() == PASSWORD) {
						// if login in namespace exists, do not return password field
						// because application form is not place to change login or password
						it.remove();
					} else {
						// else set value
						itemW.setPrefilledValue(BeansUtils.attributeValueToString(map.get(sourceAttribute)));
					}
				}
			}
		}

		List<ApplicationFormItemWithPrefilledValue> itemsWithMissingData = new ArrayList<>();

		// get user attributes from federation
		Iterator<ApplicationFormItemWithPrefilledValue> it = (itemsWithValues).iterator();
		while (it.hasNext()) {
			ApplicationFormItemWithPrefilledValue itemW = it.next();
			String fa = itemW.getFormItem().getFederationAttribute();
			if (fa != null && !fa.isEmpty()) {

				// FILL VALUE FROM FEDERATION
				String s = federValues.get(fa);
				if (s != null && !s.isEmpty()) {
					// In case of email, value from the federation can contain more than one entries, entries are separated by semi-colon
					if (itemW.getFormItem().getType().equals(ApplicationFormItem.Type.VALIDATED_EMAIL)) {
						if (itemW.getPrefilledValue() != null && !itemW.getPrefilledValue().isEmpty()) {
							s = itemW.getPrefilledValue() + ";" + s;
						}
					}
					// remove password field if (login) prefilled from federation
					if (itemW.getFormItem().getType() == PASSWORD) {
						it.remove();
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
					if (titleBefore != null && !titleBefore.trim().isEmpty())
						itemW.setPrefilledValue(titleBefore);
				} else if (URN_USER_TITLE_AFTER.equals(sourceAttribute)) {
					String titleAfter = parsedName.get("titleAfter");
					if (titleAfter != null && !titleAfter.trim().isEmpty())
						itemW.setPrefilledValue(titleAfter);
				} else if (URN_USER_FIRST_NAME.equals(sourceAttribute)) {
					String firstName = parsedName.get("firstName");
					if (firstName != null && !firstName.trim().isEmpty())
						itemW.setPrefilledValue(firstName);
				} else if (URN_USER_MIDDLE_NAME.equals(sourceAttribute)) {
					String middleName = parsedName.get("middleName");
					if (middleName != null && !middleName.trim().isEmpty()) {
						itemW.setPrefilledValue(middleName);
					} else {
						itemW.setPrefilledValue("");
					}
				} else if (URN_USER_LAST_NAME.equals(sourceAttribute)) {
					String lastName = parsedName.get("lastName");
					if (lastName != null && !lastName.trim().isEmpty())
						itemW.setPrefilledValue(lastName);
				} else if (URN_USER_DISPLAY_NAME.equals(sourceAttribute)) {

					// overwrite only if not filled by Perun
					if (itemW.getPrefilledValue() == null || itemW.getPrefilledValue().isEmpty()) {

						String displayName = "";

						if (parsedName.get("titleBefore") != null && !parsedName.get("titleBefore").isEmpty())
							displayName += parsedName.get("titleBefore");

						if (parsedName.get("firstName") != null && !parsedName.get("firstName").isEmpty()) {
							if (!displayName.isEmpty()) displayName += " ";
							displayName += parsedName.get("firstName");
						}
						if (parsedName.get("lastName") != null && !parsedName.get("lastName").isEmpty()) {
							if (!displayName.isEmpty()) displayName += " ";
							displayName += parsedName.get("lastName");
						}
						if (parsedName.get("titleAfter") != null && !parsedName.get("titleAfter").isEmpty()) {
							if (!displayName.isEmpty()) displayName += " ";
							displayName += parsedName.get("titleAfter");
						}

						itemW.setPrefilledValue(displayName);

					}

				}

			}
		}

		Map<Integer, ApplicationFormItemWithPrefilledValue> allItemsByIds = itemsWithValues.stream()
				.collect(toMap(item -> item.getFormItem().getId(), Function.identity()));

		for (ApplicationFormItemWithPrefilledValue itemW : itemsWithValues) {
			// We do require value from IDP (federation) if attribute is supposed to be pre-filled and item is required and not editable to users
			if (isEmpty(itemW.getPrefilledValue()) &&
					itemW.getFormItem().isRequired() &&
					(isItemHidden(itemW, allItemsByIds) || isItemDisabled(itemW, allItemsByIds))) {
				if (URN_USER_DISPLAY_NAME.equals(itemW.getFormItem().getPerunDestinationAttribute())) {
					log.error("Couldn't resolve displayName from: {}, parsedNames were: {}", federValues, parsedName);
				}
				itemsWithMissingData.add(itemW);
			}
		}

		if (module != null) {
			module.processFormItemsWithData(sess, appType, form, itemsWithValues);
		}

		if (!itemsWithMissingData.isEmpty() && extSourceType.equals(ExtSourcesManager.EXTSOURCE_IDP)) {
			// throw exception only if user is logged-in by Federation IDP
			String IDP = federValues.get("originIdentityProvider");
			log.error("[REGISTRAR] IDP {} doesn't provide data for following form items: {}", IDP, itemsWithMissingData);
			throw new MissingRequiredDataException("Your IDP doesn't provide data required by this application form.", itemsWithMissingData);
		}

		// return prefilled form
		return itemsWithValues;

	}

	/**
	 * Checks, if the given item will be displayed during the submission.
	 *
	 * @param formItemWithValue item that is checked
	 * @param allItemsByIds all items from the same form by their ids
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
	 * Checks, if the given item will be disabled during the submission.
	 *
	 * @param formItemWithValue item that is checked
	 * @param allItemsByIds all items from the same form by their ids
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
	 * Checks, if the given item matches the given validator, or its disabled dependency item, if specified.
	 *
	 * @param formItemWithValue the base item that is checked
	 * @param allItemsByIds all items by their ids
	 * @param validator validator that is used to check the given item, or its dependency item
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
			log.error("Application item has a dependency on item, which is not part of the same form. Item: {}", formItem);
			throw new InternalErrorException("Application item has a dependency on item, which is not part of the same form. Item: " + formItem);
		} else {
			return validator.apply(dependencyItem);
		}
	}

	/**
	 * Checks, if the given item matches the given validator, or its hidden dependency item, if specified.
	 *
	 * @param formItemWithValue the base item that is checked
	 * @param allItemsByIds all items by their ids
	 * @param validator validator that is used to check the given item, or its dependency item
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
			log.error("Application item has a dependency on item, which is not part of the same form. Item: {}", formItem);
			throw new InternalErrorException("Application item has a dependency on item, which is not part of the same. form. Item: " + formItem);
		}
		return validator.apply(dependencyItem);
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
	 * Checks, if the item will be prefilled in the gui.
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
		return Arrays.stream(enTexts.getOptions().split("\\|"))
				.map(option -> option.split("#")[0])
				.anyMatch(value -> formItemWithValue.getPrefilledValue().equals(value));
	}

	/**
	 * Check if user can submit application for specified form and type.
	 * Performs check on VO/Group membership, VO/Group expiration rules, form modules and duplicate (already submitted) applications.
	 *
	 * @param sess PerunSession for authz
	 * @param appType Type of application form
	 * @param form Application form
	 */
	private void checkDuplicateRegistrationAttempt(PerunSession sess, AppType appType, ApplicationForm form) throws DuplicateRegistrationAttemptException, AlreadyRegisteredException, PrivilegeException, ExtendMembershipException, RegistrarException, MemberNotExistsException, CantBeSubmittedException, NotGroupMemberException {

		Vo vo = form.getVo();
		Group group = form.getGroup();

		// get necessary params from session
		User user = sess.getPerunPrincipal().getUser();
		int extSourceLoa = sess.getPerunPrincipal().getExtSourceLoa();

		if (AppType.INITIAL.equals(appType)) {
			if (user != null) {
				//user is known
				try {
					Member m = membersManager.getMemberByUser(registrarSession, vo, user);
					if (group != null) {
						// get members groups
						List<Group> g = groupsManager.getMemberGroups(registrarSession, m);
						if (g.contains(group)) {
							// user is member of group - can't post more initial applications
							throw new AlreadyRegisteredException("You are already member of group "+group.getName()+".");
						} else {
							checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
							// pass if have approved or rejected app
						}
					} else {
						// user is member of vo, can't post more initial applications
						throw new AlreadyRegisteredException("You are already member of VO: "+vo.getName());
					}
				} catch (MemberNotExistsException ex) {
					// user is not member of vo
					if (group != null) {
						checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
						//throw new InternalErrorException("You must be member of vo: "+vo.getName()+" to apply for membership in group: "+group.getName());
					} else {
						checkDupplicateVoApplications(sess, vo, AppType.INITIAL);
						// pass not member and have only approved or rejected apps
					}
				}
			} else {
				// user is not known
				if (group != null) {
					checkDupplicateGroupApplications(sess, vo, group, AppType.INITIAL);
					//throw new InternalErrorException("You must be member of vo: "+vo.getName()+" to apply for membership in group: "+group.getName());
				} else {
					checkDupplicateVoApplications(sess, vo, AppType.INITIAL);
					// pass not member and have only approved or rejected apps
				}
			}
			// if false, throws exception with reason for GUI
			membersManager.canBeMemberWithReason(sess, vo, user, String.valueOf(extSourceLoa));
		}
		// if extension, user != null !!
		if (AppType.EXTENSION.equals(appType)) {
			if (user == null) {
				throw new RegistrarException("Trying to get extension application for non-existing user. Try to log-in with different identity.");
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
					throw new CantBeSubmittedException("Sponsored member cannot apply for membership extension, it must be extended by the sponsor.");
				}

			}

		}

	}

	@Override
	public boolean validateEmailFromLink(Map<String, String> urlParameters) throws PerunException {

		String idStr = urlParameters.get("i");
		if (mailManager.getMessageAuthenticationCode(idStr).equals(urlParameters.get("m"))) {
			int appDataId = Integer.parseInt(idStr, Character.MAX_RADIX);
			// validate mail
			jdbc.update("update application_data set assurance_level=1 where id = ?", appDataId);
			Application app = getApplicationById(jdbc.queryForInt("select app_id from application_data where id = ?", appDataId));
			if (app == null) {
				log.warn("Application for FormItemData ID: {} doesn't exists and therefore mail can't be verified.", appDataId);
				throw new RegistrarException("Application doesn't exists and therefore mail can't be verified.");
			}

			// if application is already approved or rejected, fake OK on mail validation and do nothing
			if (Arrays.asList(AppState.APPROVED, AppState.REJECTED).contains(app.getState())) return true;

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
					// when approval fails, we want this to be silently skipped, since for "user" called method did verified his mail address.
					log.warn("We couldn't auto-approve application {}, because of error: {}", app, ex);
				}
			}
			return true;
		}
		return false;

	}

	@Override
	public List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm form) throws PerunException {
		return getFormItems(sess, form, null);
	}

	@Override
	public List<ApplicationFormItemData> getApplicationDataById(PerunSession sess, int appId) throws PrivilegeException, RegistrarException {

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
	public void copyFormFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException {
		vosManager.checkVoExists(sess, fromVo);
		vosManager.checkVoExists(sess, toVo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyFormFromVoToVo_Vo_Vo_policy", fromVo) ||
			!AuthzResolver.authorizedInternal(sess, "copyFormFromVoToVo_Vo_Vo_policy", toVo)) {
			throw new PrivilegeException(sess, "copyFormFromVoToVo");
		}

		copyItems(sess, getFormForVo(fromVo), getFormForVo(toVo));
	}

	@Override
	public void copyFormFromVoToGroup(PerunSession sess, Vo fromVo, Group toGroup, boolean reverse) throws PerunException {
		vosManager.checkVoExists(sess, fromVo);
		groupsManager.checkGroupExists(sess, toGroup);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromVoToGroup_Vo_Group_Policy", Collections.singletonList(fromVo))
			|| !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromVoToGroup_Vo_Group_Policy", Collections.singletonList(toGroup))) {
			throw new PrivilegeException(sess, "copyFormFromVoToGroup");
		}

		if (reverse) {
			copyItems(sess, getFormForGroup(toGroup), getFormForVo(fromVo));
		} else {
			copyItems(sess, getFormForVo(fromVo), getFormForGroup(toGroup));
		}
	}

	@Override
	public void copyFormFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException {
		groupsManager.checkGroupExists(sess, fromGroup);
		groupsManager.checkGroupExists(sess, toGroup);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "source-copyFormFromGroupToGroup_Group_Group_policy", Collections.singletonList(fromGroup))
			|| !AuthzResolver.authorizedInternal(sess, "destination-copyFormFromGroupToGroup_Group_Group_policy", Collections.singletonList(toGroup))) {
			throw new PrivilegeException(sess, "copyFormFromGroupToGroup");
		}

		copyItems(sess, getFormForGroup(fromGroup), getFormForGroup(toGroup));
	}

	/**
	 * Copy items from one form to another.
	 *
	 * @param sess session
	 * @param fromForm the form from which the items are taken
	 * @param toForm the form where the items are added
	 */
	private void copyItems(PerunSession sess, ApplicationForm fromForm, ApplicationForm toForm) throws PerunException {
		List<ApplicationFormItem> items = getFormItems(sess, fromForm);
		Map<Integer, Integer> oldToNewIDs = new HashMap<>();
		for (ApplicationFormItem item : items) {
			Integer oldId = item.getId();
			item.setOrdnum(null); // reset order, id is always new inside add method
			item = addFormItem(sess, toForm, item);
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

	public void updateApplicationUser(PerunSession sess, Application app) {

		jdbc.update("update application set user_id=?, modified_at=" + Compatibility.getSysdate() + ", modified_by=? where id=?",
				(app.getUser() != null) ? app.getUser().getId() : null,
				sess.getPerunPrincipal().getActor(),
				app.getId());

	}

	public void updateFormItemData(PerunSession sess, int appId, ApplicationFormItemData data) throws RegistrarException, PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateFormItemData_int_ApplicationFormItemData_policy")) {
			throw new PrivilegeException(sess, "updateFormItemData");
		}

		Application app = getApplicationById(sess, appId);
		if (AppState.APPROVED.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) throw new RegistrarException("Form items of once approved or rejected applications can't be modified.");

		ApplicationFormItemData existingData = getFormItemDataById(data.getId(), appId);
		if (existingData == null) throw new RegistrarException("Form item data specified by ID: "+ data.getId() + " not found or doesn't belong to the application "+appId);

		List<Type> notAllowed = Arrays.asList(USERNAME, PASSWORD, HEADING, HTML_COMMENT, SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);

		if (notAllowed.contains(existingData.getFormItem().getType())) throw new RegistrarException("You are not allowed to modify "+existingData.getFormItem().getType()+" type of form items.");

		if (!existingData.getFormItem().isUpdatable()) {
			throw new RegistrarException("The item " + existingData.getFormItem().getShortname() + " is not allowed to be updated.");
		}
		updateFormItemData(sess, data);

	}

	@Transactional(rollbackFor = Exception.class)
	public void updateFormItemsData(PerunSession sess, int appId, List<ApplicationFormItemData> data) throws PerunException {

		Application app = getApplicationById(appId);

		if (app == null) throw new InternalErrorException("Application with ID="+appId+" doesn't exist.");

		if (!AuthzResolver.selfAuthorizedForApplication(sess, app)) {
			throw new PrivilegeException(sess, "updateFormItemsData");
		}

		if (AppState.APPROVED.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) throw new RegistrarException("Form items of once approved or rejected applications can't be modified.");

		// no data to change
		if (data == null || data.isEmpty()) return;

		for (ApplicationFormItemData dataItem : data) {

			ApplicationFormItemData existingData = getFormItemDataById(dataItem.getId(), appId);
			if (existingData == null) throw new RegistrarException("Form item data specified by ID: " + dataItem.getId() + " not found or doesn't belong to the application " + appId);

			List<Type> notAllowed = Arrays.asList(USERNAME, PASSWORD, HEADING, HTML_COMMENT, SUBMIT_BUTTON, AUTO_SUBMIT_BUTTON);

			if (notAllowed.contains(existingData.getFormItem().getType()))
				throw new RegistrarException("You are not allowed to modify " + existingData.getFormItem().getType() + " type of form items.");
			if (!existingData.getFormItem().isUpdatable()) {
				throw new RegistrarException("The item " + existingData.getFormItem().getShortname() + " is not allowed to be updated.");
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

	private void updateFormItemData(PerunSession session, ApplicationFormItemData dataItem) {
		try {
			if (VALIDATED_EMAIL.equals(dataItem.getFormItem().getType())) {
				handleLoaForValidatedMail(session, dataItem);
			}
			int result = jdbc.update("update application_data set value=? , assurance_level=? where id=?",
					dataItem.getValue(), ((isBlank(dataItem.getAssuranceLevel())) ? null : dataItem.getAssuranceLevel()),
					dataItem.getId());
			log.info("{} manually updated form item data {}", session.getPerunPrincipal(), dataItem);
			if (result != 1) {
				throw new InternalErrorException("Unable to update form item data");
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
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
				if (shibIdentityProvider != null && extSourcesWithMultipleIdentifiers.contains(shibIdentityProvider)) {
					String principalAdditionalIdentifiers = principal.getAdditionalInformations().get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
					if (principalAdditionalIdentifiers == null) {
						//This should not happen
						throw new InternalErrorException("Entry " + UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME + " is not defined in the principal's additional information. Either it was not provided by external source used for sign-in or the mapping configuration is wrong.");
					}
					LinkedHashMap<String, String> additionalFedAttributes = BeansUtils.stringToMapOfAttributes(application.getFedInfo());
					String applicationAdditionalIdentifiers = additionalFedAttributes.get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
					List<String> identifiersInIntersection = BeansUtils.additionalIdentifiersIntersection(principalAdditionalIdentifiers, applicationAdditionalIdentifiers);
					if (!identifiersInIntersection.isEmpty()) {
						filteredApplications.add(application);
					}
				}
				//check existing application by extSourceName and extSource login
				else if (extSourceName.equals(application.getExtSourceName()) && actor.equals(application.getCreatedBy())) {
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
							Attribute attribute = attrManager.getAttribute(sess, ues, UsersManagerBl.ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
							if (attribute.getValue() != null) {
								List<String> userIdentifiers = attribute.valueAsList();
								// Creates Arrays from principal and application identifiers and makes intersection between them.
								LinkedHashMap<String, String> additionalFedAttributes = BeansUtils.stringToMapOfAttributes(application.getFedInfo());
								String applicationAdditionalIdentifiers = additionalFedAttributes.get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
								String[] applicationIdentifiersArray = {};
								if (applicationAdditionalIdentifiers != null) {
									applicationIdentifiersArray = applicationAdditionalIdentifiers.split(UsersManagerBl.MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX);
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
	 * Retrieve form item data by its ID or NULL if not exists.
	 * It also expect, that item belongs to the passed application ID, if not, NULL is returned.
	 *
	 * @param formItemDataId ID of form item data entry
	 * @param applicationId ID of application this item belongs to
	 * @return Form item with data submitted by the User.
	 * @throws InternalErrorException When implementation fails
	 */
	private ApplicationFormItemData getFormItemDataById(int formItemDataId, int applicationId) {

		try {
			return jdbc.queryForObject("select id,item_id,shortname,value,assurance_level from application_data where id=? and app_id=?",
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
			throw new InternalErrorException("Unable to get form item data by its ID:" + formItemDataId + " and application ID: " + applicationId, ex);
		}

	}

	@Override
	public MailManager getMailManager() {
		return this.mailManager;
	}

	@Override
	public ConsolidatorManager getConsolidatorManager() {
		return this.consolidatorManager;
	}

	/**
	 * Set application to VERIFIED state if all it's
	 * mails (VALIDATED_EMAIL) have assuranceLevel >= 1 and have non-empty value (there is anything to validate).
	 * Returns TRUE if succeeded, FALSE if some mail still waits for verification.
	 *
	 * @param sess user who try to verify application
	 * @param app application to verify
	 * @return TRUE if verified / FALSE if not verified
	 * @throws InternalErrorException
	 */
	private boolean tryToVerifyApplication(PerunSession sess, Application app) throws PerunException {

		// test all fields that may need to be validated and are not empty !!
		List<Integer> loas = jdbc.query("select d.assurance_level"+Compatibility.castToInteger()+" from application a, application_form_items i, application_data d " +
						"where d.app_id=a.id and d.item_id=i.id and a.id=? and i.type=? and d.value is not null",
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

	/**
	 * Forcefully marks application as VERIFIED
	 * (only if was in NEW state before)
	 *
	 * @param sess session info to use for modified_by
	 * @param appId ID of application to verify.
	 */
	private void markApplicationVerified(PerunSession sess, int appId) {

		try {
			if (jdbc.update("update application set state=?, modified_at=" + Compatibility.getSysdate() + ", modified_by=? where id=? and state=?", AppState.VERIFIED.toString(), sess.getPerunPrincipal().getActor(), appId, AppState.NEW.toString()) > 0) {
				log.info("Application {} marked as VERIFIED", appId);
			} else {
				log.info("Application {} not marked VERIFIED, was not in state NEW", appId);
			}
		} catch (InternalErrorException ex) {
			log.error("Application {} NOT marked as VERIFIED due to error {}", appId, ex);
		}

	}

	/**
	 * Forcefully set application its state (NEW/VERIFIED/...)
	 *
	 * @param sess PerunSession
	 * @param appId ID of application
	 * @param appState AppState to be set
	 */
	private void setApplicationState(PerunSession sess, int appId, AppState appState) {
		try {
			jdbc.update("update application set state=?, modified_at=" + Compatibility.getSysdate() + ", modified_by=? where id=?",
					appState.toString(), sess.getPerunPrincipal().getActor(), appId);
		} catch (RuntimeException ex) {
			log.error("Unable to set application state: {}, to application ID: {}", appState, appId, ex);
			throw new InternalErrorException("Unable to set application state: "+appState+" to application: "+appId, ex);
		}
	}

	/**
	 * Try to approve application if auto-approve is possible
	 *
	 * @param sess user who try to approves application
	 * @param app application to approve
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

		if (AppType.INITIAL.equals(type) && !form.isAutomaticApproval()) return;
		if (AppType.EXTENSION.equals(type) && !form.isAutomaticApprovalExtension()) return;

		// do not auto-approve Group applications, if user is not member of VO
		if (app.getGroup() != null && app.getVo() != null) {

			try {
				if (app.getUser() == null) {
					LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
					PerunPrincipal applicationPrincipal = new PerunPrincipal(app.getCreatedBy(), app.getExtSourceName(), app.getExtSourceType(), app.getExtSourceLoa(), additionalAttributes);
					User u = perun.getUsersManagerBl().getUserByExtSourceInformation(sess, applicationPrincipal);
					if (u != null) {
						membersManager.getMemberByUser(sess, app.getVo(), u);
					} else {
						// user not found or null, hence can't be member of VO -> do not approve.
						return;
					}
				} else {
					// user known, but maybe not member of a vo
					membersManager.getMemberByUser(sess, app.getVo(), app.getUser());
				}
			} catch (MemberNotExistsException ex) {
				return;
			} catch (UserNotExistsException ex) {
				return;
			} catch (UserExtSourceNotExistsException ex) {
				return;
			} catch (ExtSourceNotExistsException ex) {
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
						throw new RegistrarException("Similar users are already registered in system. Automatic approval of application was canceled to prevent creation of duplicate user entry. Please check and approve application manually.");
					} else {
						// similar NOT found - continue
						approveApplication(registrarSession, app.getId());
					}
				} else { }

				*/

				// other types of application doesn't create new user - continue
				approveApplication(registrarSession, app.getId());

			}
		} catch (Exception ex) {

			ArrayList<Exception> list = new ArrayList<>();
			list.add(ex);
			getMailManager().sendMessage(app, MailType.APP_ERROR_VO_ADMIN, null, list);

			throw ex;
		}

	}

	/**
	 * Retrieves whole application object from DB
	 * (authz in parent methods)
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

	/**
	 * Extract names for User from his federation attributes
	 *
	 * @param federValues map of federation attribute names to their value
	 * @return map with exctracted names
	 */
	private Map<String, String> extractNames(Map<String, String> federValues) {

		String commonName = federValues.get(shibCommonNameVar);
		String displayName = federValues.get(shibDisplayNameVar);

		Map<String, String> parsedName;
		if (displayName != null && !displayName.isEmpty()) {
			parsedName = Utils.parseCommonName(displayName);
		} else if (commonName != null && !commonName.isEmpty()) {
			parsedName = Utils.parseCommonName(commonName);
		} else {
			parsedName = new HashMap<>();
		}
		// if the idp provided first name or last name, always use it
		String fedFirstName = federValues.get(shibFirstNameVar);
		String fedLastName = federValues.get(shibLastNameVar);

		setIfNotEmpty(parsedName, fedFirstName, "firstName");
		setIfNotEmpty(parsedName, fedLastName, "lastName");

		// do new parsing heuristic
		Candidate candidate = new Candidate();
		if (displayName != null && !displayName.isEmpty() &&
				fedFirstName != null && !fedFirstName.isEmpty() &&
				fedLastName != null && !fedLastName.isEmpty()) {
			parseTitlesAndMiddleName(candidate, displayName, fedFirstName, fedLastName);
		}

		setIfNotEmpty(parsedName, candidate.getMiddleName(), "middleName");
		setIfNotEmpty(parsedName, candidate.getTitleBefore(), "titleBefore");
		setIfNotEmpty(parsedName, candidate.getTitleAfter(), "titleAfter");

		return parsedName;

	}

	/**
	 * If the given value is not null and not empty, put it in the given map with the given key.
	 *
	 * @param map map
	 * @param value value which is checked
	 * @param key key
	 */
	private void setIfNotEmpty(Map<String, String> map, String value, String key) {
		if (value != null && !value.isEmpty()) {
			map.put(key, value);
		}
	}

	/**
	 * Return RegistrarModule for specific application form (VO or Group)
	 * so it can be used for more actions.
	 *
	 * @param form application form
	 * @return RegistrarModule if present or null
	 */
	private RegistrarModule getRegistrarModule(ApplicationForm form) {

		if (form == null) {
			// wrong input
			log.error("[REGISTRAR] Application form is null when getting it's registrar module.");
			throw new NullPointerException("Application form is null when getting it's registrar module.");
		}

		if (form.getModuleClassName() != null && !form.getModuleClassName().trim().isEmpty()) {

			RegistrarModule module = null;

			try {
				log.debug("[REGISTRAR] Attempting to instantiate class: {}", MODULE_PACKAGE_PATH + form.getModuleClassName());
				module = (RegistrarModule) Class.forName(MODULE_PACKAGE_PATH + form.getModuleClassName()).newInstance();
				module.setRegistrar(registrarManager);
			} catch (Exception ex) {
				log.error("[REGISTRAR] Exception when instantiating module.", ex);
				return module;
			}
			log.debug("[REGISTRAR] Class {} successfully created.", MODULE_PACKAGE_PATH + form.getModuleClassName());

			return module;

		}

		return null;

	}

	/**
	 * If titles before / after name are part of application form and User exists,
	 * update titles for user according to application.
	 *
	 * This method doesn't clear titles from users name if sent empty in order to prevent
	 * accidental removal when user log-in with different IDP without titles provided.
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
				log.debug("[REGISTRAR] User to update titles: {}", user);
				usersManager.updateNameTitles(registrarSession, user);
			}

		} catch (Exception ex) {
			log.error("[REGISTRAR] Exception when updating titles.", ex);
		}

	}

	/**
	 * Store values from application data as user/member attributes
	 *
	 * New values are set if old are empty, or merged if not empty.
	 * Empty new values are skipped (not even merged) as well as core attributes.
	 *
	 * User and Member must already exists !!
	 *
	 * !! LOGIN ATTRIBUTES ARE SKIPPED BY THIS METHOD AND MUST BE
	 * SET LATER BY storeApplicationLoginAttributes() METHOD !!
	 * !! USE unreserveNewLoginsFromSameNamespace() BEFORE DOING SO !!
	 *
	 * @param app Application to process attributes for
	 * @throws UserNotExistsException When User present in Application not exists
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When caller is not authorized for some action
	 * @throws MemberNotExistsException When Member resolved from VO/User from Application doesn't exist
	 * @throws VoNotExistsException When VO resolved from application doesn't exist
	 * @throws RegistrarException When implementation fails
	 * @throws AttributeNotExistsException When expected attribute doesn't exists
	 * @throws WrongAttributeAssignmentException When attribute can't be stored because of wrongly passed params
	 * @throws WrongAttributeValueException  When attribute can't be stored because of wrong value
	 * @throws WrongReferenceAttributeValueException  When attribute can't be stored because of some specific dynamic constraint (from attribute module)
	 */
	private void storeApplicationAttributes(Application app) throws UserNotExistsException, PrivilegeException, MemberNotExistsException, VoNotExistsException, RegistrarException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {

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
			// do not store null or empty values at all
			if (newValue == null || newValue.isEmpty()) continue;
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

				// NEVER STORE LOGINS THIS WAY TO PREVENT ACCIDENTAL OVERWRITE
				if (a != null && "login-namespace".equals(a.getBaseFriendlyName())) {
					continue;
				}

				// if attribute exists
				if (a != null) {
					if (a.getType().equalsIgnoreCase(LinkedHashMap.class.getName())) {
						// FIXME do not set hash map attributes - not supported in GUI and registrar
						continue;
					} else if (a.getType().equalsIgnoreCase(ArrayList.class.getName())) {
						// we expects that list contains strings
						ArrayList<String> value = a.valueAsList();
						// if value not present in list => add
						if (value == null) {
							// set as new value
							value = new ArrayList<>();
							value.add(newValue);
						} else if (!value.contains(newValue)) {
							// add value between old values
							value.add(newValue);
						}
						a.setValue(value);
						attributes.add(a);
						continue;
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
	 *
	 * New values are set only if old are empty to prevent overwrite when joining identities.
	 * Empty new values are skipped.
	 *
	 * User must already exists !!
	 *
	 * @param app Application to process attributes for
	 * @throws UserNotExistsException When User present in Application not exists
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When caller is not authorized for some action
	 * @throws RegistrarException When implementation fails
	 * @throws AttributeNotExistsException When expected attribute doesn't exists
	 * @throws WrongAttributeAssignmentException When login can't be stored because of wrongly passed params
	 * @throws WrongAttributeValueException  When login can't be stored because of wrong value
	 * @throws WrongReferenceAttributeValueException  When login can't be stored because of some specific dynamic constraint (from attribute module)
	 */
	private void storeApplicationLoginAttributes(Application app) throws UserNotExistsException, PrivilegeException, RegistrarException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {

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
			if (newValue == null || newValue.isEmpty()) continue;
			// if correct destination attribute
			if (destAttr != null && !destAttr.isEmpty()) {
				// get login attribute (for user only)
				Attribute a;
				if (destAttr.contains(AttributesManager.NS_USER_ATTR_DEF+":login-namespace:")) {
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

	/**
	 * Unreserve new login/password from KDC if user already have login in same namespace
	 *
	 * !! must be called before setting new attributes from application !!
	 *
	 * @param logins list of all new logins/namespaces pairs passed by application
	 * @param user user to check logins for
	 *
	 * @return List of login/namespace pairs which are purely new and can be set to user and validated in KDC
	 */
	private List<Pair<String, String>> unreserveNewLoginsFromSameNamespace(List<Pair<String, String>> logins, User user) throws PasswordDeletionFailedException, PasswordOperationTimeoutException, LoginNotExistsException, InvalidLoginException {

		List<Pair<String, String>> result = new ArrayList<>();

		List<Attribute> loginAttrs = perun.getAttributesManagerBl().getLogins(registrarSession, user);

		for (Pair<String, String> pair : logins) {
			boolean found = false;
			for (Attribute a : loginAttrs) {
				if (pair.getLeft().equals(a.getFriendlyNameParameter())) {
					// old login found in same namespace => unreserve new login from KDC
					usersManager.deletePassword(registrarSession, pair.getRight(), pair.getLeft());
					log.debug("[REGISTRAR] Unreserving new login: {} in namespace: {} since user already have login: {} in same namespace."
							, pair.getRight(), pair.getLeft(), a.getValue());
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

	/**
	 * Try to approve all group applications of user with auto-approval (even by user-ext-source)
	 * in specified VO.
	 *
	 * @param sess PerunSession
	 * @param vo VO to approve group applications in
	 * @param user user to approve applications for
	 */
	private void autoApproveUsersGroupApplications(PerunSession sess, Vo vo, User user) throws PerunException {

		// get group apps based on the vo
		List<Application> apps = jdbc.query(
			APP_SELECT + " where a.vo_id=? and a.group_id is not null and a.state=?",
			APP_MAPPER, vo.getId(), AppState.VERIFIED.toString());

		//filter only user's apps
		List<Application> applications = filterUserApplications(sess, user, apps);

		for (Application a : applications) {
			// if new => skipp user will approve automatically by verifying email
			if (a.getState().equals(AppState.NEW)) continue;

			// approve applications only for auto-approve forms
			if (!getFormForGroup(a.getGroup()).isAutomaticApproval() && AppType.INITIAL.equals(a.getType())) continue;
			if (!getFormForGroup(a.getGroup()).isAutomaticApprovalExtension() && AppType.EXTENSION.equals(a.getType())) continue;

			try {
				registrarManager.approveApplicationInternal(sess, a.getId());
			} catch (RegistrarException ex) {
				// case when user have UNVERIFIED group application
				// will be approved when user verify his email
				log.error("[REGISTRAR] Can't auto-approve group application after vo app approval because of exception.", ex);
			}

		}

	}

	/**
	 * Return string representation (key) of application used for locking main operations like "create/verify/approve/reject".
	 *
	 * @param application Application to get key for
	 * @return Key for Application
	 */
	private String getLockKeyForApplication(Application application) {

		return application.getType().toString() +
				application.getVo().getShortName() +
				((application.getGroup() != null) ? application.getGroup().getName() : "nogroup") +
				application.getCreatedBy()+application.getExtSourceName()+application.getExtSourceType();

	}

	/**
	 * If user provided value is the same as was pre-filled from Perun, then we set LOA=2
	 * If user provided value is between those provided by Federation, then we keep provided LOA (0 will require mail validation, >0 will skip it).
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
			prefilledValues.addAll(Arrays.stream(itemData.getPrefilledValue().split(";")).map(String::toLowerCase).collect(Collectors.toList()));
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
	 * Method creates a candidate object from application according to the application id.
	 *
	 * @param app the application
	 * @return Candidate
	 */
	private Candidate createCandidateFromApplicationData(Application app) {
		// put application data into Candidate
		final Map<String, String> attributes = new HashMap<>();
		jdbc.query("select dst_attr,value from application_data d, application_form_items i where d.item_id=i.id "
				+ "and i.dst_attr is not null and d.value is not null and app_id=?",
			(resultSet, i) -> {
				attributes.put(resultSet.getString("dst_attr"), resultSet.getString("value"));
				return null;
			}, app.getId());

		Map<String, String> fedData = BeansUtils.stringToMapOfAttributes(app.getFedInfo());

		// DO NOT STORE LOGINS THROUGH CANDIDATE
		// we do not set logins by candidate object to prevent accidental overwrite while joining identities in process
		attributes.entrySet().removeIf(entry -> entry.getKey().contains("urn:perun:user:attribute-def:def:login-namespace:"));

		Candidate candidate = new Candidate();
		candidate.setAttributes(attributes);

		log.debug("[REGISTRAR] Retrieved candidate from DB {}", candidate);

		// first try to parse display_name if not null and not empty
		parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

		// if names are separated, used them after
		for (String attrName : attributes.keySet()) {
			// if value not null or empty - set to candidate
			if (attributes.get(attrName) != null
				&& !attributes.get(attrName).isEmpty()) {
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

	public void parseNamesFromDisplayName(Candidate candidate, Map<String, String> attributes) {
		if (containsNonEmptyValue(attributes, URN_USER_DISPLAY_NAME)) {
			// parse
			Map<String, String> commonName = Utils.parseCommonName(attributes.get(URN_USER_DISPLAY_NAME));
			if (commonName.get("titleBefore") != null
				&& !commonName.get("titleBefore").isEmpty()) {
				candidate.setTitleBefore(commonName.get("titleBefore"));
			}
			if (commonName.get("firstName") != null
				&& !commonName.get("firstName").isEmpty()) {
				candidate.setFirstName(commonName.get("firstName"));
			}
			// FIXME - ? there is no middleName in Utils.parseCommonName() implementation
			if (commonName.get("middleName") != null
				&& !commonName.get("middleName").isEmpty()) {
				candidate.setMiddleName(commonName.get("middleName"));
			}
			if (commonName.get("lastName") != null
				&& !commonName.get("lastName").isEmpty()) {
				candidate.setLastName(commonName.get("lastName"));
			}
			if (commonName.get("titleAfter") != null
				&& !commonName.get("titleAfter").isEmpty()) {
				candidate.setTitleAfter(commonName.get("titleAfter"));
			}
		}
	}

	/**
	 * Check if the given fed info contains givenName and sn (surname). If so,
	 * it sets it to the candidate and tries to match titles and middle name from
	 * display name.
	 *
	 * @param candidate candidate
	 * @param attributes attributes with values
	 * @param fedInfo key-value info from idp
	 */
	public void parseNamesFromDisplayNameAndFedInfo(Candidate candidate, Map<String, String> attributes,
	                                                Map<String, String> fedInfo) {
		if (fedInfo != null && containsNonEmptyValue(fedInfo, shibFirstNameVar) &&
				containsNonEmptyValue(fedInfo, shibLastNameVar)) {
			String firstName = fedInfo.get(shibFirstNameVar);
			String lastName = fedInfo.get(shibLastNameVar);

			candidate.setFirstName(firstName);
			candidate.setLastName(lastName);

			tryToParseTitlesAndMiddleName(candidate, attributes, firstName, lastName);
		} else {
			parseNamesFromDisplayName(candidate, attributes);
		}
	}

	/**
	 * If the given map of attributes contains a user display name, it tries to match
	 * the given firstName and lastName and find titles and middle name.
	 *
	 * @param candidate candidate
	 * @param attributes map of attributes with values
	 * @param firstName first name to match
	 * @param lastName last name to match
	 */
	private void tryToParseTitlesAndMiddleName(Candidate candidate, Map<String, String> attributes, String firstName,
	                                           String lastName) {
		if (containsNonEmptyValue(attributes, URN_USER_DISPLAY_NAME)) {
			String displayName = attributes.get(URN_USER_DISPLAY_NAME);
			parseTitlesAndMiddleName(candidate, displayName, firstName, lastName);
		}
	}

	private void parseTitlesAndMiddleName(Candidate candidate, String displayName, String firstName, String lastName) {
		Pattern pattern = getNamesPattern(firstName, lastName);
		if (!tryToParseTitlesAndMiddleNameFromPattern(candidate, displayName, pattern, firstName)) {
			Pattern reversePattern = getNamesPattern(lastName, firstName);
			tryToParseTitlesAndMiddleNameFromPattern(candidate, displayName, reversePattern, lastName);
		}
	}

	/**
	 * Tries to match the given pattern to the given display name. If it matches, its sets
	 * titles and middle name from matcher of the given pattern to the given candidate.
	 *
	 * This method expects the pattern to define 3 groups in order - 1. Titles before, 2. Middle name, 3. Titles after
	 *
	 * @param candidate candidate
	 * @param displayName display name
	 * @param pattern pattern with 3 matching groups
	 * @return true, if the matcher matched
	 */
	private boolean tryToParseTitlesAndMiddleNameFromPattern(Candidate candidate, String displayName, Pattern pattern, String firstName) {
		Matcher matcher = pattern.matcher(displayName);
		if (!matcher.matches()) {
			return false;
		}
		if (matcher.groupCount() != 3) {
			throw new InternalErrorException("Expected pattern with 3 groups to match - titles before, middle name and " +
					"titles after, but get " + matcher.groupCount() + " groups." );
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
	 * To given candidate, sets titleBefore from trim of given value, or null if empty.
	 *
	 * @param candidate candidate
	 * @param value value
	 */
	private void parseTitlesBefore(Candidate candidate, String value) {
		candidate.setTitleBefore(alnumPattern.matcher(value).matches() ? value : null);
	}

	/**
	 * To given candidate, sets middle name from trim of given value.
	 * @param candidate candidate
	 * @param value value
	 */
	private void parseMiddleName(Candidate candidate, String value) {
		candidate.setMiddleName(alnumPattern.matcher(value).matches() ? value : null);
	}

	/**
	 * To given candidate, sets titleAfter from trim of given value, or null if empty.
	 *
	 * @param candidate candidate
	 * @param value value
	 */
	private void parseTitlesAfter(Candidate candidate, String value) {
		candidate.setTitleAfter(alnumPattern.matcher(value).matches() ? value : null);
	}

	/**
	 * Generates pattern for parsing titles and middle name from given values.
	 *
	 * The pattern is of format: ^(.*){firstName}(.*){lastName}(.*)$
	 *
	 * @param firstName first name
	 * @param lastName last name
	 * @return pattern for parsing titles and middle name
	 */
	private Pattern getNamesPattern(String firstName, String lastName) {
		return Pattern.compile("^(.*)" + firstName + "(.*)" + lastName + "(.*)$");
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
	 * Check whether a principal in perun session has already created application in group
	 *
	 * @param sess perun session containing principal
	 * @param vo for application
	 * @param group for application
	 * @param applicationType type of application
	 * @throws DuplicateRegistrationAttemptException if the principal has already created application
	 * @throws RegistrarException
	 * @throws PrivilegeException
	 */
	private void checkDupplicateGroupApplications(PerunSession sess, Vo vo, Group group, AppType applicationType) throws DuplicateRegistrationAttemptException, RegistrarException, PrivilegeException {
		// select neccessary information from already existing Group applications
		List<Application> applications = new ArrayList<>(jdbc.query(
			"select id, user_id, created_by, extSourceName, fed_info from application where apptype=? and vo_id=? and group_id=? and (state=? or state=?)",
			IDENTITY_APP_MAPPER,
			applicationType.toString(), vo.getId(), group.getId(), AppState.NEW.toString(), AppState.VERIFIED.toString()));
		// not member of VO - check for unprocessed applications to Group
		List<Application> filteredApplications = filterPrincipalApplications(sess, applications);
		if (!filteredApplications.isEmpty()) {
			// user have unprocessed application for group
			throw new DuplicateRegistrationAttemptException(
				"Application for Group: "+group.getName()+" already exists.",
				getApplicationById(filteredApplications.get(0).getId()),
				getApplicationDataById(registrarSession, filteredApplications.get(0).getId()));
		}
	}

	/**
	 * Check whether a principal in perun session has already created application in vo
	 *
	 * @param sess perun session containing principal
	 * @param vo for application
	 * @param applicationType type of application
	 * @throws DuplicateRegistrationAttemptException if the principal has already created application
	 * @throws RegistrarException
	 * @throws PrivilegeException
	 */
	private void checkDupplicateVoApplications(PerunSession sess, Vo vo, AppType applicationType) throws DuplicateRegistrationAttemptException, RegistrarException, PrivilegeException {
		// select neccessary information from already existing Vo applications
		List<Application> applications = jdbc.query(
			"select id, user_id, created_by, extSourceName, fed_info from application where apptype=? and vo_id=? and group_id is null and (state=? or state=?)",
			IDENTITY_APP_MAPPER,
			applicationType.toString(), vo.getId(), AppState.NEW.toString(), AppState.VERIFIED.toString());
		// not member of VO - check for unprocessed applications
		List<Application> filteredApplications = filterPrincipalApplications(sess, applications);
		if (!filteredApplications.isEmpty()) {
			// user have unprocessed application for VO - can't post more
			throw new DuplicateRegistrationAttemptException(
				"Application for VO: "+vo.getName()+" already exists.",
				getApplicationById(filteredApplications.get(0).getId()),
				getApplicationDataById(registrarSession, filteredApplications.get(0).getId()));
		}
	}

	private int processApplication(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {

		// If user is known in Perun but unknown in GUI (user joined identity by consolidator)
		if (application.getUser() == null && session.getPerunPrincipal().getUser() != null) {
			application.setUser(session.getPerunPrincipal().getUser());
		}

		// lock to prevent multiple submission of same application on server side
		String key = getLockKeyForApplication(application);

		synchronized(runningCreateApplication) {
			if (runningCreateApplication.contains(key)) {
				throw new AlreadyProcessingException("Your application submission is being processed already.");
			} else {
				runningCreateApplication.add(key);
			}
		}

		// store user-ext-source attributes and redirectURL from session to application object
		LinkedHashMap<String, String> map = new LinkedHashMap<>(session.getPerunPrincipal().getAdditionalInformations());
		if (application.getFedInfo() != null && application.getFedInfo().contains("redirectURL")) {
			String redirectURL = StringUtils.substringBetween(application.getFedInfo().substring(application.getFedInfo().indexOf("redirectURL")), "\"", "\"");
			map.put("redirectURL", redirectURL);
		}
		String additionalAttrs = BeansUtils.attributeValueToString(map, LinkedHashMap.class.getName());
		application.setFedInfo(additionalAttrs);

		Application app;
		try {

			// throws exception if user already submitted application or is already a member or can't submit it by VO/Group expiration rules.
			checkDuplicateRegistrationAttempt(session, application.getType(), (application.getGroup() != null) ? getFormForGroup(application.getGroup()) : getFormForVo(application.getVo()));

			// using this to init inner transaction
			// all minor exceptions inside are catched, if not, it's ok to throw them out
			app = this.registrarManager.createApplicationInternal(session, application, data);
		} catch (Exception ex) {
			// clear flag and re-throw exception, since application was processed with exception
			synchronized (runningCreateApplication) {
				runningCreateApplication.remove(key);
			}
			throw ex;
		}

		// try to verify (or even auto-approve) application
		try {
			boolean verified = tryToVerifyApplication(session, app);
			if (verified) {
				// try to APPROVE if auto approve
				tryToAutoApproveApplication(session, app);
			} else {
				// send request validation notification
				getMailManager().sendMessage(app, MailType.MAIL_VALIDATION, null, null);
			}
			// refresh current session, if submission was successful,
			// since user might have been created.
			AuthzResolverBlImpl.refreshSession(session);
		} catch (Exception ex) {
			log.error("[REGISTRAR] Unable to verify or auto-approve application {}, because of exception {}", app, ex);
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

		return app.getId();

	}

	@Override
	public void updateApplicationType(PerunSession session, Application application) {

		// TODO - add authorization (and add to rpc)

		if (jdbc.update("update application set apptype=? where id=?", application.getType().toString(), application.getId()) > 0) {
			log.debug("Application type changed to + " + application.getType());
		}
	}

	// ------------------ MAPPERS AND SELECTS -------------------------------------

	// FIXME - we are retrieving GROUP name using only "short_name" so it's not same as getGroupById()
	static final String APP_SELECT = "select a.id as id,a.vo_id as vo_id, a.group_id as group_id,a.apptype as apptype,a.fed_info as fed_info,a.state as state," +
			"a.user_id as user_id,a.extsourcename as extsourcename, a.extsourcetype as extsourcetype, a.extsourceloa as extsourceloa, a.user_id as user_id, a.created_at as app_created_at, a.created_by as app_created_by, a.modified_at as app_modified_at, a.modified_by as app_modified_by, " +
			"v.name as vo_name, v.short_name as vo_short_name, v.created_by as vo_created_by, v.created_at as vo_created_at, v.created_by_uid as vo_created_by_uid, v.modified_by as vo_modified_by, " +
			"v.modified_at as vo_modified_at, v.modified_by_uid as vo_modified_by_uid, g.name as group_name, g.dsc as group_description, g.created_by as group_created_by, g.created_at as group_created_at, g.modified_by as group_modified_by, g.created_by_uid as group_created_by_uid, g.modified_by_uid as group_modified_by_uid," +
			"g.modified_at as group_modified_at, g.vo_id as group_vo_id, g.parent_group_id as group_parent_group_id, g.uu_id as group_uu_id, u.first_name as user_first_name, u.last_name as user_last_name, u.middle_name as user_middle_name, " +
			"u.title_before as user_title_before, u.title_after as user_title_after, u.service_acc as user_service_acc, u.sponsored_acc as user_sponsored_acc , u.uu_id as user_uu_id from application a left outer join vos v on a.vo_id = v.id left outer join groups g on a.group_id = g.id left outer join users u on a.user_id = u.id";

	private static final String APP_TYPE_SELECT = "select apptype from application_form_item_apptypes";

	private static final String FORM_SELECT = "select id,vo_id,group_id,automatic_approval,automatic_approval_extension,module_name from application_form";

	private static final String FORM_ITEM_SELECT = "select id,ordnum,shortname,required,type,fed_attr,src_attr,dst_attr,regex,hidden,disabled,hidden_dependency_item_id,disabled_dependency_item_id,updatable from application_form_items";

	private static final String FORM_ITEM_TEXTS_SELECT = "select locale,label,options,help,error_message from application_form_item_texts";

	private static final RowMapper<Application> IDENTITY_APP_MAPPER = (resultSet, i) -> {
		Application app = new Application();
		app.setId(resultSet.getInt("id"));
		app.setUser(new User(resultSet.getInt("user_id"),"","","","",""));
		app.setCreatedBy(resultSet.getString("created_by"));
		app.setExtSourceName(resultSet.getString("extsourcename"));
		app.setFedInfo(resultSet.getString("fed_info"));
		return app;
	};

	static final RowMapper<Application> APP_MAPPER = (resultSet, i) -> {

		Application app = new Application(resultSet.getInt("id"), new Vo(resultSet.getInt("vo_id"),
				resultSet.getString("vo_name"), resultSet.getString("vo_short_name"),
				resultSet.getString("vo_created_at"), resultSet.getString("vo_created_by"),
				resultSet.getString("vo_modified_at"), resultSet.getString("vo_modified_by"),
				resultSet.getInt("vo_created_by_uid"), resultSet.getInt("vo_modified_by_uid")),
				null, AppType.valueOf(resultSet.getString("apptype")),
				resultSet.getString("fed_info"), AppState.valueOf(resultSet.getString("state")),
				resultSet.getString("extsourcename"), resultSet.getString("extsourcetype"),
				resultSet.getInt("extsourceloa"), null);

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

		return app;

	};

	private static final RowMapper<AppType> APP_TYPE_MAPPER= (resultSet, i) -> AppType.valueOf(resultSet.getString(1));

	private static final RowMapper<ApplicationFormItem> ITEM_MAPPER = (resultSet, i) -> {
		ApplicationFormItem app = new ApplicationFormItem(resultSet.getInt("id"),
				resultSet.getString("shortname"), resultSet.getBoolean("required"),
				Type.valueOf(resultSet.getString("type")), resultSet.getString("fed_attr"),
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

	private static final RowMapper<ApplicationFormItem.ItemTexts> ITEM_TEXTS_MAPPER = (resultSet, i) -> new ItemTexts(new Locale(resultSet.getString("locale")),
			resultSet.getString("label"), resultSet.getString("options"), resultSet.getString("help"),
			resultSet.getString("error_message"));

}
