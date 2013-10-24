package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.springframework.dao.EmptyResultDataAccessException;

public class AuthzResolverImpl implements AuthzResolverImplApi {

  final static Logger log = LoggerFactory.getLogger(FacilitiesManagerImpl.class);

  //http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private static SimpleJdbcTemplate jdbc;
  
  private final static Pattern patternForExtractingPerunBean = Pattern.compile("^pb_([a-z]+)_id$");

  public final static String authzRoleMappingSelectQuery = " authz.user_id as authz_user_id, authz.role_id as authz_role_id," +
  		"authz.authorized_group_id as authz_authorized_group_id, authz.vo_id as pb_vo_id, authz.group_id as pb_group_id, " +
                "authz.facility_id as pb_facility_id, authz.member_id as pb_member_id, authz.resource_id as pb_resource_id, " +
                "authz.service_id as pb_service_id, authz.service_principal_id as pb_user_id";
  
  
  protected static final RowMapper<Role> AUTHZROLE_MAPPER_FOR_ATTRIBUTES = new RowMapper<Role>() {
    public Role mapRow(ResultSet rs, int i) throws SQLException {
      Role role = Role.valueOf(rs.getString("name").toUpperCase());
      return role;
    }
  };
  
  public static final RowMapper<Pair<Role, Map<String, List<Integer>>>> AUTHZROLE_MAPPER = new RowMapper<Pair<Role, Map<String, List<Integer>>>>() {
    public Pair<Role, Map<String, List<Integer>>> mapRow(ResultSet rs, int i) throws SQLException {
      try {
        Map<String, List<Integer>> perunBeans = null;
        Role role = Role.valueOf(rs.getString("role_name").toUpperCase());
        
        // Iterate through all returned columns and try to extract PerunBean name from the labels
        for (int j = rs.getMetaData().getColumnCount(); j > 0; j--) {
          Matcher matcher = patternForExtractingPerunBean.matcher(rs.getMetaData().getColumnLabel(j).toLowerCase());
          if (matcher.find()) {
            String perunBeanName = matcher.group(1);
            int id = rs.getInt(j);
            if (!rs.wasNull()) {
              // We have to make first letter upercase
              String className = perunBeanName.substring(0, 1).toUpperCase() + perunBeanName.substring(1);
              
              if (perunBeans == null) {
                perunBeans = new HashMap<String, List<Integer>>();
              }
              if (perunBeans.get(className) == null) {
                perunBeans.put(className, new ArrayList<Integer>());
                
              }
              perunBeans.get(className).add(id);
            }
          }
        }
        
        return new Pair<Role, Map<String, List<Integer>>>(role, perunBeans);
        
      } catch (Exception e) {
        throw new InternalErrorRuntimeException(e);
      }
    }
  };
  
  public AuthzResolverImpl(DataSource perunPool) {
    jdbc = new SimpleJdbcTemplate(perunPool);
  }

  public AuthzRoles getRoles(User user) throws InternalErrorException {
    AuthzRoles authzRoles = new AuthzRoles();
    
    if (user != null) {
    try {
                  // Get roles from Authz table
		  List<Pair<Role, Map<String, List<Integer>>>> authzRolesPairs = jdbc.query("select " + authzRoleMappingSelectQuery
                          + ", roles.name as role_name from authz left join roles on authz.role_id=roles.id where authz.user_id=? or authorized_group_id in "
                          + "(select groups.id from groups join groups_members on groups.id=groups_members.group_id join members on "
                          + "members.id=groups_members.member_id join users on users.id=members.user_id where users.id=?)", AUTHZROLE_MAPPER, user.getId(), user.getId());
	    
		  for (Pair<Role, Map<String, List<Integer>>> pair : authzRolesPairs) {
		    authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight());
		  }
		  
		  // Get service users for user
		  List<Integer> authzServiceUsers = jdbc.query("select service_user_users.service_user_id as id from users, " +
		  		"service_user_users where users.id=service_user_users.user_id and users.id=?", Utils.ID_MAPPER ,user.getId());
		  for (Integer serviceUserId : authzServiceUsers) {
		    authzRoles.putAuthzRole(Role.SELF, User.class, serviceUserId);
		  }
		  
		  // Get members for user
		  List<Integer> authzMember = jdbc.query("select members.id as id from members where members.user_id=?", 
		      Utils.ID_MAPPER ,user.getId());
      for (Integer memberId : authzMember) {
        authzRoles.putAuthzRole(Role.SELF, Member.class, memberId);
      }
		  
	  } catch (RuntimeException e) {
		  throw new InternalErrorException(e);
	  }
    } 
    
    return authzRoles;
  }
  
  public void initialize() throws InternalErrorException {
    
    // Check if all roles defined in class Role exists in the DB
    for (Role role: Role.values()) {
      try {
        if (0 == jdbc.queryForInt("select count(*) from roles where name=?", role.getRoleName())) {
          int newId = Utils.getNewId(jdbc, "roles_id_seq");
          jdbc.update("insert into roles (id, name) values (?,?)", newId, role.getRoleName());
        }
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }
  }
  
  public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException {
    String actType = actionType.getActionType().toLowerCase();
    try {
      return jdbc.query("select distinct roles.name from attributes_authz " + 
               "join roles on attributes_authz.role_id=roles.id " + 
               "join action_types on attributes_authz.action_type_id=action_types.id " +
                "where attributes_authz.attr_id=? and action_types.action_type=?", AUTHZROLE_MAPPER_FOR_ATTRIBUTES, attrDef.getId(), actType);
    } catch (EmptyResultDataAccessException e) {      
      return new ArrayList<Role>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  public void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException {
    try {
      jdbc.update("delete from authz where user_id=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  public void removeAllGroupAuthz(PerunSession sess, Group group) throws InternalErrorException {
      try {
          jdbc.update("delete from authz where authorized_group_id=?", group.getId());
      } catch (RuntimeException e) {
          throw new InternalErrorException(e);
      }
  }
}
