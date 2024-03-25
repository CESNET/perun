package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberSuspended;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberUnsuspended;
import cz.metacentrum.perun.audit.events.VoManagerEvents.BanUpdatedForVo;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoDeleted;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoUpdated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.EnrichedBanOnVo;
import cz.metacentrum.perun.core.api.EnrichedVo;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VosManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
@SuppressWarnings("deprecation")
public class VosManagerBlImpl implements VosManagerBl {

  public static final String A_MEMBER_DEF_MEMBER_ORGANIZATIONS =
      AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizations";
  public static final String A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY =
      AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizationsHistory";
  private static final Logger LOG = LoggerFactory.getLogger(VosManagerBlImpl.class);
  private final VosManagerImplApi vosManagerImpl;
  private PerunBl perunBl;

  /**
   * Constructor.
   */
  public VosManagerBlImpl(VosManagerImplApi vosManagerImpl) {
    this.vosManagerImpl = vosManagerImpl;
  }

  @Override
  public void addMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException {
    checkParentVos(sess, vo, memberVo);
    vosManagerImpl.addMemberVo(sess, vo, memberVo);

    AttributeDefinition memberExpirationAttrDef;

    try {
      memberExpirationAttrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess,
          AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
    } catch (AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }
    // if VO just became a parent, assign memberOrganizations attribute to all its members
    if (perunBl.getVosManagerBl().getMemberVos(sess, vo.getId()).size() == 1 && !wasParentVoBefore(sess, vo)) {
      List<Member> voMembers = perunBl.getMembersManagerBl().getMembers(sess, vo);
      try {
        for (Member member : voMembers) {
          perunBl.getMembersManagerBl().setOrganizationsAttributes(sess, vo, member);
        }
      } catch (AttributeNotExistsException | WrongAttributeValueException | WrongAttributeAssignmentException |
               WrongReferenceAttributeValueException e) {
        throw new InternalErrorException(e);
      }
    }
    List<Member> memberVoMembers = perunBl.getMembersManagerBl().getMembers(sess, memberVo);

    for (Member member : memberVoMembers) {
      if (member.getStatus() == Status.DISABLED || member.getStatus() == Status.INVALID) {
        continue;
      }
      try {

        try {
          Member existingMember = perunBl.getMembersManagerBl().getMemberByUserId(sess, vo, member.getUserId());
          if (member.getStatus() == Status.EXPIRED) {
            // member is expired in memberVo but exists in parentVo, don't update
            continue;
          }

          // member is valid in memberVo, so reset expiration in parentVo
          perunBl.getAttributesManagerBl().removeAttribute(sess, existingMember, memberExpirationAttrDef);
          if (existingMember.getStatus() != Status.VALID) {
            perunBl.getMembersManagerBl().validateMember(sess, existingMember);
          }
          //update memberOrganizations and memberOrganizationsHistory
          perunBl.getMembersManagerBl().updateOrganizationsAttributes(sess, memberVo, existingMember);

        } catch (MemberNotExistsException e) {
          // if user is member only in member vo, create member in parent vo
          Member newMember = perunBl.getMembersManagerBl()
                                 .createMember(sess, vo, perunBl.getUsersManagerBl().getUserByMember(sess, member));
          if (member.getStatus() == Status.VALID) {
            // remove expiration set according to parentVo and update memberOrganizations and memberOrganizationsHistory
            perunBl.getAttributesManagerBl().removeAttribute(sess, newMember, memberExpirationAttrDef);
            perunBl.getMembersManagerBl().setOrganizationsAttributes(sess, memberVo, newMember);
          }
          perunBl.getMembersManagerBl().validateMember(sess, newMember);
        }

      } catch (WrongAttributeAssignmentException | WrongReferenceAttributeValueException | AttributeNotExistsException |
               WrongAttributeValueException | AlreadyMemberException e) {
        throw new InternalErrorException(e);
      } catch (ExtendMembershipException e) {
        LOG.warn(
            "Could not set expiration for member " + member + " after adding vo " + memberVo + " as member of vo " +
                vo + " for reason: " + e.getReason());
      }
    }
  }

  /**
   * Check if new member vo is not already parent of vo.
   *
   * @param sess     session
   * @param vo       vo
   * @param memberVo new member of the vo
   * @throws RelationExistsException if member vo is already parent of vo
   */
  private void checkParentVos(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException {
    if (vo.getId() == memberVo.getId()) {
      throw new RelationExistsException(String.format("Member VO %s is an ancestor.", memberVo.getShortName()));
    }
    List<Vo> parents = vosManagerImpl.getParentVos(sess, vo.getId());
    for (Vo parent : parents) {
      checkParentVos(sess, parent, memberVo);
    }
  }

  @Override
  public void checkVoExists(PerunSession sess, Vo vo) throws VoNotExistsException {
    getVosManagerImpl().checkVoExists(sess, vo);
  }

  @Override
  public void convertSponsoredUsers(PerunSession sess, Vo vo) {
    perunBl.getUsersManagerBl().getSpecificUsers(sess).stream()
        .filter(User::isSponsoredUser)
        .forEach(user -> convertToSponsoredMember(sess, user, vo));
  }

  @Override
  public void convertSponsoredUsersWithNewSponsor(PerunSession sess, Vo vo, User newSponsor) {
    perunBl.getUsersManagerBl().getSpecificUsers(sess).stream()
        .filter(User::isSponsoredUser)
        .forEach(user -> convertToSponsoredMemberWithNewSponsor(sess, user, newSponsor, vo));
  }

  /**
   * Converts given vo into enriched vo.
   *
   * @param sess
   * @param vo   vo to be converted
   * @return converted EnrichedVo
   */
  private EnrichedVo convertToEnrichedVo(PerunSession sess, Vo vo) {
    List<Vo> memberVos = this.getMemberVos(sess, vo.getId());
    List<Vo> parentVos = this.getParentVos(sess, vo.getId());
    return new EnrichedVo(vo, memberVos, parentVos);
  }

  /**
   * Converts sponsored user to sponsored member in the given vo. If the user is not member of the given vo, it is
   * skipped.
   *
   * @param sess session
   * @param user user
   * @param vo   vo where the given user will be sponsored
   */
  private void convertToSponsoredMember(PerunSession sess, User user, Vo vo) {
    try {
      Member member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);
      List<User> owners = perunBl.getUsersManagerBl().getUsersBySpecificUser(sess, user);

      for (User owner : owners) {
        sponsorMemberByUser(sess, member, owner, vo);
      }
    } catch (MemberNotExistsException e) {
      // if the sponsored user is not member of the given vo, skip it
    }
  }

