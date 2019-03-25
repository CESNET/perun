package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class FacilitiesManagerImpl implements FacilitiesManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerImpl.class);

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcPerunTemplate jdbc;

	// Part of the SQL script used for getting the Facility object
	public final static String facilityMappingSelectQuery = " facilities.id as facilities_id, facilities.name as facilities_name, facilities.dsc as facilities_dsc, "
		+ "facilities.created_at as facilities_created_at, facilities.created_by as facilities_created_by, facilities.modified_at as facilities_modified_at, facilities.modified_by as facilities_modified_by, " +
		"facilities.created_by_uid as facilities_created_by_uid, facilities.modified_by_uid as facilities_modified_by_uid";

	protected final static String hostMappingSelectQuery = "hosts.id as hosts_id, hosts.hostname as hosts_hostname, " +
		"hosts.created_at as hosts_created_at, hosts.created_by as hosts_created_by, hosts.modified_by as hosts_modified_by, hosts.modified_at as hosts_modified_at, " +
		"hosts.created_by_uid as hosts_created_by_uid, hosts.modified_by_uid as hosts_modified_by_uid";

	protected final static String facilityContactsMappingSelectQuery = " facility_contacts.facility_id as fc_id, facility_contacts.name as fc_name, " +
	    "facility_contacts.owner_id as fc_owner_id, facility_contacts.user_id as fc_user_id, facility_contacts.group_id as fc_group_id";

	protected final static String facilityContactsMappingSelectQueryWithAllEntities = facilityContactsMappingSelectQuery + ", " + UsersManagerImpl.userMappingSelectQuery + ", " +
	  facilityMappingSelectQuery + ", " + OwnersManagerImpl.ownerMappingSelectQuery + ", " + GroupsManagerImpl.groupMappingSelectQuery;

	protected final static String banOnFacilityMappingSelectQuery = "facilities_bans.id as fac_bans_id, facilities_bans.description as fac_bans_description, " +
		"facilities_bans.user_id as fac_bans_user_id, facilities_bans.facility_id as fac_bans_facility_id, facilities_bans.banned_to as fac_bans_validity_to, " +
		"facilities_bans.created_at as fac_bans_created_at, facilities_bans.created_by as fac_bans_created_by, facilities_bans.modified_at as fac_bans_modified_at, " +
		"facilities_bans.modified_by as fac_bans_modified_by, facilities_bans.created_by_uid as fac_bans_created_by_uid, facilities_bans.modified_by_uid as fac_bans_modified_by_uid";

	public static final RowMapper<Facility> FACILITY_MAPPER = (resultSet, i) -> {
		Facility facility = new Facility();

		facility.setId(resultSet.getInt("facilities_id"));
		facility.setName(resultSet.getString("facilities_name"));
		facility.setDescription(resultSet.getString("facilities_dsc"));
		facility.setCreatedAt(resultSet.getString("facilities_created_at"));
		facility.setCreatedBy(resultSet.getString("facilities_created_by"));
		facility.setModifiedAt(resultSet.getString("facilities_modified_at"));
		facility.setModifiedBy(resultSet.getString("facilities_modified_by"));
		if(resultSet.getInt("facilities_modified_by_uid") == 0) facility.setModifiedByUid(null);
		else facility.setModifiedByUid(resultSet.getInt("facilities_modified_by_uid"));
		if(resultSet.getInt("facilities_created_by_uid") == 0) facility.setCreatedByUid(null);
		else facility.setCreatedByUid(resultSet.getInt("facilities_created_by_uid"));
		return facility;
	};

	//Host mapper
	private static final RowMapper<Host> HOST_MAPPER = (resultSet, i) -> {
		Host h = new Host();
		h.setId(resultSet.getInt("hosts_id"));
		h.setHostname(resultSet.getString("hosts_hostname"));
		h.setCreatedAt(resultSet.getString("hosts_created_at"));
		h.setCreatedBy(resultSet.getString("hosts_created_by"));
		h.setModifiedAt(resultSet.getString("hosts_modified_at"));
		h.setModifiedBy(resultSet.getString("hosts_modified_by"));
		if(resultSet.getInt("hosts_modified_by_uid") == 0) h.setModifiedByUid(null);
		else h.setModifiedByUid(resultSet.getInt("hosts_modified_by_uid"));
		if(resultSet.getInt("hosts_created_by_uid") == 0) h.setCreatedByUid(null);
		else h.setCreatedByUid(resultSet.getInt("hosts_created_by_uid"));
		return h;
	};

	//Facility Contact Mapper
	private static final RowMapper<ContactGroup> FACILITY_CONTACT_MAPPER = (resultSet, i) -> {
		ContactGroup contactGroup = new ContactGroup();

		//set Facility
		Facility facility = new Facility();
		facility.setId(resultSet.getInt("facilities_id"));
		facility.setName(resultSet.getString("facilities_name"));
		facility.setDescription(resultSet.getString("facilities_dsc"));
		facility.setCreatedAt(resultSet.getString("facilities_created_at"));
		facility.setCreatedBy(resultSet.getString("facilities_created_by"));
		facility.setModifiedAt(resultSet.getString("facilities_modified_at"));
		facility.setModifiedBy(resultSet.getString("facilities_modified_by"));
		if(resultSet.getInt("facilities_modified_by_uid") == 0) facility.setModifiedByUid(null);
		else facility.setModifiedByUid(resultSet.getInt("facilities_modified_by_uid"));
		if(resultSet.getInt("facilities_created_by_uid") == 0) facility.setCreatedByUid(null);
		else facility.setCreatedByUid(resultSet.getInt("facilities_created_by_uid"));
		contactGroup.setFacility(facility);

		//set Name
		String name = resultSet.getString("fc_name");
		contactGroup.setName(name);

		//if exists set owner
		List<Owner> owners = new ArrayList<>();
		if(resultSet.getInt("owners_id") != 0) {
			Owner owner = new Owner();
			owner.setId(resultSet.getInt("owners_id"));
			owner.setName(resultSet.getString("owners_name"));
			owner.setContact(resultSet.getString("owners_contact"));
			owner.setTypeByString(resultSet.getString("owners_type"));
			owner.setCreatedAt(resultSet.getString("owners_created_at"));
			owner.setCreatedBy(resultSet.getString("owners_created_by"));
			owner.setModifiedAt(resultSet.getString("owners_modified_at"));
			owner.setModifiedBy(resultSet.getString("owners_modified_by"));
			if(resultSet.getInt("owners_modified_by_uid") == 0) owner.setModifiedByUid(null);
			else owner.setModifiedByUid(resultSet.getInt("owners_modified_by_uid"));
			if(resultSet.getInt("owners_created_by_uid") == 0) owner.setCreatedByUid(null);
			else owner.setCreatedByUid(resultSet.getInt("owners_created_by_uid"));
			owners.add(owner);
		}
		contactGroup.setOwners(owners);

		//if exists set user
		List<RichUser> users = new ArrayList<>();
		if(resultSet.getInt("users_id") != 0) {
			RichUser user = new RichUser();
			user.setId(resultSet.getInt("users_id"));
			user.setFirstName(resultSet.getString("users_first_name"));
			user.setLastName(resultSet.getString("users_last_name"));
			user.setMiddleName(resultSet.getString("users_middle_name"));
			user.setTitleBefore(resultSet.getString("users_title_before"));
			user.setTitleAfter(resultSet.getString("users_title_after"));
			user.setCreatedAt(resultSet.getString("users_created_at"));
			user.setCreatedBy(resultSet.getString("users_created_by"));
			user.setModifiedAt(resultSet.getString("users_modified_at"));
			user.setModifiedBy(resultSet.getString("users_modified_by"));
			user.setServiceUser(resultSet.getBoolean("users_service_acc"));
			if(resultSet.getInt("users_created_by_uid") == 0) user.setCreatedByUid(null);
			else user.setCreatedByUid(resultSet.getInt("users_created_by_uid"));
			if(resultSet.getInt("users_modified_by_uid") == 0) user.setModifiedByUid(null);
			else user.setModifiedByUid(resultSet.getInt("users_modified_by_uid"));
			users.add(user);
		}
		contactGroup.setUsers(users);

		//if exists set group
		List<Group> groups = new ArrayList<>();
		if(resultSet.getInt("groups_id") != 0) {
			Group group = new Group();
			group.setId(resultSet.getInt("groups_id"));
		//ParentGroup with ID=0 is not supported
		if(resultSet.getInt("groups_parent_group_id") != 0) group.setParentGroupId(resultSet.getInt("groups_parent_group_id"));
		else group.setParentGroupId(null);
		group.setName(resultSet.getString("groups_name"));
		group.setShortName(group.getName().substring(group.getName().lastIndexOf(":") + 1));
		group.setDescription(resultSet.getString("groups_dsc"));
		group.setVoId(resultSet.getInt("groups_vo_id"));
		group.setCreatedAt(resultSet.getString("groups_created_at"));
		group.setCreatedBy(resultSet.getString("groups_created_by"));
		group.setModifiedAt(resultSet.getString("groups_modified_at"));
		group.setModifiedBy(resultSet.getString("groups_modified_by"));
		if(resultSet.getInt("groups_modified_by_uid") == 0) group.setModifiedByUid(null);
		else group.setModifiedByUid(resultSet.getInt("groups_modified_by_uid"));
		if(resultSet.getInt("groups_created_by_uid") == 0) group.setCreatedByUid(null);
		else group.setCreatedByUid(resultSet.getInt("groups_created_by_uid"));
			groups.add(group);
		}
		contactGroup.setGroups(groups);

		return contactGroup;
	};

	private static final RowMapper<String> FACILITY_CONTACT_NAMES_MAPPER = (resultSet, i) -> resultSet.getString("name");

	protected static final RowMapper<BanOnFacility> BAN_ON_FACILITY_MAPPER = (resultSet, i) -> {
		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setId(resultSet.getInt("fac_bans_id"));
		banOnFacility.setUserId(resultSet.getInt("fac_bans_user_id"));
		banOnFacility.setFacilityId(resultSet.getInt("fac_bans_facility_id"));
		banOnFacility.setDescription(resultSet.getString("fac_bans_description"));
		banOnFacility.setValidityTo(resultSet.getTimestamp("fac_bans_validity_to"));
		banOnFacility.setCreatedAt(resultSet.getString("fac_bans_created_at"));
		banOnFacility.setCreatedBy(resultSet.getString("fac_bans_created_by"));
		banOnFacility.setModifiedAt(resultSet.getString("fac_bans_modified_at"));
		banOnFacility.setModifiedBy(resultSet.getString("fac_bans_modified_by"));
		if(resultSet.getInt("fac_bans_modified_by_uid") == 0) banOnFacility.setModifiedByUid(null);
		else banOnFacility.setModifiedByUid(resultSet.getInt("fac_bans_modified_by_uid"));
		if(resultSet.getInt("fac_bans_created_by_uid") == 0) banOnFacility.setCreatedByUid(null);
		else banOnFacility.setCreatedByUid(resultSet.getInt("fac_bans_created_by_uid"));
		return banOnFacility;
	};

	public FacilitiesManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
	}

	@Override
	public Facility createFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		Utils.notNull(facility.getName(), "facility.getName()");

		try {
			int newId = Utils.getNewId(jdbc, "facilities_id_seq");

			jdbc.update("insert into facilities(id, name, dsc, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId,
					facility.getName(), facility.getDescription(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			facility.setId(newId);

			log.info("Facility {} created", facility);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return facility;
	}

	@Override
	public void deleteFacility(PerunSession sess, Facility facility) throws InternalErrorException, FacilityAlreadyRemovedException {
		try {
			// Delete authz entries for this facility
			AuthzResolverBlImpl.removeAllAuthzForFacility(sess, facility);

			// Delete user-facility attributes - members are already deleted because all resources were removed
			jdbc.update("delete from user_facility_attr_values where facility_id=?", facility.getId());

			// Finally remove facility
			int numAffected = jdbc.update("delete from facilities where id=?", facility.getId());
			if(numAffected != 1) throw new FacilityAlreadyRemovedException("Facility: " + facility);
			log.info("Facility {} deleted", facility);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Facility updateFacility(PerunSession sess, Facility facility) throws InternalErrorException {

		// Get the facility stored in the DB
		Facility dbFacility;
		try {
			dbFacility = this.getFacilityById(sess, facility.getId());
		} catch (FacilityNotExistsException e) {
			throw new InternalErrorException("Facility existence was checked at the higher level",e);
		}

		if ((!dbFacility.getName().equals(facility.getName())) ||
				((dbFacility.getDescription() != null && !dbFacility.getDescription().equals(facility.getDescription())) ||
						(dbFacility.getDescription() == null && facility.getDescription() != null))) {
			try {
				jdbc.update("update facilities set name=?, dsc=?, modified_by=?, modified_by_uid=?, modified_at=" +
								Compatibility.getSysdate() + " where id=?",
						facility.getName(), facility.getDescription(), sess.getPerunPrincipal().getActor(),
						sess.getPerunPrincipal().getUserId(), facility.getId());
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		return facility;

	}

	@Override
	public void deleteFacilityOwners(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_owners where facility_id=?", facility.getId());
			log.info("Facility owners deleted. Facility: {}", facility);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Facility getFacilityById(PerunSession sess, int id) throws InternalErrorException, FacilityNotExistsException {
		try {
			return jdbc.queryForObject("select " + facilityMappingSelectQuery + " from facilities where id=?", FACILITY_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			Facility fac = new Facility();
			fac.setId(id);
			throw new FacilityNotExistsException(fac);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Facility getFacilityByName(PerunSession sess, String name) throws InternalErrorException, FacilityNotExistsException {
		try {
			return jdbc.queryForObject("select " + facilityMappingSelectQuery + " from facilities where name=?", FACILITY_MAPPER, name);
		} catch (EmptyResultDataAccessException ex) {
			Facility fac = new Facility();
			fac.setName(name);
			throw new FacilityNotExistsException(fac);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws InternalErrorException, FacilityNotExistsException {
		try {
			return jdbc.query("select distinct " + facilityMappingSelectQuery + " from facilities, destinations, facility_service_destinations " +
					" where destinations.destination=? and facility_service_destinations.destination_id=destinations.id and " +
					"facility_service_destinations.facility_id=facilities.id", FACILITY_MAPPER, destination);
		} catch (EmptyResultDataAccessException ex) {
			throw new FacilityNotExistsException("Any facility exists for destination: " + destination);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	@Override
	public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities", FACILITY_MAPPER);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int getFacilitiesCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from facilities");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Owner> getOwners(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select "+OwnersManagerImpl.ownerMappingSelectQuery+" from owners left join facility_owners on owners.id = facility_owners.owner_id where facility_id=?", OwnersManagerImpl.OWNER_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_owners where facility_id=?", facility.getId());

			for(Owner owner : owners) {
				jdbc.update("insert into facility_owners(facility_id, owner_id,created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
						"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", facility.getId(), owner.getId(),
						sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void addOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyAssignedException {
		try {
			// Check if the owner is already assigned
			int numberOfExistences = jdbc.queryForInt("select count(1) from facility_owners where facility_id=? and owner_id=?", facility.getId(), owner.getId());
			if (numberOfExistences >= 1) {
				throw new OwnerAlreadyAssignedException("Owner: " + owner + " facility: " + facility);
			} else {
				jdbc.update("insert into facility_owners(facility_id, owner_id,created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
						"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", facility.getId(), owner.getId(),
						sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
				log.info("Owner was assigned to facility. Owner:{} facility:{}", owner, facility);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException {
		try {
			if (0 == jdbc.update("delete from facility_owners where facility_id=? and owner_id=?", facility.getId(), owner.getId())) {
				throw new OwnerAlreadyRemovedException("Owner: " + owner + " facility: " + facility);
			}
			log.info("Owner was removed from facility owners. Owner: {} facility:{}", owner, facility);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			// Select only unique Vos
			return jdbc.query("select distinct "+ VosManagerImpl.voMappingSelectQuery +" from resources join vos on resources.vo_id=vos.id " +
				"where resources.facility_id=?", VosManagerImpl.VO_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Facility facility) throws InternalErrorException {
		try  {
			return jdbc.query("select distinct " + MembersManagerImpl.groupsMembersMappingSelectQuery + " from groups_resources join groups on groups_resources.group_id=groups.id" +
							" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
							" join resources on groups_resources.resource_id=resources.id " +
							" where resources.facility_id=? and members.status!=? and members.status!=?", MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, facility.getId(),
					String.valueOf(Status.INVALID.getCode()), String.valueOf(Status.DISABLED.getCode()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources where facility_id=?",
					ResourcesManagerImpl.RESOURCE_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {

		try {

			if (specificVo != null && specificService != null) {

				return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resource_services join resources on " +
						"resource_services.resource_id=resources.id where facility_id=? and vo_id=? and service_id=?",
						ResourcesManagerImpl.RESOURCE_MAPPER, facility.getId(), specificVo.getId(), specificService.getId());

			} else if (specificVo != null) {

				return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources where facility_id=? and vo_id=?",
						ResourcesManagerImpl.RESOURCE_MAPPER, facility.getId(), specificVo.getId());

			} else if (specificService != null) {

				return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resource_services join resources on " +
						"resource_services.resource_id=resources.id where facility_id=? and service_id=?",
						ResourcesManagerImpl.RESOURCE_MAPPER, facility.getId(), specificService.getId());

			} else {

				return getAssignedResources(sess, facility);

			}

		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", "
					+ facilityMappingSelectQuery + ", " + ResourcesManagerImpl.resourceTagMappingSelectQuery + " from resources join vos on resources.vo_id=vos.id join facilities on "
					+ "resources.facility_id=facilities.id  left outer join tags_resources on resources.id=tags_resources.resource_id left outer join res_tags on tags_resources.tag_id=res_tags.id where resources.facility_id=?", ResourcesManagerImpl.RICH_RESOURCE_WITH_TAGS_EXTRACTOR, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities join facility_owners on facilities.id=facility_owners.facility_id " +
					"where facility_owners.owner_id=?", FACILITY_MAPPER, owner.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities " +
					"join facility_attr_values on facilities.id=facility_attr_values.facility_id " +
					"where facility_attr_values.attr_id=? and facility_attr_values.attr_value=?",
					FACILITY_MAPPER, attribute.getId(), BeansUtils.attributeValueToString(attribute));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean facilityExists(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facilities where id=?", facility.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Facility " + facility + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see FacilitiesManagerImplApi#checkFacilityExists(PerunSession,Facility)
	 */
	@Override
	public void checkFacilityExists(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException {
		if(!facilityExists(sess, facility)) throw new FacilityNotExistsException("Facility: " + facility);
	}

	@Override
	public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			// direct admins
			Set<User> setOfAdmins = new HashSet<>(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id" +
					"  where authz.facility_id=? and authz.role_id=(select id from roles where name=?)",
				UsersManagerImpl.USER_MAPPER, facility.getId(), Role.FACILITYADMIN.getRoleName()));

			// admins through a group
			List<Group> listOfGroupAdmins = getAdminGroups(sess, facility);
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
	public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id" +
					"  where authz.facility_id=? and authz.role_id=(select id from roles where name=?)",
					UsersManagerImpl.USER_MAPPER, facility.getId(), Role.FACILITYADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on authz.authorized_group_id=groups.id" +
					" where authz.facility_id=? and authz.role_id=(select id from roles where name=?)",
					GroupsManagerImpl.GROUP_MAPPER, facility.getId(), Role.FACILITYADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Host addHost(PerunSession sess, Host host, Facility facility) throws InternalErrorException {
		Utils.notNull(host.getHostname(), "host.getHostname()");

		try {
			// Store the host into the DB
			int newId = Utils.getNewId(jdbc, "hosts_id_seq");

			jdbc.update("insert into hosts (id, hostname, facility_id, created_by, created_at, modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					newId, host.getHostname(), facility.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			host.setId(newId);

			return host;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeHost(PerunSession sess, Host host) throws InternalErrorException, HostAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from hosts where id=?", host.getId());
			if(numAffected == 0) throw new HostAlreadyRemovedException("Host: " + host);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + hostMappingSelectQuery + " from hosts where id=?", HOST_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			throw new HostNotExistsException("Host cannot be found");
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Host> getHostsByHostname(PerunSession sess, String hostname) throws InternalErrorException {
		try {
			return jdbc.query("select " + hostMappingSelectQuery + " from hosts where hosts.hostname=? order by id", HOST_MAPPER, hostname);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException {
		try {
			return jdbc.queryForObject("select " + facilityMappingSelectQuery + " from facilities join hosts on hosts.facility_id=facilities.id " +
					"where hosts.id=?", FACILITY_MAPPER, host.getId());
		} catch (EmptyResultDataAccessException e) {
			throw new ConsistencyErrorException("Host doesn't have assigned any facility.");
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities join hosts on hosts.facility_id=facilities.id " +
					"where hosts.hostname=?", FACILITY_MAPPER, hostname);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Host> getHosts(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + hostMappingSelectQuery + " from hosts where hosts.facility_id=? order by id", HOST_MAPPER, facility.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int getHostsCount(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from hosts where hosts.facility_id=?", facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities " +
							" left outer join authz on authz.facility_id=facilities.id " +
							" left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
							" left outer join members on members.id=groups_members.member_id " +
							" where (authz.user_id=? or members.user_id=?) and authz.role_id=(select id from roles where name=?) ",
					FACILITY_MAPPER, user.getId(), user.getId(), Role.FACILITYADMIN.getRoleName());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean hostExists(PerunSession sess, Host host) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from hosts where id=?", host.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Host " + host + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException {
		if(!hostExists(sess, host)) throw new HostNotExistsException("Host: " + host);
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility)throws InternalErrorException{
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users "
					+ "inner join members on users.id = members.user_id "
					+ "inner join groups_members on members.id = groups_members.member_id "
					+ "inner join groups_resources on groups_members.group_id = groups_resources.group_id "
					+ "inner join resources on resources.id = groups_resources.resource_id and resources.facility_id=? ",
					UsersManagerImpl.USER_MAPPER, facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service)throws InternalErrorException{
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users "
					+ "inner join members on users.id = members.user_id "
					+ "inner join groups_members on members.id = groups_members.member_id "
					+ "inner join groups_resources on groups_members.group_id = groups_resources.group_id "
					+ "inner join resources on resources.id = groups_resources.resource_id and resources.facility_id=? "
					+ "inner join resource_services on resources.id=resource_services.resource_id and resource_services.service_id=? ",
					UsersManagerImpl.USER_MAPPER, facility.getId(),service.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	// FACILITY CONTACTS METHODS

	@Override
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(user, "user");
		if(name == null || name.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, name, user_id) " +
					"values (?,?,?)", facility.getId(), name, user.getId());
			RichUser ru = new RichUser(user, null);
			List<RichUser> rulist = new ArrayList<>();
			rulist.add(ru);
			contactGroup = new ContactGroup(name, facility, new ArrayList<>(), new ArrayList<>(), rulist);
			log.info("Facility contact {} created", contactGroup);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return contactGroup;
	}

	@Override
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(owner, "owner");
		if(name == null || name.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, name, owner_id) " +
					"values (?,?,?)", facility.getId(), name, owner.getId());
			List<Owner> ownlist = new ArrayList<>();
			ownlist.add(owner);
			contactGroup = new ContactGroup(name, facility, new ArrayList<>(), ownlist, new ArrayList<>());
			log.info("Facility contact {} created", contactGroup);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return contactGroup;
	}

	@Override
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(group, "group");
		if(name == null || name.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, name, group_id) " +
					"values (?,?,?)", facility.getId(), name, group.getId());
			List<Group> grplist = new ArrayList<>();
			grplist.add(group);
			contactGroup = new ContactGroup(name, facility, grplist, new ArrayList<>(), new ArrayList<>());
			log.info("Facility contact {} created", contactGroup);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return contactGroup;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws InternalErrorException {
		try {
			return mergeContactGroups(jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.owner_id=?", FACILITY_CONTACT_MAPPER, owner.getId()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws InternalErrorException {
		try {
			return mergeContactGroups(jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.user_id=?", FACILITY_CONTACT_MAPPER, user.getId()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws InternalErrorException {
		try {
			return mergeContactGroups(jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.group_id=?", FACILITY_CONTACT_MAPPER, group.getId()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return mergeContactGroups(jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.facility_id=?", FACILITY_CONTACT_MAPPER, facility.getId()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String name) throws InternalErrorException, FacilityContactNotExistsException {
		try {
			List<ContactGroup> contactGroups = jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.facility_id=? and facility_contacts.name=?", FACILITY_CONTACT_MAPPER, facility.getId(), name);
			contactGroups = mergeContactGroups(contactGroups);
			if(contactGroups.size() == 1) {
				return contactGroups.get(0);
			} else {
				throw new InternalErrorException("Merging group contacts for facility " + facility + " and contact name " + name + " failed, more than 1 object returned " + name);
			}
		} catch (EmptyResultDataAccessException ex) {
			throw new FacilityContactNotExistsException(facility, name);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select distinct name from facility_contacts", FACILITY_CONTACT_NAMES_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, name, owner)) throw new FacilityContactNotExistsException(facility, name, owner);
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, name, user)) throw new FacilityContactNotExistsException(facility, name, user);
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, name, group)) throw new FacilityContactNotExistsException(facility, name, group);
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facility_contacts where facility_id=? and name=? and owner_id=?", facility.getId(), name, owner.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Facility contact of " + facility + " and " + name + " and " + owner + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facility_contacts where facility_id=? and name=? and user_id=?", facility.getId(), name, user.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Facility contact of " + facility + " and " + name + " and " + user + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facility_contacts where facility_id=? and name=? and group_id=?", facility.getId(), name, group.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Facility contact of " + facility + " and " + name + " and " + group + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllOwnerContacts(PerunSession sess, Owner owner) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where owner_id=?", owner.getId());
			log.info("All owner's {} facilities contacts deleted.", owner);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllUserContacts(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where user_id=?", user.getId());
			log.info("All user's {} facilities contacts deleted.", user);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeAllGroupContacts(PerunSession sess, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where group_id=?", group.getId());
			log.info("All group's {} facilities contacts deleted.", group);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and owner_id=? and name=?", facility.getId(), owner.getId(), name);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, Owner: " + owner, facility, name);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and user_id=? and name=?", facility.getId(), user.getId(), name);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, User: " + user, facility, name);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and group_id=? and name=?", facility.getId(), group.getId(), name);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, Group: " + group, facility, name);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + SecurityTeamsManagerImpl.securityTeamMappingSelectQuery +
							" from security_teams inner join (" +
							"select security_teams_facilities.security_team_id from security_teams_facilities where facility_id=?" +
							") " + Compatibility.getAsAlias("assigned_ids")+ " ON security_teams.id=assigned_ids.security_team_id",
					SecurityTeamsManagerImpl.SECURITY_TEAM_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException {
		try {
			jdbc.update("insert into security_teams_facilities(security_team_id, facility_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
					"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					securityTeam.getId(), facility.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException {
		try {
			jdbc.update("delete from security_teams_facilities where security_team_id=? and facility_id=?",
					securityTeam.getId(), facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void checkSecurityTeamNotAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamAlreadyAssignedException {
		if (isSecurityTeamAssigned(sess, facility, securityTeam)) {
			throw new SecurityTeamAlreadyAssignedException(securityTeam);
		}
	}

	@Override
	public void checkSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotAssignedException {
		if (!isSecurityTeamAssigned(sess, facility, securityTeam)) {
			throw new SecurityTeamNotAssignedException(securityTeam);
		}
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery +
							" from facilities inner join (" +
							"select security_teams_facilities.facility_id from security_teams_facilities where security_team_id=?" +
							") " + Compatibility.getAsAlias("assigned_ids")+ " ON facilities.id=assigned_ids.facility_id",
					FACILITY_MAPPER, securityTeam.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean banExists(PerunSession sess, int userId, int facilityId) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facilities_bans where user_id=? and facility_id=?", userId, facilityId);
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Ban of user with ID=" + userId + " on facility with ID=" + facilityId + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean banExists(PerunSession sess, int banId) throws InternalErrorException {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from facilities_bans where id=?", banId);
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Ban with ID=" + banId + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException {
		Utils.notNull(banOnFacility.getValidityTo(), "banOnFacility.getValidityTo");

		try {
			int newId = Utils.getNewId(jdbc, "facilities_bans_id_seq");

			jdbc.update("insert into facilities_bans(id, description, banned_to, user_id, facility_id, created_by, created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					newId, banOnFacility.getDescription(), Compatibility.getDate(banOnFacility.getValidityTo().getTime()), banOnFacility.getUserId(), banOnFacility.getFacilityId(), sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			banOnFacility.setId(newId);

			return banOnFacility;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnFacility getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnFacilityMappingSelectQuery + " from facilities_bans where id=? ", BAN_ON_FACILITY_MAPPER, banId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban with id " + banId + " not exists for any facility.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws InternalErrorException, BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnFacilityMappingSelectQuery + " from facilities_bans where user_id=? and facility_id=?", BAN_ON_FACILITY_MAPPER, userId, faclityId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban for user " + userId + " and facility " + faclityId + " not exists.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnFacilityMappingSelectQuery + " from facilities_bans where user_id=?", BAN_ON_FACILITY_MAPPER, userId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnFacilityMappingSelectQuery + " from facilities_bans where facility_id=?", BAN_ON_FACILITY_MAPPER, facilityId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnFacility> getAllExpiredBansOnFacilities(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + banOnFacilityMappingSelectQuery + " from facilities_bans where banned_to < " + Compatibility.getSysdate(), BAN_ON_FACILITY_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException {
		try {
			jdbc.update("update facilities_bans set description=?, banned_to=?, modified_by=?, modified_by_uid=?, modified_at=" +
							Compatibility.getSysdate() + " where id=?",
							banOnFacility.getDescription(), Compatibility.getDate(banOnFacility.getValidityTo().getTime()), sess.getPerunPrincipal().getActor(),
							sess.getPerunPrincipal().getUserId(), banOnFacility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return banOnFacility;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		try {
			int numAffected = jdbc.update("delete from facilities_bans where id=?", banId);
			if(numAffected != 1) throw new BanNotExistsException("Ban with id " + banId + " can't be remove, because not exists yet.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeBan(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException {
		try {
			int numAffected = jdbc.update("delete from facilities_bans where user_id=? and facility_id=?", userId, facilityId);
			if(numAffected != 1) throw new BanNotExistsException("Ban for user " + userId + " and facility " + facilityId + " can't be remove, because not exists yet.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private boolean isSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException {
		try {
			int number = jdbc.queryForInt("select count(1) from security_teams_facilities where security_team_id=? and facility_id=?", securityTeam.getId(), facility.getId());
			if (number == 1) {
				return true;
			} else if (number > 1) {
				throw new ConsistencyErrorException("Security team " + securityTeam + " is assigned multiple to facility " + facility);
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException | ConsistencyErrorException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Take list of contact groups and merged them.
	 *
	 * Merge means:
	 * If two groups are from the same facility with the same contactName join
	 * them to the one with groups, owners and users from both.
	 *
	 * @param notMergedContactGroups list of groups to merge
	 * @return list of merged contact groups
	 */
	private List<ContactGroup> mergeContactGroups(List<ContactGroup> notMergedContactGroups) {
		List<ContactGroup> mergedContactGroups = new ArrayList<>();
		while(!notMergedContactGroups.isEmpty()) {
			ContactGroup contactGroup = new ContactGroup();
			Iterator<ContactGroup> contactGroupIter = notMergedContactGroups.iterator();
			boolean first = true;
			while(contactGroupIter.hasNext()) {
				if(first) {
					contactGroup = contactGroupIter.next();
					if(contactGroup.getGroups() == null) contactGroup.setGroups(new ArrayList<>());
					if(contactGroup.getOwners() == null) contactGroup.setOwners(new ArrayList<>());
					if(contactGroup.getUsers() == null) contactGroup.setUsers(new ArrayList<>());
					first = false;
				} else {
					ContactGroup cp = contactGroupIter.next();
					//if same facility and same name merge them
					if(contactGroup.equalsGroup(cp)) {
						List<Group> groups = new ArrayList<>();
						groups.addAll(contactGroup.getGroups());
						groups.addAll(cp.getGroups());
						contactGroup.setGroups(groups);
						List<Owner> owners = new ArrayList<>();
						owners.addAll(contactGroup.getOwners());
						owners.addAll(cp.getOwners());
						contactGroup.setOwners(owners);
						List<RichUser> users = new ArrayList<>();
						users.addAll(contactGroup.getUsers());
						users.addAll(cp.getUsers());
						contactGroup.setUsers(users);
					// if not, skip this one for another round
					} else continue;
				}
				//remove used groups of contacts
				contactGroupIter.remove();
			}
			mergedContactGroups.add(contactGroup);
		}
		return mergedContactGroups;
	}

	@Override
	public void removeAllServiceDenials(int facilityId) throws InternalErrorException {
		try {
			jdbc.update("delete from service_denials where facility_id=?", facilityId);
		} catch (DataAccessException e) {
			throw new InternalErrorException(e);
		}
	}
}
