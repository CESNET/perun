package cz.metacentrum.perun.core.impl;

import static cz.metacentrum.perun.core.api.AttributesManager.NS_ENTITYLESS_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_FACILITY_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_GROUP_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_GROUP_RESOURCE_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_HOST_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_MEMBER_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_MEMBER_GROUP_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_MEMBER_RESOURCE_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_RESOURCE_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_UES_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_USER_FACILITY_ATTR;
import static cz.metacentrum.perun.core.api.AttributesManager.NS_VO_ATTR;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicy;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RoleObject;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.HostAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * AttributesManager implementation.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
@SuppressWarnings("Duplicates")
public class AttributesManagerImpl implements AttributesManagerImplApi {

  //Items of list delimiter. It's used while storing list into string.
  //Can't contain regex special symbols
  public static final char LIST_DELIMITER = ',';
  public static final char KEY_VALUE_DELIMITER = ':';
  public static final int MAX_SIZE_FOR_IN_CLAUSE = 32000;
  // mapping of the perun bean names to the attribute namespaces
  public static final Map<String, String> BEANS_TO_NAMESPACES_MAP = new LinkedHashMap<>();
  /**
   * List of allowed values for attribute type.
   *
   * @see cz.metacentrum.perun.core.api.BeansUtils#attributeValueToString(Attribute)
   */
  public static final List<String> ATTRIBUTE_TYPES =
      Arrays.asList(String.class.getName(), Integer.class.getName(), Boolean.class.getName(), ArrayList.class.getName(),
          LinkedHashMap.class.getName());
  protected static final String ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY =
      "attr_names.id as attr_names_id," + "attr_names.friendly_name as attr_names_friendly_name," +
      "attr_names.namespace as attr_names_namespace," + "attr_names.type as attr_names_type," +
      "attr_names.display_name as attr_names_display_name," + "attr_names.dsc as attr_names_dsc," +
      "attr_names.is_unique as attr_names_unique," + "attr_names.created_at as attr_names_created_at," +
      "attr_names.created_by as attr_names_created_by," + "attr_names.modified_by as attr_names_modified_by," +
      "attr_names.modified_at as attr_names_modified_at," + "attr_names.created_by_uid as attr_names_created_by_uid," +
      "attr_names.modified_by_uid as attr_names_modified_by_uid";
  protected static final RowMapper<AttributeDefinition> ATTRIBUTE_DEFINITION_MAPPER = (rs, i) -> {
    AttributeDefinition attribute = new AttributeDefinition();
    attribute.setId(rs.getInt("attr_names_id"));
    attribute.setFriendlyName(rs.getString("attr_names_friendly_name"));
    attribute.setNamespace(rs.getString("attr_names_namespace"));
    attribute.setType(rs.getString("attr_names_type"));
    attribute.setDisplayName(rs.getString("attr_names_display_name"));
    attribute.setDescription(rs.getString("attr_names_dsc"));
    attribute.setUnique(rs.getBoolean("attr_names_unique"));
    attribute.setCreatedAt(rs.getString("attr_names_created_at"));
    attribute.setCreatedBy(rs.getString("attr_names_created_by"));
    attribute.setModifiedAt(rs.getString("attr_names_modified_at"));
    attribute.setModifiedBy(rs.getString("attr_names_modified_by"));
    if (rs.getInt("attr_names_modified_by_uid") == 0) {
      attribute.setModifiedByUid(null);
    } else {
      attribute.setModifiedByUid(rs.getInt("attr_names_modified_by_uid"));
    }
    if (rs.getInt("attr_names_created_by_uid") == 0) {
      attribute.setCreatedByUid(null);
    } else {
      attribute.setCreatedByUid(rs.getInt("attr_names_created_by_uid"));
    }
    return attribute;
  };
  static final RowMapper<Attribute> ATTRIBUTE_MAPPER = (rs, i) -> {

    AttributeDefinition attributeDefinition = ATTRIBUTE_DEFINITION_MAPPER.mapRow(rs, i);

    Attribute attribute = new Attribute(attributeDefinition);
    attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
    attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
    attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
    attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));

    String stringValue = rs.getString("attr_value");

    try {
      attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
    } catch (InternalErrorException ex) {
      throw new InternalErrorException(ex);
    }

    attribute.setDescription(rs.getString("attr_names_dsc"));

    return attribute;
  };
  private static final String ATTRIBUTES_MODULES_PACKAGE = "cz.metacentrum.perun.core.impl.modules.attributes";
  private static final int MERGE_TRY_CNT = 10;
  private static final long MERGE_RAND_SLEEP_MAX = 100;  //max sleep time between SQL merge attempt in millisecond
  private static final Logger LOG = LoggerFactory.getLogger(AttributesManagerImpl.class);
  private static final Map<String, String> ENTITIES_TO_BEANS_MAP = new HashMap<>();
  private static final List<String> SINGLE_BEAN_ATTRIBUTES =
      Arrays.asList("user", "member", "facility", "vo", "host", "group", "resource", "user_ext_source");
  private static final List<String> DOUBLE_BEAN_ATTRIBUTES =
      Arrays.asList("member_resource", "member_group", "user_facility", "group_resource");
  private static final String ATTRIBUTE_POLICY_COLLECTION_MAPPING_SELECT_QUERY =
      "attribute_policy_collections.id as attribute_policy_collections_id, " +
      "attribute_policy_collections.attr_id as attribute_policy_collections_attr_id, " +
      "attribute_policy_collections.action as attribute_policy_collections_action, " +
      "attribute_policies.id as attribute_policies_id, " +
      "attribute_policies.role_id as attribute_policies_role_id, " +
      "attribute_policies.object as attribute_policies_object, " +
      "attribute_policies.policy_collection_id as attribute_policies_policy_collection_id, " +
      "roles.name as roles_name ";
  private static final RowMapper<AttributePolicy> ATTRIBUTE_POLICY_MAPPER =
      (rs, i) -> new AttributePolicy(rs.getInt("attribute_policies_id"), rs.getString("roles_name").toUpperCase(),
          RoleObject.valueOf(rs.getString("attribute_policies_object")),
          rs.getInt("attribute_policies_policy_collection_id"));
  private static final RowMapper<String> ENTITYLESS_KEYS_MAPPER = (rs, i) -> rs.getString("subject");
  private static final RowMapper<String> ATTRIBUTE_NAMES_MAPPER = (rs, i) -> rs.getString("attr_name");
  private static final RowMapper<String> ATTRIBUTE_FRIENDLY_NAMES_MAPPER = (rs, i) -> rs.getString("friendly_name");
  private static final RowMapper<String> APP_FORM_ITEM_SHORTNAME_MAPPER = (rs, i) -> rs.getString("shortname");
  private static final RowMapper<ApplicationForm> APPLICATION_FORM_ROW_MAPPER = (resultSet, i) -> {
    ApplicationForm form = new ApplicationForm();
    form.setId(resultSet.getInt("id"));
    Vo vo = new Vo();
    vo.setId(resultSet.getInt("vo_id"));
    form.setVo(vo);
    if (resultSet.getInt("group_id") > 0) {
      Group grp = new Group();
      grp.setId(resultSet.getInt("group_id"));
      form.setGroup(grp);
    }
    return form;
  };

  static {
    //map db table prefixes to attribute namespaces, e.g. user_ext_source -> urn:perun:ues:attribute-def
    BEANS_TO_NAMESPACES_MAP.put("user", NS_USER_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("member", NS_MEMBER_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("facility", NS_FACILITY_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("vo", NS_VO_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("host", NS_HOST_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("group", NS_GROUP_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("resource", NS_RESOURCE_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("member_resource", NS_MEMBER_RESOURCE_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("member_group", NS_MEMBER_GROUP_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("user_facility", NS_USER_FACILITY_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("group_resource", NS_GROUP_RESOURCE_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("user_ext_source", NS_UES_ATTR);
    BEANS_TO_NAMESPACES_MAP.put("entityless", NS_ENTITYLESS_ATTR);
    //create reverse mapping, e.g. ues -> user_ext_source
    for (Map.Entry<String, String> entry : BEANS_TO_NAMESPACES_MAP.entrySet()) {
      ENTITIES_TO_BEANS_MAP.put(entry.getValue().split(":")[2], entry.getKey());
    }
  }

  //Attributes modules.  name => module
  private final Map<String, AttributesModuleImplApi> attributesModulesMap = new ConcurrentHashMap<>();
  //Uninitialized attributes modules.  name => module
  private final Map<String, AttributesModuleImplApi> uninitializedAttributesModulesMap = new ConcurrentHashMap<>();
  private Perun perun;
  // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private JdbcPerunTemplate jdbc;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private AttributesManagerImplApi self;

  /**
   * Constructor.
   *
   * @param perunPool connection pool instance
   */
  public AttributesManagerImpl(DataSource perunPool) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
    this.jdbc = new JdbcPerunTemplate(perunPool);
    this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  /**
   * Escapes LIST_DELIMITER from the value.
   *
   * @return escaped value
   */
  public static String escapeListAttributeValue(String value) {
    if (value == null) {
      return null;
    }
    value = value.replace("\\", "\\\\");   //escape char '\'
    value = value.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER

    return value;
  }

  /**
   * Escapes LIST_DELIMITER and KEY_VALUE_DELIMITER from the value.
   *
   * @return escaped value
   */
  public static String escapeMapAttributeValue(String value) {
    if (value == null) {
      return null;
    }

    value = value.replace("\\", "\\\\");   //escape char '\'
    value = value.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
    value =
        value.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER

    return value;
  }

  static String getAttributeMappingSelectQuery(String nameOfValueTable) {
    return ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + ", attr_value" + ", " + nameOfValueTable +
           ".created_at as attr_value_created_at" + ", " + nameOfValueTable + ".created_by as attr_value_created_by" +
           ", " + nameOfValueTable + ".modified_at as attr_value_modified_at" + ", " + nameOfValueTable +
           ".modified_by as attr_value_modified_by";
  }

  private static ResultSetExtractor<List<AttributePolicyCollection>> getAttributePoliciesExtractor() {
    return resultSet -> {
      Map<Integer, AttributePolicyCollection> attributePolicyCollections = new HashMap<>();
      while (resultSet.next()) {
        AttributePolicy policy = ATTRIBUTE_POLICY_MAPPER.mapRow(resultSet, resultSet.getRow());

        int policyCollectionId = resultSet.getInt("attribute_policy_collections_id");
        int attributeId = resultSet.getInt("attribute_policy_collections_attr_id");
        AttributeAction action = AttributeAction.valueOf(resultSet.getString("attribute_policy_collections_action"));

        AttributePolicyCollection policyCollection = attributePolicyCollections.get(policyCollectionId);
        if (policyCollection != null) {
          policyCollection.addPolicy(policy);
        } else {
          attributePolicyCollections.put(policyCollectionId,
              new AttributePolicyCollection(policyCollectionId, attributeId, action,
                  new ArrayList<>(Collections.singletonList(policy))));
        }
      }

      return new ArrayList<>(attributePolicyCollections.values());
    };
  }

  /**
   * Converts attribute definition namespace to entity name used as a prefix for database table names. E.g.
   * "urn:perun:ues:attribute-def" to "user_ext_source".
   *
   * @param attributeDefinition attribute definition
   * @return entity name
   */
  private static String attributeToTablePrefix(AttributeDefinition attributeDefinition) {
    return ENTITIES_TO_BEANS_MAP.get(attributeDefinition.getNamespace().split(":")[2]);
  }

  @Override
  public boolean attributeExists(PerunSession sess, AttributeDefinition attribute) {
    Utils.notNull(attribute, "attribute");
    Utils.notNull(attribute.getName(), "attribute.name");
    Utils.notNull(attribute.getNamespace(), "attribute.namespace");
    Utils.notNull(attribute.getType(), "attribute.type");

    try {
      return 1 == jdbc.queryForInt(
          "select count('x') from attr_names where attr_name=? and friendly_name=? and namespace=? and id=? and type=?",
          attribute.getName(), attribute.getFriendlyName(), attribute.getNamespace(), attribute.getId(),
          attribute.getType());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  /**
   * Convert name of the attribute to name of the apropriate attribute module
   *
   * @param attributeName name to convert
   * @return name of attribute module
   */
  private String attributeNameToModuleName(String attributeName) {
    return ATTRIBUTES_MODULES_PACKAGE + "." + attributeName.replaceAll(":|-|[.]", "_");
  }

  /**
   * Build string for purposes of SQL query with given parameters.
   *
   * @param params     parameters to print
   * @param afterParam string, which will be inserted after each parameter
   * @param separator  string, which will be inserted between parameters
   * @return built query
   */
  private String buildParameters(List<String> params, String afterParam, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      sb.append(params.get(i));
      sb.append(afterParam);
      if (i < params.size() - 1) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  @Override
  public void blockAttributeValue(PerunSession session, Attribute attribute) {
    try {
      switch (attribute.getType()) {
        case "java.lang.Integer":
          jdbc.update(
                "INSERT INTO blocked_attr_values (attr_id, attr_value, created_by_uid, modified_by_uid)" +
                    " VALUES (?,?,?,?)",
                        attribute.getId(), attribute.valueAsInteger().toString(),
              session.getPerunPrincipal().getUserId(), session.getPerunPrincipal().getUserId());
          break;
        case "java.lang.String":
          jdbc.update(
                "INSERT INTO blocked_attr_values (attr_id, attr_value, created_by_uid, modified_by_uid)" +
                    " VALUES (?,?,?,?)",
                        attribute.getId(), attribute.valueAsString(),
              session.getPerunPrincipal().getUserId(), session.getPerunPrincipal().getUserId());
          break;
        case "java.util.ArrayList":
          for (String s : attribute.valueAsList()) {
            jdbc.update(
                "INSERT INTO blocked_attr_values (attr_id, attr_value, created_by_uid, modified_by_uid)" +
                    " VALUES (?,?,?,?)",
                        attribute.getId(), s, session.getPerunPrincipal().getUserId(),
                session.getPerunPrincipal().getUserId());
          }
          break;
        case "java.util.LinkedHashMap":
          for (Map.Entry<String, String> entry : attribute.valueAsMap().entrySet()) {
            jdbc.update(
                "INSERT INTO blocked_attr_values (attr_id, attr_value, created_by_uid, modified_by_uid)" +
                    " VALUES (?,?,?,?)",
                  attribute.getId(), entry.getKey() + "=" + entry.getValue(),
                session.getPerunPrincipal().getUserId(), session.getPerunPrincipal().getUserId());
          }
          break;
        default:
          break;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Facility facility, Attribute attribute) {
    //Call attribute module
    FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
    if (facilityModule == null) {
      return; //facility module doesn't exists
    }
    facilityModule.changedAttributeHook((PerunSessionImpl) sess, facility, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, String key, Attribute attribute) {
    //Call attribute module
    EntitylessAttributesModuleImplApi entitylessModule = getEntitylessAttributeModule(sess, attribute);
    if (entitylessModule == null) {
      return; //facility module doesn't exists
    }
    entitylessModule.changedAttributeHook((PerunSessionImpl) sess, key, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Vo vo, Attribute attribute) {
    //Call attribute module
    VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
    if (voModule == null) {
      return; //facility module doesn't exists
    }
    voModule.changedAttributeHook((PerunSessionImpl) sess, vo, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Host host, Attribute attribute) {
    //Call attribute module
    HostAttributesModuleImplApi hostModule = getHostAttributeModule(sess, attribute);
    if (hostModule == null) {
      return; //host module doesn't exists
    }
    hostModule.changedAttributeHook((PerunSessionImpl) sess, host, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
    if (groupModule == null) {
      return; //facility module doesn't exists
    }
    groupModule.changedAttributeHook((PerunSessionImpl) sess, group, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    UserAttributesModuleImplApi userModule = getUserAttributeModule(sess, attribute);
    if (userModule == null) {
      return; //facility module doesn't exists
    }
    userModule.changedAttributeHook((PerunSessionImpl) sess, user, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Member member, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    MemberAttributesModuleImplApi memberModule = getMemberAttributeModule(sess, attribute);
    if (memberModule == null) {
      return; //facility module doesn't exists
    }
    memberModule.changedAttributeHook((PerunSessionImpl) sess, member, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Resource resource, Group group, Attribute attribute) {
    //Call attribute module
    GroupResourceAttributesModuleImplApi resourceGroupModule = getResourceGroupAttributeModule(sess, attribute);
    if (resourceGroupModule == null) {
      return; //facility module doesn't exists
    }
    resourceGroupModule.changedAttributeHook((PerunSessionImpl) sess, group, resource, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    ResourceAttributesModuleImplApi resourceModule = getResourceAttributeModule(sess, attribute);
    if (resourceModule == null) {
      return; //facility module doesn't exists
    }
    resourceModule.changedAttributeHook((PerunSessionImpl) sess, resource, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    MemberResourceAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
    if (resourceMemberGroupModule == null) {
      return; //facility module doesn't exists
    }
    resourceMemberGroupModule.changedAttributeHook((PerunSessionImpl) sess, member, resource, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Member member, Group group, Attribute attribute) {
    //Call attribute module
    MemberGroupAttributesModuleImplApi memberGroupAttributesModule = getMemberGroupAttributeModule(sess, attribute);
    if (memberGroupAttributesModule == null) {
      return; //memberGroupAttributesModule module doesn't exists
    }
    memberGroupAttributesModule.changedAttributeHook((PerunSessionImpl) sess, member, group, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, Facility facility, User user, Attribute attribute) {
    //Call attribute module
    UserFacilityAttributesModuleImplApi facilityUserModule = getFacilityUserAttributeModule(sess, attribute);
    if (facilityUserModule == null) {
      return; //facility module doesn't exists
    }
    facilityUserModule.changedAttributeHook((PerunSessionImpl) sess, user, facility, attribute);
  }

  @Override
  public void changedAttributeHook(PerunSession sess, UserExtSource ues, Attribute attribute) {
    //Call attribute module
    UserExtSourceAttributesModuleImplApi uesModule = getUserExtSourceAttributeModule(sess, attribute);
    if (uesModule == null) {
      return;
    }
    uesModule.changedAttributeHook((PerunSessionImpl) sess, ues, attribute);
  }

  private void checkAttributeExists(PerunSession sess, AttributeDefinition attribute, String expectedNamespace)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    checkAttributeExists(sess, attribute);
    checkNamespace(sess, attribute, expectedNamespace);
  }

  @Override
  public void checkAttributeExists(PerunSession sess, AttributeDefinition attribute)
      throws AttributeNotExistsException {
    if (!attributeExists(sess, attribute)) {
      throw new AttributeNotExistsException("Attribute: " + attribute);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
    if (facilityModule == null) {
      return; //facility module doesn't exists
    }
    try {
      facilityModule.checkAttributeSemantics((PerunSessionImpl) sess, facility, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Vo vo, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //Call attribute module
    VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
    if (voModule == null) {
      return; //module doesn't exists
    }
    voModule.checkAttributeSemantics((PerunSessionImpl) sess, vo, attribute);
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
    if (groupModule == null) {
      return; //module doesn't exists
    }
    try {
      groupModule.checkAttributeSemantics((PerunSessionImpl) sess, group, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, resource, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    MemberResourceAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
    if (resourceMemberGroupModule == null) {
      return; //facility module doesn't exists
    }
    try {
      resourceMemberGroupModule.checkAttributeSemantics((PerunSessionImpl) sess, member, resource, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    //Call attribute module
    MemberGroupAttributesModuleImplApi memberGroupAttributeModule = getMemberGroupAttributeModule(sess, attribute);
    if (memberGroupAttributeModule == null) {
      return; //memberGroupAttributesModule module doesn't exists
    }
    memberGroupAttributeModule.checkAttributeSemantics((PerunSessionImpl) sess, member, group, attribute);
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    UserFacilityAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, user, facility, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, user, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Member member, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, member, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Host host, Attribute attribute) {
    HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, host, attribute);
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    GroupResourceAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, group, resource, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, String key, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    EntitylessAttributesModuleImplApi attributeModule = getEntitylessAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    try {
      attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, key, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSession sess, UserExtSource ues, Attribute attribute) {
    UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSemantics((PerunSessionImpl) sess, ues, attribute);
  }

  @SuppressWarnings("unused")
  public void checkAttributeSyntax(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongAttributeValueException {
    for (Attribute attribute : attributes) {
      checkAttributeSyntax(sess, resource, attribute);
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {
    //Call attribute module
    FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
    if (facilityModule == null) {
      return; //facility module doesn't exists
    }
    facilityModule.checkAttributeSyntax((PerunSessionImpl) sess, facility, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Vo vo, Attribute attribute) throws WrongAttributeValueException {
    //Call attribute module
    VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
    if (voModule == null) {
      return; //module doesn't exists
    }
    voModule.checkAttributeSyntax((PerunSessionImpl) sess, vo, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    //Call attribute module
    GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
    if (groupModule == null) {
      return; //module doesn't exists
    }
    groupModule.checkAttributeSyntax((PerunSessionImpl) sess, group, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, resource, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    //Call attribute module
    MemberResourceAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
    if (resourceMemberGroupModule == null) {
      return; //facility module doesn't exists
    }
    resourceMemberGroupModule.checkAttributeSyntax((PerunSessionImpl) sess, member, resource, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    //Call attribute module
    MemberGroupAttributesModuleImplApi memberGroupAttributeModule = getMemberGroupAttributeModule(sess, attribute);
    if (memberGroupAttributeModule == null) {
      return; //memberGroupAttributesModule module doesn't exists
    }
    memberGroupAttributeModule.checkAttributeSyntax((PerunSessionImpl) sess, member, group, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeValueException {
    UserFacilityAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, user, facility, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, user, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException {
    MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, member, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Host host, Attribute attribute)
      throws WrongAttributeValueException {
    HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, host, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    GroupResourceAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, group, resource, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, String key, Attribute attribute)
      throws WrongAttributeValueException {
    EntitylessAttributesModuleImplApi attributeModule = getEntitylessAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, key, attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeValueException {
    UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    attributeModule.checkAttributeSyntax((PerunSessionImpl) sess, ues, attribute);
  }

  @SuppressWarnings("unused")
  public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes,
                                    String expectedNamespace)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    Utils.notNull(attributes, "attributes");
    for (AttributeDefinition attribute : attributes) {
      checkAttributeExists(sess, attribute, expectedNamespace);
    }
  }

  @Override
  public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes)
      throws AttributeNotExistsException {
    Utils.notNull(attributes, "attributes");
    for (AttributeDefinition attribute : attributes) {
      checkAttributeExists(sess, attribute);
    }
  }

  @SuppressWarnings("unused")
  public void checkAttributesSemantics(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongReferenceAttributeValueException {
    for (Attribute attribute : attributes) {
      checkAttributeSemantics(sess, resource, attribute);
    }
  }

  @Override
  public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace)
      throws WrongAttributeAssignmentException {
    if (!isFromNamespace(attribute, namespace)) {
      throw new WrongAttributeAssignmentException(attribute);
    }
  }

  @Override
  public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace)
      throws WrongAttributeAssignmentException {
    for (AttributeDefinition attribute : attributes) {
      checkNamespace(sess, attribute, namespace);
    }
  }

  @Override
  public int convertAttributeValuesToNonunique(PerunSession session, AttributeDefinition attrDef) {
    String tablePrefix = attributeToTablePrefix(attrDef);
    try {
      return jdbc.update("DELETE FROM " + tablePrefix + "_attr_u_values WHERE attr_id=?", attrDef.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void convertAttributeValuesToUnique(PerunSession session, AttributeDefinition attrDef) {
    try {
      String tablePrefix = attributeToTablePrefix(attrDef);
      jdbc.update(Compatibility.getLockTable(tablePrefix + "_attr_values"));
      if (tablePrefix.equals("user_ext_source") || !tablePrefix.contains("_")) {
        //attribute of a single perun bean, e.g. user
        @SuppressWarnings("UnnecessaryLocalVariable") String bean = tablePrefix;
        jdbc.query("SELECT " + bean + "_id,attr_value FROM " + tablePrefix + "_attr_values WHERE attr_id=?", rs -> {
          int beanId = rs.getInt(1);
          Object value = null;
          try {
            value = BeansUtils.stringToAttributeValue(rs.getString("attr_value"), attrDef.getType());
            Utils.notNull(value, "value");
            switch (attrDef.getType()) {
              case "java.lang.String":
              case "java.lang.Integer":
              case "java.lang.Boolean":
                jdbc.update(
                    "INSERT INTO " + tablePrefix + "_attr_u_values (" + bean + "_id,attr_id,attr_value) VALUES (?,?,?)",
                    beanId, attrDef.getId(), value.toString());
                break;
              case "java.util.ArrayList":
                @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) value;
                for (String s : list) {
                  jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean +
                              "_id,attr_id,attr_value) VALUES (?,?,?)", beanId, attrDef.getId(), s);
                }
                break;
              case "java.util.LinkedHashMap":
                @SuppressWarnings("unchecked") LinkedHashMap<String, String> map =
                    (LinkedHashMap<String, String>) value;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                  jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean +
                              "_id,attr_id,attr_value) VALUES (?,?,?)", beanId, attrDef.getId(),
                      entry.getKey() + "=" + entry.getValue());
                }
                break;
              default:
                break;
            }

          } catch (InternalErrorException e) {
            throw new InternalErrorException(e);
          } catch (DuplicateKeyException ex) {
            throw new InternalErrorException(
                "value " + value + " of attribute " + attrDef.getName() + " for " + bean + "=" + beanId +
                " is not unique", ex);
          }
        }, attrDef.getId());
      } else {
        //attribute of relation between perun beans, e.g. group_resource
        String[] ss = tablePrefix.split("_");
        String bean1 = ss[0];
        String bean2 = ss[1];
        jdbc.query(
            "SELECT " + bean1 + "_id," + bean2 + "_id,attr_value FROM " + tablePrefix + "_attr_values WHERE attr_id=?",
            rs -> {
              int bean1Id = rs.getInt(1);
              int bean2Id = rs.getInt(2);
              Object value = null;
              try {
                value = BeansUtils.stringToAttributeValue(rs.getString("attr_value"), attrDef.getType());
                Utils.notNull(value, "value");
                switch (attrDef.getType()) {
                  case "java.lang.String":
                  case "java.lang.Integer":
                  case "java.lang.Boolean":
                    jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 +
                                "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(),
                        value.toString());
                    break;
                  case "java.util.ArrayList":
                    @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) value;
                    for (String s : list) {
                      jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 +
                                  "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(), s);
                    }
                    break;
                  case "java.util.LinkedHashMap":
                    @SuppressWarnings("unchecked") LinkedHashMap<String, String> map =
                        (LinkedHashMap<String, String>) value;
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                      jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 +
                                  "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(),
                          entry.getKey() + "=" + entry.getValue());
                    }
                    break;
                  default:
                    break;
                }
              } catch (InternalErrorException e) {
                throw new InternalErrorException(e);
              } catch (DuplicateKeyException ex) {
                throw new InternalErrorException(
                    "value " + value + " of attribute " + attrDef.getName() + " for " + bean1 + "=" + bean1Id + "," +
                    bean2 + "=" + bean2Id + " is not unique", ex);
              }
            }, attrDef.getId());
      }
    } catch (InternalErrorException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute)
      throws AttributeDefinitionExistsException {
    if (!attribute.getFriendlyName().matches(AttributesManager.ATTRIBUTES_REGEXP)) {
      throw new InternalErrorException(new IllegalArgumentException(
          "Wrong attribute name " + attribute.getFriendlyName() + ", attribute name must match " +
          AttributesManager.ATTRIBUTES_REGEXP));
    }
    try {
      int attributeId = Utils.getNewId(jdbc, "attr_names_id_seq");

      jdbc.update(
          "insert into attr_names (id, attr_name, type, dsc, namespace, friendly_name, display_name, is_unique, " +
          "created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
          "values (?,?,?,?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
          attributeId, attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.getNamespace(),
          attribute.getFriendlyName(), attribute.getDisplayName(), attribute.isUnique(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      attribute.setId(attributeId);
      LOG.debug("Attribute created: {}.", attribute);

      return attribute;
    } catch (DataIntegrityViolationException e) {
      throw new AttributeDefinitionExistsException("Attribute " + attribute.getName() + " already exists", attribute,
          e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

  }

  @Override
  public List<RichMember> decorateMembersWithDefOptMemberAttributes(PerunSession sess, List<RichMember> members,
                                                                    List<String> memberAttrNames) {
    List<RichMember> membersToReturn = new ArrayList<>();
    if (members.isEmpty()) {
      return members;
    }

    // only 32000 members in one query as the temporary table created for postgres IN clause uses short
    for (int batch = 0; batch <= members.size() / (MAX_SIZE_FOR_IN_CLAUSE + 1); batch++) {
      Map<Integer, RichMember> idMemberMap = new HashMap<>();
      for (int i = batch * MAX_SIZE_FOR_IN_CLAUSE; i < Math.min((batch + 1) * MAX_SIZE_FOR_IN_CLAUSE, members.size());
           i++) {
        idMemberMap.put(members.get(i).getId(), members.get(i));
      }

      MapSqlParameterSource parameters = new MapSqlParameterSource();
      parameters.addValue("mIds", idMemberMap.keySet());
      parameters.addValue("nSD", AttributesManager.NS_MEMBER_ATTR_DEF);
      parameters.addValue("nSO", AttributesManager.NS_MEMBER_ATTR_OPT);
      parameters.addValue("memberAttrNames", memberAttrNames);

      try {
        membersToReturn.addAll(namedParameterJdbcTemplate.query(
            "select member_id, " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
            "join member_attr_values mem on id=mem.attr_id and member_id in ( :mIds ) " +
            "where namespace in ( :nSD,:nSO ) and attr_names.attr_name in ( :memberAttrNames )", parameters,
            getMemberDefAttributesExtractor(sess, idMemberMap)));
      } catch (RuntimeException ex) {
        throw new InternalErrorException(ex);
      }
    }
    return membersToReturn;
  }

  @Override
  public List<RichMember> decorateMembersWithDefOptUserAttributes(PerunSession sess, List<RichMember> members,
                                                                  List<String> userAttrNames) {
    List<RichMember> membersToReturn = new ArrayList<>();
    if (members.isEmpty()) {
      return members;
    }

    // only 32000 members in one query as the temporary table created for postgres IN clause uses short
    for (int batch = 0; batch <= members.size() / (MAX_SIZE_FOR_IN_CLAUSE + 1); batch++) {
      MapSqlParameterSource parameters = new MapSqlParameterSource();
      Map<Integer, RichMember> userIdMemberMap = new HashMap<>();
      for (int i = batch * MAX_SIZE_FOR_IN_CLAUSE; i < Math.min((batch + 1) * MAX_SIZE_FOR_IN_CLAUSE, members.size());
           i++) {
        userIdMemberMap.put(members.get(i).getUserId(), members.get(i));
      }

      parameters.addValue("uIds", userIdMemberMap.keySet());
      parameters.addValue("nSD", AttributesManager.NS_USER_ATTR_DEF);
      parameters.addValue("nSO", AttributesManager.NS_USER_ATTR_OPT);
      parameters.addValue("userAttrNames", userAttrNames);

      try {
        membersToReturn.addAll(namedParameterJdbcTemplate.query(
            "select user_id, " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
            "join user_attr_values usr on id=usr.attr_id and user_id in ( :uIds ) " +
            "where namespace in ( :nSD,:nSO ) and attr_names.attr_name in ( :userAttrNames )", parameters,
            getUserDefAttributesExtractor(sess, userIdMemberMap)));
      } catch (RuntimeException ex) {
        throw new InternalErrorException(ex);
      }
    }
    return membersToReturn;
  }

  @Override
  public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) {
    try {
      // unique attributes get deleted by deletion from entity_attr_values
      jdbc.update("DELETE FROM " + attributeToTablePrefix(attribute) + "_attr_values WHERE attr_id=?",
          attribute.getId());
      jdbc.update("DELETE FROM attr_names WHERE id=?", attribute.getId());

      LOG.debug("Attribute deleted: {}.", attribute);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  /**
   * Escapes QUERY PARAMETER VALUES in URL value. Does not modify domain or parameters names. e.g. 'domain/?vo=vo name'
   * => 'domain/?vo=vo+name'
   *
   * @return url with escaped special characters in query parameter's values
   */
  public String escapeQueryParameters(String value) {
    if (value == null) {
      return null;
    }

    String queryDelimiter = "[?&].*?=";
    String[] values = value.split(queryDelimiter);
    String result = values[0]; // domain

    for (int i = 0; i < values.length - 1; i++) {
      // find which query parameter was the delimiter between values
      String parameter = StringUtils.substringBetween(value, values[i], values[i + 1]);

      // escape special characters in the query parameter value, but don't overwrite it in values
      result = result.concat(parameter).concat(URLEncoder.encode(values[i + 1], StandardCharsets.UTF_8));
    }

    return result;
  }

  @Override
  public void deletedEntityHook(PerunSession sess, User user, Attribute attribute) {
    UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      return;
    }
    LOG.debug("Called deletedEntityHook in module for user " + user + " and attr " + attribute);
    attributeModule.deletedEntityHook((PerunSessionImpl) sess, user, attribute);
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) {
    //Use attributes module
    ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }

    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Member member, Resource resource, Attribute attribute) {
    //Use attributes module
    MemberResourceAttributesModuleImplApi attributeModule = getResourceMemberAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }

    return attributeModule.fillAttribute((PerunSessionImpl) sess, member, resource, attribute);
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) {
    //Use attributes module
    MemberGroupAttributesModuleImplApi attributeModule = getMemberGroupAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }

    return attributeModule.fillAttribute((PerunSessionImpl) sess, member, group, attribute);
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) {
    //Use attributes module
    UserFacilityAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }

    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, user, facility, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) {
    UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }
    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, user, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) {
    MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}", attribute.getName());
      return attribute;
    }
    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, member, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) {
    HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }
    return attributeModule.fillAttribute((PerunSessionImpl) sess, host, attribute);
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) {
    GroupResourceAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }
    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, group, resource, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) {
    GroupAttributesModuleImplApi attributeModule = getGroupAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }
    try {
      return attributeModule.fillAttribute((PerunSessionImpl) sess, group, attribute);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) {
    UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
    if (attributeModule == null) {
      LOG.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
      return attribute;
    }
    return attributeModule.fillAttribute((PerunSessionImpl) sess, ues, attribute);
  }

  @Override
  public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group,
                                                                       String startPartOfName) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("gId", group.getId());
    parameters.addValue("nSC", AttributesManager.NS_GROUP_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_GROUP_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_GROUP_ATTR_DEF);
    parameters.addValue("startPartOfName", startPartOfName + "%");

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("grt") + " from attr_names " +
                                              "left join group_attr_values grt on id=grt.attr_id and group_id=:gId " +
                                              "where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name LIKE " +
                                              ":startPartOfName", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, group));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource,
                                                                       String startPartOfName) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("rId", resource.getId());
    parameters.addValue("nSC", AttributesManager.NS_RESOURCE_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_RESOURCE_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_RESOURCE_ATTR_DEF);
    parameters.addValue("startPartOfName", startPartOfName + "%");

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("ret") + " from attr_names " +
                                              "left join resource_attr_values ret on id=ret.attr_id and " +
                                              "resource_id=:rId " +
                                              "where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name LIKE " +
                                              ":startPartOfName", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, resource));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Object> getAllGroupResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("SELECT attr_value FROM group_resource_attr_values WHERE attr_id=?",
          new ValueRowMapper(attributeDefinition), attributeDefinition.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Object> getAllGroupValues(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("SELECT attr_value FROM group_attr_values WHERE attr_id=?",
          new ValueRowMapper(attributeDefinition), attributeDefinition.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<String> getAllNamespaces(PerunSession sess) {
    return jdbc.query(
        "SELECT friendly_name FROM attr_names WHERE friendly_name LIKE 'login-namespace:%%' AND attr_name NOT LIKE " +
        "'%%def:virt%%'", ATTRIBUTE_FRIENDLY_NAMES_MAPPER);
  }

  @Override
  public List<Object> getAllResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("SELECT attr_value FROM resource_attr_values WHERE attr_id=?",
          new ValueRowMapper(attributeDefinition), attributeDefinition.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName) {
    try {
      return jdbc.query("SELECT attr_name FROM attr_names WHERE attr_name LIKE ? || '%'", ATTRIBUTE_NAMES_MAPPER,
          startingPartOfAttributeName);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<RichAttribute<User, Facility>> getAllUserFacilityRichAttributes(PerunSession sess, User user) {
    try {
      return jdbc.query(
          "select " + getAttributeMappingSelectQuery("usr_fac") + ", " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
          ", " + FacilitiesManagerImpl.FACILITY_MAPPING_SELECT_QUERY + "   from attr_names " +
          "left join    user_facility_attr_values     usr_fac      on attr_names.id=usr_fac.attr_id     and   " +
          "usr_fac.user_id=? " + "join users on users.id = usr_fac.user_id " +
          "join facilities on facilities.id = usr_fac.facility_id " +
          "where namespace in (?,?) and usr_fac.attr_value is not null",
          new RichAttributeRowMapper<>(new SingleBeanAttributeRowMapper<>(sess, this, user),
              UsersManagerImpl.USER_MAPPER, FacilitiesManagerImpl.FACILITY_MAPPER), user.getId(),
          AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for user-facility combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Object> getAllUserValues(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("SELECT attr_value FROM user_attr_values WHERE attr_id=?",
          new ValueRowMapper(attributeDefinition), attributeDefinition.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAnonymizedValue(PerunSession sess, User user, Attribute attribute)
      throws AnonymizationNotSupportedException {
    UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
    if (attributeModule == null) {
      throw new AnonymizationNotSupportedException(
          "Cannot get anonymized attribute value. There is no module for: " + attribute.getName() + ".");
    }

    return attributeModule.getAnonymizedValue((PerunSessionImpl) sess, user, attribute);
  }

  @Override
  public List<String> getAppFormItemsForAppFormAndAttribute(PerunSession sess, int appFormId,
                                                            AttributeDefinition attr) {
    try {
      String urn = attr.getName();
      return jdbc.query(
          "select id, shortname from application_form_items afi where afi.form_id=? and (afi.src_attr=? or afi" +
          ".dst_attr=?)", APP_FORM_ITEM_SHORTNAME_MAPPER, appFormId, urn, urn);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<ApplicationForm> getAppFormsWhereAttributeRelated(PerunSession sess, AttributeDefinition attr) {
    try {
      String urn = attr.getName();
      return jdbc.query("select distinct af.id, af.vo_id, af.group_id from application_form af " +
                        "join application_form_items afi on af.id = afi.form_id " +
                        "where afi.src_attr=? or afi.dst_attr=?", APPLICATION_FORM_ROW_MAPPER, urn, urn);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") +
                                 " from attr_names left join facility_attr_values on id=attr_id and facility_id=? " +
                                 "where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, facility),
          facility.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Facility attribute - attribute.name='" + attributeName + "'");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") +
                                 " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where " +
                                 "attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(),
          attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Vo attribute - attribute.name='" + attributeName + "'");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Group group, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("group_attr_values") +
                                 " from attr_names left join group_attr_values on id=attr_id and group_id=? where " +
                                 "attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(),
          attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Group attribute - attribute.name='" + attributeName + "'");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") +
                                 " from attr_names left join resource_attr_values on id=attr_id and resource_id=? " +
                                 "where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, resource),
          resource.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Resource attribute - attribute.name='" + attributeName + "'");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Member member, Resource resource, String attributeName)
      throws AttributeNotExistsException {
    try {
      //member-resource attributes, member core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                 "left join   member_resource_attr_values mem    on id=mem.attr_id and mem" +
                                 ".resource_id=? and member_id=?" + " " + "where attr_name=?",
          new MemberResourceAttributeRowMapper(sess, this, member, resource), resource.getId(), member.getId(),
          attributeName);


    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName)
      throws AttributeNotExistsException {
    try {
      //member-group attributes, member core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
                                 "left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr" +
                                 ".group_id=? and member_id=? " + "where attr_name=?",
          new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(), attributeName);

    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Member member, String attributeName)
      throws AttributeNotExistsException {
    //member and member core attributes
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                 "left join      member_attr_values    mem    on      id=mem.attr_id    and   " +
                                 "member_id=? " + "where attr_name=?",
          new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
                                 "left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     " +
                                 "and   facility_id=? " + "and user_id=? " + "where attr_name=?",
          new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(),
          attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws AttributeNotExistsException {
    //user and user core attributes
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                                 "left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=?" +
                                 " " + "where " + "attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, user),
          user.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
                                 "left join host_attr_values on id=attr_id and host_id=? where attr_name=?",
          new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Host attribute - attribute.name='" + attributeName + "'");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
                                 "left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     " +
                                 "and   resource_id=?" + " and group_id=? " + "where attr_name=?",
          new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(),
          attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, String key, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + getAttributeMappingSelectQuery("entityless_attr_values") + " from attr_names " +
          "left join    entityless_attr_values     on id=entityless_attr_values.attr_id     and   subject=? " +
          "where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, null), key, attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + getAttributeMappingSelectQuery("user_ext_source_attr_values") + " from attr_names " +
          "left join user_ext_source_attr_values on id=attr_id and user_ext_source_id=? " + "where attr_name=?",
          new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(), attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") +
                                 " from attr_names left join facility_attr_values on id=attr_id and facility_id=? " +
                                 "where id=?", new SingleBeanAttributeRowMapper<>(sess, this, facility),
          facility.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") +
                                 " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") +
                                 " from attr_names left join resource_attr_values on id=attr_id and resource_id=? " +
                                 "where id=?", new SingleBeanAttributeRowMapper<>(sess, this, resource),
          resource.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Member member, Resource resource, int id)
      throws AttributeNotExistsException {
    try {
      //member-resource attributes, member core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                 "left join   member_resource_attr_values mem    on id=mem.attr_id and mem" +
                                 ".resource_id=? and member_id=? " + "where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, member), resource.getId(), member.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id)
      throws AttributeNotExistsException {
    try {
      //member-group attributes, member core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
                                 "left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr" +
                                 ".group_id=? and member_id=? " + "where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, member), group.getId(), member.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Member member, int id) throws AttributeNotExistsException {
    try {
      //member and member core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                 "left join      member_attr_values    mem    on      id=mem.attr_id    and   " +
                                 "member_id=? " + "where id=?", new SingleBeanAttributeRowMapper<>(sess, this, member),
          member.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
                                 "left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     " +
                                 "and   facility_id=? " + "and user_id=? " + "where id=?",
          new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, User user, int id) throws AttributeNotExistsException {
    try {
      //user and user core attributes
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                                 "left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=?" +
                                 " " + "where id=?", new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(),
          id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Host host, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") +
                                 " from attr_names left join host_attr_values on id=attr_id and host_id=? where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
                                 "left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     " +
                                 "and   resource_id=?" + " and group_id=? " + "where id=?",
          new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, Group group, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
                                 "left join group_attr_values grp on id=grp.attr_id and group_id=? " + "where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("ues") +
                                 " from attr_names left join user_ext_source_attr_values ues on id=ues.attr_id and " +
                                 "user_ext_source_id=? " + "where id=?",
          new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(), id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject(
          "SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + " FROM attr_names WHERE attr_name=?",
          ATTRIBUTE_DEFINITION_MAPPER, attributeName);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute - attribute.name='" + attributeName + "'", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + " FROM attr_names WHERE id=?",
          ATTRIBUTE_DEFINITION_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AttributePolicyCollection> getAttributePolicyCollections(PerunSession sess, final int attributeId) {
    List<AttributePolicyCollection> attributePolicyCollections;
    try {
      attributePolicyCollections = jdbc.query(
          "SELECT " + ATTRIBUTE_POLICY_COLLECTION_MAPPING_SELECT_QUERY + " FROM attribute_policies " +
          " JOIN attribute_policy_collections ON attribute_policies" +
          ".policy_collection_id=attribute_policy_collections.id " +
          " JOIN roles ON attribute_policies.role_id=roles.id " + " WHERE attr_id=?", getAttributePoliciesExtractor(),
          attributeId);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    return attributePolicyCollections;
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Facility facility) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("fac") + " from attr_names " +
                        "left join facility_attr_values fac    on id=fac.attr_id and fac.facility_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(),
          AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_DEF,
          AttributesManager.NS_FACILITY_ATTR_OPT);

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for facility exists.");
      return new ArrayList<>();

    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Facility facility, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("fId", facility.getId());
    parameters.addValue("nSC", AttributesManager.NS_FACILITY_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_FACILITY_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_FACILITY_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_FACILITY_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("fav") + " from attr_names " +
                                              "left join facility_attr_values fav on id=fav.attr_id and " +
                                              "facility_id=:fId " +
                                              "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in" +
                                              " ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, facility));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Vo vo) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("voattr") + " from attr_names " +
                        "left join vo_attr_values voattr    on id=voattr.attr_id and voattr.vo_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(), AttributesManager.NS_VO_ATTR_CORE,
          AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("groupattr") + " from attr_names " +
                        "left join group_attr_values groupattr    on id=groupattr.attr_id and groupattr.group_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE,
          AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Resource resource) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
                        "left join resource_attr_values on id=attr_id and resource_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(),
          AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_DEF,
          AttributesManager.NS_RESOURCE_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for resource exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource) {
    try {
      //member-resource attributes, member core attributes
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                        "left join   member_resource_attr_values   mem        on attr_names.id=mem.attr_id and mem" +
                        ".resource_id=? and member_id=? " + "where namespace in (?,?) and mem.attr_value is not null",
          new MemberResourceAttributeRowMapper(sess, this, member, resource), resource.getId(), member.getId(),
          AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for member-resource combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) {
    try {
      //member-group attributes, member core attributes
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
                        "left join member_group_attr_values mem_gr on attr_names.id=mem_gr.attr_id and mem_gr" +
                        ".group_id=? and " + "member_id=? " +
                        "where namespace in (?,?) and mem_gr.attr_value is not null",
          new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(),
          AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for member-group combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mId", member.getId());
    parameters.addValue("gId", group.getId());
    parameters.addValue("nSO", AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
          "left join member_group_attr_values mem_gr on id=mem_gr.attr_id and member_id=:mId and group_id=:gId " +
          "where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )", parameters,
          new MemberGroupAttributeRowMapper(sess, this, member, group));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mId", member.getId());
    parameters.addValue("rId", resource.getId());
    parameters.addValue("nSO", AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("mem_res") + " from attr_names " +
          "left join member_resource_attr_values mem_res on id=mem_res.attr_id and member_id=:mId and " +
          "resource_id=:rId " + "where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
          parameters, new MemberResourceAttributeRowMapper(sess, this, member, resource));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("gId", group.getId());
    parameters.addValue("rId", resource.getId());
    parameters.addValue("nSO", AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
          "left join group_resource_attr_values grp_res on id=grp_res.attr_id and group_id=:gId and " +
          "resource_id=:rId " + "where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
          parameters, new GroupResourceAttributeRowMapper(sess, this, group, resource));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, User user, Facility facility, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uId", user.getId());
    parameters.addValue("fId", facility.getId());
    parameters.addValue("nSO", AttributesManager.NS_USER_FACILITY_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_USER_FACILITY_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("user_fac") + " from attr_names " +
          "left join user_facility_attr_values user_fac on id=user_fac.attr_id and user_id=:uId and " +
          "facility_id=:fId " + "where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
          parameters, new UserFacilityAttributeRowMapper(sess, this, user, facility));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                        "left join    member_attr_values     mem      on id=mem.attr_id     and    member_id=? " +
                        "where namespace=? or (namespace in (?,?) and mem.attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), AttributesManager.NS_MEMBER_ATTR_CORE,
          AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_DEF);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for member exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("vId", vo.getId());
    parameters.addValue("nSC", AttributesManager.NS_VO_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_VO_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_VO_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_VO_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("vot") + " from attr_names " +
                                              "left join vo_attr_values vot on id=vot.attr_id and vo_id=:vId " +
                                              "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in" +
                                              " ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, vo));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mId", member.getId());
    parameters.addValue("nSC", AttributesManager.NS_MEMBER_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_MEMBER_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_MEMBER_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_MEMBER_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                              "left join member_attr_values mem on id=mem.attr_id and member_id=:mId " +
                                              "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in" +
                                              " ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, member));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("gId", group.getId());
    parameters.addValue("nSC", AttributesManager.NS_GROUP_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_GROUP_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_GROUP_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_GROUP_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("groupattr") + " from attr_names " +
          "left join group_attr_values groupattr on id=groupattr.attr_id and group_id=:gId " +
          "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, group));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Resource resource, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("rId", resource.getId());
    parameters.addValue("nSC", AttributesManager.NS_RESOURCE_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_RESOURCE_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_RESOURCE_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_RESOURCE_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("resattr") + " from attr_names " +
          "left join resource_attr_values resattr on id=resattr.attr_id and resource_id=:rId " +
          "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, resource));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uesId", ues.getId());
    parameters.addValue("nSC", AttributesManager.NS_UES_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_UES_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_UES_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_UES_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
                                              "left join user_ext_source_attr_values ues on id=ues.attr_id and " +
                                              "user_ext_source_id=:uesId " +
                                              "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in" +
                                              " ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, ues));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
                        "left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   " +
                        "facility_id=? " + "and user_id=? " +
                        "where namespace in (?,?) and usr_fac.attr_value is not null",
          new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(),
          AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for user-facility combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, String key) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
                        "left join entityless_attr_values enattr on id=enattr.attr_id and enattr.subject=? " +
                        "where namespace in (?,?) and enattr.attr_value is not null",
          new SingleBeanAttributeRowMapper<>(sess, this, null), key, AttributesManager.NS_ENTITYLESS_ATTR_DEF,
          AttributesManager.NS_ENTITYLESS_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, User user) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                        "left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(), AttributesManager.NS_USER_ATTR_CORE,
          AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for user exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uId", user.getId());
    parameters.addValue("nSC", AttributesManager.NS_USER_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_USER_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_USER_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_USER_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                                              "left join user_attr_values usr on id=usr.attr_id and user_id=:uId " +
                                              "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in" +
                                              " ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, user));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Host host) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
                        "left join host_attr_values on id=attr_id and host_id=? " +
                        "where namespace=? or (namespace in (?,?) and attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), AttributesManager.NS_HOST_ATTR_CORE,
          AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for host exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Host host, List<String> attrNames) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("hId", host.getId());
    parameters.addValue("nSC", AttributesManager.NS_HOST_ATTR_CORE);
    parameters.addValue("nSO", AttributesManager.NS_HOST_ATTR_OPT);
    parameters.addValue("nSD", AttributesManager.NS_HOST_ATTR_DEF);
    parameters.addValue("nSV", AttributesManager.NS_HOST_ATTR_VIRT);
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
          "left join host_attr_values on id=attr_id and host_id=:hId " +
          "where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, host));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
                        "left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   " +
                        "resource_id=?" + " and group_id=? " +
                        "where namespace in (?,?) and grp_res.attr_value is not null",
          new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(),
          AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for user-facility combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
                        "left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=? " +
                        "where namespace=? or (namespace in (?,?) and ues.attr_value is not null)",
          new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(), AttributesManager.NS_UES_ATTR_CORE,
          AttributesManager.NS_UES_ATTR_DEF, AttributesManager.NS_UES_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for UserExtSource exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess,
                                                            AttributeDefinition attributeDefinition) {
    // Get the entity from the name
    String entity = attributeDefinition.getEntity();
    try {
      return jdbc.query(
          "select " + getAttributeMappingSelectQuery(entity + "_attr_values") + " from attr_names join " + entity +
          "_attr_values on id=attr_id  where attr_name=?", ATTRIBUTE_MAPPER, attributeDefinition.getName());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) {
    try {
      return jdbc.query("SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + ", NULL AS attr_value FROM attr_names",
          ATTRIBUTE_DEFINITION_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute definition exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) {
    try {
      return jdbc.query(
  "SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + ", NULL AS attr_value FROM attr_names WHERE namespace=?",
          ATTRIBUTE_DEFINITION_MAPPER, namespace);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute definition with namespace='{}' exists.", namespace);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Get the attribute module for the attribute
   *
   * @param attribute get the attribute module for this attribute
   */
  @Override
  public Object getAttributesModule(PerunSession sess, AttributeDefinition attribute) {

    // core attributes doesn't have modules !!
    if (isCoreAttribute(sess, attribute)) {
      return null;
    }

    String moduleName;
    //first try to find specific module including parameter of attribute (full friendly name)
    if (!attribute.getFriendlyName().equals(attribute.getBaseFriendlyName())) {
      moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getFriendlyName());
      Object attributeModule = attributesModulesMap.get(moduleName);
      if (attributeModule != null) {
        return attributeModule;
      }
    }

    //if specific module not exists or attribute has no parameter, find the common one
    moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName());
    Object attributeModule = attributesModulesMap.get(moduleName);
    if (attributeModule == null) {
      LOG.trace("Attribute module not found. Module name={}", moduleName);
    }
    return attributeModule;
  }

  @Override
  public List<AttributeAction> getCriticalAttributeActions(PerunSession sess, int attrId) {
    try {
      return jdbc.queryForList("SELECT action FROM attribute_critical_actions WHERE attr_id=" + attrId,
          AttributeAction.class);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public String getEntitylessAttrValueForUpdate(PerunSession sess, int attrId, String key)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject(
          "SELECT attr_value FROM entityless_attr_values WHERE subject=? AND attr_id=? FOR UPDATE", String.class, key,
          attrId);
    } catch (EmptyResultDataAccessException ex) {
      //If there is no such entityless attribute, create new one with null value and return null (insert is for
      // transaction same like select for update)
      Attribute attr = new Attribute(this.getAttributeDefinitionById(sess, attrId));
      this.setAttributeCreatedAndModified(sess, attr);
      self.setAttributeWithNullValue(sess, key, attr);
      return null;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  private EntitylessAttributesModuleImplApi getEntitylessAttributeModule(PerunSession sess,
                                                                         AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof EntitylessAttributesModuleImplApi) {
      return (EntitylessAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't EntitylessAttributesModuleImplApi");
    }
  }

  @Override
  public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
                        "left join entityless_attr_values enattr on id=enattr.attr_id " +
                        "where attr_name=? and namespace in (?,?) and enattr.attr_value is not null",
          new SingleBeanAttributeRowMapper<>(sess, this, null), attrName, AttributesManager.NS_ENTITYLESS_ATTR_DEF,
          AttributesManager.NS_ENTITYLESS_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("SELECT subject FROM attr_names JOIN entityless_attr_values ON id=attr_id  WHERE attr_name=?",
          ENTITYLESS_KEYS_MAPPER, attributeDefinition.getName());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Map<String, String> getEntitylessStringAttributeMapping(PerunSession sess, String attributeName)
      throws AttributeNotExistsException {
    try {
      Map<String, String> map = new HashMap<>();
      jdbc.query("select subject, attr_value " + " from attr_names join entityless_attr_values on id=attr_id " +
                 " where type='java.lang.String' and attr_name=?", rs -> {
          map.put(rs.getString(1), rs.getString(2));
        }, attributeName);
      return map;
    } catch (EmptyResultDataAccessException ex) {
      throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Get facility attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance facility attribute module null if the module doesn't exist
   */
  private FacilityAttributesModuleImplApi getFacilityAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof FacilityAttributesModuleImplApi) {
      return (FacilityAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
    }
  }

  /**
   * Get user-facility attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance user-facility attribute module, null if the module doesn't exists
   */
  private UserFacilityAttributesModuleImplApi getFacilityUserAttributeModule(PerunSession sess,
                                                                             AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof UserFacilityAttributesModuleImplApi) {
      return (UserFacilityAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't FacilityUserAttributesModule");
    }
  }

  /**
   * Get user-facility virtual attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance user-facility attribute module, null if the module doesn't exists
   */
  private UserFacilityVirtualAttributesModuleImplApi getFacilityUserVirtualAttributeModule(PerunSession sess,
                                                                                     AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof UserFacilityVirtualAttributesModuleImplApi) {
      return (UserFacilityVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't FacilityUserVirtualAttributesModule");
    }
  }

  /**
   * Get facility virtual attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance facility attribute module
   */
  private FacilityVirtualAttributesModuleImplApi getFacilityVirtualAttributeModule(PerunSession sess,
                                                                                   AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof FacilityVirtualAttributesModuleImplApi) {
      return (FacilityVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't FacilityVirtualAttributesModule");
    }
  }

  /**
   * Get Group attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance group attribute module null if the module doesn't exist
   */
  private GroupAttributesModuleImplApi getGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof GroupAttributesModuleImplApi) {
      return (GroupAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't GroupAttributesModule");
    }
  }

  /**
   * Get group virtual attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance of member attribute module
   */
  private GroupVirtualAttributesModuleImplApi getGroupVirtualAttributeModule(PerunSession sess,
                                                                             AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof GroupVirtualAttributesModuleImplApi) {
      return (GroupVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't GroupVirtualAttributesModule");
    }
  }

  /**
   * Get Host attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance host attribute module null if the module doesn't exist
   */
  private HostAttributesModuleImplApi getHostAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof HostAttributesModuleImplApi) {
      return (HostAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't HostAttributesModule");
    }
  }

  /**
   * Get member attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance userattribute module, null if the module doesn't exists
   */
  private MemberAttributesModuleImplApi getMemberAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof MemberAttributesModuleImplApi) {
      return (MemberAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't MemberAttributesModule. " + attribute);
    }
  }

  private ResultSetExtractor<List<RichMember>> getMemberDefAttributesExtractor(PerunSession sess,
                                                                         Map<Integer, RichMember> memberIdMemberMap) {
    return resultSet -> {
      while (resultSet.next()) {
        RichMember currentMember = memberIdMemberMap.get(resultSet.getInt("member_id"));
        Attribute memberAttribute =
            new SingleBeanAttributeRowMapper<>(sess, this, currentMember).mapRow(resultSet, resultSet.getRow());
        currentMember.addMemberAttribute(memberAttribute);
      }
      return new ArrayList<>(memberIdMemberMap.values());
    };
  }

  /**
   * Get member-group attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance member group attribute module null if the module doesn't exist
   */
  private MemberGroupAttributesModuleImplApi getMemberGroupAttributeModule(PerunSession sess,
                                                                           AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof MemberGroupAttributesModuleImplApi) {
      return (MemberGroupAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't MemberGroupAttributesModule");
    }
  }

  /**
   * Get member-group attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance member-group attribute module null if the module doesn't exists
   */
  MemberGroupVirtualAttributesModuleImplApi getMemberGroupVirtualAttributeModule(PerunSession sess,
                                                                                 AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof MemberGroupVirtualAttributesModuleImplApi) {
      return (MemberGroupVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't MemberGroupVirtualAttributesModuleImplApi");
    }
  }

  /**
   * Get member virtual attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance of member attribute module
   */
  private MemberVirtualAttributesModuleImplApi getMemberVirtualAttributeModule(PerunSession sess,
                                                                               AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof MemberVirtualAttributesModuleImplApi) {
      return (MemberVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't MemberVirtualAttributesModule");
    }
  }

  @Override
  public Set<Pair<Integer, Integer>> getPerunBeanIdsForUniqueAttributeValue(PerunSession sess, Attribute attribute) {
    if (attribute.getValue() == null) {
      return Collections.emptySet();
    }
    String beanPrefix = attributeToTablePrefix(attribute);
    String sql;
    if (SINGLE_BEAN_ATTRIBUTES.contains(beanPrefix)) {
      sql = "select " + beanPrefix + "_id, 0 from " + beanPrefix + "_attr_u_values where attr_id=? and attr_value=?";
    } else if (DOUBLE_BEAN_ATTRIBUTES.contains(beanPrefix)) {
      String[] s = beanPrefix.split("_");
      String bean1 = s[0];
      String bean2 = s[1];
      sql = "select " + bean1 + "_id," + bean2 + "_id from " + beanPrefix +
            "_attr_u_values where attr_id=? and attr_value=?";
    } else {
      throw new RuntimeException("getPerunBeanIdsForUniqueAttributeValue() cannot be used for " + beanPrefix);
    }
    RowMapper<Pair<Integer, Integer>> pairRowMapper = (rs, i) -> new Pair<>(rs.getInt(1), rs.getInt(2));
    HashSet<Pair<Integer, Integer>> ids = new HashSet<>();
    switch (attribute.getType()) {
      case "java.lang.String":
      case "java.lang.Integer":
      case "java.lang.Boolean":
        ids.addAll(jdbc.query(sql, pairRowMapper, attribute.getId(), attribute.getValue().toString()));
        break;
      case "java.util.ArrayList":
        for (String value : attribute.valueAsList()) {
          ids.addAll(jdbc.query(sql, pairRowMapper, attribute.getId(), value));
        }
        break;
      case "java.util.LinkedHashMap":
        for (Map.Entry<String, String> entry : attribute.valueAsMap().entrySet()) {
          ids.addAll(jdbc.query(sql, pairRowMapper, attribute.getId(), entry.getKey() + "=" + entry.getValue()));

        }
        break;
      default:
        throw new RuntimeException("unknown attribute type " + attribute.getType());
    }
    return ids;
  }

  /**
   * Get ids of attributes which are required by services. Services are known from the resource.
   *
   * @param resource resource from which services are taken
   * @return list of attribute ids
   */
  private List<Integer> getRequiredAttributeIds(Resource resource) {
    return jdbc.queryForList("select distinct service_required_attrs.attr_id from service_required_attrs " +
                             "join resource_services on service_required_attrs.service_id=resource_services" +
                             ".service_id and " + "resource_services.resource_id=?", new Object[] {resource.getId()},
        Integer.class);
  }

  /**
   * Get ids of attributes which are required by the service.
   *
   * @param service service
   * @return list of attribute ids
   */
  private List<Integer> getRequiredAttributeIds(Service service) {
    return jdbc.queryForList(
        "select distinct service_required_attrs.attr_id from service_required_attrs where service_required_attrs" +
        ".service_id=?", new Object[] {service.getId()}, Integer.class);
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
                        "left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=? " +
                        "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=? )",
          new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(),
          AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE,
          AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, resource.getId());

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for facility: {} and services from resource: {}.", facility, resource);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, List<Integer> serviceIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("serviceIds", serviceIds);

    try {
      List<String> namespace = new ArrayList<>();
      namespace.add(AttributesManager.NS_RESOURCE_ATTR_DEF);
      namespace.add(AttributesManager.NS_RESOURCE_ATTR_CORE);
      namespace.add(AttributesManager.NS_RESOURCE_ATTR_OPT);
      namespace.add(AttributesManager.NS_RESOURCE_ATTR_VIRT);

      parameters.addValue("resourceId", resource.getId());
      parameters.addValue("namespace", namespace);

      return this.namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
          "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id" +
          " in (:serviceIds) " +
          "left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values" +
          ".resource_id=:resourceId " + "where namespace in (:namespace)", parameters,
          new SingleBeanAttributeRowMapper<>(sess, this, resource));

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for resource: {} and services with id {}.", resource, serviceIds);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                               Resource resource) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
                        "left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values" +
                        ".resource_id=? " + "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(),
          AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE,
          AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT,
          resourceToGetServicesFrom.getId());

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for resource: {} and services getted from it.",
          resourceToGetServicesFrom);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("member_attr_values") + " from attr_names " +
                        "left join member_attr_values on id=member_attr_values.attr_id and member_attr_values" +
                        ".member_id=? " + "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), AttributesManager.NS_MEMBER_ATTR_DEF,
          AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_OPT,
          AttributesManager.NS_MEMBER_ATTR_VIRT, resourceToGetServicesFrom.getId());

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for resource: {} and services getted from it.",
          resourceToGetServicesFrom);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                               Resource resource) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                        "left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and " +
                        "member_id=?" + " " + "where namespace in (?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new MemberResourceAttributeRowMapper(sess, this, member, resource), resource.getId(), member.getId(),
          AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT,
          AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility, User user) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                        "left join      user_facility_attr_values    usr    on      attr_names.id=usr.attr_id    and " +
                        "  " + "user_id=? and facility_id=? " + "where namespace in (?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new UserFacilityAttributeRowMapper(sess, this, user, facility), user.getId(), facility.getId(),
          AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT,
          AttributesManager.NS_USER_FACILITY_ATTR_VIRT, resource.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource,
                                               Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
                        "left join      group_resource_attr_values    grp   on      attr_names.id=grp.attr_id    and " +
                        "  grp" + ".group_id=? and grp.resource_id=? " + "where namespace in (?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new GroupResourceAttributeRowMapper(sess, this, group, resource), group.getId(), resource.getId(),
          AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT,
          AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, User user) {
    try {
      //user and user core attributes
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                        "left join      user_attr_values    usr    on      attr_names.id=usr.attr_id    and   " +
                        "user_id=? " + "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?) ", new SingleBeanAttributeRowMapper<>(sess, this, user),
          user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF,
          AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT, resource.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
                        "left join      host_attr_values   host    on      attr_names.id=host.attr_id    and   " +
                        "host_id=? " + "where namespace in (?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)", new SingleBeanAttributeRowMapper<>(sess, this, host),
          host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF,
          AttributesManager.NS_HOST_ATTR_OPT, resourceToGetServicesFrom.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
                        "left join      group_attr_values   grp    on      attr_names.id=grp.attr_id    and   " +
                        "group_id=? " + "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)", new SingleBeanAttributeRowMapper<>(sess, this, group),
          group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF,
          AttributesManager.NS_GROUP_ATTR_OPT, AttributesManager.NS_GROUP_ATTR_VIRT, resourceToGetServicesFrom.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
                        "left join facility_attr_values on attr_names.id=facility_attr_values.attr_id and " +
                        "facility_attr_values" + ".facility_id=? " + "where namespace in (?,?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id " +
                        "join resources on resource_services.resource_id=resources.id  and resources.facility_id=?)",
          new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(),
          AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE,
          AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, facility.getId());
    } catch (EmptyResultDataAccessException ex) {
      LOG.info("None required attributes found for facility: {} and services from it's resources.", facility);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=?" + " " +
                        "where namespace in (?,?,?,?)", new SingleBeanAttributeRowMapper<>(sess, this, facility),
          service.getId(), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF,
          AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT,
          AttributesManager.NS_FACILITY_ATTR_VIRT);

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for facility: {} and service: {}.", facility, service);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " + "left join vo_attr_values on id=vo_attr_values.attr_id and vo_id=? " +
                        "where namespace in (?,?,?,?)", new SingleBeanAttributeRowMapper<>(sess, this, vo),
          service.getId(), vo.getId(), AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_CORE,
          AttributesManager.NS_VO_ATTR_OPT, AttributesManager.NS_VO_ATTR_VIRT);

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for vo: {} and service: {}.", vo, service);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values" +
                        ".resource_id=? " + "where namespace in (?,?,?,?)",
          new SingleBeanAttributeRowMapper<>(sess, this, resource), service.getId(), resource.getId(),
          AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE,
          AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT);

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None required attributes found for resource: {} and service {}.", resource, service);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Resource resource) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +

                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +

                        "left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and " +
                        "member_id=?" + " " + "where namespace in (?,?,?)",
          new MemberResourceAttributeRowMapper(sess, this, member, resource), service.getId(), resource.getId(),
          member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT,
          AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource,
                                                                List<Member> members) {
    try {
      return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
                          "JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND " +
                          "service_required_attrs" + ".service_id=? " + "JOIN members ON members.id " +
                          Compatibility.getStructureForInClause() +
                          "LEFT JOIN member_resource_attr_values mem ON attr_names.id=mem.attr_id AND mem" +
                          ".resource_id=? " + "AND mem.member_id=members.id WHERE namespace IN (?,?,?)",
          (PreparedStatementCallback<HashMap<Member, List<Attribute>>>) preparedStatement -> {
            Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(members, preparedStatement);
            preparedStatement.setInt(1, service.getId());
            preparedStatement.setArray(2, sqlArray);
            preparedStatement.setInt(3, resource.getId());
            preparedStatement.setString(4, AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
            preparedStatement.setString(5, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
            preparedStatement.setString(6, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
            MemberAttributeExtractor memberAttributeExtractor =
                new MemberAttributeExtractor(sess, this, resource, members);
            return memberAttributeExtractor.extractData(preparedStatement.executeQuery());
          });
    } catch (InternalErrorException ex) {
      //Finding or invoking oracle array method was unsuccessful
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service,
                                                                List<Member> members) {
    try {
      return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
                          "JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND " +
                          "service_required_attrs" + ".service_id=? " + "JOIN members ON members.id " +
                          Compatibility.getStructureForInClause() +
                          "LEFT JOIN member_attr_values mem ON attr_names.id=mem.attr_id " +
                          "AND mem.member_id=members.id WHERE namespace IN (?,?,?,?)",
          (PreparedStatementCallback<HashMap<Member, List<Attribute>>>) preparedStatement -> {
            Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(members, preparedStatement);
            preparedStatement.setInt(1, service.getId());
            preparedStatement.setArray(2, sqlArray);
            preparedStatement.setString(3, AttributesManager.NS_MEMBER_ATTR_CORE);
            preparedStatement.setString(4, AttributesManager.NS_MEMBER_ATTR_DEF);
            preparedStatement.setString(5, AttributesManager.NS_MEMBER_ATTR_OPT);
            preparedStatement.setString(6, AttributesManager.NS_MEMBER_ATTR_VIRT);
            MemberAttributeExtractor memberAttributeExtractor = new MemberAttributeExtractor(sess, this, members);
            return memberAttributeExtractor.extractData(preparedStatement.executeQuery());
          });
    } catch (InternalErrorException ex) {
      //Finding or invoking oracle array method was unsuccessful
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility,
                                                              List<User> users) {
    try {
      return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("usr_fac") + ", users.id FROM attr_names " +
                          "JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND " +
                          "service_required_attrs" + ".service_id=? " + "JOIN users ON users.id " +
                          Compatibility.getStructureForInClause() +
                          "LEFT JOIN user_facility_attr_values usr_fac ON attr_names.id=usr_fac.attr_id AND " +
                          "facility_id=? AND " + "user_id=users.id " + "WHERE namespace IN (?,?,?)",
          (PreparedStatementCallback<HashMap<User, List<Attribute>>>) preparedStatement -> {
            Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(users, preparedStatement);
            preparedStatement.setInt(1, service.getId());
            preparedStatement.setArray(2, sqlArray);
            preparedStatement.setInt(3, facility.getId());
            preparedStatement.setString(4, AttributesManager.NS_USER_FACILITY_ATTR_DEF);
            preparedStatement.setString(5, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
            preparedStatement.setString(6, AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
            UserAttributeExtractor userAttributeExtractor = new UserAttributeExtractor(sess, this, users, facility);
            return userAttributeExtractor.extractData(preparedStatement.executeQuery());
          });
    } catch (InternalErrorException ex) {
      //Finding or invoking oracle array method was unsuccessful
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) {
    try {
      return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("usr") + ", users.id FROM attr_names " +
                          "JOIN service_required_attrs on attr_names.id=service_required_attrs.attr_id AND " +
                          "service_required_attrs" + ".service_id=? " + "JOIN users ON users.id " +
                          Compatibility.getStructureForInClause() +
                          "LEFT JOIN user_attr_values usr ON attr_names.id=usr.attr_id AND user_id=users.id " +
                          "WHERE namespace IN (?,?,?,?)",
          (PreparedStatementCallback<HashMap<User, List<Attribute>>>) preparedStatement -> {
            Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(users, preparedStatement);
            preparedStatement.setInt(1, service.getId());
            preparedStatement.setArray(2, sqlArray);
            preparedStatement.setString(3, AttributesManager.NS_USER_ATTR_CORE);
            preparedStatement.setString(4, AttributesManager.NS_USER_ATTR_DEF);
            preparedStatement.setString(5, AttributesManager.NS_USER_ATTR_OPT);
            preparedStatement.setString(6, AttributesManager.NS_USER_ATTR_VIRT);
            UserAttributeExtractor userAttributeExtractor = new UserAttributeExtractor(sess, this, users);
            return userAttributeExtractor.extractData(preparedStatement.executeQuery());
          });
    } catch (InternalErrorException ex) {
      //Finding or invoking oracle array method was unsuccessful
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and " +
                        "member_id=? " + "where namespace in (?,?,?)",
          new MemberGroupAttributeRowMapper(sess, this, member, group), service.getId(), group.getId(), member.getId(),
          AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT,
          AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Map<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<Member> members,
                                                            Group group) {
    return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("mem_gr") + ", members.id FROM attr_names " +
                        "JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND " +
                        "service_required_attrs" + ".service_id=? " + "JOIN members ON members.id " +
                        Compatibility.getStructureForInClause() +
                        "LEFT JOIN member_group_attr_values mem_gr ON attr_names.id=mem_gr.attr_id AND group_id=? AND" +
                        " " + "member_id=members.id " + "WHERE namespace IN (?,?,?)",
        (PreparedStatementCallback<HashMap<Member, List<Attribute>>>) preparedStatement -> {
          Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(members, preparedStatement);
          preparedStatement.setInt(1, service.getId());
          preparedStatement.setArray(2, sqlArray);
          preparedStatement.setInt(3, group.getId());
          preparedStatement.setString(4, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
          preparedStatement.setString(5, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
          preparedStatement.setString(6, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
          MemberGroupAttributeExtractor memberAttributeExtractor =
              new MemberGroupAttributeExtractor(sess, this, members, group);
          return memberAttributeExtractor.extractData(preparedStatement.executeQuery());
        });
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                               Group group) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
                        "left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and " +
                        "member_id=? " + "where namespace in (?,?,?) " +
                        "and attr_names.id in (select distinct service_required_attrs.attr_id from " +
                        "service_required_attrs " +
                        "join resource_services on service_required_attrs.service_id=resource_services.service_id and" +
                        " " + "resource_services.resource_id=?)",
          new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(),
          AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT,
          AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT, resourceToGetServicesFrom.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) {
    //member and member core attributes
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
                        "where namespace in (?,?,?,?)", new SingleBeanAttributeRowMapper<>(sess, this, member),
          service.getId(), member.getId(), AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_DEF,
          AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   " +
                        "facility_id=? " + "and user_id=? " + "where namespace in (?,?,?)",
          new UserFacilityAttributeRowMapper(sess, this, user, facility), service.getId(), facility.getId(),
          user.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT,
          AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) {
    //user and user core attributes
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=?" + " " +
                        "where namespace in (?,?,?,?)", new SingleBeanAttributeRowMapper<>(sess, this, user),
          service.getId(), user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF,
          AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) {
    return getRequiredAttributes(sess, Collections.singletonList(service), resource, group);
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource,
                                               Group group) {
    try {

      MapSqlParameterSource parameters = new MapSqlParameterSource();

      List<String> namespace = new ArrayList<>();
      namespace.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
      namespace.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
      namespace.add(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);

      parameters.addValue("serviceIds", services.stream().map(Service::getId).collect(Collectors.toList()));
      parameters.addValue("groupId", group.getId());
      parameters.addValue("resourceId", resource.getId());
      parameters.addValue("namespaces", namespace);

      return namedParameterJdbcTemplate.query(
          "select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
          "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id" +
          " in (:serviceIds) " +
          "left join    group_resource_attr_values     grp_res     on id=grp_res.attr_id     and   " +
          "group_id=:groupId and resource_id=:resourceId " + "where namespace in (:namespaces)", parameters,
          new GroupResourceAttributeRowMapper(sess, this, group, resource));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
                        "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs" +
                        ".service_id=? " +
                        "left join      host_attr_values    host   on      id=host.attr_id    and   " + "host_id=? " +
                        "where namespace in (?,?,?)", new SingleBeanAttributeRowMapper<>(sess, this, host),
          service.getId(), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF,
          AttributesManager.NS_HOST_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) {
    return getRequiredAttributes(sess, Collections.singletonList(service), group);
  }

  @Override
  public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Group group) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();

    List<String> namespace = new ArrayList<>();
    namespace.add(AttributesManager.NS_GROUP_ATTR_CORE);
    namespace.add(AttributesManager.NS_GROUP_ATTR_DEF);
    namespace.add(AttributesManager.NS_GROUP_ATTR_OPT);
    namespace.add(AttributesManager.NS_GROUP_ATTR_VIRT);

    parameters.addValue("serviceIds", services.stream().map(Service::getId).collect(Collectors.toList()));
    parameters.addValue("groupId", group.getId());
    parameters.addValue("namespaces", namespace);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
                                              "join service_required_attrs on id=service_required_attrs.attr_id and " +
                                              "service_required_attrs.service_id in " + "(:serviceIds) " +
                                              "left join      group_attr_values    grp   on      id=grp.attr_id    " +
                                              "and  group_id=:groupId " + "where namespace in (:namespaces)",
          parameters, new SingleBeanAttributeRowMapper<>(sess, this, group));
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) {
    try {
      return jdbc.query("SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY +
                        " FROM attr_names, service_required_attrs WHERE id=attr_id AND service_id=?",
          ATTRIBUTE_DEFINITION_MAPPER, service.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public Map<Group, List<Attribute>> getRequiredAttributesForGroups(PerunSession sess, Service service,
                                                                    List<Group> groups) {
    return jdbc.execute("SELECT " + getAttributeMappingSelectQuery("grp") + ", groups.id FROM attr_names " +
                        "JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND " +
                        "service_required_attrs" + ".service_id=? " + "JOIN groups ON groups.id " +
                        Compatibility.getStructureForInClause() +
                        "LEFT JOIN group_attr_values grp ON attr_names.id=grp.attr_id " +
                        "AND grp.group_id=groups.id WHERE namespace IN (?,?,?,?)",
        (PreparedStatementCallback<HashMap<Group, List<Attribute>>>) preparedStatement -> {
          Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbers(groups, preparedStatement);
          preparedStatement.setInt(1, service.getId());
          preparedStatement.setArray(2, sqlArray);
          preparedStatement.setString(3, AttributesManager.NS_GROUP_ATTR_CORE);
          preparedStatement.setString(4, AttributesManager.NS_GROUP_ATTR_DEF);
          preparedStatement.setString(5, AttributesManager.NS_GROUP_ATTR_OPT);
          preparedStatement.setString(6, AttributesManager.NS_GROUP_ATTR_VIRT);
          GroupAttributeExtractor groupAttributeExtractor = new GroupAttributeExtractor(sess, this, groups);
          return groupAttributeExtractor.extractData(preparedStatement.executeQuery());
        });
  }

  /**
   * Get resource attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance resource attribute module, null if the module doesn't exists
   */
  private ResourceAttributesModuleImplApi getResourceAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof ResourceAttributesModuleImplApi) {
      return (ResourceAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't ResourceAttributesModule. " + attribute);
    }
  }

  /**
   * Get group_resource attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance resource attribute module, null if the module doesn't exists
   */
  private GroupResourceAttributesModuleImplApi getResourceGroupAttributeModule(PerunSession sess,
                                                                               AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }
    if (attributeModule instanceof GroupResourceAttributesModuleImplApi) {
      return (GroupResourceAttributesModuleImplApi) attributeModule;

    } else {
      throw new InternalErrorException("Required attribute module isn't ResourceGroupAttributesModule");
    }
  }

  /**
   * Get group-resource attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance group-resource attribute module, null if the module doesn't exists
   */
  GroupResourceVirtualAttributesModuleImplApi getResourceGroupVirtualAttributeModule(PerunSession sess,
                                                                                     AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof GroupResourceVirtualAttributesModuleImplApi) {
      return (GroupResourceVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't ResourceGroupVirtualAttributesModule");
    }
  }

  /**
   * Get resource member attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance resource member attribute module null if the module doesn't exist
   */
  private MemberResourceAttributesModuleImplApi getResourceMemberAttributeModule(PerunSession sess,
                                                                                 AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof MemberResourceAttributesModuleImplApi) {
      return (MemberResourceAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't ResourceMemberAttributesModule");
    }
  }

  /**
   * Get member-resource attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance member-resource attribute module, null if the module doesn't exists
   */
  MemberResourceVirtualAttributesModuleImplApi getResourceMemberVirtualAttributeModule(PerunSession sess,
                                                                                       AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof MemberResourceVirtualAttributesModuleImplApi) {
      return (MemberResourceVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't ResourceMemberVirtualAttributesModule");
    }
  }

  @Override
  public List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource) {
    try {
      return jdbc.query("SELECT DISTINCT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY + " FROM resource_services " +
                        "JOIN service_required_attrs ON service_required_attrs.service_id=resource_services" +
                        ".service_id AND " + "resource_services.resource_id=? " +
                        "JOIN attr_names ON service_required_attrs.attr_id=attr_names.id", ATTRIBUTE_DEFINITION_MAPPER,
          resource.getId());

    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("None resource required attributes definitions found for resource: {}.", resource);
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Get resource virtual attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance of resource attribute module
   */
  private ResourceVirtualAttributesModuleImplApi getResourceVirtualAttributeModule(PerunSession sess,
                                                                                   AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof ResourceVirtualAttributesModuleImplApi) {
      return (ResourceVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't ResourceVirtualAttributesModule");
    }
  }

  @Override
  public AttributesModuleImplApi getUninitializedAttributesModule(PerunSession sess,
                                                                  AttributeDefinition attributeDefinition) {

    // core attributes doesn't have modules
    if (isCoreAttribute(sess, attributeDefinition)) {
      return null;
    }

    String moduleName =
        attributeNameToModuleName(attributeDefinition.getNamespace() + ":" + attributeDefinition.getFriendlyName());
    AttributesModuleImplApi module = uninitializedAttributesModulesMap.get(moduleName);

    if (module == null) {
      moduleName = attributeNameToModuleName(
          attributeDefinition.getNamespace() + ":" + attributeDefinition.getBaseFriendlyName());
      module = uninitializedAttributesModulesMap.get(moduleName);
    }

    return module;
  }

  @Override
  public String getUserAttrValueForUpdate(PerunSession sess, int attrId, int userId)
      throws AttributeNotExistsException {
    try {
      return jdbc.queryForObject("SELECT attr_value FROM user_attr_values WHERE user_id=? AND attr_id=? FOR UPDATE",
          String.class, userId, attrId);
    } catch (EmptyResultDataAccessException ex) {
      //If there is no such user attribute, create new one with null value and return null (insert is for transaction
      // same like select for update)
      Attribute attr = new Attribute(this.getAttributeDefinitionById(sess, attrId));
      this.setAttributeCreatedAndModified(sess, attr);
      self.setAttributeWithNullValue(sess, userId, attr);
      return null;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Get user attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance userattribute module, null if the module doesn't exists
   */
  private UserAttributesModuleImplApi getUserAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof UserAttributesModuleImplApi) {
      return (UserAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't UserAttributesModule. " + attribute);
    }
  }

  private ResultSetExtractor<List<RichMember>> getUserDefAttributesExtractor(PerunSession sess,
                                                                             Map<Integer, RichMember> userIdMemberMap) {
    return resultSet -> {
      while (resultSet.next()) {
        RichMember currentMember = userIdMemberMap.get(resultSet.getInt("user_id"));
        Attribute userAttribute =
            new SingleBeanAttributeRowMapper<>(sess, this, currentMember.getUser()).mapRow(resultSet,
                resultSet.getRow());
        currentMember.addUserAttribute(userAttribute);
      }
      return new ArrayList<>(userIdMemberMap.values());
    };
  }

  /**
   * Get user external source attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance user ext source attribute module, null if the module doesn't exists
   */
  private UserExtSourceAttributesModuleImplApi getUserExtSourceAttributeModule(PerunSession sess,
                                                                               AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof UserExtSourceAttributesModuleImplApi) {
      return (UserExtSourceAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException("Required attribute module isn't UserExtSourceAttributesModule. " + attribute);
    }
  }

  /**
   * Get UserExtSource attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance UserExtSource attribute module, null if the module doesn't exist
   */
  private UserExtSourceVirtualAttributesModuleImplApi getUserExtSourceVirtualAttributeModule(PerunSession sess,
                                                                                       AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);

    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof UserExtSourceVirtualAttributesModuleImplApi) {
      return (UserExtSourceVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new InternalErrorException(
          "Required attribute module isn't UserExtSourceVirtualAttributesModule. " + attribute);
    }
  }

  @Override
  public List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) {
    try {
      return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
                        "left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   " +
                        "facility_id=? " + "where namespace in (?,?) and usr_fac.attr_value is not null",
          new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(),
          AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No attribute for user-facility combination exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<Integer> getUserIdsByLogin(PerunSession sess, String login) {
    try {
      return jdbc.queryForList(
          "select distinct attr_val.user_id from attr_names as attr join user_attr_values attr_val on attr" +
          ".id=attr_val.attr_id\n" + "where attr.friendly_name like 'login-namespace:%%' \n" +
          "    and attr.friendly_name not like '%%persistent%%' \n" + "    and attr_val.attr_value like ?",
          Integer.class, login);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess,
                                                                          AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
    }

    if (attributeModule instanceof UserVirtualAttributesModuleImplApi) {
      return (UserVirtualAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't UserVirtualAttributesModule");
    }
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, facility),
        AttributesManager.NS_FACILITY_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Member member) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, member),
        AttributesManager.NS_MEMBER_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Vo vo) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, vo), AttributesManager.NS_VO_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Group group) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, group),
        AttributesManager.NS_GROUP_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Host host) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, host),
        AttributesManager.NS_HOST_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, resource),
        AttributesManager.NS_RESOURCE_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Resource resource) {
    return getVirtualAttributes(new MemberResourceAttributeRowMapper(sess, this, member, resource),
        AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user) {
    return getVirtualAttributes(new UserFacilityAttributeRowMapper(sess, this, user, facility),
        AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Group group) {
    return getVirtualAttributes(new MemberGroupAttributeRowMapper(sess, this, member, group),
        AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, User user) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, user),
        AttributesManager.NS_USER_ATTR_VIRT);
  }

  @Override
  public List<Attribute> getVirtualAttributes(PerunSession sess, UserExtSource ues) {
    return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, ues),
        AttributesManager.NS_UES_ATTR_VIRT);
  }

  private List<Attribute> getVirtualAttributes(RowMapper<Attribute> rowMapper, String namespace) {
    try {
      return jdbc.query(
          "SELECT " + ATTRIBUTE_DEFINITION_MAPPING_SELECT_QUERY +
              ", NULL AS attr_value FROM attr_names WHERE namespace=?",
          rowMapper, namespace);
    } catch (EmptyResultDataAccessException ex) {
      LOG.debug("No virtual attribute for " + (namespace.split(":")[2]) + " exists.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Get Vo attribute module for the attribute.
   *
   * @param attribute attribute for which you get the module
   * @return instance vo attribute module null if the module doesn't exist
   */
  private VoAttributesModuleImplApi getVoAttributeModule(PerunSession sess, AttributeDefinition attribute) {
    Object attributeModule = getAttributesModule(sess, attribute);
    if (attributeModule == null) {
      return null;
    }

    if (attributeModule instanceof VoAttributesModuleImplApi) {
      return (VoAttributesModuleImplApi) attributeModule;
    } else {
      throw new WrongModuleTypeException("Required attribute module isn't VoAttributesModule");
    }
  }

  @Override
  public void initAndRegisterAttributeModules(PerunSession sess, ServiceLoader<AttributesModuleImplApi> modules,
                                              Set<AttributeDefinition> allAttributesDef) {
    for (AttributesModuleImplApi module : modules) {
      uninitializedAttributesModulesMap.put(module.getClass().getName(), module);
    }

    for (AttributeDefinition attributeDefinition : allAttributesDef) {
      AttributesModuleImplApi module = getUninitializedAttributesModule(sess, attributeDefinition);

      if (module != null) {
        initAttributeModule(module);
        registerAttributeModule(module);
      }
    }
  }

  @Override
  public void initAttributeModule(AttributesModuleImplApi module) {
    attributesModulesMap.putIfAbsent(module.getClass().getName(), module);
    LOG.debug("Module {} loaded.", module.getClass().getSimpleName());
    uninitializedAttributesModulesMap.remove(module.getClass().getName());
  }

  @Override
  public boolean insertAttribute(PerunSession sess, Attribute attribute, String tableName, List<String> columnNames,
                                 List<Object> columnValues) {
    // add additional SQL values to the list
    List<Object> values = new ArrayList<>(columnValues);
    values.add(BeansUtils.attributeValueToString(attribute)); // valueColName
    values.add(sess.getPerunPrincipal().getActor()); // created_by
    values.add(sess.getPerunPrincipal().getActor()); // modified_by
    values.add(sess.getPerunPrincipal().getUserId()); // created_by_uid
    values.add(sess.getPerunPrincipal().getUserId()); // modified_by_uid
    // prepare correct number of question marks
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      sb.append("?,");
    }
    String questionMarks = sb.toString();

    int changed = jdbc.update("insert into " + tableName + " (" + buildParameters(columnNames, "", ", ") +
                              ", attr_value, created_by, modified_by, created_by_uid, modified_by_uid, modified_at, " +
                              "created_at) " + "values (" + questionMarks + Compatibility.getSysdate() + ", " +
                              Compatibility.getSysdate() + " )", values.toArray());
    return changed > 0;
  }

  @Override
  public boolean isAttributeActionCritical(PerunSession sess, AttributeDefinition attr, AttributeAction action) {
    try {
      return 0 < jdbc.queryForInt(
          "select count(*) from attribute_critical_actions where attr_id=? and action=?::attribute_action",
          attr.getId(), action.toString());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isAttributeActionGloballyCritical(PerunSession sess, int attrId, AttributeAction action) {
    try {
      return 0 < jdbc.queryForInt(
          "select count(*) from attribute_critical_actions where attr_id=? and action=?::attribute_action and " +
          "global=true", attrId, action.toString());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Pair<Boolean, String> isAttributeValueBlocked(PerunSession session, Attribute attribute) {
    String namespace = attribute.getFriendlyNameParameter();
    String namespaceExceptionString = namespace.isBlank() ? "" : " for namespace " + namespace;
    try {
      switch (attribute.getType()) {
        case "java.lang.Integer":
          if (0 < jdbc.queryForInt(
                "select count(*) from blocked_attr_values where attr_id=? and attr_value=?",
                        attribute.getId(), attribute.valueAsInteger().toString())) {
            return new Pair<>(true, "The attribute value '" + attribute.valueAsInteger() + "' is blocked" +
                                                namespaceExceptionString);
          }
          break;
        case "java.lang.String":
          if (0 < jdbc.queryForInt(
                "select count(*) from blocked_attr_values where attr_id=? and attr_value=?",
                        attribute.getId(), attribute.valueAsString())) {
            return new Pair<>(true, "The attribute value '" + attribute.valueAsString() + "' is blocked" +
                                                namespaceExceptionString);
          }
          break;
        case "java.util.ArrayList":
          for (String s : attribute.valueAsList()) {
            if (0 < jdbc.queryForInt(
                "select count(*) from blocked_attr_values where attr_id=? and attr_value=?",
                        attribute.getId(), s)) {
              return new Pair<>(true, "One of the values from the list is blocked: " + s +
                                                   namespaceExceptionString);
            }
          }
          break;
        case "java.util.LinkedHashMap":
          for (Map.Entry<String, String> entry : attribute.valueAsMap().entrySet()) {
            if (0 < jdbc.queryForInt(
                "select count(*) from blocked_attr_values where attr_id=? and attr_value=?",
                  attribute.getId(), entry.getKey() + "=" + entry.getValue())) {
              return new Pair<>(true, "One of the key value pairs is blocked: " + entry.getKey() + "=" +
                                                   entry.getValue() + namespaceExceptionString);
            }
          }
          break;
        default:
          break;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return new Pair<>(false, "");
  }

  @Override
  public boolean isAttributeRequiredByFacility(PerunSession sess, Facility facility,
                                               AttributeDefinition attributeDefinition) {
    try {
      return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
                                  "join resource_services on service_required_attrs.service_id=resource_services" +
                                  ".service_id " + "join resources on resource_services.resource_id=resources.id " +
                                  "where service_required_attrs.attr_id=? and resources.facility_id=?",
          attributeDefinition.getId(), facility.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isAttributeRequiredByGroup(PerunSession sess, Group group, AttributeDefinition attributeDefinition) {
    try {
      return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
                                  "join resource_services on service_required_attrs.service_id=resource_services" +
                                  ".service_id " + "join groups_resources_state on resource_services" +
                                  ".resource_id=groups_resources_state.resource_id " +
                                  "where service_required_attrs.attr_id=? and groups_resources_state.group_id=?",
          attributeDefinition.getId(), group.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isAttributeRequiredByResource(PerunSession sess, Resource resource,
                                               AttributeDefinition attributeDefinition) {
    try {
      return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
                                  "join resource_services on service_required_attrs.service_id=resource_services" +
                                  ".service_id and " + "resource_services.resource_id=? " +
                                  "where service_required_attrs.attr_id=?", resource.getId(),
          attributeDefinition.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isAttributeRequiredByVo(PerunSession sess, Vo vo, AttributeDefinition attributeDefinition) {
    try {
      return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
                                  "join resource_services on service_required_attrs.service_id=resource_services" +
                                  ".service_id " + "join resources on resource_services.resource_id=resources.id " +
                                  "where service_required_attrs.attr_id=? and resources.vo_id=?",
          attributeDefinition.getId(), vo.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().endsWith(":core");
  }

  @Override
  public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().matches("urn:perun:[^:]+:attribute-def:core-managed:[a-zA-Z]+Manager");
  }

  @Override
  public boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().endsWith(":def");
  }

  @Override
  public boolean isFromNamespace(AttributeDefinition attribute, String namespace) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().startsWith(namespace + ":") || attribute.getNamespace().equals(namespace);
  }

  @Override
  public boolean isLoginAlreadyUsed(PerunSession sess, String login, String namespace) {
    try {
      String namespaceValue = (namespace == null) ? "%" : namespace;
      return jdbc.queryForInt(String.format(
          "select count(*) from attr_names as attr join user_attr_values attr_val on attr.id=attr_val.attr_id\n" +
          "where attr.friendly_name like 'login-namespace:%s' \n" +
          "    and attr.friendly_name not like '%%persistent%%' \n" + "    and attr_val.attr_value like ?",
          namespaceValue), login) > 0;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().endsWith(":opt");
  }

  @Override
  public boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute) {
    if (attribute == null) {
      throw new InternalErrorException(new NullPointerException("Attribute attribute is null"));
    }
    if (attribute.getNamespace() == null) {
      throw new InternalErrorException(new NullPointerException("String attribute.namespace is null"));
    }
    return attribute.getNamespace().endsWith(":virt");
  }

  @Override
  public void registerAttributeModule(AttributesModuleImplApi module) {
    Auditer.registerAttributeModule(module);
    LOG.debug("Module {} was registered for audit message listening.", module.getClass().getName());
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Facility facility) {
    try {
      if (0 < jdbc.update("DELETE FROM facility_attr_values WHERE facility_id=?", facility.getId())) {
        LOG.debug("All attributes values were removed from facility {}.", facility);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Vo vo) {
    try {
      if (0 < jdbc.update("DELETE FROM vo_attr_values WHERE vo_id=?", vo.getId())) {
        LOG.debug("All attributes values were removed from vo {}.", vo);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Group group) {
    try {
      if (0 < jdbc.update("DELETE FROM group_attr_values WHERE group_id=?", group.getId())) {
        LOG.debug("All attributes values were removed from group {}.", group);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Resource resource) {
    try {
      if (0 < jdbc.update("DELETE FROM resource_attr_values WHERE resource_id=?", resource.getId())) {
        LOG.debug("All attributes values were removed from resource {}.", resource);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Member member, Resource resource) {
    try {
      if (0 <
          jdbc.update("DELETE FROM member_resource_attr_values WHERE resource_id=? AND member_id=?", resource.getId(),
              member.getId())) {
        LOG.debug("All attributes values were removed from member {} on resource {}.", member, resource);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Member member, Group group) {
    try {
      if (0 < jdbc.update("DELETE FROM member_group_attr_values WHERE group_id=? AND member_id=?", group.getId(),
          member.getId())) {
        LOG.debug("All attributes values were removed from member {} in group {}.", member, group);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Member member) {
    try {
      if (0 < jdbc.update("DELETE FROM member_attr_values WHERE member_id=?", member.getId())) {
        LOG.debug("All attributes values were removed from member {}", member);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Facility facility, User user) {
    try {
      if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE user_id=? AND facility_id=?", user.getId(),
          facility.getId())) {
        LOG.debug("All attributes values were removed from user {} on facility {}.", user, facility);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, User user) {
    try {
      if (0 < jdbc.update("DELETE FROM user_attr_values WHERE user_id=?", user.getId())) {
        LOG.debug("All attributes values were removed from user {}.", user);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Host host) {
    try {
      if (0 < jdbc.update("DELETE FROM host_attr_values WHERE host_id=?", host.getId())) {
        LOG.debug("All attributes values were removed from host {}.", host);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, Resource resource, Group group) {
    try {
      if (0 < jdbc.update("DELETE FROM group_resource_attr_values WHERE group_id=? AND resource_id=?", group.getId(),
          resource.getId())) {
        LOG.debug("All attributes values were removed from group {} on resource{}.", group, resource);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllAttributes(PerunSession sess, UserExtSource ues) {
    try {
      if (0 < jdbc.update("DELETE FROM user_ext_source_attr_values WHERE user_ext_source_id=?", ues.getId())) {
        LOG.debug("All attributes values were removed from user external source {}.", ues);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) {
    try {
      jdbc.update("DELETE FROM group_resource_attr_values WHERE resource_id=?", resource.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) {
    try {
      jdbc.update("DELETE FROM member_resource_attr_values WHERE resource_id=?", resource.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAllUserFacilityAttributes(PerunSession sess, User user) {
    try {
      if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE user_id=?", user.getId())) {
        LOG.debug("All attributes values were removed from user {} on  all facilities.", user);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public boolean removeAllUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) {
    try {
      if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE facility_id=?", facility.getId())) {
        LOG.debug("All attributes values were removed from any user on facility {}.", facility);
        return true;
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    return false;
  }

  @Override
  public void removeAndUnregisterAttrModule(PerunSession sess, AttributeDefinition attribute) {

    // core attributes doesn't have modules !!
    if (isCoreAttribute(sess, attribute)) {
      return;
    }

    String moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getFriendlyName());
    AttributesModuleImplApi attributeModule = attributesModulesMap.get(moduleName);
    if (attributeModule != null) {
      removeAttributeModule(attributeModule);
      unregisterAttributeModule(attributeModule);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM facility_attr_values WHERE attr_id=? AND facility_id=?", attribute.getId(),
          facility.getId())) {
        LOG.debug("Attribute value for {} was removed from facility {}.", attribute.getName(), facility);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM entityless_attr_values WHERE attr_id=? AND subject=?", attribute.getId(), key)) {
        LOG.debug("Attribute value for {} with key {} was removed from entityless attributes.", attribute.getName(),
            key);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM vo_attr_values WHERE attr_id=? AND vo_id=?", attribute.getId(), vo.getId())) {
        LOG.debug("Attribute value for {} was removed from vo {}.", attribute.getName(), vo);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM group_attr_values WHERE attr_id=? AND group_id=?", attribute.getId(),
          group.getId())) {
        LOG.debug("Attribute value for {} was removed from group {}.", attribute.getName(), group);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM resource_attr_values WHERE attr_id=? AND resource_id=?", attribute.getId(),
          resource.getId())) {
        LOG.debug("Attribute value for {} was removed from resource {}.", attribute.getName(), resource);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Member member, Resource resource, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM member_resource_attr_values WHERE attr_id=? AND member_id=? AND resource_id=?",
          attribute.getId(), member.getId(), resource.getId())) {
        LOG.debug("Attribute value for {} was removed from member {} on resource {}.", attribute.getName(), member,
            resource);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM member_group_attr_values WHERE attr_id=? AND member_id=? AND group_id=?",
          attribute.getId(), member.getId(), group.getId())) {
        LOG.debug("Attribute value {} was removed from member {} in group {}.", attribute, member, group);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM member_attr_values WHERE attr_id=? AND member_id=?", attribute.getId(),
          member.getId())) {
        LOG.debug("Attribute value {} was removed from member {}", attribute, member);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE attr_id=? AND user_id=? AND facility_id=?",
          attribute.getId(), user.getId(), facility.getId())) {
        LOG.debug("Attribute value {} was removed from user {} on facility {}.", attribute, user, facility);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public boolean removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) {
    try {
      if (0 <
          jdbc.update("DELETE FROM user_attr_values WHERE attr_id=? AND user_id=?", attribute.getId(), user.getId())) {
        LOG.debug("Attribute value for {} was removed from user {}.", attribute.getName(), user);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) {
    try {
      if (0 <
          jdbc.update("DELETE FROM host_attr_values WHERE attr_id=? AND host_id=?", attribute.getId(), host.getId())) {
        LOG.debug("Attribute value for {} was removed from host {}.", attribute.getName(), host);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM group_resource_attr_values WHERE attr_id=? AND resource_id=? AND group_id=?",
          attribute.getId(), resource.getId(), group.getId())) {
        LOG.debug("Attribute value for {} was removed from group {} on resource {}.", attribute.getName(), group,
            resource);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) {
    try {
      if (0 < jdbc.update("DELETE FROM user_ext_source_attr_values WHERE attr_id=? AND user_ext_source_id=?",
          attribute.getId(), ues.getId())) {
        LOG.debug("Attribute value for {} was removed from user external source {}.", attribute.getName(), ues);
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void removeAttributeModule(AttributesModuleImplApi module) {
    uninitializedAttributesModulesMap.putIfAbsent(module.getClass().getName(), module);
    LOG.debug("Module {} removed.", module.getClass().getSimpleName());
    attributesModulesMap.remove(module.getClass().getName());
  }

  @Override
  public boolean removeVirtualAttribute(PerunSession sess, Facility facility, User user,
                                        AttributeDefinition attribute) {
    return getFacilityUserVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, user,
        facility, attribute);
  }

  @Override
  public boolean removeVirtualAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException {
    return getResourceVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource,
        attribute);
  }

  @Override
  public boolean removeVirtualAttribute(PerunSession sess, Resource resource, Group group,
                                        AttributeDefinition attribute) {
    return getResourceGroupVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, group,
        resource, attribute);
  }

  @Override
  public boolean setAttribute(final PerunSession sess, final Object object, final Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException {
    String tableName;
    String columnName;
    Object identificator;
    String namespace;

    // check whether the object is String or Perun Bean:
    if (object instanceof String) {
      tableName = "entityless_attr_values";
      columnName = "subject";
      identificator = object;
      namespace = AttributesManager.NS_ENTITYLESS_ATTR;
    } else if (object instanceof PerunBean) {
      PerunBean bean = (PerunBean) object;
      // Add underscore between two letters where first is lowercase and second is uppercase, then lowercase BeanName
      String name = bean.getBeanName().replaceAll("(\\p{Ll})(\\p{Lu})", "$1_$2").toLowerCase();
      // same behaviour for rich objects as for the simple ones -> cut off "rich_" prefix
      if (name.startsWith("rich")) {
        name = name.replaceFirst("rich_", "");
      }
      // get namespace of the perun bean
      namespace = BEANS_TO_NAMESPACES_MAP.get(name);
      if (namespace == null) {
        // perun bean is not in the namespace map
        throw new InternalErrorException(
            new IllegalArgumentException("Setting attribute for perun bean " + bean + " is not allowed."));
      }
      tableName = name + "_attr_values";
      columnName = name + "_id";
      identificator = bean.getId();
    } else {
      throw new InternalErrorException(
          new IllegalArgumentException("Object " + object + " must be either String or PerunBean."));
    }

    // check that given object is consistent with the attribute
    checkNamespace(sess, attribute, namespace);

    // create lists of parameters for the where clause of the SQL query
    List<String> columnNames = Arrays.asList("attr_id", columnName);
    List<Object> columnValues = Arrays.asList(attribute.getId(), identificator);

    // save attribute
    boolean changedDb;
    if (object instanceof String) {
      changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues);
    } else {
      changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues);
    }

    if (changedDb && attribute.isUnique() && (object instanceof PerunBean)) {
      setUniqueAttributeValues(attribute, columnNames, columnValues, (PerunBean) object, null);
    }
    return changedDb;
  }

  @Override
  public boolean setAttribute(final PerunSession sess, final PerunBean bean1, final PerunBean bean2,
                              final Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException {

    // get bean names
    String name1 = bean1.getBeanName().toLowerCase();
    String name2 = bean2.getBeanName().toLowerCase();
    // same behaviour for rich objects as for the simple ones -> cut off "rich" prefix
    if (name1.startsWith("rich")) {
      name1 = name1.replaceFirst("rich", "");
    }
    if (name2.startsWith("rich")) {
      name2 = name2.replaceFirst("rich", "");
    }
    // get namespace of the perun bean
    String namespace = BEANS_TO_NAMESPACES_MAP.get(name1 + "_" + name2);
    int identificator1 = bean1.getId();
    int identificator2 = bean2.getId();
    if (namespace == null) {
      // swap the names and beans/ids and try again
      String nameTmp = name1;
      name1 = name2;
      name2 = nameTmp;
      identificator1 = bean2.getId();
      identificator2 = bean1.getId();
      namespace = BEANS_TO_NAMESPACES_MAP.get(name1 + "_" + name2);
    }
    if (namespace == null) {
      // the combination of perun beans is not in the namespace map
      throw new InternalErrorException(new IllegalArgumentException(
          "Setting attribute for perun bean " + bean1 + " and " + bean2 + " is not allowed."));
    }
    String tableName = name1 + "_" + name2 + "_attr_values";

    // check that given object is consistent with the attribute
    checkNamespace(sess, attribute, namespace);

    // create lists of parameters for the where clause of the SQL query
    List<String> columnNames = Arrays.asList("attr_id", name1 + "_id", name2 + "_id");
    List<Object> columnValues = Arrays.asList(attribute.getId(), identificator1, identificator2);

    // save attribute
    boolean changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues);
    if (changedDb && attribute.isUnique()) {
      setUniqueAttributeValues(attribute, columnNames, columnValues, bean1, bean2);
    }
    return changedDb;
  }

  @Override
  public void setAttributeActionCriticality(PerunSession sess, AttributeDefinition attr, AttributeAction action,
                                            boolean critical, boolean global)
      throws RelationExistsException, RelationNotExistsException {
    try {
      if (critical) {
        boolean globalCriticalityChanged = isAttributeActionGloballyCritical(sess, attr.getId(), action) != global;

        if (isAttributeActionCritical(sess, attr, action) && !globalCriticalityChanged) {
          throw new RelationExistsException(
              "Attribute " + attr.getName() + " is already critical on " + action + " action.");
        }

        jdbc.update(
            "insert into attribute_critical_actions (attr_id, action, global) values (?,?::attribute_action,?) " +
            "on conflict(attr_id, action) do update set global=?", attr.getId(), action.toString(), global, global);
      } else {
        if (0 == jdbc.update("delete from attribute_critical_actions where attr_id=? and action=?::attribute_action",
            attr.getId(), action.toString())) {
          throw new RelationNotExistsException(
              "Attribute " + attr.getName() + " is not critical on " + action + " action.");
        }
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  private Attribute setAttributeCreatedAndModified(PerunSession sess, Attribute attribute) {
    attribute.setValueCreatedBy(sess.getPerunPrincipal().getActor());
    attribute.setValueModifiedBy(sess.getPerunPrincipal().getActor());
    Timestamp time = new Timestamp(System.currentTimeMillis());
    attribute.setValueCreatedAt(time.toString());
    attribute.setValueModifiedAt(time.toString());
    return attribute;
  }

  private AttributeDefinition setAttributeDefinitionCreatedAndModified(PerunSession sess,
                                                                       AttributeDefinition attribute) {
    attribute.setCreatedBy(sess.getPerunPrincipal().getActor());
    attribute.setModifiedBy(sess.getPerunPrincipal().getActor());
    attribute.setCreatedByUid(sess.getPerunPrincipal().getUserId());
    attribute.setModifiedByUid(sess.getPerunPrincipal().getUserId());
    Timestamp time = new Timestamp(System.currentTimeMillis());
    attribute.setCreatedAt(time.toString());
    attribute.setModifiedAt(time.toString());
    return attribute;
  }

  private AttributeDefinition setAttributeDefinitionModified(PerunSession sess, AttributeDefinition attribute) {
    attribute.setModifiedBy(sess.getPerunPrincipal().getActor());
    attribute.setModifiedByUid(sess.getPerunPrincipal().getUserId());
    Timestamp time = new Timestamp(System.currentTimeMillis());
    attribute.setModifiedAt(time.toString());
    return attribute;
  }

  private boolean setAttributeInDB(final PerunSession sess, final Attribute attribute, final String tableName,
                                   List<String> columnNames, List<Object> columnValues) {
    try {
      //check that attribute definition is current, non-altered by upper tiers
      getAttributeDefinitionById(sess, attribute.getId()).checkEquality(attribute);
    } catch (AttributeNotExistsException e) {
      throw new InternalErrorException("cannot verify attribute definition", e);
    }
    try {
      // deleting the attribute if the given attribute value is null
      if (attribute.getValue() == null) {
        int numAffected =
            jdbc.update("delete from " + tableName + " where " + buildParameters(columnNames, "=?", " and "),
                columnValues.toArray());
        if (numAffected > 1) {
          throw new ConsistencyErrorException(String.format(
              "Too much rows to delete (" + numAffected + " rows). SQL: delete from " + tableName + " where " +
              buildParameters(columnNames, "=%s", " and "), columnValues.toArray()));
        }
        return numAffected == 1;
      }

      // if the DB value is the same as parameter, return
      try {
        Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject(
            "select attr_value from " + tableName + " where " + buildParameters(columnNames, "=?", " and "),
            String.class, columnValues.toArray()), attribute.getType());
        if (attribute.getValue().equals(value)) {
          return false;
        }
      } catch (EmptyResultDataAccessException ex) {
        //This is ok. Attribute will be stored later.
      }

      int repetatCounter = 0;
      while (true) {
        // number of values of this attribute value in db
        int numOfAttributesInDb = jdbc.queryForInt(
            "select count(attr_id) from " + tableName + " where " + buildParameters(columnNames, "=?", " and "),
            columnValues.toArray());
        switch (numOfAttributesInDb) {
          case 0: {
            // value doesn't exist -> insert
            try {
              return self.insertAttribute(sess, attribute, tableName, columnNames, columnValues);
            } catch (DataAccessException ex) {
              // unsuccessful insert, do it again in while loop
              if (++repetatCounter > MERGE_TRY_CNT) {
                throw new InternalErrorException(
                    "SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
              }
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch (InterruptedException ignored) {
                // ignored
              }
            }
            break;
          }
          case 1: {
            // value exists -> update
            try {
              return self.updateAttribute(sess, attribute, tableName, columnNames, columnValues);
            } catch (DataAccessException ex) {
              // unsuccessful insert, do it again in while loop
              if (++repetatCounter > MERGE_TRY_CNT) {
                throw new InternalErrorException(
                    "SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
              }
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch (InterruptedException ignored) {
                // ignored
              }
            }
            break;
          }
          default:
            throw new ConsistencyErrorException(String.format(
                "Attribute id " + attribute.getId() + " for " + tableName + " with parameters: " +
                buildParameters(columnNames, "=%s", " and ") + " is more than once in DB.", columnValues.toArray()));
        }
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  private Attribute setAttributeModified(PerunSession sess, Attribute attribute) {
    attribute.setValueModifiedBy(sess.getPerunPrincipal().getActor());
    Timestamp time = new Timestamp(System.currentTimeMillis());
    attribute.setValueModifiedAt(time.toString());
    return attribute;
  }

  @Override
  public void setAttributePolicyCollections(PerunSession sess, List<AttributePolicyCollection> policyCollections) {
    try {
      // deleting old attribute policies
      List<Integer> attributeIds = policyCollections.stream().map(AttributePolicyCollection::getAttributeId).distinct()
          .collect(Collectors.toList());

      for (Integer attributeId : attributeIds) {
        jdbc.update("DELETE FROM attribute_policy_collections WHERE attr_id=?", attributeId);
      }

      // inserting new policies
      for (AttributePolicyCollection apc : policyCollections) {
        if (apc.getPolicies().isEmpty()) {
          continue;
        }

        int nextId = jdbc.queryForInt("SELECT nextval('attribute_policy_collections_id_seq')");

        jdbc.update(
            "INSERT INTO attribute_policy_collections (id, attr_id, action) VALUES " + "(?, ?, ?::attribute_action)",
            nextId, apc.getAttributeId(), apc.getAction().toString());

        for (AttributePolicy ap : apc.getPolicies()) {
          jdbc.update("INSERT INTO attribute_policies (id, role_id, object, policy_collection_id) VALUES " +
                      "((nextval('attribute_policies_id_seq')), (SELECT id FROM roles WHERE name=?), ?::role_object, " +
                      "?)", ap.getRole().toLowerCase(), ap.getObject().toString(), nextId);
        }
      }

    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean setAttributeWithNullValue(final PerunSession sess, final String key, final Attribute attribute) {
    try {
      jdbc.update(
          "insert into entityless_attr_values (attr_id, subject, attr_value, created_by, modified_by, created_at, " +
          "modified_at, created_by_uid, modified_by_uid) " + "values (?,?,?,?,?," + Compatibility.getSysdate() + "," +
          Compatibility.getSysdate() + ",?,?)", attribute.getId(), key, null, sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
          sess.getPerunPrincipal().getUserId());
      return true;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean setAttributeWithNullValue(final PerunSession sess, final int userId, final Attribute attribute) {
    try {
      jdbc.update("insert into user_attr_values (attr_id, user_id, attr_value, created_by, modified_by, created_at, " +
                  "modified_at, created_by_uid, modified_by_uid) " + "values (?,?,?,?,?," + Compatibility.getSysdate() +
                  "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), userId, null,
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      return true;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public void setPerun(Perun perun) {
    this.perun = perun;
  }

  public void setSelf(AttributesManagerImplApi self) {
    this.self = self;
  }

  @SuppressWarnings("unchecked")
  private void setUniqueAttributeValues(Attribute attribute, List<String> columnNames, List<Object> columnValues,
                                        PerunBean pb1, PerunBean pb2) throws WrongAttributeValueException {
    String tableName = attributeToTablePrefix(attribute) + "_attr_u_values";
    jdbc.update("delete from " + tableName + " where " + buildParameters(columnNames, "=?", " and "),
        columnValues.toArray());
    if (attribute.getValue() == null) {
      return;
    }
    // prepare correct number of question marks
    StringBuilder questionMarks = new StringBuilder();
    for (int i = 0; i < columnValues.size(); i++) {
      questionMarks.append(",?");
    }
    //prepare list of column values for adding attribute value
    Object[] sqlArgs = new Object[columnValues.size() + 1];
    System.arraycopy(columnValues.toArray(), 0, sqlArgs, 1, columnValues.size());
    String sql = "INSERT INTO " + tableName + " (attr_value," + buildParameters(columnNames, "", ", ") + ") VALUES (?" +
                 questionMarks + ")";
    switch (attribute.getType()) {
      case "java.util.ArrayList":
        for (String value : (List<String>) attribute.getValue()) {
          sqlArgs[0] = value;
          tryToInsertUniqueValue(sql, sqlArgs, attribute, pb1, pb2);
        }
        break;
      case "java.util.LinkedHashMap":
        for (Map.Entry<String, String> entry : ((Map<String, String>) attribute.getValue()).entrySet()) {
          sqlArgs[0] = entry.getKey() + "=" + entry.getValue();
          tryToInsertUniqueValue(sql, sqlArgs, attribute, pb1, pb2);
        }
        break;
      default:
        sqlArgs[0] = attribute.getValue().toString();
        tryToInsertUniqueValue(sql, sqlArgs, attribute, pb1, pb2);
    }

  }

  /**
   * Sets value for core attribute
   *
   * @param attribute       attribute to set value for
   * @param attributeHolder primary attribute holder (Facility, Resource, Member...) for which you want the attribute
   *                        value
   * @return attribute with set value
   */
  private Attribute setValueForCoreAttribute(Attribute attribute, Object attributeHolder) {
    String methodName =
        "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
    Method method;
    try {
      method = attributeHolder.getClass().getMethod(methodName);
    } catch (NoSuchMethodException ex) {
      //if not "get", try "is"
      String methodGet = methodName;
      try {
        methodName = "is" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) +
                     attribute.getFriendlyName().substring(1);
        method = attributeHolder.getClass().getMethod(methodName);
      } catch (NoSuchMethodException e) {
        throw new InternalErrorException(
            "There is no method '" + methodGet + "' or '" + methodName + "'  for core attribute definition. " +
            attribute, e);
      }
    }
    try {
      Object value = method.invoke(attributeHolder);

      //
      // Try to automatically convert object returned from bean method call to required data type
      //
      //noinspection StatementWithEmptyBody
      if (value == null) {
        // No need to convert NULL value (for String it caused NULL->"null" conversion)
      } else if ((attribute.getType().equals(String.class.getName())) && !(value instanceof String)) {
        //TODO check exceptions
        value = String.valueOf(value);
      } else //noinspection StatementWithEmptyBody
        if (attribute.getType().equals(Integer.class.getName()) && !(value instanceof Integer)) {
          //TODO try to cast to integer
        } else //noinspection StatementWithEmptyBody
          if (attribute.getType().equals(Boolean.class.getName()) && !(value instanceof Boolean)) {
            //TODO try to cast to boolean
          } else if (attribute.getType().equals(ArrayList.class.getName()) && !(value instanceof ArrayList)) {
            if (value instanceof List) {
              //noinspection unchecked
              value = new ArrayList<String>((List) value);
            } else {
              throw new InternalErrorException(
                  "Cannot convert result of method " + attributeHolder.getClass().getName() + "." + methodName +
                  " to ArrayList.");
            }
          } else if (attribute.getType().equals(LinkedHashMap.class.getName()) && !(value instanceof LinkedHashMap)) {
            if (value instanceof Map) {
              //noinspection unchecked
              value = new LinkedHashMap<String, String>((Map) value);
            } else {
              throw new InternalErrorException(
                  "Cannot convert result of method " + attributeHolder.getClass().getName() + "." + methodName +
                  " to LinkedHashMap.");
            }
          }

      Auditable auditableHolder = (Auditable) attributeHolder;
      attribute.setCreatedAt(auditableHolder.getCreatedAt());
      attribute.setCreatedBy(auditableHolder.getCreatedBy());
      attribute.setModifiedAt(auditableHolder.getModifiedAt());
      attribute.setModifiedBy(auditableHolder.getModifiedBy());
      attribute.setModifiedByUid(auditableHolder.getModifiedByUid());
      attribute.setCreatedByUid(auditableHolder.getCreatedByUid());
      attribute.setValue(value);
      return attribute;
    } catch (IllegalAccessException ex) {
      throw new InternalErrorException(ex);
    } catch (InvocationTargetException ex) {
      throw new InternalErrorException("An exception raise while getting core attribute value.", ex);
    }
  }

  /**
   * Sets value for virtual attribute
   *
   * @param sess                  perun session
   * @param attributesManagerImpl
   * @param attribute             attribute to set value for
   * @param attributeHolder       primary attribute holder (Facility, Resource, Member...) for which you want the
   *                              attribute value
   * @param attributeHolder2      secondary attribute holder (Facility, Resource, Member...) for which you want the
   *                              attribute value
   * @return attribute with set value
   */
  private Attribute setValueForVirtualAttribute(PerunSession sess, AttributesManagerImpl attributesManagerImpl,
                                                Attribute attribute, Object attributeHolder, Object attributeHolder2) {
    if (attributeHolder == null) {
      throw new InternalErrorException("Bad usage of attributeRowMapper");
    }

    if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_USER_FACILITY_ATTR_VIRT)) {
      if (!(attributeHolder instanceof User)) {
        throw new ConsistencyErrorException("First attribute holder of user_facility attribute isn't user");
      }
      if (attributeHolder2 == null || !(attributeHolder2 instanceof Facility)) {
        throw new ConsistencyErrorException("Second attribute holder of user_facility attribute isn't facility");
      }

      try {
        UserFacilityVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getFacilityUserVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (User) attributeHolder,
            (Facility) attributeHolder2, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_FACILITY_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Facility)) {
        throw new ConsistencyErrorException("Attribute holder of facility attribute isn't facility");
      }

      try {
        FacilityVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getFacilityVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Facility) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_RESOURCE_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Resource)) {
        throw new ConsistencyErrorException("Attribute holder of resource attribute isn't resource");
      }

      try {
        ResourceVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getResourceVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_USER_ATTR_VIRT)) {
      if (!(attributeHolder instanceof User)) {
        throw new ConsistencyErrorException("Attribute holder of user attribute isn't user");
      }

      try {
        UserVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getUserVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (User) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Member)) {
        throw new ConsistencyErrorException("Attribute holder of member attribute isn't member");
      }

      try {
        MemberVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getMemberVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_GROUP_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Group)) {
        throw new ConsistencyErrorException("Attribute holder of group attribute isn't group");
      }

      try {
        GroupVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getGroupVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Group) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Group)) {
        throw new ConsistencyErrorException("First attribute holder of group_resource attribute isn't group");
      }
      if (attributeHolder2 == null || !(attributeHolder2 instanceof Resource)) {
        throw new ConsistencyErrorException("Second attribute holder of group-resource attribute isn't resource");
      }

      try {
        GroupResourceVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getResourceGroupVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Group) attributeHolder,
            (Resource) attributeHolder2, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Member)) {
        throw new ConsistencyErrorException("First attribute holder of member_resource attribute isn't Member");
      }
      if (attributeHolder2 == null || !(attributeHolder2 instanceof Resource)) {
        throw new ConsistencyErrorException("Second attribute holder of member_resource attribute isn't Resource");
      }

      try {
        MemberResourceVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getResourceMemberVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder,
            (Resource) attributeHolder2, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }
    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT)) {
      if (!(attributeHolder instanceof Member)) {
        throw new ConsistencyErrorException("First attribute holder of member_group attribute isn't Member");
      }
      if (attributeHolder2 == null || !(attributeHolder2 instanceof Group)) {
        throw new ConsistencyErrorException("Second attribute holder of member_group attribute isn't Group");
      }

      try {
        MemberGroupVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getMemberGroupVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder,
            (Group) attributeHolder2, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_UES_ATTR_VIRT)) {
      if (!(attributeHolder instanceof UserExtSource)) {
        throw new ConsistencyErrorException("Attribute holder of UserExtSource attribute isn't UserExtSource");
      }

      try {
        UserExtSourceVirtualAttributesModuleImplApi attributeModule =
            attributesManagerImpl.getUserExtSourceVirtualAttributeModule(sess, attribute);
        return attributeModule.getAttributeValue((PerunSessionImpl) sess, (UserExtSource) attributeHolder, attribute);
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

    } else {
      throw new InternalErrorException("Virtual attribute modules for this namespace isn't defined.");
    }
  }

  /**
   * Sets value of attribute. If attribute is not virtual or core, it returns it without doing anything.
   *
   * @param sess             perun session
   * @param attribute        attribute to set value for.
   * @param attributeHolder  primary attribute holder (Facility, Resource, Member...) for which you want the attribute
   *                         value
   * @param attributeHolder2 secondary holder (Facility, Resource, Member...) for which you want the attribute value
   * @return attribute with set value
   */
  private Attribute setValueOfAttribute(PerunSession sess, Attribute attribute, Object attributeHolder,
                                        Object attributeHolder2) {

    //core attribute
    if (this.isCoreAttribute(sess, attribute) && attributeHolder != null) {
      return setValueForCoreAttribute(attribute, attributeHolder);
      //virtual attribute
    } else if (this.isVirtAttribute(sess, attribute)) {
      return setValueForVirtualAttribute(sess, this, attribute, attributeHolder, attributeHolder2);
    } else {
      return attribute;
    }
  }

  /**
   * Sets values for core and virtual attributes. If it gets attributes that are not virtual or core, it returns them as
   * they are.
   *
   * @param sess             perun session
   * @param attributes       attributes to set value for.
   * @param attributeHolder  primary attribute holder (Facility, Resource, Member...) for which you want the attribute
   *                         value
   * @param attributeHolder2 secondary holder (Facility, Resource, Member...) for which you want the attribute value
   * @return list of attributes with set values
   */
  private List<Attribute> setValuesOfAttributes(PerunSession sess, List<Attribute> attributes, Object attributeHolder,
                                                Object attributeHolder2) {

    List<Attribute> attributesToReturn = new ArrayList<>();

    for (Attribute attribute : attributes) {

      //core attribute
      if (this.isCoreAttribute(sess, attribute) && attributeHolder != null) {

        Attribute attribute1 = setValueForCoreAttribute(attribute, attributeHolder);
        //add attribute to list to return
        attributesToReturn.add(attribute1);

        //virtual attribute
      } else if (this.isVirtAttribute(sess, attribute)) {
        Attribute attribute1 = setValueForVirtualAttribute(sess, this, attribute, attributeHolder, attributeHolder2);

        attributesToReturn.add(attribute1);
        //if it's neither virtual nor core attribute, its value does not need to be set, return it as it is
      } else {
        attributesToReturn.add(attribute);
      }
    }

    return attributesToReturn;
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    return getFacilityVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, facility,
        attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    return getResourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, resource,
        attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Group group, Attribute attribute) {
    return getGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, group, attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    return getFacilityUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, user,
        facility, attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    return getResourceGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, group,
        resource, attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Member member, Group group, Attribute attribute) {
    return getMemberGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member,
        group, attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, Member member, Attribute attribute) {
    return getMemberVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member,
        attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, User user, Attribute attribute) {
    return getUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, user, attribute);
  }

  @Override
  public boolean setVirtualAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) {
    return getUserExtSourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, ues,
        attribute);
  }

  private void tryToInsertUniqueValue(String sql, Object[] sqlArgs, Attribute attribute, PerunBean pb1, PerunBean pb2)
      throws WrongAttributeValueException {
    try {
      jdbc.update(sql, sqlArgs);
    } catch (DuplicateKeyException ex) {
      throw new WrongAttributeValueException(attribute, pb1, pb2, "value " + sqlArgs[0] + " is not unique");
    }
  }

  @Override
  public void unregisterAttributeModule(AttributesModuleImplApi module) {
    Auditer.unregisterAttributeModule(module);
    LOG.debug("Module {} was removed from audit message listening.", module.getClass().getName());
  }

  @Override
  public boolean updateAttribute(PerunSession sess, Attribute attribute, String tableName, List<String> columnNames,
                                 List<Object> columnValues) {
    // add additional SQL values to the list
    List<Object> values = new ArrayList<>();
    values.add(BeansUtils.attributeValueToString(attribute)); // valueColName
    values.add(sess.getPerunPrincipal().getActor()); // modified_by
    values.add(sess.getPerunPrincipal().getUserId()); // modified_by_uid
    values.addAll(columnValues);
    int changed = jdbc.update(
        "update " + tableName + " set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" +
        Compatibility.getSysdate() + " where " + buildParameters(columnNames, "=?", " and "), values.toArray());
    return changed > 0;
  }

  @Override
  public AttributeDefinition updateAttributeDefinition(PerunSession perunSession,
                                                       AttributeDefinition attributeDefinition) {
    try {
      Map<String, Object> map = jdbc.queryForMap(
          "SELECT attr_name, friendly_name, namespace, type, dsc, display_name, is_unique FROM attr_names WHERE id=?",
          attributeDefinition.getId());

      //update description
      if (!Objects.equals(attributeDefinition.getDescription(), map.get("dsc"))) {
        this.setAttributeDefinitionModified(perunSession, attributeDefinition);
        jdbc.update(
            "update attr_names set dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() +
            "  where id=?", attributeDefinition.getDescription(), perunSession.getPerunPrincipal().getActor(),
            perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());
      }

      //update displayName
      // if stored value was null and new isn't, update
      // if values not null && not equals, update
      if ((map.get("display_name") == null && attributeDefinition.getDisplayName() != null) ||
          (map.get("display_name") != null &&
           !Objects.equals(map.get("display_name"), attributeDefinition.getDisplayName()))) {
        this.setAttributeDefinitionModified(perunSession, attributeDefinition);
        jdbc.update("update attr_names set display_name=?, modified_by=?, modified_by_uid=?, modified_at=" +
                    Compatibility.getSysdate() + "  where id=?", attributeDefinition.getDisplayName(),
            perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(),
            attributeDefinition.getId());
      }

      //update unique
      boolean uniqueInDb = (Boolean) map.get("is_unique");
      if (uniqueInDb != attributeDefinition.isUnique()) {
        jdbc.update("UPDATE attr_names SET is_unique=?, modified_by=?, modified_by_uid=?, modified_at=" +
                    Compatibility.getSysdate() + " WHERE id=?", attributeDefinition.isUnique(),
            perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(),
            attributeDefinition.getId());
      }

      return attributeDefinition;
    } catch (EmptyResultDataAccessException ex) {
      throw new ConsistencyErrorException("Updating non existing attributeDefinition", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  static class SingleBeanAttributeRowMapper<T extends PerunBean> extends AttributeRowMapper<T, T> {
    SingleBeanAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, T attributeHolder) {
      super(sess, attributesManagerImpl, attributeHolder, null);
    }
  }

  protected abstract static class AttributeRowMapper<T extends PerunBean, V extends PerunBean>
      implements RowMapper<Attribute> {
    private final PerunSession sess;
    private final AttributesManagerImpl attributesManagerImpl;
    private final T attributeHolder;
    private final V attributeHolder2;

    /**
     * Constructor.
     *
     * @param attributeHolder  Facility, Resource or Member for which you want the attribute value
     * @param attributeHolder2 secondary Facility, Resource or Member for which you want the attribute value
     */
    AttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, T attributeHolder,
                       V attributeHolder2) {
      this.sess = sess;
      this.attributesManagerImpl = attributesManagerImpl;
      this.attributeHolder = attributeHolder;
      this.attributeHolder2 = attributeHolder2;
    }

    @Override
    public Attribute mapRow(ResultSet rs, int i) throws SQLException {
      Attribute attribute = new Attribute(ATTRIBUTE_DEFINITION_MAPPER.mapRow(rs, i));

      if (!this.attributesManagerImpl.isVirtAttribute(sess, attribute) &&
          !this.attributesManagerImpl.isCoreAttribute(sess, attribute)) {
        attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
        attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
        attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
        attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));
      }
      //core attributes
      if (this.attributesManagerImpl.isCoreAttribute(sess, attribute) && attributeHolder != null) {

        return this.attributesManagerImpl.setValueForCoreAttribute(attribute, attributeHolder);

        //virtual attributes
      } else if (this.attributesManagerImpl.isVirtAttribute(sess, attribute)) {

        return this.attributesManagerImpl.setValueForVirtualAttribute(sess, this.attributesManagerImpl, attribute,
            attributeHolder, attributeHolder2);

        //core managed attributes
      } else if (this.attributesManagerImpl.isCoreManagedAttribute(sess, attribute) && attributeHolder != null) {
        String managerName = attribute.getNamespace().substring(attribute.getNamespace().lastIndexOf(":") + 1);
        String methodName = "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) +
                            attribute.getFriendlyName().substring(1);

        try {
          Object manager = sess.getPerun().getClass().getMethod("get" + managerName).invoke(sess.getPerun());
          attribute.setValue(manager.getClass().getMethod(methodName, PerunSession.class, attributeHolder.getClass())
              .invoke(manager, sess, attributeHolder));
        } catch (NoSuchMethodException ex) {
          throw new InternalErrorException("Bad core-managed attribute definition.", ex);
        } catch (IllegalAccessException ex) {
          throw new InternalErrorException(ex);
        } catch (InvocationTargetException ex) {
          throw new InternalErrorException("An exception raise while geting core-managed attribute value.", ex);
        }
      }

      try {
        attribute.setValue(BeansUtils.stringToAttributeValue(rs.getString("attr_value"), attribute.getType()));
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }

      return attribute;
    }
  }

  static class UserFacilityAttributeRowMapper extends AttributeRowMapper<User, Facility> {
    UserFacilityAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, User attributeHolder,
                                   Facility attributeHolder2) {
      super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
    }
  }

  static class MemberResourceAttributeRowMapper extends AttributeRowMapper<Member, Resource> {
    MemberResourceAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl,
                                     Member attributeHolder, Resource attributeHolder2) {
      super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
    }
  }

  protected static class MemberGroupAttributeRowMapper extends AttributeRowMapper<Member, Group> {
    MemberGroupAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl,
                                  Member attributeHolder, Group attributeHolder2) {
      super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
    }
  }

  protected static class GroupResourceAttributeRowMapper extends AttributeRowMapper<Group, Resource> {
    GroupResourceAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl,
                                    Group attributeHolder, Resource attributeHolder2) {
      super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
    }
  }

  private static class ValueRowMapper implements RowMapper<Object> {
    private final AttributeDefinition attributeDefinition;

    ValueRowMapper(AttributeDefinition attributeDefinition) {
      this.attributeDefinition = attributeDefinition;
    }

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
      try {
        return BeansUtils.stringToAttributeValue(rs.getString("attr_value"), attributeDefinition.getType());
      } catch (InternalErrorException ex) {
        throw new InternalErrorException(ex);
      }
    }
  }

  private static class RichAttributeRowMapper<P, S> implements RowMapper<RichAttribute<P, S>> {
    private final AttributeRowMapper attributeRowMapper;
    private final RowMapper<P> primaryRowMapper;
    private final RowMapper<S> secondaryRowMapper;


    RichAttributeRowMapper(AttributeRowMapper attributeRowMapper, RowMapper<P> primaryRowMapper,
                           RowMapper<S> secondaryRowMapper) {
      this.attributeRowMapper = attributeRowMapper;
      this.primaryRowMapper = primaryRowMapper;
      this.secondaryRowMapper = secondaryRowMapper;
    }


    @Override
    public RichAttribute<P, S> mapRow(ResultSet rs, int i) throws SQLException {
      Attribute attribute = attributeRowMapper.mapRow(rs, i);
      P primaryHolder = primaryRowMapper.mapRow(rs, i);
      S secondaryHolder = null;
      if (secondaryRowMapper != null) {
        secondaryHolder = secondaryRowMapper.mapRow(rs, i);
      }
      return new RichAttribute<>(primaryHolder, secondaryHolder, attribute);
    }

  }

  private static class GroupAttributeExtractor implements ResultSetExtractor<HashMap<Group, List<Attribute>>> {
    private final PerunSession sess;
    private final AttributesManagerImpl attributesManager;
    private final List<Group> groups;

    /**
     * Sets up parameters for data extractor
     *
     * @param sess              perun session
     * @param attributesManager attribute manager
     * @param groups            list of groups
     */
    GroupAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<Group> groups) {
      this.sess = sess;
      this.attributesManager = attributesManager;
      this.groups = groups;
    }

    @Override
    public HashMap<Group, List<Attribute>> extractData(ResultSet rs) throws SQLException {
      HashMap<Group, List<Attribute>> map = new HashMap<>();
      HashMap<Integer, Group> groupObjectMap = new HashMap<>();

      for (Group group : groups) {
        groupObjectMap.put(group.getId(), group);
      }

      while (rs.next()) {
        // fetch from map by ID
        Integer id = rs.getInt("id");
        Group group = groupObjectMap.get(id);

        map.computeIfAbsent(group, k -> new ArrayList<>());
        // if not present, put in map
        AttributeRowMapper<Group, Group> attributeRowMapper =
            new SingleBeanAttributeRowMapper<>(sess, attributesManager, group);
        Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

        if (attribute != null) {
          // add only if exists
          map.get(group).add(attribute);
        }
      }
      return map;
    }
  }

  private static class MemberAttributeExtractor implements ResultSetExtractor<HashMap<Member, List<Attribute>>> {
    private final PerunSession sess;
    private final AttributesManagerImpl attributesManager;
    private final List<Member> members;
    private final Resource resource;

    /**
     * Sets up parameters for data extractor
     *
     * @param sess              perun session
     * @param attributesManager attribute manager
     * @param members           list of members
     */
    MemberAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<Member> members) {
      this.sess = sess;
      this.attributesManager = attributesManager;
      this.members = members;
      this.resource = null;
    }

    /**
     * Sets up parameters for data extractor For memberResource attributes we need also know the resource.
     *
     * @param sess              perun session
     * @param attributesManager attribute manager
     * @param resource          resource for member resource attributes
     * @param members           list of members
     */
    MemberAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, Resource resource,
                             List<Member> members) {
      this.sess = sess;
      this.attributesManager = attributesManager;
      this.members = members;
      this.resource = resource;
    }

    @Override
    public HashMap<Member, List<Attribute>> extractData(ResultSet rs) throws SQLException {
      HashMap<Member, List<Attribute>> map = new HashMap<>();
      HashMap<Integer, Member> memberObjectMap = new HashMap<>();

      for (Member member : members) {
        memberObjectMap.put(member.getId(), member);
      }

      while (rs.next()) {
        // fetch from map by ID
        Integer id = rs.getInt("id");
        Member mem = memberObjectMap.get(id);

        map.computeIfAbsent(mem, k -> new ArrayList<>());
        // if not present, put in map
        AttributeRowMapper attributeRowMapper;
        if (resource != null) {
          attributeRowMapper = new MemberResourceAttributeRowMapper(sess, attributesManager, mem, resource);
        } else {
          attributeRowMapper = new SingleBeanAttributeRowMapper<>(sess, attributesManager, mem);
        }
        Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

        if (attribute != null) {
          // add only if exists
          map.get(mem).add(attribute);
        }
      }
      return map;
    }
  }

  private static class MemberGroupAttributeExtractor implements ResultSetExtractor<HashMap<Member, List<Attribute>>> {
    private final PerunSession sess;
    private final AttributesManagerImpl attributesManager;
    private final List<Member> members;
    private final Group group;

    /**
     * Sets up parameters for data extractor For memberResource attributes we need also know the resource.
     *
     * @param sess              perun session
     * @param attributesManager attribute manager
     * @param resource          resource for member resource attributes
     * @param members           list of members
     */
    MemberGroupAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<Member> members,
                                  Group group) {
      this.sess = sess;
      this.attributesManager = attributesManager;
      this.members = members;
      this.group = group;
    }

    @Override
    public HashMap<Member, List<Attribute>> extractData(ResultSet rs) throws SQLException {
      HashMap<Member, List<Attribute>> map = new HashMap<>();
      HashMap<Integer, Member> memberObjectMap = new HashMap<>();

      for (Member member : members) {
        memberObjectMap.put(member.getId(), member);
      }

      while (rs.next()) {
        // fetch from map by ID
        Integer id = rs.getInt("id");
        Member mem = memberObjectMap.get(id);

        map.computeIfAbsent(mem, k -> new ArrayList<>());
        // if not present, put in map
        AttributeRowMapper attributeRowMapper;
        attributeRowMapper = new MemberGroupAttributeRowMapper(sess, attributesManager, mem, group);
        Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

        if (attribute != null) {
          // add only if exists
          map.get(mem).add(attribute);
        }
      }
      return map;
    }
  }

  private static class UserAttributeExtractor implements ResultSetExtractor<HashMap<User, List<Attribute>>> {
    private final PerunSession sess;
    private final AttributesManagerImpl attributesManager;
    private final List<User> users;
    private final Facility facility;

    UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users) {
      this(sess, attributesManager, users, null);
    }

    /**
     * Sets up parameters for data extractor
     *
     * @param sess              perun session
     * @param attributesManager attribute manager
     * @param users             list of users
     */
    UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users,
                           Facility facility) {
      this.sess = sess;
      this.attributesManager = attributesManager;
      this.users = users;
      this.facility = facility;
    }

    @Override
    public HashMap<User, List<Attribute>> extractData(ResultSet rs) throws SQLException {
      HashMap<User, List<Attribute>> map = new HashMap<>();
      HashMap<Integer, User> userObjectMap = new HashMap<>();

      for (User user : users) {
        userObjectMap.put(user.getId(), user);
      }

      while (rs.next()) {
        // fetch from map by ID
        Integer id = rs.getInt("id");
        User user = userObjectMap.get(id);

        map.computeIfAbsent(user, k -> new ArrayList<>());
        // if not preset, put in map

        AttributeRowMapper attributeRowMapper =
            new UserFacilityAttributeRowMapper(sess, attributesManager, user, facility);
        Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

        if (attribute != null) {
          // add only if exists
          map.get(user).add(attribute);
        }
      }
      return map;
    }
  }
}
