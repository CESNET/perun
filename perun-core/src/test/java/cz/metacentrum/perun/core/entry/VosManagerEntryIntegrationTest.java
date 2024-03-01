package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.EnrichedBanOnVo;
import cz.metacentrum.perun.core.api.EnrichedVo;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.MemberWithSponsors;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.blImpl.VosManagerBlImpl.A_MEMBER_DEF_MEMBER_ORGANIZATIONS;
import static cz.metacentrum.perun.core.blImpl.VosManagerBlImpl.A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests of VosManager.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class VosManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private final static String extSourceName = "VosManagerEntryIntegrationTest";
  private final static String CLASS_NAME = "VosManager.";
  private final int someNumber = 55;
  private final String voShortName = "TestShortN-" + someNumber;
  private final String voName = "Test Vo Name " + someNumber;
  private VosManager vosManagerEntry;
  private Vo myVo;
  private ExtSource es;

  @Before
  public void setUp() throws Exception {
    vosManagerEntry = perun.getVosManager();
    myVo = new Vo(0, voName, voShortName);
    ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
    es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);

  }

  @Test
  public void createVo() throws Exception {
    System.out.println(CLASS_NAME + "createVo");

    final Vo newVo = vosManagerEntry.createVo(sess, myVo);

    assertTrue("id must be greater than zero", newVo.getId() > 0);
  }

  @Test
  public void createAndUpdateVoWithLongShortName() throws Exception {
    System.out.println(CLASS_NAME + "createAndUpdateVoWithLongShortName");
    String longName = "1234567890123456789";
    String longerName = "12345678901234567890123456789012";
    Vo voWithLongShortname = new Vo(0, longName, longName);

    Vo newVo = vosManagerEntry.createVo(sess, voWithLongShortname);
    assertTrue("id must be greater than zero", newVo.getId() > 0);

    newVo.setShortName(longerName);
    newVo = vosManagerEntry.updateVo(sess, newVo);
    assertTrue("newVo shortName has 32 characters length", newVo.getShortName().length() == 32);
  }

  @Test(expected = VoExistsException.class)
  public void createVoWhichAlreadyExists() throws Exception {
    System.out.println(CLASS_NAME + "createVoWhichAlreadyExists");

    vosManagerEntry.createVo(sess, myVo);
    // this should throw exception
    vosManagerEntry.createVo(sess, myVo);
  }

  @Test
  public void createVoSetsUUID() throws Exception {
    System.out.println(CLASS_NAME + "createVoSetsUUID");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    assertThat(createdVo.getUuid()).isNotNull();
    assertThat(createdVo.getUuid().version()).isEqualTo(4);
  }

  @Test
  public void getVoById() throws Exception {
    System.out.println(CLASS_NAME + "getVoById");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    final Vo returnedVo = vosManagerEntry.getVoById(sess, createdVo.getId());

    final String createVoFailMsg = "The created vo is not ok, maybe try to check createVo()?";

    assertNotNull(createVoFailMsg, createdVo);
    assertNotNull("returned vo should not be null", returnedVo);

    assertEquals(createdVo.getId(), returnedVo.getId());
    assertEquals("name is not the same", createdVo.getName(), returnedVo.getName());
    assertEquals("shortName is not the same", createdVo.getShortName(), returnedVo.getShortName());
    assertThat(returnedVo.getUuid()).isNotNull();
    assertThat(returnedVo.getUuid().version()).isEqualTo(4);
  }

  @Test
  public void getVosByIds() throws Exception {
    System.out.println(CLASS_NAME + "getVosByIds");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    List<Vo> vos = vosManagerEntry.getVosByIds(sess, Collections.singletonList(createdVo.getId()));
    assertEquals(vos.size(), 1);
    assertTrue(vos.contains(createdVo));

    Vo anotherVo = vosManagerEntry.createVo(sess, new Vo(0, myVo.getName() + "2nd", myVo.getShortName() + "2nd"));
    vos = vosManagerEntry.getVosByIds(sess, Arrays.asList(createdVo.getId(), anotherVo.getId()));
    assertEquals(vos.size(), 2);
    assertTrue(vos.contains(createdVo));
    assertTrue(vos.contains(anotherVo));

    vos = vosManagerEntry.getVosByIds(sess, Collections.singletonList(anotherVo.getId()));
    assertEquals(vos.size(), 1);
    assertTrue(vos.contains(anotherVo));
  }

  @Test
  public void getVoByShortName() throws Exception {
    System.out.println(CLASS_NAME + "getVoByShortName");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    final Vo returnedVo = vosManagerEntry.getVoByShortName(sess, voShortName);

    assertEquals(createdVo, returnedVo);
  }

  @Test(expected = VoNotExistsException.class)
  public void getVoWhichNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getVoWhichNotExists");

    final String nonExistingShortName = "_i_am_not_in_db_";
    vosManagerEntry.getVoByShortName(sess, nonExistingShortName);
  }

  @Test
  public void getVos() throws Exception {
    System.out.println(CLASS_NAME + "getVos");

    final Vo vo = vosManagerEntry.createVo(sess, myVo);
    final List<Vo> vos = vosManagerEntry.getVos(sess);

    assertTrue(vos.contains(vo));
  }

  @Test
  public void getVosNotNull() throws Exception {
    System.out.println(CLASS_NAME + "getVosNotNull");

    // should not never return null or throw exception, even if no result
    // found
    assertNotNull(vosManagerEntry.getVos(sess));
  }

  @Test
  public void updateVo() throws Exception {
    System.out.println(CLASS_NAME + "updateVo");

    Vo voToUpdate = vosManagerEntry.createVo(sess, myVo);
    voToUpdate.setName("Cosa");
    voToUpdate.setShortName("Nostra");
    final Vo updatedVo = vosManagerEntry.updateVo(sess, voToUpdate);

    assertEquals(voToUpdate, updatedVo);
  }

  @Test(expected = VoNotExistsException.class)
  public void updateVoWhichNotExists() throws Exception {
    System.out.println(CLASS_NAME + "updateVoWhichNotExists");

    vosManagerEntry.updateVo(sess, new Vo());
  }

  @Test
  @Ignore
  public void findCandidates() throws Exception {
    System.out.println(CLASS_NAME + "findCandidates");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    addExtSourceDelegate(createdVo);

    final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
        createdVo, "kouril");

    // TODO tohle se mi moc nelibi, kde se nejaci candidates vzali? To by
    // bylo dobre plnit na zacatku testu db nebo je vytvorit rucne zde
    assertTrue("Candidates count must be greater than 0",
        candidates.size() > 0);
    removeExtSourceDelegate(createdVo);
  }

  @Test(expected = VoNotExistsException.class)
  @Ignore
  public void findCandidatesForNonExistingVo() throws Exception {
    System.out.println(CLASS_NAME + "findCandidatesForNonExistingVo");

    addExtSourceDelegate(new Vo());

    vosManagerEntry.findCandidates(sess, new Vo(), "kouril");
  }

  @Test
  @Ignore
  public void findCandidatesWithOneResult() throws Exception {
    System.out.println(CLASS_NAME + "findCandidatesWithOneResult");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    addExtSourceDelegate(createdVo);

    final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
        createdVo, "kouril", 1);

    assertEquals(1, candidates.size());
    removeExtSourceDelegate(createdVo);
  }


  @Test
  public void addAdmin() throws Exception {
    System.out.println(CLASS_NAME + "addAdmin");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    final Member member = createMemberFromExtSource(createdVo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    vosManagerEntry.addAdmin(sess, createdVo, user);
    final List<User> admins = vosManagerEntry.getAdmins(sess, createdVo);

    assertNotNull(admins);
    assertTrue(admins.size() > 0);
  }

  @Test(expected = VoNotExistsException.class)
  public void addAdminIntoNonExistingVo() throws Exception {
    System.out.println(CLASS_NAME + "addAdminIntoNonExistingVo");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    final Member member = createMemberFromExtSource(createdVo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);

    vosManagerEntry.addAdmin(sess, new Vo(), user);
  }

  @Test(expected = UserNotExistsException.class)
  public void addAdminAsNonExistingUser() throws Exception {
    System.out.println(CLASS_NAME + "addAdminAsNonExistingMember");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    vosManagerEntry.addAdmin(sess, createdVo, new User());
  }

  @Test
  public void addAdminWithGroup() throws Exception {
    System.out.println(CLASS_NAME + "addAdminWithGroup");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    // set up authorized group
    Group authorizedGroup = new Group("authorizedGroup", "testovaciGroup");
    Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
    vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

    final List<Group> admins = vosManagerEntry.getAdminGroups(sess, createdVo);

    assertNotNull(admins);
    assertTrue(admins.size() > 0);
    assertTrue(admins.contains(authorizedGroup));
  }

  @Test
  public void getAdmins() throws Exception {
    System.out.println(CLASS_NAME + "getAdmins");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    // set up first user
    final Member member = createMemberFromExtSource(createdVo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    vosManagerEntry.addAdmin(sess, createdVo, user);

    // set up authorized group
    Group authorizedGroup = new Group("authorizedGroup", "testovaciGroup");
    Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
    vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

    // set up second user
    Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
    candidate.setFirstName("Josef");
    candidate.setId(4);
    candidate.setMiddleName("");
    candidate.setLastName("Novak");
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource",
        "cz.metacentrum.perun.core.impl.ExtSourceInternal"), Long.toHexString(Double.doubleToLongBits(Math.random())));
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());

    Member member2 = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    perun.getGroupsManager().addMember(sess, returnedGroup, member2);

    // test
    List<User> admins = vosManagerEntry.getAdmins(sess, createdVo);
    assertTrue("should have 2 admins", admins.size() == 2);
    assertTrue("our member as direct user should be admin", admins.contains(user));
    assertTrue("our member as member of admin group should be admin", admins.contains(user2));
  }

  @Test
  public void getDirectAdmins() throws Exception {
    System.out.println(CLASS_NAME + "getDirectAdmins");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    final Member member = createMemberFromExtSource(createdVo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);

    vosManagerEntry.addAdmin(sess, createdVo, user);
    assertTrue(vosManagerEntry.getDirectAdmins(sess, createdVo).contains(user));
  }

  @Test
  public void getAdminGroups() throws Exception {
    System.out.println(CLASS_NAME + "getAdminGroups");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    final Group group = new Group("testGroup", "just for testing");
    perun.getGroupsManager().createGroup(sess, createdVo, group);

    vosManagerEntry.addAdmin(sess, createdVo, group);
    assertTrue(vosManagerEntry.getAdminGroups(sess, createdVo).contains(group));
  }

  @Test(expected = UserNotExistsException.class)
  public void removeAdminWhichNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeAdminWhichNotExists");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    vosManagerEntry.removeAdmin(sess, createdVo, new User());
  }

  @Test
  public void removeAdmin() throws Exception {
    System.out.println(CLASS_NAME + "removeAdmin");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    final Member member = createMemberFromExtSource(createdVo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);

    vosManagerEntry.addAdmin(sess, createdVo, user);
    assertTrue(vosManagerEntry.getAdmins(sess, createdVo).contains(user));

    vosManagerEntry.removeAdmin(sess, createdVo, user);
    assertFalse(vosManagerEntry.getAdmins(sess, createdVo).contains(user));
  }

  @Test
  public void removeAdminWithGroup() throws Exception {
    System.out.println(CLASS_NAME + "removeAdminWithGroup");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    // set up authorized group
    Group authorizedGroup = new Group("authorizedGroup", "testovaciGroup");
    Group returnedGroup = perun.getGroupsManager().createGroup(sess, createdVo, authorizedGroup);
    vosManagerEntry.addAdmin(sess, createdVo, returnedGroup);

    vosManagerEntry.removeAdmin(sess, createdVo, returnedGroup);
    assertFalse(vosManagerEntry.getAdminGroups(sess, createdVo).contains(returnedGroup));
  }

  @Test(expected = VoNotExistsException.class)
  public void deleteVo() throws Exception {
    System.out.println(CLASS_NAME + "deleteVo");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    vosManagerEntry.deleteVo(sess, createdVo);
    vosManagerEntry.getVoById(sess, createdVo.getId());
  }

  @Test(expected = VoNotExistsException.class)
  public void deleteVoWhichNotExists() throws Exception {
    System.out.println(CLASS_NAME + "deleteVoWhichNotExists");

    vosManagerEntry.deleteVo(sess, new Vo());
  }

  @Test
  public void forceDeleteVoWithGroupAndSubgroup() throws Exception {
    System.out.println(CLASS_NAME + "forceDeleteVoWithGroupAndSubgroup");

    // create vo with group and subgroup
    myVo = perun.getVosManagerBl().createVo(sess, myVo);
    Group group = perun.getGroupsManager().createGroup(sess, myVo, new Group("group", "testingGroup"));
    Group subgroup = perun.getGroupsManager().createGroup(sess, myVo, new Group("subgroup", "testingSubgroup"));
    perun.getGroupsManager().moveGroup(sess, group, subgroup);

    // force delete vo
    vosManagerEntry.deleteVo(sess, myVo, true);
    assertThatExceptionOfType(VoNotExistsException.class)
        .isThrownBy(() -> vosManagerEntry.getVoById(sess, myVo.getId()));
  }

  @Test
  public void setBanCorrectly() throws Exception {
    System.out.println(CLASS_NAME + "setBanCorrectly");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    ban = vosManagerEntry.setBan(sess, ban);

    assertThat(ban.getId()).isNotNull();
    assertThat(ban.getVoId()).isNotNull();

    BanOnVo foundBan = vosManagerEntry.getBanById(sess, ban.getId());

    assertThat(foundBan).isEqualToIgnoringNullFields(ban);
  }

  @Test
  public void removeBanCorrectly() throws Exception {
    System.out.println(CLASS_NAME + "removeBanCorrectly");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    vosManagerEntry.setBan(sess, ban);

    vosManagerEntry.removeBan(sess, ban.getId());

    assertThatExceptionOfType(BanNotExistsException.class)
        .isThrownBy(() -> vosManagerEntry.getBanById(sess, ban.getId()));
  }

  @Test
  public void removeBanForMemberCorrectly() throws Exception {
    System.out.println(CLASS_NAME + "removeBanForMemberCorrectly");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    vosManagerEntry.setBan(sess, ban);

    vosManagerEntry.removeBanForMember(sess, member);

    assertThat(vosManagerEntry.getBanForMember(sess, member))
        .isNull();
  }

  @Test
  public void getBanByIdCorrectly() throws Exception {
    System.out.println(CLASS_NAME + "getBanByIdCorrectly");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    ban = vosManagerEntry.setBan(sess, ban);

    BanOnVo foundBan = vosManagerEntry.getBanById(sess, ban.getId());

    assertThat(foundBan).isEqualToIgnoringNullFields(ban);
  }

  @Test
  public void getBanForMemberCorrectly() throws Exception {
    System.out.println(CLASS_NAME + "getBanForMemberCorrectly");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    ban = vosManagerEntry.setBan(sess, ban);

    BanOnVo foundBan = vosManagerEntry.getBanForMember(sess, member);

    assertThat(foundBan).isEqualToIgnoringNullFields(ban);
  }

  @Test
  public void getBanForMemberReturnsNullIfNoBanExists() throws Exception {
    System.out.println(CLASS_NAME + "getBanForMemberReturnsNullIfNoBanExists");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo foundBan = vosManagerEntry.getBanForMember(sess, member);

    assertThat(foundBan).isNull();
  }

  @Test
  public void getBansForVo() throws Exception {
    System.out.println(CLASS_NAME + "getBansForVo");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    vosManagerEntry.setBan(sess, ban);

    List<BanOnVo> voBans = vosManagerEntry.getBansForVo(sess, createdVo.getId());

    assertThat(voBans).containsOnly(ban);
  }

  @Test
  public void updateBan() throws Exception {
    System.out.println(CLASS_NAME + "updateBan");
    Vo vo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(vo);

    BanOnVo banOnVo = new BanOnVo();
    banOnVo.setMemberId(member.getId());
    banOnVo.setDescription("Description");
    banOnVo.setValidityTo(new Date());
    banOnVo = vosManagerEntry.setBan(sess, banOnVo);
    banOnVo.setDescription("New description");
    banOnVo.setValidityTo(new Date(banOnVo.getValidityTo().getTime() + 1000000));
    vosManagerEntry.updateBan(sess, banOnVo);

    BanOnVo returnedBan = vosManagerEntry.getBanById(sess, banOnVo.getId());
    assertEquals(banOnVo, returnedBan);
  }

  @Test
  public void getEnrichedBansForVoWithAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForVoWithAttributes");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    Member member = createMemberFromExtSource(createdVo);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());

    vosManagerEntry.setBan(sess, ban);

    List<String> attrNames = List.of(AttributesManager.NS_MEMBER_ATTR_CORE + ":id");
    List<EnrichedBanOnVo> enrichedBans = vosManagerEntry.getEnrichedBansForVo(sess, createdVo.getId(), attrNames);

    assertThat(enrichedBans).hasSize(1);
    assertThat(enrichedBans.get(0).getBan()).isEqualTo(ban);
    assertThat(enrichedBans.get(0).getMember()).isEqualTo(member);
    assertThat(enrichedBans.get(0).getVo()).isEqualTo(createdVo);
    assertThat(enrichedBans.get(0).getMember().getMemberAttributes()).hasSize(1);
    assertThat(enrichedBans.get(0).getMember().getMemberAttributes().get(0).getFriendlyName()).isEqualTo("id");
  }

  @Test
  public void getEnrichedBansForUserWithAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForUserWithAttributes");

    Vo createdVo = vosManagerEntry.createVo(sess, myVo);
    User user = perun.getUsersManagerBl()
        .createUser(sess, new User(-1, "BanManagerTestUser", "BanManagerTestUser", "BanManagerTestUser", "", ""));
    Member member = perun.getMembersManagerBl().createMember(sess, createdVo, user);

    BanOnVo ban = new BanOnVo();
    ban.setMemberId(member.getId());
    ban = vosManagerEntry.setBan(sess, ban);

    // Create other Vo, Member and Ban
    Vo otherVo = new Vo(-1, "Other vo", "othervo");
    otherVo = perun.getVosManagerBl().createVo(sess, otherVo);

    Member otherMember = perun.getMembersManagerBl().createMember(sess, otherVo, user);

    BanOnVo otherBan = new BanOnVo(-1, otherMember.getId(), otherVo.getId(), new Date(), "no reason");
    vosManagerEntry.setBan(sess, otherBan);

    List<String> attrNames = List.of(AttributesManager.NS_MEMBER_ATTR_CORE + ":id");

    List<EnrichedBanOnVo> enrichedBans = perun.getVosManagerBl().getEnrichedBansForUser(sess, user.getId(), attrNames);
    assertThat(enrichedBans).hasSize(2);
    // get each ban from the resulting list, the order is not set so have to check
    EnrichedBanOnVo memberBan =
        enrichedBans.stream().filter(enrichedBanOnVo -> enrichedBanOnVo.getMember().equals(member)).findFirst().get();
    EnrichedBanOnVo otherMemberBan =
        enrichedBans.stream().filter(enrichedBanOnVo -> enrichedBanOnVo.getMember().equals(otherMember)).findFirst()
            .get();

    assertThat(memberBan.getVo()).isEqualTo(createdVo);
    assertThat(otherMemberBan.getVo()).isEqualTo(otherVo);
    assertThat(memberBan.getMember().getId()).isEqualTo(member.getId());
    assertThat(otherMemberBan.getMember().getId()).isEqualTo(otherMember.getId());

    assertThat(memberBan.getBan()).isEqualTo(ban);
    assertThat(otherMemberBan.getBan()).isEqualTo(otherBan);

    assertThat(memberBan.getMember().getMemberAttributes()).hasSize(1);
    assertThat(memberBan.getMember().getMemberAttributes().get(0).getFriendlyName()).isEqualTo("id");
    assertThat(otherMemberBan.getMember().getMemberAttributes()).hasSize(1);
    assertThat(otherMemberBan.getMember().getMemberAttributes().get(0).getFriendlyName()).isEqualTo("id");
  }

  @Test
  public void convertSponsoredUsers() throws Exception {
    System.out.println(CLASS_NAME + "convertSponsoredUsers");

    myVo = vosManagerEntry.createVo(sess, myVo);

    User user = new User(-1, "Sponsored", "User", "", "", "");
    user.setSponsoredUser(true);
    User sponsor1 = new User(-1, "Sponsor 1", "", "", "", "");
    User sponsor2 = new User(-1, "Sponsor 2", "", "", "", "");

    user = perun.getUsersManagerBl().createUser(sess, user);
    sponsor1 = perun.getUsersManagerBl().createUser(sess, sponsor1);
    sponsor2 = perun.getUsersManagerBl().createUser(sess, sponsor2);

    perun.getUsersManagerBl().addSpecificUserOwner(sess, sponsor1, user);
    perun.getUsersManagerBl().addSpecificUserOwner(sess, sponsor2, user);

    Member member = perun.getMembersManagerBl().createMember(sess, myVo, user);

    perun.getVosManager().convertSponsoredUsers(sess, myVo);

    List<MemberWithSponsors> sponsoredMembersAndTheirSponsors = perun.getMembersManager()
        .getSponsoredMembersAndTheirSponsors(sess, myVo, Collections.emptyList());

    assertThat(sponsoredMembersAndTheirSponsors).hasSize(1);
    Member sponsoredMember = sponsoredMembersAndTheirSponsors.get(0).getMember();
    List<Sponsor> sponsors = sponsoredMembersAndTheirSponsors.get(0).getSponsors();
    assertThat(sponsoredMember).isEqualTo(member);
    assertThat(sponsors).contains(new Sponsor(new RichUser(sponsor1, Collections.emptyList())));
    assertThat(sponsors).contains(new Sponsor(new RichUser(sponsor2, Collections.emptyList())));
  }

  @Test
  public void convertSponsoredUsersWithNewSponsor() throws Exception {
    System.out.println(CLASS_NAME + "convertSponsoredUsersWithNewSponsor");

    myVo = vosManagerEntry.createVo(sess, myVo);

    User user = new User(-1, "Sponsored", "User", "", "", "");
    user.setSponsoredUser(true);
    User originalSponsor = new User(-1, "Sponsor 1", "", "", "", "");
    User newSponsor = new User(-1, "Sponsor 2", "", "", "", "");

    user = perun.getUsersManagerBl().createUser(sess, user);
    originalSponsor = perun.getUsersManagerBl().createUser(sess, originalSponsor);
    newSponsor = perun.getUsersManagerBl().createUser(sess, newSponsor);

    perun.getUsersManagerBl().addSpecificUserOwner(sess, originalSponsor, user);

    Member member = perun.getMembersManagerBl().createMember(sess, myVo, user);

    perun.getVosManager().convertSponsoredUsersWithNewSponsor(sess, myVo, newSponsor);

    List<MemberWithSponsors> sponsoredMembersAndTheirSponsors = perun.getMembersManager()
        .getSponsoredMembersAndTheirSponsors(sess, myVo, Collections.emptyList());

    assertThat(sponsoredMembersAndTheirSponsors).hasSize(1);
    Member sponsoredMember = sponsoredMembersAndTheirSponsors.get(0).getMember();
    List<Sponsor> sponsors = sponsoredMembersAndTheirSponsors.get(0).getSponsors();
    assertThat(sponsoredMember).isEqualTo(member);
    assertThat(sponsors).contains(new Sponsor(new RichUser(newSponsor, Collections.emptyList())));
  }

  @Test
  public void getCompleteCandidatesFromGroup() throws Exception {
    System.out.println(CLASS_NAME + "getCompleteCandidatesFromGroup");

    // create group and vo
    myVo = perun.getVosManagerBl().createVo(sess, myVo);
    Group group = new Group("testGroup", "testingGroup");
    Group returnedGroup = perun.getGroupsManager().createGroup(sess, myVo, group);

    // prepare second extSource
    ExtSource extSource = new ExtSource("testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
    extSource = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

    // prepare users to be returned by getCompleteCandidates
    Candidate candidate = prepareCandidateWithExtSource("Jan", es);
    User userToContain1 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Jana", es);
    User userToContain2 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Josef", es);
    User userNotToContain1 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Jan", extSource);
    RichUser userNotToContain2 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Jana", extSource);
    RichUser userToContain3 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    Member member = perun.getMembersManagerBl().createMember(sess, myVo, candidate);

    List<MemberCandidate> completeCandidates = perun.getVosManagerBl()
        .getCompleteCandidates(sess, myVo, returnedGroup, null, "jan", Arrays.asList(es));

    List<RichUser> usersOfCompleteCandidates = completeCandidates.stream()
        .map(MemberCandidate::getRichUser)
        .collect(Collectors.toList());

    assertEquals("Three users should have been returned.", 3, usersOfCompleteCandidates.size());
    assertTrue("User should've been returned.", usersOfCompleteCandidates.contains(userToContain1));
    assertTrue("User should've been returned.", usersOfCompleteCandidates.contains(userToContain2));
    assertTrue("User should've been returned.", usersOfCompleteCandidates.contains(userToContain3));
    assertFalse("User shouldn't have been returned.", usersOfCompleteCandidates.contains(userNotToContain1));
    assertFalse("User shouldn't have been returned.", usersOfCompleteCandidates.contains(userNotToContain2));
  }

  @Test
  public void getCompleteCandidatesFromGroupWithNullVo() throws Exception {
    System.out.println(CLASS_NAME + "getCompleteCandidatesFromGroupWithNullVo");

    // create group and vo
    myVo = perun.getVosManagerBl().createVo(sess, myVo);
    Group group = new Group("testGroup", "testingGroup");
    Group returnedGroup = perun.getGroupsManager().createGroup(sess, myVo, group);

    // prepare second extSource
    ExtSource extSource = new ExtSource("testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
    extSource = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

    // prepare users to be returned by getCompleteCandidates
    Candidate candidate = prepareCandidateWithExtSource("Jan", es);
    User userToContain1 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Josef", es);
    User userNotToContain1 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));
    candidate = prepareCandidateWithExtSource("Jana", extSource);
    RichUser userToContain2 = perun.getUsersManagerBl()
        .getRichUser(sess, perun.getUsersManagerBl().createUser(sess, candidate));

    List<MemberCandidate> completeCandidates = perun.getVosManagerBl()
        .getCompleteCandidates(sess, null, returnedGroup, null, "jan", Arrays.asList(es));

    List<RichUser> usersOfCompleteCandidates = completeCandidates.stream()
        .map(MemberCandidate::getRichUser)
        .collect(Collectors.toList());

    assertEquals("Three users should have been returned.", 2, usersOfCompleteCandidates.size());
    assertTrue("User should've been returned.", usersOfCompleteCandidates.contains(userToContain1));
    assertTrue("User should've been returned.", usersOfCompleteCandidates.contains(userToContain2));
    assertFalse("User shouldn't have been returned.", usersOfCompleteCandidates.contains(userNotToContain1));
  }

  @Test
  public void getVoMembersCountsByStatus() throws Exception {
    System.out.println(CLASS_NAME + "getVoMembersCountsByStatus");

    myVo = perun.getVosManagerBl().createVo(sess, myVo);

    createMemberFromExtSource(myVo);

    Member disabledMember = createMemberFromExtSource(myVo);
    perun.getMembersManager().setStatus(sess, disabledMember, Status.DISABLED);

    Map<Status, Integer> counts = vosManagerEntry.getVoMembersCountsByStatus(sess, myVo);
    assertThat(counts.get(Status.VALID)).isEqualTo(1);
    assertThat(counts.get(Status.DISABLED)).isEqualTo(1);
    assertThat(counts.get(Status.INVALID)).isEqualTo(0);
    assertThat(counts.get(Status.EXPIRED)).isEqualTo(0);
  }

  @Test(expected = VoNotExistsException.class)
  public void getVoMembersCountsByStatusWhenVoNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getVoMembersCountsByStatusWhenVoNotExists");

    vosManagerEntry.getVoMembersCountsByStatus(sess, new Vo());
  }

  @Test
  public void getMemberVos() throws Exception {
    System.out.println(CLASS_NAME + "getMemberVos");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = new Vo(-1, "Vo2", "vo2");
    memberVo = perun.getVosManagerBl().createVo(sess, memberVo);
    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThat(vosManagerEntry.getMemberVos(sess, vo.getId())).containsExactly(memberVo);
    assertThat(vosManagerEntry.getMemberVos(sess, memberVo.getId())).containsExactly();
  }

  @Test
  public void getParentVos() throws Exception {
    System.out.println(CLASS_NAME + "getParentVos");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = new Vo(-1, "Vo2", "vo2");
    memberVo = perun.getVosManagerBl().createVo(sess, memberVo);
    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThat(vosManagerEntry.getParentVos(sess, vo.getId())).containsExactly();
    assertThat(vosManagerEntry.getParentVos(sess, memberVo.getId())).containsExactly(vo);
  }

  @Test
  public void getParentVosNoRelations() throws Exception {
    System.out.println(CLASS_NAME + "getParentVosNoRelations");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    assertThat(vosManagerEntry.getParentVos(sess, vo.getId())).isEmpty();
  }

  @Test
  public void removeNonexistingMemberVo() throws Exception {
    System.out.println(CLASS_NAME + "removeNonexistingMemberVo");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo vo2 = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    assertThatExceptionOfType(RelationNotExistsException.class)
        .isThrownBy(() -> vosManagerEntry.removeMemberVo(sess, vo, vo2));
  }

  @Test
  public void addMemberVo() throws Exception {
    System.out.println(CLASS_NAME + "addMemberVo");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    vosManagerEntry.addMemberVo(sess, vo, memberVo);
    assertThat(vosManagerEntry.getMemberVos(sess, vo.getId())).containsExactly(memberVo);
  }

  @Test
  public void addMemberVoParent() throws Exception {
    System.out.println(CLASS_NAME + "addMemberVo");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    vosManagerEntry.addMemberVo(sess, vo, memberVo);
    assertThat(vosManagerEntry.getMemberVos(sess, vo.getId())).containsExactly(memberVo);

    assertThatExceptionOfType(RelationExistsException.class)
        .isThrownBy(() -> vosManagerEntry.addMemberVo(sess, memberVo, vo));
  }

  @Test
  public void removeMemberVo() throws Exception {
    System.out.println(CLASS_NAME + "removeMemberVo");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThat(vosManagerEntry.getMemberVos(sess, vo.getId())).containsExactly(memberVo);

    vosManagerEntry.removeMemberVo(sess, vo, memberVo);
    assertThat(vosManagerEntry.getMemberVos(sess, vo.getId())).containsExactly();
  }

  @Test
  public void addExistingMemberVo() throws Exception {
    System.out.println(CLASS_NAME + "addExistingMemberVo");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo vo2 = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    vosManagerEntry.addMemberVo(sess, vo, vo2);
    assertThatExceptionOfType(RelationExistsException.class)
        .isThrownBy(() -> vosManagerEntry.addMemberVo(sess, vo, vo2));
  }

  @Test
  public void getEnrichedVos() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedVos");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo parent = perun.getVosManagerBl().createVo(sess, new Vo(-1, "parent", "parent"));
    Vo member = perun.getVosManagerBl().createVo(sess, new Vo(-2, "member", "member"));

    vosManagerEntry.addMemberVo(sess, vo, member);
    vosManagerEntry.addMemberVo(sess, parent, vo);

    assertThat(vosManagerEntry.getEnrichedVos(sess).size()).isEqualTo(3);
    EnrichedVo enrichedVo = vosManagerEntry.getEnrichedVos(sess).stream()
        .filter(enrichedVo1 -> enrichedVo1.getVo().equals(vo)).toList().get(0);

    assertThat(enrichedVo.getVo()).isEqualTo(vo);
    assertThat(enrichedVo.getMemberVos()).containsOnly(member);
    assertThat(enrichedVo.getParentVos()).containsOnly(parent);
  }

  @Test
  public void getEnrichedVoById() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedVoById");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo parent = perun.getVosManagerBl().createVo(sess, new Vo(-1, "parent", "parent"));
    Vo member = perun.getVosManagerBl().createVo(sess, new Vo(-2, "member", "member"));

    vosManagerEntry.addMemberVo(sess, vo, member);
    vosManagerEntry.addMemberVo(sess, parent, vo);

    EnrichedVo enrichedVo = vosManagerEntry.getEnrichedVoById(sess, vo.getId());

    assertThat(enrichedVo.getVo()).isEqualTo(vo);
    assertThat(enrichedVo.getMemberVos()).containsOnly(member);
    assertThat(enrichedVo.getParentVos()).containsOnly(parent);
  }

  @Test
  public void addMemberVoNoExistingMember() throws Exception {
    System.out.println(CLASS_NAME + "addMemberVoNoExistingMember");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    Member member = createMemberFromExtSource(memberVo);
    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThat(perun.getMembersManagerBl().getMembers(sess, vo).size()).isEqualTo(1);
    assertThat(perun.getMembersManagerBl().getMembers(sess, vo).get(0).getUserId()).isEqualTo(member.getUserId());

    List<Member> membersInParentVo = perun.getMembersManagerBl().getMembers(sess, vo);
    assertThat(membersInParentVo.size()).isEqualTo(1);
    assertThat(
        perun.getAttributesManagerBl().getAttribute(sess, membersInParentVo.get(0), A_MEMBER_DEF_MEMBER_ORGANIZATIONS)
            .valueAsList())
        .contains(memberVo.getShortName());
    assertThat(perun.getAttributesManagerBl()
        .getAttribute(sess, membersInParentVo.get(0), A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY).valueAsList())
        .contains(memberVo.getShortName());
  }

  @Test
  public void addMemberVoExistingMember() throws Exception {
    System.out.println(CLASS_NAME + "addMemberVoExistingMember");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo1 = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    Vo memberVo2 = perun.getVosManagerBl().createVo(sess, new Vo(-2, "Vo3", "vo3"));

    Member member1 = createMemberFromExtSource(memberVo1);
    Member member2 = perun.getMembersManagerBl()
        .createMember(sess, memberVo2, perun.getUsersManagerBl().getUserByMember(sess, member1));
    perun.getMembersManagerBl().validateMember(sess, member2);
    vosManagerEntry.addMemberVo(sess, vo, memberVo1);
    vosManagerEntry.addMemberVo(sess, vo, memberVo2);

    List<Member> membersInParentVo = perun.getMembersManagerBl().getMembers(sess, vo);
    assertThat(membersInParentVo.size()).isEqualTo(1);
    assertThat(
        perun.getAttributesManagerBl().getAttribute(sess, membersInParentVo.get(0), A_MEMBER_DEF_MEMBER_ORGANIZATIONS)
            .valueAsList())
        .contains(memberVo1.getShortName(), memberVo2.getShortName());
    assertThat(perun.getAttributesManagerBl()
        .getAttribute(sess, membersInParentVo.get(0), A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY).valueAsList())
        .contains(memberVo1.getShortName(), memberVo2.getShortName());
  }

  @Test
  public void removeMemberVoClearsMemberOrganizationsAttribute() throws Exception {
    System.out.println(CLASS_NAME + "removeMemberVoClearsMemberOrganizationsAttribute");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    Member member = createMemberFromExtSource(memberVo);
    vosManagerEntry.addMemberVo(sess, vo, memberVo);
    Member memberInParent = perun.getMembersManagerBl().getMemberByUserId(sess, vo, member.getUserId());

    List<String> expectedAttributeValue = new ArrayList<>(List.of(memberVo.getShortName()));

    assertThat(perun.getAttributesManagerBl().getAttribute(sess, memberInParent, A_MEMBER_DEF_MEMBER_ORGANIZATIONS)
        .valueAsList())
        .isEqualTo(expectedAttributeValue);
    assertThat(
        perun.getAttributesManagerBl().getAttribute(sess, memberInParent, A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY)
            .valueAsList())
        .isEqualTo(expectedAttributeValue);

    vosManagerEntry.removeMemberVo(sess, vo, memberVo);
    assertThat(perun.getAttributesManagerBl().getAttribute(sess, memberInParent, A_MEMBER_DEF_MEMBER_ORGANIZATIONS)
        .valueAsList())
        .isNullOrEmpty();
    assertThat(
        perun.getAttributesManagerBl().getAttribute(sess, memberInParent, A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY)
            .valueAsList())
        .isEqualTo(expectedAttributeValue);
  }

  @Test
  public void deleteParentVoInHierarchy() throws Exception {
    System.out.println(CLASS_NAME + "deleteParentVoInHierarchy");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThatExceptionOfType(InternalErrorException.class).isThrownBy(() -> vosManagerEntry.deleteVo(sess, vo, false));

    vosManagerEntry.deleteVo(sess, vo, true);
    assertThatExceptionOfType(VoNotExistsException.class).isThrownBy(() ->
        vosManagerEntry.getVoById(sess, vo.getId()));
  }

  @Test
  public void deleteMemberVoInHierarchy() throws Exception {
    System.out.println(CLASS_NAME + "deleteMemberVoInHierarchy");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));

    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    assertThatExceptionOfType(InternalErrorException.class).isThrownBy(
        () -> vosManagerEntry.deleteVo(sess, memberVo, false));

    vosManagerEntry.deleteVo(sess, memberVo, true);
    assertThatExceptionOfType(VoNotExistsException.class).isThrownBy(() ->
        vosManagerEntry.getVoById(sess, memberVo.getId()));
  }

  @Test
  public void addMemberWithMemberVoResetsExpiration() throws Exception {
    System.out.println(CLASS_NAME + "addMemberWithMemberVoResetsExpiration");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    Member memberWithExpiration = createMemberFromExtSource(vo);
    Member memberWithoutExpiration = perun.getMembersManagerBl()
        .createMember(sess, memberVo, perun.getUsersManagerBl().getUserByMember(sess, memberWithExpiration));
    perun.getMembersManagerBl().validateMember(sess, memberWithoutExpiration);

    String membershipExpirationAttrName = perun.getAttributesManager().NS_MEMBER_ATTR_DEF + ":membershipExpiration";
    String expirationValue = "2020-02-02";

    AttributeDefinition attrDef =
        perun.getAttributesManagerBl().getAttributeDefinition(sess, membershipExpirationAttrName);
    perun.getAttributesManagerBl().setAttribute(sess, memberWithExpiration, new Attribute(attrDef, expirationValue));

    AttributeDefinition memberOrgsAttrDef =
        perun.getAttributesManagerBl().getAttributeDefinition(sess, A_MEMBER_DEF_MEMBER_ORGANIZATIONS);
    perun.getAttributesManagerBl().setAttribute(sess, memberWithExpiration,
        new Attribute(memberOrgsAttrDef, new ArrayList<>(List.of(vo.getShortName()))));
    perun.getAttributesManagerBl().setAttribute(sess, memberWithoutExpiration,
        new Attribute(memberOrgsAttrDef, new ArrayList<>(List.of(vo.getShortName()))));

    AttributeDefinition memberOrgsHistoryAttrDef =
        perun.getAttributesManagerBl().getAttributeDefinition(sess, A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY);
    perun.getAttributesManagerBl().setAttribute(sess, memberWithExpiration,
        new Attribute(memberOrgsHistoryAttrDef, new ArrayList<>(List.of(vo.getShortName()))));
    perun.getAttributesManagerBl().setAttribute(sess, memberWithoutExpiration,
        new Attribute(memberOrgsHistoryAttrDef, new ArrayList<>(List.of(vo.getShortName()))));

    assertEquals(perun.getAttributesManagerBl().getAttribute(sess, memberWithExpiration, membershipExpirationAttrName)
        .getValue(), expirationValue);
    assertNull(perun.getAttributesManagerBl().getAttribute(sess, memberWithoutExpiration, membershipExpirationAttrName)
        .getValue());

    vosManagerEntry.addMemberVo(sess, vo, memberVo);
    assertNull(perun.getAttributesManagerBl().getAttribute(sess, memberWithExpiration, membershipExpirationAttrName)
        .getValue());
    assertNull(perun.getAttributesManagerBl().getAttribute(sess, memberWithoutExpiration, membershipExpirationAttrName)
        .getValue());
  }

  @Test
  public void removeMemberVoStartsParentVoLifecycle() throws Exception {
    System.out.println(CLASS_NAME + "removeMemberVoStartsParentVoLifecycle");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    Member member = createMemberFromExtSource(memberVo);
    perun.getMembersManagerBl().validateMember(sess, member);

    // set expiration rules for parent vo
    Attribute membershipExpirationRulesAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
    LinkedHashMap<String, String> expirationRules = new LinkedHashMap<>();
    expirationRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "01.01.");
    membershipExpirationRulesAttribute.setValue(expirationRules);
    perun.getAttributesManagerBl().setAttribute(sess, vo, membershipExpirationRulesAttribute);

    vosManagerEntry.addMemberVo(sess, vo, memberVo);

    String membershipExpirationAttrName = perun.getAttributesManager().NS_MEMBER_ATTR_DEF + ":membershipExpiration";
    Member memberInParentVo = perun.getMembersManagerBl().getMemberByUserId(sess, vo, member.getUserId());

    Attribute membersExpirationAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, memberInParentVo, membershipExpirationAttrName);
    assertThat(membersExpirationAttribute.getValue() == null);

    vosManagerEntry.removeMemberVo(sess, vo, memberVo);

    membersExpirationAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, memberInParentVo, membershipExpirationAttrName);
    assertThat(membersExpirationAttribute.toString().endsWith("-01-01"));
  }

  @Test
  public void addFirstMemberVoSetsMemberOrganizations() throws Exception {
    System.out.println(CLASS_NAME + "addFirstMemberVoSetsMemberOrganizations");

    Vo vo = perun.getVosManagerBl().createVo(sess, myVo);
    Vo memberVo = perun.getVosManagerBl().createVo(sess, new Vo(-1, "Vo2", "vo2"));
    Member member = createMemberFromExtSource(vo);

    perun.getVosManagerBl().addMemberVo(sess, vo, memberVo);

    assertThat(perun.getAttributesManagerBl().getAttribute(sess, member, A_MEMBER_DEF_MEMBER_ORGANIZATIONS)
        .valueAsList()).containsOnly(vo.getShortName());
    assertThat(perun.getAttributesManagerBl().getAttribute(sess, member, A_MEMBER_DEF_MEMBER_ORGANIZATIONS_HISTORY)
        .valueAsList()).containsOnly(vo.getShortName());
  }

  // private methods ------------------------------------------------------------------

  private Member createMemberFromExtSource(final Vo createdVo) throws Exception {

    //This is obsolete approach which is dependent on extSource, remove these lines in future...
    //addExtSourceDelegate(createdVo);
    //final List<Candidate> candidates = vosManagerEntry.findCandidates(sess,
    //		createdVo, "kouril", 1);

    final Candidate candidate = prepareCandidate();

    final MembersManager membersManagerEntry = perun.getMembersManager();
    final Member member = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);//candidates.get(0));
    assertNotNull("No member created", member);
    usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
    // save user for deletion after test
    return member;
  }

  private Candidate prepareCandidate() {

    String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String extLogin =
        Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

    final Candidate candidate = new Candidate();//Mockito.mock(Candidate.class);
    candidate.setFirstName(userFirstName);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(userLastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
    final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;

  }

  private void addExtSourceDelegate(final Vo createdVo) throws Exception {
    ExtSourcesManager esme = perun.getExtSourcesManager();
    esme.addExtSource(sess, createdVo, es);
  }

  private void removeExtSourceDelegate(Vo createdVo) throws Exception {
    ExtSourcesManager esme = perun.getExtSourcesManager();
    esme.removeExtSource(sess, createdVo, es);
  }

  private Candidate prepareCandidateWithExtSource(String name, ExtSource es) {
    String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));

    final Candidate candidate = new Candidate();
    candidate.setFirstName(name);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(userLastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    final UserExtSource userExtSource = new UserExtSource(es, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;
  }

  @Test
  public void getVosCount() throws Exception {
    System.out.println(CLASS_NAME + "getVosCount");

    final Vo createdVo = vosManagerEntry.createVo(sess, myVo);

    int count = vosManagerEntry.getVosCount(sess);
    assertTrue(count > 0);
  }

}
