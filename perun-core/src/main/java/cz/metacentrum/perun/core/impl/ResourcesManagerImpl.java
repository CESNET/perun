package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import cz.metacentrum.perun.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
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
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
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

	protected final static String banOnResourceMappingSelectQuery = "resources_bans.id as res_bans_id, resources_bans.description as res_bans_description, " +
		"resources_bans.member_id as res_bans_member_id, resources_bans.resource_id as res_bans_resource_id, resources_bans.banned_to as res_bans_validity_to, " +
		"resources_bans.created_at as res_bans_created_at, resources_bans.created_by as res_bans_created_by, resources_bans.modified_at as res_bans_modified_at, " +
		"resources_bans.modified_by as res_bans_modified_by, resources_bans.created_by_uid as res_bans_created_by_uid, resources_bans.modified_by_uid as res_bans_modified_by_uid";

	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


	protected static final RowMapper<Resource> RESOURCE_MAPPER = new RowMapper<Resource>() {
		@Override
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
		@Override
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
		@Override
		public RichResource mapRow(ResultSet rs, int i) throws SQLException {
			RichResource richResource = new RichResource(RESOURCE_MAPPER.mapRow(rs, i));
			richResource.setVo(VosManagerImpl.VO_MAPPER.mapRow(rs, i));
			richResource.setFacility(FacilitiesManagerImpl.FACILITY_MAPPER.mapRow(rs, i));
			return richResource;
		}
	};

	protected static final RowMapper<BanOnResource> BAN_ON_RESOURCE_MAPPER = new RowMapper<BanOnResource>() {
		@Override
		public BanOnResource mapRow(ResultSet rs, int i) throws SQLException {
			BanOnResource banOnResource = new BanOnResource();
			banOnResource.setId(rs.getInt("res_bans_id"));
			banOnResource.setMemberId(rs.getInt("res_bans_member_id"));
			banOnResource.setResourceId(rs.getInt("res_bans_resource_id"));
			banOnResource.setDescription(rs.getString("res_bans_description"));
			banOnResource.setValidityTo(rs.getTimestamp("res_bans_validity_to"));
			banOnResource.setCreatedAt(rs.getString("res_bans_created_at"));
			banOnResource.setCreatedBy(rs.getString("res_bans_created_by"));
			banOnResource.setModifiedAt(rs.getString("res_bans_modified_at"));
			banOnResource.setModifiedBy(rs.getString("res_bans_modified_by"));
			if(rs.getInt("res_bans_modified_by_uid") == 0) banOnResource.setModifiedByUid(null);
			else banOnResource.setModifiedByUid(rs.getInt("res_bans_modified_by_uid"));
			if(rs.getInt("res_bans_created_by_uid") == 0) banOnResource.setCreatedByUid(null);
			else banOnResource.setCreatedByUid(rs.getInt("res_bans_created_by_uid"));
			return banOnResource;
		}
	};

	protected static final RichResourceExtractor RICH_RESOURCE_WITH_TAGS_EXTRACTOR = new RichResourceExtractor();

	private static class RichResourceExtractor implements ResultSetExtractor<List<RichResource>> {

		@Override
		public List<RichResource> extractData(ResultSet rs) throws SQLException, DataAccessException {
			Map<Integer, RichResource> map = new HashMap<Integer, RichResource>();
			RichResource myObject;
			while (rs.next()) {
				// fetch from map by ID
				Integer id = rs.getInt("resources_id");
				myObject = map.get(id);
				if(myObject == null){
					// if not preset, put in map
					myObject = RICH_RESOURCE_MAPPER.mapRow(rs, rs.getRow());
					map.put(id, myObject);
				}
				// fetch each resource tag and add it to rich resource
				ResourceTag tag = RESOURCE_TAG_MAPPER.mapRow(rs, rs.getRow());
				if (tag.getId() != 0) {
					// add only if exists
					myObject.addResourceTag(tag);
				}
			}
			return new ArrayList<RichResource>(map.values());
		}
	}

	public ResourcesManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);

		// Initialize resources manager
		this.initialize();
	}

	@Override
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
			return (RichResource) jdbc.queryForObject("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", " + resourceTagMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id "
					+ "join facilities on resources.facility_id=facilities.id left outer join tags_resources on resources.id=tags_resources.resource_id left outer join res_tags on tags_resources.tag_id=res_tags.id where resources.id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new ResourceNotExistsException("Resource with ID="+id+" not exists");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
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

	@Override
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

	@Override
	public void deleteResource(PerunSession sess, Vo vo, Resource resource) throws InternalErrorException, ResourceAlreadyRemovedException {
		try {
			// Delete authz entries for this resource
			AuthzResolverBlImpl.removeAllAuthzForResource(sess, resource);

			int numAffected = jdbc.update("delete from resources where id=?", resource.getId());
			if(numAffected == 0) throw new ResourceAlreadyRemovedException("Resource: " + resource + " , Vo: " + vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
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

	@Override
	public void setFacility(PerunSession sess, Resource resource, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("update resources set facility_id=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", facility.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), resource.getId());
			resource.setFacilityId(facility.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean resourceExists(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from resources where id=?", resource.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
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

	@Override
	public void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException {
		if(!this.resourceTagExists(sess, resourceTag)) throw new ResourceTagNotExistsException("resource: " + resourceTag);
	}

	@Override
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

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException {
		try  {
			return jdbc.query("select distinct " + UsersManagerImpl.userMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id join users on " +
					" users.id=members.user_id where groups_resources.resource_id=? and members.status!=? and members.status!=?", UsersManagerImpl.USER_MAPPER, resource.getId(),
					String.valueOf(Status.INVALID.getCode()), String.valueOf(Status.DISABLED.getCode()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources left outer join facilities on resources.facility_id=facilities.id" +
					" left outer join groups_resources on groups_resources.resource_id=resources.id" +
					" left outer join groups_members on groups_members.group_id=groups_resources.group_id" +
					" left outer join members on members.id=groups_members.member_id" +
					" where facilities.id=? and members.user_id=? and members.status!=?",RESOURCE_MAPPER, facility.getId(), user.getId(), String.valueOf(Status.INVALID.getCode()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Resource>();
		}	catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
		try  {
			return jdbc.query("select distinct " + MembersManagerImpl.memberMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
					" where groups_resources.resource_id=? and members.status!=? and members.status!=?", MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, resource.getId(),
					String.valueOf(Status.INVALID.getCode()), String.valueOf(Status.DISABLED.getCode()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Member>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException {
		try  {
			return jdbc.query("select distinct " + MembersManagerImpl.groupsMembersMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
					" where groups_resources.resource_id=?", MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, resource.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Member>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean isUserAssigned(PerunSession sess, User user, Resource resource) throws InternalErrorException {
		try {
			return (0 < jdbc.queryForInt("select count(*) from groups_resources join groups on groups_resources.group_id=groups.id" +
						" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id join users on " +
						" users.id=members.user_id where groups_resources.resource_id=? and users.id=?", resource.getId(), user.getId()));
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}


	@Override
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

	@Override
	public void removeGroupFromResource(PerunSession sess, Group group, Resource resource) throws InternalErrorException, GroupAlreadyRemovedFromResourceException {
		try {
			int numAffected = jdbc.update("delete from groups_resources where group_id=? and resource_id=?", group.getId(), resource.getId());
			if(numAffected == 0) throw new GroupAlreadyRemovedFromResourceException("Group: " + group + " , Resource: " + resource);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
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
	public List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException {
		try  {
			return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources join groups_resources on resources.id=groups_resources.resource_id " +
					" join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id" +
					" join resource_services on resource_services.resource_id=resources.id" +
					" where groups_members.member_id=? and resource_services.service_id=?", RESOURCE_MAPPER, member.getId(), service.getId());
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
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", "+resourceTagMappingSelectQuery+" from resources join vos on resources.vo_id=vos.id "
					+ "join facilities on resources.facility_id=facilities.id join groups_resources on "
					+ "resources.id=groups_resources.resource_id  left outer join tags_resources on resources.id=tags_resources.resource_id left outer join res_tags on tags_resources.tag_id=res_tags.id where groups_resources.group_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, group.getId());
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
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", "+resourceTagMappingSelectQuery+" from resources join vos on resources.vo_id=vos.id "
					+ "join facilities on resources.facility_id=facilities.id join groups_resources on "
					+ "resources.id=groups_resources.resource_id join groups on groups_resources.group_id=groups.id "
					+ "join groups_members on groups.id=groups_members.group_id  left outer join tags_resources on resources.id=tags_resources.resource_id left outer join res_tags on tags_resources.tag_id=res_tags.id where groups_members.member_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, member.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<RichResource>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException {
		try  {
			return jdbc.query("select " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", "+resourceTagMappingSelectQuery+" from resources join vos on resources.vo_id=vos.id "
					+ "join facilities on resources.facility_id=facilities.id "
					+ "join groups_resources on resources.id=groups_resources.resource_id "
					+ "join groups on groups_resources.group_id=groups.id "
					+ "join groups_members on groups.id=groups_members.group_id "
					+ "join resource_services on resource_services.resource_id=resources.id "
					+ "left outer join tags_resources on resources.id=tags_resources.resource_id "
					+ "left outer join res_tags on tags_resources.tag_id=res_tags.id "
					+ "where groups_members.member_id=? and resource_services.service_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, member.getId(), service.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<RichResource>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean isGroupAssigned(PerunSession sess, Group group, Resource resource) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select count(1) from groups_resources where group_id=? and resource_id=? ", group.getId(), resource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Integer> getAssignedServices(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select service_id as id from resource_services where resource_id=?", Utils.ID_MAPPER, resource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
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

	@Override
	public void removeService(PerunSession sess, Resource resource, Service service) throws InternalErrorException, ServiceNotAssignedException {
		try {
			if(0 == jdbc.update("delete from resource_services where service_id=? and resource_id=?", service.getId(), resource.getId())) {
				throw new ServiceNotAssignedException(service);
			}
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
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
	public List<Resource> getResources(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + resourceMappingSelectQuery+ " from resources", RESOURCE_MAPPER);
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
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", "+ resourceTagMappingSelectQuery +" from resources join vos on resources.vo_id=vos.id "
					+ "join facilities on resources.facility_id=facilities.id left outer join tags_resources on resources.id=tags_resources.resource_id left outer join res_tags on tags_resources.tag_id=res_tags.id where resources.vo_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, vo.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<RichResource>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getResourcesByIds(PerunSession sess, List<Integer> resourcesIds) throws InternalErrorException {
		if (resourcesIds.size() == 0) {
			return new ArrayList<Resource>();
		}

		try {
			return this.namedParameterJdbcTemplate.query("select " + resourceMappingSelectQuery + "  from resources where " +
					BeansUtils.prepareInSQLClause(resourcesIds, "resources.id"),
					RESOURCE_MAPPER);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
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

	@Override
	public int getResourcesCount(PerunSession perunSession, Vo vo) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from resources where resources.vo_id=?",
					vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException {
		try {
			jdbc.update("delete from res_tags where id=?", resourceTag.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException {
		try {
			jdbc.update("delete from res_tags where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("insert into tags_resources(tag_id, resource_id) values(?,?)", resourceTag.getId(),resource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from tags_resources where tag_id=? and resource_id=?", resourceTag.getId(), resource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from tags_resources where resource_id=?", resource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public int getResourcesCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from resources");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<User> getAdmins(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			Set<User> setOfAdmins = new HashSet<>();
			// Direct admins
			setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id" +
							"  where authz.resource_id=? and authz.role_id=(select id from roles where name=?)",
					UsersManagerImpl.USER_MAPPER, resource.getId(), Role.RESOURCEADMIN.getRoleName()));

			// Admins through a group
			List<Group> listOfGroupAdmins = getAdminGroups(sess, resource);
			for(Group authorizedGroup : listOfGroupAdmins) {
				setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users join members on users.id=members.user_id " +
						"join groups_members on groups_members.member_id=members.id where groups_members.group_id=?", UsersManagerImpl.USER_MAPPER, authorizedGroup.getId()));
			}

			return new ArrayList(setOfAdmins);

		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getDirectAdmins(PerunSession perunSession, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id" +
							"  where authz.resource_id=? and authz.role_id=(select id from roles where name=?)",
					UsersManagerImpl.USER_MAPPER, resource.getId(), Role.RESOURCEADMIN.getRoleName());

		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on authz.authorized_group_id=groups.id" +
							" where authz.resource_id=? and authz.role_id=(select id from roles where name=?)",
					GroupsManagerImpl.GROUP_MAPPER, resource.getId(), Role.RESOURCEADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + resourceMappingSelectQuery + " from resources " +
							" left outer join authz on authz.resource_id=resources.id " +
							" left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
							" left outer join members on members.id=groups_members.member_id " +
							" where (authz.user_id=? or members.user_id=?) and authz.role_id=(select id from roles where name=?) ",
					RESOURCE_MAPPER, user.getId(), user.getId(), Role.RESOURCEADMIN.getRoleName());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources " +
							" left outer join authz on authz.resource_id=resources.id " +
							" left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
							" left outer join members on members.id=groups_members.member_id " +
							" where resources.facility_id=? and resources.vo_id=? and (authz.user_id=? or members.user_id=?) " +
							" and authz.role_id=(select id from roles where name=?) "
					,RESOURCE_MAPPER, facility.getId(), vo.getId(), authorizedUser.getId(), authorizedUser.getId(), Role.RESOURCEADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources " +
							" left outer join authz on authz.resource_id=resources.id " +
							" where resources.facility_id=? and resources.vo_id=? and authz.authorized_group_id=? and authz.role_id=(select id from roles where name=?)"
					,RESOURCE_MAPPER, facility.getId(), vo.getId(), authorizedGroup.getId(), Role.RESOURCEADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean banExists(PerunSession sess, int memberId, int resourceId) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from resources_bans where member_id=? and resource_id=?", memberId, resourceId);
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean banExists(PerunSession sess, int banId) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from resources_bans where id=?", banId);
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException {
		Utils.notNull(banOnResource.getValidityTo(), "banOnResource.getValidityTo");

		try {
			int newId = Utils.getNewId(jdbc, "resources_bans_id_seq");

			jdbc.update("insert into resources_bans(id, description, banned_to, member_id, resource_id, created_by, created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					newId, banOnResource.getDescription(), Compatibility.getDate(banOnResource.getValidityTo().getTime()), banOnResource.getMemberId(), banOnResource.getResourceId(), sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			banOnResource.setId(newId);

			return banOnResource;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnResource getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnResourceMappingSelectQuery + " from resources_bans where id=? ", BAN_ON_RESOURCE_MAPPER, banId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban with id " + banId + " not exists for any facility.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnResourceMappingSelectQuery + " from resources_bans where member_id=? and resource_id=?", BAN_ON_RESOURCE_MAPPER, memberId, resourceId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban for user " + memberId + " and resource " + resourceId + " not exists.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnResourceMappingSelectQuery + " from resources_bans where member_id=?", BAN_ON_RESOURCE_MAPPER, memberId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnResourceMappingSelectQuery + " from resources_bans where resource_id=?", BAN_ON_RESOURCE_MAPPER, resourceId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnResource> getAllExpiredBansOnResources(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnResourceMappingSelectQuery + " from resources_bans where banned_to < " + Compatibility.getSysdate(), BAN_ON_RESOURCE_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException {
		try {
			jdbc.update("update resources_bans set description=?, banned_to=?, modified_by=?, modified_by_uid=?, modified_at=" +
							Compatibility.getSysdate() + " where id=?",
							banOnResource.getDescription(), Compatibility.getDate(banOnResource.getValidityTo().getTime()), sess.getPerunPrincipal().getActor(),
							sess.getPerunPrincipal().getUserId(), banOnResource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return banOnResource;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		try {
			int numAffected = jdbc.update("delete from resources_bans where id=?", banId);
			if(numAffected != 1) throw new BanNotExistsException("Ban with id " + banId + " can't be remove, because not exists yet.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException {
		try {
			int numAffected = jdbc.update("delete from resources_bans where member_id=? and resource_id=?", memberId, resourceId);
			if(numAffected != 1) throw new BanNotExistsException("Ban for member " + memberId + " and resource " + resourceId + " can't be remove, because not exists yet.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	protected void initialize() {
	}
}
