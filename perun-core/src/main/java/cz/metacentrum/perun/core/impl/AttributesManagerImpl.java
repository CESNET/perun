package cz.metacentrum.perun.core.impl;

import java.util.*;

import cz.metacentrum.perun.core.api.ActionType;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;

import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
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
import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;

import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import cz.metacentrum.perun.core.api.exceptions.rt.ConsistencyErrorRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;

import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.HostAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.Map.Entry;
import org.springframework.dao.DataAccessException;

/**
 * AttributesManager implementation.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributesManagerImpl implements AttributesManagerImplApi {

	//Items of list delimiter. It's used while storing list into string.
	//Can't contain regex special symbols
	public static final char LIST_DELIMITER = ',';
	public static final char KEY_VALUE_DELIMITER = ':';
	private static final String ATTRIBUTES_MODULES_PACKAGE = "cz.metacentrum.perun.core.impl.modules.attributes";
	private static final int MERGE_TRY_CNT = 10;
	private static final long MERGE_RAND_SLEEP_MAX = 100;  //max sleep time between SQL merge attempt in millisecond

	private final static Logger log = LoggerFactory.getLogger(AttributesManagerImpl.class);

	private Perun perun;
	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private JdbcPerunTemplate jdbc;
	private LobHandler lobHandler;
	private ClassLoader classLoader = this.getClass().getClassLoader();
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	//Attributes modules.  name => module
	private Map<String, AttributesModuleImplApi> attributesModulesMap = new ConcurrentHashMap<String, AttributesModuleImplApi>();

	private AttributesManagerImplApi self;

	// mapping of the perun bean names to the attribute namespaces
	private static final Map<String,String> NAMESPACES_BEANS_MAP = new HashMap<>();
	static {
		NAMESPACES_BEANS_MAP.put("user", NS_USER_ATTR);
		NAMESPACES_BEANS_MAP.put("member", NS_MEMBER_ATTR);
		NAMESPACES_BEANS_MAP.put("facility", NS_FACILITY_ATTR);
		NAMESPACES_BEANS_MAP.put("vo", NS_VO_ATTR);
		NAMESPACES_BEANS_MAP.put("host", NS_HOST_ATTR);
		NAMESPACES_BEANS_MAP.put("group", NS_GROUP_ATTR);
		NAMESPACES_BEANS_MAP.put("resource", NS_RESOURCE_ATTR);
		NAMESPACES_BEANS_MAP.put("member_resource", NS_MEMBER_RESOURCE_ATTR);
		NAMESPACES_BEANS_MAP.put("member_group", NS_MEMBER_GROUP_ATTR);
		NAMESPACES_BEANS_MAP.put("user_facility", NS_USER_FACILITY_ATTR);
		NAMESPACES_BEANS_MAP.put("group_resource", NS_GROUP_RESOURCE_ATTR);
		NAMESPACES_BEANS_MAP.put("user_ext_source", NS_UES_ATTR);
	}

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool instance
	 */
	public AttributesManagerImpl(DataSource perunPool) throws InternalErrorException {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
		this.jdbc = new JdbcPerunTemplate(perunPool);
		if(Compatibility.isOracle()) {
			OracleLobHandler oracleLobHandler = new OracleLobHandler();
			oracleLobHandler.setNativeJdbcExtractor(new CommonsDbcpNativeJdbcExtractor());
			lobHandler = oracleLobHandler;
		} else {
			lobHandler = new DefaultLobHandler();
		}
	}

	protected final static String attributeDefinitionMappingSelectQuery = "attr_names.id as attr_names_id, attr_names.friendly_name as attr_names_friendly_name, " +
		"attr_names.namespace as attr_names_namespace, attr_names.type as attr_names_type, attr_names.display_name as attr_names_display_name," +
		"attr_names.created_at as attr_names_created_at, attr_names.created_by as attr_names_created_by, " +
		"attr_names.modified_by as attr_names_modified_by, attr_names.modified_at as attr_names_modified_at, attr_names.dsc as attr_names_dsc, " +
		"attr_names.created_by_uid as attr_names_created_by_uid, attr_names.modified_by_uid as attr_names_modified_by_uid";

	protected final static String attributeMappingSelectQuery = attributeDefinitionMappingSelectQuery + ", attr_value";

	protected final static String attributeAndTextMappingSelectQuery = attributeMappingSelectQuery + ", attr_value_text";

	protected final static String attributeRightSelectQuery = "attributes_authz.attr_id as attr_name_id, roles.name as role_name, "
		+ "action_types.action_type as action_type";

	protected static final String getAttributeMappingSelectQuery(String nameOfValueTable) {
		String selectQuery = "";
		selectQuery = selectQuery.concat(attributeAndTextMappingSelectQuery);
		selectQuery = selectQuery.concat(", " + nameOfValueTable + ".created_at as attr_value_created_at");
		selectQuery = selectQuery.concat(", " + nameOfValueTable + ".created_by as attr_value_created_by");
		selectQuery = selectQuery.concat(", " + nameOfValueTable + ".modified_at as attr_value_modified_at");
		selectQuery = selectQuery.concat(", " + nameOfValueTable + ".modified_by as attr_value_modified_by");
		return selectQuery;
	}

	private static final RowMapper<AttributeDefinition> ATTRIBUTE_DEFINITION_MAPPER = new RowMapper<AttributeDefinition>() {
		public AttributeDefinition mapRow(ResultSet rs, int i) throws SQLException {

			AttributeDefinition attribute = new AttributeDefinition();
			attribute.setId(rs.getInt("attr_names_id"));
			attribute.setFriendlyName(rs.getString("attr_names_friendly_name"));
			attribute.setNamespace(rs.getString("attr_names_namespace"));
			attribute.setType(rs.getString("attr_names_type"));
			attribute.setDisplayName(rs.getString("attr_names_display_name"));
			attribute.setDescription(rs.getString("attr_names_dsc"));
			attribute.setCreatedAt(rs.getString("attr_names_created_at"));
			attribute.setCreatedBy(rs.getString("attr_names_created_by"));
			attribute.setModifiedAt(rs.getString("attr_names_modified_at"));
			attribute.setModifiedBy(rs.getString("attr_names_modified_by"));
			if(rs.getInt("attr_names_modified_by_uid") == 0) attribute.setModifiedByUid(null);
			else attribute.setModifiedByUid(rs.getInt("attr_names_modified_by_uid"));
			if(rs.getInt("attr_names_created_by_uid") == 0) attribute.setCreatedByUid(null);
			else attribute.setCreatedByUid(rs.getInt("attr_names_created_by_uid"));
			return attribute;
		}
	};

	/*
	 * This rowMapper is only for getting attribute values (value and valueText)
	 */
	private static final RowMapper<String> ATTRIBUTE_VALUES_MAPPER = new RowMapper<String>() {
		public String mapRow(ResultSet rs, int i) throws SQLException {
			String value;
			String valueText;
			try {
				//CLOB in oracle
				if (Compatibility.isOracle()) {
					Clob clob = rs.getClob("attr_value_text");
					char[] cbuf = null;
					if(clob == null) {
						valueText = null;
					} else {
						try {
							cbuf = new char[(int) clob.length()];
							clob.getCharacterStream().read(cbuf);
						} catch(IOException ex) {
							throw new InternalErrorRuntimeException(ex);
						}
						valueText = new String(cbuf);
					}
				} else {
					// POSTGRES READ CLOB AS STRING
					valueText = rs.getString("attr_value_text");
				}
			} catch (InternalErrorException ex) {
				// WHEN CHECK FAILS TRY TO READ AS POSTGRES
					valueText = rs.getString("attr_value_text");
			}

			value = rs.getString("attr_value");

			if(valueText != null) return valueText;
			else return value;
		}
	};

	protected static final RowMapper<Attribute> ATTRIBUTE_MAPPER = new RowMapper<Attribute>() {
		public Attribute mapRow(ResultSet rs, int i) throws SQLException {

			AttributeDefinition attributeDefinition = new AttributeDefinition();
			attributeDefinition.setId(rs.getInt("attr_names_id"));
			attributeDefinition.setFriendlyName(rs.getString("attr_names_friendly_name"));
			attributeDefinition.setNamespace(rs.getString("attr_names_namespace"));
			attributeDefinition.setType(rs.getString("attr_names_type"));
			attributeDefinition.setDisplayName("attr_names_display_name");
			attributeDefinition.setCreatedAt(rs.getString("attr_names_created_at"));
			attributeDefinition.setCreatedBy(rs.getString("attr_names_created_by"));
			attributeDefinition.setModifiedAt(rs.getString("attr_names_modified_at"));
			attributeDefinition.setModifiedBy(rs.getString("attr_names_modified_by"));
			if(rs.getInt("attr_names_modified_by_uid") == 0) attributeDefinition.setModifiedByUid(null);
			else attributeDefinition.setModifiedByUid(rs.getInt("attr_names_modified_by_uid"));
			if(rs.getInt("attr_names_created_by_uid") == 0) attributeDefinition.setCreatedByUid(null);
			else attributeDefinition.setCreatedByUid(rs.getInt("attr_names_created_by_uid"));

			Attribute attribute = new Attribute(attributeDefinition);
			attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
			attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
			attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
			attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));

			String stringValue = rs.getString("attr_value");

			try {
				attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
			} catch(InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

			attribute.setDescription(rs.getString("attr_names_dsc"));

			return attribute;
		}

	};

	private static final RowMapper<String> ENTITYLESS_KEYS_MAPPER = new RowMapper<String>() {
		public String mapRow(ResultSet rs, int i) throws SQLException {

			return rs.getString("subject");
		}
	};

	private static final RowMapper<String> ATTRIBUTE_NAMES_MAPPER = new RowMapper<String>() {
		public String mapRow(ResultSet rs, int i) throws SQLException {

			return rs.getString("attr_name");
		}
	};

	protected static class AttributeRowMapper implements RowMapper<Attribute> {
		private final PerunSession sess;
		private final AttributesManagerImpl attributesManagerImpl;
		private final Object attributeHolder;
		private final Object attributeHolder2;

		/**
		 * Constructor.
		 *
		 * @param sess
		 * @param attributesManagerImpl
		 * @param attributeHolder Facility, Resource or Member for which you want the attribute value
		 */
		public AttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Object attributeHolder) {
			this(sess, attributesManagerImpl, attributeHolder, null);
		}

		/**
		 * Constructor.
		 *
		 * @param sess
		 * @param attributesManagerImpl
		 * @param attributeHolder Facility, Resource or Member for which you want the attribute value
		 * @param attributeHolder2 secondary Facility, Resource or Member for which you want the attribute value
		 */
		public AttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Object attributeHolder, Object attributeHolder2) {
			this.sess = sess;
			this.attributesManagerImpl = attributesManagerImpl;
			this.attributeHolder = attributeHolder;
			this.attributeHolder2 = attributeHolder2;
		}

		public Attribute mapRow(ResultSet rs, int i) throws SQLException {
			Attribute attribute = new Attribute(ATTRIBUTE_DEFINITION_MAPPER.mapRow(rs, i));

			if(!this.attributesManagerImpl.isVirtAttribute(sess, attribute) && !this.attributesManagerImpl.isCoreAttribute(sess, attribute)) {
				attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
				attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
				attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
				attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));
			}
			//core attributes
			if(this.attributesManagerImpl.isCoreAttribute(sess, attribute) && attributeHolder != null) {

				String methodName = "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
				Method method;
				try {
					method = attributeHolder.getClass().getMethod(methodName);
				} catch(NoSuchMethodException ex) {
					//if not "get", try "is"
					String methodGet = methodName;
					try {
						methodName = "is" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
						method = attributeHolder.getClass().getMethod(methodName);
					} catch (NoSuchMethodException e) {
						throw new InternalErrorRuntimeException("There is no method '" + methodGet + "' or '" + methodName + "'  for core attribute definition. " + attribute , e);
					}
				}
				try {
					Object value = method.invoke(attributeHolder);

					//
					// Try to automatically convert object returned from bean method call to required data type
					//
					if (value == null) {
						// No need to convert NULL value (for String it caused NULL->"null" conversion)
					} else if((attribute.getType().equals(String.class.getName()) || attribute.getType().equals(BeansUtils.largeStringClassName)) && !(value instanceof String)) {
						//TODO check exceptions
						value = String.valueOf(value);
					} else if(attribute.getType().equals(Integer.class.getName()) && !(value instanceof Integer)) {
						//TODO try to cast to integer
					} else if(attribute.getType().equals(Boolean.class.getName()) && !(value instanceof Boolean)) {
						//TODO try to cast to boolean
					} else if((attribute.getType().equals(ArrayList.class.getName()) || attribute.getType().equals(BeansUtils.largeArrayListClassName)) && !(value instanceof ArrayList)) {
						if(value instanceof List) {
							value = new ArrayList<String>((List)value);
						} else {
							throw new InternalErrorRuntimeException("Cannot convert result of method " + attributeHolder.getClass().getName() + "." + methodName + " to ArrayList.");
						}
					} else if(attribute.getType().equals(LinkedHashMap.class.getName()) && !(value instanceof LinkedHashMap)) {
						if(value instanceof Map) {
							value = new LinkedHashMap<String, String>((Map)value);
						} else {
							throw new InternalErrorRuntimeException("Cannot convert result of method " + attributeHolder.getClass().getName() + "." + methodName + " to LinkedHashMap.");
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
				} catch(IllegalAccessException ex) {
					throw new InternalErrorRuntimeException(ex);
				} catch(InvocationTargetException ex) {
					throw new InternalErrorRuntimeException("An exception raise while getting core attribute value.", ex);
				}

				//virtual attributes
			} else if(this.attributesManagerImpl.isVirtAttribute(sess, attribute)) {
				if(attributeHolder == null) throw new InternalErrorRuntimeException("Bad usage of attributeRowMapper");

				if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR_VIRT)) {
					if(!(attributeHolder instanceof User)) throw new ConsistencyErrorRuntimeException("First attribute holder of user_facility attribute isn't user");
					if(attributeHolder2 == null || !(attributeHolder2 instanceof Facility)) throw new ConsistencyErrorRuntimeException("Second attribute holder of user_facility attribute isn't facility");

					try {
						FacilityUserVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getFacilityUserVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Facility) attributeHolder2, (User) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_FACILITY_ATTR_VIRT)) {
					if(!(attributeHolder instanceof Facility)) throw new ConsistencyErrorRuntimeException("Attribute holder of facility attribute isn't facility");

					try {
						FacilityVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getFacilityVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Facility) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_RESOURCE_ATTR_VIRT)) {
					if(!(attributeHolder instanceof Resource)) throw new ConsistencyErrorRuntimeException("Attribute holder of resource attribute isn't resource");

					try {
						ResourceVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getResourceVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR_VIRT)) {
					if(!(attributeHolder instanceof User)) throw new ConsistencyErrorRuntimeException("Attribute holder of user attribute isn't user");

					try {
						UserVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getUserVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (User) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR_VIRT)) {
					if(!(attributeHolder instanceof Member)) throw new ConsistencyErrorRuntimeException("Attribute holder of member attribute isn't member");

					try {
						MemberVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getMemberVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT)) {
					if(!(attributeHolder instanceof Group)) throw new ConsistencyErrorRuntimeException("First attribute holder of group_resource attribute isn't group");
					if(attributeHolder2 == null || !(attributeHolder2 instanceof Resource)) throw new ConsistencyErrorRuntimeException("Second attribute holder of group-resource attribute isn't resource");

					try {
						ResourceGroupVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getResourceGroupVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder2, (Group) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT)) {
					if(!(attributeHolder instanceof Resource)) throw new ConsistencyErrorRuntimeException("First attribute holder of member_resource attribute isn't Member");
					if(attributeHolder2 == null || !(attributeHolder2 instanceof Member)) throw new ConsistencyErrorRuntimeException("Second attribute holder of member_resource attribute isn't resource");

					try {
						ResourceMemberVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getResourceMemberVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder, (Member) attributeHolder2, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}
				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT)) {
					if (!(attributeHolder instanceof Member))
						throw new ConsistencyErrorRuntimeException("First attribute holder of member_group attribute isn't Member");
					if (attributeHolder2 == null || !(attributeHolder2 instanceof Group))
						throw new ConsistencyErrorRuntimeException("Second attribute holder of member_group attribute isn't Group");

					try {
						MemberGroupVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getMemberGroupVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder, (Group) attributeHolder2, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else if(this.attributesManagerImpl.isFromNamespace(sess, attribute, AttributesManager.NS_UES_ATTR_VIRT)) {
					if(!(attributeHolder instanceof UserExtSource)) throw new ConsistencyErrorRuntimeException("Attribute holder of UserExtSource attribute isn't UserExtSource");

					try {
						UserExtSourceVirtualAttributesModuleImplApi attributeModule = this.attributesManagerImpl.getUserExtSourceVirtualAttributeModule(sess, attribute);
						return attributeModule.getAttributeValue((PerunSessionImpl) sess, (UserExtSource) attributeHolder, attribute);
					} catch (InternalErrorException ex) {
						throw new InternalErrorRuntimeException(ex);
					}

				} else {
					//TODO Add virtual attribute modules for another namespaces
					throw new InternalErrorRuntimeException("Virtual attribute modules for this namespace isn't defined.");
				}

				//core managed attributes
			} else if(this.attributesManagerImpl.isCoreManagedAttribute(sess, attribute) && attributeHolder != null) {
				String managerName = attribute.getNamespace().substring(attribute.getNamespace().lastIndexOf(":") + 1);
				String methodName = "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);

				try {
					Object manager = sess.getPerun().getClass().getMethod("get" + managerName).invoke(sess.getPerun());
					attribute.setValue(manager.getClass().getMethod(methodName, PerunSession.class, attributeHolder.getClass()).invoke(manager, sess, attributeHolder));
				} catch(NoSuchMethodException ex) {
					throw new InternalErrorRuntimeException("Bad core-managed attribute definition.", ex);
				} catch(IllegalAccessException ex) {
					throw new InternalErrorRuntimeException(ex);
				} catch(InvocationTargetException ex) {
					throw new InternalErrorRuntimeException("An exception raise while geting core-managed attribute value.", ex);
				}
			}

			//FIXME use ValueRowMapper
			String stringValue;
			if(Utils.isLargeAttribute(sess, attribute)) {

				try {
					if (Compatibility.isOracle()) {
						//large attributes
						Clob clob = rs.getClob("attr_value_text");
						char[] cbuf = null;
						if(clob == null) {
							stringValue = null;
						} else {
							try {
								cbuf = new char[(int) clob.length()];
								clob.getCharacterStream().read(cbuf);
							} catch(IOException ex) {
								throw new InternalErrorRuntimeException(ex);
							}
							stringValue = new String(cbuf);
						}
					} else {
						// POSTGRES READ CLOB AS STRING
						stringValue = rs.getString("attr_value_text");
					}
				} catch (InternalErrorException ex) {
					// WHEN CHECK FAILS TRY TO READ AS POSTGRES
					stringValue = rs.getString("attr_value_text");
				}
			} else {
				//ordinary attributes read as String
				stringValue = rs.getString("attr_value");
			}

			try {
				attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
			} catch(InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

			return attribute;
		}
	}


	private static class ValueRowMapper implements RowMapper<Object> {
		private final PerunSession sess;
		private final AttributesManagerImpl attributesManagerImpl;
		private final AttributeDefinition attributeDefinition;

		/**
		 * Constructor.
		 *
		 * @param sess
		 * @param attributesManagerImpl
		 * @param attributeDefinition
		 */
		public ValueRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, AttributeDefinition attributeDefinition) {
			this.sess = sess;
			this.attributesManagerImpl = attributesManagerImpl;
			this.attributeDefinition = attributeDefinition;
		}

		public Object mapRow(ResultSet rs, int i) throws SQLException {
			String stringValue;
			if(Utils.isLargeAttribute(sess, attributeDefinition)) {
				//large attributes
				try {
					if (Compatibility.isOracle()) {
						Clob clob = rs.getClob("attr_value_text");
						char[] cbuf = null;
						if(clob == null) {
							stringValue = null;
						} else {
							try {
								cbuf = new char[(int) clob.length()];
								clob.getCharacterStream().read(cbuf);
							} catch(IOException ex) {
								throw new InternalErrorRuntimeException(ex);
							}
							stringValue = new String(cbuf);
						}
					} else {
						// POSTGRES READ CLOB AS STRING
						stringValue = rs.getString("attr_value_text");
					}
				} catch (InternalErrorException ex) {
					// WHEN CHECK FAILS TRY TO READ AS POSTGRES
					stringValue = rs.getString("attr_value_text");
				}
			} else {
				//ordinary attributes
				stringValue = rs.getString("attr_value");
			}

			try {
				return BeansUtils.stringToAttributeValue(stringValue, attributeDefinition.getType());
			} catch(InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}
		}
	}


	private static class RichAttributeRowMapper<P,S> implements RowMapper<RichAttribute<P,S>> {
		private final AttributeRowMapper attributeRowMapper;
		private final RowMapper<P> primaryRowMapper;
		private final RowMapper<S> secondaryRowMapper;


		public RichAttributeRowMapper(AttributeRowMapper attributeRowMapper, RowMapper<P> primaryRowMapper, RowMapper<S> secondaryRowMapper) {
			this.attributeRowMapper = attributeRowMapper;
			this.primaryRowMapper = primaryRowMapper;
			this.secondaryRowMapper = secondaryRowMapper;
		}


		public RichAttribute<P,S> mapRow(ResultSet rs, int i) throws SQLException {
			Attribute attribute = attributeRowMapper.mapRow(rs, i);
			P primaryHolder = primaryRowMapper.mapRow(rs, i);
			S secondaryHolder = null;
			if(secondaryRowMapper != null) secondaryHolder = secondaryRowMapper.mapRow(rs, i);
			return new RichAttribute<P,S>(primaryHolder, secondaryHolder, attribute);
		}

	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("fac") + " from attr_names " +
					"left join facility_attr_values fac    on id=fac.attr_id and fac.facility_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess,  this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_OPT);

		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for facility exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, facility), AttributesManager.NS_FACILITY_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for facility exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, member), AttributesManager.NS_MEMBER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for member exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, vo), AttributesManager.NS_VO_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for vo exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, group), AttributesManager.NS_GROUP_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for group exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Host host) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, host), AttributesManager.NS_HOST_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for host exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("voattr") + " from attr_names " +
					"left join vo_attr_values voattr    on id=voattr.attr_id and voattr.vo_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess, this, vo), vo.getId(), AttributesManager.NS_VO_ATTR_CORE, AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("groupattr") + " from attr_names " +
					"left join group_attr_values groupattr    on id=groupattr.attr_id and groupattr.group_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess, this, group), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Host host) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
					"left join host_attr_values on id=attr_id and host_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess, this, host), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for host exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
					"left join resource_attr_values on id=attr_id and resource_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess, this, resource), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for resource exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, resource), AttributesManager.NS_RESOURCE_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for resource exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		try {
			//member-resource attributes, member core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join   member_resource_attr_values   mem        on attr_names.id=mem.attr_id and mem.resource_id=? and member_id=? " +
					"where namespace in (?,?) and (mem.attr_value is not null or mem.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, resource, member),
					resource.getId(), member.getId(),
					AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for member-resource combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
							"where namespace=?",
					new AttributeRowMapper(sess, this, resource, member), AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for member-resource combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		try {
			//member-group attributes, member core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on attr_names.id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?) and (mem_gr.attr_value is not null or mem_gr.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, member, group), group.getId(), member.getId(),
					AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for member-group combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, List<String> attrNames) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("mId", member.getId());
		parameters.addValue("rId", resource.getId());
		parameters.addValue("nSO", AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("mem_res") + " from attr_names " +
							"left join member_resource_attr_values mem_res on id=mem_res.attr_id and member_id=:mId and resource_id=:rId " +
							"where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, resource, member));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("gId", group.getId());
		parameters.addValue("rId", resource.getId());
		parameters.addValue("nSO", AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
							"left join group_resource_attr_values grp_res on id=grp_res.attr_id and group_id=:gId and resource_id=:rId " +
							"where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, group, resource));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user, Facility facility, List<String> attrNames) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("uId", user.getId());
		parameters.addValue("fId", facility.getId());
		parameters.addValue("nSO", AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_USER_FACILITY_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("user_fac") + " from attr_names " +
							"left join user_facility_attr_values user_fac on id=user_fac.attr_id and user_id=:uId and facility_id=:fId " +
							"where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, user, facility));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("mId", member.getId());
		parameters.addValue("gId", group.getId());
		parameters.addValue("nSO", AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
						"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and member_id=:mId and group_id=:gId " +
						"where namespace in ( :nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
						parameters, new AttributeRowMapper(sess, this, member, group));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join    member_attr_values     mem      on id=mem.attr_id     and    member_id=? " +
					"where namespace=? or (namespace in (?,?) and (mem.attr_value is not null or mem.attr_value_text is not null))",
					new AttributeRowMapper(sess, this, member), member.getId(),
					AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_DEF);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for member exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException {

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
					"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, vo));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("gId", group.getId());
		parameters.addValue("nSC", AttributesManager.NS_GROUP_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_GROUP_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_GROUP_ATTR_DEF);
		parameters.addValue("startPartOfName", startPartOfName + "%");

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("grt") + " from attr_names " +
					"left join group_attr_values grt on id=grt.attr_id and group_id=:gId " +
					"where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name LIKE :startPartOfName",
					parameters, new AttributeRowMapper(sess, this, group));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("rId", resource.getId());
		parameters.addValue("nSC", AttributesManager.NS_RESOURCE_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_RESOURCE_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_RESOURCE_ATTR_DEF);
		parameters.addValue("startPartOfName", startPartOfName + "%");

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("ret") + " from attr_names " +
					"left join resource_attr_values ret on id=ret.attr_id and resource_id=:rId " +
					"where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name LIKE :startPartOfName",
					parameters, new AttributeRowMapper(sess, this, resource));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException {

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
					"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, member));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("gId", group.getId());
		parameters.addValue("nSC", AttributesManager.NS_GROUP_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_GROUP_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_GROUP_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_GROUP_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("groupattr") + " from attr_names " +
					"left join group_attr_values groupattr on id=groupattr.attr_id and group_id=:gId " +
					"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, group));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
					"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
					"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
					"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? " +
					"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, facility), facility.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, user, facility), AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for user-facility combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
							"where namespace=?",
					new AttributeRowMapper(sess, this, member, group), AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for member-group combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
					"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new AttributeRowMapper(sess, this, user), user.getId(),
					AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for user exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws InternalErrorException {

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
					"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, user));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, user), AttributesManager.NS_USER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for user exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("uesId", ues.getId());
		parameters.addValue("nSC", AttributesManager.NS_UES_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_UES_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_UES_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_UES_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
					"left join user_ext_source_attr_values ues on id=ues.attr_id and ues_id=:uesId " +
					"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new AttributeRowMapper(sess, this, ues));
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names " +
					"where namespace=?",
					new AttributeRowMapper(sess, this, ues), AttributesManager.NS_UES_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for user external source exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
					"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
					"where namespace in (?,?) and (grp_res.attr_value is not null or grp_res.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(),
					AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, String key) throws InternalErrorException{
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
					"left join entityless_attr_values enattr on id=enattr.attr_id and enattr.subject=? "+
					"where namespace in (?,?) and (enattr.attr_value is not null or enattr.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, null),key, AttributesManager.NS_ENTITYLESS_ATTR_DEF, AttributesManager.NS_ENTITYLESS_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
					"left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=? " +
					"where namespace=? or (namespace in (?,?) and (ues.attr_value is not null or ues.attr_value_text is not null))",
					new AttributeRowMapper(sess, this, ues), ues.getId(),
					AttributesManager.NS_UES_ATTR_CORE, AttributesManager.NS_UES_ATTR_DEF, AttributesManager.NS_UES_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for UserExtSource exists.");
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public String getEntitylessAttrValueForUpdate(PerunSession sess, int attrId, String key) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select attr_value, attr_value_text from entityless_attr_values where subject=? and attr_id=? for update", ATTRIBUTE_VALUES_MAPPER, key, attrId);
		} catch(EmptyResultDataAccessException ex) {
			//If there is no such entityless attribute, create new one with null value and return null (insert is for transaction same like select for update)
			Attribute attr = new Attribute(this.getAttributeDefinitionById(sess, attrId));
			self.setAttributeWithNullValue(sess, key, attr);
			return null;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws  InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
					"left join entityless_attr_values enattr on id=enattr.attr_id "+
					"where attr_name=? and namespace in (?,?) and (enattr.attr_value is not null or enattr.attr_value_text is not null)",
					new AttributeRowMapper(sess, this, null),attrName, AttributesManager.NS_ENTITYLESS_ATTR_DEF, AttributesManager.NS_ENTITYLESS_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException{
		try {
			return jdbc.query("select subject from attr_names join entityless_attr_values on id=attr_id  where attr_name=?", ENTITYLESS_KEYS_MAPPER, attributeDefinition.getName());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<String>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		// Get the entity from the name
		String entity = attributeDefinition.getEntity();
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery(entity + "_attr_values") + " from attr_names join " + entity + "_attr_values on id=attr_id  where attr_name=?", ATTRIBUTE_MAPPER, attributeDefinition.getName());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<RichAttribute<User,Facility>> getAllUserFacilityRichAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + ", " + UsersManagerImpl.userMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery + "   from attr_names " +
					"left join    user_facility_attr_values     usr_fac      on attr_names.id=usr_fac.attr_id     and   usr_fac.user_id=? " +
					"join users on users.id = usr_fac.user_id " +
					"join facilities on facilities.id = usr_fac.facility_id " +
					"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new RichAttributeRowMapper<User, Facility>(new AttributeRowMapper(sess, this, user), UsersManagerImpl.USER_MAPPER, FacilitiesManagerImpl.FACILITY_MAPPER),
					user.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<RichAttribute<User, Facility>>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names left join facility_attr_values on id=attr_id and facility_id=? where attr_name=?", new AttributeRowMapper(sess, this, facility), facility.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Facility attribute - attribute.name='" + attributeName + "'");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName) throws InternalErrorException {
		try {
			return jdbc.query("select attr_name from attr_names where attr_name like ? || '%'", ATTRIBUTE_NAMES_MAPPER, startingPartOfAttributeName);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<String>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where attr_name=?", new AttributeRowMapper(sess, this, vo), vo.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Vo attribute - attribute.name='" + attributeName + "'");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("group_attr_values") + " from attr_names left join group_attr_values on id=attr_id and group_id=? where attr_name=?", new AttributeRowMapper(sess, this, group), group.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Group attribute - attribute.name='" + attributeName + "'");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names left join resource_attr_values on id=attr_id and resource_id=? where attr_name=?", new AttributeRowMapper(sess, this, resource), resource.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Resource attribute - attribute.name='" + attributeName + "'");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			//member-resource attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, resource, member), resource.getId(), member.getId(), attributeName);


		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			//member-group attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where attr_name=?",
					new AttributeRowMapper(sess, this, member, group), group.getId(), member.getId(), attributeName);

		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		//member and member core attributes
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, member), member.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
					"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		//user and user core attributes
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, user), user.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
					"left join host_attr_values on id=attr_id and host_id=? where attr_name=?", new AttributeRowMapper(sess, this, host), host.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Host attribute - attribute.name='" + attributeName + "'");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
					"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, String key, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("entityless_attr_values") + " from attr_names " +
					"left join    entityless_attr_values     on id=entityless_attr_values.attr_id     and   subject=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, null), key, attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("user_ext_source_attr_values") + " from attr_names " +
					"left join user_ext_source_attr_values on id=attr_id and user_ext_source_id=? " +
					"where attr_name=?",
					new AttributeRowMapper(sess, this, ues), ues.getId(), attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + attributeDefinitionMappingSelectQuery + " from attr_names where attr_name=?", ATTRIBUTE_DEFINITION_MAPPER, attributeName);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute - attribute.name='" + attributeName + "'", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names", ATTRIBUTE_DEFINITION_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute definition exists.");
			return new ArrayList<AttributeDefinition>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + ", null as attr_value from attr_names where namespace=?", ATTRIBUTE_DEFINITION_MAPPER, namespace);
		} catch(EmptyResultDataAccessException ex) {
			log.debug("No attribute definition with namespace='{}' exists.", namespace);
			return new ArrayList<AttributeDefinition>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + attributeDefinitionMappingSelectQuery + " from attr_names where id=?", ATTRIBUTE_DEFINITION_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names left join facility_attr_values on id=attr_id and facility_id=? where id=?", new AttributeRowMapper(sess, this, facility), facility.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where id=?", new AttributeRowMapper(sess, this, vo), vo.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names left join resource_attr_values on id=attr_id and resource_id=? where id=?", new AttributeRowMapper(sess, this, resource), resource.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
					"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
					"left join group_attr_values grp on id=grp.attr_id and group_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, group), group.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Host host, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names left join host_attr_values on id=attr_id and host_id=? where id=?", new AttributeRowMapper(sess, this, host), host.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	public Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			//member-resource attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, member), resource.getId(), member.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			//member-group attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
					"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, member), group.getId(), member.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Member member, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			//member and member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, member), member.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
					"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, User user, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			//user and user core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
					"where id=?",
					new AttributeRowMapper(sess, this, user), user.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws InternalErrorException, AttributeNotExistsException {
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("ues") + " from attr_names left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=? where id=?", new AttributeRowMapper(sess, this, ues), ues.getId(), id);
		} catch(EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id +"\"", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean setAttribute(final PerunSession sess, final Object object, final Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		String tableName;
		String columnName;
		Object identificator;
		String namespace;

		// check whether the object is String or Perun Bean:
		if (object instanceof String) {
			tableName = "entityless_attr_values";
			columnName = "subject";
			identificator = (String) object;
			namespace = AttributesManager.NS_ENTITYLESS_ATTR;
		} else if (object instanceof PerunBean) {
			PerunBean bean = (PerunBean) object;
			// Add underscore between two letters where first is lowercase and second is uppercase, then lowercase BeanName
			String name = bean.getBeanName().replaceAll("(\\p{Ll})(\\p{Lu})","$1_$2").toLowerCase();
			// same behaviour for rich objects as for the simple ones -> cut off "rich_" prefix
			if (name.startsWith("rich")) {
				name = name.replaceFirst("rich_", "");
			}
			// get namespace of the perun bean
			namespace = NAMESPACES_BEANS_MAP.get(name);
			if (namespace == null) {
				// perun bean is not in the namespace map
				throw new InternalErrorException(new IllegalArgumentException("Setting attribute for perun bean " + bean + " is not allowed."));
			}
			tableName = name + "_attr_values";
			columnName = name + "_id";
			identificator = bean.getId();
		} else {
			throw new InternalErrorException(new IllegalArgumentException("Object " + object + " must be either String or PerunBean."));
		}

		// check that given object is consistent with the attribute
		checkNamespace(sess, attribute, namespace);

		// create map of parameters for the where clause of the SQL query
		Map<String,Object> params = new HashMap<>();
		params.put("attr_id", attribute.getId());
		params.put(columnName, identificator);

		// save attribute
		return setAttributeInDB(sess, attribute, tableName, params);
	}

	@Override
	public boolean setAttribute(final PerunSession sess, final PerunBean bean1, final PerunBean bean2, final Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		String tableName;
		String namespace;
		Integer identificator1;
		Integer identificator2;

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
		namespace = NAMESPACES_BEANS_MAP.get(name1 + "_" + name2);
		identificator1 = bean1.getId();
		identificator2 = bean2.getId();
		if (namespace == null) {
			// swap the names and beans and try again
			String nameTmp = name1;
			name1 = name2;
			name2 = nameTmp;
			identificator1 = bean2.getId();
			identificator2 = bean1.getId();
			namespace = NAMESPACES_BEANS_MAP.get(name1 + "_" + name2);
		}
		if (namespace == null) {
			// the combination of perun beans is not in the namespace map
			throw new InternalErrorException(new IllegalArgumentException("Setting attribute for perun bean " + bean1 + " and " + bean2 + " is not allowed."));
		}
		tableName = name1 + "_" + name2 + "_attr_values";

		// check that given object is consistent with the attribute
		checkNamespace(sess, attribute, namespace);

		// create map of parameters for the where clause of the SQL query
		Map<String,Object> params = new HashMap<>();
		params.put("attr_id", attribute.getId());
		params.put(name1 + "_id", identificator1);
		params.put(name2 + "_id", identificator2);

		// save attribute
		return setAttributeInDB(sess, attribute, tableName, params);
	}

	private boolean setAttributeInDB(final PerunSession sess, final Attribute attribute, final String tableName, final Map<String, Object> params) throws InternalErrorException {
		// get two sorted lists for parameter names and values
		List<String> columnNames = new ArrayList<>();
		List<Object> columnValues = new ArrayList<>();
		for (Entry entry: params.entrySet()) {
			columnNames.add((String) entry.getKey());
			columnValues.add(entry.getValue());
		}

		try {
			// deleting the attibute if the given attribute value is null
			if (attribute.getValue() == null) {
				int numAffected = jdbc.update("delete from " + tableName + " where " + buildParameters(columnNames, "=?", " and "), columnValues.toArray());
				if (numAffected > 1) {
					throw new ConsistencyErrorException(String.format("Too much rows to delete (" + numAffected + " rows). SQL: delete from " + tableName + " where " + buildParameters(columnNames, "=%s", " and "), columnValues.toArray()));
				}
				return numAffected == 1;
			}

			// set the column name according to the size of the attribute
			boolean largeAttribute = Utils.isLargeAttribute(sess, attribute);
			String valueColName = (largeAttribute ? "attr_value_text" : "attr_value");

			// if the DB value is the same as parameter, return
			if (!largeAttribute) {
				try {
					Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from " + tableName + " where " + buildParameters(columnNames, "=?", " and "), String.class, columnValues.toArray()), attribute.getType());
					if (attribute.getValue().equals(value)) {
						return false;
					}
				} catch (EmptyResultDataAccessException ex) {
					//This is ok. Attribute will be stored later.
				}
			}

			int repetatCounter = 0;
			while (true) {
				// number of values of this attribute value in db
				int numOfAttributesInDb = jdbc.queryForInt("select count(attr_id) from " + tableName + " where " + buildParameters(columnNames, "=?", " and "), columnValues.toArray());
				switch (numOfAttributesInDb) {
					case 0: {
						// value doesn't exist -> insert
						try {
							return self.insertAttribute(sess, valueColName, attribute, tableName, columnNames, columnValues);
						} catch (DataAccessException ex) {
							// unsuccessful insert, do it again in while loop
							if (++repetatCounter > MERGE_TRY_CNT) {
								throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
							}
							try {
								Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
							} catch (InterruptedException IGNORE) {
							}
						}
						break;
					}
					case 1: {
						// value exists -> update
						try {
							return self.updateAttribute(sess, valueColName, attribute, tableName, columnNames, columnValues);
						} catch (DataAccessException ex) {
							// unsuccessful insert, do it again in while loop
							if (++repetatCounter > MERGE_TRY_CNT) {
								throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
							}
							try {
								Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
							} catch (InterruptedException IGNORE) {
							}
						}
						break;
					}
					default:
						throw new ConsistencyErrorException(String.format("Attribute id " + attribute.getId() + " for " + tableName + " with parameters: " + buildParameters(columnNames, "=%s", " and ") + " is more than once in DB.", columnValues.toArray()));
				}
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Build string for purposes of SQL query with given parameters.
	 *
	 * @param params parameters to print
	 * @param afterParam string, which will be inserted after each parameter
	 * @param separator string, which will be inserted between parameters
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
	public boolean insertAttribute(PerunSession sess, String valueColName, Attribute attribute, String tableName, List<String> columnNames, List<Object> columnValues) throws InternalErrorException {
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

		try {
			int changed = jdbc.update("insert into " + tableName + " (" + buildParameters(columnNames, "", ", ") + ", " + valueColName + ", created_by, modified_by, created_by_uid, modified_by_uid, modified_at, created_at) "
					+ "values (" + questionMarks + Compatibility.getSysdate() + ", " + Compatibility.getSysdate() + " )", values.toArray());
			return changed > 0;
		} catch (DataAccessException ex) {
			throw ex;
		}
	}

	@Override
	public boolean updateAttribute(PerunSession sess, String valueColName, Attribute attribute, String tableName, List<String> columnNames, List<Object> columnValues) throws InternalErrorException {
		// add additional SQL values to the list
		List<Object> values = new ArrayList<>();
		values.add(BeansUtils.attributeValueToString(attribute)); // valueColName
		values.add(sess.getPerunPrincipal().getActor()); // modified_by
		values.add(sess.getPerunPrincipal().getUserId()); // modified_by_uid
		values.addAll(columnValues);
		try {
			int changed = jdbc.update("update " + tableName + " set " + valueColName + "=?, modified_by=?, modified_by_uid=?, modified_at=" +
					Compatibility.getSysdate() + " where " + buildParameters(columnNames, "=?", " and "), values.toArray());
		return changed > 0;
		} catch (DataAccessException ex) {
			throw ex;
		}
	}

	@Override
	public boolean setAttributeWithNullValue(final PerunSession sess, final String key, final Attribute attribute) throws InternalErrorException {
		try {
			jdbc.update("insert into entityless_attr_values (attr_id, subject, attr_value, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) "
									+ "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), key, null, null,
									sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			return true;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean setVirtualAttribute(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getFacilityVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, facility, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getMemberVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getResourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, resource, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, ModuleNotExistsException, WrongModuleTypeException, WrongReferenceAttributeValueException {
		return getFacilityUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getResourceGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getMemberGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member, group, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, user, attribute);
	}

	public boolean setVirtualAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
		return getUserExtSourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, ues, attribute);
	}

	public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeDefinitionExistsException {
		if (!attribute.getFriendlyName().matches(AttributesManager.ATTRIBUTES_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong attribute name " + attribute.getFriendlyName() + ", attribute name must match " + AttributesManager.ATTRIBUTES_REGEXP));
		}
		try {
			int attributeId = Utils.getNewId(jdbc, "attr_names_id_seq");

			jdbc.update("insert into attr_names (id, attr_name, type, dsc, namespace, friendly_name, display_name, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
					"values (?,?,?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					attributeId, attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.getNamespace(), attribute.getFriendlyName(), attribute.getDisplayName(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			attribute.setId(attributeId);
			log.info("Attribute created: {}", attribute);

			return attribute;
		} catch (DataIntegrityViolationException e) {
			throw new AttributeDefinitionExistsException("Attribute " + attribute.getName() + " already exists", attribute, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		//TODO prevest do BL?
		try {
			jdbc.update("delete from facility_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from resource_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from member_resource_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from user_facility_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from user_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from entityless_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from host_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from member_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from group_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from vo_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from group_resource_attr_values where attr_id=?", attribute.getId());
			jdbc.update("delete from attr_names where id=?", attribute.getId());

			log.info("Attribute deleted [{}]", attribute);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteAllAttributeAuthz(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from attributes_authz where attr_id=?", attribute.getId())) {
				log.info("All attribute_authz were deleted for Attribute={}.", attribute);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + attributeDefinitionMappingSelectQuery  + " from resource_services " +
					"join service_required_attrs on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=? " +
					"join attr_names on service_required_attrs.attr_id=attr_names.id",
					ATTRIBUTE_DEFINITION_MAPPER, resource.getId());

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None resource required attributes definitions found for resource: {}", resource);
			return new ArrayList<AttributeDefinition>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
					"left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=? " +
					"where namespace in (?,?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=? )",
					new AttributeRowMapper(sess, this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, resource.getId());

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for facility: {} and services from resource: {}.", facility, resource);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,  Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
					"left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=? " +
					"where namespace in (?,?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, resource), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services getted from it", resourceToGetServicesFrom);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("member_attr_values") + " from attr_names " +
					"left join member_attr_values on id=member_attr_values.attr_id and member_attr_values.member_id=? " +
					"where namespace in (?,?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, member), member.getId(), AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT, resourceToGetServicesFrom.getId());

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services getted from it", resourceToGetServicesFrom);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
					"where namespace in (?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, resource, member), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"left join      user_facility_attr_values    usr    on      attr_names.id=usr.attr_id    and   user_id=? and facility_id=? " +
					"where namespace in (?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, user, facility), user.getId(), facility.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT, resource.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
					"left join      group_resource_attr_values    grp   on      attr_names.id=grp.attr_id    and   grp.group_id=? and grp.resource_id=? " +
					"where namespace in (?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, group, resource), group.getId(), resource.getId(), AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, User user) throws InternalErrorException {
		try {
			//user and user core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"left join      user_attr_values    usr    on      attr_names.id=usr.attr_id    and   user_id=? " +
					"where namespace in (?,?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?) ",
					new AttributeRowMapper(sess, this, user), user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT, resource.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
					"left join      host_attr_values   host    on      attr_names.id=host.attr_id    and   host_id=? " +
					"where namespace in (?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, host), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT, resourceToGetServicesFrom.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
					"left join      group_attr_values   grp    on      attr_names.id=grp.attr_id    and   group_id=? " +
					"where namespace in (?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, group), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT, resourceToGetServicesFrom.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws InternalErrorException {
		try {
			return jdbc.query("select " + attributeDefinitionMappingSelectQuery + " from attr_names, service_required_attrs where id=attr_id and service_id=?", ATTRIBUTE_DEFINITION_MAPPER, service.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<AttributeDefinition>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=? " +
					"where namespace in (?,?,?,?)",
					new AttributeRowMapper(sess, this, facility), service.getId(), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT);

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for facility: {} and service: {}.", facility, service);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join vo_attr_values on id=vo_attr_values.attr_id and vo_id=? " +
					"where namespace in (?,?,?,?)",
					new AttributeRowMapper(sess, this, vo), service.getId(), vo.getId(), AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_CORE, AttributesManager.NS_VO_ATTR_OPT, AttributesManager.NS_VO_ATTR_VIRT);

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for vo: {} and service: {}.", vo, service);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=? " +
					"where namespace in (?,?,?,?)",
					new AttributeRowMapper(sess, this, resource), service.getId(), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT);

		} catch(EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and service {} ", resource, service);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, List<Integer> serviceIds) throws InternalErrorException {
		try {
			List<String> namespace = new ArrayList();
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_DEF);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_CORE);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_OPT);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_VIRT);

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("serviceIds", serviceIds);
			parameters.addValue("resourceId", resource.getId());
			parameters.addValue("namespace", namespace);

			return this.namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names "
					+ "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id in (:serviceIds) "
					+ "left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=:resourceId "
					+ "where namespace in (:namespace)",
					parameters, new AttributeRowMapper(sess, this, resource));

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services with id {} ", resource, serviceIds);
			return new ArrayList<Attribute>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +

					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +

					"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
					"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, resource, member), service.getId(), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, member), service.getId(), group.getId(), member.getId(), AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new AttributeRowMapper(sess, this, member, group), group.getId(), member.getId(), AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws InternalErrorException {
		//member and member core attributes
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
					"where namespace in (?,?,?,?)",
					new AttributeRowMapper(sess, this, member), service.getId(), member.getId(), AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
					"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, user, facility), service.getId(), facility.getId(), user.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join    group_resource_attr_values     grp_res     on id=grp_res.attr_id     and   group_id=? and resource_id=? " +
					"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, group, resource), service.getId(), group.getId(), resource.getId(), AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) throws InternalErrorException {
		//user and user core attributes
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
					"where namespace in (?,?,?,?)",
					new AttributeRowMapper(sess, this, user), service.getId(), user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
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
		 * @param sess perun session
		 * @param attributesManager attribute manager
		 * @param members list of members
		 */
		public MemberAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<Member> members) {
			this.sess = sess;
			this.attributesManager = attributesManager;
			this.members = members;
			this.resource = null;
		}

		/**
		 * Sets up parameters for data extractor
		 * For memberResource attributes we need also know the resource.
		 *
		 * @param sess perun session
		 * @param attributesManager attribute manager
		 * @param resource resource for member resource attributes
		 * @param members list of members
		 */
		public MemberAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, Resource resource, List<Member> members) {
			this.sess = sess;
			this.attributesManager = attributesManager;
			this.members = members;
			this.resource = resource;
		}

		public HashMap<Member, List<Attribute>> extractData(ResultSet rs) throws SQLException, DataAccessException {
			HashMap<Member, List<Attribute>> map = new HashMap<>();
			HashMap<Integer, Member> memberObjectMap = new HashMap<>();
		 	List<Attribute> memAttrs;

			for(Member member : members) {
				memberObjectMap.put(member.getId(), member);
			}

			while (rs.next()) {
				// fetch from map by ID
				Integer id = rs.getInt("id");
				Member mem = memberObjectMap.get(id);

				memAttrs = map.get(mem);
				if(memAttrs == null){
					// if not present, put in map
					memAttrs = new ArrayList<>();
					map.put(mem, memAttrs);
				}
				AttributeRowMapper attributeRowMapper;
				if(resource != null) {
					attributeRowMapper = new AttributeRowMapper(sess, attributesManager, resource, mem);
				} else {
					attributeRowMapper = new AttributeRowMapper(sess, attributesManager, mem);
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

	private static class UserAttributeExtractor implements ResultSetExtractor<HashMap<User, List<Attribute>>> {
		private final PerunSession sess;
		private final AttributesManagerImpl attributesManager;
		private final List<User> users;
		private final Facility facility;

		/**
		 * Sets up parameters for data extractor
		 *
		 * @param sess perun session
		 * @param attributesManager attribute manager
		 * @param users list of users
		 */
		public UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users, Facility facility) {
			this.sess = sess;
			this.attributesManager = attributesManager;
			this.users = users;
			this.facility = facility;
		}

		public UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users) {
			this(sess, attributesManager, users, null);
		}

		public HashMap<User, List<Attribute>> extractData(ResultSet rs) throws SQLException, DataAccessException {
			HashMap<User, List<Attribute>> map = new HashMap<>();
			HashMap<Integer, User> userObjectMap = new HashMap<>();
			List<Attribute> userAttrs;

			for(User user : users) {
				userObjectMap.put(user.getId(), user);
			}

			while (rs.next()) {
				// fetch from map by ID
				Integer id = rs.getInt("id");
				User user = userObjectMap.get(id);

				userAttrs = map.get(user);
				if(userAttrs == null){
					// if not preset, put in map
					userAttrs = new ArrayList<>();
					map.put(user, userAttrs);
				}

				AttributeRowMapper attributeRowMapper = new AttributeRowMapper(sess, attributesManager, user, facility);
				Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

				if (attribute != null) {
					// add only if exists
					map.get(user).add(attribute);
				}
			}
			return map;
		}
	}

	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException {
		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN members ON " + BeansUtils.prepareInSQLClause("members.id", members) +
							"LEFT JOIN member_resource_attr_values mem ON attr_names.id=mem.attr_id AND mem.resource_id=? " +
							"AND mem.member_id=members.id WHERE namespace IN (?,?,?)",
					new MemberAttributeExtractor(sess, this, resource, members), service.getId(), resource.getId(),
					AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException {
		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN members ON " + BeansUtils.prepareInSQLClause("members.id", members) +
							"LEFT JOIN member_attr_values mem ON attr_names.id=mem.attr_id " +
							"AND mem.member_id=members.id WHERE namespace IN (?,?,?,?)",
					new MemberAttributeExtractor(sess, this, members), service.getId(),
					AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new HashMap<Member, List<Attribute>>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException {
		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("usr_fac") + ", users.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN users ON " + BeansUtils.prepareInSQLClause("users.id", users) +
							"LEFT JOIN user_facility_attr_values usr_fac ON attr_names.id=usr_fac.attr_id AND facility_id=? AND user_id=users.id " +
							"WHERE namespace IN (?,?,?)",
					new UserAttributeExtractor(sess, this, users, facility), service.getId(), facility.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new HashMap<User, List<Attribute>>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException {
		//user and user core attributes
		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("usr") + ", users.id FROM attr_names " +
							"JOIN service_required_attrs on attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN users ON " + BeansUtils.prepareInSQLClause("users.id", users) +
							"LEFT JOIN user_attr_values usr ON attr_names.id=usr.attr_id AND user_id=users.id " +
							"WHERE namespace IN (?,?,?,?)",
					new UserAttributeExtractor(sess, this, users), service.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT);
		} catch(EmptyResultDataAccessException ex) {
			return new HashMap<User, List<Attribute>>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join      host_attr_values    host   on      id=host.attr_id    and   host_id=? " +
					"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, host), service.getId(), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
					"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
					"left join      group_attr_values    grp   on      id=grp.attr_id    and  group_id=? " +
					"where namespace in (?,?,?)",
					new AttributeRowMapper(sess, this, group), service.getId(), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
					"left join facility_attr_values on attr_names.id=facility_attr_values.attr_id and facility_attr_values.facility_id=? " +
					"where namespace in (?,?,?,?) " +
					"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
					"join resources on resource_services.resource_id=resources.id  and resources.facility_id=?)",
					new AttributeRowMapper(sess, this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, facility.getId());
		} catch(EmptyResultDataAccessException ex) {
			log.info("None required attributes found for facility: {} and services from it's resources.", facility);
			return new ArrayList<Attribute>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no attribute module for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		ResourceMemberAttributesModuleImplApi attributeModule = getResourceMemberAttributeModule(sess, attribute);
		if(attributeModule == null) {
				log.debug("fillAttribute - There's no attribute module for this attribute. Attribute wasn't filled. Attribute={}", attribute);
				return attribute;
			}

			try {return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, member, attribute);
			} catch(WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		MemberGroupAttributesModuleImplApi attributeModule = getMemberGroupAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no attribute module for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, member, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		FacilityUserAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no attribute module for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, facility, user, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException {
		UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException {
		MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException {
		ResourceGroupAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException {
		HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, host, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException {
		GroupAttributesModuleImplApi attributeModule = getGroupAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException {
		UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
		if(attributeModule == null) {
			log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, ues, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
		if(facilityModule == null) return; //facility module doesn't exists
		try {
			facilityModule.changedAttributeHook((PerunSessionImpl) sess,facility, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		EntitylessAttributesModuleImplApi entitylessModule = getEntitylessAttributeModule(sess, attribute);
		if(entitylessModule == null) return; //facility module doesn't exists
		try {
			entitylessModule.changedAttributeHook((PerunSessionImpl) sess, key, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		HostAttributesModuleImplApi hostModule = getHostAttributeModule(sess, attribute);
		if(hostModule == null) return; //host module doesn't exists
		try {
			hostModule.changedAttributeHook((PerunSessionImpl) sess, host, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
		if(voModule == null) return; //facility module doesn't exists
		try {
			voModule.changedAttributeHook((PerunSessionImpl) sess, vo, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
		if(groupModule == null) return; //facility module doesn't exists
		try {
			groupModule.changedAttributeHook((PerunSessionImpl) sess, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		UserAttributesModuleImplApi userModule = getUserAttributeModule(sess, attribute);
		if(userModule == null) return; //facility module doesn't exists
		try {
			userModule.changedAttributeHook((PerunSessionImpl) sess, user, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberAttributesModuleImplApi memberModule = getMemberAttributeModule(sess, attribute);
		if(memberModule == null) return; //facility module doesn't exists
		try {
			memberModule.changedAttributeHook((PerunSessionImpl) sess, member, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceGroupAttributesModuleImplApi resourceGroupModule = getResourceGroupAttributeModule(sess, attribute);
		if(resourceGroupModule == null) return; //facility module doesn't exists
		try {
			resourceGroupModule.changedAttributeHook((PerunSessionImpl) sess, resource, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceAttributesModuleImplApi resourceModule = getResourceAttributeModule(sess, attribute);
		if(resourceModule == null) return; //facility module doesn't exists
		try {
			resourceModule.changedAttributeHook((PerunSessionImpl) sess, resource, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceMemberAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
		if(resourceMemberGroupModule == null) return; //facility module doesn't exists
		try {
			resourceMemberGroupModule.changedAttributeHook((PerunSessionImpl) sess, resource, member, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberGroupAttributesModuleImplApi memberGroupAttributesModule = getMemberGroupAttributeModule(sess, attribute);
		if(memberGroupAttributesModule == null) return; //memberGroupAttributesModule module doesn't exists
		try {
			memberGroupAttributesModule.changedAttributeHook((PerunSessionImpl) sess, member, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityUserAttributesModuleImplApi facilityUserModule = getFacilityUserAttributeModule(sess, attribute);
		if(facilityUserModule == null) return; //facility module doesn't exists
		try {
			facilityUserModule.changedAttributeHook((PerunSessionImpl) sess, facility, user, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void changedAttributeHook(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		UserExtSourceAttributesModuleImplApi uesModule = getUserExtSourceAttributeModule(sess, attribute);
		if(uesModule == null) return;
		try {
			uesModule.changedAttributeHook((PerunSessionImpl) sess, ues, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
		if(facilityModule == null) return; //facility module doesn't exists
		try {
			facilityModule.checkAttributeValue((PerunSessionImpl) sess, facility, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
		if(voModule == null) return; //module doesn't exists
		try {
			voModule.checkAttributeValue((PerunSessionImpl) sess, vo, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,WrongReferenceAttributeValueException {
		//Call attribute module
		GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
		if(groupModule == null) return; //module doesn't exists
		try {
			groupModule.checkAttributeValue((PerunSessionImpl) sess, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException  {
		ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, resource, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		for(Attribute attribute : attributes) {
			checkAttributeValue(sess, resource, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceMemberAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
		if(resourceMemberGroupModule == null) return; //facility module doesn't exists
		try {
			resourceMemberGroupModule.checkAttributeValue((PerunSessionImpl) sess, resource, member, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberGroupAttributesModuleImplApi memberGroupAttributeModule = getMemberGroupAttributeModule(sess, attribute);
		if(memberGroupAttributeModule == null) return; //memberGroupAttributesModule module doesn't exists
		try {
			memberGroupAttributeModule.checkAttributeValue((PerunSessionImpl) sess, member, group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		FacilityUserAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, ues, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, host, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		ResourceGroupAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, resource,group, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkAttributeValue(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		EntitylessAttributesModuleImplApi attributeModule = getEntitylessAttributeModule(sess, attribute);
		if(attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, key, attribute);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from entityless_attr_values where attr_id=? and subject=?", attribute.getId(), key)) {
				log.info("Attribute (its value) with key was removed from entityless attributes. Attribute={}, key={}.", attribute, key);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from group_resource_attr_values where resource_id=?", resource.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from member_resource_attr_values where resource_id=?", resource.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from facility_attr_values where attr_id=? and facility_id=?", attribute.getId(), facility.getId())) {
				log.info("Attribute (its value) was removed from facility. Attribute={}, facility={}.", attribute, facility);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from facility_attr_values where facility_id=?", facility.getId())) {
				log.info("All attributes (theirs values) were removed from facility. Facility={}.", facility);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from vo_attr_values where attr_id=? and vo_id=?", attribute.getId(), vo.getId())) {
				log.info("Attribute (its value) was removed from vo. Attribute={}, vo={}.", attribute, vo);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from vo_attr_values where vo_id=?", vo.getId())) {
				log.info("All attributes (theirs values) were removed from vo. Vo={}.", vo);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from group_attr_values where attr_id=? and group_id=?", attribute.getId(), group.getId())) {
				log.info("Attribute (its value) was removed from group. Attribute={}, group={}.", attribute, group);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from group_attr_values where group_id=?", group.getId())) {
				log.info("All attributes (theirs values) were removed from group. Group={}.", group);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from resource_attr_values where attr_id=? and resource_id=?", attribute.getId(), resource.getId())) {
				log.info("Attribute (its value) was removed from resource. Attribute={}, resource={}.", attribute, resource);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from resource_attr_values where resource_id=?", resource.getId())) {
				log.info("All attributes (theirs values) were removed from resource. Resource={}.", resource);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_resource_attr_values where attr_id=? and member_id=? and resource_id=?", attribute.getId(), member.getId(), resource.getId())) {
				log.info("Attribute (its value) was removed from member on resource. Attribute={}, member={}, resource=" + resource, attribute, member);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_resource_attr_values where resource_id=? and member_id=?", resource.getId(), member.getId())) {
				log.info("All attributes (theirs values) were removed from member on resource. Member={}, resource={}.", member, resource);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_group_attr_values where attr_id=? and member_id=? and group_id=?", attribute.getId(), member.getId(), group.getId())) {
				log.info("Attribute (its value) was removed from member in group. Attribute={}, member={}, group=" + group, attribute, member);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_group_attr_values where group_id=? and member_id=?", group.getId(), member.getId())) {
				log.info("All attributes (theirs values) were removed from member in group. Member={}, group={}.", member, group);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_attr_values where attr_id=? and member_id=?", attribute.getId(), member.getId())) {
				log.info("Attribute (its value) was removed from member. Attribute={}, member={}", attribute, member);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from member_attr_values where member_id=?", member.getId())) {
				log.info("All attributes (their values) were removed from member. Member={}", member);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_facility_attr_values where attr_id=? and user_id=? and facility_id=?", attribute.getId(), user.getId(), facility.getId())) {
				log.info("Attribute (its value) was removed from user on facility. Attribute={}, user={}, facility=" + facility, attribute, user);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_facility_attr_values where user_id=? and facility_id=?", user.getId(), facility.getId())) {
				log.info("All attributes (theirs values) were removed from user on facility. User={}, facility={}", user, facility);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_facility_attr_values where facility_id=?", facility.getId())) {
				log.info("All attributes (theirs values) were removed from any user on facility. Facility={}", facility);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllUserFacilityAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_facility_attr_values where user_id=?", user.getId())) {
				log.info("All attributes (theirs values) were removed from user on  all facilities. User={}", user);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeVirtualAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		return getFacilityUserVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
	}

	public boolean removeVirtualAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return getResourceVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, attribute);
	}

	public boolean removeVirtualAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return getResourceGroupVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
	}

	public boolean removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_attr_values where attr_id=? and user_id=?", attribute.getId(), user.getId())) {
				log.info("Attribute (its value) was removed from user. Attribute={}, user={}", attribute, user);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_attr_values where user_id=?", user.getId())) {
				log.info("All attributes (their values) were removed from user. User={}", user);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	public boolean removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from group_resource_attr_values where attr_id=? and resource_id=? and group_id=?", attribute.getId(),resource.getId(),group.getId())) {
				log.info("Attribute (its value) was removed from group on resource. Attribute={}, group={}, resource=" + attribute, group, resource);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from group_resource_attr_values where group_id=? and resource_id=?", group.getId(), resource.getId())) {
				log.info("All attributes (theirs values) were removed from group on resource. Group={}, Resource={}", group, resource);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	public boolean removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from host_attr_values where attr_id=? and host_id=?", attribute.getId(), host.getId())) {
				log.info("Attribute (its value) was removed from host. Attribute={}, host={}", attribute, host);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Host host) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from host_attr_values where host_id=?", host.getId())) {
				log.info("All attributes (their values) were removed from host. Host={}", host);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_ext_source_attr_values where attr_id=? and user_ext_source_id=?", attribute.getId(), ues.getId())) {
				log.info("Attribute (its value) was removed from user external source. Attribute={}, UserExtSource={}", attribute, ues);
				return true;
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		try {
			if(0 < jdbc.update("delete from user_ext_source_attr_values where user_ext_source_id=?", ues.getId())) {
				log.info("All attributes (their values) were removed from user external source. UserExtSource={}", ues);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean attributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Utils.notNull(attribute, "attribute");
		Utils.notNull(attribute.getName(), "attribute.name");
		Utils.notNull(attribute.getNamespace(), "attribute.namespace");
		Utils.notNull(attribute.getType(), "attribute.type");

		try {
			return 1 == jdbc.queryForInt("select count('x') from attr_names where attr_name=? and friendly_name=? and namespace=? and id=? and type=?", attribute.getName(), attribute.getFriendlyName(), attribute.getNamespace(), attribute.getId(), attribute.getType());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean actionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException {
		Utils.notNull(actionType, "actionType");
		Utils.notNull(actionType.getActionType(), "actionType.actionType");

		try {
			return 1 == jdbc.queryForInt("select count('x') from action_types where action_type=?", actionType.getActionType());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void checkActionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException, ActionTypeNotExistsException {
		if(!actionTypeExists(sess, actionType)) throw new ActionTypeNotExistsException("ActionType: " + actionType);
	}

	public void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeNotExistsException {
		if(!attributeExists(sess, attribute)) throw new AttributeNotExistsException("Attribute: " + attribute);
	}

	public void checkAttributeExists(PerunSession sess, AttributeDefinition attribute, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		checkAttributeExists(sess, attribute);
		checkNamespace(sess, attribute, expectedNamespace);
	}

	public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotExistsException {
		Utils.notNull(attributes, "attributes");
		for(AttributeDefinition attribute : attributes) {
			checkAttributeExists(sess, attribute);
		}
	}

	public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.notNull(attributes, "attributes");
		for(AttributeDefinition attribute : attributes) {
			checkAttributeExists(sess, attribute, expectedNamespace);
		}
	}

	public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":core");
	}

	public boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute) {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":def");
	}

	public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":opt");
	}

	public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().matches("urn:perun:[^:]+:attribute-def:core-managed:[a-zA-Z]+Manager");
	}

	public boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute) {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":virt");
	}

	public boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace)  {
		if(attribute == null) throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if(attribute.getNamespace() == null) throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().startsWith(namespace + ":") || attribute.getNamespace().equals(namespace);
	}

	public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException {
		if(!isFromNamespace(sess, attribute, namespace)) throw new WrongAttributeAssignmentException(attribute);
	}

	public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException {
		for(AttributeDefinition attribute : attributes) {
			checkNamespace(sess, attribute, namespace);
		}
	}

	public List<Object> getAllResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return jdbc.query("select attr_value from resource_attr_values where attr_id=?", new ValueRowMapper(sess, this, attributeDefinition), attributeDefinition.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Object>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Object> getAllGroupResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return jdbc.query("select attr_value from group_resource_attr_values where attr_id=?", new ValueRowMapper(sess, this, attributeDefinition), attributeDefinition.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Object>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Object> getAllGroupValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return jdbc.query("select attr_value from group_attr_values where attr_id=?", new ValueRowMapper(sess, this, attributeDefinition), attributeDefinition.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Object>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean isAttributeRequiredByFacility(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
					"join resources on resource_services.resource_id=resources.id " +
					"where service_required_attrs.attr_id=? and resources.facility_id=?",
					attributeDefinition.getId(), facility.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean isAttributeRequiredByVo(PerunSession sess, Vo vo, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
					"join resources on resource_services.resource_id=resources.id " +
					"where service_required_attrs.attr_id=? and resources.vo_id=?",
					attributeDefinition.getId(), vo.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean isAttributeRequiredByGroup(PerunSession sess, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
					"join groups_resources on resource_services.resource_id=groups_resources.resource_id " +
					"where service_required_attrs.attr_id=? and groups_resources.group_id=?",
					attributeDefinition.getId(), group.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean isAttributeRequiredByResource(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=? " +
					"where service_required_attrs.attr_id=?",
					resource.getId(), attributeDefinition.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Escapes LIST_DELIMITER from the value.
	 *
	 * @param value
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
	 * @param value
	 * @return escaped value
	 */
	public static String escapeMapAttributeValue(String value) {
		if (value == null) {
			return null;
		}

		value = value.replace("\\", "\\\\");   //escape char '\'
		value = value.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
		value = value.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER

		return value;
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
	 * Get Group attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance group attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private GroupAttributesModuleImplApi getGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof GroupAttributesModuleImplApi) {
			return (GroupAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	/**
	 * Get Host attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance host attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private HostAttributesModuleImplApi getHostAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof HostAttributesModuleImplApi) {
			return (HostAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't HostAttributesModule");
		}
	}

	/**
	 * Get Vo attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance vo attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private VoAttributesModuleImplApi getVoAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof VoAttributesModuleImplApi) {
			return (VoAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	/**
	 * Get resource member attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance resource member attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private ResourceMemberAttributesModuleImplApi getResourceMemberAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof ResourceMemberAttributesModuleImplApi) {
			return (ResourceMemberAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	/**
	 * Get member-group attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance member group attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private MemberGroupAttributesModuleImplApi getMemberGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof MemberGroupAttributesModuleImplApi) {
			return (MemberGroupAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't MemberGroupAttributesModule");
		}
	}

	/**
	 * Get facility attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance facility attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws WrongModuleTypeException
	 * @throws InternalErrorException
	 */
	private FacilityAttributesModuleImplApi getFacilityAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof FacilityAttributesModuleImplApi) {
			return (FacilityAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	private EntitylessAttributesModuleImplApi getEntitylessAttributeModule(PerunSession sess, AttributeDefinition attribute) throws WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof EntitylessAttributesModuleImplApi) {
			return (EntitylessAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't EntitylessAttributesModuleImplApi");
		}
	}

	/**
	 * Get facility virtual attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance facility attribute module
	 *
	 * @throws InternalErrorException
	 * @throws WrongModuleTypeException
	 * @throws ModuleNotExistsException
	 */
	private FacilityVirtualAttributesModuleImplApi getFacilityVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if(attributeModule instanceof FacilityVirtualAttributesModuleImplApi) {
			return (FacilityVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityVirtualAttributesModule");
		}
	}

	/**
	 * Get resource virtual attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance of resource attribute module
	 *
	 * @throws InternalErrorException
	 * @throws WrongModuleTypeException
	 * @throws ModuleNotExistsException
	 */
	private ResourceVirtualAttributesModuleImplApi getResourceVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if(attributeModule instanceof ResourceVirtualAttributesModuleImplApi) {
			return (ResourceVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't ResourceVirtualAttributesModule");
		}
	}


	@Override
	public UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if(attributeModule instanceof UserVirtualAttributesModuleImplApi) {
			return (UserVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't UserVirtualAttributesModule");
		}
	}

	/**
	 * Get member virtual attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance of member attribute module
	 *
	 * @throws InternalErrorException
	 * @throws WrongModuleTypeException
	 * @throws ModuleNotExistsException
	 */
	private MemberVirtualAttributesModuleImplApi getMemberVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if(attributeModule instanceof MemberVirtualAttributesModuleImplApi) {
			return (MemberVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't MemberVirtualAttributesModule");
		}
	}

	/**
	 * Get user-facility attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance user-facility attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	public FacilityUserAttributesModuleImplApi getFacilityUserAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof FacilityUserAttributesModuleImplApi) {
			return (FacilityUserAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't FacilityUserAttributesModule");
		}
	}

	/**
	 * Get user-facility virtual attribute module for the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance user-facility attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 * @throws WrongModuleTypeException
	 * @throws ModuleNotExistsException
	 */
	private FacilityUserVirtualAttributesModuleImplApi getFacilityUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if(attributeModule instanceof FacilityUserVirtualAttributesModuleImplApi) {
			return (FacilityUserVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityUserVirtualAttributesModule");
		}
	}

	/**
	 * Get user attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance userattribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private UserAttributesModuleImplApi getUserAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof UserAttributesModuleImplApi) {
			return (UserAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserAttributesModule. " + attribute);
		}
	}

	/**
	 * Get member attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance userattribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private MemberAttributesModuleImplApi getMemberAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof MemberAttributesModuleImplApi) {
			return (MemberAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't MemberAttributesModule. " + attribute);
		}
	}

	/**
	 * Get resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance resource attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private ResourceAttributesModuleImplApi getResourceAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof ResourceAttributesModuleImplApi) {
			return (ResourceAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceAttributesModule. " + attribute);
		}
	}

	/**
	 * Get group_resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance resource attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private ResourceGroupAttributesModuleImplApi getResourceGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;
		if(attributeModule instanceof ResourceGroupAttributesModuleImplApi) {
			return (ResourceGroupAttributesModuleImplApi) attributeModule;

		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceGroupAttributesModule");
		}
	}

	/**
	 * Get user external source attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance user ext source attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private UserExtSourceAttributesModuleImplApi getUserExtSourceAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof UserExtSourceAttributesModuleImplApi) {
			return (UserExtSourceAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserExtSourceAttributesModule. " + attribute);
		}
	}

	/**
	 * Get group-resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance group-resource attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	public ResourceGroupVirtualAttributesModuleImplApi getResourceGroupVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof ResourceGroupVirtualAttributesModuleImplApi) {
			return (ResourceGroupVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceGroupVirtualAttributesModule");
		}
	}

	/**
	 * Get member-resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance member-resource attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	public ResourceMemberVirtualAttributesModuleImplApi getResourceMemberVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof ResourceMemberVirtualAttributesModuleImplApi) {
			return (ResourceMemberVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceMemberVirtualAttributesModule");
		}
	}

	/**
	 * Get member-group attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance member-group attribute module null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	public MemberGroupVirtualAttributesModuleImplApi getMemberGroupVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof MemberGroupVirtualAttributesModuleImplApi) {
			return (MemberGroupVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't MemberGroupVirtualAttributesModuleImplApi");
		}
	}

	/**
	 * Get UserExtSource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance UserExtSource attribute module
	 *         null if the module doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private UserExtSourceVirtualAttributesModuleImplApi getUserExtSourceVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if(attributeModule == null) return null;

		if(attributeModule instanceof UserExtSourceVirtualAttributesModuleImplApi) {
			return (UserExtSourceVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserExtSourceVirtualAttributesModule. " + attribute);
		}
	}

	/**
	 * Get the attribute module for the attribute
	 *
	 * @param attribute get the attribute module for this attribute
	 * @see AttributesManagerImpl#getAttributesModule(cz.metacentrum.perun.core.api.PerunSession, String)
	 */
	public Object getAttributesModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		String moduleName = null;
		//first try to find specific module including parameter of attribute (full friendly name)
		if(!attribute.getFriendlyName().equals(attribute.getBaseFriendlyName())) {
			moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getFriendlyName());
			Object attributeModule = getAttributesModule(sess, moduleName);
			if(attributeModule != null) return attributeModule;
		}

		//if specific module not exists or attribute has no parameter, find the common one
		moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName());
		Object attributeModule = getAttributesModule(sess, moduleName);
		if(attributeModule == null) log.debug("Attribute module not found. Module name={}", moduleName);
		return attributeModule;
	}

	/**
	 * Get the atributeModule
	 *
	 * @param moduleName name of the module
	 * @return instance of attribute module
	 *         null if attribute doesn't exists
	 *
	 * @throws InternalErrorException
	 */
	private Object getAttributesModule(PerunSession sess, String moduleName) throws InternalErrorException {
		//try to get already loaded module.
		if(attributesModulesMap.containsKey(moduleName)) return attributesModulesMap.get(moduleName);

		try {
			Class<?> moduleClass = classLoader.loadClass(moduleName);
			log.debug("Attribute module found. Module class={}  Module name={}", moduleClass, moduleName);
			Object module =  moduleClass.newInstance();
			attributesModulesMap.put(moduleName, (AttributesModuleImplApi) module);
			return module;
		} catch(ClassNotFoundException ex) {
			//attrribute module don't exist
			return null;
		} catch(InstantiationException ex) {
			throw new InternalErrorException("Attribute module " + moduleName + " cannot be instaciated.", ex);
		} catch(IllegalAccessException ex) {
			throw new InternalErrorException(ex);
		}

	}

	protected void initialize() throws InternalErrorException {
		log.debug("AttributesManagerImpl initialize started.");

		//Get PerunSession
		//String attributesManagerInitializator = "attributesManagerInitializator";
		//PerunPrincipal pp = new PerunPrincipal(attributesManagerInitializator, ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		//PerunSession sess = perun.getPerunSession(pp);
		//load all attributes modules

		ServiceLoader<AttributesModuleImplApi> attributeModulesLoader = ServiceLoader.load(AttributesModuleImplApi.class);
		for(AttributesModuleImplApi module : attributeModulesLoader) {
			attributesModulesMap.put(module.getClass().getName(), module);
			if(module instanceof VirtualAttributesModuleImplApi) {
				Auditer.registerAttributeModule((VirtualAttributesModuleImplApi) module);
			}
			log.debug("Module " + module.getClass().getSimpleName() + " loaded.");
		}


		Utils.notNull(jdbc, "jdbc");

		//check if all core atributes exists, create it doesn't
		List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();

		//Facility.id
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Facility id");
		attributes.add(attr);

		//Facility.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Facility name");
		attributes.add(attr);

		//Resource.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Resource id");
		attributes.add(attr);

		//Resource.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Resource name");
		attributes.add(attr);

		//Resource.description
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("description");
		attr.setDisplayName("Resource description");
		attributes.add(attr);

		//Member.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Member id");
		attributes.add(attr);

		//User.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("User id");
		attributes.add(attr);

		//User.firstName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("firstName");
		attr.setDisplayName("User first name");
		attributes.add(attr);

		//User.lastName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lastName");
		attr.setDisplayName("User last name");
		attributes.add(attr);

		//User.middleName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("middleName");
		attr.setDisplayName("User middle name");
		attributes.add(attr);

		//User.titleBefore
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("titleBefore");
		attr.setDisplayName("User title before");
		attributes.add(attr);

		//User.titleAfter
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("titleAfter");
		attr.setDisplayName("User title after");
		attributes.add(attr);

		//User.serviceUser
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(Boolean.class.getName());
		attr.setFriendlyName("serviceUser");
		attr.setDisplayName("If user is service user or not.");
		attributes.add(attr);

		//Group.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Group id");
		attributes.add(attr);

		//Group.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Group full name");
		attributes.add(attr);

		//Group.description
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("description");
		attr.setDisplayName("Group description");
		attributes.add(attr);

		//Group.parentGroupId
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("parentGroupId");
		attr.setDisplayName("Id of group's parent group.");
		attributes.add(attr);

		//Vo.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Vo id");
		attributes.add(attr);

		//Vo.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Vo full name");
		attributes.add(attr);

		//Vo.createdAt
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("createdAt");
		attr.setDisplayName("Vo created date");
		attributes.add(attr);

		//Vo.shortName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("shortName");
		attr.setDisplayName("Vo short name");
		attributes.add(attr);

		//Host.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Host id");
		attributes.add(attr);

		//Host.hostname
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("hostname");
		attr.setDisplayName("Host hostname");
		attributes.add(attr);


		// *** Def attributes

		//urn:perun:group:attribute-def:def:groupExtSource
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupExtSource");
		attr.setDisplayName("Group extSource");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:groupMembersExtSource
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupMembersExtSource");
		attr.setDisplayName("Group members extSource");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:groupMembersQuery
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupMembersQuery");
		attr.setDisplayName("Group members query");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:synchronizatinEnabled
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("synchronizationEnabled");
		attr.setDisplayName("Group synchronization enabled");
		attr.setDescription("Enables group synchronization from external source.");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:synchronizationInterval
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("synchronizationInterval");
		attr.setDisplayName("Synchronization interval");
		attr.setDescription("Time between two successful synchronizations.");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:lastSynchronizationState
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setDescription("If group is synchronized, there will be information about state of last synchronization.");
		attr.setFriendlyName("lastSynchronizationState");
		attr.setDisplayName("Last synchronization state");
		attributes.add(attr);

		//urn:perun:group:attribute-def:def:lastSynchronizationTimestamp
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setDescription("If group is synchronized, there will be the last timestamp of group synchronization.");
		attr.setFriendlyName("lastSynchronizationTimestamp");
		attr.setDisplayName("Last Synchronization timestamp");
		attributes.add(attr);

		if(perun.isPerunReadOnly()) log.debug("Loading attributes manager init in readOnly version.");

		for(AttributeDefinition attribute : attributes) {
			if(!checkAttributeExistsForInitialize(attribute)) {
				if(perun.isPerunReadOnly()) {
					throw new InternalErrorException("There is missing required attribute " + attribute + " and can't be created because this instance is read only.");
				} else {
					self.createAttributeExistsForInitialize(attribute);
				}
			}
		}
		log.debug("AttributesManagerImpl initialize ended.");
	}

	private boolean checkAttributeExistsForInitialize(AttributeDefinition attribute) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select count('x') from attr_names where attr_name=? and friendly_name=? and namespace=? and type=?",
					attribute.getName(), attribute.getFriendlyName(), attribute.getNamespace(), attribute.getType());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void createAttributeExistsForInitialize(AttributeDefinition attribute) throws InternalErrorException {
		try {
			int attributeId = Utils.getNewId(jdbc, "attr_names_id_seq");

			jdbc.update("insert into attr_names (id, attr_name, type, dsc, namespace, friendly_name, display_name, default_attr_id) values (?,?,?,?,?,?,?,NULL)",
					attributeId, attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.getNamespace(), attribute.getFriendlyName(), attribute.getDisplayName());
			log.info("Attribute created during initialization of attributesManager: {}", attribute);
		} catch (DataIntegrityViolationException e) {
			throw new ConsistencyErrorException("Attribute already exists: " + attribute, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public void setSelf(AttributesManagerImplApi self) {
		this.self = self;
	}

	public AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			Map<String, Object> map = jdbc.queryForMap("select attr_name, friendly_name, namespace, type, dsc, display_name from attr_names where id=?", attributeDefinition.getId());

			if (!attributeDefinition.getDescription().equals(map.get("dsc"))) {
				jdbc.update("update attr_names set dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", attributeDefinition.getDescription(), perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());
			}

			// if stored value was null and new isn't, update
			// if values not null && not equals, update
			if ((map.get("display_name") == null &&  attributeDefinition.getDisplayName() != null) ||
					(map.get("display_name") != null && !map.get("display_name").equals(attributeDefinition.getDisplayName()))) {
				jdbc.update("update attr_names set display_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", attributeDefinition.getDisplayName(), perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());
					}

			return attributeDefinition;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException("Updating non existing attributeDefinition", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Result Set Extractor for AttributeRights object
	 */
	private class AttributeRightsExtractor implements ResultSetExtractor<List<AttributeRights>> {

		private int attributeId;

		public AttributeRightsExtractor(int attributeId) {
			this.attributeId = attributeId;
		}

		public List<AttributeRights> extractData(ResultSet rs) throws SQLException, DataAccessException {

			Map<Role, List<ActionType>> map = new HashMap<>();

			while (rs.next()) {

				Role role = Role.valueOf(rs.getString("role_name").toUpperCase());
				ActionType actionType = ActionType.valueOf(rs.getString("action_type").toUpperCase());

				if (map.get(role) != null) {
					map.get(role).add(actionType);
				} else {
					map.put(role, new ArrayList<>(Arrays.asList(actionType)));
				}

			}

			List<AttributeRights> rights = new ArrayList<>();
			for (Role r : map.keySet()) {
				rights.add(new AttributeRights(attributeId, r, map.get(r)));
			}

			return rights;

		}
	}

	@Override
	public List<AttributeRights> getAttributeRights(PerunSession sess, final int attributeId) throws InternalErrorException {

		List<AttributeRights> rights = null;
		try {
			rights = jdbc.query("select " + attributeRightSelectQuery + " from attributes_authz join roles on "
					+ "attributes_authz.role_id=roles.id join action_types on attributes_authz.action_type_id=action_types.id where "
					+ "attributes_authz.attr_id=?", new AttributeRightsExtractor(attributeId), attributeId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		// set also empty rights for other roles (not present in DB)

		boolean roleExists;

		List<Role> listOfRoles = new ArrayList<Role>();
		listOfRoles.add(Role.FACILITYADMIN);
		listOfRoles.add(Role.GROUPADMIN);
		listOfRoles.add(Role.SELF);
		listOfRoles.add(Role.VOADMIN);

		for (Role roleToTry : listOfRoles) {
			roleExists = false;

			Iterator itr = rights.iterator();
			while ((itr.hasNext()) && (!roleExists)) {
				AttributeRights right = (AttributeRights) itr.next();
				if (right.getRole().equals(roleToTry)) {
					roleExists = true;
				}
			}
			if (!roleExists) {
				rights.add(new AttributeRights(attributeId, roleToTry, new ArrayList<ActionType>()));
			}
		}

		return rights;

	}

	@Override
	public void setAttributeRight(PerunSession sess, AttributeRights rights) throws InternalErrorException {
		try {
			// get action types of the attribute and role from the database
			List<ActionType> dbActionTypes = jdbc.query("select action_types.action_type as action_type from attributes_authz join action_types "
							+ "on attributes_authz.action_type_id=action_types.id where attr_id=? and "
							+ "role_id=(select id from roles where name=?)",
					new RowMapper<ActionType>() {
						@Override
						public ActionType mapRow(ResultSet rs, int rowNum) throws SQLException {
							return ActionType.valueOf(rs.getString("action_type").toUpperCase());
						}
					}, rights.getAttributeId(), rights.getRole().getRoleName());

			// inserting
			List<ActionType> actionTypesToInsert = new ArrayList<ActionType>();
			actionTypesToInsert.addAll(rights.getRights());
			actionTypesToInsert.removeAll(dbActionTypes);
			for (ActionType actionType : actionTypesToInsert) {
				jdbc.update("insert into attributes_authz (attr_id, role_id, action_type_id) values "
						+ "(?, (select id from roles where name=?), (select id from action_types where action_type=?))",
						rights.getAttributeId(), rights.getRole().getRoleName(), actionType.getActionType());
			}
			// deleting
			List<ActionType> actionTypesToDelete = new ArrayList<ActionType>();
			actionTypesToDelete.addAll(dbActionTypes);
			actionTypesToDelete.removeAll(rights.getRights());
			for (ActionType actionType : actionTypesToDelete) {
				if (0 == jdbc.update("delete from attributes_authz where attr_id=? and role_id=(select id from roles where name=?) and "
							+ "action_type_id=(select id from action_types where action_type=?)", rights.getAttributeId(),
							rights.getRole().getRoleName(), actionType.getActionType())) {
					throw new ConsistencyErrorException("Trying to delete non existing row : AttributeRight={ attributeId="
							+ Integer.toString(rights.getAttributeId()) + " role=" + rights.getRole().getRoleName() + " actionType=" + actionType.getActionType());
							}
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}
}
