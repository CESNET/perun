package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthzResolverImplIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "AttributesManagerImplIntegrationTest.";
  private static final User user1 = new User(1, "", "", "", "", "");
  private static final User user2 = new User(2, "", "", "", "", "");
  private static final User user3 = new User(3, "", "", "", "", "");
  private static final User user4 = new User(4, "", "", "", "", "");
  private static Vo createdVo;
  final ExtSource extSource = new ExtSource(0, "AuthzResolverExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private AuthzResolverImplApi authzResolverImpl;
  private int userLoginSequence = 0;


  @Before
  public void setUp() throws Exception {
    authzResolverImpl = (AuthzResolverImplApi) ReflectionTestUtils.getField(
        AuthzResolverBlImpl.class, "authzResolverImpl"
    );

    createdVo = perun.getVosManagerBl().createVo(sess, new Vo(1, "", ""));
    perun.getUsersManagerBl().createUser(sess, user1);
    perun.getUsersManagerBl().createUser(sess, user2);
    perun.getUsersManagerBl().createUser(sess, user3);
    perun.getUsersManagerBl().createUser(sess, user4);
  }

  @Test
  public void setRole() throws Exception {
    System.out.println(CLASS_NAME + "setRole");

    Map<String, Integer> mapping = prepareMapping(user1);

    authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
    AuthzRoles userRoles = AuthzResolverBlImpl.getUserRoles(sess, user1, true);

    assertTrue(userRoles.hasRole(Role.VOADMIN, createdVo));
  }

  @Test
  public void getRolesOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getRolesOnlyValid");
    Member member1 = createSomeMember(createdVo);
    User testUser = perun.getUsersManagerBl().getUserByMember(sess, member1);
    Group testGroup = setUpGroup(createdVo, member1, "testGroup");

    Vo anotherCreatedVo = perun.getVosManagerBl().createVo(sess, new Vo(2, "another VO", "another VO"));

    AuthzResolver.setRole(sess, testUser, anotherCreatedVo, Role.VOADMIN);
    AuthzResolver.setRole(sess, testGroup, createdVo, Role.VOADMIN);

    AuthzRoles userRoles = authzResolverImpl.getRoles(testUser, true);
    assertTrue(userRoles.hasRole(Role.VOADMIN, createdVo));

    perun.getMembersManagerBl().invalidateMember(sess, member1);

    userRoles = authzResolverImpl.getRoles(testUser, true);
    assertFalse(userRoles.hasRole(Role.VOADMIN, createdVo));
    assertTrue(userRoles.hasRole(Role.VOADMIN, anotherCreatedVo));

    perun.getMembersManagerBl().validateMember(sess, member1);
    userRoles = authzResolverImpl.getRoles(testUser, true);
    assertTrue(userRoles.hasRole(Role.VOADMIN, createdVo));
    assertTrue(userRoles.hasRole(Role.VOADMIN, anotherCreatedVo));

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, testGroup);

    userRoles = authzResolverImpl.getRoles(testUser, true);
    assertFalse(userRoles.hasRole(Role.VOADMIN, createdVo));
    assertTrue(userRoles.hasRole(Role.VOADMIN, anotherCreatedVo));
  }

  @Test
  public void getUserRolesFromAuthorizedGroupsOnly() throws Exception {
    System.out.println(CLASS_NAME + "getUserRolesFromAuthorizedGroupsOnly");

    Member testMember = createSomeMember(createdVo);
    User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
    Group testGroup = setUpGroup(createdVo, testMember, "testGroup");

    // set role for group
    AuthzResolver.setRole(sess, testGroup, createdVo, Role.VOADMIN);

    // check that user has VO
    AuthzRoles userRoles = authzResolverImpl.getRolesObtainedFromAuthorizedGroupMemberships(testUser);
    assertEquals(1, userRoles.size());
    assertTrue(userRoles.hasRole(Role.VOADMIN, createdVo));
  }

  @Test
  public void getUserRolesWithoutAuthorizedGroupsBased() throws Exception {
    System.out.println(CLASS_NAME + "getUserRolesWithoutAuthorizedGroupsBased");

    Member testMember = createSomeMember(createdVo);
    User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
    Group testGroup = setUpGroup(createdVo, testMember, "testGroup");

    // set role for group
    AuthzResolver.setRole(sess, testGroup, createdVo, Role.VOADMIN);

    // check that user has VO
    AuthzRoles userRoles = authzResolverImpl.getRoles(testUser, false);
    assertFalse(userRoles.hasRole(Role.VOADMIN, createdVo));
  }

  @Test
  public void getRoleComplementaryObjectsWithAuthorizedGroupsTest() throws Exception {
    System.out.println(CLASS_NAME + "getRoleComplementaryObjectsWithAuthorizedGroupsTest");

    Vo anotherCreatedVo = perun.getVosManagerBl().createVo(sess, new Vo(2, "another VO", "another VO"));

    Member testMember = createSomeMember(createdVo);
    User testUser = perun.getUsersManagerBl().getUserByMember(sess, testMember);
    Group testGroup = setUpGroup(createdVo, testMember, "testGroup");
    Group anotherTestGroup = setUpGroup(anotherCreatedVo, testMember, "anotherTestGroup");
    Group testObserverGroup = setUpGroup(createdVo, testMember, "testObserverGroup");

    // set role for group
    AuthzResolver.setRole(sess, testGroup, createdVo, Role.VOADMIN);
    AuthzResolver.setRole(sess, anotherTestGroup, createdVo, Role.VOADMIN);
    AuthzResolver.setRole(sess, testObserverGroup, createdVo, Role.VOOBSERVER);

    // check that user has VO
    Map<String, Map<String, Map<Integer, List<Group>>>> rolesWithComplementaryObjectsWithAuthorizedGroups =
        authzResolverImpl.getRoleComplementaryObjectsWithAuthorizedGroups(testUser);

    // two role names expected: "VOADMIN" and "VOOBSERVER"
    assertEquals(2, rolesWithComplementaryObjectsWithAuthorizedGroups.keySet().size());
    assertTrue(rolesWithComplementaryObjectsWithAuthorizedGroups.containsKey("VOADMIN"));
    assertTrue(rolesWithComplementaryObjectsWithAuthorizedGroups.containsKey("VOOBSERVER"));

    // VOADMIN should have one associated complementary object type (VO)
    assertEquals(1, rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOADMIN").keySet().size());
    assertTrue(rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOADMIN").containsKey("vo"));

    // VOADMIN -> VO (type)   =>   should have one bean ID (createdVo ID)
    assertEquals(1, rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOADMIN").get("vo").keySet().size());
    assertTrue(
        rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOADMIN").get("vo").containsKey(createdVo.getId()));

    List<Group> groups =
        rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOADMIN").get("vo").get(createdVo.getId());
    assertEquals(2, groups.size());

    // VOADMIN -> VO (type) -> Group_1 (testGroup)
    // try to find specific group to be compared to
    List<Group> found = groups.stream().filter(group -> group.getId() == testGroup.getId()).toList();
    assertEquals(1, found.size());
    assertEquals(testGroup.getId(), found.get(0).getId());
    assertEquals(testGroup.getName(), found.get(0).getName());
    assertEquals(testGroup.getDescription(), found.get(0).getDescription());
    assertEquals(testGroup.getVoId(), found.get(0).getVoId());

    // VOADMIN -> VO (type) -> Group_2 (anotherTestGroup)
    // try to find specific group to be compared to
    found = groups.stream().filter(group -> group.getId() == anotherTestGroup.getId()).toList();
    assertEquals(1, found.size());
    assertEquals(anotherTestGroup.getId(), found.get(0).getId());
    assertEquals(anotherTestGroup.getName(), found.get(0).getName());
    assertEquals(anotherTestGroup.getDescription(), found.get(0).getDescription());
    assertEquals(anotherTestGroup.getVoId(), found.get(0).getVoId());

    // VOOBSERVER should have one complementary object type (VO)
    assertEquals(1, rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOOBSERVER").keySet().size());
    assertTrue(rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOOBSERVER").containsKey("vo"));

    // VOOBSERVER -> VO (type)   =>   should have one bean ID (createdVo ID)
    assertEquals(1, rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOOBSERVER").get("vo").keySet().size());
    assertTrue(
        rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOOBSERVER").get("vo").containsKey(createdVo.getId()));

    groups = rolesWithComplementaryObjectsWithAuthorizedGroups.get("VOOBSERVER").get("vo").get(createdVo.getId());
    assertEquals(1, groups.size());

    // VOOBSERVER -> VO -> Group_3 (testObserverGroup)
    found = groups.stream().filter(group -> group.getId() == testObserverGroup.getId()).toList();
    assertEquals(1, found.size());
    assertEquals(testObserverGroup.getId(), found.get(0).getId());
    assertEquals(testObserverGroup.getName(), found.get(0).getName());
    assertEquals(testObserverGroup.getDescription(), found.get(0).getDescription());
    assertEquals(testObserverGroup.getVoId(), found.get(0).getVoId());
  }

  @Test
  public void unsetRole() throws Exception {
    System.out.println(CLASS_NAME + "unsetRole");

    Map<String, Integer> mapping = prepareMapping(user2);

    authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
    authzResolverImpl.unsetRole(sess, mapping, Role.VOADMIN);

    assertFalse(AuthzResolverBlImpl.getUserRoleNames(sess, user2).contains(Role.VOADMIN));
  }

  @Test(expected = RoleNotSetException.class)
  public void unsetRoleWhichIsNotSet() throws Exception {
    System.out.println(CLASS_NAME + "unsetRoleWhichIsNotSet");

    Map<String, Integer> mapping = prepareMapping(user3);

    authzResolverImpl.unsetRole(sess, mapping, Role.VOADMIN);
  }

  @Test(expected = RoleAlreadySetException.class)
  public void setRoleWhichIsAlreadySet() throws Exception {
    System.out.println(CLASS_NAME + "setRoleWhichIsAlreadySet");

    Map<String, Integer> mapping = prepareMapping(user4);

    authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
    authzResolverImpl.setRole(sess, mapping, Role.VOADMIN);
  }

  private Map<String, Integer> prepareMapping(User user) {
    Map<String, Integer> mapping = new HashMap<>();

    mapping.put("user_id", user.getId());
    mapping.put("vo_id", createdVo.getId());
    mapping.put("role_id", authzResolverImpl.getRoleId(Role.VOADMIN));

    return mapping;
  }

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
