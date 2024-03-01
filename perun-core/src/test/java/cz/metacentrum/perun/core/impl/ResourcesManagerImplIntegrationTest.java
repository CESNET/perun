package cz.metacentrum.perun.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class ResourcesManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "ResourcesManagerImpl.";

  final ExtSource extSource = new ExtSource(0, "ResourcesManagerExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private int userLoginSequence = 0;

  private ResourcesManagerImplApi resourcesManagerImpl;

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
  public void getAdminsOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getAdminsOnlyValid");

    Member member1 = createSomeMember(vo);
    Member member2 = createSomeMember(vo);
    member2 = perun.getMembersManagerBl().invalidateMember(sess, member2);

    Group group1 = setUpGroup(vo, member1, "testGroup1");
    Group group2 = setUpGroup(vo, member2, "testGroup2");

    Facility facility = setUpFacility("testFac");

    Resource resource = setUpResource(vo, facility, "testRes");

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);

    AuthzResolver.setRole(sess, group1, resource, Role.RESOURCEADMIN);

    AuthzResolver.setRole(sess, group2, resource, Role.RESOURCEADMIN);

    assertThat(resourcesManagerImpl.getAdmins(sess, resource)).containsExactlyInAnyOrder(user1);
    perun.getMembersManagerBl().validateMember(sess, member2);
    assertThat(resourcesManagerImpl.getAdmins(sess, resource)).containsExactlyInAnyOrder(user1, user2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(resourcesManagerImpl.getAdmins(sess, resource)).containsExactly(user2);
  }

  @Test
  public void getResourcesWhereUserIsAdminOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesWhereUserIsAdminOnlyValid");

    Member member1 = createSomeMember(vo);

    Group group1 = setUpGroup(vo, member1, "testGroup");

    Facility facility = setUpFacility("testFac");

    Resource resource1 = setUpResource(vo, facility, "testRes");
    Resource resource2 = setUpResource(vo, facility, "testRes2");

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource1, false, false, false);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);

    AuthzResolver.setRole(sess, group1, resource1, Role.RESOURCEADMIN);
    AuthzResolver.setRole(sess, user1, resource2, Role.RESOURCEADMIN);


    assertThat(resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(resource1,
        resource2);
    perun.getMembersManagerBl().invalidateMember(sess, member1);
    // should still be an admin of resource2, since admin status wasn't derived from group membership
    assertThat(resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(resource2);

    perun.getMembersManagerBl().validateMember(sess, member1);
    assertThat(resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(resource1,
        resource2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user1)).containsExactlyInAnyOrder(resource2);
  }

  // private methods ==============================================================

  @Before
  public void setUp() throws Exception {
    resourcesManagerImpl =
        (ResourcesManagerImplApi) ReflectionTestUtils.getField(perun.getResourcesManagerBl(), "resourcesManagerImpl");
    if (resourcesManagerImpl == null) {
      throw new RuntimeException("Failed to get resourcesManagerImpl");
    }
    sess = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
        ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());

    vo = new Vo(0, "ResourcesImplTestVo", "ResourcesImplTestVo");
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

  private Facility setUpFacility(String name) throws Exception {

    Facility facility = new Facility();
    facility.setName(name);
    facility = perun.getFacilitiesManager().createFacility(sess, facility);
        /*
             Owner owner = new Owner();
             owner.setName("ResourcesManagerTestOwner");
             owner.setContact("testingOwner");
             perun.getOwnersManager().createOwner(sess, owner);
             perun.getFacilitiesManager().addOwner(sess, facility, owner);
             */
    return facility;

  }

  private Group setUpGroup(Vo vo, Member member, String name) throws Exception {

    Group group = new Group(name, "test group");
    group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

    perun.getGroupsManagerBl().addMember(sess, group, member);

    return group;
  }

  private Resource setUpResource(Vo vo, Facility facility, String name) throws Exception {

    Resource resource = new Resource();
    resource.setName(name);
    resource.setDescription("Testovaci");
    resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
    return resource;

  }
}
