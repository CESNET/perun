package cz.metacentrum.perun.core.impl;

import java.util.ServiceLoader;

import cz.metacentrum.perun.core.api.ActionType;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;

import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;

import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;

import cz.metacentrum.perun.core.api.exceptions.AttributeExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
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
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.Iterator;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * AttributesManager implementation.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id$
 */
public class AttributesManagerImpl implements AttributesManagerImplApi {

    //Items of list delimiter. It's used while storing list into string.
    //Can't contain regex special symbols
    public static final char LIST_DELIMITER = ',';  
    public static final char KEY_VALUE_DELIMITER = ':';  
    private static final String ATTRIBUTES_MODULES_PACKAGE = "cz.metacentrum.perun.core.impl.modules.attributes";
    private static final int MERGE_TRY_CNT = 10;
    private static final long MERGE_RAND_SLEEP_MAX = 100;  //max sleep time between SQL merge atempt in milisecond 

    private final static Logger log = LoggerFactory.getLogger(AttributesManagerImpl.class);

    private Perun perun;
    // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
    private JdbcTemplate jdbc;
    private LobHandler lobHandler;
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

    //Attributes modules.  name => module
    private Map<String, AttributesModuleImplApi> attributesModulesMap = new ConcurrentHashMap<String, AttributesModuleImplApi>();

