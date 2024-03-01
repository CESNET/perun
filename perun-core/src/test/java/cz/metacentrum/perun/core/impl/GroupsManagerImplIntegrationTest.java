package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class GroupsManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private final static String CLASS_NAME = "GroupsManagerImpl.";

  final ExtSource extSource = new ExtSource(0, "GroupsManagerExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private int userLoginSequence = 0;

  private GroupsManagerImplApi groupsManagerImpl;

  private PerunSession sess;
  private Vo vo;

  @Before
  public void setUp() throws Exception {
    groupsManagerImpl =
        (GroupsManagerImplApi) ReflectionTestUtils.getField(perun.getGroupsManagerBl(), "groupsManagerImpl");
    if (groupsManagerImpl == null) {
      throw new RuntimeException("Failed to get groupsManagerImpl");
    }

    sess = perun.getPerunSession(
        new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL),
        new PerunClient());

    vo = new Vo(0, "GroupsImplTestVo", "GrpImplTestVo");
    vo = perun.getVosManagerBl().createVo(sess, vo);
  }

  @Test
  public void getAdminsOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getAdminsOnlyValid");

    Member member1 = createSomeMember(vo);
    member1 = perun.getMembersManagerBl().validateMember(sess, member1);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);

    Member member2 = createSomeMember(vo);
    member2 = perun.getMembersManagerBl().invalidateMember(sess, member2);

    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);

    Group group = setUpGroup(vo, member1, "testGroup");
    perun.getGroupsManagerBl().addMember(sess, group, member2);


    AuthzResolver.setRole(sess, group, group, Role.GROUPADMIN);

    assertThat(groupsManagerImpl.getAdmins(sess, group)).containsExactly(user1);
    perun.getMembersManagerBl().validateMember(sess, member2);
    assertThat(groupsManagerImpl.getAdmins(sess, group)).containsExactlyInAnyOrder(user1, user2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member2, group);
    assertThat(groupsManagerImpl.getAdmins(sess, group)).containsExactly(user1);

  }


  // private methods ==============================================================

  private Group setUpGroup(Vo vo, Member member, String name) throws Exception {

    Group group = new Group(name, "test group");
    group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

    perun.getGroupsManagerBl().addMember(sess, group, member);

    return group;
  }

  private Candidate setUpCandidate(String login) {

    String userFirstName = "FirstTest";
    String userLastName = "LastTest";

    Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
    candidate.setFirstName(userFirstName);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(userLastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    final UserExtSource userExtSource = new UserExtSource(extSource, login);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;

  }

  private Member createSomeMember(final Vo createdVo)
      throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {
    final Candidate candidate = setUpCandidate("Login" + userLoginSequence++);
    final Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
    return createdMember;
  }
}
