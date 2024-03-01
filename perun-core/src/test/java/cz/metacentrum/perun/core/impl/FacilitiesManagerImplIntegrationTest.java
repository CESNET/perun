package cz.metacentrum.perun.core.impl;

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
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Flor <493294@mail.muni.cz>
 */
public class FacilitiesManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private final static String CLASS_NAME = "FacilitiesManagerImpl.";

  final ExtSource extSource = new ExtSource(0, "FacilitiesManagerExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private int userLoginSequence = 0;

  private FacilitiesManagerImplApi facilitiesManagerImpl;

  private PerunSession sess;
  private Vo vo;

  @Before
  public void setUp() throws Exception {
    facilitiesManagerImpl = (FacilitiesManagerImplApi) ReflectionTestUtils.getField(perun.getFacilitiesManagerBl(),
        "facilitiesManagerImpl");
    if (facilitiesManagerImpl == null) {
      throw new RuntimeException("Failed to get facilitiesManagerImpl");
    }

    sess = perun.getPerunSession(
        new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL),
        new PerunClient());

    vo = new Vo(0, "FacilitiesImplTestVo", "FcltImplTestVo");
    vo = perun.getVosManagerBl().createVo(sess, vo);

  }

  @Test
  public void getAdminsOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getAdminsOnlyValid");

    Member member1 = createSomeMember(vo);
    Member member2 = createSomeMember(vo);
    member2 = perun.getMembersManagerBl().invalidateMember(sess, member2);

    Facility facility = setUpFacility("testFac");

    Resource resource = setUpResource(vo, facility, "testRes");

    Group group1 = setUpGroup(vo, member1, "testGroup1");

    Group group2 = setUpGroup(vo, member2, "testGroup2");

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource, false, false, false);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);

    AuthzResolver.setRole(sess, group1, facility, Role.FACILITYADMIN);

    AuthzResolver.setRole(sess, group2, facility, Role.FACILITYADMIN);

    assertThat(facilitiesManagerImpl.getAdmins(sess, facility)).containsExactlyInAnyOrder(user1);
    perun.getMembersManagerBl().validateMember(sess, member2);
    assertThat(facilitiesManagerImpl.getAdmins(sess, facility)).containsExactlyInAnyOrder(user1, user2);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(facilitiesManagerImpl.getAdmins(sess, facility)).containsExactlyInAnyOrder(user2);
  }

  @Test
  public void getFacilitiesWhereUserIsAdminOnlyValid() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesWhereUserIsAdminOnlyValid");

    Member member1 = createSomeMember(vo);
    member1 = perun.getMembersManagerBl().validateMember(sess, member1);

    Facility facility = setUpFacility("testFac");

    Group group = setUpGroup(vo, member1, "testGroup");

    Resource resource = setUpResource(vo, facility, "testRes");
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);

    AuthzResolver.setRole(sess, group, facility, Role.FACILITYADMIN);

    assertThat(facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user1)).containsExactly(facility);
    perun.getMembersManagerBl().invalidateMember(sess, member1);
    assertThat(facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user1)).containsExactly();

    perun.getMembersManagerBl().validateMember(sess, member1);
    assertThat(facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user1)).containsExactly(facility);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group);
    assertThat(facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user1)).containsExactly();
  }


  // private methods ==============================================================

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

  private Resource setUpResource(Vo vo, Facility facility, String name) throws Exception {

    Resource resource = new Resource();
    resource.setName(name);
    resource.setDescription("Testovaci");
    resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
    return resource;

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
