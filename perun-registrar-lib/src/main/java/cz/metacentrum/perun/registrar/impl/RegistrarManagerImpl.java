package cz.metacentrum.perun.registrar.impl;

import cz.metacentrum.perun.core.api.exceptions.*;

import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.HTML_COMMENT;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.PASSWORD;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.SUBMIT_BUTTON;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.USERNAME;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.VALIDATED_EMAIL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import cz.metacentrum.perun.registrar.exceptions.ApplicationNotCreatedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.exceptions.DuplicateRegistrationAttemptException;
import cz.metacentrum.perun.registrar.exceptions.FormNotExistsException;
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
 * @version $Id$
 */
public class RegistrarManagerImpl implements RegistrarManager {

    final static Logger log = LoggerFactory.getLogger(RegistrarManagerImpl.class);

    // identifiers for selected attributes
    private static final String URN_USER_TITLE_BEFORE = "urn:perun:user:attribute-def:core:titleBefore";
    private static final String URN_USER_TITLE_AFTER = "urn:perun:user:attribute-def:core:titleAfter";
    private static final String URN_USER_FIRST_NAME = "urn:perun:user:attribute-def:core:firstName";
    private static final String URN_USER_LAST_NAME = "urn:perun:user:attribute-def:core:lastName";
    private static final String URN_USER_MIDDLE_NAME = "urn:perun:user:attribute-def:core:middleName";
    private static final String URN_USER_DISPLAY_NAME = "urn:perun:user:attribute-def:core:displayName";

    private static final String DISPLAY_NAME_VO_FROM_EMAIL = "\"From\" email address";
    private static final String FRIENDLY_NAME_VO_FROM_EMAIL = "fromEmail";
    private static final String NAMESPACE_VO_FROM_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
    private static final String URN_VO_FROM_EMAIL = NAMESPACE_VO_FROM_EMAIL  + ":" + FRIENDLY_NAME_VO_FROM_EMAIL;

    private static final String DISPLAY_NAME_VO_TO_EMAIL = "\"To\" email addresses";
    private static final String FRIENDLY_NAME_VO_TO_EMAIL = "toEmail";
    private static final String NAMESPACE_VO_TO_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
    private static final String URN_VO_TO_EMAIL = NAMESPACE_VO_TO_EMAIL + ":" +  FRIENDLY_NAME_VO_TO_EMAIL;

    private static final String DISPLAY_NAME_GROUP_FROM_EMAIL = "\"From\" email address";
    private static final String FRIENDLY_NAME_GROUP_TO_EMAIL = "toEmail";
    private static final String NAMESPACE_GROUP_TO_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
    private static final String URN_GROUP_TO_EMAIL = NAMESPACE_GROUP_TO_EMAIL + ":" +  FRIENDLY_NAME_GROUP_TO_EMAIL;

    private static final String DISPLAY_NAME_GROUP_TO_EMAIL = "\"To\" email addresses";
    private static final String FRIENDLY_NAME_GROUP_FROM_EMAIL = "fromEmail";
    private static final String NAMESPACE_GROUP_FROM_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
    private static final String URN_GROUP_FROM_EMAIL = NAMESPACE_GROUP_FROM_EMAIL + ":" +  FRIENDLY_NAME_GROUP_FROM_EMAIL;

    private static final String DISPLAY_NAME_VO_LANGUAGE_EMAIL = "Notification default language";
    private static final String FRIENDLY_NAME_VO_LANGUAGE_EMAIL = "notificationsDefLang";
    private static final String NAMESPACE_VO_LANGUAGE_EMAIL = AttributesManager.NS_VO_ATTR_DEF;
    private static final String URN_VO_LANGUAGE_EMAIL = NAMESPACE_VO_LANGUAGE_EMAIL  + ":" + FRIENDLY_NAME_VO_LANGUAGE_EMAIL;

    private static final String DISPLAY_NAME_GROUP_LANGUAGE_EMAIL = "Notification default language";
    private static final String FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL = "notificationsDefLang";
    private static final String NAMESPACE_GROUP_LANGUAGE_EMAIL = AttributesManager.NS_GROUP_ATTR_DEF;
    private static final String URN_GROUP_LANGUAGE_EMAIL = NAMESPACE_GROUP_LANGUAGE_EMAIL + ":" +  FRIENDLY_NAME_GROUP_LANGUAGE_EMAIL;

    private static final String MODULE_PACKAGE_PATH = "cz.metacentrum.perun.registrar.modules.";

    @Autowired PerunBl perun;
    @Autowired MailManager mailManager;
    private RegistrarManager registrarManager;
    private PerunSession registrarSession;
    private SimpleJdbcTemplate jdbc;
    private boolean useMailManager = false;  // for production compatibility, default is false
    private AttributesManager attrManager;
    private MembersManager membersManager;
    private UsersManager usersManager;
    private VosManager vosManager;

