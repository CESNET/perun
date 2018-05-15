package cz.metacentrum.perun.core.impl;

import com.google.common.io.CharStreams;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.*;
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
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.HostAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.lang.IllegalArgumentException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
	private Map<String, AttributesModuleImplApi> attributesModulesMap = new ConcurrentHashMap<>();

	private AttributesManagerImplApi self;

	// mapping of the perun bean names to the attribute namespaces
	public static final Map<String, String> BEANS_TO_NAMESPACES_MAP = new LinkedHashMap<>();
	private static final Map<String, String> ENTITIES_TO_BEANS_MAP = new HashMap<>();
	private static final List<String> SINGLE_BEAN_ATTRIBUTES = Arrays.asList("user","member","facility","vo","host","group","resource","user_ext_source");
	private static final List<String> DOUBLE_BEAN_ATTRIBUTES = Arrays.asList("member_resource","member_group","user_facility","group_resource");

	/**
	 * List of allowed values for attribute type.
	 * @see cz.metacentrum.perun.core.api.BeansUtils#attributeValueToString(Attribute)
	 */
	public static final List<String> ATTRIBUTE_TYPES = Arrays.asList(
			String.class.getName(),
			Integer.class.getName(),
			Boolean.class.getName(),
			ArrayList.class.getName(),
			LinkedHashMap.class.getName(),
			BeansUtils.largeStringClassName,
			BeansUtils.largeArrayListClassName
	);

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
		for(Map.Entry<String,String> entry : BEANS_TO_NAMESPACES_MAP.entrySet()) {
			ENTITIES_TO_BEANS_MAP.put(entry.getValue().split(":")[2],entry.getKey());
		}
	}

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool instance
	 */
	public AttributesManagerImpl(DataSource perunPool) throws InternalErrorException {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	protected final static String attributeDefinitionMappingSelectQuery =
			"attr_names.id as attr_names_id," +
			"attr_names.friendly_name as attr_names_friendly_name," +
			"attr_names.namespace as attr_names_namespace," +
			"attr_names.type as attr_names_type," +
			"attr_names.display_name as attr_names_display_name," +
			"attr_names.dsc as attr_names_dsc," +
			"attr_names.is_unique as attr_names_unique," +
			"attr_names.created_at as attr_names_created_at," +
			"attr_names.created_by as attr_names_created_by," +
			"attr_names.modified_by as attr_names_modified_by," +
			"attr_names.modified_at as attr_names_modified_at," +
			"attr_names.created_by_uid as attr_names_created_by_uid," +
			"attr_names.modified_by_uid as attr_names_modified_by_uid";

	private final static String attributeRightSelectQuery =
			"attributes_authz.attr_id as attr_name_id," +
					"roles.name as role_name," +
					"action_types.action_type as action_type";

	static String getAttributeMappingSelectQuery(String nameOfValueTable) {
		return attributeDefinitionMappingSelectQuery + ", attr_value, attr_value_text" +
				", " + nameOfValueTable + ".created_at as attr_value_created_at" +
				", " + nameOfValueTable + ".created_by as attr_value_created_by" +
				", " + nameOfValueTable + ".modified_at as attr_value_modified_at" +
				", " + nameOfValueTable + ".modified_by as attr_value_modified_by";
	}

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
		if (rs.getInt("attr_names_modified_by_uid") == 0) attribute.setModifiedByUid(null);
		else attribute.setModifiedByUid(rs.getInt("attr_names_modified_by_uid"));
		if (rs.getInt("attr_names_created_by_uid") == 0) attribute.setCreatedByUid(null);
		else attribute.setCreatedByUid(rs.getInt("attr_names_created_by_uid"));
		return attribute;
	};

	/*
	 * This rowMapper is only for getting attribute values (value and valueText)
	 */
	private static final RowMapper<String> ATTRIBUTE_VALUES_MAPPER = (rs, i) -> {
		String value;
		String valueText;
		//CLOB in oracle
		if (Compatibility.isOracle()) {
			Clob clob = rs.getClob("attr_value_text");
			char[] cbuf;
			if (clob == null) {
				valueText = null;
			} else {
				try {
					cbuf = new char[(int) clob.length()];
					//noinspection ResultOfMethodCallIgnored
					clob.getCharacterStream().read(cbuf);
				} catch (IOException ex) {
					throw new InternalErrorRuntimeException(ex);
				}
				valueText = new String(cbuf);
			}
		} else {
			// POSTGRES READ CLOB AS STRING
			valueText = rs.getString("attr_value_text");
		}
		value = rs.getString("attr_value");

		if (valueText != null) return valueText;
		else return value;
	};

	static final RowMapper<Attribute> ATTRIBUTE_MAPPER = (rs, i) -> {

		AttributeDefinition attributeDefinition = ATTRIBUTE_DEFINITION_MAPPER.mapRow(rs,i);

		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
		attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
		attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
		attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));

		String stringValue = rs.getString("attr_value");

		try {
			attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
		} catch (InternalErrorException ex) {
			throw new InternalErrorRuntimeException(ex);
		}

		attribute.setDescription(rs.getString("attr_names_dsc"));

		return attribute;
	};

	private static final RowMapper<String> ENTITYLESS_KEYS_MAPPER = (rs, i) -> rs.getString("subject");

	private static final RowMapper<String> ATTRIBUTE_NAMES_MAPPER = (rs, i) -> rs.getString("attr_name");

	static class SingleBeanAttributeRowMapper<T extends PerunBean> extends AttributeRowMapper<T, T> {
		SingleBeanAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, T attributeHolder) {
			super(sess, attributesManagerImpl, attributeHolder, null);
		}
	}

	protected static abstract class AttributeRowMapper<T extends PerunBean, V extends PerunBean> implements RowMapper<Attribute> {
		private final PerunSession sess;
		private final AttributesManagerImpl attributesManagerImpl;
		private final T attributeHolder;
		private final V attributeHolder2;

		/**
		 * Constructor.
		 *
		 * @param attributeHolder       Facility, Resource or Member for which you want the attribute value
		 * @param attributeHolder2      secondary Facility, Resource or Member for which you want the attribute value
		 */
		AttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, T attributeHolder, V attributeHolder2) {
			this.sess = sess;
			this.attributesManagerImpl = attributesManagerImpl;
			this.attributeHolder = attributeHolder;
			this.attributeHolder2 = attributeHolder2;
		}

		@Override
		public Attribute mapRow(ResultSet rs, int i) throws SQLException {
			Attribute attribute = new Attribute(ATTRIBUTE_DEFINITION_MAPPER.mapRow(rs, i));

			if (!this.attributesManagerImpl.isVirtAttribute(sess, attribute) && !this.attributesManagerImpl.isCoreAttribute(sess, attribute)) {
				attribute.setValueCreatedAt(rs.getString("attr_value_created_at"));
				attribute.setValueCreatedBy(rs.getString("attr_value_created_by"));
				attribute.setValueModifiedAt(rs.getString("attr_value_modified_at"));
				attribute.setValueModifiedBy(rs.getString("attr_value_modified_by"));
			}
			//core attributes
			if (this.attributesManagerImpl.isCoreAttribute(sess, attribute) && attributeHolder != null) {

				return this.attributesManagerImpl.setValueForCoreAttribute(attribute, attributeHolder);

				//virtual attributes
			} else if(this.attributesManagerImpl.isVirtAttribute(sess, attribute)) {

				return this.attributesManagerImpl.setValueForVirtualAttribute(sess, this.attributesManagerImpl, attribute, attributeHolder, attributeHolder2);

				//core managed attributes
			} else if(this.attributesManagerImpl.isCoreManagedAttribute(sess, attribute) && attributeHolder != null) {
				String managerName = attribute.getNamespace().substring(attribute.getNamespace().lastIndexOf(":") + 1);
				String methodName = "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);

				try {
					Object manager = sess.getPerun().getClass().getMethod("get" + managerName).invoke(sess.getPerun());
					attribute.setValue(manager.getClass().getMethod(methodName, PerunSession.class, attributeHolder.getClass()).invoke(manager, sess, attributeHolder));
				} catch (NoSuchMethodException ex) {
					throw new InternalErrorRuntimeException("Bad core-managed attribute definition.", ex);
				} catch (IllegalAccessException ex) {
					throw new InternalErrorRuntimeException(ex);
				} catch (InvocationTargetException ex) {
					throw new InternalErrorRuntimeException("An exception raise while geting core-managed attribute value.", ex);
				}
			}

			//FIXME use ValueRowMapper
			String stringValue;
			if (Utils.isLargeAttribute(sess, attribute)) {
				if (Compatibility.isOracle()) {
					//large attributes
					Clob clob = rs.getClob("attr_value_text");
					char[] cbuf;
					if (clob == null) {
						stringValue = null;
					} else {
						try {
							cbuf = new char[(int) clob.length()];
							//noinspection ResultOfMethodCallIgnored
							clob.getCharacterStream().read(cbuf);
						} catch (IOException ex) {
							throw new InternalErrorRuntimeException(ex);
						}
						stringValue = new String(cbuf);
					}
				} else {
					// POSTGRES READ CLOB AS STRING
					stringValue = rs.getString("attr_value_text");
				}
			} else {
				//ordinary attributes read as String
				stringValue = rs.getString("attr_value");
			}

			try {
				attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

			return attribute;
		}
	}

	protected static class AttributeHoldersRowMapper implements RowMapper<AttributeHolders> {
		private final PerunSession sess;
		private final AttributesManagerImplApi attributesManagerImpl;
		private final Holder.HolderType primaryHolderType;
		private final Holder.HolderType secondaryHolderType;

		/**
		 * Constructor.
		 *
		 * @param sess perun session
		 * @param primaryHolderType Facility, Resource or Member for which you want the attribute value
		 * @param secondaryHolderType secondary Facility, Resource or Member for which you want the attribute value
		 */
		public AttributeHoldersRowMapper(PerunSession sess, AttributesManagerImplApi attributesManagerImpl, Holder.HolderType primaryHolderType, Holder.HolderType secondaryHolderType) {
			this.sess = sess;
			this.attributesManagerImpl = attributesManagerImpl;
			this.primaryHolderType = primaryHolderType;
			this.secondaryHolderType = secondaryHolderType;
		}

		public AttributeHolders mapRow(ResultSet rs, int i) throws SQLException {
			Attribute attribute = new Attribute(ATTRIBUTE_MAPPER.mapRow(rs, i), true);

			//FIXME use ValueRowMapper
			String stringValue;
			if(Utils.isLargeAttribute(sess, attribute)) {

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
				try {
					attribute.setValue(BeansUtils.stringToAttributeValue(stringValue, attribute.getType()));
				} catch(InternalErrorException ex) {
					throw new InternalErrorRuntimeException(ex);
				}
			}

			try {
				if(this.primaryHolderType == null) return new AttributeHolders(attribute, rs.getString("subject"), AttributeHolders.SavedBy.ID);
				else {
					Holder primaryHolder = new Holder(rs.getInt("primary_holder_id"), primaryHolderType);
					Holder secondaryHolder = null;

					if(secondaryHolderType != null) {
						secondaryHolder = new Holder(rs.getInt("secondary_holder_id"), secondaryHolderType);
					}
					return new AttributeHolders(attribute, primaryHolder, secondaryHolder, AttributeHolders.SavedBy.ID);
				}
			} catch (InternalErrorException e) {
				throw new InternalErrorRuntimeException(e);
			}
		}
	}


	/**
	 * Sets value for core attribute
	 *
	 * @param attribute attribute to set value for
	 * @param attributeHolder primary attribute holder (Facility, Resource, Member...) for which you want the attribute value
	 * @return attribute with set value
	 */
	private Attribute setValueForCoreAttribute(Attribute attribute, Object attributeHolder) {
		String methodName = "get" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
		Method method;
		try {
			method = attributeHolder.getClass().getMethod(methodName);
		} catch (NoSuchMethodException ex) {
			//if not "get", try "is"
			String methodGet = methodName;
			try {
				methodName = "is" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
				method = attributeHolder.getClass().getMethod(methodName);
			} catch (NoSuchMethodException e) {
				throw new InternalErrorRuntimeException("There is no method '" + methodGet + "' or '" + methodName + "'  for core attribute definition. " + attribute, e);
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
			} else if ((attribute.getType().equals(String.class.getName()) || attribute.getType().equals(BeansUtils.largeStringClassName)) && !(value instanceof String)) {
				//TODO check exceptions
				value = String.valueOf(value);
			} else //noinspection StatementWithEmptyBody
				if (attribute.getType().equals(Integer.class.getName()) && !(value instanceof Integer)) {
					//TODO try to cast to integer
				} else //noinspection StatementWithEmptyBody
					if (attribute.getType().equals(Boolean.class.getName()) && !(value instanceof Boolean)) {
						//TODO try to cast to boolean
					} else if ((attribute.getType().equals(ArrayList.class.getName()) || attribute.getType().equals(BeansUtils.largeArrayListClassName)) && !(value instanceof ArrayList)) {
						if (value instanceof List) {
							//noinspection unchecked
							value = new ArrayList<String>((List) value);
						} else {
							throw new InternalErrorRuntimeException("Cannot convert result of method " + attributeHolder.getClass().getName() + "." + methodName + " to ArrayList.");
						}
					} else if (attribute.getType().equals(LinkedHashMap.class.getName()) && !(value instanceof LinkedHashMap)) {
						if (value instanceof Map) {
							//noinspection unchecked
							value = new LinkedHashMap<String, String>((Map) value);
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
		} catch (IllegalAccessException ex) {
			throw new InternalErrorRuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new InternalErrorRuntimeException("An exception raise while getting core attribute value.", ex);
		}
	}

	/**
	 * Sets value for virtual attribute
	 *
	 * @param sess perun session
	 * @param attributesManagerImpl
	 * @param attribute attribute to set value for
	 * @param attributeHolder primary attribute holder (Facility, Resource, Member...) for which you want the attribute value
	 * @param attributeHolder2 secondary attribute holder (Facility, Resource, Member...) for which you want the attribute value
	 * @return attribute with set value
	 */
	private Attribute setValueForVirtualAttribute(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Attribute attribute, Object attributeHolder, Object attributeHolder2) {
		if (attributeHolder == null) throw new InternalErrorRuntimeException("Bad usage of attributeRowMapper");

		if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_USER_FACILITY_ATTR_VIRT)) {
			if (!(attributeHolder instanceof User))
				throw new ConsistencyErrorRuntimeException("First attribute holder of user_facility attribute isn't user");
			if (attributeHolder2 == null || !(attributeHolder2 instanceof Facility))
				throw new ConsistencyErrorRuntimeException("Second attribute holder of user_facility attribute isn't facility");

			try {
				FacilityUserVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getFacilityUserVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Facility) attributeHolder2, (User) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_FACILITY_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Facility))
				throw new ConsistencyErrorRuntimeException("Attribute holder of facility attribute isn't facility");

			try {
				FacilityVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getFacilityVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Facility) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_RESOURCE_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Resource))
				throw new ConsistencyErrorRuntimeException("Attribute holder of resource attribute isn't resource");

			try {
				ResourceVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getResourceVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_USER_ATTR_VIRT)) {
			if (!(attributeHolder instanceof User))
				throw new ConsistencyErrorRuntimeException("Attribute holder of user attribute isn't user");

			try {
				UserVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getUserVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (User) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Member))
				throw new ConsistencyErrorRuntimeException("Attribute holder of member attribute isn't member");

			try {
				MemberVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getMemberVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_GROUP_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Group))
				throw new ConsistencyErrorRuntimeException("Attribute holder of group attribute isn't group");

			try {
				GroupVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getGroupVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Group) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Group))
				throw new ConsistencyErrorRuntimeException("First attribute holder of group_resource attribute isn't group");
			if (attributeHolder2 == null || !(attributeHolder2 instanceof Resource))
				throw new ConsistencyErrorRuntimeException("Second attribute holder of group-resource attribute isn't resource");

			try {
				ResourceGroupVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getResourceGroupVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder2, (Group) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Resource))
				throw new ConsistencyErrorRuntimeException("First attribute holder of member_resource attribute isn't Member");
			if (attributeHolder2 == null || !(attributeHolder2 instanceof Member))
				throw new ConsistencyErrorRuntimeException("Second attribute holder of member_resource attribute isn't resource");

			try {
				ResourceMemberVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getResourceMemberVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Resource) attributeHolder, (Member) attributeHolder2, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}
		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT)) {
			if (!(attributeHolder instanceof Member))
				throw new ConsistencyErrorRuntimeException("First attribute holder of member_group attribute isn't Member");
			if (attributeHolder2 == null || !(attributeHolder2 instanceof Group))
				throw new ConsistencyErrorRuntimeException("Second attribute holder of member_group attribute isn't Group");

			try {
				MemberGroupVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getMemberGroupVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (Member) attributeHolder, (Group) attributeHolder2, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else if (attributesManagerImpl.isFromNamespace(attribute, AttributesManager.NS_UES_ATTR_VIRT)) {
			if (!(attributeHolder instanceof UserExtSource))
				throw new ConsistencyErrorRuntimeException("Attribute holder of UserExtSource attribute isn't UserExtSource");

			try {
				UserExtSourceVirtualAttributesModuleImplApi attributeModule = attributesManagerImpl.getUserExtSourceVirtualAttributeModule(sess, attribute);
				return attributeModule.getAttributeValue((PerunSessionImpl) sess, (UserExtSource) attributeHolder, attribute);
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}

		} else {
			throw new InternalErrorRuntimeException("Virtual attribute modules for this namespace isn't defined.");
		}
	}

	/**
	 * Sets values for core and virtual attributes. If it gets attributes that are not virtual or core, it returns them as they are.
	 *
	 * @param sess perun session
	 * @param attributes attributes to set value for.
	 * @param attributeHolder primary attribute holder (Facility, Resource, Member...) for which you want the attribute value
	 * @param attributeHolder2 secondary holder (Facility, Resource, Member...) for which you want the attribute value
	 * @return list of attributes with set values
	 */
	private List<Attribute> setValuesOfAttributes(PerunSession sess, List<Attribute> attributes, Object attributeHolder, Object attributeHolder2) {

		List<Attribute> attributesToReturn = new ArrayList<>();

		for(Attribute attribute: attributes) {

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

	/**
	 * Sets value of attribute. If attribute is not virtual or core, it returns it without doing anything.
	 *
	 * @param sess perun session
	 * @param attribute attribute to set value for.
	 * @param attributeHolder primary attribute holder (Facility, Resource, Member...) for which you want the attribute value
	 * @param attributeHolder2 secondary holder (Facility, Resource, Member...) for which you want the attribute value
	 * @return attribute with set value
	 */
	private Attribute setValueOfAttribute(PerunSession sess, Attribute attribute, Object attributeHolder, Object attributeHolder2) throws InternalErrorException {

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

	static class UserFacilityAttributeRowMapper extends AttributeRowMapper<User, Facility> {
		UserFacilityAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, User attributeHolder, Facility attributeHolder2) {
			super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
		}
	}

	static class ResourceMemberAttributeRowMapper extends AttributeRowMapper<Resource, Member> {
		ResourceMemberAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Resource attributeHolder, Member attributeHolder2) {
			super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
		}
	}

	protected static class MemberGroupAttributeRowMapper extends AttributeRowMapper<Member, Group> {
		MemberGroupAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Member attributeHolder, Group attributeHolder2) {
			super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
		}
	}

	protected static class GroupResourceAttributeRowMapper extends AttributeRowMapper<Group, Resource> {
		GroupResourceAttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Group attributeHolder, Resource attributeHolder2) {
			super(sess, attributesManagerImpl, attributeHolder, attributeHolder2);
		}
	}

	private static class ValueRowMapper implements RowMapper<Object> {
		private final PerunSession sess;
		private final AttributeDefinition attributeDefinition;

		/**
		 * Constructor.
		 */
		ValueRowMapper(PerunSession sess, AttributeDefinition attributeDefinition) {
			this.sess = sess;
			this.attributeDefinition = attributeDefinition;
		}

		@Override
		public Object mapRow(ResultSet rs, int i) throws SQLException {
			String stringValue;
			if (Utils.isLargeAttribute(sess, attributeDefinition)) {
				//large attributes
				if (Compatibility.isOracle()) {
					Clob clob = rs.getClob("attr_value_text");
					char[] cbuf;
					if (clob == null) {
						stringValue = null;
					} else {
						try {
							cbuf = new char[(int) clob.length()];
							//noinspection ResultOfMethodCallIgnored
							clob.getCharacterStream().read(cbuf);
						} catch (IOException ex) {
							throw new InternalErrorRuntimeException(ex);
						}
						stringValue = new String(cbuf);
					}
				} else {
					// POSTGRES READ CLOB AS STRING
					stringValue = rs.getString("attr_value_text");
				}
			} else {
				//ordinary attributes
				stringValue = rs.getString("attr_value");
			}

			try {
				return BeansUtils.stringToAttributeValue(stringValue, attributeDefinition.getType());
			} catch (InternalErrorException ex) {
				throw new InternalErrorRuntimeException(ex);
			}
		}
	}


	private static class RichAttributeRowMapper<P, S> implements RowMapper<RichAttribute<P, S>> {
		private final AttributeRowMapper attributeRowMapper;
		private final RowMapper<P> primaryRowMapper;
		private final RowMapper<S> secondaryRowMapper;


		RichAttributeRowMapper(AttributeRowMapper attributeRowMapper, RowMapper<P> primaryRowMapper, RowMapper<S> secondaryRowMapper) {
			this.attributeRowMapper = attributeRowMapper;
			this.primaryRowMapper = primaryRowMapper;
			this.secondaryRowMapper = secondaryRowMapper;
		}


		@Override
		public RichAttribute<P, S> mapRow(ResultSet rs, int i) throws SQLException {
			Attribute attribute = attributeRowMapper.mapRow(rs, i);
			P primaryHolder = primaryRowMapper.mapRow(rs, i);
			S secondaryHolder = null;
			if (secondaryRowMapper != null) secondaryHolder = secondaryRowMapper.mapRow(rs, i);
			return new RichAttribute<>(primaryHolder, secondaryHolder, attribute);
		}

	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return this.setValuesOfAttributes(sess, attrs, facility, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("fac") + " from attr_names " +
							"left join facility_attr_values fac    on id=fac.attr_id and fac.facility_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_OPT);

		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for facility exists.");
			return new ArrayList<>();

		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility, List<String> attrNames) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("fId", facility.getId());
		parameters.addValue("nSC", AttributesManager.NS_FACILITY_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_FACILITY_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_FACILITY_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_FACILITY_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("fav") + " from attr_names " +
							"left join facility_attr_values fav on id=fav.attr_id and facility_id=:fId " +
							"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, facility));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private List<Attribute> getVirtualAttributes(RowMapper<Attribute> rowMapper, String namespace) throws InternalErrorException {
		try {
			return jdbc.query("SELECT " + attributeDefinitionMappingSelectQuery + ", NULL AS attr_value FROM attr_names WHERE namespace=?", rowMapper, namespace);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No virtual attribute for "+(namespace.split(":")[2])+" exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.FACILITY, null);
			return this.setValuesOfAttributes(sess, attrs, facility, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, facility), AttributesManager.NS_FACILITY_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.MEMBER, null);
			return this.setValuesOfAttributes(sess, attrs, member, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, member), AttributesManager.NS_MEMBER_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.VO, null);
			return this.setValuesOfAttributes(sess, attrs, vo, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, vo), AttributesManager.NS_VO_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.GROUP, null);
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, group), AttributesManager.NS_GROUP_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Host host) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.HOST, null);
			return this.setValuesOfAttributes(sess, attrs, host, null);
		}
		return  getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, host), AttributesManager.NS_HOST_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.RESOURCE, null);
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, resource), AttributesManager.NS_RESOURCE_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.USER, null);
			return this.setValuesOfAttributes(sess, attrs, user, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, user), AttributesManager.NS_USER_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.UES, null);
			return this.setValuesOfAttributes(sess, attrs, ues, null);
		}
		return getVirtualAttributes(new SingleBeanAttributeRowMapper<>(sess, this, ues), AttributesManager.NS_UES_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.MEMBER, Holder.HolderType.RESOURCE);
			return this.setValuesOfAttributes(sess, attrs, member, resource);
		}
		return getVirtualAttributes(new ResourceMemberAttributeRowMapper(sess, this, resource, member), AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.USER, Holder.HolderType.FACILITY);
			return this.setValuesOfAttributes(sess, attrs, user, facility);
		}
		return getVirtualAttributes(new UserFacilityAttributeRowMapper(sess, this, user, facility), AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
	}

	@Override
	public List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getVirtualAttributes(Holder.HolderType.MEMBER, Holder.HolderType.GROUP);
			return this.setValuesOfAttributes(sess, attrs, member, group);
		}
		return  getVirtualAttributes(new MemberGroupAttributeRowMapper(sess, this, member, group), AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
	}


	@Override
	public List<Attribute> getAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(vo.getId(), Holder.HolderType.VO));
			return this.setValuesOfAttributes(sess, attrs, vo, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("voattr") + " from attr_names " +
							"left join vo_attr_values voattr    on id=voattr.attr_id and voattr.vo_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(), AttributesManager.NS_VO_ATTR_CORE, AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("groupattr") + " from attr_names " +
							"left join group_attr_values groupattr    on id=groupattr.attr_id and groupattr.group_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Host host) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(host.getId(), Holder.HolderType.HOST));
			return this.setValuesOfAttributes(sess, attrs, host, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
							"left join host_attr_values on id=attr_id and host_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for host exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
							"left join resource_attr_values on id=attr_id and resource_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for resource exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction())
			return perun.getCacheManager().getAllNonEmptyAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));

		try {
			//member-resource attributes, member core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join   member_resource_attr_values   mem        on attr_names.id=mem.attr_id and mem.resource_id=? and member_id=? " +
							"where namespace in (?,?) and (mem.attr_value is not null or mem.attr_value_text is not null)",
					new ResourceMemberAttributeRowMapper(sess, this, resource, member),
					resource.getId(), member.getId(),
					AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for member-resource combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction())
			return perun.getCacheManager().getAllNonEmptyAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));

		try {
			//member-group attributes, member core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on attr_names.id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?) and (mem_gr.attr_value is not null or mem_gr.attr_value_text is not null)",
					new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(),
					AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for member-group combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, member, resource);
		}

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
					parameters, new ResourceMemberAttributeRowMapper(sess, this, resource, member));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, group, resource);
		}

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
					parameters, new GroupResourceAttributeRowMapper(sess, this, group, resource));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user, Facility facility, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_USER_FACILITY_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return this.setValuesOfAttributes(sess, attrs, user, facility);
		}

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
					parameters, new UserFacilityAttributeRowMapper(sess, this, user, facility));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, member, group);
		}

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
					parameters, new MemberGroupAttributeRowMapper(sess, this, member, group));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER));
			return this.setValuesOfAttributes(sess, attrs, member, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join    member_attr_values     mem      on id=mem.attr_id     and    member_id=? " +
							"where namespace=? or (namespace in (?,?) and (mem.attr_value is not null or mem.attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(),
					AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_DEF);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for member exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_VO_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(vo.getId(), Holder.HolderType.VO), null);
			return this.setValuesOfAttributes(sess, attrs, vo, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, vo));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllAttributesByStartPartOfName(startPartOfName, new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, group));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllAttributesByStartPartOfName(startPartOfName, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, resource));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(member.getId(), Holder.HolderType.MEMBER), null);
			return this.setValuesOfAttributes(sess, attrs, member, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, member));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_GROUP_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(group.getId(), Holder.HolderType.GROUP), null);
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, group));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, List<String> attrNames) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("rId", resource.getId());
		parameters.addValue("nSC", AttributesManager.NS_RESOURCE_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_RESOURCE_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_RESOURCE_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_RESOURCE_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("resattr") + " from attr_names " +
							"left join resource_attr_values resattr on id=resattr.attr_id and resource_id=:rId " +
							"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, resource));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction())
			return perun.getCacheManager().getAllNonEmptyAttributes(new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
							"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
							"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getUserFacilityAttributesForAnyUser(facility.getId());

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
							"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? " +
							"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(user.getId(), Holder.HolderType.USER));
			return this.setValuesOfAttributes(sess, attrs, user, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
							"where namespace=? or (namespace in (?,?) and (attr_value is not null or attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(),
					AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for user exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				//check namespace
				if(attributeName.startsWith(AttributesManager.NS_USER_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(user.getId(), Holder.HolderType.USER), null);
			return this.setValuesOfAttributes(sess, attrs, user, null);
		}

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
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, user));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}



	@Override
	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<String> controlledAttrNames = new ArrayList<>();

			for(String attributeName: attrNames) {
				// Check namespace
				if(attributeName.startsWith(AttributesManager.NS_UES_ATTR)) controlledAttrNames.add(attributeName);
			}

			List<Attribute> attrs = perun.getCacheManager().getAttributesByNames(controlledAttrNames, new Holder(ues.getId(), Holder.HolderType.UES), null);
			return this.setValuesOfAttributes(sess, attrs, ues, null);
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("uesId", ues.getId());
		parameters.addValue("nSC", AttributesManager.NS_UES_ATTR_CORE);
		parameters.addValue("nSO", AttributesManager.NS_UES_ATTR_OPT);
		parameters.addValue("nSD", AttributesManager.NS_UES_ATTR_DEF);
		parameters.addValue("nSV", AttributesManager.NS_UES_ATTR_VIRT);
		parameters.addValue("attrNames", attrNames);

		try {
			return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
							"left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=:uesId " +
							"where namespace in ( :nSC,:nSO,:nSD,:nSV ) and attr_names.attr_name in ( :attrNames )",
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, ues));
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction())
			return perun.getCacheManager().getAllNonEmptyAttributes(new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
							"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
							"where namespace in (?,?) and (grp_res.attr_value is not null or grp_res.attr_value_text is not null)",
					new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(),
					AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, String key) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) return perun.getCacheManager().getAllNonEmptyEntitylessAttributes(key);
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
							"left join entityless_attr_values enattr on id=enattr.attr_id and enattr.subject=? " +
							"where namespace in (?,?) and (enattr.attr_value is not null or enattr.attr_value_text is not null)",
					new SingleBeanAttributeRowMapper<>(sess, this, null), key, AttributesManager.NS_ENTITYLESS_ATTR_DEF, AttributesManager.NS_ENTITYLESS_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Attribute> attrs = perun.getCacheManager().getAllNonEmptyAttributes(new Holder(ues.getId(), Holder.HolderType.UES));
			return this.setValuesOfAttributes(sess, attrs, ues, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("ues") + " from attr_names " +
							"left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=? " +
							"where namespace=? or (namespace in (?,?) and (ues.attr_value is not null or ues.attr_value_text is not null))",
					new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(),
					AttributesManager.NS_UES_ATTR_CORE, AttributesManager.NS_UES_ATTR_DEF, AttributesManager.NS_UES_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for UserExtSource exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public String getEntitylessAttrValueForUpdate(PerunSession sess, int attrId, String key) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			String value = perun.getCacheManager().getEntitylessAttrValue(attrId, key);

			if(value != null) return value;
			else {
				//If there is no such entityless attribute, create new one with null value and return null (insert is for transaction same like select for update)
				Attribute attr = new Attribute(this.getAttributeDefinitionById(sess, attrId));
				setAttributeCreatedAndModified(sess, attr);
				self.setAttributeWithNullValue(sess, key, attr);
				perun.getCacheManager().setEntitylessAttribute(attr, key);
				return null;
			}
		}

		try {
			return jdbc.queryForObject("SELECT attr_value, attr_value_text FROM entityless_attr_values WHERE subject=? AND attr_id=? FOR UPDATE", ATTRIBUTE_VALUES_MAPPER, key, attrId);
		} catch (EmptyResultDataAccessException ex) {
			//If there is no such entityless attribute, create new one with null value and return null (insert is for transaction same like select for update)
			Attribute attr = new Attribute(this.getAttributeDefinitionById(sess, attrId));
			this.setAttributeCreatedAndModified(sess, attr);
			self.setAttributeWithNullValue(sess, key, attr);
			return null;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws  InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAllNonEmptyEntitylessAttributesByName(attrName);
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("enattr") + " from attr_names " +
							"left join entityless_attr_values enattr on id=enattr.attr_id " +
							"where attr_name=? and namespace in (?,?) and (enattr.attr_value is not null or enattr.attr_value_text is not null)",
					new SingleBeanAttributeRowMapper<>(sess, this, null), attrName, AttributesManager.NS_ENTITYLESS_ATTR_DEF, AttributesManager.NS_ENTITYLESS_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getEntitylessAttrKeys(attributeDefinition.getName());

		try {
			return jdbc.query("SELECT subject FROM attr_names JOIN entityless_attr_values ON id=attr_id  WHERE attr_name=?", ENTITYLESS_KEYS_MAPPER, attributeDefinition.getName());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAttributesByAttributeDefinition(attributeDefinition);

		// Get the entity from the name
		String entity = attributeDefinition.getEntity();
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery(entity + "_attr_values") + " from attr_names join " + entity + "_attr_values on id=attr_id  where attr_name=?", ATTRIBUTE_MAPPER, attributeDefinition.getName());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<RichAttribute<User, Facility>> getAllUserFacilityRichAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + ", " + UsersManagerImpl.userMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery + "   from attr_names " +
							"left join    user_facility_attr_values     usr_fac      on attr_names.id=usr_fac.attr_id     and   usr_fac.user_id=? " +
							"join users on users.id = usr_fac.user_id " +
							"join facilities on facilities.id = usr_fac.facility_id " +
							"where namespace in (?,?) and (usr_fac.attr_value is not null or usr_fac.attr_value_text is not null)",
					new RichAttributeRowMapper<>(new SingleBeanAttributeRowMapper<>(sess, this, user), UsersManagerImpl.USER_MAPPER, FacilitiesManagerImpl.FACILITY_MAPPER),
					user.getId(),
					AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute for user-facility combination exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(facility.getId(), Holder.HolderType.FACILITY), null);
			return setValueOfAttribute(sess, attr, facility, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names left join facility_attr_values on id=attr_id and facility_id=? where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Facility attribute - attribute.name='" + attributeName + "'");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAllSimilarAttributeNames(startingPartOfAttributeName);

		try {
			return jdbc.query("SELECT attr_name FROM attr_names WHERE attr_name LIKE ? || '%'", ATTRIBUTE_NAMES_MAPPER, startingPartOfAttributeName);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(vo.getId(), Holder.HolderType.VO), null);
			return setValueOfAttribute(sess, attr, vo, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Vo attribute - attribute.name='" + attributeName + "'");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(group.getId(), Holder.HolderType.GROUP), null);
			return setValueOfAttribute(sess, attr, group, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("group_attr_values") + " from attr_names left join group_attr_values on id=attr_id and group_id=? where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Group attribute - attribute.name='" + attributeName + "'");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(resource.getId(), Holder.HolderType.RESOURCE), null);
			return setValueOfAttribute(sess, attr, resource, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names left join resource_attr_values on id=attr_id and resource_id=? where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Resource attribute - attribute.name='" + attributeName + "'");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return setValueOfAttribute(sess, attr, member, resource);
		}

		try {
			//member-resource attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
							"where attr_name=?",
					new ResourceMemberAttributeRowMapper(sess, this, resource, member), resource.getId(), member.getId(), attributeName);


		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
			return setValueOfAttribute(sess, attr, member, group);
		}

		try {
			//member-group attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where attr_name=?",
					new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(), attributeName);

		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(member.getId(), Holder.HolderType.MEMBER), null);
			return setValueOfAttribute(sess, attr, member, null);
		}

		//member and member core attributes
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
							"where attr_name=?",
					new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return setValueOfAttribute(sess, attr, user, facility);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
							"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
							"where attr_name=?",
					new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(user.getId(), Holder.HolderType.USER), null);
			return setValueOfAttribute(sess, attr, user, null);
		}

		//user and user core attributes
		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
							"where attr_name=?",
					new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(host.getId(), Holder.HolderType.HOST), null);
			return setValueOfAttribute(sess, attr, host, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names " +
					"left join host_attr_values on id=attr_id and host_id=? where attr_name=?", new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Host attribute - attribute.name='" + attributeName + "'");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return setValueOfAttribute(sess, attr, group, resource);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
							"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
							"where attr_name=?",
					new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, String key, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getEntitylessAttribute(attributeName, key);
			return setValueOfAttribute(sess, attr, null, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("entityless_attr_values") + " from attr_names " +
							"left join    entityless_attr_values     on id=entityless_attr_values.attr_id     and   subject=? " +
							"where attr_name=?",
					new SingleBeanAttributeRowMapper<>(sess, this, null), key, attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Map<String,String> getEntitylessStringAttributeMapping(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			Map<String,String> keyValueMap = new HashMap<>();
			List<String> entitylessAttrKeys = perun.getCacheManager().getEntitylessAttrKeys(attributeName);
			for (String key: entitylessAttrKeys) {
				Attribute entitylessAttribute = perun.getCacheManager().getEntitylessAttribute(attributeName, key);
				String entitylessAttrValue = perun.getCacheManager().getEntitylessAttrValue(entitylessAttribute.getId(), key);
				keyValueMap.put(key, entitylessAttrValue);
			}

			return keyValueMap;
		}

		try {
			Map<String,String> map = new HashMap<>();
			jdbc.query("select subject, attr_value " +
							" from attr_names join entityless_attr_values on id=attr_id " +
							" where type='java.lang.String' and attr_name=?",
					rs -> { map.put(rs.getString(1), rs.getString(2)); }, attributeName);
			return map;
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeByName(attributeName, new Holder(ues.getId(), Holder.HolderType.UES), null);
			return setValueOfAttribute(sess, attr, ues, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("user_ext_source_attr_values") + " from attr_names " +
							"left join user_ext_source_attr_values on id=attr_id and user_ext_source_id=? " +
							"where attr_name=?",
					new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(), attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute name: \"" + attributeName + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) return perun.getCacheManager().getAttributeDefinition(attributeName);

		try {
			return jdbc.queryForObject("SELECT " + attributeDefinitionMappingSelectQuery + " FROM attr_names WHERE attr_name=?", ATTRIBUTE_DEFINITION_MAPPER, attributeName);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute - attribute.name='" + attributeName + "'", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAttributesDefinitions();

		try {
			return jdbc.query("SELECT " + attributeDefinitionMappingSelectQuery + ", NULL AS attr_value FROM attr_names", ATTRIBUTE_DEFINITION_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute definition exists.");
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAttributesDefinitionsByNamespace(namespace);

		try {
			return jdbc.query("SELECT " + attributeDefinitionMappingSelectQuery + ", NULL AS attr_value FROM attr_names WHERE namespace=?", ATTRIBUTE_DEFINITION_MAPPER, namespace);
		} catch (EmptyResultDataAccessException ex) {
			log.debug("No attribute definition with namespace='{}' exists.", namespace);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) return perun.getCacheManager().getAttributeDefinition(id);

		try {
			return jdbc.queryForObject("SELECT " + attributeDefinitionMappingSelectQuery + " FROM attr_names WHERE id=?", ATTRIBUTE_DEFINITION_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(facility.getId(), Holder.HolderType.FACILITY), null);
			return setValueOfAttribute(sess, attr, facility, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names left join facility_attr_values on id=attr_id and facility_id=? where id=?", new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(vo.getId(), Holder.HolderType.VO), null);
			return setValueOfAttribute(sess, attr, vo, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names left join vo_attr_values on id=attr_id and vo_id=? where id=?", new SingleBeanAttributeRowMapper<>(sess, this, vo), vo.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(resource.getId(), Holder.HolderType.RESOURCE), null);
			return setValueOfAttribute(sess, attr, resource, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names left join resource_attr_values on id=attr_id and resource_id=? where id=?", new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return setValueOfAttribute(sess, attr, group, resource);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
							"left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
							"where id=?",
					new GroupResourceAttributeRowMapper(sess, this, group, resource), resource.getId(), group.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(group.getId(), Holder.HolderType.GROUP), null);
			return setValueOfAttribute(sess, attr, group, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
							"left join group_attr_values grp on id=grp.attr_id and group_id=? " +
							"where id=?",
					new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Host host, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(host.getId(), Holder.HolderType.HOST), null);
			return setValueOfAttribute(sess, attr, host, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("host_attr_values") + " from attr_names left join host_attr_values on id=attr_id and host_id=? where id=?", new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return setValueOfAttribute(sess, attr, member, resource);
		}

		try {
			//member-resource attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
							"where id=?",
					new SingleBeanAttributeRowMapper<>(sess, this, member), resource.getId(), member.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
			return setValueOfAttribute(sess, attr, member, group);
		}

		try {
			//member-group attributes, member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where id=?",
					new SingleBeanAttributeRowMapper<>(sess, this, member), group.getId(), member.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(member.getId(), Holder.HolderType.MEMBER), null);
			return setValueOfAttribute(sess, attr, member, null);
		}

		try {
			//member and member core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
							"where id=?",
					new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return setValueOfAttribute(sess, attr, user, facility);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
							"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
							"where id=?",
					new UserFacilityAttributeRowMapper(sess, this, user, facility), facility.getId(), user.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, User user, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(user.getId(), Holder.HolderType.USER), null);
			return setValueOfAttribute(sess, attr, user, null);
		}

		try {
			//user and user core attributes
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
							"where id=?",
					new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws InternalErrorException, AttributeNotExistsException {
		if(!CacheManager.isCacheDisabled()) {
			Attribute attr = perun.getCacheManager().getAttributeById(id, new Holder(ues.getId(), Holder.HolderType.UES), null);
			return setValueOfAttribute(sess, attr, ues, null);
		}

		try {
			return jdbc.queryForObject("select " + getAttributeMappingSelectQuery("ues") + " from attr_names left join user_ext_source_attr_values ues on id=ues.attr_id and user_ext_source_id=? where id=?", new SingleBeanAttributeRowMapper<>(sess, this, ues), ues.getId(), id);
		} catch (EmptyResultDataAccessException ex) {
			throw new AttributeNotExistsException("Attribute id= \"" + id + "\"", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean setAttribute(final PerunSession sess, final Object object, final Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException {
		String tableName;
		String columnName;
		Object identificator;
		String namespace;
		Holder holder = null;

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
				throw new InternalErrorException(new IllegalArgumentException("Setting attribute for perun bean " + bean + " is not allowed."));
			}
			tableName = name + "_attr_values";
			columnName = name + "_id";
			identificator = bean.getId();
			holder = createHolderTypeByStringAndId((Integer) identificator, name);
		} else {
			throw new InternalErrorException(new IllegalArgumentException("Object " + object + " must be either String or PerunBean."));
		}

		// check that given object is consistent with the attribute
		checkNamespace(sess, attribute, namespace);

		// create lists of parameters for the where clause of the SQL query
		List<String> columnNames = Arrays.asList( "attr_id", columnName);
		List<Object> columnValues = Arrays.asList( attribute.getId(), identificator);

		// save attribute
		boolean changedDb;
		if (object instanceof String) {
			changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues, object, null);
		} else {
			changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues, holder, null);
		}

		if(changedDb && attribute.isUnique() && (object instanceof PerunBean)) {
			setUniqueAttributeValues(attribute, columnNames, columnValues, (PerunBean)object, null);
		}
		return changedDb;
	}

	@Override
	public boolean setAttribute(final PerunSession sess, final PerunBean bean1, final PerunBean bean2, final Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException {
		String tableName;
		String namespace;
		Integer identificator1;
		Integer identificator2;
		Holder holder1;
		Holder holder2;

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
		namespace = BEANS_TO_NAMESPACES_MAP.get(name1 + "_" + name2);
		identificator1 = bean1.getId();
		identificator2 = bean2.getId();
		holder1 = createHolderTypeByStringAndId(identificator1, name1);
		holder2 = createHolderTypeByStringAndId(identificator2, name2);

		if (namespace == null) {
			// swap the names and beans and try again
			String nameTmp = name1;
			name1 = name2;
			name2 = nameTmp;
			identificator1 = bean2.getId();
			identificator2 = bean1.getId();
			namespace = BEANS_TO_NAMESPACES_MAP.get(name1 + "_" + name2);
		}
		if (namespace == null) {
			// the combination of perun beans is not in the namespace map
			throw new InternalErrorException(new IllegalArgumentException("Setting attribute for perun bean " + bean1 + " and " + bean2 + " is not allowed."));
		}
		tableName = name1 + "_" + name2 + "_attr_values";

		// check that given object is consistent with the attribute
		checkNamespace(sess, attribute, namespace);

		// create lists of parameters for the where clause of the SQL query
		List<String> columnNames = Arrays.asList( "attr_id", name1 + "_id", name2 + "_id");
		List<Object> columnValues = Arrays.asList( attribute.getId(), identificator1, identificator2);

		// save attribute
		boolean changedDb = setAttributeInDB(sess, attribute, tableName, columnNames, columnValues, holder1, holder2);
		if(changedDb && attribute.isUnique()) {
			setUniqueAttributeValues(attribute, columnNames, columnValues, bean1, bean2);
		}
		return changedDb;
	}

	@SuppressWarnings("unchecked")
	private void setUniqueAttributeValues(Attribute attribute, List<String> columnNames, List<Object> columnValues, PerunBean pb1, PerunBean pb2) throws WrongAttributeValueException {
		String tableName = attributeToTablePrefix(attribute) + "_attr_u_values";
		jdbc.update("delete from " + tableName + " where " + buildParameters(columnNames, "=?", " and "), columnValues.toArray());
		if(attribute.getValue()==null) return;
		// prepare correct number of question marks
		StringBuilder questionMarks = new StringBuilder();
		for (int i = 0; i < columnValues.size(); i++) {
			questionMarks.append(",?");
		}
		//prepare list of column values for adding attribute value
		Object[] sqlArgs = new Object[columnValues.size()+1];
		System.arraycopy(columnValues.toArray(),0,sqlArgs,1,columnValues.size());
		String sql = "INSERT INTO " + tableName + " (attr_value," + buildParameters(columnNames, "", ", ") + ") VALUES (?" + questionMarks + ")";
			switch (attribute.getType()) {
				case "java.util.ArrayList":
				case BeansUtils.largeArrayListClassName:
					for (String value : (List<String>) attribute.getValue()) {
						sqlArgs[0] = value;
						tryToInsertUniqueValue(sql,sqlArgs, attribute, pb1, pb2);
					}
					break;
				case "java.util.LinkedHashMap":
					for (Map.Entry<String, String> entry : ((Map<String, String>) attribute.getValue()).entrySet()) {
						sqlArgs[0] = entry.getKey() + "=" + entry.getValue();
						tryToInsertUniqueValue(sql,sqlArgs, attribute, pb1, pb2);
					}
					break;
				default:
					sqlArgs[0] = attribute.getValue().toString();
					tryToInsertUniqueValue(sql,sqlArgs, attribute, pb1, pb2);
			}

	}

	private void tryToInsertUniqueValue(String sql, Object[] sqlArgs, Attribute attribute,PerunBean pb1, PerunBean pb2) throws WrongAttributeValueException {
		try {
			jdbc.update(sql, sqlArgs);
		} catch (DuplicateKeyException ex) {
			throw new WrongAttributeValueException(attribute, pb1, pb2, "value "+sqlArgs[0]+" is not unique");
		}
	}

	private boolean setAttributeInDB(final PerunSession sess, final Attribute attribute, final String tableName, List<String> columnNames, List<Object> columnValues, Object holder1, Object holder2) throws InternalErrorException {
		try {
			//check that attribute definition is current, non-altered by upper tiers
			getAttributeDefinitionById(sess, attribute.getId()).checkEquality(attribute);
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException("cannot verify attribute definition",e);
		}
		try {
			// deleting the attribute if the given attribute value is null
			if (attribute.getValue() == null) {
				int numAffected = jdbc.update("delete from " + tableName + " where " + buildParameters(columnNames, "=?", " and "), columnValues.toArray());
				if (numAffected > 1) {
					throw new ConsistencyErrorException(String.format("Too much rows to delete (" + numAffected + " rows). SQL: delete from " + tableName + " where " + buildParameters(columnNames, "=%s", " and "), columnValues.toArray()));
				}
				if (holder2 != null) {
					if(!CacheManager.isCacheDisabled() && numAffected == 1) perun.getCacheManager().removeAttribute(attribute, (Holder) holder1, (Holder) holder2);
				} else {
					if (holder1 instanceof String) {
						if(!CacheManager.isCacheDisabled() && numAffected == 1) perun.getCacheManager().removeEntitylessAttribute(attribute, (String) holder1);
					} else {
						if(!CacheManager.isCacheDisabled() && numAffected == 1) perun.getCacheManager().removeAttribute(attribute, (Holder) holder1, null);
					}
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
							boolean check = self.insertAttribute(sess, valueColName, attribute, tableName, columnNames, columnValues);
							if (holder2 != null) {
								if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setAttribute(attribute, (Holder) holder1, (Holder) holder2);
							} else {
								if (holder1 instanceof String) {
									if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setEntitylessAttribute(attribute, (String) holder1);
								} else {
									if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setAttribute(attribute, (Holder) holder1, null);
								}
							}
							return check;
						} catch (DataAccessException ex) {
							// unsuccessful insert, do it again in while loop
							if (++repetatCounter > MERGE_TRY_CNT) {
								throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
							}
							try {
								Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
							} catch (InterruptedException ignored) {
							}
						}
						break;
					}
					case 1: {
						// value exists -> update
						try {
							boolean check = self.updateAttribute(sess, valueColName, attribute, tableName, columnNames, columnValues);
							if (holder2 != null) {
								if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setAttributeWithExistenceCheck(attribute, (Holder) holder1, (Holder) holder2);
							} else {
								if (holder1 instanceof String) {
									if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setEntitylessAttributeWithExistenceCheck(attribute, (String) holder1);
								} else {
									if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setAttributeWithExistenceCheck(attribute, (Holder) holder1, null);
								}
							}
							return check;
						} catch (DataAccessException ex) {
							// unsuccessful insert, do it again in while loop
							if (++repetatCounter > MERGE_TRY_CNT) {
								throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.");
							}
							try {
								Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
							} catch (InterruptedException ignored) {
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

	private Holder createHolderTypeByStringAndId(Integer id, String type) {
		if (id == null || type == null) {
			return null;
		}

		Holder holder;
		switch (type) {
			case "facility":
				holder = new Holder(id, Holder.HolderType.FACILITY);
				break;
			case "member":
				holder = new Holder(id, Holder.HolderType.MEMBER);
				break;
			case "vo":
				holder = new Holder(id, Holder.HolderType.VO);
				break;
			case "group":
				holder = new Holder(id, Holder.HolderType.GROUP);
				break;
			case "host":
				holder = new Holder(id, Holder.HolderType.HOST);
				break;
			case "resource":
				holder = new Holder(id, Holder.HolderType.RESOURCE);
				break;
			case "user":
				holder = new Holder(id, Holder.HolderType.USER);
				break;
			case "user_ext_source":
				holder = new Holder(id, Holder.HolderType.UES);
				break;
			default:
				holder = null;
		}
		return holder;
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

		int changed = jdbc.update("insert into " + tableName + " (" + buildParameters(columnNames, "", ", ") + ", " + valueColName + ", created_by, modified_by, created_by_uid, modified_by_uid, modified_at, created_at) "
				+ "values (" + questionMarks + Compatibility.getSysdate() + ", " + Compatibility.getSysdate() + " )", values.toArray());
		return changed > 0;
	}

	@Override
	public boolean updateAttribute(PerunSession sess, String valueColName, Attribute attribute, String tableName, List<String> columnNames, List<Object> columnValues) throws InternalErrorException {
		// add additional SQL values to the list
		List<Object> values = new ArrayList<>();
		values.add(BeansUtils.attributeValueToString(attribute)); // valueColName
		values.add(sess.getPerunPrincipal().getActor()); // modified_by
		values.add(sess.getPerunPrincipal().getUserId()); // modified_by_uid
		values.addAll(columnValues);
		int changed = jdbc.update("update " + tableName + " set " + valueColName + "=?, modified_by=?, modified_by_uid=?, modified_at=" +
				Compatibility.getSysdate() + " where " + buildParameters(columnNames, "=?", " and "), values.toArray());
		return changed > 0;
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

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getFacilityVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, facility, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getMemberVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getResourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, resource, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, group, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getFacilityUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getResourceGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getMemberGroupVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, member, group, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, user, attribute);
	}

	@Override
	public boolean setVirtualAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return getUserExtSourceVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, ues, attribute);
	}

	@Override
	public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeDefinitionExistsException {
		if (!attribute.getFriendlyName().matches(AttributesManager.ATTRIBUTES_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong attribute name " + attribute.getFriendlyName() + ", attribute name must match " + AttributesManager.ATTRIBUTES_REGEXP));
		}
		try {
			int attributeId = Utils.getNewId(jdbc, "attr_names_id_seq");

			jdbc.update("insert into attr_names (id, attr_name, type, dsc, namespace, friendly_name, display_name, is_unique, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
							"values (?,?,?,?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					attributeId, attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.getNamespace(), attribute.getFriendlyName(), attribute.getDisplayName(), attribute.isUnique(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			attribute.setId(attributeId);
			log.debug("Attribute created: {}.", attribute);

			if(!CacheManager.isCacheDisabled()) perun.getCacheManager().setAttributeDefinition(attribute);
			return attribute;
		} catch (DataIntegrityViolationException e) {
			throw new AttributeDefinitionExistsException("Attribute " + attribute.getName() + " already exists", attribute, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	/**
	 * Converts attribute definition namespace to entity name used as a prefix for database table names.
	 * E.g. "urn:perun:ues:attribute-def" to "user_ext_source".
	 *
	 * @param attributeDefinition attribute definition
	 * @return entity name
	 */
	private static String attributeToTablePrefix(AttributeDefinition attributeDefinition) {
		return ENTITIES_TO_BEANS_MAP.get(attributeDefinition.getNamespace().split(":")[2]);
	}

	@Override
	public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		try {

			jdbc.update("DELETE FROM "+ attributeToTablePrefix(attribute)+"_attr_values WHERE attr_id=?", attribute.getId());
			jdbc.update("DELETE FROM attr_names WHERE id=?", attribute.getId());
			if(!CacheManager.isCacheDisabled()) perun.getCacheManager().deleteAttribute(attribute.getId(), sess, this);

			log.debug("Attribute deleted: {}.", attribute);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteAllAttributeAuthz(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM attributes_authz WHERE attr_id=?", attribute.getId())) {
				log.debug("All attribute_authz were deleted for {}.", attribute);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<Integer> attrIds = getRequiredAttributeIds(resource);
			List<AttributeDefinition> attrDefs = perun.getCacheManager().getAttributesDefinitions(attrIds);
			if(attrDefs.isEmpty()) log.debug("None resource required attributes definitions found for resource: {}", resource);
			return attrDefs;
		}

		try {
			return jdbc.query("SELECT DISTINCT " + attributeDefinitionMappingSelectQuery + " FROM resource_services " +
							"JOIN service_required_attrs ON service_required_attrs.service_id=resource_services.service_id AND resource_services.resource_id=? " +
							"JOIN attr_names ON service_required_attrs.attr_id=attr_names.id",
					ATTRIBUTE_DEFINITION_MAPPER, resource.getId());

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None resource required attributes definitions found for resource: {}.", resource);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Get ids of attributes which are required by services. Services are known from the resource.
	 *
	 * @param resource resource from which services are taken
	 * @return list of attribute ids
	 */
	private List<Integer> getRequiredAttributeIds(Resource resource) {
		return jdbc.queryForList("select distinct service_required_attrs.attr_id from service_required_attrs " +
				"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?", new Object[] {resource.getId()}, Integer.class);
	}

	/**
	 * Get ids of attributes which are required by the service.
	 *
	 * @param service service
	 * @return list of attribute ids
	 */
	private List<Integer> getRequiredAttributeIds(Service service) {
		return jdbc.queryForList("select distinct service_required_attrs.attr_id from service_required_attrs where service_required_attrs.service_id=?",
				new Object[] {service.getId()}, Integer.class);
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resource);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(facility.getId(), Holder.HolderType.FACILITY));
			if(attrs.isEmpty()) log.debug("None required attributes found for facility: {} and services from resource: {}.", facility, resource);
			return this.setValuesOfAttributes(sess, attrs, facility, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
							"left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=? )",
					new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, resource.getId());

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for facility: {} and services from resource: {}.", facility, resource);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			if(attrs.isEmpty()) log.debug("None required attributes found for resource: {} and services from resource to get services from: {}.", resource, resourceToGetServicesFrom);
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
							"left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new SingleBeanAttributeRowMapper<>(sess, this, resource), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services getted from it.", resourceToGetServicesFrom);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER));
			if(attrs.isEmpty()) log.debug("None required attributes found for member: {} and services from resource: {}.", member, resourceToGetServicesFrom);
			return this.setValuesOfAttributes(sess, attrs, member, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("member_attr_values") + " from attr_names " +
							"left join member_attr_values on id=member_attr_values.attr_id and member_attr_values.member_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new SingleBeanAttributeRowMapper<>(sess, this, member), member.getId(), AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT, resourceToGetServicesFrom.getId());

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services getted from it.", resourceToGetServicesFrom);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, member, resource);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new ResourceMemberAttributeRowMapper(sess, this, resource, member), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Facility facility, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resource);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return this.setValuesOfAttributes(sess, attrs, user, facility);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"left join      user_facility_attr_values    usr    on      attr_names.id=usr.attr_id    and   user_id=? and facility_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new UserFacilityAttributeRowMapper(sess, this, user, facility), user.getId(), facility.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT, resource.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, group, resource);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
							"left join      group_resource_attr_values    grp   on      attr_names.id=grp.attr_id    and   grp.group_id=? and grp.resource_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new GroupResourceAttributeRowMapper(sess, this, group, resource), group.getId(), resource.getId(), AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resource);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER));
			return this.setValuesOfAttributes(sess, attrs, user, null);
		}

		try {
			//user and user core attributes
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"left join      user_attr_values    usr    on      attr_names.id=usr.attr_id    and   user_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?) ",
					new SingleBeanAttributeRowMapper<>(sess, this, user), user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT, resource.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(host.getId(), Holder.HolderType.HOST));
			return this.setValuesOfAttributes(sess, attrs, host, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
							"left join      host_attr_values   host    on      attr_names.id=host.attr_id    and   host_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new SingleBeanAttributeRowMapper<>(sess, this, host), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT, resourceToGetServicesFrom.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
							"left join      group_attr_values   grp    on      attr_names.id=grp.attr_id    and   group_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new SingleBeanAttributeRowMapper<>(sess, this, group), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT, AttributesManager.NS_GROUP_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			return perun.getCacheManager().getAttributesDefinitions(attrIds);
		}

		try {
			return jdbc.query("SELECT " + attributeDefinitionMappingSelectQuery + " FROM attr_names, service_required_attrs WHERE id=attr_id AND service_id=?", ATTRIBUTE_DEFINITION_MAPPER, service.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(facility.getId(), Holder.HolderType.FACILITY));
			if(attrs.isEmpty()) log.debug("None required attributes found for facility: {} and service: {}.", facility, service);
			return this.setValuesOfAttributes(sess, attrs, facility, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join facility_attr_values on id=facility_attr_values.attr_id and facility_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, facility), service.getId(), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT);

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for facility: {} and service: {}.", facility, service);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(vo.getId(), Holder.HolderType.VO));
			if(attrs.isEmpty()) log.debug("None required attributes found for vo: {} and service: {}.", vo, service);
			return this.setValuesOfAttributes(sess, attrs, vo, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("vo_attr_values") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join vo_attr_values on id=vo_attr_values.attr_id and vo_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, vo), service.getId(), vo.getId(), AttributesManager.NS_VO_ATTR_DEF, AttributesManager.NS_VO_ATTR_CORE, AttributesManager.NS_VO_ATTR_OPT, AttributesManager.NS_VO_ATTR_VIRT);

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for vo: {} and service: {}.", vo, service);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			if(attrs.isEmpty()) log.debug("None required attributes found for resource: {} and service {} ", resource, service);
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, resource), service.getId(), resource.getId(), AttributesManager.NS_RESOURCE_ATTR_DEF, AttributesManager.NS_RESOURCE_ATTR_CORE, AttributesManager.NS_RESOURCE_ATTR_OPT, AttributesManager.NS_RESOURCE_ATTR_VIRT);

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and service {}.", resource, service);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, List<Integer> serviceIds) throws InternalErrorException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("serviceIds", serviceIds);

		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = this.namedParameterJdbcTemplate.queryForList("select distinct service_required_attrs.attr_id from service_required_attrs " +
							"where service_required_attrs.service_id in (:serviceIds)",
					parameters, Integer.class);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			if(attrs.isEmpty()) log.debug("None required attributes found for resource: {} and services with id {} ", resource, serviceIds);
			return this.setValuesOfAttributes(sess, attrs, resource, null);
		}

		try {
			List<String> namespace = new ArrayList<>();
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_DEF);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_CORE);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_OPT);
			namespace.add(AttributesManager.NS_RESOURCE_ATTR_VIRT);

			parameters.addValue("resourceId", resource.getId());
			parameters.addValue("namespace", namespace);

			return this.namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("resource_attr_values") + " from attr_names "
							+ "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id in (:serviceIds) "
							+ "left join resource_attr_values on id=resource_attr_values.attr_id and resource_attr_values.resource_id=:resourceId "
							+ "where namespace in (:namespace)",
					parameters, new SingleBeanAttributeRowMapper<>(sess, this, resource));

		} catch (EmptyResultDataAccessException ex) {
			log.debug("None required attributes found for resource: {} and services with id {}.", resource, serviceIds);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, member, resource);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +

							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +

							"left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " +
							"where namespace in (?,?,?)",
					new ResourceMemberAttributeRowMapper(sess, this, resource, member), service.getId(), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, member, group);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, member), service.getId(), group.getId(), member.getId(), AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(resourceToGetServicesFrom);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, member, group);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem_gr") + " from attr_names " +
							"left join member_group_attr_values mem_gr on id=mem_gr.attr_id and mem_gr.group_id=? and member_id=? " +
							"where namespace in (?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=?)",
					new MemberGroupAttributeRowMapper(sess, this, member, group), group.getId(), member.getId(), AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT, resourceToGetServicesFrom.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER));
			return this.setValuesOfAttributes(sess, attrs, member, null);
		}

		//member and member core attributes
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join      member_attr_values    mem    on      id=mem.attr_id    and   member_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, member), service.getId(), member.getId(), AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return this.setValuesOfAttributes(sess, attrs, user, facility);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr_fac") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join    user_facility_attr_values     usr_fac      on id=usr_fac.attr_id     and   facility_id=? and user_id=? " +
							"where namespace in (?,?,?)",
					new UserFacilityAttributeRowMapper(sess, this, user, facility), service.getId(), facility.getId(), user.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
			return this.setValuesOfAttributes(sess, attrs, group, resource);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join    group_resource_attr_values     grp_res     on id=grp_res.attr_id     and   group_id=? and resource_id=? " +
							"where namespace in (?,?,?)",
					new GroupResourceAttributeRowMapper(sess, this, group, resource), service.getId(), group.getId(), resource.getId(), AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF, AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER));
			return this.setValuesOfAttributes(sess, attrs, user, null);
		}

		//user and user core attributes
		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join      user_attr_values    usr    on      id=usr.attr_id    and   user_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, user), service.getId(), user.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
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
		 * Sets up parameters for data extractor
		 * For memberResource attributes we need also know the resource.
		 *
		 * @param sess              perun session
		 * @param attributesManager attribute manager
		 * @param resource          resource for member resource attributes
		 * @param members           list of members
		 */
		MemberAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, Resource resource, List<Member> members) {
			this.sess = sess;
			this.attributesManager = attributesManager;
			this.members = members;
			this.resource = resource;
		}

		@Override
		public HashMap<Member, List<Attribute>> extractData(ResultSet rs) throws SQLException, DataAccessException {
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
					attributeRowMapper = new ResourceMemberAttributeRowMapper(sess, attributesManager, resource, mem);
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

	private static class UserAttributeExtractor implements ResultSetExtractor<HashMap<User, List<Attribute>>> {
		private final PerunSession sess;
		private final AttributesManagerImpl attributesManager;
		private final List<User> users;
		private final Facility facility;

		/**
		 * Sets up parameters for data extractor
		 *
		 * @param sess              perun session
		 * @param attributesManager attribute manager
		 * @param users             list of users
		 */
		UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users, Facility facility) {
			this.sess = sess;
			this.attributesManager = attributesManager;
			this.users = users;
			this.facility = facility;
		}

		UserAttributeExtractor(PerunSession sess, AttributesManagerImpl attributesManager, List<User> users) {
			this(sess, attributesManager, users, null);
		}

		@Override
		public HashMap<User, List<Attribute>> extractData(ResultSet rs) throws SQLException, DataAccessException {
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

				AttributeRowMapper attributeRowMapper = new UserFacilityAttributeRowMapper(sess, attributesManager, user, facility);
				Attribute attribute = attributeRowMapper.mapRow(rs, rs.getRow());

				if (attribute != null) {
					// add only if exists
					map.get(user).add(attribute);
				}
			}
			return map;
		}
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			HashMap<Member, List<Attribute>> hashMap = new HashMap<>();
			for (Member member: members) {
				List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				List<Attribute> setAttrs = this.setValuesOfAttributes(sess, attrs, member, resource);
				hashMap.put(member, setAttrs);
			}
			return hashMap;
		}

		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN members ON " + BeansUtils.prepareInSQLClause("members.id", members) +
							"LEFT JOIN member_resource_attr_values mem ON attr_names.id=mem.attr_id AND mem.resource_id=? " +
							"AND mem.member_id=members.id WHERE namespace IN (?,?,?)",
					new MemberAttributeExtractor(sess, this, resource, members), service.getId(), resource.getId(),
					AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			HashMap<Member, List<Attribute>> hashMap = new HashMap<>();
			for (Member member: members) {
				List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(member.getId(), Holder.HolderType.MEMBER));
				List<Attribute> setAttrs = this.setValuesOfAttributes(sess, attrs, member, null);
				hashMap.put(member, setAttrs);
			}
			return hashMap;
		}

	    try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("mem") + ", members.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN members ON " + BeansUtils.prepareInSQLClause("members.id", members) +
							"LEFT JOIN member_attr_values mem ON attr_names.id=mem.attr_id " +
							"AND mem.member_id=members.id WHERE namespace IN (?,?,?,?)",
					new MemberAttributeExtractor(sess, this, members), service.getId(),
					AttributesManager.NS_MEMBER_ATTR_CORE, AttributesManager.NS_MEMBER_ATTR_DEF, AttributesManager.NS_MEMBER_ATTR_OPT, AttributesManager.NS_MEMBER_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new HashMap<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			HashMap<User, List<Attribute>> hashMap = new HashMap<>();
			for (User user: users) {
				List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
				List<Attribute> setAttrs = this.setValuesOfAttributes(sess, attrs, user, facility);
				hashMap.put(user, setAttrs);
			}
			return hashMap;
		}

		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("usr_fac") + ", users.id FROM attr_names " +
							"JOIN service_required_attrs ON attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN users ON " + BeansUtils.prepareInSQLClause("users.id", users) +
							"LEFT JOIN user_facility_attr_values usr_fac ON attr_names.id=usr_fac.attr_id AND facility_id=? AND user_id=users.id " +
							"WHERE namespace IN (?,?,?)",
					new UserAttributeExtractor(sess, this, users, facility), service.getId(), facility.getId(), AttributesManager.NS_USER_FACILITY_ATTR_DEF, AttributesManager.NS_USER_FACILITY_ATTR_OPT, AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new HashMap<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			HashMap<User, List<Attribute>> hashMap = new HashMap<>();
			for (User user: users) {
				List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(user.getId(), Holder.HolderType.USER));
				List<Attribute> setAttrs = this.setValuesOfAttributes(sess, attrs, user, null);
				hashMap.put(user, setAttrs);
			}
			return hashMap;
		}

		//user and user core attributes
		try {
			return jdbc.query("SELECT " + getAttributeMappingSelectQuery("usr") + ", users.id FROM attr_names " +
							"JOIN service_required_attrs on attr_names.id=service_required_attrs.attr_id AND service_required_attrs.service_id=? " +
							"JOIN users ON " + BeansUtils.prepareInSQLClause("users.id", users) +
							"LEFT JOIN user_attr_values usr ON attr_names.id=usr.attr_id AND user_id=users.id " +
							"WHERE namespace IN (?,?,?,?)",
					new UserAttributeExtractor(sess, this, users), service.getId(), AttributesManager.NS_USER_ATTR_CORE, AttributesManager.NS_USER_ATTR_DEF, AttributesManager.NS_USER_ATTR_OPT, AttributesManager.NS_USER_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new HashMap<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(host.getId(), Holder.HolderType.HOST));
			return this.setValuesOfAttributes(sess, attrs, host, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("host") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join      host_attr_values    host   on      id=host.attr_id    and   host_id=? " +
							"where namespace in (?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, host), service.getId(), host.getId(), AttributesManager.NS_HOST_ATTR_CORE, AttributesManager.NS_HOST_ATTR_DEF, AttributesManager.NS_HOST_ATTR_OPT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = getRequiredAttributeIds(service);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(group.getId(), Holder.HolderType.GROUP));
			return this.setValuesOfAttributes(sess, attrs, group, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("grp") + " from attr_names " +
							"join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +
							"left join      group_attr_values    grp   on      id=grp.attr_id    and  group_id=? " +
							"where namespace in (?,?,?,?)",
					new SingleBeanAttributeRowMapper<>(sess, this, group), service.getId(), group.getId(), AttributesManager.NS_GROUP_ATTR_CORE, AttributesManager.NS_GROUP_ATTR_DEF, AttributesManager.NS_GROUP_ATTR_OPT, AttributesManager.NS_GROUP_ATTR_VIRT);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) {
			List<Integer> attrIds = jdbc.queryForList("select distinct service_required_attrs.attr_id from service_required_attrs " +
					"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
					"join resources on resource_services.resource_id=resources.id and resources.facility_id=?", new Object[] {facility.getId()}, Integer.class);
			List<Attribute> attrs = perun.getCacheManager().getAttributesByIds(attrIds, new Holder(facility.getId(), Holder.HolderType.FACILITY));
			return this.setValuesOfAttributes(sess, attrs, facility, null);
		}

		try {
			return jdbc.query("select " + getAttributeMappingSelectQuery("facility_attr_values") + " from attr_names " +
							"left join facility_attr_values on attr_names.id=facility_attr_values.attr_id and facility_attr_values.facility_id=? " +
							"where namespace in (?,?,?,?) " +
							"and attr_names.id in (select distinct service_required_attrs.attr_id from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
							"join resources on resource_services.resource_id=resources.id  and resources.facility_id=?)",
					new SingleBeanAttributeRowMapper<>(sess, this, facility), facility.getId(), AttributesManager.NS_FACILITY_ATTR_DEF, AttributesManager.NS_FACILITY_ATTR_CORE, AttributesManager.NS_FACILITY_ATTR_OPT, AttributesManager.NS_FACILITY_ATTR_VIRT, facility.getId());
		} catch (EmptyResultDataAccessException ex) {
			log.info("None required attributes found for facility: {} and services from it's resources.", facility);
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		ResourceMemberAttributesModuleImplApi attributeModule = getResourceMemberAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		MemberGroupAttributesModuleImplApi attributeModule = getMemberGroupAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, member, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException {
		//Use attributes module
		FacilityUserAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}

		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, facility, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException {
		UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException {
		MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException {
		ResourceGroupAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, resource, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException {
		HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, host, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException {
		GroupAttributesModuleImplApi attributeModule = getGroupAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException {
		UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
		if (attributeModule == null) {
			log.debug("Attribute wasn't filled. There is no module for: {}.", attribute.getName());
			return attribute;
		}
		try {
			return attributeModule.fillAttribute((PerunSessionImpl) sess, ues, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
		if (facilityModule == null) return; //facility module doesn't exists
		try {
			facilityModule.changedAttributeHook((PerunSessionImpl) sess, facility, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		EntitylessAttributesModuleImplApi entitylessModule = getEntitylessAttributeModule(sess, attribute);
		if (entitylessModule == null) return; //facility module doesn't exists
		try {
			entitylessModule.changedAttributeHook((PerunSessionImpl) sess, key, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		HostAttributesModuleImplApi hostModule = getHostAttributeModule(sess, attribute);
		if (hostModule == null) return; //host module doesn't exists
		try {
			hostModule.changedAttributeHook((PerunSessionImpl) sess, host, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
		if (voModule == null) return; //facility module doesn't exists
		try {
			voModule.changedAttributeHook((PerunSessionImpl) sess, vo, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
		if (groupModule == null) return; //facility module doesn't exists
		try {
			groupModule.changedAttributeHook((PerunSessionImpl) sess, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		UserAttributesModuleImplApi userModule = getUserAttributeModule(sess, attribute);
		if (userModule == null) return; //facility module doesn't exists
		try {
			userModule.changedAttributeHook((PerunSessionImpl) sess, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberAttributesModuleImplApi memberModule = getMemberAttributeModule(sess, attribute);
		if (memberModule == null) return; //facility module doesn't exists
		try {
			memberModule.changedAttributeHook((PerunSessionImpl) sess, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceGroupAttributesModuleImplApi resourceGroupModule = getResourceGroupAttributeModule(sess, attribute);
		if (resourceGroupModule == null) return; //facility module doesn't exists
		try {
			resourceGroupModule.changedAttributeHook((PerunSessionImpl) sess, resource, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceAttributesModuleImplApi resourceModule = getResourceAttributeModule(sess, attribute);
		if (resourceModule == null) return; //facility module doesn't exists
		try {
			resourceModule.changedAttributeHook((PerunSessionImpl) sess, resource, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceMemberAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
		if (resourceMemberGroupModule == null) return; //facility module doesn't exists
		try {
			resourceMemberGroupModule.changedAttributeHook((PerunSessionImpl) sess, resource, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberGroupAttributesModuleImplApi memberGroupAttributesModule = getMemberGroupAttributeModule(sess, attribute);
		if (memberGroupAttributesModule == null) return; //memberGroupAttributesModule module doesn't exists
		try {
			memberGroupAttributesModule.changedAttributeHook((PerunSessionImpl) sess, member, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityUserAttributesModuleImplApi facilityUserModule = getFacilityUserAttributeModule(sess, attribute);
		if (facilityUserModule == null) return; //facility module doesn't exists
		try {
			facilityUserModule.changedAttributeHook((PerunSessionImpl) sess, facility, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void changedAttributeHook(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		UserExtSourceAttributesModuleImplApi uesModule = getUserExtSourceAttributeModule(sess, attribute);
		if (uesModule == null) return;
		try {
			uesModule.changedAttributeHook((PerunSessionImpl) sess, ues, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
		if (facilityModule == null) return; //facility module doesn't exists
		try {
			facilityModule.checkAttributeValue((PerunSessionImpl) sess, facility, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		VoAttributesModuleImplApi voModule = getVoAttributeModule(sess, attribute);
		if (voModule == null) return; //module doesn't exists
		try {
			voModule.checkAttributeValue((PerunSessionImpl) sess, vo, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		GroupAttributesModuleImplApi groupModule = getGroupAttributeModule(sess, attribute);
		if (groupModule == null) return; //module doesn't exists
		try {
			groupModule.checkAttributeValue((PerunSessionImpl) sess, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		ResourceAttributesModuleImplApi attributeModule = getResourceAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, resource, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@SuppressWarnings("unused")
	public void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		for (Attribute attribute : attributes) {
			checkAttributeValue(sess, resource, attribute);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		ResourceMemberAttributesModuleImplApi resourceMemberGroupModule = getResourceMemberAttributeModule(sess, attribute);
		if (resourceMemberGroupModule == null) return; //facility module doesn't exists
		try {
			resourceMemberGroupModule.checkAttributeValue((PerunSessionImpl) sess, resource, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//Call attribute module
		MemberGroupAttributesModuleImplApi memberGroupAttributeModule = getMemberGroupAttributeModule(sess, attribute);
		if (memberGroupAttributeModule == null) return; //memberGroupAttributesModule module doesn't exists
		try {
			memberGroupAttributeModule.checkAttributeValue((PerunSessionImpl) sess, member, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		FacilityUserAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, user, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		MemberAttributesModuleImplApi attributeModule = getMemberAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, member, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		UserExtSourceAttributesModuleImplApi attributeModule = getUserExtSourceAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, ues, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		HostAttributesModuleImplApi attributeModule = getHostAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, host, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		ResourceGroupAttributesModuleImplApi attributeModule = getResourceGroupAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkAttributeValue(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		EntitylessAttributesModuleImplApi attributeModule = getEntitylessAttributeModule(sess, attribute);
		if (attributeModule == null) return;
		try {
			attributeModule.checkAttributeValue((PerunSessionImpl) sess, key, attribute);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM entityless_attr_values WHERE attr_id=? AND subject=?", attribute.getId(), key)) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeEntitylessAttribute(attribute, key);
				log.debug("Attribute value for {} with key {} was removed from entityless attributes.", attribute.getName(), key);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("DELETE FROM group_resource_attr_values WHERE resource_id=?", resource.getId());
			if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(Holder.HolderType.GROUP, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("DELETE FROM member_resource_attr_values WHERE resource_id=?", resource.getId());
			if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(Holder.HolderType.MEMBER, new Holder(resource.getId(), Holder.HolderType.RESOURCE));
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM facility_attr_values WHERE attr_id=? AND facility_id=?", attribute.getId(), facility.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(facility.getId(), Holder.HolderType.FACILITY), null);
				log.debug("Attribute value for {} was removed from facility {}.", attribute.getName(), facility);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM facility_attr_values WHERE facility_id=?", facility.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(facility.getId(), Holder.HolderType.FACILITY));
				log.debug("All attributes values were removed from facility {}.", facility);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM vo_attr_values WHERE attr_id=? AND vo_id=?", attribute.getId(), vo.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(vo.getId(), Holder.HolderType.VO), null);
				log.debug("Attribute value for {} was removed from vo {}.", attribute.getName(), vo);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM vo_attr_values WHERE vo_id=?", vo.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(vo.getId(), Holder.HolderType.VO));
				log.debug("All attributes values were removed from vo {}.", vo);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM group_attr_values WHERE attr_id=? AND group_id=?", attribute.getId(), group.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(group.getId(), Holder.HolderType.GROUP), null);
				log.debug("Attribute value for {} was removed from group {}.", attribute.getName(), group);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM group_attr_values WHERE group_id=?", group.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(group.getId(), Holder.HolderType.GROUP));
				log.debug("All attributes values were removed from group {}.", group);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM resource_attr_values WHERE attr_id=? AND resource_id=?", attribute.getId(), resource.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(resource.getId(), Holder.HolderType.RESOURCE), null);
				log.debug("Attribute value for {} was removed from resource {}.", attribute.getName(), resource);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM resource_attr_values WHERE resource_id=?", resource.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				log.debug("All attributes values were removed from resource {}.", resource);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_resource_attr_values WHERE attr_id=? AND member_id=? AND resource_id=?", attribute.getId(), member.getId(), resource.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				log.debug("Attribute value for {} was removed from member {} on resource {}.", attribute.getName(), member, resource);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_resource_attr_values WHERE resource_id=? AND member_id=?", resource.getId(), member.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				log.debug("All attributes values were removed from member {} on resource {}.", member, resource);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_group_attr_values WHERE attr_id=? AND member_id=? AND group_id=?", attribute.getId(), member.getId(), group.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
				log.debug("Attribute value {} was removed from member {} in group {}.", attribute, member, group);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_group_attr_values WHERE group_id=? AND member_id=?", group.getId(), member.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER), new Holder(group.getId(), Holder.HolderType.GROUP));
				log.debug("All attributes values were removed from member {} in group {}.", member, group);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_attr_values WHERE attr_id=? AND member_id=?", attribute.getId(), member.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(member.getId(), Holder.HolderType.MEMBER), null);
				log.debug("Attribute value {} was removed from member {}", attribute, member);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM member_attr_values WHERE member_id=?", member.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(member.getId(), Holder.HolderType.MEMBER));
				log.debug("All attributes values were removed from member {}", member);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE attr_id=? AND user_id=? AND facility_id=?", attribute.getId(), user.getId(), facility.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
				log.debug("Attribute value {} was removed from user {} on facility {}.", attribute, user, facility);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE user_id=? AND facility_id=?", user.getId(), facility.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(user.getId(), Holder.HolderType.USER), new Holder(facility.getId(), Holder.HolderType.FACILITY));
				log.debug("All attributes values were removed from user {} on facility {}.", user, facility);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE facility_id=?", facility.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(Holder.HolderType.USER, new Holder(facility.getId(), Holder.HolderType.FACILITY));
				log.debug("All attributes values were removed from any user on facility {}.", facility);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllUserFacilityAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_facility_attr_values WHERE user_id=?", user.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(user.getId(), Holder.HolderType.USER), Holder.HolderType.FACILITY);
				log.debug("All attributes values were removed from user {} on  all facilities.", user);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeVirtualAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
		return getFacilityUserVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
	}

	@Override
	public boolean removeVirtualAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return getResourceVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, attribute);
	}

	@Override
	public boolean removeVirtualAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return getResourceGroupVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
	}

	@Override
	public boolean removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_attr_values WHERE attr_id=? AND user_id=?", attribute.getId(), user.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(user.getId(), Holder.HolderType.USER), null);
				log.debug("Attribute value for {} was removed from user {}.", attribute.getName(), user);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_attr_values WHERE user_id=?", user.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(user.getId(), Holder.HolderType.USER));
				log.debug("All attributes values were removed from user {}.", user);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM group_resource_attr_values WHERE attr_id=? AND resource_id=? AND group_id=?", attribute.getId(), resource.getId(), group.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				log.debug("Attribute value for {} was removed from group {} on resource {}.", attribute.getName(), group, resource);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM group_resource_attr_values WHERE group_id=? AND resource_id=?", group.getId(), resource.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(group.getId(), Holder.HolderType.GROUP), new Holder(resource.getId(), Holder.HolderType.RESOURCE));
				log.debug("All attributes values were removed from group {} on resource{}.", group, resource);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM host_attr_values WHERE attr_id=? AND host_id=?", attribute.getId(), host.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAttribute(attribute, new Holder(host.getId(), Holder.HolderType.HOST), null);
				log.debug("Attribute value for {} was removed from host {}.", attribute.getName(), host);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Host host) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM host_attr_values WHERE host_id=?", host.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(host.getId(), Holder.HolderType.HOST));
				log.debug("All attributes values were removed from host {}.", host);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_ext_source_attr_values WHERE attr_id=? AND user_ext_source_id=?", attribute.getId(), ues.getId())) {
				if (!CacheManager.isCacheDisabled()) {
					perun.getCacheManager().removeAttribute(attribute, new Holder(ues.getId(), Holder.HolderType.UES), null);
				}
				log.debug("Attribute value for {} was removed from user external source {}.", attribute.getName(), ues);
				return true;
			}
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		try {
			if (0 < jdbc.update("DELETE FROM user_ext_source_attr_values WHERE user_ext_source_id=?", ues.getId())) {
				if (!CacheManager.isCacheDisabled()) perun.getCacheManager().removeAllAttributes(new Holder(ues.getId(), Holder.HolderType.UES));
				log.debug("All attributes values were removed from user external source {}.", ues);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean attributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Utils.notNull(attribute, "attribute");
		Utils.notNull(attribute.getName(), "attribute.name");
		Utils.notNull(attribute.getNamespace(), "attribute.namespace");
		Utils.notNull(attribute.getType(), "attribute.type");

		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().checkAttributeExists(attribute);

		try {
			return 1 == jdbc.queryForInt("select count('x') from attr_names where attr_name=? and friendly_name=? and namespace=? and id=? and type=?", attribute.getName(), attribute.getFriendlyName(), attribute.getNamespace(), attribute.getId(), attribute.getType());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	private boolean actionTypeExists(ActionType actionType) throws InternalErrorException {
		Utils.notNull(actionType, "actionType");
		Utils.notNull(actionType.getActionType(), "actionType.actionType");

		try {
			return 1 == jdbc.queryForInt("select count('x') from action_types where action_type=?", actionType.getActionType());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void checkActionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException, ActionTypeNotExistsException {
		if (!actionTypeExists(actionType)) throw new ActionTypeNotExistsException("ActionType: " + actionType);
	}

	@Override
	public void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeNotExistsException {
		if (!attributeExists(sess, attribute)) throw new AttributeNotExistsException("Attribute: " + attribute);
	}

	private void checkAttributeExists(PerunSession sess, AttributeDefinition attribute, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		checkAttributeExists(sess, attribute);
		checkNamespace(sess, attribute, expectedNamespace);
	}

	@Override
	public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotExistsException {
		Utils.notNull(attributes, "attributes");
		for (AttributeDefinition attribute : attributes) {
			checkAttributeExists(sess, attribute);
		}
	}

	@SuppressWarnings("unused")
	public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.notNull(attributes, "attributes");
		for (AttributeDefinition attribute : attributes) {
			checkAttributeExists(sess, attribute, expectedNamespace);
		}
	}

	@Override
	public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":core");
	}

	@Override
	public boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":def");
	}

	@Override
	public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":opt");
	}

	@Override
	public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().matches("urn:perun:[^:]+:attribute-def:core-managed:[a-zA-Z]+Manager");
	}

	@Override
	public boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().endsWith(":virt");
	}

	@Override
	public boolean isFromNamespace(AttributeDefinition attribute, String namespace) {
		if (attribute == null)
			throw new InternalErrorRuntimeException(new NullPointerException("Attribute attribute is null"));
		if (attribute.getNamespace() == null)
			throw new InternalErrorRuntimeException(new NullPointerException("String attribute.namespace is null"));
		return attribute.getNamespace().startsWith(namespace + ":") || attribute.getNamespace().equals(namespace);
	}

	public boolean isLargeAttribute(PerunSession sess, AttributeDefinition attribute) {
		return (attribute.getType().equals(LinkedHashMap.class.getName()) ||
				attribute.getType().equals(BeansUtils.largeStringClassName) ||
				attribute.getType().equals(BeansUtils.largeArrayListClassName));
	}

	@Override
	public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException {
		if (!isFromNamespace(attribute, namespace)) throw new WrongAttributeAssignmentException(attribute);
	}

	@Override
	public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException {
		for (AttributeDefinition attribute : attributes) {
			checkNamespace(sess, attribute, namespace);
		}
	}

	@Override
	public List<Object> getAllResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAllValues(Holder.HolderType.RESOURCE, attributeDefinition);

		try {
			return jdbc.query("SELECT attr_value FROM resource_attr_values WHERE attr_id=?", new ValueRowMapper(sess, attributeDefinition), attributeDefinition.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Object> getAllGroupResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAllValues(Holder.HolderType.GROUP, Holder.HolderType.RESOURCE, attributeDefinition);

		try {
			return jdbc.query("SELECT attr_value FROM group_resource_attr_values WHERE attr_id=?", new ValueRowMapper(sess, attributeDefinition), attributeDefinition.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Object> getAllGroupValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		if(!CacheManager.isCacheDisabled() && !perun.getCacheManager().wasCacheUpdatedInTransaction()) return perun.getCacheManager().getAllValues(Holder.HolderType.GROUP, attributeDefinition);

		try {
			return jdbc.query("SELECT attr_value FROM group_attr_values WHERE attr_id=?", new ValueRowMapper(sess, attributeDefinition), attributeDefinition.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean isAttributeRequiredByFacility(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
							"join resources on resource_services.resource_id=resources.id " +
							"where service_required_attrs.attr_id=? and resources.facility_id=?",
					attributeDefinition.getId(), facility.getId());
		} catch (RuntimeException ex) {
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
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean isAttributeRequiredByGroup(PerunSession sess, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id " +
							"join groups_resources on resource_services.resource_id=groups_resources.resource_id " +
							"where service_required_attrs.attr_id=? and groups_resources.group_id=?",
					attributeDefinition.getId(), group.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean isAttributeRequiredByResource(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return 0 < jdbc.queryForInt("select count(*) from service_required_attrs " +
							"join resource_services on service_required_attrs.service_id=resource_services.service_id and resource_services.resource_id=? " +
							"where service_required_attrs.attr_id=?",
					resource.getId(), attributeDefinition.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
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
	 * @param attribute attribute for which you get the module
	 * @return instance group attribute module
	 * null if the module doesn't exists
	 */
	private GroupAttributesModuleImplApi getGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof GroupAttributesModuleImplApi) {
			return (GroupAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	/**
	 * Get Host attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance host attribute module
	 * null if the module doesn't exists
	 */
	private HostAttributesModuleImplApi getHostAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof HostAttributesModuleImplApi) {
			return (HostAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't HostAttributesModule");
		}
	}

	/**
	 * Get Vo attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance vo attribute module
	 * null if the module doesn't exists
	 */
	private VoAttributesModuleImplApi getVoAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof VoAttributesModuleImplApi) {
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
	 * null if the module doesn't exists
	 */
	private ResourceMemberAttributesModuleImplApi getResourceMemberAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof ResourceMemberAttributesModuleImplApi) {
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
	 * null if the module doesn't exists
	 */
	private MemberGroupAttributesModuleImplApi getMemberGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof MemberGroupAttributesModuleImplApi) {
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
	 * null if the module doesn't exists
	 */
	private FacilityAttributesModuleImplApi getFacilityAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof FacilityAttributesModuleImplApi) {
			return (FacilityAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityAttributesModule");
		}
	}

	private EntitylessAttributesModuleImplApi getEntitylessAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof EntitylessAttributesModuleImplApi) {
			return (EntitylessAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't EntitylessAttributesModuleImplApi");
		}
	}

	/**
	 * Get facility virtual attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance facility attribute module
	 */
	private FacilityVirtualAttributesModuleImplApi getFacilityVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof FacilityVirtualAttributesModuleImplApi) {
			return (FacilityVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityVirtualAttributesModule");
		}
	}

	/**
	 * Get resource virtual attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance of resource attribute module
	 */
	private ResourceVirtualAttributesModuleImplApi getResourceVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof ResourceVirtualAttributesModuleImplApi) {
			return (ResourceVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't ResourceVirtualAttributesModule");
		}
	}


	@Override
	public UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof UserVirtualAttributesModuleImplApi) {
			return (UserVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't UserVirtualAttributesModule");
		}
	}

	/**
	 * Get member virtual attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance of member attribute module
	 */
	private MemberVirtualAttributesModuleImplApi getMemberVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof MemberVirtualAttributesModuleImplApi) {
			return (MemberVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't MemberVirtualAttributesModule");
		}
	}

	/**
	 * Get user-facility attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance user-facility attribute module, null if the module doesn't exists
	 */
	private FacilityUserAttributesModuleImplApi getFacilityUserAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof FacilityUserAttributesModuleImplApi) {
			return (FacilityUserAttributesModuleImplApi) attributeModule;
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
	private FacilityUserVirtualAttributesModuleImplApi getFacilityUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof FacilityUserVirtualAttributesModuleImplApi) {
			return (FacilityUserVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't FacilityUserVirtualAttributesModule");
		}
	}

	/**
	 * Get user attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance userattribute module, null if the module doesn't exists
	 */
	private UserAttributesModuleImplApi getUserAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof UserAttributesModuleImplApi) {
			return (UserAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserAttributesModule. " + attribute);
		}
	}

	/**
	 * Get member attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance userattribute module, null if the module doesn't exists
	 */
	private MemberAttributesModuleImplApi getMemberAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof MemberAttributesModuleImplApi) {
			return (MemberAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't MemberAttributesModule. " + attribute);
		}
	}

	/**
	 * Get resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance resource attribute module, null if the module doesn't exists
	 */
	private ResourceAttributesModuleImplApi getResourceAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

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
	private ResourceGroupAttributesModuleImplApi getResourceGroupAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;
		if (attributeModule instanceof ResourceGroupAttributesModuleImplApi) {
			return (ResourceGroupAttributesModuleImplApi) attributeModule;

		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceGroupAttributesModule");
		}
	}

	/**
	 * Get user external source attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance user ext source attribute module, null if the module doesn't exists
	 */
	private UserExtSourceAttributesModuleImplApi getUserExtSourceAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof UserExtSourceAttributesModuleImplApi) {
			return (UserExtSourceAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserExtSourceAttributesModule. " + attribute);
		}
	}

	/**
	 * Get group-resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance group-resource attribute module, null if the module doesn't exists
	 */
	ResourceGroupVirtualAttributesModuleImplApi getResourceGroupVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof ResourceGroupVirtualAttributesModuleImplApi) {
			return (ResourceGroupVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't ResourceGroupVirtualAttributesModule");
		}
	}

	/**
	 * Get member-resource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance member-resource attribute module, null if the module doesn't exists
	 */
	ResourceMemberVirtualAttributesModuleImplApi getResourceMemberVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof ResourceMemberVirtualAttributesModuleImplApi) {
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
	 */
	MemberGroupVirtualAttributesModuleImplApi getMemberGroupVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null) return null;

		if (attributeModule instanceof MemberGroupVirtualAttributesModuleImplApi) {
			return (MemberGroupVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't MemberGroupVirtualAttributesModuleImplApi");
		}
	}

	/**
	 * Get UserExtSource attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance UserExtSource attribute module, null if the module doesn't exists
	 */
	private UserExtSourceVirtualAttributesModuleImplApi getUserExtSourceVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);

		if (attributeModule == null) {
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");
		}

		if (attributeModule instanceof UserExtSourceVirtualAttributesModuleImplApi) {
			return (UserExtSourceVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new InternalErrorException("Required attribute module isn't UserExtSourceVirtualAttributesModule. " + attribute);
		}
	}

	/**
	 * Get group virtual attribute module for the attribute.
	 *
	 * @param attribute attribute for which you get the module
	 * @return instance of member attribute module
	 */
	private GroupVirtualAttributesModuleImplApi getGroupVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		Object attributeModule = getAttributesModule(sess, attribute);
		if (attributeModule == null)
			throw new ModuleNotExistsException("Virtual attribute module for " + attribute + " doesn't exists.");

		if (attributeModule instanceof GroupVirtualAttributesModuleImplApi) {
			return (GroupVirtualAttributesModuleImplApi) attributeModule;
		} else {
			throw new WrongModuleTypeException("Required attribute module isn't GroupVirtualAttributesModule");
		}
	}

	/**
	 * Get the attribute module for the attribute
	 *
	 * @param attribute get the attribute module for this attribute
	 * @see #getAttributesModule(String)
	 */
	@Override
	public Object getAttributesModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		String moduleName;
		//first try to find specific module including parameter of attribute (full friendly name)
		if (!attribute.getFriendlyName().equals(attribute.getBaseFriendlyName())) {
			moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getFriendlyName());
			Object attributeModule = getAttributesModule(moduleName);
			if (attributeModule != null) return attributeModule;
		}

		//if specific module not exists or attribute has no parameter, find the common one
		moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName());
		Object attributeModule = getAttributesModule(moduleName);
		if (attributeModule == null) log.debug("Attribute module not found. Module name={}", moduleName);
		return attributeModule;
	}

	/**
	 * Get the attributeModule
	 *
	 * @param moduleName name of the module
	 * @return instance of attribute module
	 * null if attribute doesn't exists
	 */
	private Object getAttributesModule(String moduleName) throws InternalErrorException {
		//try to get already loaded module.
		if (attributesModulesMap.containsKey(moduleName)) return attributesModulesMap.get(moduleName);

		try {
			Class<?> moduleClass = classLoader.loadClass(moduleName);
			log.debug("Attribute module found. Module class={}  Module name={}", moduleClass, moduleName);
			Object module = moduleClass.newInstance();
			attributesModulesMap.put(moduleName, (AttributesModuleImplApi) module);
			return module;
		} catch (ClassNotFoundException ex) {
			//attribute module doesn't exist
			return null;
		} catch (InstantiationException ex) {
			throw new InternalErrorException("Attribute module " + moduleName + " cannot be instantiated.", ex);
		} catch (IllegalAccessException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public void initAttributeModules(ServiceLoader<AttributesModuleImplApi> modules) {
		for (AttributesModuleImplApi module : modules) {
			attributesModulesMap.put(module.getClass().getName(), module);
			log.debug("Module {} loaded.", module.getClass().getSimpleName());
		}
	}

	@Override
	public void registerVirtAttributeModules(ServiceLoader<AttributesModuleImplApi> modules) {
		for (AttributesModuleImplApi module : modules) {
			if (module instanceof VirtualAttributesModuleImplApi) {
				Auditer.registerAttributeModule((VirtualAttributesModuleImplApi) module);
				log.debug("Module {} was registered for audit message listening.", module.getClass().getName());
			}
		}
	}

	@Override
	public Set<Pair<Integer,Integer>> getPerunBeanIdsForUniqueAttributeValue(PerunSession sess, Attribute attribute) {
		if(attribute.getValue()==null) return Collections.emptySet();
		String beanPrefix = attributeToTablePrefix(attribute);
		String sql;
		if(SINGLE_BEAN_ATTRIBUTES.contains(beanPrefix)) {
			sql = "select "+beanPrefix+"_id, 0 from "+beanPrefix+"_attr_u_values where attr_id=? and attr_value=?";
		} else if(DOUBLE_BEAN_ATTRIBUTES.contains(beanPrefix)) {
			String[] s = beanPrefix.split("_");
			String bean1 = s[0];
			String bean2 = s[1];
			sql = "select " + bean1 + "_id," + bean2 + "_id from " + beanPrefix + "_attr_u_values where attr_id=? and attr_value=?";
		} else {
			throw new RuntimeException("getPerunBeanIdsForUniqueAttributeValue() cannot be used for "+beanPrefix);
		}
		RowMapper<Pair<Integer, Integer>> pairRowMapper = (rs, i) -> new Pair<>(rs.getInt(1), rs.getInt(2));
		HashSet<Pair<Integer,Integer>> ids = new HashSet<>();
		switch(attribute.getType()) {
			case "java.lang.String":
			case BeansUtils.largeStringClassName:
			case "java.lang.Integer":
			case "java.lang.Boolean":
				ids.addAll(jdbc.query(sql, pairRowMapper, attribute.getId(), attribute.getValue().toString()));
				break;
			case "java.util.ArrayList":
			case BeansUtils.largeArrayListClassName:
				for(String value : attribute.valueAsList()) {
					ids.addAll(jdbc.query(sql, pairRowMapper,attribute.getId(),value));
				}
				break;
			case "java.util.LinkedHashMap":
				for(Map.Entry<String,String> entry : attribute.valueAsMap().entrySet()) {
					ids.addAll(jdbc.query(sql, pairRowMapper,attribute.getId(),entry.getKey()+"="+entry.getValue()));

				}
				break;
			default:
				throw new RuntimeException("unknown attribute type "+attribute.getType());
		}
		return ids;
	}

	@Override
	public void convertAttributeValuesToUnique(PerunSession session, AttributeDefinition attrDef) throws InternalErrorException {
		try {
			String tablePrefix = attributeToTablePrefix(attrDef);
			jdbc.update(Compatibility.getLockTable(tablePrefix + "_attr_values"));
			if (tablePrefix.equals("user_ext_source") || !tablePrefix.contains("_")) {
				//attribute of a single perun bean, e.g. user
				@SuppressWarnings("UnnecessaryLocalVariable")
				String bean = tablePrefix;
				final AtomicInteger counter = new AtomicInteger(0);
				jdbc.query("SELECT " + bean + "_id,attr_value,attr_value_text FROM " + tablePrefix + "_attr_values WHERE attr_id=?", rs -> {
					int beanId = rs.getInt(1);
					Object value = null;
					try {
						value = BeansUtils.stringToAttributeValue(readAttributeValue(session, attrDef, rs), attrDef.getType());
						switch (attrDef.getType()) {
							case "java.lang.String":
							case BeansUtils.largeStringClassName:
							case "java.lang.Integer":
							case "java.lang.Boolean":
								jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean + "_id,attr_id,attr_value) VALUES (?,?,?)", beanId, attrDef.getId(), value.toString());
								break;
							case "java.util.ArrayList":
							case BeansUtils.largeArrayListClassName:
								@SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) value;
								for (String s : list) {
									jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean + "_id,attr_id,attr_value) VALUES (?,?,?)", beanId, attrDef.getId(), s);
								}
								break;
							case "java.util.LinkedHashMap":
								@SuppressWarnings("unchecked") LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) value;
								for (Map.Entry<String, String> entry : map.entrySet()) {
									jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean + "_id,attr_id,attr_value) VALUES (?,?,?)", beanId, attrDef.getId(), entry.getKey() + "=" + entry.getValue());
								}
								break;
						}
						int c = counter.addAndGet(1);
						if(c%1000==0) log.debug("{} values of {} were converted", c, attrDef.getName());

					} catch (InternalErrorException e) {
						throw new InternalErrorRuntimeException(e);
					} catch (DuplicateKeyException ex) {
						throw new InternalErrorRuntimeException("value " + value + " of attribute " + attrDef.getName() + " for " + bean + "=" + beanId + " is not unique", ex);
					}
				}, attrDef.getId());
				log.debug("{} values of {} were converted", counter.get(), attrDef.getName());
			} else {
				//attribute of relation between perun beans, e.g. group_resource
				String[] ss = tablePrefix.split("_");
				String bean1 = ss[0];
				String bean2 = ss[1];
				jdbc.query("SELECT " + bean1 + "_id," + bean2 + "_id,attr_value,attr_value_text FROM " + tablePrefix + "_attr_values WHERE attr_id=?", rs -> {
					int bean1Id = rs.getInt(1);
					int bean2Id = rs.getInt(2);
					Object value = null;
					try {
						value = BeansUtils.stringToAttributeValue(readAttributeValue(session, attrDef, rs), attrDef.getType());
						switch (attrDef.getType()) {
							case "java.lang.String":
							case BeansUtils.largeStringClassName:
							case "java.lang.Integer":
							case "java.lang.Boolean":
								jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 + "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(), value.toString());
								break;
							case "java.util.ArrayList":
							case BeansUtils.largeArrayListClassName:
								@SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) value;
								for (String s : list) {
									jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 + "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(), s);
								}
								break;
							case "java.util.LinkedHashMap":
								@SuppressWarnings("unchecked") LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) value;
								for (Map.Entry<String, String> entry : map.entrySet()) {
									jdbc.update("INSERT INTO " + tablePrefix + "_attr_u_values (" + bean1 + "_id," + bean2 + "_id,attr_id,attr_value) VALUES (?,?,?,?)", bean1Id, bean2Id, attrDef.getId(), entry.getKey() + "=" + entry.getValue());
								}
								break;
						}
					} catch (InternalErrorException e) {
						throw new InternalErrorRuntimeException(e);
					} catch (DuplicateKeyException ex) {
						throw new InternalErrorRuntimeException("value " + value + " of attribute " + attrDef.getName() + " for " + bean1 + "=" + bean1Id + "," + bean2 + "=" + bean2Id + " is not unique", ex);
					}
				}, attrDef.getId());
			}
		} catch (InternalErrorRuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	private static String readAttributeValue(PerunSession session, AttributeDefinition attrDef, ResultSet rs) throws InternalErrorRuntimeException, SQLException {
		if (Utils.isLargeAttribute(session, attrDef)) {
			if (Compatibility.isOracle()) {
				//large attributes
				Clob clob = rs.getClob("attr_value_text");
				try(Reader characterStream = clob.getCharacterStream()) {
					return CharStreams.toString(characterStream);
				} catch (IOException e) {
					throw new InternalErrorRuntimeException("cannot read CLOB",e);
				} finally {
					clob.free();
				}
			} else {
				// POSTGRES READ CLOB AS STRING
				return rs.getString("attr_value_text");
			}
		} else {
			return rs.getString("attr_value");
		}
	}

	public void setSelf(AttributesManagerImplApi self) {
		this.self = self;
	}

	@Override
	public AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			Map<String, Object> map = jdbc.queryForMap("SELECT attr_name, friendly_name, namespace, type, dsc, display_name, is_unique FROM attr_names WHERE id=?", attributeDefinition.getId());

			//update description
			if (!attributeDefinition.getDescription().equals(map.get("dsc"))) {
				this.setAttributeDefinitionModified(perunSession, attributeDefinition);
				jdbc.update("update attr_names set dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", attributeDefinition.getDescription(), perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());

				if(!CacheManager.isCacheDisabled()) perun.getCacheManager().updateAttributeDefinition(attributeDefinition);
			}

			//update displayName
			// if stored value was null and new isn't, update
			// if values not null && not equals, update
			if ((map.get("display_name") == null && attributeDefinition.getDisplayName() != null) ||
					(map.get("display_name") != null && !map.get("display_name").equals(attributeDefinition.getDisplayName()))) {
				this.setAttributeDefinitionModified(perunSession, attributeDefinition);
				jdbc.update("update attr_names set display_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", attributeDefinition.getDisplayName(), perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());
				if(!CacheManager.isCacheDisabled()) perun.getCacheManager().updateAttributeDefinition(attributeDefinition);
			}

			//update unique
			boolean uniqueInDb;
			if (Compatibility.isOracle()) {
				uniqueInDb = "1".equals(map.get("is_unique"));
			} else {
				uniqueInDb = (Boolean) map.get("is_unique");
			}
			if (uniqueInDb != attributeDefinition.isUnique()) {
				jdbc.update("UPDATE attr_names SET is_unique=" + Compatibility.getTrue() + ", modified_by=?, modified_by_uid=?, modified_at="
						+ Compatibility.getSysdate() + " WHERE id=?", perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), attributeDefinition.getId());
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

		AttributeRightsExtractor(int attributeId) {
			this.attributeId = attributeId;
		}

		@Override
		public List<AttributeRights> extractData(ResultSet rs) throws SQLException, DataAccessException {

			Map<Role, List<ActionType>> map = new HashMap<>();

			while (rs.next()) {

				Role role = Role.valueOf(rs.getString("role_name").toUpperCase());
				ActionType actionType = ActionType.valueOf(rs.getString("action_type").toUpperCase());

				if (map.get(role) != null) {
					map.get(role).add(actionType);
				} else {
					map.put(role, new ArrayList<>(Collections.singletonList(actionType)));
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

		List<AttributeRights> rights;
		try {
			rights = jdbc.query("SELECT " + attributeRightSelectQuery + " FROM attributes_authz JOIN roles ON "
					+ "attributes_authz.role_id=roles.id JOIN action_types ON attributes_authz.action_type_id=action_types.id WHERE "
					+ "attributes_authz.attr_id=?", new AttributeRightsExtractor(attributeId), attributeId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		// set also empty rights for other roles (not present in DB)

		boolean roleExists;

		List<Role> listOfRoles = new ArrayList<>();
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
				rights.add(new AttributeRights(attributeId, roleToTry, new ArrayList<>()));
			}
		}

		return rights;

	}

	@Override
	public void setAttributeRight(PerunSession sess, AttributeRights rights) throws InternalErrorException {
		try {
			// get action types of the attribute and role from the database
			List<ActionType> dbActionTypes = jdbc.query("SELECT action_types.action_type AS action_type FROM attributes_authz JOIN action_types "
							+ "ON attributes_authz.action_type_id=action_types.id WHERE attr_id=? AND "
							+ "role_id=(SELECT id FROM roles WHERE name=?)",
					(rs, rowNum) -> ActionType.valueOf(rs.getString("action_type").toUpperCase()), rights.getAttributeId(), rights.getRole().getRoleName());

			// inserting
			List<ActionType> actionTypesToInsert = new ArrayList<>();
			actionTypesToInsert.addAll(rights.getRights());
			actionTypesToInsert.removeAll(dbActionTypes);
			for (ActionType actionType : actionTypesToInsert) {
				jdbc.update("INSERT INTO attributes_authz (attr_id, role_id, action_type_id) VALUES "
								+ "(?, (SELECT id FROM roles WHERE name=?), (SELECT id FROM action_types WHERE action_type=?))",
						rights.getAttributeId(), rights.getRole().getRoleName(), actionType.getActionType());
			}
			// deleting
			List<ActionType> actionTypesToDelete = new ArrayList<>();
			actionTypesToDelete.addAll(dbActionTypes);
			actionTypesToDelete.removeAll(rights.getRights());
			for (ActionType actionType : actionTypesToDelete) {
				if (0 == jdbc.update("DELETE FROM attributes_authz WHERE attr_id=? AND role_id=(SELECT id FROM roles WHERE name=?) AND "
								+ "action_type_id=(SELECT id FROM action_types WHERE action_type=?)", rights.getAttributeId(),
						rights.getRole().getRoleName(), actionType.getActionType())) {
					throw new ConsistencyErrorException("Trying to delete non existing row : AttributeRight={ attributeId="
							+ Integer.toString(rights.getAttributeId()) + " role=" + rights.getRole().getRoleName() + " actionType=" + actionType.getActionType());
				}
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	private Attribute setAttributeCreatedAndModified(PerunSession sess, Attribute attribute) throws InternalErrorException {
		attribute.setValueCreatedBy(sess.getPerunPrincipal().getActor());
		attribute.setValueModifiedBy(sess.getPerunPrincipal().getActor());
		Timestamp time = new Timestamp(System.currentTimeMillis());
		attribute.setValueCreatedAt(time.toString());
		attribute.setValueModifiedAt(time.toString());
		return attribute;
	}

	private Attribute setAttributeModified(PerunSession sess, Attribute attribute) throws InternalErrorException {
		attribute.setValueModifiedBy(sess.getPerunPrincipal().getActor());
		Timestamp time = new Timestamp(System.currentTimeMillis());
		attribute.setValueModifiedAt(time.toString());
		return attribute;
	}

	private AttributeDefinition setAttributeDefinitionCreatedAndModified(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		attribute.setCreatedBy(sess.getPerunPrincipal().getActor());
		attribute.setModifiedBy(sess.getPerunPrincipal().getActor());
		attribute.setCreatedByUid(sess.getPerunPrincipal().getUserId());
		attribute.setModifiedByUid(sess.getPerunPrincipal().getUserId());
		Timestamp time = new Timestamp(System.currentTimeMillis());
		attribute.setCreatedAt(time.toString());
		attribute.setModifiedAt(time.toString());
		return attribute;
	}

	private AttributeDefinition setAttributeDefinitionModified(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		attribute.setModifiedBy(sess.getPerunPrincipal().getActor());
		attribute.setModifiedByUid(sess.getPerunPrincipal().getUserId());
		Timestamp time = new Timestamp(System.currentTimeMillis());
		attribute.setModifiedAt(time.toString());
		return attribute;
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}
}
