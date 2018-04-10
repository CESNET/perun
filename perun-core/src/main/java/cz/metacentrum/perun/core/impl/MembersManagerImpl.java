package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MembersManagerImpl implements MembersManagerImplApi {

	final static String memberMappingSelectQuery = "members.id as members_id, members.user_id as members_user_id, members.vo_id as members_vo_id, members.status as members_status, " +
			"members.sponsored as members_sponsored, " +
			"members.created_at as members_created_at, members.created_by as members_created_by, members.modified_by as members_modified_by, members.modified_at as members_modified_at, " +
			"members.created_by_uid as members_created_by_uid, members.modified_by_uid as members_modified_by_uid";

	final static String groupsMembersMappingSelectQuery = memberMappingSelectQuery + ", groups_members.membership_type as membership_type, " +
			"groups_members.source_group_id as source_group_id, groups_members.source_group_status as source_group_status, groups_members.group_id as group_id";

	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	static final RowMapper<Member> MEMBER_MAPPER = (rs, i) -> {
		Member member = new Member(rs.getInt("members_id"), rs.getInt("members_user_id"), rs.getInt("members_vo_id"), Status.getStatus(rs.getInt("members_status")),
				rs.getString("members_created_at"), rs.getString("members_created_by"), rs.getString("members_modified_at"), rs.getString("members_modified_by"),
				rs.getInt("members_created_by_uid") == 0 ? null : rs.getInt("members_created_by_uid"),
				rs.getInt("members_modified_by_uid") == 0 ? null : rs.getInt("members_modified_by_uid"));
		member.setSponsored(rs.getBoolean("members_sponsored"));
		try {
			member.addGroupStatus(rs.getInt("group_id"), MemberGroupStatus.getMemberGroupStatus(rs.getInt("source_group_status")));
			member.setMembershipType(MembershipType.getMembershipType(rs.getInt("membership_type")));
			member.setSourceGroupId(rs.getInt("source_group_id"));
		} catch (SQLException ex) {
			// this is ok, member does not need to always have membership_type and source_group_id set
		}
		return member;
	};


	public static final ResultSetExtractor<List<Member>> MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR = resultSet -> {
		Map<Integer, Member> members = new HashMap<>();

		while(resultSet.next()) {
			Member member = MembersManagerImpl.MEMBER_MAPPER.mapRow(resultSet, resultSet.getRow());
			if (members.containsKey(member.getId())) {
				members.get(member.getId()).addGroupStatuses(member.getGroupStatuses());
			} else {
				member.setSourceGroupId(null);
				member.setMembershipType((String)null);
				members.put(member.getId(), member);
			}
		}

		return new ArrayList<>(members.values());
	};

	/**
	 * Constructor
	 */
	public MembersManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException {
		Member member;
		try {
			// Set the new Member id
			int newId = Utils.getNewId(jdbc, "members_id_seq");

			jdbc.update("insert into members (id, vo_id, user_id, status, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) "
							+ "values (?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
					newId, vo.getId(),
					user.getId(), Status.INVALID.getCode(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

			member = new Member(newId, user.getId(), vo.getId(), Status.INVALID);

		} catch (DuplicateKeyException e) {
			throw new AlreadyMemberException(e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return member;
	}

	@Override
	public Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws InternalErrorException, MemberNotExistsException {
		try {
			return jdbc.queryForObject("SELECT " + memberMappingSelectQuery + " FROM" +
							" members WHERE members.user_id=? AND members.vo_id=?",
					MEMBER_MAPPER, userId, vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("user id " + userId + " is not member of VO " + vo);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException {
		try {
			return jdbc.query("SELECT " + memberMappingSelectQuery + " FROM" +
							" members WHERE members.user_id=?",
					MEMBER_MAPPER, user.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException(new MemberNotExistsException("user=" + user));
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException {
		try {
			return jdbc.queryForObject("SELECT " + memberMappingSelectQuery + " FROM members "
					+ " WHERE members.id=?", MEMBER_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("member id=" + id);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void deleteMember(final PerunSession sess, final Member member) throws InternalErrorException, MemberAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("DELETE FROM members WHERE id=?", member.getId());
			if (numAffected == 0) throw new MemberAlreadyRemovedException("Member: " + member);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public int getMemberVoId(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select vo_id from members where id=?", member.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource userExtSource) throws InternalErrorException, MemberNotExistsException {
		try {
			return jdbc.queryForObject("SELECT " + memberMappingSelectQuery + " FROM members, user_ext_sources WHERE " +
							"user_ext_sources.login_ext=? AND user_ext_sources.ext_sources_id=? AND members.vo_id=? AND members.user_id=user_ext_sources.user_id",
					MEMBER_MAPPER, userExtSource.getLogin(), userExtSource.getExtSource().getId(), vo.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new MemberNotExistsException("member userExtSource=" + userExtSource);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public boolean memberExists(PerunSession sess, Member member) throws InternalErrorException {
		Utils.notNull(member, "member");
		try {
			return 1 == jdbc.queryForInt("select 1 from members where id=?", member.getId());
		} catch (EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkMemberExists(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		if (!memberExists(sess, member)) throw new MemberNotExistsException("Member: " + member);
	}

	@Override
	public void setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException {
		try {
			jdbc.update("update members set status=?, modified_by=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", status.getCode(), sess.getPerunPrincipal().getActor(), member.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) throws InternalErrorException {
		// If usersIds is empty, we can immediatelly return empty results
		if (usersIds.size() == 0) {
			return new ArrayList<>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", usersIds);
		parameters.addValue("vo", vo.getId());

		try {
			return this.namedParameterJdbcTemplate.query("SELECT " + memberMappingSelectQuery +
							" FROM members JOIN users ON members.user_id=users.id WHERE members.user_id IN ( :ids ) AND members.vo_id=:vo " +
							"ORDER BY users.last_name, users.first_name",
					parameters, MEMBER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) throws InternalErrorException {
		// If usersIds is empty, we can immediatelly return empty results
		if (users.size() == 0) {
			return new ArrayList<>();
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		Set<Integer> usersIds = new HashSet<>();
		for (User user : users) {
			usersIds.add(user.getId());
		}
		parameters.addValue("ids", usersIds);
		parameters.addValue("vo", vo.getId());

		try {
			return this.namedParameterJdbcTemplate.query("SELECT " + memberMappingSelectQuery + " FROM members WHERE members.user_id IN ( :ids ) AND members.vo_id=:vo",
					parameters, MEMBER_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from members where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
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

	@Override
	public int storePasswordResetRequest(PerunSession sess, User user, String namespace) throws InternalErrorException {

		int newId = Utils.getNewId(jdbc, "pwdreset_id_seq");

		try {
			jdbc.update("insert into pwdreset (id, namespace, user_id, created_by, created_by_uid, created_at) "
							+ "values (?,?,?,?,?," + Compatibility.getSysdate() + ")",
					newId, namespace, user.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return newId;

	}

	@Override
	public Member createSponsoredMember(PerunSession session, Vo vo, User sponsored, User sponsor) throws AlreadyMemberException, InternalErrorException {
		Member sponsoredMember = this.createMember(session, vo, sponsored);
		sponsoredMember.setSponsored(true);
		try {
			jdbc.update("UPDATE members SET sponsored="+Compatibility.getTrue()+" WHERE id=?", sponsoredMember.getId());
			this.addSponsor(session, sponsoredMember, sponsor);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return sponsoredMember;
	}

	@Override
	public void addSponsor(PerunSession session, Member sponsoredMember, User sponsor) throws InternalErrorException {
		try {
			PerunPrincipal pp = session.getPerunPrincipal();
			jdbc.update("INSERT INTO members_sponsored (active,sponsored_id,sponsor_id,created_by,created_at,created_by_uid,modified_by,modified_at,modified_by_uid) " +
					"VALUES ('1' ,?,?,?," + Compatibility.getSysdate() + ",?,?,"+ Compatibility.getSysdate() + ",?)" ,
					sponsoredMember.getId(), sponsor.getId(), pp.getActor(), pp.getUserId(),pp.getActor(), pp.getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeSponsor(PerunSession session, Member sponsoredMember, User sponsor) throws InternalErrorException {
		try {
			PerunPrincipal pp = session.getPerunPrincipal();
			jdbc.update("UPDATE members_sponsored SET active='0',modified_by=?,modified_at="+Compatibility.getSysdate() +",modified_by_uid=? " +
							"WHERE sponsored_id=? AND sponsor_id=?" ,
					pp.getActor(), pp.getUserId(),sponsoredMember.getId(), sponsor.getId() );
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo, User sponsor) throws InternalErrorException {
		try {
			return jdbc.query("SELECT "+memberMappingSelectQuery+" FROM members JOIN members_sponsored ms ON (members.id=ms.sponsored_id) " +
					"WHERE members.vo_id=? AND ms.active='1' AND ms.sponsor_id=?", MEMBER_MAPPER, vo.getId(), sponsor.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("SELECT "+memberMappingSelectQuery+" FROM members JOIN members_sponsored ms ON (members.id=ms.sponsored_id) " +
			        "WHERE members.vo_id=? AND ms.active='1'", MEMBER_MAPPER, vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteSponsorLinks(PerunSession sess, Member sponsoredMember) throws InternalErrorException {
		try {
			jdbc.update("DELETE FROM members_sponsored WHERE sponsored_id=?", sponsoredMember.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

}
