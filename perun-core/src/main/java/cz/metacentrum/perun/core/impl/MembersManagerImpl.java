package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;

public class MembersManagerImpl implements MembersManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(MembersManagerImpl.class);

	public final static String PERUN_USERS = "perunUsers";

	protected final static String memberMappingSelectQuery = "members.id as members_id, members.user_id as members_user_id, members.vo_id as members_vo_id, members.status as members_status, " +
		"members.created_at as members_created_at, members.created_by as members_created_by, members.modified_by as members_modified_by, members.modified_at as members_modified_at, " +
		"members.created_by_uid as members_created_by_uid, members.modified_by_uid as members_modified_by_uid";

	protected final static String memberMappingSelectQueryWithMemTypeAndSourceGroupId = memberMappingSelectQuery + 
			", groups_members.membership_type AS membership_type, groups_members.source_group_id AS source_group_id"; 
	
	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	protected static final RowMapper<Member> MEMBER_MAPPER = new RowMapper<Member>() {
		public Member mapRow(ResultSet rs, int i) throws SQLException {
			Member member = new Member(rs.getInt("members_id"), rs.getInt("members_user_id"), rs.getInt("members_vo_id"), Status.getStatus(rs.getInt("members_status")),
					rs.getString("members_created_at"), rs.getString("members_created_by"), rs.getString("members_modified_at"), rs.getString("members_modified_by"),
					rs.getInt("members_created_by_uid") == 0 ? null : rs.getInt("members_created_by_uid"),
					rs.getInt("members_modified_by_uid") == 0 ? null : rs.getInt("members_modified_by_uid"));
			return member;
		}
	};

	protected static final RowMapper<Member> EXTENDED_MEMBER_MAPPER = new RowMapper<Member>() {
		public Member mapRow(ResultSet rs, int i) throws SQLException {
			Member member = new Member(rs.getInt("members_id"), rs.getInt("members_user_id"), rs.getInt("members_vo_id"), Status.getStatus(rs.getInt("members_status")),
					rs.getString("members_created_at"), rs.getString("members_created_by"), rs.getString("members_modified_at"), rs.getString("members_modified_by"),
					rs.getInt("members_created_by_uid") == 0 ? null : rs.getInt("members_created_by_uid"),
					rs.getInt("members_modified_by_uid") == 0 ? null : rs.getInt("members_modified_by_uid"));

			member.setSourceGroupId(rs.getInt("source_group_id"));
			member.setMembershipType(MembershipType.getMembershipType(rs.getInt("membership_type")));

			return member;
		}
	};

	/**
	 * Constructor
	 */
	public MembersManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
	}

	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException {
		Member member = null;
		try {
			// Set the new Member id
			int newId = Utils.getNewId(jdbc, "members_id_seq");

			jdbc.update("insert into members (id, vo_id, user_id, status, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) "
					+ "values (?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					newId, vo.getId(),
					user.getId(), Status.INVALID.getCode(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			member = new Member(newId, user.getId(), vo.getId(), Status.INVALID);

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return member;
	}

	public Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws InternalErrorException, MemberNotExistsException {
		try {
			return jdbc.queryForObject("select " + memberMappingSelectQuery + " from" +
					" members where members.user_id=? and members.vo_id=?",
					MEMBER_MAPPER, userId, vo.getId());

		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("user id=" + userId + " VO= " + vo);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("select " + memberMappingSelectQuery + " from" +
					" members where members.user_id=?",
					MEMBER_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException(new MemberNotExistsException("user=" + user));
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException {
		try {
			Member member = jdbc.queryForObject("select " + memberMappingSelectQuery + " from members "
					+ " where members.id=?",
					MEMBER_MAPPER, id);

			return member;
		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("member id=" + id);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void deleteMember(final PerunSession sess, final Member member) throws InternalErrorException, MemberAlreadyRemovedException{
		try {
			int numAffected = jdbc.update("delete from members where id=?", member.getId());
			if(numAffected == 0) throw new MemberAlreadyRemovedException("Member: " + member);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public int getMemberVoId(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select vo_id from members where id=?", member.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource userExtSource) throws InternalErrorException, MemberNotExistsException {
		try {
			return jdbc.queryForObject("select " + memberMappingSelectQuery + " from members, user_ext_sources where " +
					"user_ext_sources.login_ext=? and user_ext_sources.ext_sources_id=? and members.vo_id=? and members.user_id=user_ext_sources.user_id",
					MEMBER_MAPPER, userExtSource.getLogin(), userExtSource.getExtSource().getId(), vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("member userExtSource=" + userExtSource);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public boolean memberExists(PerunSession sess, Member member) throws InternalErrorException {
		Utils.notNull(member, "member");
		try {
			return 1 == jdbc.queryForInt("select 1 from members where id=?", member.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkMemberExists(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		if(!memberExists(sess, member)) throw new MemberNotExistsException("Member: " + member);
	}

	public void setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException {
		try {
			jdbc.update("update members set status=?, modified_by=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", status.getCode(), sess.getPerunPrincipal().getActor(), member.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) throws InternalErrorException {
		// If usersIds is empty, we can immediatelly return empty results
		if (usersIds.size() == 0) {
			return new ArrayList<Member>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", usersIds);
		parameters.addValue("vo", vo.getId());

		try {
			return this.namedParameterJdbcTemplate.query("select " + memberMappingSelectQuery +
					" from members join users on members.user_id=users.id where members.user_id in ( :ids ) and members.vo_id=:vo " +
					"order by users.last_name, users.first_name",
					parameters, MEMBER_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Member>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) throws InternalErrorException {
		// If usersIds is empty, we can immediatelly return empty results
		if (users.size() == 0) {
			return new ArrayList<Member>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		Set<Integer> usersIds = new HashSet<Integer>();
		for (User user: users) {
			usersIds.add(user.getId());
		}
		parameters.addValue("ids", usersIds);
		parameters.addValue("vo", vo.getId());

		try {
			return this.namedParameterJdbcTemplate.query("select " + memberMappingSelectQuery + " from members where members.user_id in ( :ids ) and members.vo_id=:vo",
					parameters, MEMBER_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Member>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from members where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException {
		try {
			if (Compatibility.isPostgreSql()) {
				return jdbc.queryForInt("select count(*) from members where vo_id=? and status=?", vo.getId(), String.valueOf(status.getCode()));
			} else {
				return jdbc.queryForInt("select count(*) from members where vo_id=? and status=?", vo.getId(), status.getCode());
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public int storePasswordResetRequest(PerunSession sess, User user, String namespace) throws InternalErrorException {

		int newId = Utils.getNewId(jdbc, "pwdreset_id_seq");

		jdbc.update("insert into pwdreset (id, namespace, user_id, created_by, created_by_uid, created_at) "
						+ "values (?,?,?,?,?," + Compatibility.getSysdate() + ")",
				newId, namespace, user.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());

		return newId;

	}

}
