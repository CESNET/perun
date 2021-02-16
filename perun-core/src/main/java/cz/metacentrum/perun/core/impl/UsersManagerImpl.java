package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkExpiredException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkNotValidException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RESOURCE_MAPPER;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RICH_RESOURCE_WITH_TAGS_EXTRACTOR;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.resourceMappingSelectQuery;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.resourceTagMappingSelectQuery;

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
	protected final static String userMappingSelectQuery = "users.id as users_id, users.uu_id as users_uu_id, users.first_name as users_first_name, users.last_name as users_last_name, " +
		"users.middle_name as users_middle_name, users.title_before as users_title_before, users.title_after as users_title_after, " +
		"users.created_at as users_created_at, users.created_by as users_created_by, users.modified_by as users_modified_by, users.modified_at as users_modified_at, " +
		"users.sponsored_acc as users_sponsored_acc, users.service_acc as users_service_acc, users.created_by_uid as users_created_by_uid, users.modified_by_uid as users_modified_by_uid";

	protected final static String userExtSourceMappingSelectQuery = "user_ext_sources.id as user_ext_sources_id, user_ext_sources.login_ext as user_ext_sources_login_ext, " +
		"user_ext_sources.user_id as user_ext_sources_user_id, user_ext_sources.loa as user_ext_sources_loa, user_ext_sources.created_at as user_ext_sources_created_at, user_ext_sources.created_by as user_ext_sources_created_by, " +
		"user_ext_sources.modified_by as user_ext_sources_modified_by, user_ext_sources.modified_at as user_ext_sources_modified_at, " +
		"user_ext_sources.created_by_uid as ues_created_by_uid, user_ext_sources.modified_by_uid as ues_modified_by_uid, user_ext_sources.last_access as ues_last_access";

	private static final Map<String, Pattern> userExtSourcePersistentPatterns;

	static {
		// Prepare userExtSourcePersistentPatterns for matching regex from perun property file.
		// It is done in advance because of performance.
		userExtSourcePersistentPatterns = new HashMap<>();
		String persistentConfig = BeansUtils.getCoreConfig().getUserExtSourcesPersistent();
		for (String extSource : persistentConfig.split(";")) {
			String[] extSourceTuple = extSource.split(",", 2);
			if (extSourceTuple.length > 1) {
				userExtSourcePersistentPatterns.put(extSourceTuple[0], Pattern.compile(extSourceTuple[1]));
			} else {
				userExtSourcePersistentPatterns.put(extSource, Pattern.compile(".*"));
			}
		}
	}

	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

	protected static final RowMapper<User> USER_MAPPER = (resultSet, i) ->
		new User(resultSet.getInt("users_id"), resultSet.getObject("users_uu_id", UUID.class), resultSet.getString("users_first_name"), resultSet.getString("users_last_name"),
			resultSet.getString("users_middle_name"), resultSet.getString("users_title_before"), resultSet.getString("users_title_after"),
			resultSet.getString("users_created_at"), resultSet.getString("users_created_by"), resultSet.getString("users_modified_at"), resultSet.getString("users_modified_by"), resultSet.getBoolean("users_service_acc"),
			resultSet.getBoolean("users_sponsored_acc"),
			resultSet.getInt("users_created_by_uid") == 0 ? null : resultSet.getInt("users_created_by_uid"), resultSet.getInt("users_modified_by_uid") == 0 ? null : resultSet.getInt("users_modified_by_uid"));

	private static final RowMapper<UserExtSource> USEREXTSOURCE_MAPPER = new RowMapper<UserExtSource>() {
		@Override
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
					rs.getInt("ues_modified_by_uid") == 0 ? null : rs.getInt("ues_modified_by_uid"),
					rs.getString("ues_last_access"));
		}
	};

        protected static final ResultSetExtractor<List<Pair<User,String>>> USERBLACKLIST_EXTRACTOR = resultSet -> {
            List<Pair<User, String>> result = new ArrayList<>();

            int row = 0;
            while(resultSet.next()){
                result.add(new Pair<>(USER_MAPPER.mapRow(resultSet, row), resultSet.getString("description")));
                row++;
            }

            return result;
        };

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool
	 */
	public UsersManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public User getUserById(PerunSession sess, int id) throws UserNotExistsException {
		try {
			return jdbc.queryForObject("select " + userMappingSelectQuery + " from users where users.id=? ", USER_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new UserNotExistsException("user id=" + id);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws UserNotExistsException {
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
	public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users join user_ext_sources on users.id=user_ext_sources.user_id join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id"
					+ " where ext_sources.type=? and user_ext_sources.login_ext=?", USER_MAPPER, extSourceType, login);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public User getUserByMember(PerunSession sess, Member member) {
		try {
			return jdbc.queryForObject("select " + userMappingSelectQuery + " from users, members " +
					"where members.id=? and members.user_id=users.id", USER_MAPPER, member.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException("Member has to have a corresponding User", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<User> getUsersByVo(PerunSession sess, Vo vo) {
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users, members " +
					"where members.user_id=users.id and members.vo_id=?", USER_MAPPER, vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getUsers(PerunSession sess) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					"  from users", USER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getSpecificUsersByUser(PerunSession sess, User user) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, specific_user_users where users.id=specific_user_users.specific_user_id and specific_user_users.status=0 and specific_user_users.user_id=?", USER_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, specific_user_users where users.id=specific_user_users.user_id and specific_user_users.status=0 and specific_user_users.specific_user_id=? " +
					" and specific_user_users.type=?", USER_MAPPER, specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws SpecificUserOwnerAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from specific_user_users where user_id=? and specific_user_id=? and specific_user_users.type=?",
					user.getId(), specificUser.getId(),specificUser.getMajorSpecificType().getSpecificUserType());
			if(numAffected == 0) throw new SpecificUserOwnerAlreadyRemovedException("SpecificUser-Owner: " + user + " , SpecificUser: " + specificUser);

		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) {
		try {
			jdbc.update("insert into specific_user_users(user_id,specific_user_id,status,created_by_uid,modified_at,type) values (?,?,0,?," + Compatibility.getSysdate() + ",?)",
					user.getId(), specificUser.getId(), sess.getPerunPrincipal().getUserId(), specificUser.getMajorSpecificType().getSpecificUserType());

		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void enableOwnership(PerunSession sess, User user, User specificUser) {
		try {
			jdbc.update("update specific_user_users set status=0, modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
					sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (RuntimeException er) {
			throw new InternalErrorException(er);
		}
	}

	@Override
	public void disableOwnership(PerunSession sess, User user, User specificUser) {
		try {
			jdbc.update("update specific_user_users set status=1, modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
					sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
		} catch (RuntimeException er) {
			throw new InternalErrorException(er);
		}
	}

	@Override
	public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from specific_user_users where user_id=? and specific_user_id=? and type=?",
					user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Ownership between user " + user + " and specificUser " + specificUser +  " exists more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getSpecificUsers(PerunSession sess) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					"  from users where users.service_acc=true or users.sponsored_acc=true", USER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			// Return empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteUser(PerunSession sess, User user) throws UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
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

	@Override
	public User createUser(PerunSession sess, User user) {
		int newId;
		try {
			newId = Utils.getNewId(jdbc, "users_id_seq");
			jdbc.update("insert into users(id,first_name,last_name,middle_name,title_before,title_after,created_by,modified_by,service_acc,sponsored_acc,created_by_uid,modified_by_uid)" +
					" values (?,?,?,?,?,?,?,?,?,?,?,?)", newId, user.getFirstName(), user.getLastName(), user.getMiddleName(),
					user.getTitleBefore(), user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), user.isServiceUser(), user.isSponsoredUser(),
					sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
		User newUser;
		try {
			newUser = getUserById(sess, newId);
		} catch (UserNotExistsException e) {
			throw new InternalErrorException("Failed to read newly created user with id: " + newId, e);
		}
		user.setId(newId);
		user.setUuid(newUser.getUuid());
		return newUser;
	}

	@Override
	public User setSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType) {
		try {
			if(specificUserType.equals(SpecificUserType.SERVICE)) {
				jdbc.update("update users set service_acc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						true, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				user.setServiceUser(true);
			} else if(specificUserType.equals(SpecificUserType.SPONSORED)) {
				jdbc.update("update users set sponsored_acc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						true, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				user.setSponsoredUser(true);
			} else {
				throw new InternalErrorException("Unsupported specific user type " + specificUserType.getSpecificUserType());
			}
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}

		return user;
	}

	@Override
	public User unsetSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType) {
		try {
			if(specificUserType.equals(SpecificUserType.SERVICE)) {
				jdbc.update("update users set service_acc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						false, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				user.setServiceUser(false);
			} else if(specificUserType.equals(SpecificUserType.SPONSORED)) {
				jdbc.update("update users set sponsored_acc=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
						false, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
				user.setSponsoredUser(false);
			} else {
				throw new InternalErrorException("Unsupported specific user type " + specificUserType.getSpecificUserType());
			}
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}

		return user;
	}

	@Override
	public User updateUser(PerunSession sess, User user) {
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

	@Override
	public User updateNameTitles(PerunSession sess, User user) {
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

	@Override
	public User anonymizeUser(PerunSession sess, User user) {
		try {
			user.setFirstName("");
			user.setMiddleName("");
			user.setLastName("");
			user.setTitleBefore("");
			user.setTitleAfter("");
			jdbc.update("update users set first_name=NULL, last_name=NULL, middle_name=NULL, title_before=NULL, title_after=NULL, " +
					"anonymized=true, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
				sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
			return user;
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) {
		try {
			jdbc.update("update user_ext_sources set last_access=" + Compatibility.getSysdate() + " where id=?", userExtSource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource) throws UserExtSourceExistsException {
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
			if (userExtSource.getLogin() != null && !userExtSourceDb.getLogin().equals(userExtSource.getLogin())) {
				try {
					jdbc.update("update user_ext_sources set login_ext=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?",
							userExtSource.getLogin(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), userExtSource.getId());
				} catch (DuplicateKeyException ex) {
					throw new UserExtSourceExistsException("UES with same login already exists: " + userExtSource);
				}
			}

			return userExtSource;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) {
		try {
			Utils.notNull(userExtSource.getLogin(), "userExtSource.getLogin");

			int ueaId = Utils.getNewId(jdbc, "user_ext_sources_id_seq");

			log.trace("Adding new user ext source: ueaId {}, user.getId() {}, userExtSource.getLogin() {}, userExtSource.getLoa() {}, userExtSource.getExtSource().getId() {}, " +
					"sess.getPerunPrincipal().getActor() {}, sess.getPerunPrincipal().getActor() {}, " +
					"sess.getPerunPrincipal().getUser().getId() {}, sess.getPerunPrincipal().getUser().getId() {}", ueaId, user.getId(), userExtSource.getLogin(),
				userExtSource.getLoa(), userExtSource.getExtSource().getId(),
				sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
				sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			if (userExtSource.getLastAccess() != null) {
				// user ext source has last access info
				jdbc.update("insert into user_ext_sources (id, user_id, login_ext, loa, ext_sources_id, last_access, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
								"values (?,?,?,?,?,"+Compatibility.toDate("?", "'YYYY-MM-DD HH24:MI:SS.US'")+",?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
						ueaId, user.getId(), userExtSource.getLogin(), userExtSource.getLoa(), userExtSource.getExtSource().getId(), userExtSource.getLastAccess(),
						sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			} else {
				// adding new user ext source with default current timestamp
				jdbc.update("insert into user_ext_sources (id, user_id, login_ext, loa, ext_sources_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
								"values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
						ueaId, user.getId(), userExtSource.getLogin(), userExtSource.getLoa(), userExtSource.getExtSource().getId(),
						sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			}

			userExtSource.setId(ueaId);
			userExtSource.setUserId(user.getId());

			return userExtSource;
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin) throws UserExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
			        " from user_ext_sources left join ext_sources " +
					" on user_ext_sources.ext_sources_id=ext_sources.id " +
					" where ext_sources.id=? and user_ext_sources.login_ext=?", USEREXTSOURCE_MAPPER, source.getId(), extLogin);
		} catch (EmptyResultDataAccessException e) {
			throw new UserExtSourceNotExistsException("ExtSource: " + source + " for extLogin " + extLogin, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) {
		//get now date
		LocalDate date = LocalDate.now();
		date = date.minusMonths(MAX_OLD_OF_ACTIVE_USER_EXTSOURCE);

		try {
			String query = "select " + userExtSourceMappingSelectQuery + ", " + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where " +
					" user_ext_sources.user_id=? and " +
					" user_ext_sources.last_access > " + Compatibility.toDate("'" + date + "'", "'YYYY-MM-DD'");

			return jdbc.query(query, USEREXTSOURCE_MAPPER, user.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) {
		try {
			return jdbc.query("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
							" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
							" ext_sources.type=? and user_ext_sources.login_ext=?", USEREXTSOURCE_MAPPER, extType, extLogin);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws UserExtSourceNotExistsException {
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

	@Override
	public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue) throws UserExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
				" from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id " +
				" left join user_ext_source_attr_u_values on user_ext_source_attr_u_values.user_ext_source_id=user_ext_sources.id" +
				" where user_ext_source_attr_u_values.attr_id=? and user_ext_source_attr_u_values.attr_value=?", USEREXTSOURCE_MAPPER, attrId, uniqueValue);
		} catch (EmptyResultDataAccessException e) {
			throw new UserExtSourceNotExistsException(e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids) {
		try {
			return jdbc.execute("select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
					"  from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where user_ext_sources.id " + Compatibility.getStructureForInClause(),
				(PreparedStatementCallback<List<UserExtSource>>) preparedStatement -> {
					Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(ids, preparedStatement);
					preparedStatement.setArray(1, sqlArray);
					ResultSet rs = preparedStatement.executeQuery();
					List<UserExtSource> userExtSources = new ArrayList<>();
					while (rs.next()) {
						userExtSources.add(USEREXTSOURCE_MAPPER.mapRow(rs, rs.getRow()));
					}
					return userExtSources;
				});
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<UserExtSource> getUserExtSources(PerunSession sess, User user) {
		try {
			return jdbc.query("SELECT " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
			        " FROM user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id" +
			        " WHERE user_ext_sources.user_id=?", USEREXTSOURCE_MAPPER, user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) throws UserExtSourceAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from user_ext_sources where id=?", userExtSource.getId());
			if(numAffected == 0) throw new UserExtSourceAlreadyRemovedException("User: " + user + " , UserExtSource: " + userExtSource);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllUserExtSources(PerunSession sess, User user) {
		try {
			jdbc.update("delete from user_ext_sources where user_id=?",user.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) {
		try {
			return jdbc.query("select distinct " + GroupsManagerImpl.groupMappingSelectQuery + " from groups where groups.id in " +
							" (select group_id from authz where ( authz.user_id=? or  authz.authorized_group_id in " +
							" (select distinct groups.id from groups join groups_members on groups_members.group_id=groups.id " +
							" join members on groups_members.member_id=members.id where members.user_id=?) " +
							" and authz.role_id=(select id from roles where roles.name=?))) ",
					GroupsManagerImpl.GROUP_MAPPER, user.getId(), user.getId(), Role.GROUPADMIN.toLowerCase());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user) {
		try {
			return jdbc.query("select distinct " + GroupsManagerImpl.groupMappingSelectQuery + " from groups where groups.id in " +
							" (select group_id from authz where ( authz.user_id=? or  authz.authorized_group_id in " +
							" (select distinct groups.id from groups join groups_members on groups_members.group_id=groups.id " +
							" join members on groups_members.member_id=members.id where members.user_id=?) " +
							" and authz.role_id=(select id from roles where roles.name=?))) and groups.vo_id=? ",
					GroupsManagerImpl.GROUP_MAPPER, user.getId(), user.getId(), Role.GROUPADMIN.toLowerCase(), vo.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) {
		try {
			return jdbc.query("select " + VosManagerImpl.voMappingSelectQuery + " from authz join vos on authz.vo_id=vos.id " +
					" left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
					" left outer join members on members.id=groups_members.member_id " +
					" where (authz.user_id=? or members.user_id=?) and authz.role_id=(select id from roles where name=?)",
					VosManagerImpl.VO_MAPPER, user.getId(), user.getId(), Role.VOADMIN.toLowerCase());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) {
		try {
			return jdbc.query("select " + VosManagerImpl.voMappingSelectQuery + " from users join members on users.id=members.user_id, vos where " +
					"users.id=? and members.vo_id=vos.id", VosManagerImpl.VO_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException e) {
			// If user is not member of any vo, just return empty list
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) {
		try {
			return jdbc.query("select " + userMappingSelectQuery +
					" from users, user_attr_values where " +
					" user_attr_values.attr_value=? and users.id=user_attr_values.user_id and user_attr_values.attr_id=?",
					USER_MAPPER, BeansUtils.attributeValueToString(attribute), attribute.getId());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefinition, String attributeValue) {
		String value = "";
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
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> findUsers(PerunSession sess, String searchString) {
		return findUsers(searchString, false);
	}

	@Override
	public List<User> findUsersByExactMatch(PerunSession sess, String searchString) {
		return findUsers(searchString, true);
	}

	/**
	 * Returns list of users who matches the searchString, searching name, id, member attributes, user attributes
	 * and userExtSource attributes (listed in CoreConfig).
	 *
	 * @param searchString string used to search by
	 * @param exactMatch if true, searches name only by exact match
	 * @return list of users
	 */
	private List<User> findUsers(String searchString, boolean exactMatch) {
		String userNameQueryString;
		if (exactMatch) {
			// Part of query to search by user name (exact)
			userNameQueryString = Utils.prepareUserSearchQueryExactMatch();
		} else {
			// Part of query to search by user name (not exact)
			userNameQueryString = Utils.prepareUserSearchQuerySimilarMatch();
		}

		// Part of query to search by user id
		String idQueryString = "";
		try {
			int id = Integer.parseInt(searchString);
			idQueryString = " users.id=" + id + " or ";
		} catch (NumberFormatException e) {
			// IGNORE wrong format of ID
		}

		// Divide attributes received from CoreConfig into member, user and userExtSource attributes
		Map<String, List<String>> attributesToSearchBy = Utils.getDividedAttributes();

		// Parts of query to search by attributes
		Map<String, Pair<String, String>> attributesToSearchByQueries = Utils.getAttributesQuery(attributesToSearchBy.get("memberAttributes"), attributesToSearchBy.get("userAttributes"), attributesToSearchBy.get("uesAttributes"));

		MapSqlParameterSource namedParams = Utils.getMapSqlParameterSourceToSearchUsersOrMembers(searchString, attributesToSearchBy);

		// Search by member attributes
		// Search by user attributes
		// Search by login in userExtSources
		// Search by userExtSource attributes
		// Search by user id
		// Search by name for user
		Set<User> users = new HashSet<>(namedParameterJdbcTemplate.query("select distinct " + userMappingSelectQuery +
			" from users " +
			" left join members on users.id=members.user_id " +
			" left join user_ext_sources ues on ues.user_id=users.id " +
			attributesToSearchByQueries.get("memberAttributesQuery").getLeft() +
			attributesToSearchByQueries.get("userAttributesQuery").getLeft() +
			attributesToSearchByQueries.get("uesAttributesQuery").getLeft() +
			" where " +
			" ( " +
			" lower(ues.login_ext)=lower(:searchString) or " +
			attributesToSearchByQueries.get("memberAttributesQuery").getRight() +
			attributesToSearchByQueries.get("userAttributesQuery").getRight() +
			attributesToSearchByQueries.get("uesAttributesQuery").getRight() +
			idQueryString +
			userNameQueryString +
			" ) ", namedParams, USER_MAPPER));

		log.debug("Searching for users using searchString '{}'", searchString);

		return new ArrayList<>(users);
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String searchString) {
		if (searchString == null || searchString.isEmpty()) {
			return new ArrayList<>();
		}

		// Convert to lowercase
		searchString = searchString.toLowerCase();
		log.trace("Search string '{}' converted into lower-cased", searchString);

		// remove spaces from the search string
		searchString = searchString.replaceAll(" ", "");

		log.debug("Searching users by name using searchString '{}'", searchString);

		// the searchString is already lower cased
		try {
			return jdbc.query("select " + userMappingSelectQuery + "  from users " +
					"where strpos(lower("+Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')")+")," + Compatibility.convertToAscii("?") + ") > 0",
					USER_MAPPER, searchString);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) {

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
					" where coalesce(lower("+Compatibility.convertToAscii("users.title_before")+"), '%') like "+Compatibility.convertToAscii("?")+" and lower("+Compatibility.convertToAscii("users.first_name")+") like "+Compatibility.convertToAscii("?")+
					" and coalesce(lower("+Compatibility.convertToAscii("users.middle_name")+"),'%') like "+Compatibility.convertToAscii("?")+" and lower("+Compatibility.convertToAscii("users.last_name")+") like "+Compatibility.convertToAscii("?")+
					" and coalesce(lower("+Compatibility.convertToAscii("users.title_after")+"), '%') like "+Compatibility.convertToAscii("?"),
					USER_MAPPER, titleBefore, firstName, middleName, lastName, titleAfter);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> findUsersByExactName(PerunSession sess, String searchString) {
		if (searchString == null || searchString.isEmpty()) {
			return new ArrayList<>();
		}

		// Convert to lower case
		searchString = searchString.toLowerCase();
		log.debug("Search string '{}' converted into the lowercase", searchString);

		// remove spaces from the search string
		searchString = searchString.replaceAll(" ", "");

		log.debug("Searching users by name using searchString '{}'", searchString);

		// the searchString is already lower cased
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users "
							+ "where lower(" + Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')") + ")=" + Compatibility.convertToAscii("?"),
					USER_MAPPER, searchString);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean isUserPerunAdmin(PerunSession sess, User user) {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.PERUNADMIN.toLowerCase());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("User " + user + " is PERUNADMIN more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean userExists(PerunSession sess, User user) {
		Utils.notNull(user, "user");
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from users where id=? and service_acc=? and sponsored_acc=?", user.getId(), user.isServiceUser(), user.isSponsoredUser());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("User " + user + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws UserExtSourceNotExistsException {
		if(!userExtSourceExists(sess, userExtSource)) throw new UserExtSourceNotExistsException("UserExtSource: " + userExtSource);
	}

	@Override
	public void checkUserExtSourceExistsById(PerunSession sess, int id) throws UserExtSourceNotExistsException {

		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from user_ext_sources where id=?", id);
			if (numberOfExistences == 0) throw new UserExtSourceNotExistsException("UserExtSource with ID=" + id + " doesn't exists.");
			if (numberOfExistences > 1) throw new ConsistencyErrorException("UserExtSource wit ID=" + id + " exists more than once.");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public void checkReservedLogins(PerunSession sess, String namespace, String login) throws AlreadyReservedLoginException {
		if(isLoginReserved(sess, namespace, login)) throw new AlreadyReservedLoginException(namespace, login);
	}

	@Override
	public boolean isLoginReserved(PerunSession sess, String namespace, String login) {
		Utils.notNull(namespace, "loginNamespace");
		Utils.notNull(login, "userLogin");

		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from application_reserved_logins where namespace=? and login=?",
					namespace, login);
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Login " + login + " in namespace " + namespace + " is reserved more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) {
		Utils.notNull(userExtSource, "userExtSource");
		Utils.notNull(userExtSource.getLogin(), "userExtSource.getLogin");
		Utils.notNull(userExtSource.getExtSource(), "userExtSource.getExtSource");

		try {

			// check by ext identity (login/ext source ID)
			if (userExtSource.getUserId() >= 0) {
				int numberOfExistences = jdbc.queryForInt("select count(1) from user_ext_sources where login_ext=? and ext_sources_id=? and user_id=?",
						userExtSource.getLogin(), userExtSource.getExtSource().getId(), userExtSource.getUserId());
				if (numberOfExistences == 1) {
					return true;
				} else if (numberOfExistences > 1) {
					throw new ConsistencyErrorException("UserExtSource " + userExtSource + " exists more than once.");
				}
				return false;
			} else {
				int numberOfExistences = jdbc.queryForInt("select count(1) from user_ext_sources where login_ext=? and ext_sources_id=?",
						userExtSource.getLogin(), userExtSource.getExtSource().getId());
				if (numberOfExistences == 1) {
					return true;
				} else if (numberOfExistences > 1) {
					throw new ConsistencyErrorException("UserExtSource " + userExtSource + " exists more than once.");
				}
				return false;
			}

		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) {
		// If usersIds is empty, we can immediately return empty results
		if (usersIds.size() == 0) {
			return new ArrayList<>();
		}
		return jdbc.execute("select " + userMappingSelectQuery + "  from users where users.id " + Compatibility.getStructureForInClause(),
			(PreparedStatementCallback<List<User>>) preparedStatement -> {
				Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(usersIds, preparedStatement);
				preparedStatement.setArray(1, sqlArray);
				ResultSet rs = preparedStatement.executeQuery();
				List<User> users = new ArrayList<>();
				while (rs.next()) {
					users.add(USER_MAPPER.mapRow(rs, rs.getRow()));
				}
				return users;
			});
	}

	@Override
	public List<User> getUsersWithoutVoAssigned(PerunSession sess) {
		try {
			return jdbc.query("select " + userMappingSelectQuery + " from users where " +
					"users.id not in (select user_id from members) order by last_name, first_name", USER_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllAuthorships(PerunSession sess, User user) {

		try {
			jdbc.update("delete from cabinet_authorships where userid=?", user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public List<Pair<String, String>> getUsersReservedLogins(User user) {

		List<Pair<String, String>> result = new ArrayList<>();

		try {
			List<Integer> ids = jdbc.query("select id from application where user_id=?", (resultSet, i) -> resultSet.getInt("id"),user.getId());

			for (Integer id : ids) {

				result.addAll(jdbc.query("select namespace,login from application_reserved_logins where app_id=?",
					(resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), id));

			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return result;

	}

	@Override
	public void deleteUsersReservedLogins(User user) {

		try {
			List<Integer> ids = jdbc.query("select id from application where user_id=?", (resultSet, i) -> resultSet.getInt("id"),user.getId());

			for (Integer id : ids) {

				jdbc.update("delete from application_reserved_logins where app_id=?", id);

			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void deleteUsersApplications(User user) {
		try {
			jdbc.update("delete from application where user_id=?", user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int requestPreferredEmailChange(PerunSession sess, User user, String email) {

		int id = Utils.getNewId(jdbc, "mailchange_id_seq");

		try {
			jdbc.update("insert into mailchange(id, value, user_id, created_by, created_by_uid) values (?,?,?,?,?) ",
					id, email, user.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return id;

	}

	@Override
	public String getPreferredEmailChangeRequest(PerunSession sess, User user, String i, String m) {

		int changeId = Integer.parseInt(i, Character.MAX_RADIX);

		int validWindow = BeansUtils.getCoreConfig().getMailchangeValidationWindow();

		// get new email if possible
		String newEmail;
		try {
			newEmail = jdbc.queryForObject("select value from mailchange where id=? and user_id=? and (created_at > (now() - interval '"+validWindow+" hours'))", String.class, changeId, user.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException("Preferred mail change request with ID="+changeId+" doesn't exists or isn't valid anymore.");
		}

		return newEmail;

	}

	@Override
	public void removeAllPreferredEmailChangeRequests(PerunSession sess, User user) {

		try {
			jdbc.update("delete from mailchange where user_id=?", user.getId());
		} catch (Exception ex) {
			throw new InternalErrorException("Unable to remove preferred mail change requests for user: "+user, ex);
		}

	}

	@Override
	public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) {

		int validWindow = BeansUtils.getCoreConfig().getMailchangeValidationWindow();

		try {
			return jdbc.query("select value from mailchange where user_id=? and (created_at > (now() - interval '" + validWindow + " hours'))",
				(resultSet, i) -> resultSet.getString("value"), user.getId());
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}

	}

	@Override
	public void checkPasswordResetRequestIsValid(PerunSession sess, User user, int requestId) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
		this.checkAndGetPasswordResetRequest(user, requestId);
	}

	private Pair<String, String> checkAndGetPasswordResetRequest(User user, int requestId) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
		try {
			int numberOfRequests = jdbc.queryForInt("select count(1) from pwdreset where user_id=? and id=?", user.getId(), requestId);
			if (numberOfRequests == 0) {
				throw new PasswordResetLinkNotValidException("Password request " + requestId + " doesn't exist.");
			} else if (numberOfRequests > 1) {
				throw new ConsistencyErrorException("Password reset request " + requestId + " exists more than once.");
			}

			return jdbc.queryForObject("select namespace, mail from pwdreset where user_id=? and id=? and validity_to >= now()",
					(resultSet, i) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("mail")), user.getId(), requestId);
		} catch (EmptyResultDataAccessException ex) {
			throw new PasswordResetLinkExpiredException("Password reset request " + requestId + " has already expired.");
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Pair<String,String> loadPasswordResetRequest(PerunSession sess, User user, int requestId) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
		Pair<String,String> result = this.checkAndGetPasswordResetRequest(user, requestId);

		try {
			jdbc.update("delete from pwdreset where user_id=? and id=?", user.getId(), requestId);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return result;
	}

	@Override
	public void removeAllPasswordResetRequests(PerunSession sess, User user) {

		try {
			jdbc.update("delete from pwdreset where user_id=?", user.getId());
		} catch (Exception ex) {
			throw new InternalErrorException("Unable to remove password reset requests for user: "+user, ex);
		}

	}

	@Override
	public int getUsersCount(PerunSession sess) {
		try {
			return jdbc.queryForInt("select count(*) from users");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkUserExists(PerunSession sess, User user) throws UserNotExistsException {
		if(!userExists(sess, user)) throw new UserNotExistsException("User: " + user);
	}

	@Override
	public PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) {

		if (namespace == null || namespace.isEmpty()) throw new InternalErrorException("Login-namespace to get password manager module must be specified.");

		namespace = namespace.replaceAll("[^A-Za-z0-9]", "");
		namespace = Character.toUpperCase(namespace.charAt(0)) + namespace.substring(1);

		try {
			return (PasswordManagerModule) Class.forName("cz.metacentrum.perun.core.impl.modules.pwdmgr." + namespace + "PasswordManagerModule").newInstance();
		} catch (ClassNotFoundException ex) {
			return null;
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new InternalErrorException("Unable to instantiate password manager module.", ex);
		}

	}

	@Override
	public List<User> getSponsors(PerunSession sess, Member sponsoredMember) {
		try {
			return jdbc.query("SELECT " + userMappingSelectQuery + " FROM users JOIN members_sponsored ms ON (users.id=ms.sponsor_id)" +
					"WHERE ms.active=? AND ms.sponsored_id=? ", USER_MAPPER, true, sponsoredMember.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteSponsorLinks(PerunSession sess, User sponsor) {
		try {
			jdbc.update("DELETE FROM members_sponsored WHERE sponsor_id=?", sponsor.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd, List<String> excludeValueEnds) {
		try {
			StringBuilder sb = new StringBuilder("SELECT DISTINCT " + userMappingSelectQuery + " FROM users " +
					"  JOIN user_ext_sources ues ON users.id = ues.user_id " +
					"  JOIN user_ext_source_attr_values v ON ues.id = v.user_ext_source_id" +
					"  JOIN attr_names a ON (v.attr_id = a.id AND a.attr_name=?)" +
					"  WHERE v.attr_value LIKE ? ");
			List<String> args = new ArrayList<>();
			args.add(attributeName);
			args.add("%"+valueEnd);
			for(String excl : excludeValueEnds) {
				sb.append(" AND v.attr_value NOT LIKE ?");
				args.add("%"+excl);
			}
			return jdbc.query(sb.toString(), USER_MAPPER, args.toArray());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, User user) {
		try  {
			return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources" +
					" join groups_resources on resources.id=groups_resources.resource_id " +
					" join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id " +
					" join members on groups_members.member_id=members.id " +
					" where members.user_id=?", RESOURCE_MAPPER, user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getAllowedResources(PerunSession sess, User user) {
		try  {
			return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources" +
							" join groups_resources on resources.id=groups_resources.resource_id " +
							" join groups on groups_resources.group_id=groups.id" +
							" join groups_members on groups.id=groups_members.group_id " +
							" join members on groups_members.member_id=members.id " +
							" where members.user_id=? and members.status!=? and members.status!=?",
					RESOURCE_MAPPER, user.getId(), Status.INVALID.getCode(), Status.DISABLED.getCode());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) {
		try {
			return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources "+
							" join groups_resources on groups_resources.resource_id=resources.id" +
							" join groups_members on groups_members.group_id=groups_resources.group_id" +
							" join members on members.id=groups_members.member_id" +
							" where resources.facility_id=? and members.user_id=?",
					RESOURCE_MAPPER, facility.getId(), user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, User user) {
		try  {
			return jdbc.query("select distinct " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
					FacilitiesManagerImpl.facilityMappingSelectQuery + ", "+resourceTagMappingSelectQuery+" from resources" +
					" join vos on resources.vo_id=vos.id " +
					" join facilities on resources.facility_id=facilities.id " +
					" join groups_resources on resources.id=groups_resources.resource_id " +
					" join groups on groups_resources.group_id=groups.id " +
					" join groups_members on groups.id=groups_members.group_id " +
					" join members on groups_members.member_id=members.id " +
					" left outer join tags_resources on resources.id=tags_resources.resource_id" +
					" left outer join res_tags on tags_resources.tag_id=res_tags.id" +
					" where members.user_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

}
