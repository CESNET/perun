package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.EnrichedBanOnVo;
import cz.metacentrum.perun.core.api.EnrichedVo;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VosManager entry logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerEntry implements VosManager {

  private static final Logger LOG = LoggerFactory.getLogger(VosManagerEntry.class);

  private PerunBl perunBl;
  private VosManagerBl vosManagerBl;

  /**
   * Constructor.
   */
  public VosManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.vosManagerBl = this.perunBl.getVosManagerBl();
  }

  public VosManagerEntry() {
  }

  @Override
  public void addAdmin(PerunSession sess, Vo vo, User user)
          throws PrivilegeException, AlreadyAdminException, VoNotExistsException, UserNotExistsException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getUsersManagerBl().checkUserExists(sess, user);

    AuthzResolver.setRole(sess, user, vo, Role.VOADMIN);
  }

  @Override
  public void addAdmin(PerunSession sess, Vo vo, Group group)
          throws PrivilegeException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

    AuthzResolver.setRole(sess, group, vo, Role.VOADMIN);
  }

  @Override
  public void addMemberVo(PerunSession sess, Vo vo, Vo memberVo)
      throws RelationExistsException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    vosManagerBl.checkVoExists(sess, memberVo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "result-addMemberVo_Vo_Vo_policy", vo) ||
            !AuthzResolver.authorizedInternal(sess, "operand-addMemberVo_Vo_Vo_policy", memberVo)) {
      throw new PrivilegeException("addMemberVo");
    }

    vosManagerBl.addMemberVo(sess, vo, memberVo);
  }

  /**
   * Adds role SPONSOR for user in a VO.
   */
  @Override
  public void addSponsorRole(PerunSession sess, Vo vo, User user)
          throws AlreadyAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getUsersManagerBl().checkUserExists(sess, user);

    LOG.debug("addSponsorRole({},{})", vo.getShortName(), user.getId());

    AuthzResolver.setRole(sess, user, vo, Role.SPONSOR);
  }

  /**
   * Adds role SPONSOR for group in a VO.
   */
  @Override
  public void addSponsorRole(PerunSession sess, Vo vo, Group group)
          throws AlreadyAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

    AuthzResolver.setRole(sess, group, vo, Role.SPONSOR);
  }

  @Override
  public void convertSponsoredUsers(PerunSession sess, Vo vo) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "convertSponsoredUsers_Vo_policy", vo)) {
      throw new PrivilegeException("convertSponsoredUsers");
    }

    vosManagerBl.convertSponsoredUsers(sess, vo);
  }

  @Override
  public void convertSponsoredUsersWithNewSponsor(PerunSession sess, Vo vo, User newSponsor) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "convertSponsoredUsersWithNewSponsor_Vo_User_policy", vo, newSponsor)) {
      throw new PrivilegeException("convertSponsoredUsersWithNewSponsor");
    }

    vosManagerBl.convertSponsoredUsersWithNewSponsor(sess, vo, newSponsor);
  }

  @Override
  public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");
    Utils.notNull(vo, "vo");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createVo_Vo_policy")) {
      throw new PrivilegeException(sess, "createVo");
    }

    if (vo.getName().length() > 128) {
      throw new IllegalArgumentException("VO name is too long, >128 characters");
    }

    if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,32}$")) {
      throw new IllegalArgumentException(
          "Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 32 characters.");
    }

    if (!vo.getShortName().equals(vo.getShortName().trim())) {
      throw new IllegalArgumentException("Wrong VO short name - cannot contain leading and trailing spaces");
    }

    return vosManagerBl.createVo(sess, vo);
  }

  @Override
  public void deleteVo(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");

    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteVo_Vo_policy", vo)) {
      throw new PrivilegeException(sess, "deleteVo");
    }

    vosManagerBl.deleteVo(sess, vo);
  }

  @Override
  public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");

    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteVo_Vo_boolean_policy", vo)) {
      throw new PrivilegeException(sess, "deleteVo");
    }

    vosManagerBl.deleteVo(sess, vo, forceDelete);
  }

  /**
   * Filters member candidates which were found via extSource of given group / vo or principal has right to read them
   *
   * @param sess       session
   * @param candidates candidates
   * @param extSources extSources of group or vo where candidate is suggested
   * @return list of eligible candidates
   */
  private List<MemberCandidate> filterMemberCandidates(PerunSession sess, List<MemberCandidate> candidates,
                                                       List<ExtSource> extSources) {
    List<MemberCandidate> eligibleCandidates = new ArrayList<>();

    // check principal can see candidates
    for (MemberCandidate candidate : candidates) {
      if (candidate.getRichUser() == null) {
        if (candidate.getCandidate().getUserExtSources().stream().map(UserExtSource::getExtSource)
            .anyMatch(extSources::contains)) {
          eligibleCandidates.add(candidate);
        }
        continue;
      }

      if (candidate.getRichUser().getUserExtSources().stream().map(UserExtSource::getExtSource)
          .anyMatch(extSources::contains)) {
        // ext sources of candidate match some of vo's / group's ext sources
        eligibleCandidates.add(candidate);
        continue;
      }

      boolean isEligible;

      List<Vo> membersVos = perunBl.getUsersManagerBl().getVosWhereUserIsMember(sess, candidate.getRichUser());
      if (membersVos.isEmpty()) {
        isEligible = AuthzResolver.authorizedInternal(sess, "filter-getCompleteCandidates_policy");
      } else {
        isEligible = membersVos.stream()
            .anyMatch(vo -> AuthzResolver.authorizedInternal(sess, "filter-getCompleteCandidates_policy", vo));
      }

      List<Group> membersGroups = perunBl.getGroupsManagerBl().getUserGroups(sess, candidate.getRichUser());
      if (!membersGroups.isEmpty() && !isEligible) {
        isEligible = membersGroups.stream()
            .anyMatch(group -> AuthzResolver.authorizedInternal(sess, "filter-getCompleteCandidates_policy", group));
      }

      if (isEligible) {
        eligibleCandidates.add(candidate);
      }
    }

    return eligibleCandidates;
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults)
      throws VoNotExistsException, PrivilegeException {
    Utils.notNull(searchString, "searchString");
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "findCandidates_Vo_String_int_policy", vo)) {
      throw new PrivilegeException(sess, "findCandidates");
    }

    return vosManagerBl.findCandidates(sess, vo, searchString, maxNumOfResults);
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString)
      throws VoNotExistsException, PrivilegeException {
    Utils.notNull(searchString, "searchString");
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "findCandidates_Vo_String_policy", vo)) {
      throw new PrivilegeException(sess, "findCandidates");
    }

    return vosManagerBl.findCandidates(sess, vo, searchString);
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString)
      throws GroupNotExistsException, PrivilegeException {
    Utils.notNull(searchString, "searchString");
    Utils.notNull(sess, "sess");
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "findCandidates_Group_String_policy", group)) {
      throw new PrivilegeException(sess, "findCandidates");
    }

    return vosManagerBl.findCandidates(sess, group, searchString);
  }

  @Override
  public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, String role)
      throws PrivilegeException, VoNotExistsException, RoleNotSupportedException {
    Utils.checkPerunSession(perunSession);
    Utils.notNull(role, "role");
    vosManagerBl.checkVoExists(perunSession, vo);

    if (!AuthzResolver.roleExists(role)) {
      throw new RoleNotSupportedException("Role: " + role + " does not exists.", role);
    }

    //Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
    if (!role.equals(Role.TOPGROUPCREATOR) && !role.equals(Role.VOADMIN) && !role.equals(Role.SPONSOR) &&
        !role.equals(Role.VOOBSERVER)) {
      throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver, Sponsor and TopGroupCreator.",
          role);
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getAdminGroups_Vo_String_policy", vo)) {
      throw new PrivilegeException(perunSession, "getAdminGroups");
    }

    return vosManagerBl.getAdminGroups(perunSession, vo, role);
  }

  @Deprecated
  @Override
  public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getAdminGroups");
    }

    return vosManagerBl.getAdminGroups(sess, vo);
  }

  @Override
  public List<User> getAdmins(PerunSession perunSession, Vo vo, String role, boolean onlyDirectAdmins)
      throws PrivilegeException, VoNotExistsException, RoleNotSupportedException {
    Utils.checkPerunSession(perunSession);
    Utils.notNull(role, "role");
    vosManagerBl.checkVoExists(perunSession, vo);

    if (!AuthzResolver.roleExists(role)) {
      throw new RoleNotSupportedException("Role: " + role + " does not exists.", role);
    }

    //Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
    if (!role.equals(Role.TOPGROUPCREATOR) && !role.equals(Role.VOADMIN) && !role.equals(Role.VOOBSERVER)) {
      throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver and TopGroupCreator.", role);
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getAdmins_Vo_String_boolean_policy", vo)) {
      throw new PrivilegeException(perunSession, "getAdmins");
    }

    return vosManagerBl.getAdmins(perunSession, vo, role, onlyDirectAdmins);
  }

  @Override
  @Deprecated
  public List<User> getAdmins(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getAdmins");
    }

    return vosManagerBl.getAdmins(sess, vo);
  }

  @Override
  public List<Vo> getAllVos(PerunSession perunSession) throws PrivilegeException {
    Utils.notNull(perunSession, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getAllVos_policy")) {
      throw new PrivilegeException(perunSession, "getAllVos");
    }
    return vosManagerBl.getVos(perunSession);
  }

  @Override
  public BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    BanOnVo ban = vosManagerBl.getBanById(sess, banId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "vo-getBanById_int_policy", ban)) {
      throw new PrivilegeException("getBanById");
    }

    return ban;
  }

  @Override
  public BanOnVo getBanForMember(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    Optional<BanOnVo> ban = vosManagerBl.getBanForMember(sess, member.getId());

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "vo-getBanForMember_member_policy", member)) {
      throw new PrivilegeException("getBanForMember");
    }

    return ban.orElse(null);
  }

  @Override
  public List<BanOnVo> getBansForVo(PerunSession sess, int voId) throws PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    Vo vo = vosManagerBl.getVoById(sess, voId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getBansForVo_int_policy", vo)) {
      throw new PrivilegeException("getBansForVo");
    }

    return vosManagerBl.getBansForVo(sess, voId);
  }

  @Override
  public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames,
                                                     String searchString)
      throws VoNotExistsException, PrivilegeException {
    Utils.notNull(searchString, "searchString");
    Utils.notNull(sess, "sess");
    Utils.notNull(vo, "vo");
    Utils.notNull(attrNames, "attrNames");

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    searchString = searchString.trim();

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getCompleteCandidates_Vo_List<String>_String_policy", vo)) {
      throw new PrivilegeException(sess, "getCompleteCandidates");
    }

    List<MemberCandidate> candidates = vosManagerBl.getCompleteCandidates(sess, vo, attrNames, searchString);

    List<ExtSource> voExtSources = perunBl.getExtSourcesManagerBl().getVoExtSources(sess, vo);

    return filterMemberCandidates(sess, candidates, voExtSources);
  }

  @Override
  public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Group group, List<String> attrNames,
                                                     String searchString)
      throws GroupNotExistsException, PrivilegeException {
    Utils.notNull(searchString, "searchString");
    Utils.notNull(sess, "sess");
    Utils.notNull(group, "group");
    Utils.notNull(attrNames, "attrNames");

    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

    searchString = searchString.trim();

    List<ExtSource> extSources;

    Vo vo = getPerunBl().getGroupsManagerBl().getVo(sess, group);

    // Authorization
    if (AuthzResolver.authorizedInternal(sess, "getCompleteCandidates_Group_List<String>_String_policy", vo)) {
      extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);

      // null the vo so users are searched in whole perun
      vo = null;
    } else if (AuthzResolver.authorizedInternal(sess,
        "groupExtSource-getCompleteCandidates_Group_List<String>_String_policy", group)) {
      extSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
    } else {
      throw new PrivilegeException(sess, "getCompleteCandidates");
    }

    List<MemberCandidate> candidates =
        vosManagerBl.getCompleteCandidates(sess, vo, group, attrNames, searchString, extSources);

    return filterMemberCandidates(sess, candidates, extSources);
  }

  @Deprecated
  @Override
  public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getDirectAdmins");
    }

    return vosManagerBl.getDirectAdmins(sess, vo);
  }

  @Override
  @Deprecated
  public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo,
                                                                  List<String> specificAttributes)
      throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getDirectRichAdminsWithSpecificAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        vosManagerBl.getDirectRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
  }

  public List<EnrichedBanOnVo> getEnrichedBansForUser(PerunSession sess, int userId, List<String> attrNames)
      throws PrivilegeException, UserNotExistsException, AttributeNotExistsException {
    Utils.checkPerunSession(sess);

    User user = perunBl.getUsersManagerBl().getUserById(sess, userId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getEnrichedVoBansForUser_int_List<String>_policy", user)) {
      throw new PrivilegeException(sess, "getEnrichedBansForUser");
    }

    List<EnrichedBanOnVo> bans = vosManagerBl.getEnrichedBansForUser(sess, userId, attrNames);
    bans.forEach(
        ban -> ban.setMember(perunBl.getMembersManagerBl().filterOnlyAllowedAttributes(sess, ban.getMember())));
    return bans;
  }

  public List<EnrichedBanOnVo> getEnrichedBansForVo(PerunSession sess, int voId, List<String> attrNames)
      throws PrivilegeException, AttributeNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    Vo vo = vosManagerBl.getVoById(sess, voId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getEnrichedVoBansForVo_int_List<String>_policy", vo)) {
      throw new PrivilegeException(sess, "getEnrichedBansForVo");
    }
    List<EnrichedBanOnVo> bans = vosManagerBl.getEnrichedBansForVo(sess, vo, attrNames);
    bans.forEach(
        ban -> ban.setMember(perunBl.getMembersManagerBl().filterOnlyAllowedAttributes(sess, ban.getMember())));
    return bans;
  }

  @Override
  public EnrichedVo getEnrichedVoById(PerunSession sess, int id) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");
    EnrichedVo enrichedVo = vosManagerBl.getEnrichedVoById(sess, id);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getEnrichedVoById_int_policy", enrichedVo.getVo())) {
      throw new PrivilegeException(sess, "getEnrichedVoById");
    }

    return enrichedVo;
  }

  @Override
  public List<EnrichedVo> getEnrichedVos(PerunSession sess) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getEnrichedVos_policy")) {
      throw new PrivilegeException(sess, "getEnrichedVos");
    } else {
      List<EnrichedVo> tempVos = vosManagerBl.getEnrichedVos(sess);
      tempVos.removeIf(enrichedVo -> {
        try {
          return !AuthzResolver.authorizedInternal(sess, "filter-getEnrichedVos_policy", enrichedVo.getVo());
        } catch (InternalErrorException e) {
          // if we can't determine authorization prevent returning it
          return true;
        }
      });

      return tempVos;
    }
  }

  @Override
  public List<Vo> getMemberVos(PerunSession sess, int voId) throws PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    Vo vo = vosManagerBl.getVoById(sess, voId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getMemberVos_int_policy", vo)) {
      throw new PrivilegeException("getMemberVos");
    }

    return vosManagerBl.getMemberVos(sess, voId);
  }

  @Override
  public List<Vo> getParentVos(PerunSession sess, int memberVoId) throws PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    Vo memberVo = vosManagerBl.getVoById(sess, memberVoId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getParentVos_int_policy", memberVo)) {
      throw new PrivilegeException("getParentVos");
    }

    return vosManagerBl.getParentVos(sess, memberVoId);
  }

  /**
   * Gets the perunBl for this instance.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, String role, List<String> specificAttributes,
                                      boolean allUserAttributes, boolean onlyDirectAdmins)
      throws PrivilegeException, VoNotExistsException, UserNotExistsException, RoleNotSupportedException {
    Utils.notNull(perunSession, "perunSession");
    Utils.notNull(role, "role");
    vosManagerBl.checkVoExists(perunSession, vo);

    if (!AuthzResolver.roleExists(role)) {
      throw new RoleNotSupportedException("Role: " + role + " does not exists.", role);
    }

    //Role can be only supported one (TopGroupCreator, VoAdmin or VoObserver)
    if (!role.equals(Role.TOPGROUPCREATOR) && !role.equals(Role.VOADMIN) && !role.equals(Role.VOOBSERVER) &&
        !role.equals(Role.SPONSOR)) {
      throw new RoleNotSupportedException("Supported roles are VoAdmin, VoObserver, Sponsor and TopGroupCreator.",
          role);
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(perunSession, "getRichAdmins_Vo_String_List<String>_boolean_boolean_policy",
        vo)) {
      throw new PrivilegeException(perunSession, "getDirectRichAdminsWithSpecificAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession,
        vosManagerBl.getRichAdmins(perunSession, vo, role, specificAttributes, allUserAttributes, onlyDirectAdmins));
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdmins(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getRichAdmins");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdmins(sess, vo));
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Vo vo)
      throws PrivilegeException, VoNotExistsException, UserNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getRichAdminsWithAttributes");
    }

    return getPerunBl().getUsersManagerBl()
        .filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithAttributes(sess, vo));
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes)
      throws PrivilegeException, VoNotExistsException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    //  Authorization - Vo admin required
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
      throw new PrivilegeException(sess, "getRichAdminsWithSpecificAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        vosManagerBl.getRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
  }

  @Override
  public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");
    Vo vo = vosManagerBl.getVoById(sess, id);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVoById_int_policy", vo)) {
      throw new PrivilegeException(sess, "getVoById");
    }

    return vo;
  }

  @Override
  public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(shortName, "shortName");
    Utils.notNull(sess, "sess");
    Vo vo = vosManagerBl.getVoByShortName(sess, shortName);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVoByShortName_String_policy", vo)) {
      throw new PrivilegeException(sess, "getVoByShortName");
    }

    return vo;
  }

  @Override
  public Map<Status, Integer> getVoMembersCountsByStatus(PerunSession sess, Vo vo)
      throws VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVoMembersCountsByStatus_Vo_policy", vo)) {
      throw new PrivilegeException(sess, "getVoMembersCountsByStatus");
    }

    return vosManagerBl.getVoMembersCountsByStatus(sess, vo);
  }

  @Override
  public List<Vo> getVos(PerunSession sess) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVos_policy")) {
      throw new PrivilegeException(sess, "getVos");
    } else {
      List<Vo> tempVos = vosManagerBl.getVos(sess);
      tempVos.removeIf(vo -> {
        try {
          return !AuthzResolver.authorizedInternal(sess, "filter-getVos_policy", vo);
        } catch (InternalErrorException e) {
          // if we can't determine authorization prevent returning it
          return true;
        }
      });

      return tempVos;
    }
  }

  @Override
  public List<Vo> getVosByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVosByIds_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "getVosByIds");
    }
    List<Vo> vos = vosManagerBl.getVosByIds(sess, ids);
    vos.removeIf(vo -> !AuthzResolver.authorizedInternal(sess, "filter-getVosByIds_List<Integer>_policy", vo));

    return vos;
  }

  @Override
  public int getVosCount(PerunSession sess) {
    Utils.checkPerunSession(sess);

    return vosManagerBl.getVosCount(sess);
  }

  @Override
  public void removeAdmin(PerunSession sess, Vo vo, User user)
      throws PrivilegeException, VoNotExistsException, UserNotAdminException, UserNotExistsException,
      RoleCannotBeManagedException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getUsersManagerBl().checkUserExists(sess, user);

    AuthzResolver.unsetRole(sess, user, vo, Role.VOADMIN);
  }

  @Override
  public void removeAdmin(PerunSession sess, Vo vo, Group group)
      throws PrivilegeException, VoNotExistsException, GroupNotAdminException, GroupNotExistsException,
      RoleCannotBeManagedException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

    AuthzResolver.unsetRole(sess, group, vo, Role.VOADMIN);
  }

  @Override
  public void removeBan(PerunSession sess, int banId) throws PrivilegeException, BanNotExistsException {
    Utils.checkPerunSession(sess);

    BanOnVo banOnVo = perunBl.getVosManagerBl().getBanById(sess, banId);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "vo-removeBan_int_policy", banOnVo)) {
      throw new PrivilegeException("removeBan");
    }

    vosManagerBl.removeBan(sess, banId);
  }

  @Override
  public void removeBanForMember(PerunSession sess, Member member)
      throws PrivilegeException, BanNotExistsException, MemberNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "vo-removeBanForMember_member_policy", member)) {
      throw new PrivilegeException("removeBanForMember");
    }

    Optional<BanOnVo> banOnVo = perunBl.getVosManagerBl().getBanForMember(sess, member.getId());

    if (!banOnVo.isPresent()) {
      throw new BanNotExistsException("Given member is not banned.");
    }

    vosManagerBl.removeBanForMember(sess, member.getId());
  }

  @Override
  public void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo)
      throws RelationNotExistsException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    vosManagerBl.checkVoExists(sess, memberVo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeMemberVo_Vo_Vo_policy", vo)) {
      throw new PrivilegeException("removeMemberVo");
    }

    vosManagerBl.removeMemberVo(sess, vo, memberVo);
  }

  /**
   * Removes role SPONSOR from user in a VO.
   */
  @Override
  public void removeSponsorRole(PerunSession sess, Vo vo, User user)
      throws UserNotAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException,
      RoleCannotBeManagedException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getUsersManagerBl().checkUserExists(sess, user);

    AuthzResolver.unsetRole(sess, user, vo, Role.SPONSOR);
  }

  /**
   * Removes role SPONSOR from group in a VO.
   */
  @Override
  public void removeSponsorRole(PerunSession sess, Vo vo, Group group)
      throws GroupNotAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException,
      RoleCannotBeManagedException {
    Utils.checkPerunSession(sess);
    vosManagerBl.checkVoExists(sess, vo);
    perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

    AuthzResolver.unsetRole(sess, group, vo, Role.SPONSOR);
  }

  @Override
  public BanOnVo setBan(PerunSession sess, BanOnVo ban)
      throws PrivilegeException, MemberNotExistsException, BanAlreadyExistsException {
    Utils.checkPerunSession(sess);

    // We have to fetch the member object from DB because of authorization. The given ban object might contain
    // invalid combination of voId and memberId
    Member member = perunBl.getMembersManagerBl().getMemberById(sess, ban.getMemberId());

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "setBan_BanOnVo_policy", member)) {
      throw new PrivilegeException("setBan");
    }

    return vosManagerBl.setBan(sess, ban);
  }

  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  /**
   * Sets the vosManagerBl for this instance.
   *
   * @param vosManagerBl The vosManagerBl.
   */
  public void setVosManagerBl(VosManagerBl vosManagerBl) {
    this.vosManagerBl = vosManagerBl;
  }

  @Override
  public BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo)
      throws PrivilegeException, BanNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    vosManagerBl.getBanById(sess, banOnVo.getId());

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateBan_BanOnVo_policy", Collections.singletonList(banOnVo))) {
      throw new PrivilegeException(sess, "updateBan");
    }

    banOnVo = vosManagerBl.updateBan(sess, banOnVo);
    return banOnVo;
  }

  @Override
  public Vo updateVo(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");
    vosManagerBl.checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateVo_Vo_policy", vo)) {
      throw new PrivilegeException(sess, "updateVo");
    }
    if (vo.getName().length() > 128) {
      throw new IllegalArgumentException("VO name is too long, >128 characters");
    }

    if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,32}$")) {
      throw new IllegalArgumentException(
          "Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 32 characters.");
    }

    if (!vo.getShortName().equals(vo.getShortName().trim())) {
      throw new IllegalArgumentException("Wrong VO short name - cannot contain leading and trailing spaces");
    }

    return vosManagerBl.updateVo(sess, vo);
  }
}
