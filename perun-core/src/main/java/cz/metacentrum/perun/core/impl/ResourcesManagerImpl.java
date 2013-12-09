package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id: 7a518311bfc5616bc7c7addd63ecd50b5de50583 $
 */
public class ResourcesManagerImpl implements ResourcesManagerImplApi {

    private static final int MERGE_TRY_CNT = 10;
    private static final long MERGE_RAND_SLEEP_MAX = 100;  //max sleep time between SQL merge atempt in milisecond

    final static Logger log = LoggerFactory.getLogger(ResourcesManagerImpl.class);

    protected final static String resourceMappingSelectQuery = "resources.id as resources_id, resources.facility_id as resources_facility_id, " +
            "resources.name as resources_name, resources.dsc as resources_dsc, resources.vo_id as resources_vo_id, " +
            "resources.created_at as resources_created_at, resources.created_by as resources_created_by, resources.modified_by as resources_modified_by, " +
            "resources.modified_at as resources_modified_at, resources.modified_by_uid as resources_modified_by_uid, resources.created_by_uid as resources_created_by_uid";

    protected final static String resourceTagMappingSelectQuery = "res_tags.id as res_tags_id, res_tags.vo_id as res_tags_vo_id, res_tags.tag_name as res_tags_tag_name, " +
            "res_tags.created_at as res_tags_created_at, res_tags.created_by as res_tags_created_by, res_tags.modified_by as res_tags_modified_by, " +
            "res_tags.modified_at as res_tags_modified_at, res_tags.modified_by_uid as res_tags_modified_by_uid, res_tags.created_by_uid as res_tags_created_by_uid";

    private SimpleJdbcTemplate jdbc;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    protected static final RowMapper<Resource> RESOURCE_MAPPER = new RowMapper<Resource>() {
        public Resource mapRow(ResultSet rs, int i) throws SQLException {
            Resource resource = new Resource();
            resource.setId(rs.getInt("resources_id"));
            resource.setName(rs.getString("resources_name"));
            resource.setDescription(rs.getString("resources_dsc"));
            resource.setFacilityId(rs.getInt("resources_facility_id"));
            resource.setVoId(rs.getInt("resources_vo_id"));
            resource.setCreatedAt(rs.getString("resources_created_at"));
            resource.setCreatedBy(rs.getString("resources_created_by"));
            resource.setModifiedAt(rs.getString("resources_modified_at"));
            resource.setModifiedBy(rs.getString("resources_modified_by"));
            if(rs.getInt("resources_modified_by_uid") == 0) resource.setModifiedByUid(null);
            else resource.setModifiedByUid(rs.getInt("resources_modified_by_uid"));
            if(rs.getInt("resources_created_by_uid") == 0) resource.setCreatedByUid(null);
            else resource.setCreatedByUid(rs.getInt("resources_created_by_uid"));
            return resource;
        }
    };

    protected static final RowMapper<ResourceTag> RESOURCE_TAG_MAPPER = new RowMapper<ResourceTag>() {
        public ResourceTag mapRow(ResultSet rs, int i) throws SQLException {
            ResourceTag resourceTag = new ResourceTag();
            resourceTag.setId(rs.getInt("res_tags_id"));
            resourceTag.setVoId(rs.getInt("res_tags_vo_id"));
            resourceTag.setTagName(rs.getString("res_tags_tag_name"));
            resourceTag.setCreatedAt(rs.getString("res_tags_created_at"));
            resourceTag.setCreatedBy(rs.getString("res_tags_created_by"));
            resourceTag.setModifiedAt(rs.getString("res_tags_modified_at"));
            resourceTag.setModifiedBy(rs.getString("res_tags_modified_by"));
            if(rs.getInt("res_tags_modified_by_uid") == 0) resourceTag.setModifiedByUid(null);
            else resourceTag.setModifiedByUid(rs.getInt("res_tags_modified_by_uid"));
            if(rs.getInt("res_tags_created_by_uid") == 0) resourceTag.setCreatedByUid(null);
            else resourceTag.setCreatedByUid(rs.getInt("res_tags_created_by_uid"));
            return resourceTag;
        }
    };

    protected static final RowMapper<RichResource> RICH_RESOURCE_MAPPER = new RowMapper<RichResource>() {
        public RichResource mapRow(ResultSet rs, int i) throws SQLException {
            RichResource richResource = new RichResource(RESOURCE_MAPPER.mapRow(rs, i));
            richResource.setVo(VosManagerImpl.VO_MAPPER.mapRow(rs, i));
            richResource.setFacility(FacilitiesManagerImpl.FACILITY_MAPPER.mapRow(rs, i));
            return richResource;
        }
    };


