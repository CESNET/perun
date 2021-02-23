package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.NamespaceRules;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NamespaceRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SponsorshipDoesNotExistException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;
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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MembersManagerImpl implements MembersManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(MembersManagerImpl.class);

	final static String memberMappingSelectQuery = "members.id as members_id, members.user_id as members_user_id, members.vo_id as members_vo_id, members.status as members_status, " +
			"members.sponsored as members_sponsored, " +
			"members.created_at as members_created_at, members.created_by as members_created_by, members.modified_by as members_modified_by, members.modified_at as members_modified_at, " +
			"members.created_by_uid as members_created_by_uid, members.modified_by_uid as members_modified_by_uid";

	final static String groupsMembersMappingSelectQuery = memberMappingSelectQuery + ", groups_members.membership_type as membership_type, " +
			"groups_members.source_group_id as source_group_id, groups_members.source_group_status as source_group_status, groups_members.group_id as group_id";

	final static String memberSponsorshipSelectQuery = "members_sponsored.active as members_sponsored_active, " +
			"members_sponsored.sponsored_id as members_sponsored_sponsored_id, " +
			"members_sponsored.sponsor_id as members_sponsored_sponsor_id, " +
			"members_sponsored.validity_to as members_sponsored_validity_to";

	private final JdbcPerunTemplate jdbc;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private SponsoredAccountsConfigLoader sponsoredAccountsConfigLoader;
	private SponsoredAccountsConfigContainer sponsoredAccountsConfigContainer = new SponsoredAccountsConfigContainer();

	/**
	 * Simple member mapper.
	 *
	 * Use with `memberMappingSelectQuery`
	 */
	static final RowMapper<Member> MEMBER_MAPPER = (rs, i) -> {
		Member member = new Member(rs.getInt("members_id"), rs.getInt("members_user_id"), rs.getInt("members_vo_id"), Status.getStatus(rs.getInt("members_status")),
				rs.getString("members_created_at"), rs.getString("members_created_by"), rs.getString("members_modified_at"), rs.getString("members_modified_by"),
				rs.getInt("members_created_by_uid") == 0 ? null : rs.getInt("members_created_by_uid"),
				rs.getInt("members_modified_by_uid") == 0 ? null : rs.getInt("members_modified_by_uid"));
		member.setSponsored(rs.getBoolean("members_sponsored"));
		return member;
	};

	static final RowMapper<Sponsorship> MEMBER_SPONSORSHIP_MAPPER = (rs, i) -> {
		Sponsorship ms = new Sponsorship();
		ms.setActive(rs.getBoolean("members_sponsored_active"));
		ms.setSponsoredId(rs.getInt("members_sponsored_sponsored_id"));
		ms.setSponsorId(rs.getInt("members_sponsored_sponsor_id"));
		Date validityTo = rs.getDate("members_sponsored_validity_to");
		if (validityTo != null) {
			ms.setValidityTo(validityTo.toLocalDate());
		}
		return ms;
	};

	/**
	 * Member mapper that also maps member's group statues.
	 *
	 * Use with `groupsMembersMappingSelectQuery`.
	 */
	static final RowMapper<Member> MEMBER_MAPPER_WITH_GROUP = (rs, i) -> {
		Member member = MEMBER_MAPPER.mapRow(rs, i);
		if (member != null) {
			member.putGroupStatus(rs.getInt("group_id"), MemberGroupStatus.getMemberGroupStatus(rs.getInt("source_group_status")));
			member.setMembershipType(MembershipType.getMembershipType(rs.getInt("membership_type")));
			member.setSourceGroupId(rs.getInt("source_group_id"));
		}
		return member;
	};

	public static final String A_D_MEMBER_MAIl = AttributesManager.NS_MEMBER_ATTR_DEF + ":mail";
	public static final String A_D_USER_PREFERRED_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";

	/**
	 * Member extractor that also sets correctly all member group statues.
	 *
	 * Use with `groupsMembersMappingSelectQuery`
	 */
	public static final ResultSetExtractor<List<Member>> MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR = resultSet -> {
		Map<Integer, Member> members = new HashMap<>();

		while(resultSet.next()) {
			Member member = MembersManagerImpl.MEMBER_MAPPER_WITH_GROUP.mapRow(resultSet, resultSet.getRow());
			if (member != null) {
				if (members.containsKey(member.getId())) {
					members.get(member.getId()).putGroupStatuses(member.getGroupStatuses());
				} else {
					member.setSourceGroupId(null);
					member.setMembershipType((String) null);
					members.put(member.getId(), member);
				}
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
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	/**
	 * Load all namespaces rules for sponsored accounts
	 *
	 */
	public void initialize() {
		this.sponsoredAccountsConfigLoader.loadSponsoredAccountsConfig();
		sponsoredAccountsConfigContainer.setNamespacesRules(this.sponsoredAccountsConfigLoader.loadSponsoredAccountsConfig());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user) throws AlreadyMemberException {
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
	public Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws MemberNotExistsException {
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
	public List<Member> getMembersByUser(PerunSession sess, User user) {
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
	public List<Member> getMembersByUserWithStatus(PerunSession sess, User user, Status status) {
		try {
			return jdbc.query("SELECT " + memberMappingSelectQuery + " FROM" +
							" members WHERE members.user_id=? and members.status"+Compatibility.castToInteger()+"=?",
					MEMBER_MAPPER, user.getId(), status.getCode());
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException(new MemberNotExistsException("user=" + user));
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Member getMemberById(PerunSession sess, int id) throws MemberNotExistsException {
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
	public List<Member> getMembersByIds(PerunSession perunSession, List<Integer> ids) {
		try {
			return jdbc.execute("select " + memberMappingSelectQuery + " from members where id " + Compatibility.getStructureForInClause(),
				(PreparedStatementCallback<List<Member>>) preparedStatement -> {
					Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(ids, preparedStatement);
					preparedStatement.setArray(1, sqlArray);
					ResultSet rs = preparedStatement.executeQuery();
					List<Member> members = new ArrayList<>();
					while (rs.next()) {
						members.add(MEMBER_MAPPER.mapRow(rs, rs.getRow()));
					}
					return members;
				});
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteMember(final PerunSession sess, final Member member) throws MemberAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("DELETE FROM members WHERE id=?", member.getId());
			if (numAffected == 0) throw new MemberAlreadyRemovedException("Member: " + member);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public int getMemberVoId(PerunSession sess, Member member) {
		try {
			return jdbc.queryForInt("select vo_id from members where id=?", member.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource userExtSource) throws MemberNotExistsException {
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
	public boolean memberExists(PerunSession sess, Member member) {
		Utils.notNull(member, "member");
		try {
			int number = jdbc.queryForInt("select count(1) from members where id=?", member.getId());
			if (number == 1) {
				return true;
			} else if (number > 1) {
				throw new ConsistencyErrorException("Member " + member + " exists more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkMemberExists(PerunSession sess, Member member) throws MemberNotExistsException {
		if (!memberExists(sess, member)) throw new MemberNotExistsException("Member: " + member);
	}

	@Override
	public void setStatus(PerunSession sess, Member member, Status status) {
		try {
			jdbc.update("update members set status=?, modified_by=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", status.getCode(), sess.getPerunPrincipal().getActor(), member.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) {
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
	public List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) {
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
	public int getMembersCount(PerunSession sess, Vo vo) {
		try {
			return jdbc.queryForInt("select count(*) from members where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo, Status status) {
		try {
			return jdbc.queryForInt("select count(*) from members where vo_id=? and status=?", vo.getId(), status.getCode());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int storePasswordResetRequest(PerunSession sess, User user, String namespace, String mail, LocalDateTime validityTo) {

		int newId = Utils.getNewId(jdbc, "pwdreset_id_seq");

		try {
			jdbc.update("insert into pwdreset (id, namespace, user_id, mail, validity_to, created_by, created_by_uid, created_at) "
							+ "values (?,?,?,?,?,?,?," + Compatibility.getSysdate() + ")",
					newId, namespace, user.getId(), mail, validityTo, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return newId;

	}

	@Override
	public Member createSponsoredMember(PerunSession session, Vo vo, User sponsored, User sponsor, LocalDate validityTo) throws AlreadyMemberException, AlreadySponsorException {
		Member sponsoredMember = this.createMember(session, vo, sponsored);
		return setSponsorshipForMember(session, sponsoredMember, sponsor, validityTo);
	}

	@Override
	public Member setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws AlreadySponsorException {
		sponsoredMember.setSponsored(true);
		try {
			jdbc.update("UPDATE members SET sponsored="+Compatibility.getTrue()+" WHERE id=?", sponsoredMember.getId());
			this.addSponsor(session, sponsoredMember, sponsor, validityTo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return sponsoredMember;
	}

	@Override
	public Member unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) {
		sponsoredMember.setSponsored(false);
		try {
			jdbc.update("UPDATE members SET sponsored="+Compatibility.getFalse()+" WHERE id=?", sponsoredMember.getId());
			this.deleteAllSponsors(session, sponsoredMember);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return sponsoredMember;
	}

	@Override
	public void addSponsor(PerunSession session, Member sponsoredMember, User sponsor) throws AlreadySponsorException {
		addSponsor(session, sponsoredMember, sponsor, null);
	}

	@Override
	public void addSponsor(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws AlreadySponsorException {
		try {
			PerunPrincipal pp = session.getPerunPrincipal();

			try {
				// check if there exists sponsorship between sponsoredMember and sponsor
				Sponsorship sponsorship = getSponsorship(session, sponsoredMember, sponsor);

				// if it exists and is inactive -> update (reactivate) it
				if (!sponsorship.isActive()) {
					jdbc.update("UPDATE members_sponsored SET active=?, validity_to=?, modified_by=?, " +
							"modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? WHERE sponsored_id=? AND sponsor_id=?",
						true, validityTo != null ? Timestamp.valueOf(validityTo.atStartOfDay()) : null, pp.getActor(),
						pp.getUserId(), sponsoredMember.getId(), sponsor.getId());
				} else { // if it exists and is active -> throw exception
					new AlreadySponsorException("member " + sponsoredMember.getId() + " is already sponsored by user " + sponsor.getId());
				}
			} catch (SponsorshipDoesNotExistException ex) {
				// if sponsorship doesn't exist -> insert it
				jdbc.update("INSERT INTO members_sponsored (" +
						"active," +
						"sponsored_id," +
						"sponsor_id," +
						"created_by," +
						"created_at," +
						"created_by_uid," +
						"modified_by," +
						"modified_at," +
						"modified_by_uid," +
						"validity_to) " +
						"VALUES (?,?,?,?," + Compatibility.getSysdate() + ",?,?," + Compatibility.getSysdate() + ",?,?)",
					true, sponsoredMember.getId(), sponsor.getId(), pp.getActor(), pp.getUserId(), pp.getActor(),
					pp.getUserId(), validityTo != null ? Timestamp.valueOf(validityTo.atStartOfDay()) : null);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeSponsor(PerunSession session, Member sponsoredMember, User sponsor) {
		try {
			PerunPrincipal pp = session.getPerunPrincipal();
			jdbc.update("UPDATE members_sponsored SET active=?, modified_by=?, modified_at="+Compatibility.getSysdate() +", modified_by_uid=? " +
							"WHERE sponsored_id=? AND sponsor_id=?" ,
					false, pp.getActor(), pp.getUserId(),sponsoredMember.getId(), sponsor.getId() );
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteAllSponsors(PerunSession session, Member sponsoredMember) {
		try {
			jdbc.update("DELETE FROM members_sponsored WHERE sponsored_id=?", sponsoredMember.getId() );
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public Sponsorship getSponsorship(PerunSession sess, Member sponsoredMember, User sponsor) throws SponsorshipDoesNotExistException {
		try {
			return jdbc.queryForObject("SELECT " + memberSponsorshipSelectQuery + " FROM members_sponsored "
					+ " WHERE members_sponsored.sponsor_id=? AND members_sponsored.sponsored_id=?", MEMBER_SPONSORSHIP_MAPPER,
					sponsor.getId(), sponsoredMember.getId());
		} catch (EmptyResultDataAccessException ex) {
			throw new SponsorshipDoesNotExistException(sponsoredMember, sponsor);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo, User sponsor) {
		try {
			return jdbc.query("SELECT "+memberMappingSelectQuery+" FROM members JOIN members_sponsored ms ON (members.id=ms.sponsored_id) " +
					"WHERE members.vo_id=? AND ms.active=? AND ms.sponsor_id=?", MEMBER_MAPPER, vo.getId(), true, sponsor.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, User sponsor) {
		try {
			return jdbc.query("SELECT "+memberMappingSelectQuery+" FROM members JOIN members_sponsored ms ON (members.id=ms.sponsored_id) " +
					"WHERE ms.active=? AND ms.sponsor_id=?", MEMBER_MAPPER, true, sponsor.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo) {
		try {
			return jdbc.query("SELECT DISTINCT "+memberMappingSelectQuery+" FROM members JOIN members_sponsored ms ON (members.id=ms.sponsored_id) " +
			        "WHERE members.vo_id=? AND ms.active=?", MEMBER_MAPPER, vo.getId(), true);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteSponsorLinks(PerunSession sess, Member sponsoredMember) {
		try {
			jdbc.update("DELETE FROM members_sponsored WHERE sponsored_id=?", sponsoredMember.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, Member member, Resource resource) {

		try {
			Member result = jdbc.queryForObject("select distinct " + MembersManagerImpl.groupsMembersMappingSelectQuery +
							" from groups_resources join groups on groups_resources.group_id=groups.id" +
					" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
					" where groups_resources.resource_id=? and members.id=?",
					MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, resource.getId(),
					member.getId());
			return result.getGroupStatus();
		} catch (EmptyResultDataAccessException ex) {
			return null;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, User user, Facility facility) {

		try {

			List<Member> result = jdbc.query("select distinct " + MembersManagerImpl.groupsMembersMappingSelectQuery +
							" from groups_resources join groups on groups_resources.group_id=groups.id" +
							" join groups_members on groups.id=groups_members.group_id join members on groups_members.member_id=members.id " +
							" join resources on groups_resources.resource_id=resources.id " +
							" where resources.facility_id=? and members.user_id=?",
					MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, facility.getId(),
					user.getId());

			if (result != null && !result.isEmpty()) {

				for (Member member : result) {
					if (Objects.equals(MemberGroupStatus.VALID, member.getGroupStatus())) {
						// is active at least by one member
						return member.getGroupStatus();
					}
				}
				return MemberGroupStatus.EXPIRED;

			} else {
				return null;
			}

		} catch (EmptyResultDataAccessException ex) {
			return null;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<Member> findMembers(PerunSession sess, Vo vo, String searchString, boolean onlySponsored) {

		String voIdQueryString = "";
		if(vo != null) {
			voIdQueryString = " members.vo_id=" + vo.getId() + " and ";
		}

		String sponsoredQueryString = "";
		if(onlySponsored) {
			//only sponsored members
			sponsoredQueryString+=" members.sponsored=true and ";
		}

		String userNameQueryString = Utils.prepareUserSearchQuerySimilarMatch();

		String idQueryString = "";
		try {
			int id = Integer.parseInt(searchString);
			idQueryString = " members.user_id=" + id + " or members.id=" + id + " or ";
		} catch (NumberFormatException e) {
			// IGNORE wrong format of ID
		}

		// Divide attributes received from CoreConfig into member, user and userExtSource attributes
		Map<String, List<String>> attributesToSearchBy = Utils.getDividedAttributes();

		// Parts of query to search by attributes
		Map<String, Pair<String, String>> attributesToSearchByQueries = Utils.getAttributesQuery(attributesToSearchBy.get("memberAttributes"), attributesToSearchBy.get("userAttributes"), attributesToSearchBy.get("uesAttributes"));

		MapSqlParameterSource namedParams = Utils.getMapSqlParameterSourceToSearchUsersOrMembers(searchString, attributesToSearchBy);

		//searching by member attributes
		//searching by user attributes
		//searching by login in userExtSources
		//searching by userExtSource attributes
		//searching by name for user
		//searching by user and member id
		Set<Member> members = new HashSet<>(namedParameterJdbcTemplate.query("select distinct " + memberMappingSelectQuery +
				" from members " +
				" left join users on members.user_id=users.id " +
				" left join user_ext_sources ues on ues.user_id=users.id " +
				attributesToSearchByQueries.get("memberAttributesQuery").getLeft() +
				attributesToSearchByQueries.get("userAttributesQuery").getLeft() +
				attributesToSearchByQueries.get("uesAttributesQuery").getLeft() +
				" where " +
				voIdQueryString +
				sponsoredQueryString +
				" ( " +
				" lower(ues.login_ext)=lower(:searchString) or " +
				attributesToSearchByQueries.get("memberAttributesQuery").getRight() +
				attributesToSearchByQueries.get("userAttributesQuery").getRight() +
				attributesToSearchByQueries.get("uesAttributesQuery").getRight() +
				idQueryString +
				userNameQueryString +
				" ) ", namedParams, MEMBER_MAPPER));

		if (vo != null) {
			log.debug("Searching members of VO '{}' using searchString '{}', sponsored '{}'. Found: {} member(s).", vo.getShortName(), searchString, onlySponsored, members.size());
		} else {
			log.debug("Searching all members using searchString '{}', sponsored '{}'. Found: {} member(s).", searchString, onlySponsored, members.size());
		}

		return new ArrayList<>(members);
	}

	@Override
	public void updateSponsorshipValidity(PerunSession sess, Member sponsoredMember, User sponsor,
	                                      LocalDate newValidity) throws SponsorshipDoesNotExistException {
		int rows = jdbc.update("UPDATE members_sponsored SET validity_to=? WHERE sponsored_id=? AND sponsor_id=?",
				newValidity != null ? Timestamp.valueOf(newValidity.atStartOfDay()) : null,
				sponsoredMember.getId(),
				sponsor.getId());
		if (rows == 0) {
			throw new SponsorshipDoesNotExistException(sponsoredMember, sponsor);
		}
	}

	@Override
	public List<Sponsorship> getSponsorshipsExpiringInRange(PerunSession sess, LocalDate from, LocalDate to) {
		return jdbc.query("SELECT " + memberSponsorshipSelectQuery + " FROM members_sponsored WHERE active=? AND " +
						"validity_to >= ? AND validity_to < ?",
				MEMBER_SPONSORSHIP_MAPPER, true, from, to);
	}

	@Override
	public void moveMembersApplications(PerunSession sess, Member sourceMember, Member targetMember) {
		try {
			jdbc.update("update application set user_id=?, modified_at=" + Compatibility.getSysdate() + ", modified_by_uid=? where user_id=? and vo_id=?",
				targetMember.getUserId(), sess.getPerunPrincipal().getUserId(), sourceMember.getVoId(), sourceMember.getVoId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void setSponsoredAccountsConfigLoader(SponsoredAccountsConfigLoader sponsoredAccountsConfigLoader) {
		this.sponsoredAccountsConfigLoader = sponsoredAccountsConfigLoader;
	}

	@Override
	public List<NamespaceRules> getAllNamespacesRules() {
		return sponsoredAccountsConfigContainer.getAllNamespacesRules();
	}

	@Override
	public NamespaceRules getNamespaceRules(String namespace) throws NamespaceRulesNotExistsException {
		return sponsoredAccountsConfigContainer.getNamespaceRules(namespace);
	}
}
