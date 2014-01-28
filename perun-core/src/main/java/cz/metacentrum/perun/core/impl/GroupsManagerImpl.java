package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of GroupsManager
 * 
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class GroupsManagerImpl implements GroupsManagerImplApi {

  private final static Logger log = LoggerFactory.getLogger(GroupsManagerImpl.class);

  public final static int MEMBERSGROUP = 1;
  public final static int ADMINSGROUP = 2;
  public final static int SUBGROUP = 3;

  protected final static String groupMappingSelectQuery = "groups.id as groups_id, groups.parent_group_id as groups_parent_group_id, groups.name as groups_shortName, groups.dsc as groups_dsc, " +
      "groups.vo_id as groups_vo_id, groups.created_at as groups_created_at, groups.created_by as groups_created_by, groups.modified_by as groups_modified_by, groups.modified_at as groups_modified_at, " + 
      "groups.modified_by_uid as groups_modified_by_uid, groups.created_by_uid as groups_created_by_uid, "+
"("+Compatibility.getWithClause()+" temp (id,name,parent_group_id) as ( " +
"select id, name"+Compatibility.castToVarchar()+", parent_group_id " +
"from Groups " +
"where parent_group_id is null " +
"union all " +
"(select Groups.id, concat(temp.name"+Compatibility.castToVarchar()+",concat(':',Groups.name"+Compatibility.castToVarchar()+"))"+Compatibility.castToVarchar()+" , Groups.parent_group_id " +
"from Groups " +
"inner join temp " +
"    on temp.id = Groups.parent_group_id " +
") " +
") " +
"select name"+Compatibility.castToVarchar()+" from temp where temp.id = groups.id ) as groups_name ";

  // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private SimpleJdbcTemplate jdbc;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  // Group mapper
  protected static final RowMapper<Group> GROUP_MAPPER = new RowMapper<Group>() {
    public Group mapRow(ResultSet rs, int i) throws SQLException {
      Group g = new Group();
      g.setId(rs.getInt("groups_id"));
      //ParentGroup with ID=0 is not supported
      if(rs.getInt("groups_parent_group_id") != 0) g.setParentGroupId(rs.getInt("groups_parent_group_id"));
      else g.setParentGroupId(null);
      g.setName(rs.getString("groups_name"));
      
      g.setShortName(rs.getString("groups_shortName"));
      g.setDescription(rs.getString("groups_dsc"));
      g.setVoId(rs.getInt("groups_vo_id"));
      g.setCreatedAt(rs.getString("groups_created_at"));
      g.setCreatedBy(rs.getString("groups_created_by"));
      g.setModifiedAt(rs.getString("groups_modified_at"));
      g.setModifiedBy(rs.getString("groups_modified_by"));
      if(rs.getInt("groups_modified_by_uid") == 0) g.setModifiedByUid(null);
      else g.setModifiedByUid(rs.getInt("groups_modified_by_uid"));
      if(rs.getInt("groups_created_by_uid") == 0) g.setCreatedByUid(null);
      else g.setCreatedByUid(rs.getInt("groups_created_by_uid"));
      return g;
    }
  };

  private static final RowMapper<Pair<Group, Resource>> GROUP_RESOURCE_MAPPER = new RowMapper<Pair<Group, Resource>>() {
    public Pair<Group, Resource> mapRow(ResultSet rs, int i) throws SQLException {
      Pair<Group, Resource> pair = new Pair<Group, Resource>();
      pair.put(GROUP_MAPPER.mapRow(rs, i), ResourcesManagerImpl.RESOURCE_MAPPER.mapRow(rs, i));
      return pair;
    }
  };
  
  /**
   * Create new instance of this class.
   * 
   */
  public GroupsManagerImpl(DataSource perunPool) {
    this.jdbc = new SimpleJdbcTemplate(perunPool);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
  }

  public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, InternalErrorException {
    Utils.notNull(group, "group");
    Utils.notNull(group.getName(), "group.getName()");
      
    // Check if the group already exists
    if (1 == jdbc.queryForInt("select count('x') from groups where name=? and vo_id=?", group.getShortName(), vo.getId())) {
      throw new GroupExistsException("Group [" + group.getName() + "] already exists under VO [" + vo.getShortName() + "]");
    }

     
    // Check the group name, it can contain only a-Z0-9_- and space
    if (!group.getShortName().matches("^[- a-zA-Z.0-9_]+$")) {
      throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name can contain only a-Z0-9.-_: and space characters. " + group));
    }

    try {
      // Store the group into the DB
      int newId = Utils.getNewId(jdbc, "groups_id_seq");

      jdbc.update("insert into groups (id, parent_group_id, name, dsc, vo_id, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " + 
              "values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, group.getParentGroupId(),
              group.getShortName(), group.getDescription(), vo.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      group.setId(newId);
      
      group.setVoId(vo.getId());
        
      return group;
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  
  
  public String getName(int id){
    List name= jdbc.query("group.name as (with temp (name, id, parent_group_id) as ((select name, id, parent_group_id from GROUPS where parent_group_id is null) union all (select cast((temp.name + ':' + groups.name) as varchar(128)), " +
      "groups.id, groups.parent_group_id from groups inner join temp on temp.id = groups.parent_group_id )) select name from temp where group.id = ?"
,new RowMapper() {
      public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getString(1);
      }
},id);
      String result=(String)name.get(0);
 return result;
  }
  
  /*
   * Create a subgroup
   * 
   * @see cz.metacentrum.perun.core.implApi.GroupsManagerImplApi#createGroup(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Group)
   */
  public Group createGroup(PerunSession sess, Vo vo, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException {
    // Create new subGroup
      
    group.setParentGroupId(parentGroup.getId());
    
    group.setName(parentGroup.getName()+":"+group.getShortName());
      
    group = createGroup(sess, vo, group);
      
    return group;
  }

  public void deleteGroup(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupAlreadyRemovedException {
    Utils.notNull(group.getName(), "group.getName()");

    try {
      // Delete group's members
      jdbc.update("delete from groups_members where source_group_id=?", group.getId());

      // Delete authz entries for this group
      jdbc.update("delete from authz where group_id=?", group.getId());
      
      int rowAffected = jdbc.update("delete from groups where id=?", group.getId());
      if(rowAffected == 0) throw new GroupAlreadyRemovedException("Group: " + group + " , Vo: " + vo);
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  public Group updateGroup(PerunSession sess, Group group)      throws InternalErrorException {
    Utils.notNull(group.getName(), "group.getName()");

    // Get the group stored in the DB
    Group dbGroup;
    try {
      dbGroup = this.getGroupById(sess, group.getId());
    } catch (GroupNotExistsException e) {
      throw new InternalErrorException("Group existence was checked at the higher level",e);
    }

    if (!dbGroup.getShortName().equals(group.getShortName())) {
      try {
        jdbc.update("update groups set name=?,modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", group.getShortName(),
                sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), group.getId());
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
      dbGroup.setShortName(group.getShortName());
    }

    if (group.getDescription() != null && !group.getDescription().equals(dbGroup.getDescription())) {
      try {
        jdbc.update("update groups set dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", group.getDescription(),
                sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), group.getId());
      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
      dbGroup.setDescription(group.getDescription());
    }
    return dbGroup;
  }

  public Group getGroupById(PerunSession sess, int id) throws GroupNotExistsException, InternalErrorException {
    try {
      Group group = jdbc.queryForObject("select " + groupMappingSelectQuery + " from groups where groups.id=? ", GROUP_MAPPER, id);
      return group;
    } catch (EmptyResultDataAccessException err) {
      throw new GroupNotExistsException("Group id=" + id);
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  public List<User> getGroupUsers(PerunSession sess, Group group) throws InternalErrorException { 
    try {
      return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from groups_members join members on members.id=member_id join " +
          "users on members.user_id=users.id where group_id=? order by "+Compatibility.orderByBinary("users.last_name")+", " +
          Compatibility.orderByBinary("users.first_name")+")", UsersManagerImpl.USER_MAPPER, group.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public boolean isUserMemberOfGroup(PerunSession sess, User user, Group group) throws InternalErrorException {
    try {
      return 1 <= jdbc.queryForInt("select count(1) from groups_members join members on members.id = member_id where members.user_id=? and groups_members.group_id=?", user.getId(), group.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
    try {
      return jdbc.query("select " + MembersManagerImpl.memberMappingSelectQuery + ", groups_members.membership_type as membership_type from groups_members join members on members.id=groups_members.member_id " +
          " where groups_members.group_id=?", MembersManagerImpl.MEMBER_MAPPER, group.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Member>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public List<Member> getGroupMembers(PerunSession sess, Group group, List<Status> statuses, boolean excludeStatusInsteadOfIncludeStatus) throws InternalErrorException {
    try {
      MapSqlParameterSource parameters = new MapSqlParameterSource();
      List<Integer> statusesCodes = new ArrayList<Integer>();
      for (Status status: statuses) {
        statusesCodes.add(status.getCode());
      }
      parameters.addValue("statuses", statusesCodes);
      parameters.addValue("group_id", group.getId());

      if (excludeStatusInsteadOfIncludeStatus) {
        // Exclude members with one of the status
        return this.namedParameterJdbcTemplate.query("select " + MembersManagerImpl.memberMappingSelectQuery + ", groups_members.membership_type as membership_type from groups_members join members on members.id=groups_members.member_id " +
            " where groups_members.group_id=:group_id and members.status"+Compatibility.castToInteger()+" not in (:statuses)", parameters, MembersManagerImpl.MEMBER_MAPPER);
      } else {
        // Include members with one of the status
        return this.namedParameterJdbcTemplate.query("select " + MembersManagerImpl.memberMappingSelectQuery + ", groups_members.membership_type as membership_type from groups_members join members on members.id=groups_members.member_id " +
            " where groups_members.group_id=:group_id and members.status"+Compatibility.castToInteger()+" in (:statuses)", parameters, MembersManagerImpl.MEMBER_MAPPER);
      }
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Member>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException {
    try {
      return jdbc.query("select  " + groupMappingSelectQuery + " from groups " + 
          "where vo_id=? order by "+Compatibility.orderByBinary("groups.name"+Compatibility.castToVarchar()),
          GROUP_MAPPER, vo.getId());

    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + " from groups join " + 
          "groups_resources on groups.id=groups_resources.group_id "+
          "where groups_resources.resource_id=?",
          GROUP_MAPPER, resource.getId());
    } catch (EmptyResultDataAccessException e) {      
      return new ArrayList<Group>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException {
    try {
      return jdbc.query("select  " + groupMappingSelectQuery + " from groups " + 
          "where groups.parent_group_id=? order by "+Compatibility.orderByBinary("groups.name"+Compatibility.castToVarchar()),
          GROUP_MAPPER, parentGroup.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Group>();
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    } 
  }

  public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException {
    try {
      return jdbc.queryForInt("select count(1) from groups where parent_group_id=?", parentGroup.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<Group> getAllGroups(PerunSession sess, Vo vo)     throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + " from groups where vo_id=?", GROUP_MAPPER, vo.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  public Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, ParentGroupNotExistsException {
    try  {
      return jdbc.queryForObject("select " + groupMappingSelectQuery + " from groups " +
          "where groups.id=?", GROUP_MAPPER, group.getParentGroupId());
    } catch (EmptyResultDataAccessException e) {
      throw new ParentGroupNotExistsException(e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public Group getGroupByName(PerunSession sess, Vo vo, String name) throws GroupNotExistsException, InternalErrorException {
    try {
      return jdbc.queryForObject("select " + groupMappingSelectQuery + " from groups where groups.name=? and groups.vo_id=?",
          GROUP_MAPPER, name, vo.getId());
    } catch (EmptyResultDataAccessException err) {
      throw new GroupNotExistsException("Group name=" + name + ", vo id=" + vo.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  public Member addMember(PerunSession sess, Group group, Member member, MembershipType type, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException {
    //TODO already member exception
      member.setMembershipType(type);
    try { 
      jdbc.update("insert into groups_members (group_id, member_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid, membership_type, source_group_id) " +
          "values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?,?,?)", group.getId(),
          member.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId(), type.getCode(), sourceGroupId);
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex); 
    }
    return member;

  }

  public List<Group> getGroupsByIds(PerunSession sess, List<Integer> groupsIds) throws InternalErrorException {
    // If usersIds is empty, we can immediatelly return empty results
    if (groupsIds.size() == 0) {
      return new ArrayList<Group>();
    }

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("ids", groupsIds);

    try {
      return this.namedParameterJdbcTemplate.query("select " + groupMappingSelectQuery + "  from groups where groups.id in ( :ids )", 
          parameters, GROUP_MAPPER);
    } catch(EmptyResultDataAccessException ex) {
      return new ArrayList<Group>();
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }


  public List<Integer> getMemberGroupsIds(PerunSession sess, Member member, Vo vo) throws InternalErrorException {
    try {
      return jdbc.query("select groups.id as id from groups_members join groups on groups_members.group_id = groups.id " + 
          "where groups.vo_id=? and groups_members.member_id=?", 
          Utils.ID_MAPPER, vo.getId(), member.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + " from groups_members join groups on groups_members.group_id = groups.id " + 
          "where groups_members.member_id=?", 
          GROUP_MAPPER, member.getId());
    } catch (EmptyResultDataAccessException e) {      
      return new ArrayList<Group>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }  
  
  public List<Integer> getMemberGroupsIdsForResources(PerunSession sess, Member member, Vo vo) throws InternalErrorException {    
    /*XXX New DB implementation ...  
    //FIXME Where can I get the resource?
    try {
      jdbc.query("select groups_members.group_id as id from groups_members join groups_resources on groups_members.group_id = groups_resources.group_id " +
                 "where groups_members.member_id=? and groups_resources.resource_id=?", 
                 Utils.ID_MAPPER, member.getId(), resource.getId()); 
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
     */
    //FIXME delete this method after removing grouper
    throw new InternalErrorException("Unsupported method");
  }

  public List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + " from groups " + 
          "join group_attr_values on groups.id=group_attr_values.group_id where group_attr_values.attr_id=? and " +
          "group_attr_values.attr_value=?",
          GROUP_MAPPER, attribute.getId(), BeansUtils.attributeValueToString(attribute));
    } catch (EmptyResultDataAccessException e) {      
      return new ArrayList<Group>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public List<Pair<Group,Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + ", " + ResourcesManagerImpl.resourceMappingSelectQuery + 
          " from group_resource_attr_values " + 
          "join groups on groups.id=group_resource_attr_values.group_id " + 
          "join resources on resources.id=group_resource_attr_values.resource_id " + 
          "where group_resource_attr_values.attr_id=? and group_resource_attr_values.attr_value=?",
          GROUP_RESOURCE_MAPPER, attribute.getId(), BeansUtils.attributeValueToString(attribute));
    } catch (EmptyResultDataAccessException e) {      
      return new ArrayList<Pair<Group, Resource>>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public boolean isGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException {
    try {
      return 1 <= jdbc.queryForInt("select count(1) from groups_members where group_id=? and member_id=?", group.getId(), member.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public boolean isDirectGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException {
    try {
        int count = jdbc.queryForInt("select count(1) from groups_members where group_id=? and member_id=? and membership_type = ?", group.getId(), member.getId(), MembershipType.DIRECT.getCode());
        if (1 < count) throw new ConsistencyErrorException("There is more than one direct member in group" + group);
      return 1 == count;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException {
    int ret;
    try {
      ret = jdbc.update("delete from groups_members where source_group_id=? and member_id=?", group.getId(), member.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
    if(ret == 0) {
      throw new NotGroupMemberException(member);
    } else if(ret >= 1) {
      return;
    } else {
      throw new ConsistencyErrorException(member + " and " + group + " have " + ret + " rows in groups_members table");
    }

  }

  public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
    try {
      // Add GROUPADMIN role + groupId and voId
      jdbc.update("insert into authz (user_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)", 
          user.getId(), Role.GROUPADMIN.getRoleName(), group.getId(), group.getVoId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in group " + group, e, user, group);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)", 
          authorizedGroup.getId(), Role.GROUPADMIN.getRoleName(), group.getId(), group.getVoId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + authorizedGroup.getId() + " is already group admin in group " + group, e, authorizedGroup, group);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
    try {
      if (0 == jdbc.update("delete from authz where user_id=? and group_id=? and role_id=(select id from roles where name=?)", 
          user.getId(), group.getId(), Role.GROUPADMIN.getRoleName())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the group " + group);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  @Override
  public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
    try {
      if (0 == jdbc.update("delete from authz where authorized_group_id=? and group_id=? and role_id=(select id from roles where name=?)", 
          authorizedGroup.getId(), group.getId(), Role.GROUPADMIN.getRoleName())) {
        throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not admin of the group " + group);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getAdmins(PerunSession sess, Group group) throws InternalErrorException {
    try {
        Set<User> setOfAdmins = new HashSet<User>();
        // direct admins
        setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
          "where authz.group_id=? and authz.role_id=(select id from roles where name='groupadmin')", UsersManagerImpl.USER_MAPPER, group.getId()));
        
        // admins through a group
          List<Group> listOfGroupAdmins = getGroupAdmins(sess, group);
          for(Group authorizedGroup : listOfGroupAdmins) {
              setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users join members on users.id=members.user_id " +
                      "join groups_members on groups_members.member_id=members.id where groups_members.group_id=?", UsersManagerImpl.USER_MAPPER, authorizedGroup.getId()));
          }
          
          return new ArrayList(setOfAdmins);
          
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<User>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
   @Override
  public List<User> getDirectAdmins(PerunSession sess, Group group) throws InternalErrorException {
    try {
        return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
          "where authz.group_id=? and authz.role_id=(select id from roles where name='groupadmin')", UsersManagerImpl.USER_MAPPER, group.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<User>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  
  @Override
   public List<Group> getGroupAdmins(PerunSession sess, Group group) throws InternalErrorException {
    try {
      return jdbc.query("select " + groupMappingSelectQuery + " from authz join groups on authz.authorized_group_id=groups.id " +
          "where authz.group_id=? and authz.role_id=(select id from roles where name='groupadmin')", 
          GROUP_MAPPER, group.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Group>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException {
    try {
      return jdbc.queryForInt("select count(1) from groups where vo_id=?", vo.getId());
    } catch(RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public int getVoId(PerunSession sess, Group group) throws InternalErrorException {
    try {
      return jdbc.queryForInt("select vo_id from groups where id=?", group.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public void checkGroupExists(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException {
    if (!groupExists(sess, group)) throw new GroupNotExistsException("Group " + group);
  }

  public boolean groupExists(PerunSession sess, Group group) throws InternalErrorException {
    try {
      return 1 == jdbc.queryForInt("select 1 from groups where id=?", group.getId());
    } catch(EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Gets all groups which have enabled synchronization.
   * 
   * @param sess
   * @return list of groups to synchronize
   * @throws InternalErrorException
   */
  public List<Group> getGroupsToSynchronize(PerunSession sess) throws InternalErrorException {
    try {
      // Get all groups which have defined
      return jdbc.query("select " + groupMappingSelectQuery + " from groups, attr_names, group_attr_values" +
          " where attr_names.attr_name=? and attr_names.id=group_attr_values.attr_id and group_attr_values.attr_value='true' and " +
          "group_attr_values.group_id=groups.id", GROUP_MAPPER, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<Group>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
  /**
   * Converts Member to its Id in grouper.
   * 
   * @param member
   *            member
   * @return grouper Id
   */
  protected static String toGrouperId(PerunSession sess, Member member) throws InternalErrorException {
    log.trace("Entering toGrouperId: member='" + member + "'");
    if (member == null) throw new InternalErrorRuntimeException(new NullPointerException("member"));
    return Integer.toString(member.getUserId());
  }

    @Override
    public List<Integer> getGroupApplicationIds(PerunSession sess, Group group) {
        // get app ids for all applications
        return jdbc.query("select id from application where group_id=?", new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int arg1)
                    throws SQLException {
                return rs.getInt("id");
            }
        },group.getId());
    }

    @Override
    public List<Pair<String, String>> getApplicationReservedLogins(Integer appId) {
        return jdbc.query("select namespace,login from application_reserved_logins where app_id=?", new RowMapper<Pair<String, String>>() {
            @Override
            public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
                return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
            }
        }, appId);
    }

    @Override
    public void deleteGroupReservedLogins(PerunSession sess, Group group) {
        // remove all reserved logins first
        for (Integer appId : getGroupApplicationIds(sess, group)) {
            jdbc.update("delete from application_reserved_logins where app_id=?", appId);
        }
    }
}