    public ResourcesManagerImpl(DataSource perunPool) {
        this.jdbc = new SimpleJdbcTemplate(perunPool);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);

        // Initialize resources manager
        this.initialize();
    }

    public Resource getResourceById(PerunSession sess, int id) throws InternalErrorException, ResourceNotExistsException {
        try {
            return jdbc.queryForObject("select " + resourceMappingSelectQuery + " from resources where resources.id=?", RESOURCE_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotExistsException(e);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    @Override
    public RichResource getRichResourceById(PerunSession sess, int id) throws InternalErrorException, ResourceNotExistsException {
        try {
            return jdbc.queryForObject("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
                    FacilitiesManagerImpl.facilityMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id "
                    + "join facilities on resources.facility_id=facilities.id  where resources.id=?", RICH_RESOURCE_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotExistsException(e);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, ResourceNotExistsException {
        try {
            return jdbc.queryForObject("select " + resourceMappingSelectQuery + " from resources where resources.name=? and facility_id=? and vo_id=?",
                    RESOURCE_MAPPER, name, facility.getId(), vo.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotExistsException(e);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public Resource createResource(PerunSession sess, Vo vo, Resource resource, Facility facility) throws InternalErrorException {
        Utils.notNull(resource.getName(), "resource.getName()");

        try {
            int newId = Utils.getNewId(jdbc, "resources_id_seq");

            jdbc.update("insert into resources(id, name, dsc, facility_id, vo_id, created_by, created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
                    "values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
                    newId, resource.getName(), resource.getDescription(), facility.getId(), vo.getId(), sess.getPerunPrincipal().getActor(),
                    sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

            resource.setId(newId);
            resource.setFacilityId(facility.getId());
            resource.setVoId(vo.getId());

            return resource;
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void deleteResource(PerunSession sess, Vo vo, Resource resource) throws InternalErrorException, ResourceAlreadyRemovedException {
        try {
            // Delete authz entries for this resource
            jdbc.update("delete from authz where resource_id=?", resource.getId());

            int numAffected = jdbc.update("delete from resources where id=?", resource.getId());
            if(numAffected == 0) throw new ResourceAlreadyRemovedException("Resource: " + resource + " , Vo: " + vo);
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Deprecated
    public int getFacilityId(PerunSession sess, Resource resource) throws InternalErrorException {
        try {
            return jdbc.queryForInt("select facility_id from resources where id=?", resource.getId());
        } catch(EmptyResultDataAccessException ex) {
            throw new ConsistencyErrorException("Resource doesn't have assigned facility", ex);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void setFacility(PerunSession sess, Resource resource, Facility facility) throws InternalErrorException {
        try {
            jdbc.update("update resources set facility_id=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", facility.getId(),
                    sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), resource.getId());
            resource.setFacilityId(facility.getId());
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public boolean resourceExists(PerunSession sess, Resource resource) throws InternalErrorException {
        try {
            return 1 == jdbc.queryForInt("select 1 from resources where id=?", resource.getId());
        } catch(EmptyResultDataAccessException ex) {
            return false;
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void checkResourceExists(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException {
        if(!this.resourceExists(sess, resource)) throw new ResourceNotExistsException("resource: " + resource);
    }

    public boolean resourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException {
        try {
            return 1 == jdbc.queryForInt("select 1 from res_tags where id=?", resourceTag.getId());
        } catch(EmptyResultDataAccessException ex) {
            return false;
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException {
        if(!this.resourceTagExists(sess, resourceTag)) throw new ResourceTagNotExistsException("resource: " + resourceTag);
    }

    public List<User> getUsers(PerunSession sess, Resource resource) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + UsersManagerImpl.userMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id join users on " +
                    " users.id=members.user_id where groups_resources.resource_id=?", UsersManagerImpl.USER_MAPPER, resource.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<User>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + UsersManagerImpl.userMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id join users on " +
                    " users.id=members.user_id where groups_resources.resource_id=? and members.status!=?", UsersManagerImpl.USER_MAPPER, resource.getId(), String.valueOf(Status.INVALID.getCode()));
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<User>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + MembersManagerImpl.memberMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
                    " where groups_resources.resource_id=? and members.status!=?", MembersManagerImpl.MEMBER_MAPPER, resource.getId(), String.valueOf(Status.INVALID.getCode()));
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Member>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + MembersManagerImpl.memberMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
                    " where groups_resources.resource_id=?", MembersManagerImpl.MEMBER_MAPPER, resource.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Member>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public boolean isUserAssigned(PerunSession sess, User user, Resource resource) throws InternalErrorException {
        try {
            return (0 < jdbc.queryForInt("select count(*) from groups_resources join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id join users on " +
                    " users.id=members.user_id where groups_resources.resource_id=? and users.id=?", resource.getId(), user.getId()));
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }


    public void assignGroupToResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, GroupAlreadyAssignedException {
        try {
            if(1==jdbc.queryForInt("select count(1) from groups_resources where group_id=? and resource_id=?", group.getId(), resource.getId())) {
                throw new GroupAlreadyAssignedException(group);
            }else{
                jdbc.update("insert into groups_resources (group_id, resource_id, modified_by, modified_at, created_by, created_at, created_by_uid, modified_by_uid) " +
                        "values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", group.getId(),
                        resource.getId(),sess.getPerunPrincipal().getActor(),sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
            }
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void removeGroupFromResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, GroupAlreadyRemovedFromResourceException {
        try {
            int numAffected = jdbc.update("delete from groups_resources where group_id=? and resource_id=?", group.getId(), resource.getId());
            if(numAffected == 0) throw new GroupAlreadyRemovedFromResourceException("Group: " + group + " , Resource: " + resource);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }


    public List<Resource> getAssignedResources(PerunSession sess, Vo vo, Group group) throws InternalErrorException {

        try {
            return jdbc.query("select " + resourceMappingSelectQuery + " from resources join " +
                    "groups_resources on resources.id=groups_resources.resource_id "+
                    "where groups_resources.group_id=?",
                    RESOURCE_MAPPER, group.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources join groups_resources on resources.id=groups_resources.resource_id " +
                    " join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id " +
                    " where groups_members.member_id=?", RESOURCE_MAPPER, member.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) throws InternalErrorException {
        try  {
            return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources join groups_resources on resources.id=groups_resources.resource_id " +
                    " join groups on groups_resources.group_id=groups.id" +
                    " join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
                    " where members.user_id=? and members.vo_id=?", RESOURCE_MAPPER, user.getId(), vo.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public List<RichResource> getAssignedRichResources(PerunSession sess, Group group) throws InternalErrorException {

        try {
            return jdbc.query("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
                    FacilitiesManagerImpl.facilityMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id "
                    + "join facilities on resources.facility_id=facilities.id join groups_resources on "
                    + "resources.id=groups_resources.resource_id where groups_resources.group_id=?", RICH_RESOURCE_MAPPER, group.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<RichResource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException {
        try  {
            return jdbc.query("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
                    FacilitiesManagerImpl.facilityMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id "
                    + "join facilities on resources.facility_id=facilities.id join groups_resources on "
                    + "resources.id=groups_resources.resource_id join groups on groups_resources.group_id=groups.id "
                    + "join groups_members on groups.id=groups_members.group_id where groups_members.member_id=?", RICH_RESOURCE_MAPPER, member.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<RichResource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public boolean isGroupAssigned(PerunSession sess, Group group, Resource resource) throws InternalErrorException {
        try {
            return 1 == jdbc.queryForInt("select count(1) from groups_resources where group_id=? and resource_id=? ", group.getId(), resource.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Integer> getAssignedServices(PerunSession sess, Resource resource) throws InternalErrorException {
        try {
            return jdbc.query("select service_id as id from resource_services where resource_id=?", Utils.ID_MAPPER, resource.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public void assignService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, ServiceAlreadyAssignedException {
        try {
            if (0 < jdbc.queryForInt("select count(*) from resource_services where service_id=? and resource_id=?", service.getId(), resource.getId())) {
                throw new ServiceAlreadyAssignedException(service);
            }
            jdbc.update("insert into resource_services(service_id, resource_id, created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
                    "values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", service.getId(), resource.getId(),
                    sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void removeService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, ServiceNotAssignedException {
        try {
            if(0 == jdbc.update("delete from resource_services where service_id=? and resource_id=?", service.getId(), resource.getId())) {
                throw new ServiceNotAssignedException(service);
            }
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<Resource> getResources(PerunSession sess, Vo vo) throws InternalErrorException {
        try {
            return jdbc.query("select " + resourceMappingSelectQuery+ " from resources where resources.vo_id=?", RESOURCE_MAPPER, vo.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    @Override
    public List<RichResource> getRichResources(PerunSession sess, Vo vo) throws InternalErrorException {
        try {
            return jdbc.query("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
                    FacilitiesManagerImpl.facilityMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id "
                    + "join facilities on resources.facility_id=facilities.id where resources.vo_id=?", RICH_RESOURCE_MAPPER, vo.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<RichResource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Resource> getResourcesByIds(PerunSession sess, List<Integer> resourcesIds) throws InternalErrorException {
        if (resourcesIds.size() == 0) {
            return new ArrayList<Resource>();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", resourcesIds);

        try {
            return this.namedParameterJdbcTemplate.query("select " + resourceMappingSelectQuery + "  from resources where resources.id in ( :ids )",
                    parameters, RESOURCE_MAPPER);
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public Resource updateResource(PerunSession sess, Resource resource) throws InternalErrorException {
        try {
            Map<String, Object> map = jdbc.queryForMap("select name, dsc from resources where id=?", resource.getId());

            if (!resource.getName().equals(map.get("name")) && !resource.getDescription().equals(map.get("dsc"))) {
                jdbc.update("update resources set name=?, dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", resource.getName(),
                        resource.getDescription(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), resource.getId());
            } else if (!resource.getDescription().equals(map.get("dsc"))) {
                jdbc.update("update resources set dsc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", resource.getDescription(),
                        sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), resource.getId());
            } else if (!resource.getName().equals(map.get("name"))) {
                jdbc.update("update resources set name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", resource.getName(),
                        sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), resource.getId());
            }

            return resource;
        } catch (EmptyResultDataAccessException ex) {
            throw new ConsistencyErrorException("Updating non existing Resource", ex);
        } catch (RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public int getResourcesCount(PerunSession perunSession, Vo vo) throws InternalErrorException {
        try {
            return jdbc.queryForInt("select count(*) from resources where resources.vo_id=?",
                    vo.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Resource> getResourcesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
        try {
            return jdbc.query("select " + resourceMappingSelectQuery + " from resources join " +
                    "resource_attr_values on resources.id=resource_attr_values.resource_id " +
                    "where resource_attr_values.attr_id=? and resource_attr_values.attr_value=?",
                    RESOURCE_MAPPER, attribute.getId(), BeansUtils.attributeValueToString(attribute));
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws InternalErrorException {
        try {
            int newId = Utils.getNewId(jdbc, "res_tags_seq");

            jdbc.update("insert into res_tags(id, vo_id, tag_name, created_by, created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
                    "values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
                    newId, vo.getId(), resourceTag.getTagName(), perunSession.getPerunPrincipal().getActor(),
                    perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(),
                    perunSession.getPerunPrincipal().getUserId());

            resourceTag.setId(newId);
            resourceTag.setVoId(vo.getId());

            return resourceTag;
        } catch(RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
        try {
            Map<String, Object> map = jdbc.queryForMap("select tag_name from res_tags where id=?", resourceTag.getId());

            if (!resourceTag.getTagName().equals(map.get("tag_name"))) {
                jdbc.update("update res_tags set tag_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?",
                        resourceTag.getTagName(), perunSession.getPerunPrincipal().getActor(), perunSession.getPerunPrincipal().getUserId(), resourceTag.getId());
            }

            return resourceTag;
        } catch (EmptyResultDataAccessException ex) {
            throw new ConsistencyErrorException("Updating non existing resourceTag", ex);
        } catch (RuntimeException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
        try {
            jdbc.update("delete from res_tags where id=?", resourceTag.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException {
        try {
            jdbc.update("delete from res_tags where vo_id=?", vo.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException {
        try {
            jdbc.update("insert into tags_resources(tag_id, resource_id) values(?,?)", resourceTag.getId(),resource.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException {
        try {
            jdbc.update("delete from tags_resources where tag_id=? and resource_id=?", resourceTag.getId(), resource.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
        try {
            jdbc.update("delete from tags_resources where resource_id=?", resource.getId());
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
        try {
            return jdbc.query("select " + resourceMappingSelectQuery + " from resources join " +
                    "tags_resources on resources.id=tags_resources.resource_id " +
                    "where tags_resources.tag_id=?",
                    RESOURCE_MAPPER, resourceTag.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Resource>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException {
        try {
            return jdbc.query("select " + resourceTagMappingSelectQuery + " from vos join " +
                    "res_tags on vos.id=res_tags.vo_id " +
                    "where res_tags.vo_id=?",
                    RESOURCE_TAG_MAPPER, vo.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<ResourceTag>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    public List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
        try {
            return jdbc.query("select " + resourceTagMappingSelectQuery + " from tags_resources join " +
                    "res_tags on tags_resources.tag_id=res_tags.id " +
                    "where tags_resources.resource_id=?",
                    RESOURCE_TAG_MAPPER, resource.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<ResourceTag>();
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    protected void initialize() {
    }
}