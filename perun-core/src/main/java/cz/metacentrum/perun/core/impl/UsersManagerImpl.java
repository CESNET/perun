package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * UsersManager implementation.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 */
public class UsersManagerImpl implements UsersManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(UsersManagerImpl.class);

	// time window size for mail validation if not taken from peruns configuration file
	private final static int VALIDATION_ALLOWED_HOURS = 6;

	// If user extSource is older than 'number' months, it is not defined as ACTIVE in methods
	// INACTIVE userExtSources are skipped in counting max loa for user
	private static final int MAX_OLD_OF_ACTIVE_USER_EXTSOURCE = 13;

	// Part of the SQL script used for getting the User object
	protected final static String userMappingSelectQuery = "users.id as users_id, users.first_name as users_first_name, users.last_name as users_last_name, " +
		"users.middle_name as users_middle_name, users.title_before as users_title_before, users.title_after as users_title_after, " +
		"users.created_at as users_created_at, users.created_by as users_created_by, users.modified_by as users_modified_by, users.modified_at as users_modified_at, " +
		"users.sponsored_acc as users_sponsored_acc, users.service_acc as users_service_acc, users.created_by_uid as users_created_by_uid, users.modified_by_uid as users_modified_by_uid";

	protected final static String userExtSourceMappingSelectQuery = "user_ext_sources.id as user_ext_sources_id, user_ext_sources.login_ext as user_ext_sources_login_ext, " +
		"user_ext_sources.user_id as user_ext_sources_user_id, user_ext_sources.loa as user_ext_sources_loa, user_ext_sources.created_at as user_ext_sources_created_at, user_ext_sources.created_by as user_ext_sources_created_by, " +
		"user_ext_sources.modified_by as user_ext_sources_modified_by, user_ext_sources.modified_at as user_ext_sources_modified_at, " +
		"user_ext_sources.created_by_uid as ues_created_by_uid, user_ext_sources.modified_by_uid as ues_modified_by_uid";

	private static Map<String, Pattern> userExtSourcePersistentPatterns;

	static {
		// Prepare userExtSourcePersistentPatterns for matching regex from perun property file.
		// It is done in advance because of performance.
		try {
			userExtSourcePersistentPatterns = new HashMap<>();
			String persistentConfig = BeansUtils.getPropertyFromConfiguration("perun.userExtSources.persistent");
			for (String extSource : persistentConfig.split(";")) {
				String[] extSourceTuple = extSource.split(",", 2);
				if (extSourceTuple.length > 1) {
					userExtSourcePersistentPatterns.put(extSourceTuple[0], Pattern.compile(extSourceTuple[1]));
				} else {
					userExtSourcePersistentPatterns.put(extSource, Pattern.compile(".*"));
				}
			}
		} catch (InternalErrorException e) {
			log.info("Error when reading property perun.userExtSources.persistent. Serving default behavior.", e);
			// If error occurred no persistent user ext sources are considered. e.g. property is not set.
			userExtSourcePersistentPatterns = new HashMap<>();
		}
	}

	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

	protected static final RowMapper<User> USER_MAPPER = new RowMapper<User>() {
		public User mapRow(ResultSet rs, int i) throws SQLException {
			return new User(rs.getInt("users_id"), rs.getString("users_first_name"), rs.getString("users_last_name"),
					rs.getString("users_middle_name"), rs.getString("users_title_before"), rs.getString("users_title_after"),
					rs.getString("users_created_at"), rs.getString("users_created_by"), rs.getString("users_modified_at"), rs.getString("users_modified_by"), rs.getBoolean("users_service_acc"),
					rs.getBoolean("users_sponsored_acc"),
					rs.getInt("users_created_by_uid") == 0 ? null : rs.getInt("users_created_by_uid"), rs.getInt("users_modified_by_uid") == 0 ? null : rs.getInt("users_modified_by_uid"));
		}
	};

	private static final RowMapper<UserExtSource> USEREXTSOURCE_MAPPER = new RowMapper<UserExtSource>() {
		public UserExtSource mapRow(ResultSet rs, int i) throws SQLException {
			ExtSource extSource = new ExtSource();
			extSource.setId(rs.getInt("ext_sources_id"));
			extSource.setName(rs.getString("ext_sources_name"));
			extSource.setType(rs.getString("ext_sources_type"));
			extSource.setCreatedAt(rs.getString("ext_sources_created_at"));
			extSource.setCreatedBy(rs.getString("ext_sources_created_by"));
			extSource.setModifiedAt(rs.getString("ext_sources_modified_at"));
			extSource.setModifiedBy(rs.getString("ext_sources_modified_by"));
			if(rs.getInt("ext_sources_modified_by_uid") == 0) extSource.setModifiedByUid(null);
			else extSource.setModifiedByUid(rs.getInt("ext_sources_modified_by_uid"));
			if(rs.getInt("ext_sources_created_by_uid") == 0) extSource.setCreatedByUid(null);
			else extSource.setCreatedByUid(rs.getInt("ext_sources_created_by_uid"));

			boolean persistent = false;
			Pattern p = userExtSourcePersistentPatterns.get(rs.getString("ext_sources_name"));
			if (p != null) {
				if (p.matcher(rs.getString("user_ext_sources_login_ext")).matches()) {
					persistent = true;
				}
			}

			return new UserExtSource(rs.getInt("user_ext_sources_id"), extSource, rs.getString("user_ext_sources_login_ext"),
					rs.getInt("user_ext_sources_user_id"), rs.getInt("user_ext_sources_loa"), persistent,
					rs.getString("user_ext_sources_created_at"), rs.getString("user_ext_sources_created_by"),
					rs.getString("user_ext_sources_modified_at"), rs.getString("user_ext_sources_modified_by"),
					rs.getInt("ues_created_by_uid") == 0 ? null : rs.getInt("ues_created_by_uid"),
					rs.getInt("ues_modified_by_uid") == 0 ? null : rs.getInt("ues_modified_by_uid"));
		}
	};

        protected static final ResultSetExtractor<List<Pair<User,String>>> USERBLACKLIST_EXTRACTOR = new ResultSetExtractor<List<Pair<User,String>>>(){
            @Override
            public List<Pair<User,String>> extractData(ResultSet rs) throws SQLException{
                List<Pair<User, String>> result = new ArrayList<>();

                int row = 0;
                while(rs.next()){
                    result.add(new Pair<User, String>(USER_MAPPER.mapRow(rs, row), rs.getString("description")));
                    row++;
                }

                return result;
            }
        };

	private static class AttributeAndUserRowMapper<User> implements RowMapper<Pair<User,Attribute>> {

		private final RowMapper<Attribute> attributeRowMapper;
		private final RowMapper<User> userRowMapper;

		public AttributeAndUserRowMapper(RowMapper<User> userRowMapper, RowMapper<Attribute> attributeRowMapper) {
			this.userRowMapper = userRowMapper;
			this.attributeRowMapper = attributeRowMapper;
		}

		public Pair<User, Attribute> mapRow(ResultSet rs, int i) throws SQLException {
			User user = userRowMapper.mapRow(rs, i);
			Attribute attribute = attributeRowMapper.mapRow(rs, i);
			return new Pair<User, Attribute>(user, attribute);
		}

	}



	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool
	 */
	public UsersManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
	}

	public List<Pair<User, Attribute>> getAllRichUsersWithAllNonVirutalAttributes(PerunSession sess) throws InternalErrorException {
		AttributeAndUserRowMapper<User> attributeAndUserRowMapper = new AttributeAndUserRowMapper<User>(USER_MAPPER, AttributesManagerImpl.ATTRIBUTE_MAPPER);
		try {
			return jdbc.query("select " + userMappingSelectQuery + ", " + AttributesManagerImpl.getAttributeMappingSelectQuery("usr") + " from users " +
					"left join user_attr_values usr on usr.user_id=users.id " +
					"left join attr_names on usr.attr_id=attr_names.id", attributeAndUserRowMapper);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<Pair<User,Attribute>>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public User getUserById(PerunSession sess, int id) throws InternalErrorException, UserNotExistsException {
		try {
			return jdbc.queryForObject("select " + userMappingSelectQuery + " from users where users.id=? ", USER_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new UserNotExistsException("user id=" + id);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException {
		try {
			return jdbc.queryForObject("select " + userMappingSelectQuery +
					" from users, user_ext_sources " +
					"where users.id=user_ext_sources.user_id and user_ext_sources.login_ext=? and user_ext_sources.ext_sources_id=? ", USER_MAPPER, userExtSource.getLogin(), userExtSource.getExtSource().getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new UserNotExistsException("userExtSource=" + userExtSource.toString());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users join user_ext_sources on users.id=user_ext_sources.user_id join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id"
					+ " where ext_sources.type=? and user_ext_sources.login_ext=?", USER_MAPPER, extSourceType, login);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<User>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public User getUserByMember(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return jdbc.queryForObject("select " + userMappingSelectQuery + " from users, members " +
					"where members.id=? and members.user_id=users.id", USER_MAPPER, member.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException("Member has to have a corresponding User", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<User> getUsersByVo(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users, members " +
					"where members.user_id=users.id and members.vo_id=?", USER_MAPPER, vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getUsers(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					"  from users", USER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getSpecificUsersByUser(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, specific_user_users where users.id=specific_user_users.specific_user_id and specific_user_users.status='0' and specific_user_users.user_id=?", USER_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, specific_user_users where users.id=specific_user_users.user_id and specific_user_users.status='0' and specific_user_users.specific_user_id=? " +
					" and specific_user_users.type=?", USER_MAPPER, specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, SpecificUserOwnerAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from specific_user_users where user_id=? and specific_user_id=? and specific_user_users.type=?",
					user.getId(), specificUser.getId(),specificUser.getMajorSpecificType().getSpecificUserType());
			if(numAffected == 0) throw new SpecificUserOwnerAlreadyRemovedException("SpecificUser-Owner: " + user + " , SpecificUser: " + specificUser);

		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		try {
			jdbc.update("insert into specific_user_users(user_id,specific_user_id,status,created_by_uid,modified_at,type) values (?,?,'0',?," + Compatibility.getSysdate() + ",?)",
					user.getId(), specificUser.getId(), sess.getPerunPrincipal().getUserId(), specificUser.getMajorSpecificType().getSpecificUserType());

		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void enableOwnership(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		try {
			jdbc.update("update specific_user_users set status='0', modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
					sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (RuntimeException er) {
			throw new InternalErrorException(er);
		}
	}

	public void disableOwnership(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		try {
			jdbc.update("update specific_user_users set status='1', modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
					sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (RuntimeException er) {
			throw new InternalErrorException(er);
		}
	}

	public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from specific_user_users where user_id=? and specific_user_id=? and type=?",
					user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					"  from users where users.service_acc='1' or users.sponsored_acc='1'", USER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteUser(PerunSession sess, User user) throws InternalErrorException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
		try {
			// delete all relations like  user -> sponsor -> service
			jdbc.update("delete from specific_user_users where specific_user_id=? or user_id=?", user.getId(), user.getId());
			int numAffected = jdbc.update("delete from users where id=?", user.getId());
			if(numAffected == 0) {
				if (user.isSpecificUser()) throw new SpecificUserAlreadyRemovedException("SpecificUser: " + user);
				throw new UserAlreadyRemovedException("User: " + user);
			}
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public User createUser(PerunSession sess, User user) throws InternalErrorException {
		try {
			int newId = Utils.getNewId(jdbc, "users_id_seq");
			char serviceAcc = '0';
			char sponsoredAcc = '0';
			if (user.isServiceUser()) {
				serviceAcc = '1';
			}
			if (user.isSponsoredUser()) {
				sponsoredAcc = '1';
			}
			jdbc.update("insert into users(id,first_name,last_name,middle_name,title_before,title_after,created_by,modified_by,service_acc,sponsored_acc,created_by_uid,modified_by_uid)" +
					" values (?,?,?,?,?,?,?,?,?,?,?,?)", newId, user.getFirstName(), user.getLastName(), user.getMiddleName(),
					user.getTitleBefore(), user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), "" + serviceAcc, "" + sponsoredAcc,
					sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			user.setId(newId);

			return user;
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public User updateUser(PerunSession sess, User user) throws InternalErrorException {
		try {
			User userDb = jdbc.queryForObject("select " + userMappingSelectQuery + " from users where id=? ", USER_MAPPER, user.getId());

			if (userDb == null) {
				throw new ConsistencyErrorException("Updating non existing user");
			}

			if ((user.getFirstName() != null && !user.getFirstName().equals(userDb.getFirstName())) ||
							(user.getFirstName() == null && userDb.getFirstName() != null)) {
				jdbc.update("update users set first_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getFirstName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setFirstName(user.getFirstName());
			}
			if (user.getLastName() != null && !user.getLastName().equals(userDb.getLastName())) {
				jdbc.update("update users set last_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getLastName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setLastName(user.getLastName());
			}
			if ((user.getMiddleName() != null && !user.getMiddleName().equals(userDb.getMiddleName())) ||
							(user.getMiddleName() == null && userDb.getMiddleName() != null)) {
				jdbc.update("update users set middle_name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getMiddleName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setMiddleName(user.getMiddleName());
			}
			if ((user.getTitleBefore() != null && !user.getTitleBefore().equals(userDb.getTitleBefore())) || 
							(user.getTitleBefore() == null && userDb.getTitleBefore() != null)) {
				jdbc.update("update users set title_before=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getTitleBefore(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setTitleBefore(user.getTitleBefore());
			}
			if ((user.getTitleAfter() != null && !user.getTitleAfter().equals(userDb.getTitleAfter())) ||
							(user.getTitleAfter() == null && userDb.getTitleAfter() != null)) {
				jdbc.update("update users set title_after=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setTitleAfter(user.getTitleAfter());
			}
		
			return userDb;
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public User updateNameTitles(PerunSession sess, User user) throws InternalErrorException {
		try {
			User userDb = jdbc.queryForObject("select " + userMappingSelectQuery + " from users where id=? ", USER_MAPPER, user.getId());

			if (userDb == null) {
				throw new ConsistencyErrorException("Updating titles for non existing user");
			}

			// changed condition to updateUser case to handle: fill, change and remove
			
			if ((user.getTitleBefore() != null && !user.getTitleBefore().equals(userDb.getTitleBefore())) ||
					(user.getTitleBefore() == null && userDb.getTitleBefore() != null)) {
				jdbc.update("update users set title_before=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getTitleBefore(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setTitleBefore(user.getTitleBefore());
			}
			if ((user.getTitleAfter() != null && !user.getTitleAfter().equals(userDb.getTitleAfter())) ||
					((user.getTitleAfter() == null && userDb.getTitleAfter() != null))) {
				jdbc.update("update users set title_after=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				userDb.setTitleAfter(user.getTitleAfter());
			}
			
			return userDb;
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		try {
			jdbc.update("update user_ext_sources set last_access=" + Compatibility.getSysdate() + " where id=?", userExtSource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		try {
			UserExtSource userExtSourceDb = jdbc.queryForObject("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
					" user_ext_sources.id=?", USEREXTSOURCE_MAPPER, userExtSource.getId());

			if (userExtSourceDb == null) {
				throw new ConsistencyErrorException("Updating non existing userExtSource");
			}

			if (userExtSource.getLoa() != userExtSourceDb.getLoa()) {
				jdbc.update("update user_ext_sources set loa=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						userExtSource.getLoa(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), userExtSource.getId());
			}
			if (userExtSource.getLogin() != null && userExtSourceDb.getLogin().equals(userExtSource.getLogin())) {
				jdbc.update("update user_ext_sources set login_ext=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						userExtSource.getLogin(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), userExtSource.getId());
			}

			return userExtSource;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException {
		try {
			Utils.notNull(userExtSource.getLogin(), "userExtSource.getLogin");

			int ueaId = Utils.getNewId(jdbc, "user_ext_sources_id_seq");

			log.debug("ueaId {}, user.getId() {}, userExtSource.getLogin() {}, userExtSource.getLoa() {}, userExtSource.getExtSource().getId() {}, " +
					"sess.getPerunPrincipal().getActor() {}, sess.getPerunPrincipal().getActor() {}, " +
					"sess.getPerunPrincipal().getUser().getId() {}, sess.getPerunPrincipal().getUser().getId() {}", new Object[]{ueaId, user.getId(), userExtSource.getLogin(),
						userExtSource.getLoa(), userExtSource.getExtSource().getId(),
				sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
				sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId()});
			jdbc.update("insert into user_ext_sources (id, user_id, login_ext, loa, ext_sources_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
					"values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					ueaId, user.getId(), userExtSource.getLogin(), userExtSource.getLoa(), userExtSource.getExtSource().getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			userExtSource.setId(ueaId);
			userExtSource.setUserId(user.getId());

			return userExtSource;
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException {
		int ueaId;
		try {
			ueaId =  jdbc.queryForInt("select user_ext_sources.id from user_ext_sources left join ext_sources on " +
					"user_ext_sources.ext_sources_id=ext_sources.id " +
					"where ext_sources.id=? and user_ext_sources.login_ext=?", source.getId(), extLogin);
		} catch (EmptyResultDataAccessException e) {
			throw new UserExtSourceNotExistsException("ExtSource: " + source + " for extLogin " + extLogin, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return this.getUserExtSourceById(sess, ueaId);
	}

	public List<Integer> getUserExtSourcesIds(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select id from user_ext_sources where user_id=?", Utils.ID_MAPPER, user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		//get now date
		Calendar date = Calendar.getInstance();
		date.add(Calendar.MONTH, -MAX_OLD_OF_ACTIVE_USER_EXTSOURCE);

		// create sql toDate()
		String compareDate = BeansUtils.getDateFormatterWithoutTime().format(date.getTime());

		try {
			String query = "select " + userExtSourceMappingSelectQuery + ", " + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where " +
					" user_ext_sources.user_id=? and " +
					" user_ext_sources.last_access > " + Compatibility.toDate("'" + compareDate + "'", "'YYYY-MM-DD'");

			return jdbc.query(query, USEREXTSOURCE_MAPPER, user.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) throws InternalErrorException {
		try {
			return jdbc.query("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
							" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
							" ext_sources.type=? and user_ext_sources.login_ext=?", USEREXTSOURCE_MAPPER, extType, extLogin);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<UserExtSource>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<UserExtSource> getUserExtsourcesByIds(PerunSession sess, List<Integer> ids) throws InternalErrorException {
		if (ids.size() == 0) {
			return new ArrayList<UserExtSource>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", ids);

		try {
			return namedParameterJdbcTemplate.query("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
					" user_ext_sources.id in ( :ids )", parameters, USEREXTSOURCE_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<UserExtSource>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
					" user_ext_sources.id=?", USEREXTSOURCE_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			throw new UserExtSourceNotExistsException(e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from user_ext_sources where id=?", userExtSource.getId());
			if(numAffected == 0) throw new UserExtSourceAlreadyRemovedException("User: " + user + " , UserExtSource: " + userExtSource);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllUserExtSources(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from user_ext_sources where user_id=?",user.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on authz.group_id=groups.id " +
					" where authz.user_id=? and authz.role_id=(select id from roles where name='groupadmin')", GroupsManagerImpl.GROUP_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Group>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + VosManagerImpl.voMappingSelectQuery + " from authz join vos on authz.vo_id=vos.id " +
					" where authz.user_id=? and authz.role_id=(select id from roles where name='voadmin')", VosManagerImpl.VO_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Vo>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + VosManagerImpl.voMappingSelectQuery + " from users join members on users.id=members.user_id, vos where " +
					"users.id=? and members.vo_id=vos.id", VosManagerImpl.VO_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException e) {
			// If user is not member of any vo, just return empty list
			return new ArrayList<Vo>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, user_attr_values where " +
					" user_attr_values.attr_value=? and users.id=user_attr_values.user_id and user_attr_values.attr_id=?",
					USER_MAPPER, BeansUtils.attributeValueToString(attribute), attribute.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefinition, String attributeValue) throws InternalErrorException {
		String value = "";;
		String operator = "=";
		if (attributeDefinition.getType().equals(String.class.getName())) {
			value = attributeValue.trim();
			operator = "=";
		} else if (attributeDefinition.getType().equals(Integer.class.getName())) {
			value = attributeValue.trim();
			operator = "=";
		}  else if (attributeDefinition.getType().equals(Boolean.class.getName())) {
			value = attributeValue.trim();
			operator = "=";
		} else if (attributeDefinition.getType().equals(ArrayList.class.getName())) {
			value = "%" + attributeValue.trim() + "%";
			operator = "like";
		} else if (attributeDefinition.getType().equals(LinkedHashMap.class.getName())) {
			value = "%" + attributeValue.trim() + "%";
			operator = "like";
		}

		String query = "select " + userMappingSelectQuery + " from users, user_attr_values where " +
			" user_attr_values.attr_value " + operator + " :value and users.id=user_attr_values.user_id and user_attr_values.attr_id=:attr_id";

		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("value", value);
		namedParams.addValue("attr_id", attributeDefinition.getId());

		try {
			return namedParameterJdbcTemplate.query(query, namedParams, USER_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException {
		Set<User> users = new HashSet<User>();

		log.debug("Searching for users using searchString '{}'", searchString);

		// Search by mail (member)
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
					" from users, members, member_attr_values, attr_names " +
					"where members.user_id=users.id and members.id=member_attr_values.member_id and member_attr_values.attr_id=attr_names.id and " +
					"attr_names.attr_name='urn:perun:member:attribute-def:def:mail' and " +
					"lower(member_attr_values.attr_value)=lower(?)", USER_MAPPER, searchString.toLowerCase()));

		// Search preferred email (user)
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
					" from users, user_attr_values, attr_names " +
					"where users.id=user_attr_values.user_id and user_attr_values.attr_id=attr_names.id and " +
					"attr_names.attr_name='urn:perun:user:attribute-def:def:preferredMail' and " +
					"lower(user_attr_values.attr_value)=lower(?)", USER_MAPPER, searchString.toLowerCase()));

		// Search logins in userExtSources
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
					" from users, user_ext_sources " +
					"where user_ext_sources.login_ext=? and user_ext_sources.user_id=users.id", USER_MAPPER, searchString));

		// Search logins in attributes: login-namespace:*
		users.addAll(jdbc.query("select distinct " + userMappingSelectQuery +
					" from attr_names, user_attr_values, users " +
					"where attr_names.friendly_name like 'login-namespace:%' and user_attr_values.attr_value=? " +
					"and attr_names.id=user_attr_values.attr_id and user_attr_values.user_id=users.id",
					USER_MAPPER, searchString));

		// Search by userId
		try {
			int userId = Integer.parseInt(searchString);
			users.addAll(jdbc.query("select " + userMappingSelectQuery + " from users where id=?", USER_MAPPER, userId));
		} catch (NumberFormatException e) {
			// IGNORE
		}

		users.addAll(findUsersByName(sess, searchString));

		return new ArrayList<User>(users);
	}

	public List<User> findUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException {
		Set<User> users = new HashSet<User>();

		log.debug("Searching for users using searchString '{}'", searchString);

		// Search by mail (member)
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
				" from users, members, member_attr_values, attr_names " +
				"where members.user_id=users.id and members.id=member_attr_values.member_id and member_attr_values.attr_id=attr_names.id and " +
				"attr_names.attr_name='urn:perun:member:attribute-def:def:mail' and " +
				"lower(member_attr_values.attr_value)=lower(?)", USER_MAPPER, searchString.toLowerCase()));

		// Search preferred email (user)
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
				" from users, user_attr_values, attr_names " +
				"where users.id=user_attr_values.user_id and user_attr_values.attr_id=attr_names.id and " +
				"attr_names.attr_name='urn:perun:user:attribute-def:def:preferredMail' and " +
				"lower(user_attr_values.attr_value)=lower(?)", USER_MAPPER, searchString.toLowerCase()));

		// Search logins in userExtSources
		users.addAll(jdbc.query("select " + userMappingSelectQuery +
				" from users, user_ext_sources " +
				"where user_ext_sources.login_ext=? and user_ext_sources.user_id=users.id", USER_MAPPER, searchString));

		// Search logins in attributes: login-namespace:*
		users.addAll(jdbc.query("select distinct " + userMappingSelectQuery +
						" from attr_names, user_attr_values, users " +
						"where attr_names.friendly_name like 'login-namespace:%' and user_attr_values.attr_value=? " +
						"and attr_names.id=user_attr_values.attr_id and user_attr_values.user_id=users.id",
				USER_MAPPER, searchString));

		// Search by userId
		try {
			int userId = Integer.parseInt(searchString);
			users.addAll(jdbc.query("select " + userMappingSelectQuery + " from users where id=?", USER_MAPPER, userId));
		} catch (NumberFormatException e) {
			// IGNORE
		}

		users.addAll(findUsersByExactName(sess, searchString));

		return new ArrayList<User>(users);
	}

	public List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException {
		if (searchString == null || searchString.isEmpty()) {
			return new ArrayList<User>();
		}

		// Convert to lower case
		searchString = searchString.toLowerCase();
		log.debug("Search string '{}' converted into the lowercase", searchString);

		// Convert to ASCII
		searchString = Utils.utftoasci(searchString);
		log.debug("Search string '{}' converted into the ASCII", searchString);

		// remove spaces from the search string
		searchString = searchString.replaceAll(" ", "");

		log.debug("Searching users by name using searchString '{}'", searchString);

		// the searchString is already lower cased and converted into the ASCII
		try {
			if (Compatibility.isOracle()) {
				// Search users' names
				return (jdbc.query("select " + userMappingSelectQuery + " from users " +
							"where lower("+Compatibility.convertToAscii("users.first_name || users.middle_name || users.last_name")+") like '%' || ? || '%'",
							USER_MAPPER, searchString));
			} else if (Compatibility.isPostgreSql()) {
				return jdbc.query("select " + userMappingSelectQuery + "  from users " +
						"where strpos(lower("+Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')")+"),?) > 0",
						USER_MAPPER, searchString);
			} else if (Compatibility.isHSQLDB()) {
				return jdbc.query("select " + userMappingSelectQuery + "  from users " +
								"where lower("+Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')")+") like '%' || ? || '%'",
						USER_MAPPER, searchString);
			} else {
				throw new InternalErrorException("Unsupported db type");
			}
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException {

		if (titleBefore.isEmpty()) {
			titleBefore = "%";
		}
		if (firstName.isEmpty()) {
			firstName = "%";
		}
		if (middleName.isEmpty()) {
			middleName = "%";
		}
		if (lastName.isEmpty()) {
			lastName = "%";
		}
		if (titleAfter.isEmpty()) {
			titleAfter = "%";
		}

		// the searchString is already lower cased
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users " +
					" where coalesce(lower("+Compatibility.convertToAscii("users.title_before")+"), '%') like ? and lower("+Compatibility.convertToAscii("users.first_name")+") like ? and coalesce(lower("+Compatibility.convertToAscii("users.middle_name")+"),'%') like ? and " +
					"lower("+Compatibility.convertToAscii("users.last_name")+") like ? and coalesce(lower("+Compatibility.convertToAscii("users.title_after")+"), '%') like ?",
					USER_MAPPER, titleBefore, firstName, middleName, lastName, titleAfter);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException {
		if (searchString == null || searchString.isEmpty()) {
			return new ArrayList<User>();
		}

		// Convert to lower case
		searchString = searchString.toLowerCase();
		log.debug("Search string '{}' converted into the lowercase", searchString);

		// Convert to ASCII
		searchString = Utils.utftoasci(searchString);
		log.debug("Search string '{}' converted into the ASCII", searchString);

		// remove spaces from the search string
		searchString = searchString.replaceAll(" ", "");

		log.debug("Searching users by name using searchString '{}'", searchString);

		// the searchString is already lower cased and converted into the ASCII
		try {
			if (Compatibility.isOracle()) {
				// Search users' names
				return (jdbc.query("select " + userMappingSelectQuery + " from users "
								+ "where lower(" + Compatibility.convertToAscii("users.first_name || users.middle_name || users.last_name") + ")=?",
						USER_MAPPER, searchString));
			} else if (Compatibility.isPostgreSql()) {
				return jdbc.query("select " + userMappingSelectQuery + " from users "
								+ "where lower(" + Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')") + ")=?",
						USER_MAPPER, searchString);
			} else if (Compatibility.isHSQLDB()) {
				return jdbc.query("select " + userMappingSelectQuery + "  from users " +
								"where lower("+Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')")+")=?",
						USER_MAPPER, searchString);
			} else {
				throw new InternalErrorException("Unsupported db type");
			}
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.PERUNADMIN.getRoleName());
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean userExists(PerunSession sess, User user) throws InternalErrorException {
		Utils.notNull(user, "user");
		try {
			return 1 == jdbc.queryForInt("select 1 from users where id=? and service_acc=? and sponsored_acc=?", user.getId(), user.isServiceUser() ? "1" : "0", user.isSponsoredUser() ? "1" : "0");
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException {
		if(!userExtSourceExists(sess, userExtSource)) throw new UserExtSourceNotExistsException("UserExtSource: " + userExtSource);
	}

	public void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException {
		if(isLoginReserved(sess, namespace, login)) throw new AlreadyReservedLoginException(namespace, login);
	}

	public boolean isLoginReserved(PerunSession sess, String namespace, String login) throws InternalErrorException {
		Utils.notNull(namespace, "loginNamespace");
		Utils.notNull(login, "userLogin");

		try {
			return 1 == jdbc.queryForInt("select 1 from application_reserved_logins where namespace=? and login=?",
					namespace, login);
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException {
		Utils.notNull(userExtSource, "userExtSource");

		try {
			return 1==jdbc.queryForInt("select 1 from user_ext_sources where id=?", userExtSource.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) throws InternalErrorException {
		// If usersIds is empty, we can immediatelly return empty results
		if (usersIds.size() == 0) {
			return new ArrayList<User>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", usersIds);

		try {
			return namedParameterJdbcTemplate.query("select " + userMappingSelectQuery +
					"  from users where users.id in ( :ids )",
					parameters, USER_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<User>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users where " +
					"users.id not in (select user_id from members) order by last_name, first_name", USER_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllAuthorships(PerunSession sess, User user) throws InternalErrorException {

		try {
			jdbc.update("delete from cabinet_authorships where userid=?", user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	public List<Pair<String, String>> getUsersReservedLogins(User user) throws InternalErrorException {

		List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();

		try {
			List<Integer> ids = jdbc.query("select id from application where user_id=?", new RowMapper<Integer>() {
				@Override
				public Integer mapRow(ResultSet rs, int arg1) throws SQLException {
					return rs.getInt("id");
				}
			},user.getId());

			for (Integer id : ids) {

				result.addAll(jdbc.query("select namespace,login from application_reserved_logins where app_id=?", new RowMapper<Pair<String, String>>() {
					@Override
					public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
						return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
					}
				}, id));

			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return result;

	}

	public void deleteUsersReservedLogins(User user) throws InternalErrorException {

		try {
			List<Integer> ids = jdbc.query("select id from application where user_id=?", new RowMapper<Integer>() {
				@Override
				public Integer mapRow(ResultSet rs, int arg1)
				throws SQLException {
				return rs.getInt("id");
				}
			},user.getId());

			for (Integer id : ids) {

				jdbc.update("delete from application_reserved_logins where app_id=?", id);

			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	public int requestPreferredEmailChange(PerunSession sess, User user, String email) throws InternalErrorException {

		int id = Utils.getNewId(jdbc, "mailchange_id_seq");

		try {
			jdbc.update("insert into mailchange(id, value, user_id, created_by, created_by_uid) values (?,?,?,?,?) ",
					id, email, user.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return id;

	}

	public String getPreferredEmailChangeRequest(PerunSession sess, User user, String i, String m) throws InternalErrorException {

		int changeId = Integer.parseInt(i, Character.MAX_RADIX);

		int validWindow = VALIDATION_ALLOWED_HOURS;
		try {
			validWindow = Integer.parseInt(BeansUtils.getPropertyFromConfiguration("perun.mailchange.validationWindow"));
		} catch (Exception ex) {
			log.error("Unable to load validation window interval from perun.properties. Falling back to default in source-code.");
		}

		// get new email if possible
		String newEmail = "";
		try {
			if (Compatibility.isPostgreSql()) {
				// postgres
				newEmail = jdbc.queryForObject("select value from mailchange where id=? and user_id=? and (created_at > (now() - interval '"+validWindow+" hours'))", String.class, changeId, user.getId());
			} else {
				// oracle
				newEmail = jdbc.queryForObject("select value from mailchange where id=? and user_id=? and (created_at > (SYSTIMESTAMP - INTERVAL '"+validWindow+"' HOUR))", String.class, changeId, user.getId());
			}
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException("Preferred mail change request with ID="+changeId+" doesn't exists or isn't valid anymore.");
		}

		return newEmail;

	}

	public void removeAllPreferredEmailChangeRequests(PerunSession sess, User user) throws InternalErrorException {

		try {
			jdbc.update("delete from mailchange where user_id=?", user.getId());
		} catch (Exception ex) {
			throw new InternalErrorException("Unable to remove preferred mail change requests for user: "+user, ex);
		}

	}

	public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws InternalErrorException {

		int validWindow = VALIDATION_ALLOWED_HOURS;
		try {
			validWindow = Integer.parseInt(BeansUtils.getPropertyFromConfiguration("perun.mailchange.validationWindow"));
		} catch (Exception ex) {
			log.error("Unable to load validation window interval from perun.properties. Falling back to default in source-code.");
		}

		try {
			if (Compatibility.isPostgreSql()) {

				return jdbc.query("select value from mailchange where user_id=? and (created_at > (now() - interval '" + validWindow + " hours'))", new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet resultSet, int i) throws SQLException {
						return resultSet.getString("value");
					}
				}, user.getId());

			} else {

				return jdbc.query("select value from mailchange where user_id=? and (created_at > (SYSTIMESTAMP - INTERVAL '"+validWindow+"' HOUR))", new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet resultSet, int i) throws SQLException {
						return resultSet.getString("value");
					}
				}, user.getId());

			}
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<String>();
		}

	}

	public String loadPasswordResetRequest(User user, int requestId) throws InternalErrorException {

		int validWindow = VALIDATION_ALLOWED_HOURS;
		try {
			validWindow = Integer.parseInt(BeansUtils.getPropertyFromConfiguration("perun.pwdreset.validationWindow"));
		} catch (Exception ex) {
			log.error("Unable to load validation window interval from perun.properties. Falling back to default in source-code.");
		}

		String result = "";
		try {
			if (Compatibility.isPostgreSql()) {

				result = jdbc.queryForObject("select namespace from pwdreset where user_id=? and id=? and (created_at > (now() - interval '" + validWindow + " hours'))", new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet resultSet, int i) throws SQLException {
						return resultSet.getString("namespace");
					}
				}, user.getId(), requestId);

			} else {

				result =  jdbc.queryForObject("select namespace from pwdreset where user_id=? and id=? and (created_at > (SYSTIMESTAMP - INTERVAL '"+validWindow+"' HOUR))", new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet resultSet, int i) throws SQLException {
						return resultSet.getString("namespace");
					}
				}, user.getId(), requestId);

			}

			jdbc.update("delete from pwdreset where user_id=? and id=?", user.getId(), requestId);
			return result;

		} catch (EmptyResultDataAccessException ex) {
			return result;
		}

	}

	public void removeAllPasswordResetRequests(PerunSession sess, User user) throws InternalErrorException {

		try {
			jdbc.update("delete from pwdreset where user_id=?", user.getId());
		} catch (Exception ex) {
			throw new InternalErrorException("Unable to remove password reset requests for user: "+user, ex);
		}

	}

	public int getUsersCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from users");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkUserExists(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		if(!userExists(sess, user)) throw new UserNotExistsException("User: " + user);
	}

	@Override
	public Map<String,String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws InternalErrorException {

		PasswordManagerModule module = getPasswordManagerModule(session, namespace);
		if (module != null) {
			return module.generateAccount(session, parameters);
		}
		return null;

	}

	@Override
	public PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) throws InternalErrorException {

		if (namespace == null || namespace.isEmpty()) throw new InternalErrorException("Login-namespace to get password manager module must be specified.");

		namespace = namespace.replaceAll("[^A-Za-z0-9]", "");
		namespace = Character.toUpperCase(namespace.charAt(0)) + namespace.substring(1);

		try {
			return (PasswordManagerModule) Class.forName("cz.metacentrum.perun.core.impl.modules.pwdmgr." + namespace + "PasswordManagerModule").newInstance();
		} catch (Exception ex) {
			throw new InternalErrorException("Unable to instantiate password manager module.", ex);
		}

	}

}
