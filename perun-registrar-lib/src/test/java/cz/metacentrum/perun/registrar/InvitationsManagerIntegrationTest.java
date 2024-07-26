package cz.metacentrum.perun.registrar;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.registrar.api.InvitationsManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-registrar-lib.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public class InvitationsManagerIntegrationTest {
  @Autowired
  PerunBl perun;
  @Autowired
  RegistrarManager registrarManager;
  @Autowired
  MailManager mailManager;
  PerunSession session;
  private static final String CLASS_NAME = "InvitationsManagerIntegrationTest.";
  private Vo vo;
  private Group group;
  private User sender;
  private PerunSession senderSess;

  @Autowired
  private InvitationsManager invitationsManager;

  @Autowired
  private InvitationsManagerBl invitationsManagerBl;

  @Before
  public void setUp() throws Exception {
    session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
          ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
    vo = perun.getVosManagerBl().createVo(session, new Vo(0, "Test Vo", "TestVo"));
    group = setUpGroup("TestGroup", "Test group");
    sender = setUpUser("Invitation", "Sender");
    senderSess = setUpSenderSession(sender);
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
    assertEquals(2, result.size());
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
    assertEquals(2, result.size());
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
    assertEquals(2, result.size());
  }

  @Test
  public void expireInvitation() throws Exception {
    System.out.println(CLASS_NAME + "expireInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(InvitationStatus.PENDING, invitation1.getStatus());

    invitation1 = invitationsManagerBl.expireInvitation(session, invitation1);
    assertEquals(InvitationStatus.EXPIRED, invitation1.getStatus());
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
  public void revokeInvitationById() throws Exception {
    System.out.println(CLASS_NAME + "revokeInvitationById");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(InvitationStatus.PENDING, invitation1.getStatus());

    invitation1 = invitationsManager.revokeInvitationById(session, invitation1.getId());
    assertEquals(InvitationStatus.REVOKED, invitation1.getStatus());
  }

  @Test
  public void revokeInvitationByUuid() throws Exception {
    System.out.println(CLASS_NAME + "revokeInvitationByUuid");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(InvitationStatus.PENDING, invitation1.getStatus());

    invitation1 = invitationsManager.revokeInvitationByUuid(session, invitation1.getToken());
    assertEquals(InvitationStatus.REVOKED, invitation1.getStatus());
  }
  
  @Test(expected = InvalidInvitationStatusException.class)
  public void revokeRevokedInvitation() throws Exception {
    System.out.println(CLASS_NAME + "revokeRevokedInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1.setStatus(InvitationStatus.REVOKED);
    
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    
    invitation1 = invitationsManager.revokeInvitationById(session, invitation1.getId());
  }

  @Test
  public void acceptInvitation() throws Exception {
    System.out.println(CLASS_NAME + "acceptInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);
    assertEquals(InvitationStatus.PENDING, invitation1.getStatus());

    invitation1 = invitationsManagerBl.markInvitationAccepted(session, invitation1);
    assertEquals(InvitationStatus.ACCEPTED, invitation1.getStatus());
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

  @Test
  public void createInvitationUrl() throws Exception {
    System.out.println(CLASS_NAME + "createInvitationUrl");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
    "receiver name", "receiver@email.com", Locale.ENGLISH,
    LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    Attribute authTypeAttr = new Attribute(perun.getAttributesManager().getAttributeDefinition(session,
        AttributesManager.NS_GROUP_ATTR_DEF + ":authType"));
    authTypeAttr.setValue("krb");
    perun.getAttributesManager().setAttribute(session, group, authTypeAttr);


    String url = invitationsManagerBl.createInvitationUrl(session, invitation1.getToken().toString());
    assertEquals("perun-dev/krb/registrar/?vo=" + vo.getShortName() +"&group=" + group.getShortName() +
                     "&token=" + invitation1.getToken(), url);
  }

  @Test
  public void inviteToGroup() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroup");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      Invitation invitation = invitationsManagerBl.inviteToGroup(senderSess, vo, group, "test receiver",
          "test@receiver.com", "en", null, "");


      verify(spyMailSender, times(1)).send(any(MimeMessage.class));
    } finally {
        ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void inviteToGroupCorrectlySetFields() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupCorrectlySetFields");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      doNothing().when(spyMailSender).send(any(MimeMessage.class));
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      Invitation invitation = invitationsManagerBl.inviteToGroup(senderSess, vo, group, "test receiver",
          "test@receiver.com", "en", null, "");

      assertEquals(InvitationStatus.PENDING, invitation.getStatus());
      assertEquals(sender.getId(), invitation.getSenderId());
      assertEquals(LocalDate.now().plusMonths(1), invitation.getExpiration());
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test(expected = RegistrarException.class)
  public void inviteToGroupIncorrectEmail() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupIncorrectEmail");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      doNothing().when(spyMailSender).send(any(MimeMessage.class));
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      Invitation invitation = invitationsManagerBl.inviteToGroup(senderSess, vo, group, "test receiver",
          "test-receiver.com", "en", null, "");
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void inviteToGroupSendingFailed() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupSendingFailed");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      doThrow(new MailSendException("test")).when(spyMailSender).send(any(MimeMessage.class));
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      Invitation invitation = invitationsManagerBl.inviteToGroup(senderSess, vo, group, "test receiver",
          "test@receiver.com", "en", null, "");

      assertEquals(InvitationStatus.UNSENT, invitation.getStatus());
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void inviteToGroupFromCsvSendCalled() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupFromCsvSendCalled");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      List<String> data = Arrays.asList("receiver1@email.com;Receiver Name1", "receiver2@email.com;Receiver Name2");
      Map<String, String> result = invitationsManagerBl.inviteToGroupFromCsv(senderSess, vo, group, data, "en",
          null, "");

      verify(spyMailSender, times(2)).send(any(MimeMessage.class));
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void inviteToGroupFromCsvResultOk() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupFromCsvResultOk");
    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);

    List<String> data = Arrays.asList("receiver1@email.com;Receiver Name1", "receiver2@email.com;Receiver Name2");
    try {
      doNothing().when(spyMailSender).send(any(MimeMessage.class));
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);
      Map<String, String> result = invitationsManagerBl.inviteToGroupFromCsv(senderSess, vo, group, data, "en",
          null, "");
      Map<String, String> expected = new HashMap<>();
      expected.put("receiver1@email.com - Receiver Name1", "OK");
      expected.put("receiver2@email.com - Receiver Name2", "OK");
      assertEquals(expected, result);
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void inviteToGroupFromCsvResultError() throws Exception {
    System.out.println(CLASS_NAME + "inviteToGroupFromCsvResultError");

    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    try {
      doThrow(new MailSendException("test")).when(spyMailSender).send(any(MimeMessage.class));
      ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

      List<String> data = Arrays.asList("receiver1@email.com;Receiver Name1", "receiver2@email.com;Receiver Name2");
      Map<String, String> result = invitationsManagerBl.inviteToGroupFromCsv(senderSess, vo, group, data, "en",
          null, "");
      List<Boolean> resultVals = result.values().stream().map(val -> (val.contains("ERROR:"))).toList();
      assertEquals(Arrays.asList(true, true), resultVals);
    } finally {
      ReflectionTestUtils.setField(mailManager, "mailSender", mailSender);
    }

  }

  @Test
  public void canInvitationBeAcceptedExistingPendingInvitation() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedExistingPendingInvitation");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitationsManagerBl.canInvitationBeAccepted(session, invitation1.getToken());
  }

  @Test(expected = InvitationNotExistsException.class)
  public void canInvitationBeAcceptedInvitationDoesNotExist() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedInvitationDoesNotExist");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    UUID nonExistingUUID = UUID.fromString("bdf5788f-55dd-4fcc-9566-fb38f9d6fcd2");
    invitationsManagerBl.canInvitationBeAccepted(session, nonExistingUUID);
  }

  @Test(expected = InvalidInvitationStatusException.class)
  public void canInvitationBeAcceptedInvitationNotPending() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedInvitationNotPending");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1.setStatus(InvitationStatus.ACCEPTED);
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitationsManagerBl.canInvitationBeAccepted(session, invitation1.getToken());
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

  private PerunSession setUpSenderSession(User sender) {
    PerunPrincipal senderPrincipal = new PerunPrincipal("sender", "", "",
        sender);
    return new PerunSessionImpl(session.getPerun(), senderPrincipal, session.getPerunClient());
  }
}