  /**
   * Sponsor given user by the given newSponsor in the given vo. If the newSponsor doesn't have the SPONSOR role, it
   * will be set to him.
   *
   * @param sess       session
   * @param user       user to be sponsored
   * @param newSponsor new sponsor
   * @param vo         vo where the given user will be sponsored
   */
  private void convertToSponsoredMemberWithNewSponsor(PerunSession sess, User user, User newSponsor, Vo vo) {
    try {
      Member member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);

      sponsorMemberByUser(sess, member, newSponsor, vo);
    } catch (MemberNotExistsException e) {
      // if the sponsored user is not member of the given vo, skip it
    }
  }

  /**
   * Creates MemberCandidates for given RichUsers, vo and candidates.
   *
   * @param sess       session
   * @param users      users
   * @param candidates candidates
   * @param attrNames  names of attributes that will be returned
   * @return list of MemberCandidates for given RichUsers, vo and candidates
   * @throws InternalErrorException internal error
   */
  private List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo,
                                                       List<Candidate> candidates, List<String> attrNames) {
    return createMemberCandidates(sess, users, vo, null, candidates, attrNames);
  }

  /**
   * Creates MemberCandidates for given RichUsers, vo, group and candidates. If the given group is not null then to all
   * members who are in this group is assigned the sourceGroupId of the given group. The given group can be null.
   *
   * @param sess       session
   * @param users      users
   * @param group      group
   * @param candidates candidates
   * @param attrNames  names of attributes that will be returned
   * @return list of MemberCandidates for given RichUsers, group and candidates
   * @throws InternalErrorException internal error
   */
  public List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo, Group group,
                                                      List<Candidate> candidates, List<String> attrNames) {
    List<MemberCandidate> memberCandidates = new ArrayList<>();
    Set<Integer> allUsersIds = new HashSet<>();
    int userId;

    // try to find matching RichUser for candidates
    for (Candidate candidate : candidates) {
      MemberCandidate mc = new MemberCandidate();

      try {
        User user = getPerunBl().getUsersManagerBl().getUserByUserExtSources(sess, candidate.getUserExtSources());
        userId = user.getId();

        // check if user already exists in the list
        if (!allUsersIds.contains(userId)) {
          RichUser richUser =
              getPerunBl().getUsersManagerBl().convertUserToRichUserWithAttributesByNames(sess, user, attrNames);
          mc.setRichUser(richUser);
          memberCandidates.add(mc);
        }
        allUsersIds.add(userId);

      } catch (UserNotExistsException ignored) {
        // no matching user was found
        mc.setCandidate(candidate);
        memberCandidates.add(mc);
      }

    }

    List<RichUser> foundRichUsers = memberCandidates.stream()
                                        .map(MemberCandidate::getRichUser)
                                        .collect(Collectors.toList());

    // create MemberCandidates for RichUsers without candidate
    for (RichUser richUser : users) {
      if (!foundRichUsers.contains(richUser)) {
        MemberCandidate mc = new MemberCandidate();
        mc.setRichUser(richUser);
        memberCandidates.add(mc);
      }
    }

    // try to find member for MemberCandidates with not null RichUser
    for (MemberCandidate memberCandidate : memberCandidates) {
      if (memberCandidate.getRichUser() != null) {
        Member member = null;
        try {

          member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, memberCandidate.getRichUser());
          if (group != null) {
            member = getPerunBl().getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
          }

        } catch (MemberNotExistsException ignored) {
          // no matching VO member was found
        } catch (NotGroupMemberException e) {
          // not matching Group member was found
        }

        // put null or matching member
        memberCandidate.setMember(member);

      }
    }

    return memberCandidates;
  }

  @Override
  public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException {
    // Create entries in the DB and Grouper
    vo = getVosManagerImpl().createVo(sess, vo);
    getPerunBl().getAuditer().log(sess, new VoCreated(vo));

    User user = sess.getPerunPrincipal().getUser();
    //set creator as VO manager
    if (user != null) {
      try {
        AuthzResolverBlImpl.setRole(sess, user, vo, Role.VOADMIN);
        LOG.debug("User {} added like administrator to VO {}", user, vo);
      } catch (AlreadyAdminException ex) {
        throw new ConsistencyErrorException(
            "Add manager to newly created VO failed because there is a particular manager already assigned", ex);
      } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
        throw new InternalErrorException(e);
      }
    } else {
      LOG.error("Can't set VO manager during creating of the VO. User from perunSession is null. {} {}", vo, sess);
    }

    try {
      // Create group containing VO members
      Group members =
          new Group(VosManager.MEMBERS_GROUP, VosManager.MEMBERS_GROUP_DESCRIPTION + " for VO " + vo.getName());
      getPerunBl().getGroupsManagerBl().createGroup(sess, vo, members);
      LOG.debug("Members group created, vo '{}'", vo);
    } catch (GroupExistsException e) {
      throw new ConsistencyErrorException("Group already exists", e);
    }

    // create empty application form
    getVosManagerImpl().createApplicationForm(sess, vo);

    LOG.info("Vo {} created", vo);

    return vo;
  }

  @Override
  public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) {
    LOG.debug("Deleting vo {}", vo);

    try {
      //remove admins of this vo
      List<Group> adminGroups = getVosManagerImpl().getAdminGroups(sess, vo);

      for (Group adminGroup : adminGroups) {
        try {
          AuthzResolverBlImpl.unsetRole(sess, adminGroup, vo, Role.VOADMIN);
        } catch (GroupNotAdminException e) {
          LOG.warn("When trying to unsetRole VoAdmin for group {} in the vo {} the exception was thrown {}", adminGroup,
              vo, e);
          //skip and log as warning
        }
      }

      List<User> adminUsers = getVosManagerImpl().getAdmins(sess, vo);

      for (User adminUser : adminUsers) {
        try {
          AuthzResolverBlImpl.unsetRole(sess, adminUser, vo, Role.VOADMIN);
        } catch (UserNotAdminException e) {
          LOG.warn("When trying to unsetRole VoAdmin for user {} in the vo {} the exception was thrown {}", adminUser,
              vo, e);
          //skip and log as warning
        }
      }

      List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);

      LOG.debug("Deleting vo {} members", vo);
      // Check if there are some members left
      if (members != null && members.size() > 0) {
        if (forceDelete) {
          getPerunBl().getMembersManagerBl().deleteAllMembers(sess, vo);
        } else {
          throw new RelationExistsException("Vo vo=" + vo + " contains members");
        }
      }

      LOG.debug("Removing vo {} resources and theirs attributes", vo);
      // Delete resources
      List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
      if ((resources.size() == 0) || forceDelete) {
        for (Resource resource : resources) {
          getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource);
          // Remove binding between service and resource
          List<Service> services = getPerunBl().getResourcesManagerBl().getAssignedServices(sess, resource);
          for (Service service : services) {
            getPerunBl().getResourcesManagerBl().removeService(sess, resource, service);
          }
          getPerunBl().getResourcesManagerBl().deleteResource(sess, resource);
        }
      } else {
        throw new RelationExistsException("Vo vo=" + vo + " contains resources");
      }

      LOG.debug("Removing vo {} groups", vo);
      // Delete all groups

      List<Group> groups = getPerunBl().getGroupsManagerBl().getGroups(sess, vo);
      if (groups.size() != 1) {
        if (groups.size() < 1) {
          throw new ConsistencyErrorException("'members' group is missing");
        }
        if (forceDelete) {
          getPerunBl().getGroupsManagerBl().deleteAllGroups(sess, vo);
        } else {
          throw new RelationExistsException("Vo vo=" + vo + " contains groups");
        }
      }

      // Remove hierarchical relations
      LOG.debug("Removing {} hierarchical relations with parent organizations", vo);
      List<Vo> parentVos = getParentVos(sess, vo.getId());
      if (parentVos.size() != 0 && !forceDelete) {
        throw new RelationExistsException("Vo vo=" + vo + " is member of hierarchical organization");
      }
      for (Vo parentVo : parentVos) {
        removeMemberVo(sess, parentVo, vo);
      }
      LOG.debug("Removing {} hierarchical relations with member organizations", vo);
      List<Vo> memberVos = getMemberVos(sess, vo.getId());
      if (memberVos.size() != 0 && !forceDelete) {
        throw new RelationExistsException("Vo vo=" + vo + " has member organizations");
      }
      for (Vo memberVo : memberVos) {
        removeMemberVo(sess, vo, memberVo);
      }

      // Finally delete binding between Vo and external source
      List<ExtSource> ess = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
      LOG.debug("Deleting {} external sources binded to the vo {}", ess.size(), vo);
      for (ExtSource es : ess) {
        getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, vo, es);
      }

      // Delete members group
      LOG.debug("Removing an administrators' group from the vo {}", vo);
      getPerunBl().getGroupsManagerBl().deleteMembersGroup(sess, vo);

      // delete all VO reserved logins from KDC and DB
      List<Integer> list = getVosManagerImpl().getVoApplicationIds(sess, vo);
      for (Integer appId : list) {
        perunBl.getUsersManagerBl().deleteReservedLoginsOnlyByGivenApp(sess, appId);
      }

      // VO applications, submitted data and app_form are deleted on cascade with "deleteVo()"

      // Delete VO attributes
      getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, vo);

      // Delete all Vo tags (for resources in Vo)
      getPerunBl().getResourcesManagerBl().deleteAllResourcesTagsForVo(sess, vo);

    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }

    // Finally delete the VO
    Vo deletedVo = getVosManagerImpl().deleteVo(sess, vo);
    getPerunBl().getAuditer().log(sess, new VoDeleted(deletedVo));
  }

  @Override
  public void deleteVo(PerunSession sess, Vo vo) {
    // delete VO only if it is completely empty
    this.deleteVo(sess, vo, false);
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) {
    List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
    return this.findCandidates(sess, vo, searchString, maxNumOfResults, extSources, true);
  }

  public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults,
                                        List<ExtSource> extSources, boolean filterExistingMembers) {
    List<Candidate> candidates = new ArrayList<>();
    int numOfResults = 0;

    try {
      // Iterate through given extSources
      for (ExtSource source : extSources) {
        try {
          // Info if this is only simple ext source, change behavior if not
          boolean simpleExtSource = true;

          // Get potential subjects from the extSource
          List<Map<String, String>> subjects;
          try {
            if (source instanceof ExtSourceApi) {
              // find subjects with all their properties
              subjects = ((ExtSourceApi) source).findSubjects(searchString, maxNumOfResults);
              simpleExtSource = false;
            } else {
              // find subjects only with logins - they then must be retrieved by login
              subjects = ((ExtSourceSimpleApi) source).findSubjectsLogins(searchString, maxNumOfResults);
            }
          } catch (ExtSourceUnsupportedOperationException e1) {
            LOG.warn("ExtSource {} doesn't support findSubjects", source.getName());
            continue;
          } catch (InternalErrorException e) {
            LOG.error("Error occurred on ExtSource {},  Exception {}.", source.getName(), e);
            continue;
          } finally {
            try {
              ((ExtSourceSimpleApi) source).close();
            } catch (ExtSourceUnsupportedOperationException e) {
              // ExtSource doesn't support that functionality, so silently skip it.
            } catch (InternalErrorException e) {
              LOG.error("Can't close extSource connection.", e);
            }
          }

          Set<String> uniqueLogins = new HashSet<>();
          for (Map<String, String> s : subjects) {
            // Check if the user has unique identifier within extSource
            if ((s.get("login") == null) || (s.get("login") != null && s.get("login").isEmpty())) {
              LOG.error("User '{}' cannot be added, because he/she doesn't have a unique identifier (login)", s);
              // Skip to another user
              continue;
            }

            String extLogin = s.get("login");

            // check uniqueness of every login in extSource
            if (uniqueLogins.contains(extLogin)) {
              throw new InternalErrorException(
                  "There are more than 1 login '" + extLogin + "' getting from extSource '" + source + "'");
            } else {
              uniqueLogins.add(extLogin);
            }

            // Get Candidate
            Candidate candidate;
            try {
              if (simpleExtSource) {
                // retrieve data about subjects from ext source based on ext. login
                candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin));
              } else {
                // retrieve data about subjects from subjects we already have locally
                candidate =
                    new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin));
              }
            } catch (CandidateNotExistsException e) {
              throw new ConsistencyErrorException(
                  "findSubjects returned that candidate, but getCandidate cannot find him using login " + extLogin, e);
            } catch (ExtSourceUnsupportedOperationException e) {
              throw new InternalErrorException("extSource supports findSubjects but not getCandidate???", e);
            }

            if (filterExistingMembers) {
              try {
                getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, vo, candidate.getUserExtSources());
                // Candidate is already a member of the VO, so do not add him to the list of candidates
                continue;
              } catch (MemberNotExistsException e) {
                // This is OK
              }
            }

            // Add candidate to the list of candidates
            LOG.debug("findCandidates: returning candidate: {}", candidate);
            candidates.add(candidate);

            numOfResults++;
            // Stop getting new members if the number of already retrieved members exceeded the maxNumOfResults
            if (maxNumOfResults > 0 && numOfResults >= maxNumOfResults) {
              break;
            }
          }

        } catch (InternalErrorException e) {
          LOG.error("Failed to get candidates from ExtSource: {}", source);
        } finally {
          if (source instanceof ExtSourceSimpleApi) {
            try {
              ((ExtSourceSimpleApi) source).close();
            } catch (ExtSourceUnsupportedOperationException e) {
              // silently skip
            } catch (Exception e) {
              LOG.error("Failed to close connection to extsource", e);
            }
          }
        }
        // Stop walking through next sources if the number of already retrieved members exceeded the maxNumOfResults
        if (maxNumOfResults > 0 && numOfResults >= maxNumOfResults) {
          break;
        }
      }

      LOG.debug("Returning {} potential members for vo {}", candidates.size(), vo);
      return candidates;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) {
    return this.findCandidates(sess, vo, searchString, 0);
  }

  @Override
  public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) {
    List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
    return this.findCandidates(sess, group, searchString, extSources, true);
  }

  public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString, List<ExtSource> extSources,
                                        boolean filterExistingMembers) {
    List<Candidate> candidates = new ArrayList<>();

    try {
      // Iterate through given extSources
      for (ExtSource source : extSources) {
        try {
          // Info if this is only simple ext source, change behavior if not
          boolean simpleExtSource = true;

          // Get potential subjects from the extSource
          List<Map<String, String>> subjects;
          try {
            if (source instanceof ExtSourceApi) {
              // find subjects with all their properties
              subjects = ((ExtSourceApi) source).findSubjects(searchString);
              simpleExtSource = false;
            } else {
              // find subjects only with logins - they then must be retrieved by login
              subjects = ((ExtSourceSimpleApi) source).findSubjectsLogins(searchString);
            }
          } catch (ExtSourceUnsupportedOperationException e1) {
            LOG.warn("ExtSource {} doesn't support findSubjects", source.getName());
            continue;
          } catch (InternalErrorException e) {
            LOG.error("Error occurred on ExtSource {},  Exception {}.", source.getName(), e);
            continue;
          } finally {
            try {
              ((ExtSourceSimpleApi) source).close();
            } catch (ExtSourceUnsupportedOperationException e) {
              // ExtSource doesn't support that functionality, so silently skip it.
            } catch (InternalErrorException e) {
              LOG.error("Can't close extSource connection.", e);
            }
          }

          Set<String> uniqueLogins = new HashSet<>();
          for (Map<String, String> s : subjects) {
            // Check if the user has unique identifier within extSource
            if ((s.get("login") == null) || (s.get("login") != null && s.get("login").isEmpty())) {
              LOG.error("User '{}' cannot be added, because he/she doesn't have a unique identifier (login)", s);
              // Skip to another user
              continue;
            }

            String extLogin = s.get("login");

            // check uniqueness of every login in extSource
            if (uniqueLogins.contains(extLogin)) {
              throw new InternalErrorException(
                  "There are more than 1 login '" + extLogin + "' getting from extSource '" + source + "'");
            } else {
              uniqueLogins.add(extLogin);
            }

            // Get Candidate
            Candidate candidate;
            try {
              if (simpleExtSource) {
                // retrieve data about subjects from ext source based on ext. login
                candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin));
              } else {
                // retrieve data about subjects from subjects we already have locally
                candidate =
                    new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin));
              }
            } catch (CandidateNotExistsException e) {
              throw new ConsistencyErrorException(
                  "findSubjects returned that candidate, but getCandidate cannot find him using login " + extLogin, e);
            } catch (ExtSourceUnsupportedOperationException e) {
              throw new InternalErrorException("extSource supports findSubjects but not getCandidate???", e);
            }

            if (filterExistingMembers) {
              try {
                Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
                getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, vo, candidate.getUserExtSources());
                // Candidate is already a member of the VO, so do not add him to the list of candidates
                continue;
              } catch (VoNotExistsException e) {
                throw new InternalErrorException(e);
              } catch (MemberNotExistsException e) {
                // This is OK
              }
            }

            // Add candidate to the list of candidates
            LOG.debug("findCandidates: returning candidate: {}", candidate);
            candidates.add(candidate);

          }
        } catch (InternalErrorException e) {
          LOG.error("Failed to get candidates from ExtSource: {}", source);
        } finally {
          if (source instanceof ExtSourceSimpleApi) {
            try {
              ((ExtSourceSimpleApi) source).close();
            } catch (ExtSourceUnsupportedOperationException e) {
              // silently skip
            } catch (Exception e) {
              LOG.error("Failed to close connection to extsource", e);
            }
          }
        }
      }

      LOG.debug("Returning {} potential members for group {}", candidates.size(), group);
      return candidates;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, String role) {
    return getVosManagerImpl().getAdminGroups(perunSession, vo, role);
  }

  @Deprecated
  @Override
  public List<Group> getAdminGroups(PerunSession sess, Vo vo) {
    return getVosManagerImpl().getAdminGroups(sess, vo);
  }

  @Override
  public List<User> getAdmins(PerunSession perunSession, Vo vo, String role, boolean onlyDirectAdmins) {
    if (onlyDirectAdmins) {
      return getVosManagerImpl().getDirectAdmins(perunSession, vo, role);
    } else {
      return getVosManagerImpl().getAdmins(perunSession, vo, role);
    }
  }

  @Override
  @Deprecated
  public List<User> getAdmins(PerunSession sess, Vo vo) {
    return getVosManagerImpl().getAdmins(sess, vo);
  }

  @Override
  public BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException {
    return vosManagerImpl.getBanById(sess, banId);
  }

  @Override
  public Optional<BanOnVo> getBanForMember(PerunSession sess, int memberId) {
    BanOnVo ban = vosManagerImpl.getBanForMember(sess, memberId);
    if (ban == null) {
      return Optional.empty();
    } else {
      return Optional.of(ban);
    }
  }

  @Override
  public List<BanOnVo> getBansForUser(PerunSession sess, int userId) {
    return vosManagerImpl.getBansForUser(sess, userId);
  }

  @Override
  public List<BanOnVo> getBansForVo(PerunSession sess, int voId) {
    return vosManagerImpl.getBansForVo(sess, voId);
  }

  @Override
  public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames,
                                                     String searchString) {
    List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
    List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, attrNames, searchString);
    List<Candidate> candidates = findCandidates(sess, vo, searchString, 0, extSources, false);

    return createMemberCandidates(sess, richUsers, vo, candidates, attrNames);
  }

  @Override
  public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, Group group, List<String> attrNames,
                                                     String searchString, List<ExtSource> extSources) {
    List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, vo, attrNames, searchString, extSources);
    List<Candidate> candidates = findCandidates(sess, group, searchString, extSources, false);

    if (vo == null) {
      vo = getPerunBl().getGroupsManagerBl().getVo(sess, group);
    }

    return createMemberCandidates(sess, richUsers, vo, group, candidates, attrNames);
  }

  @Deprecated
  @Override
  public List<User> getDirectAdmins(PerunSession sess, Vo vo) {
    return getVosManagerImpl().getDirectAdmins(sess, vo);
  }

  @Deprecated
  @Override
  public List<RichUser> getDirectRichAdmins(PerunSession sess, Vo vo) {
    return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(sess, getVosManagerImpl().getDirectAdmins(sess, vo));
  }

  @Override
  @Deprecated
  public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo,
                                                                  List<String> specificAttributes) {
    try {
      return getPerunBl().getUsersManagerBl()
                 .convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, vo),
                     getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException("One of Attribute not exist.", ex);
    }
  }

  public List<EnrichedBanOnVo> getEnrichedBansForUser(PerunSession sess, int userId, List<String> attrNames)
      throws AttributeNotExistsException {
    List<EnrichedBanOnVo> enrichedBans = new ArrayList<>();
    List<BanOnVo> bans = getBansForUser(sess, userId);

    // Parse attrNames
    List<AttributeDefinition> attrDefs = new ArrayList<>();
    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    for (BanOnVo ban : bans) {
      try {
        Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, ban.getVoId());
        Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, ban.getMemberId());
        RichMember richMember = getPerunBl().getMembersManagerBl().getRichMember(sess, member);
        if (attrDefs.isEmpty()) {
          richMember =
              getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, List.of(richMember))
                  .get(0);
        } else {
          richMember = getPerunBl().getMembersManagerBl()
                           .convertMembersToRichMembersWithAttributes(sess, List.of(richMember), attrDefs).get(0);
        }
        enrichedBans.add(new EnrichedBanOnVo(richMember, vo, ban));
      } catch (VoNotExistsException e) {
        LOG.error("Vo not exists when getting enriched ban for vo.", e);
      } catch (MemberNotExistsException e) {
        LOG.error("Member not exists when getting enriched ban for user.", e);
      }
    }

    return enrichedBans;
  }

  public List<EnrichedBanOnVo> getEnrichedBansForVo(PerunSession sess, Vo vo, List<String> attrNames)
      throws AttributeNotExistsException {
    List<EnrichedBanOnVo> enrichedBans = new ArrayList<>();
    List<BanOnVo> bans = getBansForVo(sess, vo.getId());

    // Parse attrNames
    List<AttributeDefinition> attrDefs = new ArrayList<>();
    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    for (BanOnVo ban : bans) {
      try {
        Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, ban.getMemberId());
        RichMember richMember = getPerunBl().getMembersManagerBl().getRichMember(sess, member);
        if (attrDefs.isEmpty()) {
          richMember =
              getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, List.of(richMember))
                  .get(0);
        } else {
          richMember = getPerunBl().getMembersManagerBl()
                           .convertMembersToRichMembersWithAttributes(sess, List.of(richMember), attrDefs).get(0);
        }
        enrichedBans.add(new EnrichedBanOnVo(richMember, vo, ban));
      } catch (MemberNotExistsException e) {
        LOG.error("Member not exists when getting enriched ban for user.", e);
      }
    }
    return enrichedBans;
  }

  @Override
  public EnrichedVo getEnrichedVoById(PerunSession sess, int id) throws VoNotExistsException {
    return convertToEnrichedVo(sess, this.getVoById(sess, id));
  }

  @Override
  public List<EnrichedVo> getEnrichedVos(PerunSession sess) {
    return getVos(sess).stream()
               .map(vo -> convertToEnrichedVo(sess, vo))
               .collect(Collectors.toList());
  }

  @Override
  public List<Vo> getMemberVos(PerunSession sess, int voId) {
    return vosManagerImpl.getMemberVos(sess, voId);
  }

  @Override
  public List<Vo> getParentVos(PerunSession sess, int memberVoId) {
    return vosManagerImpl.getParentVos(sess, memberVoId);
  }

  /**
   * Gets the perunBl.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, String role, List<String> specificAttributes,
                                      boolean allUserAttributes, boolean onlyDirectAdmins)
      throws UserNotExistsException {
    List<User> users = this.getAdmins(perunSession, vo, role, onlyDirectAdmins);
    List<RichUser> richUsers;

    if (allUserAttributes) {
      richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
    } else {
      try {
        richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession,
            perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users),
            getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
      } catch (AttributeNotExistsException ex) {
        throw new InternalErrorException("One of Attribute not exist.", ex);
      }
    }
    return richUsers;
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) {
    List<User> users = this.getAdmins(perunSession, vo);
    return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws UserNotExistsException {
    List<User> users = this.getAdmins(perunSession, vo);
    return perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo,
                                                            List<String> specificAttributes) {
    try {
      return getPerunBl().getUsersManagerBl()
                 .convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, vo),
                     getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException("One of Attribute not exist.", ex);
    }
  }

  /**
   * <p>Finds RichUsers who matches the given search string. Users are searched in the whole Perun.</p>
   * <p>The RichUsers are returned with attributes of given names.</p>
   *
   * @param sess         session
   * @param attrNames    names of attributes that will be returned
   * @param searchString string used to find users
   * @return List of RichUsers from whole Perun, who matches the given String
   * @throws InternalErrorException internal error
   */
  private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, List<String> attrNames,
                                                         String searchString) {
    return getRichUsersForMemberCandidates(sess, null, attrNames, searchString, null);
  }

  /**
   * <p>Finds RichUsers who matches the given search string. If the given Vo is null,
   * they are searched in the whole Perun. If th Vo is not null, then are returned only RichUsers who has a member
   * inside this Vo or who has ues in any of given ExtSources.</p>
   * <p>The RichUsers are returned with attributes of given names.</p>
   *
   * @param sess         session
   * @param vo           virtual organization, users are searched inside this vo; if is null, then in the whole Perun
   * @param attrNames    names of attributes that will be returned
   * @param searchString string used to find users
   * @param extSources   list of extSources to possibly search users with ues in these extSources
   * @return List of RichUsers inside given Vo, or in whole perun, who matches the given String
   * @throws InternalErrorException internal error
   */
  private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, Vo vo, List<String> attrNames,
                                                         String searchString, List<ExtSource> extSources) {
    List<RichUser> richUsers;

    if (vo != null) {
      try {
        List<RichUser> allRichUsers =
            getPerunBl().getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames);
        richUsers = new ArrayList<>();

        // filter users who don't have ues in any of the extSources nor they are in given vo
        for (RichUser richUser : allRichUsers) {
          boolean extSourceMatch = getPerunBl().getUsersManagerBl().getUserExtSources(sess, richUser).stream()
                                       .map(UserExtSource::getExtSource)
                                       .anyMatch(extSources::contains);
          if (extSourceMatch) {
            richUsers.add(richUser);
          } else {
            try {
              Member member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, richUser);
              richUsers.add(richUser);
            } catch (MemberNotExistsException e) {
              // richUser is not in vo nor he has ues in any of given ExtSources, skip him
            }
          }
        }
      } catch (UserNotExistsException e) {
        richUsers = new ArrayList<>();
      }
    } else {
      try {
        richUsers = getPerunBl().getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames);
      } catch (UserNotExistsException e) {
        richUsers = new ArrayList<>();
      }

    }
    return richUsers;
  }

  @Override
  public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException {
    return getVosManagerImpl().getVoById(sess, id);
  }

  @Override
  public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException {
    return getVosManagerImpl().getVoByShortName(sess, shortName);
  }

  @Override
  public Map<Status, Integer> getVoMembersCountsByStatus(PerunSession sess, Vo vo) {
    List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);

    Map<Status, Integer> counts = new HashMap<>();
    for (Status status : Status.values()) {
      counts.put(status, 0);
    }
    members.forEach(member -> counts.computeIfPresent(member.getStatus(), (key, value) -> value + 1));

    return counts;
  }

  @Override
  public List<Vo> getVos(PerunSession sess) {
    return getVosManagerImpl().getVos(sess);
  }

  @Override
  public List<Vo> getVosByIds(PerunSession sess, List<Integer> ids) {
    return getVosManagerImpl().getVosByIds(sess, ids);
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, Group group) throws VoNotExistsException {
    return Collections.singletonList(getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId()));
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, Member member) {
    return Collections.singletonList(getPerunBl().getMembersManagerBl().getMemberVo(sess, member));
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, Resource resource) throws VoNotExistsException {
    return Collections.singletonList(getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId()));
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, User user) {
    return new ArrayList<>(new HashSet<>(getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user)));
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, Host host) {
    Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
    return new ArrayList<>(new HashSet<>(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility)));
  }

  @Override
  public List<Vo> getVosByPerunBean(PerunSession sess, Facility facility) {
    return new ArrayList<>(new HashSet<>(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility)));
  }

  @Override
  public int getVosCount(PerunSession sess) {
    return getVosManagerImpl().getVosCount(sess);
  }

  /**
   * Gets the vosManagerImpl.
   *
   * @return The vosManagerImpl.
   */
  private VosManagerImplApi getVosManagerImpl() {
    return this.vosManagerImpl;
  }

  @Override
  public void handleGroupLostVoRole(PerunSession sess, Group group, Vo vo, String role) {
    switch (role) {
      case Role.SPONSOR:
        //remove all group members as sponsors
        UsersManagerBl um = getPerunBl().getUsersManagerBl();
        for (Member groupMember : getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group)) {
          removeSponsorFromSponsoredMembers(sess, vo, um.getUserByMember(sess, groupMember));
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void handleUserLostVoRole(PerunSession sess, User user, Vo vo, String role) {
    LOG.debug("handleUserLostVoRole(user={},vo={},role={})", user.getLastName(), vo.getShortName(), role);
    switch (role) {
      case Role.SPONSOR:
        removeSponsorFromSponsoredMembers(sess, vo, user);
        break;
      default:
        break;
    }
  }

  @Override
  public boolean isMemberBanned(PerunSession sess, int memberId) {
    return vosManagerImpl.isMemberBanned(sess, memberId);
  }

  @Override
  public boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo, boolean checkGroups) {
    if (AuthzResolverBlImpl.isUserInRoleForVo(session, user, role, vo)) {
      return true;
    }
    if (checkGroups) {
      List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(session, user);
      List<Group> allGroups = new ArrayList<>();
      for (Member member : members) {
        allGroups.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(session, member));
      }
      for (Group group : allGroups) {
        if (AuthzResolverBlImpl.isGroupInRoleForVo(session, group, role, vo)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void removeBan(PerunSession sess, int banId) throws BanNotExistsException {
    BanOnVo ban = vosManagerImpl.getBanById(sess, banId);

    vosManagerImpl.removeBan(sess, banId);
    Member member;
    try {
      member = perunBl.getMembersManagerBl().getMemberById(sess, ban.getMemberId());
    } catch (MemberNotExistsException e) {
      // shouldn't happen
      LOG.error("Failed to find member who was just banned.", e);
      throw new ConsistencyErrorException("Failed to find member who was just banned.", e);
    }

    perunBl.getAuditer().log(sess, new MemberUnsuspended(member));
  }

  @Override
  public void removeBanForMember(PerunSession sess, int memberId) throws BanNotExistsException {
    BanOnVo ban = vosManagerImpl.getBanForMember(sess, memberId);
    if (ban == null) {
      throw new BanNotExistsException("Ban for member with id " + memberId + " does not exist.");
    }
    removeBan(sess, ban.getId());
  }

  @Override
  public void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationNotExistsException {
    for (Group group : perunBl.getGroupsManagerBl().getAllAllowedGroupsToHierarchicalVo(sess, vo, memberVo)) {
      perunBl.getGroupsManagerBl().disallowGroupToHierarchicalVo(sess, group, vo);
    }

    vosManagerImpl.removeMemberVo(sess, vo, memberVo);

    List<Member> parentVoMembers = perunBl.getMembersManagerBl().getMembers(sess, vo);

    for (Member member : parentVoMembers) {
      try {
        Attribute attribute =
            perunBl.getAttributesManagerBl().getAttribute(sess, member, A_MEMBER_DEF_MEMBER_ORGANIZATIONS);
        ArrayList<String> currentValue = attribute.valueAsList();
        if (currentValue == null || !currentValue.contains(memberVo.getShortName())) {
          continue;
        }
        currentValue.remove(memberVo.getShortName());
        attribute.setValue(currentValue);
        perunBl.getAttributesManagerBl().setAttribute(sess, member, attribute);
        if (currentValue.isEmpty() || currentValue.equals(new ArrayList<>(List.of(vo.getShortName())))) {
          // removed last memberVo and lifecycle is now managed by parentVo
          perunBl.getMembersManagerBl().extendMembership(sess, member);
        }
      } catch (WrongAttributeValueException | WrongAttributeAssignmentException |
               WrongReferenceAttributeValueException | AttributeNotExistsException e) {
        throw new InternalErrorException(e);
      } catch (ExtendMembershipException e) {
        LOG.warn(
            "Could not set expiration for member " + member + " after removing its member organization " + memberVo +
                " for reason: " + e.getReason());
      }
    }
  }

  private void removeSponsorFromSponsoredMembers(PerunSession sess, Vo vo, User user) {
    LOG.debug("removeSponsorFromSponsoredMembers(vo={},user={})", vo.getShortName(), user.getLastName());
    MembersManagerBl membersManagerBl = getPerunBl().getMembersManagerBl();
    for (Member sponsoredMember : membersManagerBl.getSponsoredMembers(sess, vo, user)) {
      LOG.debug("removing sponsor from sponsored member {}", sponsoredMember.getId());
      membersManagerBl.removeSponsor(sess, sponsoredMember, user);
    }
  }

  @Override
  public BanOnVo setBan(PerunSession sess, BanOnVo banOnVo) throws MemberNotExistsException, BanAlreadyExistsException {
    Utils.notNull(banOnVo, "banOnVo");

    Member member = perunBl.getMembersManagerBl().getMemberById(sess, banOnVo.getMemberId());
    banOnVo.setVoId(member.getVoId());

    if (vosManagerImpl.isMemberBanned(sess, member.getId())) {
      throw new BanAlreadyExistsException(banOnVo);
    }


    banOnVo = vosManagerImpl.setBan(sess, banOnVo);

    // fetch all ban information
    try {
      banOnVo = vosManagerImpl.getBanById(sess, banOnVo.getId());
    } catch (BanNotExistsException e) {
      // shouldn't happen
      throw new ConsistencyErrorException(e);
    }

    perunBl.getAuditer().log(sess, new MemberSuspended(member));

    return banOnVo;
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
   * Sponsor the given member by the given sponsor in the given vo. If the member is already sponsored, this method just
   * adds the sponsor to the given member. If the member is not sponsored at all, it will transform it into a sponsored
   * one with the given sponsor.
   *
   * @param sess    session
   * @param member  member to be sponsored
   * @param sponsor sponsor
   * @param vo      vo where the member is sponsored
   */
  private void sponsorMemberByUser(PerunSession sess, Member member, User sponsor, Vo vo) {
    try {
      if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(sess, sponsor, Role.SPONSOR, vo, true)) {
        AuthzResolverBlImpl.setRole(sess, sponsor, vo, Role.SPONSOR);
      }

      // we need to refresh information and check if the member is already sponsored
      member = perunBl.getMembersManagerBl().getMemberById(sess, member.getId());

      if (member.isSponsored()) {
        // if the member is already sponsored, just add another sponsor
        perunBl.getMembersManagerBl().sponsorMember(sess, member, sponsor);
      } else {
        // if the member is not sponsored, transform him into a sponsored one
        perunBl.getMembersManagerBl().setSponsorshipForMember(sess, member, sponsor);
      }
    } catch (AlreadySponsorException e) {
      // if the user is already sponsoring the given member, just silently skip
    } catch (PerunException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo) {
    Utils.notNull(banOnVo, "banOnVo");

    banOnVo = getVosManagerImpl().updateBan(sess, banOnVo);
    getPerunBl().getAuditer().log(sess, new BanUpdatedForVo(banOnVo, banOnVo.getMemberId(), banOnVo.getVoId()));
    return banOnVo;
  }

  @Override
  public Vo updateVo(PerunSession sess, Vo vo) {
    getPerunBl().getAuditer().log(sess, new VoUpdated(vo));
    return getVosManagerImpl().updateVo(sess, vo);
  }

  @Override
  public boolean usesEmbeddedGroupRegistrations(PerunSession sess, Vo vo) {
    return vosManagerImpl.hasEmbeddedGroupsItemInForm(sess, vo.getId());
  }

  /**
   * Returns true if VO was ever a parent VO
   *
   * @param sess session
   * @param vo   VO
   */
  private boolean wasParentVoBefore(PerunSession sess, Vo vo) {
    try {
      List<Member> members = perunBl.getMembersManagerBl().getMembers(sess, vo);
      if (members.size() != 0) {
        Attribute history = perunBl.getAttributesManagerBl()
                                .getAttribute(sess, members.get(0), A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY);
        return history.valueAsList() != null;
      }
      return false;
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }
  }


}
