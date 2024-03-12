package cz.metacentrum.perun.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnVo;
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
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class VosManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "VosManagerImpl.";

  final ExtSource extSource = new ExtSource(0, "VosManagerExtSource", ExtSourcesManager.EXTSOURCE_LDAP);
  private int userLoginSequence = 0;

  private User user;
  private Member member;
  private Member otherMember;
  private Vo vo;
  private Vo otherVo;

  private VosManagerImplApi vosManagerImpl;

  private Member createSomeMember(final Vo createdVo)
      throws ExtendMembershipException, AlreadyMemberException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {
    final Candidate candidate = setUpCandidate("Login" + userLoginSequence++);
    final Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
    return createdMember;
  }

  @Test
  public void getAdminsOnlyValidMembers() throws Exception {
    System.out.println(CLASS_NAME + "getAdminsOnlyValidMembers");

    Member member1 = createSomeMember(vo);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);

    Group group1 = setUpGroup(vo, member1, "testGroup");

    AuthzResolver.setRole(sess, group1, this.vo, Role.VOADMIN);

    assertThat(vosManagerImpl.getAdmins(sess, vo, Role.VOADMIN)).containsExactly(user1);

    perun.getMembersManagerBl().invalidateMember(sess, member1);
    assertThat(vosManagerImpl.getAdmins(sess, vo, Role.VOADMIN)).containsExactly();

    perun.getMembersManagerBl().validateMember(sess, member1);
    assertThat(vosManagerImpl.getAdmins(sess, vo, Role.VOADMIN)).containsExactly(user1);

    perun.getGroupsManagerBl().expireMemberInGroup(sess, member1, group1);
    assertThat(vosManagerImpl.getAdmins(sess, vo, Role.VOADMIN)).containsExactly();
  }

  @Test
  public void getBanById() throws Exception {
    System.out.println(CLASS_NAME + "getBanById");

    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    originBan = vosManagerImpl.setBan(sess, originBan);

    BanOnVo actualBan = vosManagerImpl.getBanById(sess, originBan.getId());

    isValidBan(actualBan, originBan.getId(), originBan.getMemberId(), originBan.getVoId(), originBan.getValidityTo(),
        originBan.getDescription());
  }

  @Test
  public void getBanForMember() throws Exception {
    System.out.println(CLASS_NAME + "getBanForMember");

    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    originBan = vosManagerImpl.setBan(sess, originBan);

    BanOnVo actualBan = vosManagerImpl.getBanById(sess, originBan.getId());

    isValidBan(actualBan, originBan.getId(), originBan.getMemberId(), originBan.getVoId(), originBan.getValidityTo(),
        originBan.getDescription());
  }

  @Test
  public void getBansForVo() {
    System.out.println(CLASS_NAME + "getBansForVo");

    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    originBan = vosManagerImpl.setBan(sess, originBan);

    BanOnVo otherBan = new BanOnVo(-1, otherMember.getId(), otherVo.getId(), new Date(), "noob");
    vosManagerImpl.setBan(sess, otherBan);

    List<BanOnVo> voBans = vosManagerImpl.getBansForVo(sess, vo.getId());

    assertThat(voBans).containsOnly(originBan);
  }

  private void isValidBan(BanOnVo ban, int banId, int memberId, int voId, Date validity, String description) {
    assertThat(ban.getId()).isEqualTo(banId);
    assertThat(ban.getMemberId()).isEqualTo(memberId);
    assertThat(ban.getVoId()).isEqualTo(voId);
    assertThat(ban.getValidityTo()).isEqualTo(validity);
    assertThat(ban.getDescription()).isEqualTo(description);

    assertThat(ban.getCreatedAt()).isNotNull();
    assertThat(ban.getCreatedBy()).isNotNull();
    assertThat(ban.getCreatedByUid()).isNotNull();
    assertThat(ban.getModifiedAt()).isNotNull();
    assertThat(ban.getModifiedBy()).isNotNull();
    assertThat(ban.getModifiedByUid()).isNotNull();
  }

  @Test
  public void removeBan() throws Exception {
    System.out.println(CLASS_NAME + "removeBan");

    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    vosManagerImpl.setBan(sess, originBan);

    vosManagerImpl.removeBan(sess, originBan.getId());

    assertThatExceptionOfType(BanNotExistsException.class).isThrownBy(
        () -> vosManagerImpl.getBanById(sess, originBan.getId()));
  }

  @Test
  public void setBan() throws Exception {
    System.out.println(CLASS_NAME + "setBan");

    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    originBan = vosManagerImpl.setBan(sess, originBan);

    BanOnVo actualBan = vosManagerImpl.getBanForMember(sess, originBan.getMemberId());

    assertThat(originBan).isEqualTo(actualBan);
  }

  @Before
  public void setUp() throws Exception {
    MembersManagerImplApi membersManagerImplApi =
        (MembersManagerImplApi) ReflectionTestUtils.getField(perun.getMembersManagerBl(), "membersManagerImpl");
    if (membersManagerImplApi == null) {
      throw new RuntimeException("Failed to get membersManagerImpl");
    }

    vosManagerImpl = (VosManagerImplApi) ReflectionTestUtils.getField(perun.getVosManagerBl(), "vosManagerImpl");
    if (vosManagerImpl == null) {
      throw new RuntimeException("Failed to get vosManagerImpl");
    }

    user = new User(-1, "John", "Doe", "", "", "");
    user = perun.getUsersManagerBl().createUser(sess, user);

    vo = new Vo(-1, "Vo", "vo");
    vo = perun.getVosManagerBl().createVo(sess, vo);

    member = membersManagerImplApi.createMember(sess, vo, user);

    otherVo = new Vo(-1, "Other vo", "othervo");
    otherVo = perun.getVosManagerBl().createVo(sess, otherVo);

    otherMember = membersManagerImplApi.createMember(sess, otherVo, user);
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


  // private methods ==============================================================

  private void testUpdateBan(Consumer<BanOnVo> banChange) throws Exception {
    BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
    originBan = vosManagerImpl.setBan(sess, originBan);
    originBan = vosManagerImpl.getBanById(sess, originBan.getId());

    banChange.accept(originBan);

    vosManagerImpl.updateBan(sess, originBan);

    BanOnVo updatedBan = vosManagerImpl.getBanById(sess, originBan.getId());

    assertThat(updatedBan).isEqualByComparingTo(originBan);
  }

  @Test
  public void updateBanDescription() throws Exception {
    System.out.println(CLASS_NAME + "updateBanDescription");

    testUpdateBan(ban -> ban.setDescription("Updated Description"));
  }

  @Test
  public void updateBanValidity() throws Exception {
    System.out.println(CLASS_NAME + "updateBanValidity");

    testUpdateBan(ban -> ban.setValidityTo(new Date(1434343L)));
  }
}
