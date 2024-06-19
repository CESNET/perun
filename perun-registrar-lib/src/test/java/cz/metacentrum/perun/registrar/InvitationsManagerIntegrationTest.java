package cz.metacentrum.perun.registrar;

import static org.junit.Assert.assertEquals;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.api.InvitationsManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InvitationsManagerIntegrationTest extends RegistrarBaseIntegrationTest {

  private static final String CLASS_NAME = "InvitationsManagerIntegrationTest.";
  private Vo vo;
  private Group group;
  private User sender;

  @Autowired
  private InvitationsManager invitationsManager;

  @Autowired
  private InvitationsManagerBl invitationsManagerBl;

  @Before
  public void setUp() throws Exception {
    vo = perun.getVosManagerBl().createVo(session, new Vo(0, "Test Vo", "TestVo"));
    group = setUpGroup("TestGroup", "Test group");
    sender = setUpUser("Invitation", "Sender");
  }

  @Test
  public void getInvitationById() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationById");

    Invitation invitationToCreate = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitationToCreate = invitationsManager.createInvitation(session, invitationToCreate);
    Invitation gotInvitation = invitationsManager.getInvitationById(session, invitationToCreate.getId());
    assertEquals(invitationToCreate, gotInvitation);
  }

  @Test
  public void getInvitationsForGroup() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsForGroup");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    Invitation invitation2 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation2 = invitationsManager.createInvitation(session, invitation2);

    List<Invitation> result = invitationsManager.getInvitationsForGroup(session, group);
    assertEquals(result.size(), 2);
  }

  @Test
  public void getInvitationsForVo() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsForVo");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    Invitation invitation2 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation2 = invitationsManager.createInvitation(session, invitation2);

    List<Invitation> result = invitationsManager.getInvitationsForVo(session, vo);
    assertEquals(result.size(), 2);
  }

  @Test
  public void getInvitationsForSender() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsForSender");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    Invitation invitation2 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation2 = invitationsManager.createInvitation(session, invitation2);

    Group group2 = setUpGroup("testGroup2", "test group 2");
    // invitation in another group - should not be in result
    Invitation invitation3 = new Invitation(0, vo.getId(), group2.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation3 = invitationsManager.createInvitation(session, invitation3);

    User sender2 = setUpUser("Test", "Sender2");
    // invitation by another sender - should not be in result
    Invitation invitation4 = new Invitation(0, vo.getId(), group.getId(), sender2.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation4 = invitationsManager.createInvitation(session, invitation4);

    List<Invitation> result = invitationsManager.getInvitationsForSender(session, group, sender);
    assertEquals(result.size(), 2);
  }

  @Test
  public void expireInvitation() throws Exception {
    System.out.println(CLASS_NAME + "expireInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.PENDING);

    invitation1 = invitationsManagerBl.expireInvitation(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.EXPIRED);
  }

  @Test(expected = InvalidInvitationStatusException.class)
  public void expireExpiredInvitation() throws Exception {
    System.out.println(CLASS_NAME + "expireExpiredInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    invitation1.setStatus(InvitationStatus.EXPIRED);

    invitation1 = invitationsManagerBl.expireInvitation(session, invitation1);
  }

  @Test
  public void revokeInvitation() throws Exception {
    System.out.println(CLASS_NAME + "revokeInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.PENDING);

    invitation1 = invitationsManagerBl.revokeInvitation(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.REVOKED);
  }

  @Test(expected = InvalidInvitationStatusException.class)
  public void revokeRevokedInvitation() throws Exception {
    System.out.println(CLASS_NAME + "revokeRevokedInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    invitation1.setStatus(InvitationStatus.EXPIRED);

    invitation1 = invitationsManagerBl.revokeInvitation(session, invitation1);
  }

  @Test
  public void acceptInvitation() throws Exception {
    System.out.println(CLASS_NAME + "acceptInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.PENDING);

    invitation1 = invitationsManagerBl.markInvitationAccepted(session, invitation1);
    assertEquals(invitation1.getStatus(), InvitationStatus.ACCEPTED);
  }

  @Test(expected = InvalidInvitationStatusException.class)
  public void acceptAcceptedInvitation() throws Exception {
    System.out.println(CLASS_NAME + "acceptAcceptedInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    invitation1.setStatus(InvitationStatus.ACCEPTED);

    invitation1 = invitationsManagerBl.markInvitationAccepted(session, invitation1);
  }

  private Group setUpGroup(String name, String desc) throws Exception {
    GroupsManager groupsManager = perun.getGroupsManager();

    // create group in VO, generate group application form
    Group group = new Group(name, desc);
    group = groupsManager.createGroup(session, vo, group);

    registrarManager.createApplicationFormInGroup(session, group);
    ApplicationForm groupForm = registrarManager.getFormForGroup(group);
    groupForm.setAutomaticApproval(true);
    registrarManager.updateForm(session, groupForm);

    return group;
  }

  private User setUpUser(String firstName, String lastName) {
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    perun.getUsersManagerBl().createUser(session, user);
    return user;
  }
}
