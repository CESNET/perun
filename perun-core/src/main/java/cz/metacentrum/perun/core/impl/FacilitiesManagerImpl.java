package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import java.util.HashSet;
import java.util.Iterator;
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

	protected final static String facilityContactsMappingSelectQuery = " facility_contacts.facility_id as facility_contacts_id, facility_contacts.contact_group_name as facility_contacts_contact_group_name, " +
	    "facility_contacts.owner_id as facility_contacts_owner_id, facility_contacts.user_id as facility_contacts_user_id, facility_contacts.group_id as facility_contacts_group_id";
	
	protected final static String facilityContactsMappingSelectQueryWithAllEntities = facilityContactsMappingSelectQuery + ", " + UsersManagerImpl.userMappingSelectQuery + ", " + 
	  facilityMappingSelectQuery + ", " + OwnersManagerImpl.ownerMappingSelectQuery + ", " + GroupsManagerImpl.groupMappingSelectQuery;

	public static final RowMapper<Facility> FACILITY_MAPPER = new RowMapper<Facility>() {
		public Facility mapRow(ResultSet rs, int i) throws SQLException {
			Facility facility = new Facility();

			facility.setId(rs.getInt("facilities_id"));
			facility.setName(rs.getString("facilities_name"));
			facility.setDescription(rs.getString("facilities_dsc"));
			facility.setCreatedAt(rs.getString("facilities_created_at"));
			facility.setCreatedBy(rs.getString("facilities_created_by"));
			facility.setModifiedAt(rs.getString("facilities_modified_at"));
			facility.setModifiedBy(rs.getString("facilities_modified_by"));
			if(rs.getInt("facilities_modified_by_uid") == 0) facility.setModifiedByUid(null);
			else facility.setModifiedByUid(rs.getInt("facilities_modified_by_uid"));
			if(rs.getInt("facilities_created_by_uid") == 0) facility.setCreatedByUid(null);
			else facility.setCreatedByUid(rs.getInt("facilities_created_by_uid"));
			return facility;
		}
	};

	//Host mapper
	private static final RowMapper<Host> HOST_MAPPER = new RowMapper<Host>() {
		public Host mapRow(ResultSet rs, int i) throws SQLException {
			Host h = new Host();
			h.setId(rs.getInt("hosts_id"));
			h.setHostname(rs.getString("hosts_hostname"));
			h.setCreatedAt(rs.getString("hosts_created_at"));
			h.setCreatedBy(rs.getString("hosts_created_by"));
			h.setModifiedAt(rs.getString("hosts_modified_at"));
			h.setModifiedBy(rs.getString("hosts_modified_by"));
			if(rs.getInt("hosts_modified_by_uid") == 0) h.setModifiedByUid(null);
			else h.setModifiedByUid(rs.getInt("hosts_modified_by_uid"));
			if(rs.getInt("hosts_created_by_uid") == 0) h.setCreatedByUid(null);
			else h.setCreatedByUid(rs.getInt("hosts_created_by_uid"));
			return h;
		}
	};

	//Facility Contact Mapper
	private static final RowMapper<ContactGroup> FACILITY_CONTACT_MAPPER = new RowMapper<ContactGroup>() {
		public ContactGroup mapRow(ResultSet rs, int i) throws SQLException {
			ContactGroup contactGroup = new ContactGroup();

			//set Facility
			Facility facility = new Facility();
			facility.setId(rs.getInt("facilities_id"));
			facility.setName(rs.getString("facilities_name"));
			facility.setDescription(rs.getString("facilities_dsc"));
			facility.setCreatedAt(rs.getString("facilities_created_at"));
			facility.setCreatedBy(rs.getString("facilities_created_by"));
			facility.setModifiedAt(rs.getString("facilities_modified_at"));
			facility.setModifiedBy(rs.getString("facilities_modified_by"));
			if(rs.getInt("facilities_modified_by_uid") == 0) facility.setModifiedByUid(null);
			else facility.setModifiedByUid(rs.getInt("facilities_modified_by_uid"));
			if(rs.getInt("facilities_created_by_uid") == 0) facility.setCreatedByUid(null);
			else facility.setCreatedByUid(rs.getInt("facilities_created_by_uid"));
			contactGroup.setFacility(facility);

			//set Name
			String contactGroupName = rs.getString("facility_contacts_contact_group_name");
			contactGroup.setContactGroupName(contactGroupName);

			//if exists set owner
			List<Owner> owners = new ArrayList<>();
			if(rs.getInt("owners_id") != 0) {
				Owner owner = new Owner();
				owner.setId(rs.getInt("owners_id"));
				owner.setName(rs.getString("owners_name"));
				owner.setContact(rs.getString("owners_contact"));
				owner.setTypeByString(rs.getString("owners_type"));
				owner.setCreatedAt(rs.getString("owners_created_at"));
				owner.setCreatedBy(rs.getString("owners_created_by"));
				owner.setModifiedAt(rs.getString("owners_modified_at"));
				owner.setModifiedBy(rs.getString("owners_modified_by"));
				if(rs.getInt("owners_modified_by_uid") == 0) owner.setModifiedByUid(null);
				else owner.setModifiedByUid(rs.getInt("owners_modified_by_uid"));
				if(rs.getInt("owners_created_by_uid") == 0) owner.setCreatedByUid(null);
				else owner.setCreatedByUid(rs.getInt("owners_created_by_uid"));
				owners.add(owner);
			}
			contactGroup.setOwners(owners);

			//if exists set user
			List<RichUser> users = new ArrayList<>();
			if(rs.getInt("users_id") != 0) {
				RichUser user = new RichUser();
				user.setId(rs.getInt("users_id"));
				user.setFirstName(rs.getString("users_first_name"));
				user.setLastName(rs.getString("users_last_name"));
				user.setMiddleName(rs.getString("users_middle_name"));
				user.setTitleBefore(rs.getString("users_title_before"));
				user.setTitleAfter(rs.getString("users_title_after"));
				user.setCreatedAt(rs.getString("users_created_at"));
				user.setCreatedBy(rs.getString("users_created_by"));
				user.setModifiedAt(rs.getString("users_modified_at"));
				user.setModifiedBy(rs.getString("users_modified_by"));
				user.setServiceUser(rs.getBoolean("users_service_acc"));
				if(rs.getInt("users_created_by_uid") == 0) user.setCreatedByUid(null);
				else user.setCreatedByUid(rs.getInt("users_created_by_uid"));
				if(rs.getInt("users_modified_by_uid") == 0) user.setModifiedByUid(null);
				else user.setModifiedByUid(rs.getInt("users_modified_by_uid"));
				users.add(user);
			}
			contactGroup.setUsers(users);

			//if exists set group
			List<Group> groups = new ArrayList<>();
			if(rs.getInt("groups_id") != 0) {
				Group group = new Group();
				group.setId(rs.getInt("groups_id"));
			//ParentGroup with ID=0 is not supported
			if(rs.getInt("groups_parent_group_id") != 0) group.setParentGroupId(rs.getInt("groups_parent_group_id"));
			else group.setParentGroupId(null);
			group.setName(rs.getString("groups_name"));
			group.setShortName(group.getName().substring(group.getName().lastIndexOf(":") + 1));
			group.setDescription(rs.getString("groups_dsc"));
			group.setVoId(rs.getInt("groups_vo_id"));
			group.setCreatedAt(rs.getString("groups_created_at"));
			group.setCreatedBy(rs.getString("groups_created_by"));
			group.setModifiedAt(rs.getString("groups_modified_at"));
			group.setModifiedBy(rs.getString("groups_modified_by"));
			if(rs.getInt("groups_modified_by_uid") == 0) group.setModifiedByUid(null);
			else group.setModifiedByUid(rs.getInt("groups_modified_by_uid"));
			if(rs.getInt("groups_created_by_uid") == 0) group.setCreatedByUid(null);
			else group.setCreatedByUid(rs.getInt("groups_created_by_uid"));
				groups.add(group);
			}
			contactGroup.setGroups(groups);

			return contactGroup;
		}
	};

	private static final RowMapper<String> FACILITY_CONTACT_NAMES_MAPPER = new RowMapper<String>() {
		public String mapRow(ResultSet rs, int i) throws SQLException {

			return rs.getString("contact_group_name");
		}
	};

	public FacilitiesManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
	}

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

	public void deleteFacilityOwners(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_owners where facility_id=?", facility.getId());
			log.info("Facility owners deleted. Facility: {}", facility);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

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


	public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities", FACILITY_MAPPER);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public int getFacilitiesCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from facilities");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Integer> getOwnersIds(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select id from owners, facility_owners where owners.id=owner_id and facility_id=?", Utils.ID_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

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

	public void addOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyAssignedException {
		try {
			try {
				// Check if the owner is already assigned
				jdbc.queryForInt("select 1 from facility_owners where facility_id=? and owner_id=?", facility.getId(), owner.getId());
				throw new OwnerAlreadyAssignedException("Owner: " + owner + " facility: " + facility);
			} catch (EmptyResultDataAccessException e) {
				jdbc.update("insert into facility_owners(facility_id, owner_id,created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
						"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", facility.getId(), owner.getId(),
						sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
				log.info("Owner was assigned to facility. Owner:{} facility:{}", owner, facility);
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

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

	public List<Integer> getAllowedVosIds(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			// Select only unique ids
			return jdbc.query("select distinct vos.id as id from resources, vos where resources.facility_id=? and " +
					"resources.vo_id=vos.id", Utils.ID_MAPPER, facility.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources where facility_id=?",
					ResourcesManagerImpl.RESOURCE_MAPPER, facility.getId());
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

	public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities join facility_owners on facilities.id=facility_owners.facility_id " +
					"where facility_owners.owner_id=?", FACILITY_MAPPER, owner.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities " +
					"join facility_attr_values on facilities.id=facility_attr_values.facility_id " +
					"where facility_attr_values.attr_id=? and facility_attr_values.attr_value=?",
					FACILITY_MAPPER, attribute.getId(), BeansUtils.attributeValueToString(attribute));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Facility>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean facilityExists(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from facilities where id=?", facility.getId());
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
	public void checkFacilityExists(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException {
		if(!facilityExists(sess, facility)) throw new FacilityNotExistsException("Facility: " + facility);
	}

	public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			Set<User> setOfAdmins = new HashSet<User>();
			// direct admins
			setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id" +
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
			return new ArrayList<User>();
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
			return new ArrayList<User>();
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
			return new ArrayList<Group>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

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

	public void removeHost(PerunSession sess, Host host) throws InternalErrorException, HostAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from hosts where id=?", host.getId());
			if(numAffected == 0) throw new HostAlreadyRemovedException("Host: " + host);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + hostMappingSelectQuery + " from hosts where id=?", HOST_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			throw new HostNotExistsException("Host cannot be found");
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Host> getHostsByHostname(PerunSession sess, String hostname) throws InternalErrorException {
		try {
			return jdbc.query("select " + hostMappingSelectQuery + " from hosts where hosts.hostname=? order by id", HOST_MAPPER, hostname);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Host>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

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

	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities join hosts on hosts.facility_id=facilities.id " +
					"where hosts.hostname=?", FACILITY_MAPPER, hostname);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Facility>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Host> getHosts(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + hostMappingSelectQuery + " from hosts where hosts.facility_id=? order by id", HOST_MAPPER, facility.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Host>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public int getHostsCount(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from hosts where hosts.facility_id=?", facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityMappingSelectQuery + " from facilities, authz where authz.user_id=? and " +
					"authz.role_id=(select id from roles where name=?) and authz.facility_id=facilities.id",
					FACILITY_MAPPER, user.getId(), Role.FACILITYADMIN.getRoleName());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean hostExists(PerunSession sess, Host host) throws InternalErrorException {
		try {
			return 1==jdbc.queryForInt("select 1 from hosts where id=?", host.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException {
		if(!hostExists(sess, host)) throw new HostNotExistsException("Host: " + host);
	}

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
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(user, "user");
		if(contactGroupName == null || contactGroupName.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, contact_group_name, user_id) " +
					"values (?,?,?)", facility.getId(), contactGroupName, user.getId());
			RichUser ru = new RichUser(user, null);
			List<RichUser> rulist = new ArrayList<>();
			rulist.add(ru);
			contactGroup = new ContactGroup(contactGroupName, facility, new ArrayList<Group>(), new ArrayList<Owner>(), rulist);
			log.info("Facility contact {} created", contactGroup);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return contactGroup;
	}

	@Override
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(owner, "owner");
		if(contactGroupName == null || contactGroupName.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, contact_group_name, owner_id) " +
					"values (?,?,?)", facility.getId(), contactGroupName, owner.getId());
			List<Owner> ownlist = new ArrayList<>();
			ownlist.add(owner);
			contactGroup = new ContactGroup(contactGroupName, facility, new ArrayList<Group>(), ownlist, new ArrayList<RichUser>());
			log.info("Facility contact {} created", contactGroup);

		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return contactGroup;
	}

	@Override
	public ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException {
		Utils.notNull(facility, "facility");
		Utils.notNull(group, "group");
		if(contactGroupName == null || contactGroupName.isEmpty()) {
			throw new InternalErrorException("ContactGroupName can't be null or empty.");
		}

		ContactGroup contactGroup;
		try {
			jdbc.update("insert into facility_contacts(facility_id, contact_group_name, group_id) " +
					"values (?,?,?)", facility.getId(), contactGroupName, group.getId());
			List<Group> grplist = new ArrayList<>();
			grplist.add(group);
			contactGroup = new ContactGroup(contactGroupName, facility, grplist, new ArrayList<Owner>(), new ArrayList<RichUser>());
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
	public ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String contactGroupName) throws InternalErrorException, FacilityContactNotExistsException {
		try {
			List<ContactGroup> contactGroups = jdbc.query("select " + facilityContactsMappingSelectQueryWithAllEntities + " from facility_contacts " +
			  "left join facilities on facilities.id=facility_contacts.facility_id " +
			  "left join owners on owners.id=facility_contacts.owner_id " +
			  "left join users on users.id=facility_contacts.user_id " +
			  "left join groups on groups.id=facility_contacts.group_id " +
			  "where facility_contacts.facility_id=? and facility_contacts.contact_group_name=?", FACILITY_CONTACT_MAPPER, facility.getId(), contactGroupName);
			contactGroups = mergeContactGroups(contactGroups);
			if(contactGroups.size() == 1) {
				return contactGroups.get(0);
			} else {
				throw new InternalErrorException("Merging group contacts for facility " + facility + " and contact name " + contactGroupName + " failed, more than 1 object returned " + contactGroupName);
			}
		} catch (EmptyResultDataAccessException ex) {
			throw new FacilityContactNotExistsException(facility, contactGroupName);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select distinct contact_group_name from facility_contacts", FACILITY_CONTACT_NAMES_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, contactGroupName, owner)) throw new FacilityContactNotExistsException(facility, contactGroupName, owner);
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, contactGroupName, user)) throw new FacilityContactNotExistsException(facility, contactGroupName, user);
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException, FacilityContactNotExistsException {
		if(!facilityContactExists(sess, facility, contactGroupName, group)) throw new FacilityContactNotExistsException(facility, contactGroupName, group);
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from facility_contacts where facility_id=? and contact_group_name=? and owner_id=?", facility.getId(), contactGroupName, owner.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from facility_contacts where facility_id=? and contact_group_name=? and user_id=?", facility.getId(), contactGroupName, user.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private boolean facilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from facility_contacts where facility_id=? and contact_group_name=? and group_id=?", facility.getId(), contactGroupName, group.getId());
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
	public void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and owner_id=? and contact_group_name=?", facility.getId(), owner.getId(), contactGroupName);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, Owner: " + owner, facility, contactGroupName);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and user_id=? and contact_group_name=?", facility.getId(), user.getId(), contactGroupName);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, User: " + user, facility, contactGroupName);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_contacts where facility_id=? and group_id=? and contact_group_name=?", facility.getId(), group.getId(), contactGroupName);
			log.info("Facility contact deleted. Facility: {}, ContactName: {}, Group: " + group, facility, contactGroupName);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
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
					if(contactGroup.getGroups() == null) contactGroup.setGroups(new ArrayList<Group>());
					if(contactGroup.getOwners() == null) contactGroup.setOwners(new ArrayList<Owner>());
					if(contactGroup.getUsers() == null) contactGroup.setUsers(new ArrayList<RichUser>());
					first = false;
				} else {
					ContactGroup cp = contactGroupIter.next();
					if(contactGroup.equalsGroup(cp)) {
						contactGroup.getGroups().addAll(cp.getGroups());
						contactGroup.getUsers().addAll(cp.getUsers());
						contactGroup.getOwners().addAll(cp.getOwners());
					}
				}
				contactGroupIter.remove();
			}
			mergedContactGroups.add(contactGroup);
		}
		return mergedContactGroups;
	}
}
