package cz.metacentrum.perun.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class UsersManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "UsersManagerImpl.";

  final ExtSource extSource = new ExtSource(0, "UsersManagerExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private int userLoginSequence = 0;

  private UsersManagerImplApi usersManagerImpl;

  private PerunSession sess;
  private Vo vo;

  private Member createSomeMember(final Vo createdVo)
      throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {
    final Candidate candidate = setUpCandidate("Login" + userLoginSequence++);
    final Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
    return createdMember;
  }

  @Test
  public void getGroupsWhereUserIsAdminOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getGroupsWhereUserIsAdminOnlyValid");

    Member member1 = createSomeMember(vo);
    Member member2 = createSomeMember(vo);
    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);

    Group group1 = setUpGroup(vo, member1, "group1");
    Group group2 = setUpGroup(vo, member2, "group2");

    AuthzResolver.setRole(sess, group1, group2, Role.GROUPADMIN);

    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(group2);
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, vo, user1)).containsExactlyInAnyOrder(group2);

    perun.getMembersManagerBl().invalidateMember(sess, member1);
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, user1)).containsExactly();
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, vo, user1)).containsExactly();

    perun.getMembersManagerBl().validateMember(sess, member1);
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(group2);
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, vo, user1)).containsExactlyInAnyOrder(group2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, user1)).containsExactly();
    assertThat(usersManagerImpl.getGroupsWhereUserIsAdmin(sess, vo, user1)).containsExactly();
  }

  @Test
  public void getVosWhereUserIsAdminOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getVosWhereUserIsAdminOnlyValid");

    Vo vo2 = new Vo(1, "testVo2", "testVo2");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member1 = createSomeMember(vo);

    User user = perun.getUsersManagerBl().getUserByMember(sess, member1);

    Group group1 = setUpGroup(vo, member1, "group1");

    AuthzResolver.setRole(sess, group1, vo2, Role.VOADMIN);

    assertThat(usersManagerImpl.getVosWhereUserIsAdmin(sess, user)).containsExactly(vo2);

    perun.getMembersManagerBl().invalidateMember(sess, member1);
    assertThat(usersManagerImpl.getVosWhereUserIsAdmin(sess, user)).containsExactly();

    perun.getMembersManagerBl().validateMember(sess, member1);
    assertThat(usersManagerImpl.getVosWhereUserIsAdmin(sess, user)).containsExactly(vo2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(usersManagerImpl.getVosWhereUserIsAdmin(sess, user)).containsExactly();
  }


  // private methods ==============================================================

  @Before
  public void setUp() throws Exception {
    usersManagerImpl =
        (UsersManagerImplApi) ReflectionTestUtils.getField(perun.getUsersManagerBl(), "usersManagerImpl");
    if (usersManagerImpl == null) {
      throw new RuntimeException("Failed to get usersManagerImpl");
    }

    sess = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());


    vo = new Vo(0, "testVo1", "testVo1");
    vo = perun.getVosManagerBl().createVo(sess, vo);

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

  private Group setUpGroup(Vo vo, Member member, String name) throws Exception {

    Group group = new Group(name, "test group");
    group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

    perun.getGroupsManagerBl().addMember(sess, group, member);

    return group;
  }
}