    // federation attribute name constants
    private String shibDisplayNameVar = "displayName";
    private String shibCommonNameVar = "cn";
    private String shibFirstNameVar = "givenName";
    private String shibLastNameVar = "sn";
    private String shibLoAVar = "loa";

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new SimpleJdbcTemplate(dataSource);
    }

    public void setRegistrarManager(RegistrarManager registrarManager) {
        this.registrarManager = registrarManager;
    }

    public void setShibDisplayNameVar(String shibDisplayNameVar) {
        this.shibDisplayNameVar = shibDisplayNameVar;
    }

    public void setShibCommonNameVar(String shibCommonNameVar) {
        this.shibCommonNameVar = shibCommonNameVar;
    }

    public void setShibFirstNameVar(String shibFirstNameVar) {
        this.shibFirstNameVar = shibFirstNameVar;
    }

    public void setShibLastNameVar(String shibLastNameVar) {
        this.shibLastNameVar = shibLastNameVar;
    }

    public void setShibLoAVar(String shibLoAVar) {
        this.shibLoAVar = shibLoAVar;
    }

    protected void initialize() throws PerunException {

        // gets session for a system principal "perunRegistrar"
        final PerunPrincipal pp = new PerunPrincipal("perunRegistrar",
                ExtSourcesManager.EXTSOURCE_INTERNAL,
                ExtSourcesManager.EXTSOURCE_INTERNAL);
        registrarSession = perun.getPerunSession(pp);

        // set managers
        this.attrManager = perun.getAttributesManager();
        this.membersManager = perun.getMembersManager();
        this.usersManager = perun.getUsersManager();
        this.vosManager = perun.getVosManager();

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
            attrManager.createAttribute(registrarSession, attrDef);
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
            attrManager.createAttribute(registrarSession, attrDef);
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
            attrManager.createAttribute(registrarSession, attrDef);
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
            attrManager.createAttribute(registrarSession, attrDef);
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
            attrManager.createAttribute(registrarSession, attrDef);
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
            attrManager.createAttribute(registrarSession, attrDef);
        }

        // set mailing type
        useMailManager = Boolean.parseBoolean(mailManager.getPropertyFromConfiguration("useMailManager"));

    }

    @Override
    public List<Attribute> initialize(String voShortName, String groupName) throws PerunException {

        Vo vo = vosManager.getVoByShortName(registrarSession, voShortName);
        List<Attribute> list = attrManager.getAttributes(registrarSession, vo);
        // load group info if needed
        if (groupName != null && !groupName.isEmpty()) {
            Group group = perun.getGroupsManager().getGroupByName(registrarSession, vo, groupName);
            list.addAll(attrManager.getAttributes(registrarSession, group));
        }
        return list;

    }

    @Override
    public void createApplicationFormInVo(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException {

        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
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
    public void createApplicationFormInGroup(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException {

        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
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
    public ApplicationForm getFormForVo(final Vo vo) throws PerunException {

        if (vo == null) {
            throw new InternalErrorException("VO can't be null");
        }

        List<ApplicationForm> forms = new ArrayList<ApplicationForm>();
        forms = jdbc.query(FORM_SELECT+" where vo_id=? and group_id is null", new RowMapper<ApplicationForm>(){
            @Override
            public ApplicationForm mapRow(ResultSet rs, int arg1) throws SQLException {
                ApplicationForm form = new ApplicationForm();
                form.setId(rs.getInt("id"));
                form.setAutomaticApproval(rs.getBoolean("automatic_approval"));
                form.setAutomaticApprovalExtension(rs.getBoolean("automatic_approval_extension"));
                form.setModuleClassName(rs.getString("module_name"));
                form.setVo(vo);
                return form;
            }
        }, vo.getId());
        if (forms.size() > 1) {
            throw new InternalErrorException("VO: "+vo.getName()+" should have exactly: 1 application form, but has: "+forms.size());
        }
        if (forms.size() == 0) {
            throw new FormNotExistsException("Form for VO: "+vo.getName()+" doesn't exists.");
        }
        return forms.get(0);

    }

    @Override
    public ApplicationForm getFormForGroup(final Group group) throws PerunException {

        if (group == null) {
            throw new InternalErrorException("Group can't be null");
        }

        List<ApplicationForm> forms = new ArrayList<ApplicationForm>();
        forms = jdbc.query(FORM_SELECT+" where vo_id=? and group_id=?", new RowMapper<ApplicationForm>(){
            @Override
            public ApplicationForm mapRow(ResultSet rs, int arg1) throws SQLException {
                ApplicationForm form = new ApplicationForm();
                form.setId(rs.getInt("id"));
                form.setAutomaticApproval(rs.getBoolean("automatic_approval"));
                form.setAutomaticApprovalExtension(rs.getBoolean("automatic_approval_extension"));
                form.setModuleClassName(rs.getString("module_name"));
                form.setGroup(group);
                try {
                    form.setVo(vosManager.getVoById(registrarSession, group.getVoId()));
                } catch (Exception ex) {
                    // we don't care, shouldn't happen for internal identity.
                }
                return form;
            }
        }, group.getVoId(), group.getId());
        if (forms.size() > 1) {
            throw new InternalErrorException("GROUP: "+group.getName()+" should have exactly: 1 application form, but has: "+forms.size());
        }
        if (forms.size() == 0) {
            throw new FormNotExistsException("Form for GROUP: "+group.getName()+" doesn't exists.");
        }
        return forms.get(0);

    }

    @Transactional
    @Override
    public ApplicationFormItem addFormItem(PerunSession user, ApplicationForm form, ApplicationFormItem item) throws PrivilegeException, InternalErrorException {

        if (form.getGroup() == null) {
            // VO application
            if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo())) {
                throw new PrivilegeException(user, "addFormItem");
            }
        } else {
            if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo()) &&
                    !AuthzResolver.isAuthorized(user, Role.GROUPADMIN, form.getGroup()) ) {
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
                "insert into application_form_items(id,form_id,ordnum,shortname,required,type,fed_attr,dst_attr,regex) values (?,?,?,?,?,?,?,?,?)",
                itemId, form.getId(), ordnum, item.getShortname(), item.isRequired() ? "1" : "0",
                item.getType().name(), item.getFederationAttribute(),
                item.getPerunDestinationAttribute(), item.getRegex());

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
        return item;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFormItems(PerunSession sess, ApplicationForm form, List<ApplicationFormItem> items) throws PrivilegeException, InternalErrorException {

        if (form.getGroup() == null) {
            // VO application
            if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo())) {
                throw new PrivilegeException(sess, "updateFormItems");
            }
        } else {
            if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo()) &&
                    !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, form.getGroup()) ) {
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

            int result = jdbc.update("update application_form_items set ordnum=?,shortname=?,required=?,type=?,fed_attr=?,dst_attr=?,regex=? where id=?",
                    item.getOrdnum(), item.getShortname(), item.isRequired() ? "1" : "0", item
                    .getType().toString(), item.getFederationAttribute(), item
                    .getPerunDestinationAttribute(), item.getRegex(), item
                    .getId());
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

        // return number of updated rows
        return finalResult;

    }

    @Override
    public int updateForm(PerunSession user, ApplicationForm form) throws InternalErrorException, PrivilegeException {

        if (form.getGroup() == null) {
            // VO application
            if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo())) {
                throw new PrivilegeException(user, "updateForm");
            }
        } else {
            if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo()) &&
                    !AuthzResolver.isAuthorized(user, Role.GROUPADMIN, form.getGroup()) ) {
                throw new PrivilegeException(user, "updateForm");
            }
        }

        return jdbc.update(
                "update application_form set automatic_approval=?, automatic_approval_extension=?, module_name=? where id=?",
                form.isAutomaticApproval() ? "1" : "0", form.isAutomaticApprovalExtension() ? "1" : "0", form.getModuleClassName(), form.getId());
    }

    @Transactional
    @Override
    public void deleteFormItem(PerunSession user, ApplicationForm form, int ordnum) throws InternalErrorException, PrivilegeException {

        if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo())) {
            throw new PrivilegeException(user, "deleteFormItem");
        }
        jdbc.update("delete from application_form_items where form_id=? and ordnum=?", form.getId(), ordnum);
        jdbc.update("update application_form_items set ordnum=ordnum-1 where form_id=? and ordnum>?", form.getId(), ordnum);

    }

    @Transactional
    @Override
    public void moveFormItem(PerunSession user, ApplicationForm form, int ordnum, boolean up) throws InternalErrorException, PrivilegeException {

        if (!AuthzResolver.isAuthorized(user, Role.VOADMIN, form.getVo())) {
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
    public void updateFormItemTexts(PerunSession sess, ApplicationFormItem item, Locale locale) {

        ItemTexts texts = item.getTexts(locale);
        jdbc.update("update application_form_item_texts set label=?,options=?,help=?,error_message=? where item_id=? and locale=?",
                texts.getLabel(), texts.getOptions(), texts.getHelp(),
                texts.getErrorMessage(), item.getId(), locale.getLanguage());

    }

    @Override
    public List<ApplicationFormItemData> createApplication(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {

        // using this to init inner transaction
        // all minor exceptions inside are catched, if not, it's ok to throw them
        Application app = this.registrarManager.createApplicationInternal(session, application, data);

        // try to verify (or even auto-approve) application
        try {
            tryToVerifyApplication(session, app);
        } catch (Exception ex) {
            log.error("[REGISTRAR] Unable to verify or auto-approve application {}, because of exception {}", app, ex);
            throw ex;
        }

        return data;

    }


    @Override
    @Transactional(rollbackFor = ApplicationNotCreatedException.class)
    public Application createApplicationInternal(PerunSession session, Application application, List<ApplicationFormItemData> data) throws PerunException {

        // exceptions to send to vo admin with new app created email
        List<Exception> exceptions = new ArrayList<Exception>();
        boolean applicationNotCreated = false;

        try {

            // 1) create application
            int appId = Utils.getNewId(jdbc, "APPLICATION_ID_SEQ");
            application.setId(appId);

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
                    application.getExtSourceLoa(), AppState.NEW.toString(),
                    application.getCreatedBy(),application.getCreatedBy());

            // 2) process & store app data
            for (ApplicationFormItemData itemData : data) {

                Type itemType = itemData.getFormItem().getType();
                if (itemType == HTML_COMMENT || itemType == SUBMIT_BUTTON || itemType == PASSWORD) continue;

                // Check if mails needs to be validated
                if (itemType == VALIDATED_EMAIL) {
                    // default = mail not same as pre-filled
                    itemData.setAssuranceLevel("");
                    // If mail is contained in pre-filled value, then it can be validated automatically
                    // We must use contains, because IdP can send more than one email, emails are separated by semi-colon
                    if (itemData.getPrefilledValue() != null && itemData.getValue() != null && !itemData.getValue().isEmpty()) {
                        if (itemData.getPrefilledValue().toLowerCase().contains(itemData.getValue().toLowerCase())) {
                            itemData.setAssuranceLevel("1");
                        }
                    }
                    // If "mail" NOT REQUIRED and value is empty or null, set 1 to skip validation of application
                    // it's save, empty attributes are not set to DB nor any notification is sent
                    if (!itemData.getFormItem().isRequired() && (itemData.getValue() == null || itemData.getValue().isEmpty())) {
                        itemData.setAssuranceLevel("1");
                    }
                }

                itemData.setId(Utils.getNewId(jdbc, "APPLICATION_DATA_ID_SEQ"));
                jdbc.update("insert into application_data(id,app_id,item_id,shortname,value,assurance_level) values (?,?,?,?,?,?)",
                        itemData.getId(), appId, itemData.getFormItem().getId(), itemData
                        .getFormItem().getShortname(), itemData.getValue(), itemData
                        .getAssuranceLevel());

            }

            // 3) process all logins and passwords

            // create list of logins and passwords to process
            List<ApplicationFormItemData> logins = new ArrayList<ApplicationFormItemData>();
            for (ApplicationFormItemData itemData : data) {

                Type itemType = itemData.getFormItem().getType();
                if (itemType == USERNAME || itemType == PASSWORD) {
                    // skip unchanged pre-filled logins, since they must have been handled last time
                    if (itemData.getValue().equals(itemData.getPrefilledValue()) && itemType != PASSWORD) continue;
                    logins.add(itemData);
                }
            }

            for (ApplicationFormItemData loginItem : logins) {
                if (loginItem.getFormItem().getType() == USERNAME) {
                    // values to store
                    String login = loginItem.getValue();
                    String pass = ""; // filled later
                    // Get login namespace
                    String dstAttr = loginItem.getFormItem().getPerunDestinationAttribute();
                    AttributeDefinition loginAttribute = attrManager.getAttributeDefinition(registrarSession, dstAttr);
                    String loginNamespace = loginAttribute.getFriendlyNameParameter();

                    // try to book new login in namespace if the application hasn't been approved yet
                    if (perun.getUsersManagerBl().isLoginAvailable(registrarSession, loginNamespace, login)) {
                        try {
                            // Reserve login
                            jdbc.update("insert into application_reserved_logins(login,namespace,app_id,created_by,created_at) values(?,?,?,?,?)",
                                    login, loginNamespace, appId, application.getCreatedBy(), new Date());
                            log.debug("Added login reservation for login: {} in namespace: {}.", login, loginNamespace);

                            // process password for this login
                            for (ApplicationFormItemData passItem : logins) {
                                ApplicationFormItem item = passItem.getFormItem();
                                if (item.getType() == PASSWORD && item.getPerunDestinationAttribute() != null) {
                                    if (item.getPerunDestinationAttribute().equals(dstAttr)) {
                                        pass = passItem.getValue();
                                        try {
                                            // reserve password
                                            perun.getUsersManagerBl().reservePassword(registrarSession, login, loginNamespace, pass);
                                            log.debug("Password for login: {} in namespace: {} successfully reserved in external system.", login, loginNamespace);
                                        } catch (Exception ex) {
                                            // login reservation fail must cause rollback !!
                                            log.error("Unable to reserve password for login: {} in namespace: {} in external system. Exception: " + ex, login, loginNamespace);
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
                            log.error("Unable to reserve login: {} in namespace: {}. Exception: " + ex, login, loginNamespace);
                            exceptions.add(ex);
                        }
                    } else {
                        // login is not available
                        log.error("Login: " + login + " in namespace: " + loginNamespace + " is already occupied but it shouldn't (race condition).");
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
            log.error("{}", ex);
            exceptions.add(ex);
        } finally {

            // process rest only if it was not exception related to PASSWORDS creation
            if (!applicationNotCreated) {

                // use or don't use new mail manager
                if (useMailManager == true) {
                    getMailManager().sendMessage(application, MailType.APP_CREATED_USER, null, null);
                    getMailManager().sendMessage(application, MailType.APP_CREATED_VO_ADMIN, null, exceptions);
                }
                // if there were exceptions, throw some to let know GUI about it
                if (!exceptions.isEmpty()) {
                    RegistrarException ex = new RegistrarException("Your application (ID="+ application.getId()+
                            ") has been created with errors. Administrator of " + application.getVo().getName() + " has been notified. If you want, you can use \"Send report to RT\" button to send this information to Perun administrators.");
                    log.error("New application {} created with errors {}. This is case of PerunException {}", new Object[] {application, exceptions, ex.getErrorId()});
                    throw ex;
                }
                log.info("New application {} created.", application);
                perun.getAuditer().log(session, "New {} created.", application);

            }
        }

        // return stored data
        return application;

    }

    @Override
    public void deleteApplication(PerunSession sess, Application app) throws PerunException {

        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
            if (app.getGroup() != null) {
                if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
                    throw new PrivilegeException(sess, "deleteApplication");
                }
            } else {
                throw new PrivilegeException(sess, "deleteApplication");
            }
        }

        if (AppState.NEW.equals(app.getState()) || AppState.REJECTED.equals(app.getState())) {

            // Try to get reservedLogin and reservedNamespace before deletion
            List<Pair<String, String>> logins;
            try {
                logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?", new RowMapper<Pair<String, String>>() {
                    @Override
                    public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
                        return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
                    }
                }, app.getId());
            } catch (EmptyResultDataAccessException e) {
                // set empty logins
                logins = new ArrayList<Pair<String,String>>();
            }
            // delete passwords in KDC
            for (Pair<String,String> login : logins) {
                // delete LOGIN in NAMESPACE
                perun.getUsersManagerBl().deletePassword(sess, login.getRight(), login.getLeft());
            }

            // delete application and data on cascade
            jdbc.update("delete from application where id=?", app.getId());

        } else {
            throw new RegistrarException("Only applications in NEW or REJECTED state can be deleted.");
        }

    }

    @Override
    public Application verifyApplication(PerunSession sess, int appId) throws PrivilegeException, InternalErrorException {

        Application app = getApplicationById(appId);

        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
            if (app.getGroup() != null) {
                if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
                    throw new PrivilegeException(sess, "verifyApplication");
                }
            } else {
                throw new PrivilegeException(sess, "verifyApplication");
            }
        }
        // proceed
        markApplicationVerified(appId);
        // return updated application
        return getApplicationById(appId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Application rejectApplication(PerunSession sess, int appId, String reason) throws PerunException {

        Application app = getApplicationById(appId);
        // authz
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
            if (app.getGroup() != null) {
                if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
                    throw new PrivilegeException(sess, "rejectApplication");
                }
            } else {
                throw new PrivilegeException(sess, "rejectApplication");
            }
        }

        // only VERIFIED applications can be rejected
        if (AppState.APPROVED.equals(app.getState())) {
            throw new RegistrarException("Once approved applications can't be rejected !");
        } else if (AppState.REJECTED.equals(app.getState())) {
            throw new RegistrarException("Once rejected applications can't be rejected !");
        }

        // mark as rejected
        int result = jdbc.update("update application set state=?, modified_by=?, modified_at=? where id=?", AppState.REJECTED.toString(), sess.getPerunPrincipal().getActor(), new Date(), appId);
        if (result == 0) {
            throw new RegistrarException("Application with ID="+appId+" not found.");
        } else if (result > 1) {
            throw new ConsistencyErrorException("More than one application is stored under ID="+appId+".");
        }
        // set back as rejected
        app.setState(AppState.REJECTED);
        log.info("Application {} marked as REJECTED.", appId);

        // get all reserved logins
        List<Pair<String, String>> logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?",
                new RowMapper<Pair<String, String>>() {
                    @Override
                    public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
                        return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
                    }
                }, appId);

        // delete passwords for reserved logins
        for (Pair<String, String> login : logins) {
            try {
                // left = namespace / right = login
                usersManager.deletePassword(registrarSession, login.getRight(), login.getLeft());
            } catch (LoginNotExistsException ex) {
                log.error("Login: {} not exists while deleting passwords in rejected application: {}", login.getLeft(), appId);
            }
        }
        // free any login from reservation when application is rejected
        jdbc.update("delete from application_reserved_logins where app_id=?", appId);

        // log
        perun.getAuditer().log(sess, "{} rejected.", app);

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
        if (useMailManager==true) {
            getMailManager().sendMessage(app, MailType.APP_REJECTED_USER, reason, null);
        } else {
            log.error("[REGISTRAR] Unable to send APP_REJECTED_USER mail, because new way of sending is disabled");
        }

        // return updated application
        return app;

    }

    @Override
    public Application approveApplication(PerunSession sess, int appId) throws PerunException {

        Application app;
        try {
            app = registrarManager.approveApplicationInternal(sess, appId);
        } catch (AlreadyMemberException ex) {
            // case when user joined identity after sending initial application and former user was already member of VO
            throw new RegistrarException("User is already member of your VO with ID:"+ex.getMember().getId()+" (user joined his identities after sending new application). You can reject this application and re-validate old member to keep old data (e.g. login,email).", ex);
        } catch (MemberNotExistsException ex) {
            throw new RegistrarException("To approve application user must already be member of VO.", ex);
        }

        Member member = perun.getMembersManager().getMemberByUser(registrarSession, app.getVo(), app.getUser());

        try {
            // validate member async when all changes are commited
            perun.getMembersManagerBl().validateMemberAsync(registrarSession, member);
        } catch (Exception ex) {
            // we skip any exception thrown from here
            log.error("Exception when validating {} after approving application {}.", member, app);
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
    public Application approveApplicationInternal(PerunSession sess, int appId) throws PerunException {

        Application app = getApplicationById(appId);
        Member member = null;

        // authz
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
            if (app.getGroup() != null) {
                if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
                    throw new PrivilegeException(sess, "approveApplication");
                }
            } else {
                throw new PrivilegeException(sess, "approveApplication");
            }
        }

        // only VERIFIED applications can be approved
        if (!AppState.VERIFIED.equals(app.getState())) {
            throw new RegistrarException("Only applications in state VERIFIED can be approved. Please verify application manually before approval.");
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
            logins = jdbc.query("select namespace,login from application_reserved_logins where app_id=?", new RowMapper<Pair<String, String>>() {
                @Override
                public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
                    return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
                }
            }, appId);
        } catch (EmptyResultDataAccessException e) {
            // set empty logins
            logins = new ArrayList<Pair<String,String>>();
        }

        // FOR INITIAL APPLICATION
        if (AppType.INITIAL.equals(app.getType())) {

            if (app.getGroup() != null) {

                // free reserved logins so they can be set as attributes
                jdbc.update("delete from application_reserved_logins where app_id=?", appId);

                // add new member of VO as member of group (for group applications)
                // !! MUST BE MEMBER OF VO !!
                member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

                // store all attributes (but not logins)
                storeApplicationAttributes(app);

                // unreserve new duplicite logins and get purely new logins back
                logins = unreserveNewLoginsFromSameNamespace(logins, app.getUser());

                // store purely new logins to user
                storeApplicationLoginAttributes(app);

                for (Pair<String, String> pair : logins) {
                    // LOGIN IN NAMESPACE IS PURELY NEW => VALIDATE ENTRY IN KDC
                    // left = namespace, right = login
                    perun.getUsersManagerBl().validatePasswordAndSetExtSources(registrarSession, app.getUser(), pair.getRight(), pair.getLeft());
                }

                perun.getGroupsManager().addMember(registrarSession, app.getGroup(), member);

                log.debug("Member {} added to Group {}.",member, app.getGroup());

            } else {

                // put application data into Candidate
                final Map<String, String> attributes = new HashMap<String, String>();
                jdbc.query("select dst_attr,value from application_data d, application_form_items i where d.item_id=i.id "
                        + "and i.dst_attr is not null and d.value is not null and app_id=?",
                        new RowMapper<Object>() {
                            @Override
                            public Object mapRow(ResultSet rs, int i) throws SQLException {
                                attributes.put(rs.getString("dst_attr"), rs.getString("value"));
                                return null;
                            }
                        }, appId);

                // DO NOT STORE LOGINS THROUGH CANDIDATE
                // we do not set logins by candidate object to prevent accidental overwrite while joining identities in process
                Iterator<Map.Entry<String,String>> iter = attributes.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String,String> entry = iter.next();
                    if(entry.getKey().contains("urn:perun:user:attribute-def:def:login-namespace:")){
                        iter.remove();
                    }
                }

                Candidate candidate = new Candidate();
                candidate.setAttributes(attributes);

                log.debug("Retrieved candidate from DB {}", candidate);

                // first try to parse display_name if not null and not empty
                if (attributes.containsKey(URN_USER_DISPLAY_NAME) && attributes.get(URN_USER_DISPLAY_NAME) != null &&
                        !attributes.get(URN_USER_DISPLAY_NAME).isEmpty()) {
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

                // free reserved logins so they can be set as attributes
                jdbc.update("delete from application_reserved_logins where app_id=?", appId);

                // create member and user
                log.debug("Trying to make member from candidate {}", candidate);

                member = membersManager.createMember(sess, app.getVo(), app.getExtSourceName(), app.getExtSourceType(), app.getExtSourceLoa(), app.getCreatedBy(), candidate);
                // set user id back to application
                User u = usersManager.getUserById(registrarSession, member.getUserId());
                app.setUser(u);
                result = jdbc.update("update application set user_id=? where id=?", member.getUserId(), appId);
                if (result == 0) {
                    throw new RegistrarException("User ID hasn't been associated with the application " + appId + ", because the application was not found!");
                } else if (result > 1) {
                    throw new ConsistencyErrorException("User ID hasn't been associated with the application " + appId + ", because more than one application exists under the same ID.");
                }
                log.info("Member " + member.getId() + " created for: " + app.getCreatedBy() + " / " + app.getExtSourceName());

                // unreserve new login if user already have login in same namespace
                // also get back purely new logins
                logins = unreserveNewLoginsFromSameNamespace(logins, u);

                // store purely new logins to user
                storeApplicationLoginAttributes(app);

                for (Pair<String, String> pair : logins) {
                    // LOGIN IN NAMESPACE IS PURELY NEW => VALIDATE ENTRY IN KDC
                    // left = namespace, right = login
                    perun.getUsersManagerBl().validatePasswordAndSetExtSources(registrarSession, u, pair.getRight(), pair.getLeft());
                }

                // log
                perun.getAuditer().log(sess, "{} created for approved {}.", member, app);

            }

            // FOR EXTENSION APPLICATION
        } else if (AppType.EXTENSION.equals(app.getType())) {

            // free reserved logins so they can be set as attributes
            jdbc.update("delete from application_reserved_logins where app_id=?", app.getId());

            member = membersManager.getMemberByUser(registrarSession, app.getVo(), app.getUser());

            storeApplicationAttributes(app);

            // extend user's membership
            membersManager.extendMembership(registrarSession, member);

            // unreserve new logins, if user already have login in same namespace
            // also get back logins, which are purely new
            logins = unreserveNewLoginsFromSameNamespace(logins, app.getUser());

            // store purely new logins from application
            storeApplicationLoginAttributes(app);

            // validate purely new logins in KDC
            for (Pair<String, String> pair : logins) {
                // left = namespace, right = login
                perun.getUsersManagerBl().validatePasswordAndSetExtSources(registrarSession, app.getUser(), pair.getRight(), pair.getLeft());
            }

            // log
            perun.getAuditer().log(sess, "Membership extended for {} in {} for approved {}.", member, app.getVo(), app);

        }

        // CONTINUE FOR BOTH APP TYPES

        // call registrar module
        RegistrarModule module;
        if (app.getGroup() != null) {
            module = getRegistrarModule(getFormForGroup(app.getGroup()));
        } else {
            module = getRegistrarModule(getFormForVo(app.getVo()));
        }
        if (module != null) {
            module.approveApplication(sess, app);
        }

        // switch between new and old mail manager
        if (useMailManager==true) {
            getMailManager().sendMessage(app, MailType.APP_APPROVED_USER, null, null);
        } else {
            log.error("[REGISTRAR] Unable to send APP_APPROVED_USER mail, because new way of sending is disabled");
        }

        // return updated application
        return app;

    }

    @Override
    public Application getApplicationById(PerunSession sess, int appId) throws PerunException {


        // get application
        Application app = getApplicationById(appId);

        // authz ex post
        if (app.getGroup() == null) {
            if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
                throw new PrivilegeException(sess, "getApplicationById");
            }
        } else {
            if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo()) &&
                    !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
                throw new PrivilegeException(sess, "getApplicationById");
            }
        }

        return app;

    }

    @Override
    public List<Application> getApplicationsForVo(PerunSession userSession, Vo vo, List<String> state) throws PerunException {

        // authz
        if (!AuthzResolver.isAuthorized(userSession, Role.VOADMIN, vo)) {
            throw new PrivilegeException(userSession, "getApplicationsForVo");
        }
        if (state == null) {
            // list all
            try {
                return jdbc.query(APP_SELECT + " where a.vo_id=? order by a.created_at", APP_MAPPER, vo.getId());
            } catch (EmptyResultDataAccessException ex) {
                return new ArrayList<Application>();
            }
        } else {
            // filter by state
            try {
                String stateString = "";
                for (String s : state) {
                    stateString += "'"+s+"',";
                }
                if (stateString.length() > 1) {
                    stateString = stateString.substring(0, stateString.length()-1);
                }
                return jdbc.query(APP_SELECT + " where a.vo_id=? and state in ("+stateString+") order by a.created_at", APP_MAPPER, vo.getId());
            } catch (EmptyResultDataAccessException ex) {
                return new ArrayList<Application>();
            }
        }

    }

    @Override
    public List<Application> getApplicationsForGroup(PerunSession userSession, Group group, List<String> state) throws PerunException {

        // authz
        if (!AuthzResolver.isAuthorized(userSession, Role.VOADMIN, group) &&
                !AuthzResolver.isAuthorized(userSession, Role.GROUPADMIN, group)) {
            throw new PrivilegeException(userSession, "getApplicationsForGroup");
        }
        if (state == null) {
            // list all
            try {
                return jdbc.query(APP_SELECT + " where a.group_id=? order by a.created_at", APP_MAPPER, group.getId());
            } catch (EmptyResultDataAccessException ex) {
                return new ArrayList<Application>();
            }
        } else {
            // filter by state
            try {
                String stateString = "";
                for (String s : state) {
                    stateString += "'"+s+"',";
                }
                if (stateString.length() > 1) {
                    stateString = stateString.substring(0, stateString.length()-1);
                }
                return jdbc.query(APP_SELECT + " where a.group_id=? and state in ("+stateString+") order by a.created_at", APP_MAPPER, group.getId());
            } catch (EmptyResultDataAccessException ex) {
                return new ArrayList<Application>();
            }
        }

    }

    @Override
    public List<Application> getApplicationsForUser(User user) {

        try {
            return jdbc.query(APP_SELECT + " where user_id=? order by a.created_at", APP_MAPPER, user.getId());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<Application>();
        }

    }

    @Override
    public List<Application> getApplicationsForUser(PerunSession sess) {

        try {
            PerunPrincipal pp = sess.getPerunPrincipal();
            return jdbc.query(APP_SELECT + " where a.created_by=? and extsourcename=? order by a.created_at", APP_MAPPER, pp.getActor(), pp.getExtSourceName());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<Application>();
        }

    }

    @Override
    public List<Application> getApplicationsForMember(PerunSession sess, Group group, Member member) throws PerunException {

        // authz
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
            if (group != null) {
                if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
                    throw new PrivilegeException(sess, "getApplicationsForMember");
                }
            } else {
                throw new PrivilegeException(sess, "getApplicationsForMember");
            }
        }

        try {
            if (group == null) {
                return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? order by a.created_at", APP_MAPPER, member.getUserId(), member.getVoId());
            } else {
                return jdbc.query(APP_SELECT + " where user_id=? and a.vo_id=? and a.group_id=? order by a.created_at", APP_MAPPER, member.getUserId(), member.getVoId(), group.getId());
            }
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<Application>();
        }

    };

    @Override
    public List<ApplicationFormItem> getFormItems(ApplicationForm form, AppType appType) {

        List<ApplicationFormItem> items;
        if (appType == null) {
            items = jdbc.query("select id,ordnum,shortname,required,type,fed_attr,dst_attr,regex from application_form_items where form_id=? order by ordnum asc",
                    ITEM_MAPPER, form.getId());
        } else {
            items = jdbc.query("select id,ordnum,shortname,required,type,fed_attr,dst_attr,regex from application_form_items i,application_form_item_apptypes t "
                    + " where form_id=? and i.id=t.item_id and t.apptype=? order by ordnum asc",
                    ITEM_MAPPER, form.getId(), appType.toString());
        }
        for (ApplicationFormItem item : items) {
            List<ItemTexts> texts = jdbc
                    .query("select locale,label,options,help,error_message from application_form_item_texts where item_id=?",
                            ITEM_TEXTS_MAPPER, item.getId());
            for (ItemTexts itemTexts : texts) {
                item.getI18n().put(itemTexts.getLocale(), itemTexts);
            }
            List<AppType> appTypes = jdbc.query("select apptype from application_form_item_apptypes where item_id=?",
                    new RowMapper<AppType>() {
                        @Override
                        public AppType mapRow(ResultSet rs, int i) throws SQLException {
                            return AppType.valueOf(rs.getString(1));
                        }
                    }, item.getId());
            item.setApplicationTypes(appTypes);
        }

        return items;
    }

    @Override
    public ApplicationFormItem getFormItemById(int id) {
        ApplicationFormItem item;
        item = jdbc.queryForObject("select id,ordnum,shortname,required,type,fed_attr,dst_attr,regex from application_form_items where id=?",
                new RowMapper<ApplicationFormItem>() {
                    public ApplicationFormItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ApplicationFormItem app = new ApplicationFormItem(rs
                                .getInt("id"), rs.getString("shortname"), rs
                                .getBoolean("required"),
                                Type.valueOf(rs.getString("type")), rs
                                .getString("fed_attr"), rs.getString("dst_attr"), rs
                                .getString("regex"));
                        app.setOrdnum(rs.getInt("ordnum"));
                        return app;
                    }
                }, id);
        if (item != null) {
            List<ItemTexts> texts = jdbc.query("select locale,label,options,help,error_message from application_form_item_texts where item_id=?",
                    new RowMapper<ApplicationFormItem.ItemTexts>() {
                        @Override
                        public ItemTexts mapRow(ResultSet rs, int i) throws SQLException {
                            return new ItemTexts(new Locale(rs.getString("locale")), rs
                                    .getString("label"), rs.getString("options"), rs
                                    .getString("help"), rs.getString("error_message"));
                        }
                    }, item.getId());
            for (ItemTexts itemTexts : texts) {
                item.getI18n().put(itemTexts.getLocale(), itemTexts);
            }
            List<AppType> appTypes = jdbc.query("select apptype from application_form_item_apptypes where item_id=?",
                    new RowMapper<AppType>() {
                        @Override
                        public AppType mapRow(ResultSet rs, int i) throws SQLException {
                            return AppType.valueOf(rs.getString(1));
                        }
                    }, item.getId());
            item.setApplicationTypes(appTypes);
        }

        return item;

    }

    @Override
    public List<ApplicationFormItemWithPrefilledValue> getFormItemsWithPrefilledValues(PerunSession sess, AppType appType, ApplicationForm form) throws PerunException {

        // refresh session
        AuthzResolver.refreshAuthz(sess);

        Vo vo = form.getVo();
        Group group = form.getGroup();

        // get necessary params from session
        User user = sess.getPerunPrincipal().getUser();
        String actor = sess.getPerunPrincipal().getActor();
        String extSourceName = sess.getPerunPrincipal().getExtSourceName();
        int extSourceLoa = sess.getPerunPrincipal().getExtSourceLoa();
        Map<String, String> federValues = sess.getPerunPrincipal().getAdditionalInformations();

        // Check if it's not DuplicateRegistrationAttempt (for initial)
        if (AppType.INITIAL.equals(appType)) {

            List<Integer> regs = new ArrayList<Integer>();
            if (user != null) {
                // user is known
                try {
                    Member m = membersManager.getMemberByUser(registrarSession, vo, user);
                    if (group != null) {
                        // get members groups
                        List<Group> g = perun.getGroupsManager().getMemberGroups(registrarSession, m);
                        if (g.contains(group)) {
                            // user is member of group - can't post more initial applications
                            throw new RegistrarException("You are already member of group: "+group.getName());
                        } else {
                            // user isn't member of group
                            regs.clear();
                            regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id=? and user_id=? and state=?",
                                    new SingleColumnRowMapper<Integer>(Integer.class),
                                    AppType.INITIAL.toString(), vo.getId(), group.getId(), user.getId(), AppState.NEW.toString()));
                            regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id=? and user_id=? and state=?",
                                    new SingleColumnRowMapper<Integer>(Integer.class),
                                    AppType.INITIAL.toString(), vo.getId(), group.getId(), user.getId(), AppState.VERIFIED.toString()));
                            if (!regs.isEmpty()) {
                                // user have unprocessed application for group
                                throw new DuplicateRegistrationAttemptException("Initial application for Group: "+group.getName()+" already exists", actor, extSourceName, regs.get(0));
                            }
                            // pass if have approved or rejected app
                        }
                    } else {
                        // user is member of vo, can't post more initial applications
                        throw new RegistrarException("You are already member of VO: "+vo.getName());
                    }
                } catch (MemberNotExistsException ex) {
                    // user is not member of vo
                    if (group != null) {
                        // not member of VO - check for unprocessed applications to Group
                        regs.clear();
                        regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id=? and user_id=? and state=?",
                                new SingleColumnRowMapper<Integer>(Integer.class),
                                AppType.INITIAL.toString(), vo.getId(), group.getId(), user.getId(), AppState.NEW.toString()));
                        regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id=? and user_id=? and state=?",
                                new SingleColumnRowMapper<Integer>(Integer.class),
                                AppType.INITIAL.toString(), vo.getId(), group.getId(), user.getId(), AppState.VERIFIED.toString()));
                        if (!regs.isEmpty()) {
                            // user have unprocessed application for group - can't post more
                            throw new DuplicateRegistrationAttemptException("Initial application for Group: "+group.getName()+" already exists", actor, extSourceName, regs.get(0));
                        }
                        //throw new InternalErrorException("You must be member of vo: "+vo.getName()+" to apply for membership in group: "+group.getName());
                    } else {
                        // not member of VO - check for unprocessed applications
                        regs.clear();
                        regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id is null and user_id=? and state=?",
                                new SingleColumnRowMapper<Integer>(Integer.class),
                                AppType.INITIAL.toString(), vo.getId(), user.getId(), AppState.NEW.toString()));
                        regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id is null and user_id=? and state=?",
                                new SingleColumnRowMapper<Integer>(Integer.class),
                                AppType.INITIAL.toString(), vo.getId(), user.getId(), AppState.VERIFIED.toString()));
                        if (!regs.isEmpty()) {
                            // user have unprocessed application for VO - can't post more
                            throw new DuplicateRegistrationAttemptException("Initial application for VO: "+vo.getName()+" already exists", actor, extSourceName, regs.get(0));
                        }
                        // pass not member and have only approved or rejected apps
                    }
                }
            } else {

                // user is not known
                if (group != null) {
                    // group application
                    // get registrations by user logged identity
                    regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id=? and created_by=? and extSourceName=? and state<>?",
                            new SingleColumnRowMapper<Integer>(Integer.class),
                            AppType.INITIAL.toString(), vo.getId(), group.getId(), actor, extSourceName, AppState.REJECTED.toString()));

                    if (!regs.isEmpty()) {
                        throw new DuplicateRegistrationAttemptException("Initial application for Group: "+group.getName()+" already exists", actor, extSourceName, regs.get(0));
                    }
                } else {
                    // vo application
                    // get registrations by user logged identity
                    regs.addAll(jdbc.query("select id from application where apptype=? and vo_id=? and group_id is null and created_by=? and extSourceName=? and state<>?",
                            new SingleColumnRowMapper<Integer>(Integer.class),
                            AppType.INITIAL.toString(), vo.getId(), actor, extSourceName, AppState.REJECTED.toString()));

                    if (!regs.isEmpty()) {
                        throw new DuplicateRegistrationAttemptException("Initial application for VO: "+vo.getName()+" already exists", actor, extSourceName, regs.get(0));
                    }
                }

            }

            // if false, throws exception with reason for GUI
            membersManager.canBeMemberWithReason(sess, vo, user, String.valueOf(extSourceLoa));

        }
        // if extension, user != null !!
        if (AppType.EXTENSION.equals(appType)) {

            if (user == null) {
                throw new RegistrarException("Trying to get extension application for non-existing user. Try to log-in with different identity known to Perun.");
            }
            if (form.getGroup() != null) {
                throw new RegistrarException("Membership in group can't be extended by application. It last as long as VO membership.");
            }

            Member member = membersManager.getMemberByUser(sess, vo, user);
            // if false, throws exception with reason for GUI
            membersManager.canExtendMembershipWithReason(sess, member);

        }

        // PROCEED
        Map<String, String> parsedName = extractNames(federValues);
        List<ApplicationFormItem> formItems = getFormItems(form, appType);

        List<ApplicationFormItemWithPrefilledValue> itemsWithValues = new ArrayList<ApplicationFormItemWithPrefilledValue>();
        for (ApplicationFormItem item : formItems) {
            itemsWithValues.add(new ApplicationFormItemWithPrefilledValue(item, null));
        }

        // get user and member attributes from DB for existing users
        if (user != null) {

            Map<String, Attribute> map = new HashMap<String, Attribute>();

            // process user attributes
            List<Attribute> userAttributes = attrManager.getAttributes(registrarSession, user);
            for (Attribute att : userAttributes) {
                map.put(att.getName(), att);
            }
            // process member attributes
            try {
                Member member = membersManager.getMemberByUser(registrarSession, vo, user);
                List<Attribute> memberAttributes = attrManager.getAttributes(registrarSession, member);
                for (Attribute att : memberAttributes) {
                    map.put(att.getName(), att);
                }
            } catch (MemberNotExistsException ex) {
                // we don't care that user is not yet member
            }

            Iterator<ApplicationFormItemWithPrefilledValue> it = ((Collection<ApplicationFormItemWithPrefilledValue>) itemsWithValues).iterator();
            while (it.hasNext()) {
                ApplicationFormItemWithPrefilledValue itemW = it.next();
                String dstAtt = itemW.getFormItem().getPerunDestinationAttribute();
                // skip items without perun attr reference
                if (dstAtt == null || dstAtt.equals(""))
                    continue;
                // if attr exist and value != null
                if (map.get(dstAtt) != null && map.get(dstAtt).getValue() != null) {
                    if (itemW.getFormItem().getType() == PASSWORD) {
                        // if login in namespace exists, do not return password field
                        // because application form is not place to change login or password
                        it.remove();
                    } else {
                        // else set value
                        itemW.setPrefilledValue(BeansUtils.attributeValueToString(map.get(dstAtt)));
                    }
                }
            }
        }
        // get user attributes from federation
        Iterator<ApplicationFormItemWithPrefilledValue> it = ((Collection<ApplicationFormItemWithPrefilledValue>) itemsWithValues).iterator();
        while (it.hasNext()) {
            ApplicationFormItemWithPrefilledValue itemW = it.next();
            String fa = itemW.getFormItem().getFederationAttribute();
            if (fa != null && !fa.isEmpty()) {
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
                    String loa = federValues.get(shibLoAVar);
                    itemW.setAssuranceLevel(loa);
                }
            }
        }

        // set names from federation attributes if not empty
        for (ApplicationFormItemWithPrefilledValue itemW : itemsWithValues) {
            ApplicationFormItem item = itemW.getFormItem();
            String dstAtt = item.getPerunDestinationAttribute();
            if (URN_USER_TITLE_BEFORE.equals(dstAtt)) {
                String titleBefore = parsedName.get("titleBefore");
                if (titleBefore != null && !titleBefore.trim().isEmpty())
                    itemW.setPrefilledValue(titleBefore);
            } else if (URN_USER_TITLE_AFTER.equals(dstAtt)) {
                String titleAfter = parsedName.get("titleAfter");
                if (titleAfter != null && !titleAfter.trim().isEmpty())
                    itemW.setPrefilledValue(titleAfter);
            } else if (URN_USER_FIRST_NAME.equals(dstAtt)) {
                String firstName = parsedName.get("firstName");
                if (firstName != null && !firstName.trim().isEmpty())
                    itemW.setPrefilledValue(firstName);
            } else if (URN_USER_LAST_NAME.equals(dstAtt)) {
                String lastName = parsedName.get("lastName");
                if (lastName != null && !lastName.trim().isEmpty())
                    itemW.setPrefilledValue(lastName);
            }
        }

        // return prefilled form
        return itemsWithValues;

    }

    @Override
    public boolean validateEmailFromLink(Map<String, String> urlParameters) throws PerunException {

        String idStr = urlParameters.get("i");
        if (mailManager.getMessageAuthenticationCode(idStr).equals(urlParameters.get("m"))) {
            int appDataId = Integer.parseInt(idStr, Character.MAX_RADIX);
            jdbc.update("update application_data set assurance_level=1 where id = ?", appDataId);
            Application app = getApplicationById(jdbc.queryForInt("select app_id from application_data where id = ?", appDataId));
            tryToVerifyApplication(registrarSession, app);
            return true;
        }
        return false;

    }

    @Override
    public List<ApplicationFormItem> getFormItems(ApplicationForm form) {
        return getFormItems(form, null);
    }

    @Override
    public List<ApplicationFormItemData> getApplicationDataById(PerunSession sess, int appId) {
        // TODO authorization based on session (vo admin / user)
        return jdbc.query("select id,item_id,shortname,value,assurance_level from application_data where app_id=?",
                new RowMapper<ApplicationFormItemData>() {
                    @Override
                    public ApplicationFormItemData mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ApplicationFormItemData data = new ApplicationFormItemData();
                        data.setId(rs.getInt("id"));
                        data.setFormItem(getFormItemById(rs.getInt("item_id")));
                        data.setShortname(rs.getString("shortname"));
                        data.setValue(rs.getString("value"));
                        data.setAssuranceLevel(rs.getString("assurance_level"));
                        return data;
                    }
                }, appId);

    }

    @Override
    public void copyFormFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException {

        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, fromVo) ||
                !AuthzResolver.isAuthorized(sess, Role.VOADMIN, toVo)) {
            throw new PrivilegeException(sess, "copyFormFromVoToVo");
        }

        List<ApplicationFormItem> items = getFormItems(getFormForVo(fromVo));
        for (ApplicationFormItem item : items) {
            item.setOrdnum(null); // reset order, id is always new inside add method
            addFormItem(sess, getFormForVo(toVo), item);
        }

    }

    @Override
    public void copyFormFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException {

        if ((!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, fromGroup) && !AuthzResolver.isAuthorized(sess, Role.VOADMIN, fromGroup)) ||
                (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, toGroup) && !AuthzResolver.isAuthorized(sess, Role.VOADMIN, toGroup))) {
            throw new PrivilegeException(sess, "copyFormFromGroupToGroup");
        }

        List<ApplicationFormItem> items = getFormItems(getFormForGroup(fromGroup));
        for (ApplicationFormItem item : items) {
            item.setOrdnum(null); // reset order, id is always new inside add method
            addFormItem(sess, getFormForGroup(toGroup), item);
        }

    }

    @Override
    public MailManager getMailManager() {
        return this.mailManager;
    }

    @Override
    public List<User> checkForSimilarUsers(PerunSession sess, int appId) throws PerunException {

        // TODO authz

        String name = "";
        List<User> result = new ArrayList<User>();

        Application app = getApplicationById(appId);
        // only for initial VO applications if user==null
        if (app.getType().equals(AppType.INITIAL) && app.getGroup() == null && app.getUser() == null) {

            try {

                UserExtSource ues = new UserExtSource();
                ExtSource es = new ExtSource();
                es.setName(app.getExtSourceName());
                es.setType(app.getExtSourceType());
                ues.setLogin(app.getCreatedBy());
                ues.setExtSource(es);

                User u = usersManager.getUserByUserExtSource(registrarSession, ues);
                if (u != null) {
                    // user connected his identity after app creation and before it's approval.
                    // do not show error message in GUI by returning an empty array.
                    return result;
                }
            } catch (Exception ex){
                // we don't care, let's try to search by name
            }

            List<ApplicationFormItemData> data = getApplicationDataById(sess, appId);
            for (ApplicationFormItemData item : data) {
                if ("urn:perun:user:attribute-def:core:displayName".equals(item.getFormItem().getPerunDestinationAttribute())) {
                    name = item.getValue();
                    if (name != null && !name.isEmpty()) break;
                } else if ("urn:perun:user:attribute-def:core:lastName".equals(item.getFormItem().getPerunDestinationAttribute())) {
                    name = item.getValue();
                    if (name != null && !name.isEmpty()) break;
                }
            }

            if (name != null && !name.isEmpty()) {
                return usersManager.findUsersByName(registrarSession, name);
            } else {
                return result;
            }

        } else {

            return result;

        }

    }

    /**
     * Set application to VERIFIED state if all it's
     * mails (VALIDATED_EMAIL) have assuranceLevel = "1".
     *
     * @param sess user who try to verify application
     * @param app application to verify
     * @return TRUE if verified / FALSE if not verified
     * @throws InternalErrorException
     */
    private boolean tryToVerifyApplication(PerunSession sess, Application app) throws PerunException {

        // test all fields that may need to be validated on a required level
        List<String> loas = jdbc.query("select d.assurance_level from application a, application_form_items i, application_data d " +
                "where d.app_id=a.id and d.item_id=i.id and a.id=? and i.type=?",
                new SingleColumnRowMapper<String>(String.class), app.getId(), Type.VALIDATED_EMAIL.toString());

        boolean allValidated = true;
        for (String loa : loas) {
            if (!"1".equals(loa)) allValidated = false;
        }

        if (allValidated) {
            // mark VERIFIED
            markApplicationVerified(app.getId());
            // try to APPROVE if auto approve
            tryToAutoApproveApplication(sess, app);
        } else {
            // send request validation notification
            if (useMailManager == true) {
                getMailManager().sendMessage(app, MailType.MAIL_VALIDATION, null, null);
            } else {
                log.error("[REGISTRAR] Unable to send MAIL_VALIDATION mail, because new way of sending is disabled.");
            }
        }

        return allValidated;

    }

    /**
     * Forcefully marks application as VERIFIED
     * (only if was in NEW state before)
     *
     * @param appId ID of application to verify.
     */
    private void markApplicationVerified(int appId) {

        if (jdbc.update("update application set state=? where id=? and state=?", AppState.VERIFIED.toString(), appId, AppState.NEW.toString()) > 0) {
            log.info("Application {} marked as VERIFIED", appId);
        } else {
            log.info("Application {} not marked VERIFIED, was not in state NEW", appId);
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

        String appState = jdbc.queryForObject("select state from application where id=?", String.class, app.getId());

        try {
            if (AppState.VERIFIED.toString().equals(appState)) {
                // with registrar session, since only VO admin can approve application
                approveApplication(registrarSession, app.getId());
            }
        } catch (Exception ex) {

            // switch between new and old mail manager
            if (useMailManager==true) {
                ArrayList<Exception> list = new ArrayList<Exception>();
                list.add(ex);
                getMailManager().sendMessage(app, MailType.APP_ERROR_VO_ADMIN, null, list);
            } else {
                log.error("[REGISTRAR] Unable to send APP_APPROVED_USER mail, because new way of sending is disabled");
            }

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
     * @param federValues
     * @return map with names
     */
    private Map<String, String> extractNames(Map<String, String> federValues) throws PerunException {

        String commonName = federValues.get(shibCommonNameVar);
        String displayName = federValues.get(shibDisplayNameVar);
        Map<String, String> parsedName;
        if (displayName != null) {
            parsedName = Utils.parseCommonName(displayName);
        } else if (commonName != null) {
            parsedName = Utils.parseCommonName(commonName);
        } else {
            parsedName = new HashMap<String, String>();
            parsedName.put("firstName", federValues.get(shibFirstNameVar));
            parsedName.put("lastName", federValues.get(shibLastNameVar));
        }
        return parsedName;

    }

    /**
     * Return RegistrarModule for specific application form (VO or Group)
     * so it can be used for more actions.
     *
     * @param form
     * @return RegistrarModule if present or null
     */
    private RegistrarModule getRegistrarModule(ApplicationForm form) {

        RegistrarModule module = null;

        if (form == null) {
            // wrong input
            log.error("[REGISTRAR] Application form is null when getting it's registrar module.");
            throw new NullPointerException("Application form is null when getting it's registrar module.");
        }
        if (form.getModuleClassName() == null || form.getModuleClassName().trim().equals("")) {
            // module not set
            return module;
        }

        try {
            log.debug("Attempting to instantiate class {}...", MODULE_PACKAGE_PATH + form.getModuleClassName());
            module = (RegistrarModule) Class.forName(MODULE_PACKAGE_PATH + form.getModuleClassName()).newInstance();
        } catch (Exception ex) {
            log.error("[REGISTRAR] Exception when instantiating module: {}", ex);
            return module;
        }
        log.debug("Class {} successfully created.", MODULE_PACKAGE_PATH + form.getModuleClassName());

        return module;

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
     * @throws PerunException
     */
    private void storeApplicationAttributes(Application app) throws PerunException {

        // user and member must exists if it's extension !!
        User user = usersManager.getUserById(registrarSession, app.getUser().getId());
        Member member = membersManager.getMemberByUser(registrarSession, app.getVo(), user);

        // get all app items
        List<ApplicationFormItemData> items = getApplicationDataById(registrarSession, app.getId());

        // attributes to set
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (ApplicationFormItemData item : items) {
            String destAttr = item.getFormItem().getPerunDestinationAttribute();
            String newValue = item.getValue();
            // do not store null or empty values at all
            if (newValue == null || newValue.isEmpty()) continue;
            // if correct destination attribute
            if (destAttr != null && !destAttr.isEmpty()) {
                // get attribute (for user and member only)
                Attribute a = null;
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
                    if (a.getType().equalsIgnoreCase("java.util.LinkedHashMap")) {
                        // FIXME do not set hash map attributes - not supported in GUI and registrar
                        continue;
                    } else if (a.getType().equalsIgnoreCase("java.util.ArrayList")) {
                        // we expects that list contains strings
                        ArrayList<String> value = ((ArrayList<String>)a.getValue());
                        // if value not present in list => add
                        if (value == null) {
                            // set as new value
                            value = new ArrayList<String>();
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
     * @throws PerunException
     */
    private void storeApplicationLoginAttributes(Application app) throws PerunException {

        // user must exists
        User user = usersManager.getUserById(registrarSession, app.getUser().getId());

        // get all app items
        List<ApplicationFormItemData> items = getApplicationDataById(registrarSession, app.getId());

        // attributes to set
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (ApplicationFormItemData item : items) {
            String destAttr = item.getFormItem().getPerunDestinationAttribute();
            String newValue = item.getValue();
            // do not store null or empty values at all
            if (newValue == null || newValue.isEmpty()) continue;
            // if correct destination attribute
            if (destAttr != null && !destAttr.isEmpty()) {
                // get login attribute (for user only)
                Attribute a = null;
                if (destAttr.contains("urn:perun:user:attribute-def:def:login-namespace:")) {
                    a = attrManager.getAttribute(registrarSession, user, destAttr);
                } else {
                    continue;
                }

                // if attribute exists
                if (a != null) {
                    // skip if login already existed
                    if (a.getValue() != null && !((String)a.getValue()).isEmpty()) {
                        continue;
                    } else {
                        // set login attribute if initial (new) value
                        a.setValue(newValue);
                        attributes.add(a);
                    }
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
    private List<Pair<String, String>> unreserveNewLoginsFromSameNamespace(List<Pair<String, String>> logins, User user) throws PerunException {

        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();

        List<Attribute> loginAttrs = perun.getAttributesManagerBl().getLogins(registrarSession, user);

        for (Pair<String, String> pair : logins) {
            boolean found = false;
            for (Attribute a : loginAttrs) {
                if (pair.getLeft().equals(a.getFriendlyNameParameter())) {
                    // old login found in same namespace => unreserve new login from KDC
                    perun.getUsersManagerBl().deletePassword(registrarSession, pair.getRight(), pair.getLeft());
                    log.debug("[REGISTRAR] Unreserving new login: "+pair.getRight()+" in namespace: "+pair.getLeft()+" since user already have login: "+a.getValue()+" in same namespace.");
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


    // ------------------ MAPPERS AND SELECTS -------------------------------------

    private static final String APP_SELECT = "select a.id as id,a.vo_id as vo_id, a.group_id as group_id,a.apptype as apptype,a.fed_info as fed_info,a.state as state," +
            "a.user_id as user_id,a.extsourcename as extsourcename, a.extsourcetype as extsourcetype, a.extsourceloa as extsourceloa, a.user_id as user_id, a.created_at as app_created_at, a.created_by as app_created_by, a.modified_at as app_modified_at, a.modified_by as app_modified_by, " +
            "v.name as vo_name, v.short_name as vo_short_name, v.created_by as vo_created_by, v.created_at as vo_created_at, v.modified_by as vo_modified_by, " +
            "v.modified_at as vo_modified_at, g.name as group_name, g.dsc as group_description, g.created_by as group_created_by, g.created_at as group_created_at, g.modified_by as group_modified_by, " +
            "g.modified_at as group_modified_at, g.vo_id as group_vo_id, g.parent_group_id as group_parent_group_id, u.first_name as user_first_name, u.last_name as user_last_name, u.middle_name as user_middle_name, " +
            "u.title_before as user_title_before, u.title_after as user_title_after from application a left outer join vos v on a.vo_id = v.id left outer join groups g on a.group_id = g.id left outer join users u on a.user_id = u.id";

    private static final String FORM_SELECT = "select id,vo_id,group_id,automatic_approval,automatic_approval_extension,module_name from application_form";

    private static RowMapper<Application> APP_MAPPER = new RowMapper<Application>() {

        @Override
        public Application mapRow(ResultSet rs, int i) throws SQLException {

            Application app = new Application(rs.getInt("id"), new Vo(rs.getInt("vo_id"),
                    rs.getString("vo_name"), rs.getString("vo_short_name"),
                    rs.getString("vo_created_at"), rs.getString("vo_created_by"),
                    rs.getString("vo_modified_at"), rs.getString("vo_modified_by")),
                    null, Application.AppType.valueOf(rs.getString("apptype")),
                    rs.getString("fed_info"), AppState.valueOf(rs.getString("state")),
                    rs.getString("extsourcename"), rs.getString("extsourcetype"),
                    rs.getInt("extsourceloa"), null);

            // if group present
            if (rs.getInt("group_id") != 0) {
                app.setGroup(new Group(rs.getInt("group_id"), rs.getString("group_name"),
                        rs.getString("group_description"), rs.getString("group_created_at"),
                        rs.getString("group_created_by"), rs.getString("group_modified_at"),
                        rs.getString("group_modified_by")));
                app.getGroup().setVoId(rs.getInt("vo_id"));

                if (rs.getInt("group_parent_group_id") != 0) {
                    app.getGroup().setParentGroupId(rs.getInt("group_parent_group_id"));
                }

            }

            // if user present
            if (rs.getInt("user_id") != 0) {
                app.setUser(new User(rs.getInt("user_id"), rs.getString("user_first_name"),
                        rs.getString("user_last_name"), rs.getString("user_middle_name"),
                        rs.getString("user_title_before"), rs.getString("user_title_after")));
            }

            app.setCreatedAt(rs.getString("app_created_at"));
            app.setCreatedBy(rs.getString("app_created_by"));
            app.setModifiedAt(rs.getString("app_modified_at"));
            app.setModifiedBy(rs.getString("app_modified_by"));

            return app;

        }
    };

    private static RowMapper<ApplicationFormItem> ITEM_MAPPER = new RowMapper<ApplicationFormItem>() {
        @Override
        public ApplicationFormItem mapRow(ResultSet rs, int i) throws SQLException {
            ApplicationFormItem app = new ApplicationFormItem(rs.getInt("id"),
                    rs.getString("shortname"), rs.getBoolean("required"),
                    Type.valueOf(rs.getString("type")), rs.getString("fed_attr"),
                    rs.getString("dst_attr"), rs.getString("regex"));
            app.setOrdnum(rs.getInt("ordnum"));
            return app;
        }
    };

    private static RowMapper<ApplicationFormItem.ItemTexts> ITEM_TEXTS_MAPPER = new RowMapper<ApplicationFormItem.ItemTexts>() {
        @Override
        public ItemTexts mapRow(ResultSet rs, int i) throws SQLException {
            return new ItemTexts(new Locale(rs.getString("locale")),
                    rs.getString("label"), rs.getString("options"), rs.getString("help"),
                    rs.getString("error_message"));
        }
    };

}