    /**
     * Constructor.
     *
     * @param perunPool connection pool instance
     */
    public AttributesManagerImpl(DataSource perunPool) throws InternalErrorException {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
        this.jdbc = new JdbcTemplate(perunPool);
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
       * @param attributeHolder Facility, Resource or Member for which you want the attribute value
       * @param useDefaultAttribute set to true, if sql query returns column named 'attr_value_default' which will be used only if attr_value is null
       */
      public AttributeRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, Object attributeHolder) {
        this(sess, attributesManagerImpl, attributeHolder, null);
      }

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
            throw new InternalErrorRuntimeException("Bad core attribute definition. " + attribute , ex);
          }
          try {
            Object value = method.invoke(attributeHolder);

            // 
            // Try to automatically convert object returned from bean method call to required data type
            //
            if(attribute.getType().equals(String.class.getName()) && !(value instanceof String)) {
              //TODO check exceptions
              value = String.valueOf(value);
            } else if(attribute.getType().equals(Integer.class.getName()) && !(value instanceof Integer)) {
              //TODO try to cast to integer
            } else if(attribute.getType().equals(ArrayList.class.getName()) && !(value instanceof ArrayList)) {
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
        if(attributesManagerImpl.isLargeAttribute(sess, attribute)) {

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
       * @param useDefaultAttribute set to true, if sql query returns column named 'attr_value_default' which will be used only if attr_value is null
       */

      public ValueRowMapper(PerunSession sess, AttributesManagerImpl attributesManagerImpl, AttributeDefinition attributeDefinition) {
        this.sess = sess;
        this.attributesManagerImpl = attributesManagerImpl;
        this.attributeDefinition = attributeDefinition;
      }

      public Object mapRow(ResultSet rs, int i) throws SQLException {
        String stringValue;
        if(attributesManagerImpl.isLargeAttribute(sess, attributeDefinition)) {
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
                                     new AttributeRowMapper(sess, this, null), 
                                     resource.getId(), member.getId(), 
                                     AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT);
      } catch(EmptyResultDataAccessException ex) {
        log.debug("No attribute for member-resource combination exists.");
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
        parameters.addValue("attrNames", attrNames);

        try {
          return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("vot") + " from attr_names " +
                                                            "left join vo_attr_values vot on id=vot.attr_id and vo_id=:vId " +
                                                            "where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name in ( :attrNames )",
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
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " +
                                                        "left join member_attr_values mem on id=mem.attr_id and member_id=:mId " +
                                                        "where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name in ( :attrNames )",
                                                        parameters, new AttributeRowMapper(sess, this, member));
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
                                    new AttributeRowMapper(sess, this, null), facility.getId(), user.getId(),  
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
                                    new AttributeRowMapper(sess, this, null), facility.getId(),  
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
    parameters.addValue("attrNames", attrNames);

    try {
      return namedParameterJdbcTemplate.query("select " + getAttributeMappingSelectQuery("usr") + " from attr_names " +
                                                        "left join user_attr_values usr on id=usr.attr_id and user_id=:uId " +
                                                        "where namespace in ( :nSC,:nSO,:nSD ) and attr_names.attr_name in ( :attrNames )",
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

    public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException {
        try {
        return jdbc.query("select " + getAttributeMappingSelectQuery("grp_res") + " from attr_names " +
                                    "left join    group_resource_attr_values     grp_res      on id=grp_res.attr_id     and   resource_id=? and group_id=? " +
                                    "where namespace in (?,?) and (grp_res.attr_value is not null or grp_res.attr_value_text is not null)",
                                    new AttributeRowMapper(sess, this, null), resource.getId(), group.getId(),
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
                                    new RichAttributeRowMapper<User, Facility>(new AttributeRowMapper(sess, this, null), UsersManagerImpl.USER_MAPPER, FacilitiesManagerImpl.FACILITY_MAPPER),
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
                                      new AttributeRowMapper(sess, this, null), resource.getId(), member.getId(), attributeName);


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
                                    new AttributeRowMapper(sess, this, null), resource.getId(), group.getId(), id);
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
                                    new AttributeRowMapper(sess, this, null), group.getId(), id);
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

    public boolean setAttribute(final PerunSession sess, final Facility facility, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from facility_attr_values where attr_id=? and facility_id=?", attribute.getId(), facility.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from facility_attr_values where attr_id="+ attribute.getId() +" and facility_id=" + facility.getId());
                return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {                    
                      return 0 < jdbc.execute("merge into facility_attr_values using dual on (attr_id=? and facility_id=?) " +
                                              "when not matched   then insert (attr_id, facility_id, attr_value_text, created_by, modified_by, created_at, modified_at, modified_by_uid, created_by_uid) " +
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, facility.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, facility.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from facility_attr_values where attr_id=? and facility_id=? for update", attribute.getId(), facility.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into facility_attr_values (attr_id, facility_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), facility.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update facility_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and facility_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), facility.getId());
                    return true;
                    //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
              if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from facility_attr_values where attr_id=? and facility_id=?", attribute.getId(), facility.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from facility_attr_values where attr_id="+ attribute.getId() +" and facility_id=" + facility.getId());
                return numAffected == 1;
              }

              try {
                  Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from facility_attr_values where attr_id=? and facility_id=?", String.class, attribute.getId(), facility.getId()), attribute.getType());
                  if(attribute.getValue().equals(value)) return false;
              } catch(EmptyResultDataAccessException ex) {
                //This is ok. Attribute will be stored later.
              }

              int repetatCounter = 0;
              while(true) {
                try {
                  if(Compatibility.isMergeSupported()) {
                      jdbc.update("merge into facility_attr_values using dual on (attr_id=? and facility_id=?) " +
                                      "when not matched then insert (attr_id, facility_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                      "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?) when matched then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                      attribute.getId(), facility.getId(), attribute.getId(), facility.getId(),
                                      BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                      sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                      BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId() 
                      );
                      return true;
                  } else {
                      try {
                        jdbc.queryForInt("select attr_id from facility_attr_values where attr_id=? and facility_id=? for update", attribute.getId(), facility.getId());
                      } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into facility_attr_values (attr_id, facility_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                            "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), facility.getId(),
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                        return true;
                      }
                      //Exception wasn't thrown -> update
                      jdbc.update("update facility_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and facility_id=?",
                              BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), facility.getId());
                      return true;
                  }
                } catch(DataIntegrityViolationException ex) {
                  if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }
            }
        } catch (RuntimeException e) {
                throw new InternalErrorException(e);
        }        
    }

    public boolean setAttribute(final PerunSession sess, final Vo vo, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from vo_attr_values where attr_id=? and vo_id=?", attribute.getId(), vo.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from vo_attr_values where attr_id="+ attribute.getId() +" and vo_id=" + vo.getId());
                return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into vo_attr_values using dual on (attr_id=? and vo_id=?) " +
                                              "when not matched   then insert (attr_id, vo_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, vo.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, vo.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());                                                 
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from vo_attr_values where attr_id=? and vo_id=? for update", attribute.getId(), vo.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into vo_attr_values (attr_id, vo_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), vo.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update vo_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and vo_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), vo.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
              if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from vo_attr_values where attr_id=? and vo_id=?", attribute.getId(), vo.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from vo_attr_values where attr_id="+ attribute.getId() +" and vo_id=" + vo.getId());
                return numAffected == 1;
              }
              try {
                  Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from vo_attr_values where attr_id=? and vo_id=?", String.class, attribute.getId(), vo.getId()), attribute.getType());
                  if(attribute.getValue().equals(value)) return false;
              } catch(EmptyResultDataAccessException ex) {
                //This is ok. Attribute will be stored later.
              }
              
              int repetatCounter = 0;
              while(true) {
                try {
                    if(Compatibility.isMergeSupported()) {
                        jdbc.update("merge into vo_attr_values using dual on (attr_id=? and vo_id=?) " +
                                        "when not matched then insert (attr_id, vo_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                        "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?) when matched then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                        attribute.getId(), vo.getId(), attribute.getId(), vo.getId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                        sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                        );
                        return true;
                    } else {
                        try {
                          jdbc.queryForInt("select attr_id from vo_attr_values where attr_id=? and vo_id=? for update", attribute.getId(), vo.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into vo_attr_values (attr_id, vo_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), vo.getId(),
                              BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                                  
                        return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update vo_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and vo_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), vo.getId());
                        return true;
                    }
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }
            }
        } catch (RuntimeException e) {
                throw new InternalErrorException(e);
        }  
    }

    public boolean setAttribute(final PerunSession sess, final Host host, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from host_attr_values where attr_id=? and host_id=?", attribute.getId(), host.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from host_attr_values where attr_id="+ attribute.getId() +" and host_id=" + host.getId());
                return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into host_attr_values using dual on (attr_id=? and host_id=?) " +
                                              "when not matched   then insert (attr_id, host_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, host.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, host.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from host_attr_values where attr_id=? and host_id=? for update", attribute.getId(), host.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into host_attr_values (attr_id, host_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), host.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update host_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and host_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), host.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
              if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from host_attr_values where attr_id=? and host_id=?", attribute.getId(), host.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from host_attr_values where attr_id="+ attribute.getId() +" and host_id=" + host.getId());
                return numAffected == 1;
              }
              try {
                  Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from host_attr_values where attr_id=? and host_id=?", String.class, attribute.getId(), host.getId()), attribute.getType());
                  if(attribute.getValue().equals(value)) return false;
              } catch(EmptyResultDataAccessException ex) {
                //This is ok. Attribute will be stored later.
              }
              
              int repetatCounter = 0;
              while(true) {
                try {
                    if(Compatibility.isMergeSupported()) {
                        jdbc.update("merge into host_attr_values using dual on (attr_id=? and host_id=?) " +
                                        "when not matched then insert (attr_id, host_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                        "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?) when matched then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                        attribute.getId(), host.getId(), attribute.getId(), host.getId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                        sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                        );
                        return true;
                    } else {
                        try {
                          jdbc.queryForInt("select attr_id from host_attr_values where attr_id=? and host_id=? for update", attribute.getId(), host.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into host_attr_values (attr_id, host_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                               "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), host.getId(),
                               BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                        return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update host_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and host_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), host.getId());
                        return true;
                    }     
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }
            }
        } catch (RuntimeException e) {
                throw new InternalErrorException(e);
        }
    }

    public boolean setAttribute(final PerunSession sess, final Group group, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from group_attr_values where attr_id=? and group_id=?", attribute.getId(), group.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from group_attr_values where attr_id="+ attribute.getId() +" and group_id=" + group.getId());
                return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into group_attr_values using dual on (attr_id=? and group_id=?) " +
                                              "when not matched   then insert (attr_id, group_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +  
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, group.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, group.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from group_attr_values where attr_id=? and group_id=? for update", attribute.getId(), group.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into group_attr_values (attr_id, group_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), group.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update group_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and group_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), group.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
              if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from group_attr_values where attr_id=? and group_id=?", attribute.getId(), group.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from group_attr_values where attr_id="+ attribute.getId() +" and group_id=" + group.getId());
                return numAffected == 1;
              }
              try {
                  Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from group_attr_values where attr_id=? and group_id=?", String.class, attribute.getId(), group.getId()), attribute.getType());
                  if(attribute.getValue().equals(value)) return false;
              } catch(EmptyResultDataAccessException ex) {
                //This is ok. Attribute will be stored later.
              }
              
              int repetatCounter = 0;
              while(true) {
                try {
                    if(Compatibility.isMergeSupported()) {
                        jdbc.update("merge into group_attr_values using dual on (attr_id=? and group_id=?) " +
                                        "when not matched then insert (attr_id, group_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                        "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?) when matched then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                        attribute.getId(), group.getId(), attribute.getId(), group.getId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                        sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                        BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                        );
                        return true;
                    } else {
                        try {
                          jdbc.queryForInt("select attr_id from group_attr_values where attr_id=? and group_id=? for update", attribute.getId(), group.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into group_attr_values (attr_id, group_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), group.getId(),
                              BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                          return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update group_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and group_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), group.getId());
                        return true;
                    }
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }
            }
        } catch (RuntimeException e) {
                throw new InternalErrorException(e);
        }  
    }

    public boolean setAttribute(final PerunSession sess, final Resource resource, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from resource_attr_values where attr_id=? and resource_id=?", attribute.getId(), resource.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from resource_attr_values where attr_id="+ attribute.getId() +" and resource_id=" + resource.getId());
                return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into resource_attr_values using dual on (attr_id=? and resource_id=?) " +
                                              "when not matched   then insert (attr_id, resource_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, resource.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, resource.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from resource_attr_values where attr_id=? and resource_id=? for update", attribute.getId(), resource.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into resource_attr_values (attr_id, resource_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), resource.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update resource_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
              if(attribute.getValue() == null) {
                int numAffected = jdbc.update("delete from resource_attr_values where attr_id=? and resource_id=?", attribute.getId(), resource.getId());
                if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from resource_attr_values where attr_id="+ attribute.getId() +" and resource_id=" + resource.getId());
                return numAffected == 1;
              }
              try {
                  Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from resource_attr_values where attr_id=? and resource_id=?", String.class, attribute.getId(), resource.getId()), attribute.getType());
                  if(attribute.getValue().equals(value)) return false;
              } catch(EmptyResultDataAccessException ex) {
                //This is ok. Attribute will be stored later.
              }
              
              int repetatCounter = 0;
              while(true) {
                try {
                    if(Compatibility.isMergeSupported()) {
                        jdbc.update("merge into resource_attr_values using dual on (attr_id=? and resource_id=?) " +
                            "when not matched   then insert (attr_id, resource_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                            "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                            "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                            attribute.getId(), resource.getId(), attribute.getId(), resource.getId(),
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                            sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                            );
                        return true;
                    } else {
                        try {
                          jdbc.queryForInt("select attr_id from resource_attr_values where attr_id=? and resource_id=? for update", attribute.getId(), resource.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into resource_attr_values (attr_id, resource_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +  
                               "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), resource.getId(),
                               BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                          return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update resource_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId());
                        return true;
                    }
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }        
        }
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }

    public boolean setAttribute(final PerunSession sess, final Resource resource, final Member member, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from member_resource_attr_values where attr_id=? and member_id=? and resource_id=?", attribute.getId(), member.getId(), resource.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from member_resource_attr_values where attr_id="+ attribute.getId() +" and member_id=" + member.getId() + " and resource_id=" + resource.getId());
              return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into member_resource_attr_values using dual on (attr_id=? and resource_id=? and member_id=?) " +
                                              "when not matched   then insert (attr_id, resource_id, member_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, resource.getId());
                                                  ps.setInt(3, member.getId());
                                                  ps.setInt(4, attribute.getId());
                                                  ps.setInt(5, resource.getId());
                                                  ps.setInt(6, member.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 7, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(8, sess.getPerunPrincipal().getActor());
                                                    ps.setString(9, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(10, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(11, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 11, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(12, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(13, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from member_resource_attr_values where attr_id=? and resource_id=? and member_id=? for update", attribute.getId(), resource.getId(), member.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        return 0 < jdbc.update("insert into member_resource_attr_values (attr_id, resource_id, member_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)",attribute.getId(), resource.getId(),
                                member.getId(), BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                    }
                    //Exception wasn't thrown -> update
                    return 0 < jdbc.update("update member_resource_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=? and member_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId(), member.getId());
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
            if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from member_resource_attr_values where attr_id=? and member_id=? and resource_id=?", attribute.getId(), member.getId(), resource.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from member_resource_attr_values where attr_id="+ attribute.getId() +" and member_id=" + member.getId() + " and resource_id=" + resource.getId());
              return numAffected == 1;
            }
            try {
                Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from member_resource_attr_values where attr_id=? and member_id=? and resource_id=?", String.class, attribute.getId(), member.getId(), resource.getId()), attribute.getType());
                if(attribute.getValue().equals(value)) return false;
            } catch(EmptyResultDataAccessException ex) {
              //This is ok. Attribute will be stored later.
            }
            int repetatCounter = 0;
            while(true) {
                try { 
                    if(Compatibility.isMergeSupported()) {
                        return 0 < jdbc.update("merge into member_resource_attr_values using dual on (attr_id=? and resource_id=? and member_id=?) " +
                                               "when not matched   then insert (attr_id, resource_id, member_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                               "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                               "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                               attribute.getId(), resource.getId(), member.getId(), attribute.getId(), resource.getId(), member.getId(),
                                               BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                               sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                               BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                        );
                    } else {
                        try {
                          jdbc.queryForInt("select attr_id from member_resource_attr_values where attr_id=? and resource_id=? and member_id=? for update", attribute.getId(), resource.getId(), member.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          return 0 < jdbc.update("insert into member_resource_attr_values (attr_id, resource_id, member_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                         "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)",attribute.getId(), resource.getId(),
                                         member.getId(), BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                        }
                        //Exception wasn't thrown -> update
                        return 0 < jdbc.update("update member_resource_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=? and member_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId(), member.getId());
                    }
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }
            }
      } catch(RuntimeException ex) { 
        throw new InternalErrorException(ex);
      }        
    }

    public boolean setAttribute(final PerunSession sess, final Facility facility, final User user, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from user_facility_attr_values where attr_id=? and facility_id=? and user_id=?", attribute.getId(), facility.getId(), user.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from user_facility_attr_values where attr_id=" + attribute.getId() + " and facility_id=" + facility.getId() + " and user_id=" + user.getId());
              return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into user_facility_attr_values using dual on (attr_id=? and facility_id=? and user_id=?) " +
                                              "when not matched   then insert (attr_id, facility_id, user_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, facility.getId());
                                                  ps.setInt(3, user.getId());
                                                  ps.setInt(4, attribute.getId());
                                                  ps.setInt(5, facility.getId());
                                                  ps.setInt(6, user.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 7, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(8, sess.getPerunPrincipal().getActor());
                                                    ps.setString(9, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(10, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(11, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 11, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(12, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(13, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        // FIXME ?? This is vulnerable to race conditions ??
                        jdbc.queryForInt("select attr_id from user_facility_attr_values where attr_id=? and facility_id=? and user_id=? for update", attribute.getId(), facility.getId(), user.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this method)
                        jdbc.update("insert into user_facility_attr_values (attr_id, facility_id, user_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), facility.getId(), user.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update user_facility_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and facility_id=? and user_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), facility.getId(), user.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
            if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from user_facility_attr_values where attr_id=? and facility_id=? and user_id=?", attribute.getId(), facility.getId(), user.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from user_facility_attr_values where attr_id=" + attribute.getId() + " and facility_id=" + facility.getId() + " and user_id=" + user.getId());
              return numAffected == 1;
            }
            try {
                Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from user_facility_attr_values where attr_id=? and facility_id=? and user_id=?", String.class, attribute.getId(), facility.getId(), user.getId()), attribute.getType());
                if(attribute.getValue().equals(value)) return false;
            } catch(EmptyResultDataAccessException ex) {
              //This is ok. Attribute will be stored later.
            }
            
            int repetatCounter = 0;
            while(true) {
                try {           
                    if(Compatibility.isMergeSupported()) {
                          jdbc.update("merge into user_facility_attr_values using dual on (attr_id=? and facility_id=? and user_id=?) " +
                                                 "when not matched   then insert (attr_id, facility_id, user_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                                 "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                                 "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                                 attribute.getId(), facility.getId(), user.getId(), attribute.getId(), facility.getId(), user.getId(),
                                                 BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                                 sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                                 BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                          );
                          return true;
                    } else {
                        try {
                          //FIXME This is vunerable to race conditions
                          jdbc.queryForInt("select attr_id from user_facility_attr_values where attr_id=? and facility_id=? and user_id=? for update", attribute.getId(), facility.getId(), user.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into user_facility_attr_values (attr_id, facility_id, user_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), facility.getId(), user.getId(),
                              BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                          return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update user_facility_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and facility_id=? and user_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), facility.getId(), user.getId());
                        return true;
                    }
                } catch(DataIntegrityViolationException ex) {
                    if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                  try {
                    Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                  } catch(InterruptedException IGNORE) { }
                }
              }    
            }
      } catch(RuntimeException ex) { 
        throw new InternalErrorException(ex);
      }   
    }

    public boolean setAttribute(final PerunSession sess, final User user, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from user_attr_values where attr_id=? and user_id=?", attribute.getId(), user.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from user_attr_values where attr_id="+ attribute.getId() +" and user_id=" + user.getId());
              return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                  //TODO return false when attr_value_text is not changed
                      return 0 < jdbc.execute("merge into user_attr_values using dual on (attr_id=? and user_id=?) " +
                                              "when not matched   then insert (attr_id, user_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(), 
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, user.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, user.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        jdbc.queryForInt("select attr_id from user_attr_values where attr_id=? and user_id=? for update", attribute.getId(), user.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into user_attr_values (attr_id, user_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), user.getId(), BeansUtils.attributeValueToString(attribute),
                                sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update user_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and user_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), user.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
          if(attribute.getValue() == null) {
            int numAffected = jdbc.update("delete from user_attr_values where attr_id=? and user_id=?", attribute.getId(), user.getId());
            if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from user_attr_values where attr_id="+ attribute.getId() +" and user_id=" + user.getId());
            return numAffected == 1;
          }
          try {
              Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from user_attr_values where attr_id=? and user_id=?", String.class, attribute.getId(), user.getId()), attribute.getType());
              if(attribute.getValue().equals(value)) return false;
          } catch(EmptyResultDataAccessException ex) {
            //This is ok. Attribute will be stored later.
          }
          
          int repetatCounter = 0;
          while(true) {
            try { 
                if(Compatibility.isMergeSupported()) {
                      jdbc.update("merge into user_attr_values using dual on (attr_id=? and user_id=?) " +
                                             "when not matched   then insert (attr_id, user_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                             "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?) " +
                                             "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(), 
                                             attribute.getId(), user.getId(), attribute.getId(), user.getId(),
                                             BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                             sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                             BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                      );
                      return true;
                } else {
                    try {
                      jdbc.queryForInt("select attr_id from user_attr_values where attr_id=? and user_id=? for update", attribute.getId(), user.getId());
                    } catch(EmptyResultDataAccessException ex) {
                      //Value doesn't exist -> insert   (and return from this metod)
                      jdbc.update("insert into user_attr_values (attr_id, user_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                          "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), user.getId(), BeansUtils.attributeValueToString(attribute),
                          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                      return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update user_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and user_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), user.getId());
                    return true;
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }    
        }
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }

    public boolean setAttribute(final PerunSession sess, final Member member, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from member_attr_values where attr_id=? and member_id=?", attribute.getId(), member.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from member_attr_values where attr_id="+ attribute.getId() +" and member_id=" + member.getId());
              return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                  //TODO return false when attr_value_text is not changed
                      return 0 < jdbc.execute("merge into member_attr_values using dual on (attr_id=? and member_id=?) " +
                                              "when not matched   then insert (attr_id, member_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(), 
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, member.getId());
                                                  ps.setInt(3, attribute.getId());
                                                  ps.setInt(4, member.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(6, sess.getPerunPrincipal().getActor());
                                                    ps.setString(7, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(11, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        //FIXME ?? This is vulnerable to race conditions
                        jdbc.queryForInt("select attr_id from member_attr_values where attr_id=? and member_id=? for update", attribute.getId(), member.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into member_attr_values (attr_id, member_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), member.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update member_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and member_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), member.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
          if(attribute.getValue() == null) {
            int numAffected = jdbc.update("delete from member_attr_values where attr_id=? and member_id=?", attribute.getId(), member.getId());
            if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from member_attr_values where attr_id="+ attribute.getId() +" and member_id=" + member.getId());
            return numAffected == 1;
          }
        try {
            Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from member_attr_values where attr_id=? and member_id=?", String.class, attribute.getId(), member.getId()), attribute.getType());
            if(attribute.getValue().equals(value)) return false;
        } catch(EmptyResultDataAccessException ex) {
          //This is ok. Attribute will be stored later.
        }
        
        int repetatCounter = 0;
        while(true) {
            try { 
                if(Compatibility.isMergeSupported()) {
                      jdbc.update("merge into member_attr_values using dual on (attr_id=? and member_id=?) " +
                                             "when not matched   then insert (attr_id, member_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                             "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                             "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                             attribute.getId(), member.getId(), attribute.getId(), member.getId(),
                                             BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                             sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                             BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                      );
                      return true;
                } else {
                    try {
                      //FIXME This is vunerable to race conditions
                      jdbc.queryForInt("select attr_id from member_attr_values where attr_id=? and member_id=? for update", attribute.getId(), member.getId());
                    } catch(EmptyResultDataAccessException ex) {
                      //Value doesn't exist -> insert   (and return from this metod)
                      jdbc.update("insert into member_attr_values (attr_id, member_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                          "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), member.getId(),
                          BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()); 
                      return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update member_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and member_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), member.getId());
                    return true;
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }     
        }
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }

    public boolean setAttribute(final PerunSession sess, final Resource resource, final Group group, final Attribute attribute) throws InternalErrorException {
      try {
        if(isLargeAttribute(sess, attribute)) {
          if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from group_resource_attr_values where attr_id=? and resource_id=? and group_id=?", attribute.getId(), resource.getId(), group.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from group_resource_attr_values where attr_id=" + attribute.getId() + " and resource_id=" + resource.getId() + " and group_id=" + group.getId());
              return numAffected == 1;
          }
          int repetatCounter = 0;
          while(true) {
            try {  
                if(Compatibility.isMergeSupported()) {
                      return 0 < jdbc.execute("merge into group_resource_attr_values using dual on (attr_id=? and resource_id=? and group_id=?) " +
                                              "when not matched   then insert (attr_id, resource_id, group_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                              "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                              new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                                                public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                                  ps.setInt(1, attribute.getId());
                                                  ps.setInt(2, resource.getId());
                                                  ps.setInt(3, group.getId());
                                                  ps.setInt(4, attribute.getId());
                                                  ps.setInt(5, resource.getId());
                                                  ps.setInt(6, group.getId());
                                                  try {
                                                    lobCreator.setClobAsString(ps, 7, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(8, sess.getPerunPrincipal().getActor());
                                                    ps.setString(9, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(10, sess.getPerunPrincipal().getUserId());
                                                    ps.setInt(11, sess.getPerunPrincipal().getUserId());
                                                    lobCreator.setClobAsString(ps, 11, BeansUtils.attributeValueToString(attribute));
                                                    ps.setString(12, sess.getPerunPrincipal().getActor());
                                                    ps.setInt(13, sess.getPerunPrincipal().getUserId());
                                                  } catch(InternalErrorException ex) {
                                                    throw new InternalErrorRuntimeException(ex);
                                                  }
                                                }
                                              }
                      );
                } else {
                    try {
                        //FIXME This is vulnerable to race conditions
                        jdbc.queryForInt("select attr_id from  group_resource_attr_values where attr_id=? and resource_id=? and group_id=? for update", attribute.getId(), resource.getId(), group.getId());
                    } catch(EmptyResultDataAccessException ex) {
                        //Value doesn't exist -> insert   (and return from this metod)
                        jdbc.update("insert into group_resource_attr_values (attr_id, resource_id, group_id, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " +
                                "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), resource.getId(), group.getId(),
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                        return true;
                    }
                    //Exception wasn't thrown -> update
                    jdbc.update("update group_resource_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=? and group_id=?",
                            BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId(), group.getId());
                    return true;
                  //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                }
            } catch(DataIntegrityViolationException ex) {
                if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
              try {
                Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
              } catch(InterruptedException IGNORE) { }
            }
          }        
        } else {
            if(attribute.getValue() == null) {
              int numAffected = jdbc.update("delete from group_resource_attr_values where attr_id=? and resource_id=? and group_id=?", attribute.getId(), resource.getId(), group.getId());
              if(numAffected > 1) throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from group_resource_attr_values where attr_id=" + attribute.getId() + " and resource_id=" + resource.getId() + " and group_id=" + group.getId());
              return numAffected == 1;
            }
            try {
                Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from group_resource_attr_values where attr_id=? and resource_id=? and group_id=?", String.class, attribute.getId(), resource.getId(), group.getId()), attribute.getType());
                if(attribute.getValue().equals(value)) return false;
            } catch(EmptyResultDataAccessException ex) {
              //This is ok. Attribute will be stored later.
            }

            int repetatCounter = 0;
            while(true) {
                try { 
                    if(Compatibility.isMergeSupported()) {
                          jdbc.update("merge into group_resource_attr_values using dual on (attr_id=? and resource_id=? and group_id=?) " +
                                                 "when not matched   then insert (attr_id, resource_id, group_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                                                 "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)" +
                                                 "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                                 attribute.getId(), resource.getId(), group.getId(), attribute.getId(), resource.getId(), group.getId(),
                                                 BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                                 sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                                 BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                          );
                          return true;
                    } else {
                        try {
                          //FIXME This is vunerable to race conditions
                          jdbc.queryForInt("select attr_id from  group_resource_attr_values where attr_id=? and resource_id=? and group_id=? for update", attribute.getId(), resource.getId(), group.getId());
                        } catch(EmptyResultDataAccessException ex) {
                          //Value doesn't exist -> insert   (and return from this metod)
                          jdbc.update("insert into group_resource_attr_values (attr_id, resource_id, group_id, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " + 
                              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), resource.getId(), group.getId(),
                              BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                          return true;
                        }
                        //Exception wasn't thrown -> update
                        jdbc.update("update group_resource_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and resource_id=? and group_id=?",
                                BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), resource.getId(), group.getId());
                        return true;
                    }
                } catch(DataIntegrityViolationException ex) {
                  if(++repetatCounter > MERGE_TRY_CNT) throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                try {
                  Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                } catch(InterruptedException IGNORE) { }
              }
            }
          }         
      } catch(RuntimeException ex) {
        throw new InternalErrorException(ex);
      }         
    }

    public boolean setAttribute(final PerunSession sess, final String key, final Attribute attribute) throws InternalErrorException {
        try {
            if (isLargeAttribute(sess, attribute)) {
                if (attribute.getValue() == null) {
                    int numAffected = jdbc.update("delete from entityless_attr_values where attr_id=? and subject=?", attribute.getId(), key);
                    if (numAffected > 1) {
                        throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from entityless_attr_values where attr_id=" + attribute.getId() + " and subject=" + key);
                    }
                    return numAffected == 1;
                }
                int repetatCounter = 0;
                while (true) {
                    try {
                        if (Compatibility.isMergeSupported()) {
                            //TODO return false when attr_value_text is not changed
                            return 0 < jdbc.execute("merge into entityless_attr_values using dual on (attr_id=? and subject=?) "
                                    + "when not matched   then insert (attr_id, subject, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) "
                                    + "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)"
                                    + "when matched       then update set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                    new AbstractLobCreatingPreparedStatementCallback(lobHandler) {

                                        public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                            ps.setInt(1, attribute.getId());
                                            try {
                                                ps.setString(2, BeansUtils.attributeValueToString(attribute));
                                            } catch (InternalErrorException ex) {
                                                throw new InternalErrorRuntimeException(ex);
                                            }
                                            ps.setInt(3, attribute.getId());
                                            try {
                                                ps.setString(4, BeansUtils.attributeValueToString(attribute));
                                            } catch (InternalErrorException ex) {
                                                throw new InternalErrorRuntimeException(ex);
                                            }
                                            try {
                                                lobCreator.setClobAsString(ps, 5, BeansUtils.attributeValueToString(attribute));
                                                ps.setString(6, sess.getPerunPrincipal().getActor());
                                                ps.setString(7, sess.getPerunPrincipal().getActor());
                                                ps.setInt(8, sess.getPerunPrincipal().getUserId());
                                                ps.setInt(9, sess.getPerunPrincipal().getUserId());
                                                lobCreator.setClobAsString(ps, 10, BeansUtils.attributeValueToString(attribute));
                                                ps.setString(11, sess.getPerunPrincipal().getActor());
                                                ps.setInt(12, sess.getPerunPrincipal().getUserId());
                                            } catch (InternalErrorException ex) {
                                                throw new InternalErrorRuntimeException(ex);
                                            }
                                        }
                                    });
                        } else {
                            try {
                                //FIXME ?? This is vulnerable to race conditions
                                jdbc.queryForInt("select attr_id from entityless_attr_values where attr_id=? and subject=? for update", attribute.getId(), key);
                            } catch (EmptyResultDataAccessException ex) {
                                //Value doesn't exist -> insert   (and return from this metod)
                                jdbc.update("insert into entityless_attr_values (attr_id, subject, attr_value_text, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) "
                                        + "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), key, BeansUtils.attributeValueToString(attribute),
                                        sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                                return true;
                            }
                            //Exception wasn't thrown -> update
                            jdbc.update("update entityless_attr_values set attr_value_text=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and subject=?",
                                    BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), key);
                            return true;
                            //throw new InternalErrorException("Set large attribute isn't supported yet for databases without merge statement supported.");
                        }
                    } catch (DataIntegrityViolationException ex) {
                        if (++repetatCounter > MERGE_TRY_CNT) {
                            throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                        }
                        try {
                            Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                        } catch (InterruptedException IGNORE) {
                        }
                    }
                }
            } else {
                if (attribute.getValue() == null) {
                    int numAffected = jdbc.update("delete from entityless_attr_values where attr_id=? and subject=?", attribute.getId(), key);
                    if (numAffected > 1) {
                        throw new ConsistencyErrorException("Too much rows to delete (" + numAffected + " rows). SQL: delete from entityless_attr_values where attr_id=" + attribute.getId() + " and subject=" + key);
                    }
                    return numAffected == 1;
                }
                try {
                    Object value = BeansUtils.stringToAttributeValue(jdbc.queryForObject("select attr_value from entityless_attr_values where attr_id=? and subject=?", String.class, attribute.getId(), key), attribute.getType());
                    if (attribute.getValue().equals(value)) {
                        return false;
                    }
                } catch (EmptyResultDataAccessException ex) {
                    //This is ok. Attribute will be stored later.
                }

                int repetatCounter = 0;
                while (true) {
                    try {
                        if (Compatibility.isMergeSupported()) {
                            jdbc.update("merge into entityless_attr_values using dual on (attr_id=? and subject=?) "
                                    + "when not matched   then insert (attr_id, subject, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " 
                                    + "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)"
                                    + "when matched       then update set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate(),
                                    attribute.getId(), key, attribute.getId(), key,
                                    BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
                                    sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(),
                                    BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId()
                                    );
                            return true;
                        } else {
                            try {
                                //FIXME This is vunerable to race conditions
                                jdbc.queryForInt("select attr_id from entityless_attr_values where attr_id=? and subject=? for update", attribute.getId(), key);
                            } catch (EmptyResultDataAccessException ex) {
                                //Value doesn't exist -> insert   (and return from this metod)
                                jdbc.update("insert into entityless_attr_values (attr_id, subject, attr_value, created_by, modified_by, created_at, modified_at, created_by_uid, modified_by_uid) " 
                                     + "values (?,?,?,?,?," + Compatibility.getSysdate() + "," + Compatibility.getSysdate() + ",?,?)", attribute.getId(), key, BeansUtils.attributeValueToString(attribute),
                                     sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
                                return true;
                            }
                            //Exception wasn't thrown -> update
                            jdbc.update("update entityless_attr_values set attr_value=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where attr_id=? and subject=?",
                                    BeansUtils.attributeValueToString(attribute), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), attribute.getId(), key);
                            return true;
                        }
                    } catch (DataIntegrityViolationException ex) {
                        if (++repetatCounter > MERGE_TRY_CNT) {
                            throw new InternalErrorException("SQL merger (or other UPSERT command) failed more than " + MERGE_TRY_CNT + " times.", ex);
                        }
                        try {
                            Thread.sleep(Math.round(MERGE_RAND_SLEEP_MAX * Math.random())); //randomized sleep
                        } catch (InterruptedException IGNORE) {
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
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
    
    public boolean setVirtualAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException {
      return getUserVirtualAttributeModule(sess, attribute).setAttributeValue((PerunSessionImpl) sess, user, attribute);
    }

    /**
     * {@inheritDoc}
     * @param defaultAttribute attribute which value si used as default value for created attribute. If defaultAttribute is null, then no defaultAttribute will be used.
     * @see cz.metacentrum.perun.core.api.AttributesManager#createAttribute(PerunSession,Attribute)
     */
    public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeExistsException {
      
      
      
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
        throw new AttributeExistsException("Attribute " + attribute.getName() + " already exists", e);
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
            new AttributeRowMapper(sess, this, null), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT, resourceToGetServicesFrom.getId());
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

    public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException {
      try {
        return jdbc.query("select " + getAttributeMappingSelectQuery("mem") + " from attr_names " + 

            "join service_required_attrs on id=service_required_attrs.attr_id and service_required_attrs.service_id=? " +

            "left join   member_resource_attr_values mem    on id=mem.attr_id and mem.resource_id=? and member_id=? " + 
            "where namespace in (?,?,?)",
            new AttributeRowMapper(sess, this, member), service.getId(), resource.getId(), member.getId(), AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, AttributesManager.NS_MEMBER_RESOURCE_ATTR_OPT, AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
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

       log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
       return attribute;
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
       //TODO
       log.debug("fillAttribute - There's no rule for this attribute. Attribute wasn't filled. Attribute={}", attribute);
       return attribute;
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
       throw new InternalErrorException("This method is not supported yet.");
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
   
    public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
      //Call attribute module
      FacilityAttributesModuleImplApi facilityModule = getFacilityAttributeModule(sess, attribute);
      if(facilityModule == null) return; //facility module doesn't exists
      try { 
        facilityModule.checkAttributeValue((PerunSessionImpl) sess,facility, attribute);
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

    public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException {
        FacilityUserAttributesModuleImplApi attributeModule = getFacilityUserAttributeModule(sess, attribute);
        if(attributeModule == null) return;
        try {  
          attributeModule.checkAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
        } catch(WrongAttributeAssignmentException ex) { 
          throw new InternalErrorException(ex);
        }
    }

    public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
        UserAttributesModuleImplApi attributeModule = getUserAttributeModule(sess, attribute);
        if(attributeModule == null) return;
        try {
            attributeModule.checkAttributeValue((PerunSessionImpl) sess, user, attribute);
        } catch (WrongAttributeAssignmentException ex) {
           throw new InternalErrorException(ex);
        } catch (WrongReferenceAttributeValueException ex) {
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
        } catch (WrongReferenceAttributeValueException ex) {
          throw new InternalErrorException(ex);
        }
    }

    public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
        //TODO
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
      //TODO
    }

    public void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException {
        try {
        if(0 < jdbc.update("delete from entityless_attr_values where attr_id=? and subject=?", attribute.getId(), key)) {
          log.info("Attribute (its value) with key was removed from entityless attributes. Attribute={}, key={}.", attribute, key);
        }
        } catch(RuntimeException ex) {
        throw new InternalErrorException(ex);
      }
    }
    
    public void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from facility_attr_values where attr_id=? and facility_id=?", attribute.getId(), facility.getId())) {
          log.info("Attribute (its value) was removed from facility. Attribute={}, facility={}.", attribute, facility); 
        }
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

    public void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 > jdbc.update("delete from vo_attr_values where attr_id=? and vo_id=?", attribute.getId(), vo.getId())) {
          log.info("Attribute (its value) was removed from vo. Attribute={}, vo={}.", attribute, vo); 
        }
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

    public void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 > jdbc.update("delete from group_attr_values where attr_id=? and group_id=?", attribute.getId(), group.getId())) {
          log.info("Attribute (its value) was removed from group. Attribute={}, group={}.", attribute, group); 
        }
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

    public void removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from resource_attr_values where attr_id=? and resource_id=?", attribute.getId(), resource.getId())) {
          log.info("Attribute (its value) was removed from resource. Attribute={}, resource={}.", attribute, resource); 
        }
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

    public void removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from member_resource_attr_values where attr_id=? and member_id=? and resource_id=?", attribute.getId(), member.getId(), resource.getId())) {
          log.info("Attribute (its value) was removed from member on resource. Attribute={}, member={}, resource=" + resource, attribute, member); 
        }
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

    public void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from member_attr_values where attr_id=? and member_id=?", attribute.getId(), member.getId())) {
          log.info("Attribute (its value) was removed from member. Attribute={}, member={}", attribute, member); 
        }
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

    public void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from user_facility_attr_values where attr_id=? and user_id=? and facility_id=?", attribute.getId(), user.getId(), facility.getId())) {
          log.info("Attribute (its value) was removed from user on facility. Attribute={}, user={}, facility=" + facility, attribute, user); 
        }
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

    public void removeVirtualAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException {
      getFacilityUserVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, facility, user, attribute);
    }
    
    public void removeVirtualAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
      getResourceVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, attribute);
    }
    
    public void removeVirtualAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
      getResourceGroupVirtualAttributeModule(sess, attribute).removeAttributeValue((PerunSessionImpl) sess, resource, group, attribute);
    }

    public void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException {
      try {
        if(0 < jdbc.update("delete from user_attr_values where attr_id=? and user_id=?", attribute.getId(), user.getId())) {
          log.info("Attribute (its value) was removed from user. Attribute={}, user={}", attribute, user); 
        }
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
    public void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException {
        try {
            if(0 < jdbc.update("delete from group_resource_attr_values where attr_id=? and resource_id=? and group_id=?", attribute.getId(),resource.getId(),group.getId())) {
                log.info("Attribute (its value) was removed from group on resource. Attribute={}, group={}, resource=" + attribute, group, resource);
            }
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
    public void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException {
        try {
        if(0 < jdbc.update("delete from host_attr_values where attr_id=? and host_id=?", attribute.getId(), host.getId())) {
          log.info("Attribute (its value) was removed from host. Attribute={}, host={}", attribute, host);
        }
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

    public boolean attributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
      Utils.notNull(attribute, "attribute");
      Utils.notNull(attribute.getName(), "attribute.name");
      Utils.notNull(attribute.getNamespace(), "attribute.namespace");
      Utils.notNull(attribute.getType(), "attribute.type");

      return 1 == jdbc.queryForInt("select count('x') from attr_names where attr_name=? and friendly_name=? and namespace=? and id=? and type=?", attribute.getName(), attribute.getFriendlyName(), attribute.getNamespace(), attribute.getId(), attribute.getType());
    }

    public boolean actionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException {
      Utils.notNull(actionType, "actionType");
      Utils.notNull(actionType.getActionType(), "actionType.actionType");

      return 1 == jdbc.queryForInt("select count('x') from action_types where action_type=?", actionType.getActionType());
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

    public boolean isLargeAttribute(PerunSession sess, AttributeDefinition attribute) {
      return attribute.getType().equals(LinkedHashMap.class.getName());
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
      return ATTRIBUTES_MODULES_PACKAGE + "." + attributeName.replaceAll(":|-", "_");
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
     * Get the atributeModule for the attribute
     *
     * @param attribute get the attribute module for this attribute
     * @see AttributesManagerImpl#getAttributesModule(String)
     */
    public Object getAttributesModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
      String moduleName = attributeNameToModuleName(attribute.getNamespace() + ":" + attribute.getBaseFriendlyName());
      return getAttributesModule(sess, moduleName);
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
        log.debug("Attribute module not found. Module name={}", moduleName);
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
      attributes.add(attr);

      //Facility.name
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("name");
      attributes.add(attr);

      //Facility.type
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("type");
      attributes.add(attr);

      //Resource.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //Resource.name
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("name");
      attributes.add(attr);

      //Resource.description
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("description");
      attributes.add(attr);

      //Member.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //User.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //User.firstName
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("firstName");
      attributes.add(attr);

      //User.lastName
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("lastName");
      attributes.add(attr);

      //User.middleName
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("middleName");
      attributes.add(attr);

      //User.titleBefore
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("titleBefore");
      attributes.add(attr);

      //User.titleAfter
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("titleAfter");
      attributes.add(attr);

      //Group.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //Group.name
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("name");
      attributes.add(attr);

      //Group.description
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("description");
      attributes.add(attr);

      //Vo.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //Vo.name
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("name");
      attributes.add(attr);

      //Vo.shortName
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("shortName");
      attributes.add(attr);

      //Host.id
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
      attr.setType(Integer.class.getName());
      attr.setFriendlyName("id");
      attributes.add(attr);

      //Host.hostname
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
      attr.setType(String.class.getName());
      attr.setFriendlyName("hostname");
      attributes.add(attr);


      // *** Def attributes
      
      //urn:perun:group:attribute-def:def:groupExtSource
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
      attr.setType(String.class.getName());
      attr.setFriendlyName("groupExtSource");
      attributes.add(attr);
      
      //urn:perun:group:attribute-def:def:groupMembersExtSource
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
      attr.setType(String.class.getName());
      attr.setFriendlyName("groupMembersExtSource");
      attributes.add(attr);
      
      //urn:perun:group:attribute-def:def:groupMembersQuery
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
      attr.setType(String.class.getName());
      attr.setFriendlyName("groupMembersQuery");
      attributes.add(attr);
      
      //urn:perun:group:attribute-def:def:synchronizatinEnabled
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
      attr.setType(String.class.getName());
      attr.setFriendlyName("synchronizationEnabled");
      attributes.add(attr);
      
    //urn:perun:group:attribute-def:def:synchronizationInterval
      attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
      attr.setType(String.class.getName());
      attr.setFriendlyName("synchronizationInterval");
      attributes.add(attr);
      
      for(AttributeDefinition attribute : attributes) {
        if(!checkAttributeExistsForInitialize(attribute)) createAttributeExistsForInitialize(attribute);
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

    private void createAttributeExistsForInitialize(AttributeDefinition attribute) throws InternalErrorException {
      try {
        int attributeId = Utils.getNewId(jdbc, "attr_names_id_seq");
        
        jdbc.update("insert into attr_names (id, attr_name, type, dsc, namespace, friendly_name, default_attr_id) values (?,?,?,?,?,?,NULL)", 
                    attributeId, attribute.getName(), attribute.getType(), attribute.getDescription(), attribute.getNamespace(), attribute.getFriendlyName());
        log.info("Attribute created during inicialization of attributesMamager: {}", attribute);
      } catch (DataIntegrityViolationException e) {
        throw new ConsistencyErrorException("Attribute already exists: " + attribute, e);
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }

    public void setPerun(Perun perun) {
        this.perun = perun;
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

    @Override
    public List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException {
        try {
        SqlRowSet rs = jdbc.queryForRowSet("select " + attributeRightSelectQuery + " from attributes_authz join roles on "
                + "attributes_authz.role_id=roles.id join action_types on attributes_authz.action_type_id=action_types.id where "
                + "attributes_authz.attr_id=?", attributeId);
        
        List<AttributeRights> attributeRights = new ArrayList<AttributeRights>();
        rs.beforeFirst();
        Role role;
        ActionType actionType;
        boolean roleExists;
        while (rs.next()) {       
            role = Role.valueOf(rs.getString("role_name").toUpperCase());
            actionType = ActionType.valueOf(rs.getString("action_type").toUpperCase());
            roleExists = false;
            
            Iterator itr = attributeRights.iterator();
            while ((itr.hasNext())&&(!roleExists)) {
                AttributeRights right = (AttributeRights) itr.next();
                if (right.getRole().equals(role)) {
                    right.getRights().add(actionType);
                    roleExists = true;
                }
            }
            
            if (!roleExists) {
                List<ActionType> actionTypes = new ArrayList<ActionType>();
                    actionTypes.add(actionType);
                    attributeRights.add(new AttributeRights(attributeId, role, actionTypes));
                }
            }
            // add roles with empty rights
            List<Role> listOfRoles = new ArrayList<Role>();
            listOfRoles.add(Role.FACILITYADMIN);
            listOfRoles.add(Role.GROUPADMIN);
            listOfRoles.add(Role.SELF);
            listOfRoles.add(Role.VOADMIN);
            for (Role roleToTry : listOfRoles) {
                roleExists = false;

                Iterator itr = attributeRights.iterator();
                while ((itr.hasNext()) && (!roleExists)) {
                    AttributeRights right = (AttributeRights) itr.next();
                    if (right.getRole().equals(roleToTry)) {
                        roleExists = true;
                    }
                }
                if (!roleExists) {
                    attributeRights.add(new AttributeRights(attributeId, roleToTry, new ArrayList<ActionType>()));
                }
            }
            
            return attributeRights;
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public void setAttributeRight(PerunSession sess, AttributeRights rights) throws InternalErrorException {
        try {
            // get action types of the attribute and role from the database
            SqlRowSet rs = jdbc.queryForRowSet("select action_types.action_type as action_type from attributes_authz join action_types "
                    + "on attributes_authz.action_type_id=action_types.id where attr_id=? and "
                    + "role_id=(select id from roles where name=?)", rights.getAttributeId(), rights.getRole().getRoleName());
            rs.beforeFirst();
            List<ActionType> dbActionTypes = new ArrayList<ActionType>();
            while (rs.next()) {
                dbActionTypes.add(ActionType.valueOf(rs.getString("action_type").toUpperCase()));
            }
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
