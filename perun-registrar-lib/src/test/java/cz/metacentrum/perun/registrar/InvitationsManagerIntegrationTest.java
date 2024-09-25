package cz.metacentrum.perun.registrar;

import static cz.metacentrum.perun.registrar.model.Application.AppType.INITIAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SortingOrder;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.registrar.api.InvitationsManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.InvitationWithSender;
import cz.metacentrum.perun.registrar.model.InvitationsOrderColumn;
import cz.metacentrum.perun.registrar.model.InvitationsPageQuery;
import java.time.LocalDate;
import java.util.ArrayList;
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
    sender = setUpUser("Invitation", "Sender", "preferredMail@mail.com");
    senderSess = setUpSenderSession(sender);
  }

  @Test
  public void getInvitationByApplication() throws PerunException {
    System.out.println("getInvitationByApplication");

    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);
    User sender = new User(-1, "Sender", "Sending", "", "", "");
    sender = perun.getUsersManagerBl().createUser(session, user);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());

    Invitation invitation = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation = invitationsManager.createInvitation(session, invitation);

    Application applicationToGroup = prepareApplicationToGroup(user, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>(), invitation.getToken());

    invitation = invitationsManager.getInvitationById(session, invitation.getId());
    assertEquals(invitation, invitationsManager.getInvitationByApplication(session, applicationToGroup));
  }

  @Test
  public void getInvitationByApplicationNotExist() throws PerunException {
    System.out.println("getInvitationByApplicationNotExist");

    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());
    registrarManager.approveApplication(session, applicationToVo.getId());

    Application applicationToGroup = prepareApplicationToGroup(user, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>());
    registrarManager.approveApplication(session, applicationToGroup.getId());

    assertNull(invitationsManager.getInvitationByApplication(session, applicationToGroup));
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

    User sender2 = setUpUser("Test", "Sender2", "preferredMail2@mail.com");
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

    invitationsManagerBl.canInvitationBeAccepted(session, invitation1.getToken(), group);
  }

  @Test(expected = InvitationNotExistsException.class)
  public void canInvitationBeAcceptedInvitationDoesNotExist() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedInvitationDoesNotExist");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    UUID nonExistingUUID = UUID.fromString("bdf5788f-55dd-4fcc-9566-fb38f9d6fcd2");
    invitationsManagerBl.canInvitationBeAccepted(session, nonExistingUUID, group);
  }

  @Test(expected = InvalidInvitationStatusException.class)
  public void canInvitationBeAcceptedInvitationNotPending() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedInvitationNotPending");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1.setStatus(InvitationStatus.ACCEPTED);
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitationsManagerBl.canInvitationBeAccepted(session, invitation1.getToken(), group);
  }

  @Test(expected = IllegalArgumentException.class)
  public void canInvitationBeAcceptedWrongGroup() throws Exception {
    System.out.println(CLASS_NAME + "canInvitationBeAcceptedWrongGroup");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    Group wrongGroup = new Group("name", "description");
    wrongGroup = perun.getGroupsManager().createGroup(session, vo, wrongGroup);

    invitation1.setStatus(InvitationStatus.ACCEPTED);
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitationsManagerBl.canInvitationBeAccepted(session, invitation1.getToken(), wrongGroup);
  }
  @Test
  public void extendInvitationExpiration() throws Exception {
    System.out.println(CLASS_NAME + "extendInvitationExpiration");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitation1 = invitationsManagerBl.extendInvitationExpiration(session, invitation1, LocalDate.now().plusMonths(2));
    assertEquals(LocalDate.now().plusMonths(2), invitation1.getExpiration());
  }

  @Test
  public void extendInvitationExpirationNoExpirationDate() throws Exception {
    System.out.println(CLASS_NAME + "extendInvitationExpirationNoExpirationDate");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusMonths(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitation1 = invitationsManagerBl.extendInvitationExpiration(session, invitation1, null);
    assertEquals(LocalDate.now().plusMonths(2), invitation1.getExpiration());
  }

  @Test(expected = IllegalArgumentException.class)
  public void extendInvitationExpirationNewExpirationEarlier() throws Exception {
    System.out.println(CLASS_NAME + "extendInvitationExpirationNewExpirationEarlier");

    Invitation invitation1 = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusMonths(1));
    invitation1 = invitationsManager.createInvitation(session, invitation1);

    invitation1 = invitationsManagerBl.extendInvitationExpiration(session, invitation1, LocalDate.now().plusDays(1));
    assertEquals(LocalDate.now().plusMonths(1), invitation1.getExpiration());
  }


  @Test
  public void getInvitationsPageBasic() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageBasic");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation3 = invitationsManager.createInvitation(session, new Invitation(3, vo.getId(), group.getId(), sender.getId(), "receiver3", "test3@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(3, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.ID, "", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(3, result.getData().size());
    assertEquals(invitation1.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageByStatus() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageByStatus");

    Invitation invitation1 = new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1));
    Invitation invitation2 = new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1));

    invitation1.setStatus(InvitationStatus.ACCEPTED);

    invitation1 = invitationsManager.createInvitation(session, invitation1);
    invitation2 = invitationsManager.createInvitation(session, invitation2);

    InvitationsPageQuery query = new InvitationsPageQuery(2, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.ID, "", List.of(InvitationStatus.ACCEPTED), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(1, result.getData().size());
    assertEquals(invitation1.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageSortedByStatus() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageSortedByStatus");

    Invitation invitation1 = new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1));
    Invitation invitation2 = new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1));
    Invitation invitation3 = new Invitation(3, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1));

    invitation1.setStatus(InvitationStatus.REVOKED);
    invitation2.setStatus(InvitationStatus.ACCEPTED);
    invitation3.setStatus(InvitationStatus.PENDING);

    invitation1 = invitationsManager.createInvitation(session, invitation1);
    invitation2 = invitationsManager.createInvitation(session, invitation2);
    invitation3 = invitationsManager.createInvitation(session, invitation3);

    InvitationsPageQuery query = new InvitationsPageQuery(3, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.STATUS, "", new ArrayList<>());

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(3, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageByExpiration() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageByExpiration");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(5)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test3@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(2, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.ID, "", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(1, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageByReceiverName() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageByReceiverName");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation3 = invitationsManager.createInvitation(session, new Invitation(3, vo.getId(), group.getId(), sender.getId(), "other", "test3@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(3, 0, SortingOrder.DESCENDING, InvitationsOrderColumn.RECEIVER_NAME, "receiver", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(2, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageByReceiverEmail() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageByReceiverEmail");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), sender.getId(), "receiver2", "test2@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation3 = invitationsManager.createInvitation(session, new Invitation(3, vo.getId(), group.getId(), sender.getId(), "receiver3", "other@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(3, 0, SortingOrder.DESCENDING, InvitationsOrderColumn.RECEIVER_EMAIL, "test", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(2, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageBySenderName() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageBySenderName");

    User otherSender = setUpUser("Other", "Sender", "otherPreferredMail@mail.com");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), otherSender.getId(), "receiver3", "test3@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(2, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.ID, "other", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(1, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
  }

  @Test
  public void getInvitationsPageBySenderEmail() throws Exception {
    System.out.println(CLASS_NAME + "getInvitationsPageBySenderEmail");

    User otherSender = setUpUser("Other", "Sender", "otherPreferredMail@mail.com");

    Invitation invitation1 = invitationsManager.createInvitation(session, new Invitation(1, vo.getId(), group.getId(), sender.getId(), "receiver1", "test1@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));
    Invitation invitation2 = invitationsManager.createInvitation(session, new Invitation(2, vo.getId(), group.getId(), otherSender.getId(), "receiver3", "test3@email.com", Locale.ENGLISH, LocalDate.now().plusDays(1)));

    InvitationsPageQuery query = new InvitationsPageQuery(2, 0, SortingOrder.ASCENDING, InvitationsOrderColumn.ID, "otherPreferredMail", List.of(InvitationStatus.PENDING), LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));

    Paginated<InvitationWithSender> result = invitationsManager.getInvitationsPage(session, group, query);

    assertEquals(1, result.getData().size());
    assertEquals(invitation2.getId(), result.getData().get(0).getId());
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

    ApplicationMail mail = new ApplicationMail(0, INITIAL, groupForm.getId(), ApplicationMail.MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new ApplicationMail.MailText(Locale.ENGLISH, "test","Submit your application here {preapprovedInvitationLink} until {expirationDate}"));

    mailManager.addMail(session, groupForm, mail);

    return group;
  }

  private User setUpUser(String firstName, String lastName, String email) {
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    perun.getUsersManagerBl().createUser(session, user);

    try {
      Attribute attrEmail = new Attribute(
          perun.getAttributesManagerBl()
              .getAttributeDefinition(session, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));
      attrEmail.setValue(email);
      perun.getAttributesManagerBl().setAttribute(session, user, attrEmail);
    } catch (AttributeNotExistsException | WrongAttributeValueException | WrongAttributeAssignmentException |
             WrongReferenceAttributeValueException ex) {
      // Ignore
    }
    return user;
  }

  private PerunSession setUpSenderSession(User sender) {
    PerunPrincipal senderPrincipal = new PerunPrincipal("sender", "", "",
        sender);
    return new PerunSessionImpl(session.getPerun(), senderPrincipal, session.getPerunClient());
  }

  private Application prepareApplicationToGroup(User user, Group group) {
    Application application = new Application();
    application.setVo(vo);
    application.setGroup(group);
    application.setUser(user);
    application.setId(-1);
    application.setCreatedBy(session.getPerunPrincipal().getActor());
    application.setType(Application.AppType.INITIAL);
    application.setExtSourceName("ExtSource");
    application.setExtSourceType(ExtSourcesManager.EXTSOURCE_IDP);
    return application;
  }

  private Application prepareApplicationToVo(User user) {
    Application application = new Application();
    application.setVo(vo);
    application.setUser(user);
    application.setId(-1);
    application.setCreatedBy(session.getPerunPrincipal().getActor());
    application.setType(Application.AppType.INITIAL);
    application.setExtSourceName("ExtSource");
    application.setExtSourceType(ExtSourcesManager.EXTSOURCE_IDP);
    return application;
  }
}
