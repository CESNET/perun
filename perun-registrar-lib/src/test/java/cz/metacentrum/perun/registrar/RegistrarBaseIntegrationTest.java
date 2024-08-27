package cz.metacentrum.perun.registrar;

import static cz.metacentrum.perun.registrar.model.Application.AppType.INITIAL;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.CS;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.EN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SortingOrder;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FormItemNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAllowedToAutoRegistrationException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InvalidHtmlInputException;
import cz.metacentrum.perun.core.api.exceptions.MultipleApplicationFormItemsException;
import cz.metacentrum.perun.core.api.exceptions.OpenApplicationExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.ApplicationMailTextMissingException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.impl.InvitationsManagerImpl;
import cz.metacentrum.perun.registrar.impl.RegistrarManagerImpl;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailText;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;
import cz.metacentrum.perun.registrar.model.ApplicationOperationResult;
import cz.metacentrum.perun.registrar.model.ApplicationsOrderColumn;
import cz.metacentrum.perun.registrar.model.ApplicationsPageQuery;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.RichApplication;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base registrar-lib test class
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-registrar-lib.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public class RegistrarBaseIntegrationTest {


  @Autowired
  PerunBl perun;
  @Autowired
  RegistrarManager registrarManager;
  @Autowired
  MailManager mailManager;
  @Autowired
  InvitationsManagerImpl invitationsManager;
  PerunSession session;
  private Vo vo;

  private static void applyForMembershipInVO(RegistrarManager registrarManager, PerunBl perun, Vo vo, PerunSession user)
      throws PerunException {

    Map<String, String> feder = new HashMap<>();
    feder.put("Shib-Person-displayName", "pplk. doc. Ing. Václav Rumcajs, DrSc.");
    feder.put("Shib-Person-commonName", "Václav Rumcajs");
    feder.put("Shib-Person-givenName", "Václav");
    feder.put("Shib-Person-sureName", "Rumcajs");
    feder.put("Shib-Person-o", "Les Řáholec");
    feder.put("Shib-EP-Affiliation", "member");
    feder.put("Shib-InetOrgPerson-mail", "mail@gmail.org");
    feder.put("Shib-EP-PrincipalName", user.getPerunPrincipal().getActor());

    user.getPerunPrincipal().getAdditionalInformations().putAll(feder);


    //data z federace a od uzivatele
    Application application = new Application();
    application.setType(INITIAL);
    application.setCreatedAt(user.getPerunPrincipal().getActor());
    application.setExtSourceName(user.getPerunPrincipal().getExtSourceName());
    application.setExtSourceType(ExtSourcesManager.EXTSOURCE_IDP);
    application.setFedInfo(feder.toString());
    application.setVo(vo);
    List<ApplicationFormItemWithPrefilledValue> prefilledForm =
        registrarManager.getFormItemsWithPrefilledValues(user, INITIAL, registrarManager.getFormForVo(vo));
    List<ApplicationFormItemData> data = new ArrayList<>();
    for (ApplicationFormItemWithPrefilledValue itemW : prefilledForm) {
      ApplicationFormItem item = itemW.getFormItem();
      //log.info("prefilled item "+itemW);
      if (item.getShortname().equals("preferredMail")) {
        data.add(new ApplicationFormItemData(item, item.getShortname(), "rumcajs@gmail.com", "0"));
      } else if (item.getShortname().equals("username")) {
        data.add(new ApplicationFormItemData(item, item.getShortname(), "rumcik", "0"));
      } else {
        //nechej predvyplnenou hodnotu
        data.add(new ApplicationFormItemData(item, item.getShortname(), itemW.getPrefilledValue(),
            itemW.getAssuranceLevel()));
      }
    }
    registrarManager.createApplication(user, application, data);
  }

  @Test
  public void addAndGetGroupsForAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.addAndGetGroupsForAutoRegistration");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);

    assertEquals(List.of(group), registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));
  }
  /*

         @Test
         public void stringTest() {

         String text = "anytext{login-egi-ui}anytext{neco}and{login-neco}";
         Pattern MY_PATTERN = Pattern.compile("\\-(.*?)\\}");

         Matcher m = MY_PATTERN.matcher(text);
         while (m.find()) {
         System.out.println(m.group(0));
         }

         Pattern MY_PATTERN2 = Pattern.compile("\\{login-[^\\}]+\\}");
         Matcher m2 = MY_PATTERN2.matcher(text);
         while (m2.find()) {
         System.out.println(m2.group(0));
         }

         }

         @Test
         public void getAppsTest() throws Exception {

         List<String> apps = new ArrayList<String>();
         apps.add("NEW");
         apps.add("VERIFIED");

// get compass
Vo vo = perun.getVosManager().getVoById(session, 321);
System.out.println(vo);
List<Application> result = registrarManager.getApplicationsForVo(session, vo, apps);
System.out.println("APPS ["+result.size()+"]:" + result);

         }

         @Test
         @Transactional
         public void testModule() throws PerunException {

         registrarManager.approveApplication(session, 1543);

         }

*/

  @Test(expected = FormItemNotExistsException.class)
  public void addAndGetGroupsForAutoRegistrationFormItemNotExists() throws Exception {
    System.out.println("RegistrarManager.addAndGetGroupsForAutoRegistrationFormItemNotExists");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    // create embedded groups form item but don't store it !!!
    ApplicationForm form = registrarManager.getFormForVo(vo);
    ApplicationFormItem embeddedGroupsItem = new ApplicationFormItem();
    embeddedGroupsItem.setType(ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION);
    embeddedGroupsItem.setShortname("embeddedGroups");

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);
  }

  @Test
  public void addAndGetGroupsForAutoRegistrationGroupForm() throws Exception {
    System.out.println("RegistrarManager.addAndGetGroupsForAutoRegistrationGroupForm");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group group2 = new Group("Group2", "Group2 description");
    perun.getGroupsManagerBl().createGroup(session, group, group2);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForGroupForm(group);

    registrarManager.addGroupsToAutoRegistration(session, List.of(group2), embeddedGroupsItem);

    assertEquals(List.of(group2), registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));
  }

  @Test(expected = GroupNotAllowedToAutoRegistrationException.class)
  public void addGroupWithSyncToAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.addGroupWithSyncToAutoRegistration");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    ExtSource extSource = new ExtSource(0, "testExSrc", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
    ExtSource es = perun.getExtSourcesManagerBl().createExtSource(session, extSource, null);
    perun.getExtSourcesManagerBl().addExtSource(session, vo, es);

    Attribute synchroAttr1 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME));
    synchroAttr1.setValue("5");
    perun.getAttributesManager().setAttribute(session, group, synchroAttr1);

    Attribute synchroAttr2 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
    synchroAttr2.setValue(es.getName());
    perun.getAttributesManager().setAttribute(session, group, synchroAttr2);

    Attribute synchroAttr3 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
    synchroAttr3.setValue("testVal");
    perun.getAttributesManager().setAttribute(session, group, synchroAttr3);

    Attribute synchroAttr4 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
    synchroAttr4.setValue("true");
    perun.getAttributesManager().setAttribute(session, group, synchroAttr4);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);
  }

  @Test
  public void addGroupsToAutoRegistration_emptyFormExpectedToBeCreated() throws PerunException {
    Group group1 = new Group("GroupA", "Cool folks");
    perun.getGroupsManager().createGroup(session, vo, group1);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group1), embeddedGroupsItem);

    assertNotNull(registrarManager.getFormForGroup(group1));
  }

  @Test(expected = GroupNotAllowedToAutoRegistrationException.class)
  public void addMemberGroupToAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.addMemberGroupToAutoRegistration");

    Group membersGroup = perun.getGroupsManagerBl().getGroupByName(session, vo, perun.getVosManager().MEMBERS_GROUP);
    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(membersGroup), embeddedGroupsItem);
  }

  @Test(expected = GroupNotAllowedToAutoRegistrationException.class)
  public void addSubgroupOfGroupWithStructureSyncToAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.addSubgroupOfGroupWithStructureSyncToAutoRegistration");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group group2 = new Group("Group2", "Group2 description");
    perun.getGroupsManagerBl().createGroup(session, group, group2);
    ExtSource extSource = new ExtSource(0, "testExSrc", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
    ExtSource es = perun.getExtSourcesManagerBl().createExtSource(session, extSource, null);
    perun.getExtSourcesManagerBl().addExtSource(session, vo, es);

    Attribute synchroAttr1 =
        new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPSQUERY_ATTRNAME));
    synchroAttr1.setValue("testVal");
    perun.getAttributesManager().setAttribute(session, group, synchroAttr1);

    Attribute synchroAttr2 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
    synchroAttr2.setValue("testVal");
    perun.getAttributesManager().setAttribute(session, group, synchroAttr2);

    Attribute synchroAttr3 = new Attribute(
        perun.getAttributesManager().getAttributeDefinition(session, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
    synchroAttr3.setValue(es.getName());
    perun.getAttributesManager().setAttribute(session, group, synchroAttr3);

    Attribute synchroAttr4 = new Attribute(perun.getAttributesManager()
        .getAttributeDefinition(session, GroupsManager.GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME));
    synchroAttr4.setValue(true);
    perun.getAttributesManager().setAttribute(session, group, synchroAttr4);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group2), embeddedGroupsItem);
  }

  @After
  public void cleanTest() throws Exception {

    //perun.getVosManagerBl().deleteVo(session, vo, true);

  }

  @Test
  public void copyItemsCorrectDependencyIds() throws Exception {
    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationFormItem firstItem = new ApplicationFormItem();
    firstItem.setShortname("dependency");
    firstItem = registrarManager.addFormItem(session, form, firstItem);

    ApplicationFormItem otherItem = new ApplicationFormItem();
    otherItem.setShortname("dep");
    otherItem.setHidden(ApplicationFormItem.Hidden.IF_PREFILLED);
    otherItem.setHiddenDependencyItemId(firstItem.getId());
    otherItem.setDisabledDependencyItemId(firstItem.getId());
    registrarManager.addFormItem(session, form, otherItem);

    Vo otherVo = perun.getVosManagerBl().createVo(session, new Vo(-1, "other", ""));
    registrarManager.copyFormFromVoToVo(session, vo, otherVo);

    var items = registrarManager.getFormItems(session, registrarManager.getFormForVo(otherVo));
    assertThat(items).hasSize(2);
    assertThat(items.get(1).getHiddenDependencyItemId()).isEqualTo(items.get(0).getId());
    assertThat(items.get(1).getDisabledDependencyItemId()).isEqualTo(items.get(0).getId());
  }

  @Test
  public void createAppMail() throws PerunException {
    System.out.println("createAppMail()");

    // get form for VO (if not exists, it's created)
    //Vo vo = perun.getVosManager().getVoByShortName(session, "meta");
    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText t = mail.getMessage(new Locale("cs"));
    t.setText("Český text mailu.");
    t.setSubject("Český předmět mailu");
    MailText t2 = mail.getMessage(new Locale("en"));
    t2.setSubject("Anglický předmět mailu");
    t2.setText("Anglický text mailu.");

    int id = mailManager.addMail(session, form, mail);
    mail = mailManager.getMailById(session, id);

    List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
    assertTrue("Mails are empty", (mails != null && !mails.isEmpty()));
    assertTrue("Our mail was not returned", mails.contains(mail));
    //System.out.println(mails);

  }

  @Test
  public void createAppMailWithBothMessageTemplates() throws PerunException {
    System.out.println("createAppMailWithBothMessageTemplate()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText t = mail.getMessage(new Locale("cs"));
    t.setSubject("Český předmět mailu");
    t.setText("Český text mailu.");
    MailText html = mail.getHtmlMessage(new Locale("cs"));
    html.setSubject("Český předmět mailu pro html");
    html.setText("<p>Český text mailu <b>v html</b>.</p>");
    MailText t2 = mail.getMessage(new Locale("en"));
    t2.setSubject("Anglický předmět mailu");
    t2.setText("Anglický text mailu.");
    MailText html2 = mail.getHtmlMessage(new Locale("en"));
    html2.setSubject("Anglický předmět mailu pro html");
    html2.setText("<p>Anglický text mailu <b>v html</b>.</p>");

    int id = mailManager.addMail(session, form, mail);
    mail = mailManager.getMailById(session, id);

    List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
    assertTrue("Mails are empty", (mails != null && !mails.isEmpty()));
    assertTrue("Our mail was not returned", mails.contains(mail));
    assertEquals("Plain text message (cs) doesn't contain correct text",
        mails.get(0).getMessage(new Locale("cs")).getText(), "Český text mailu.");
    assertEquals("Html message (cs) doesn't contain correct text",
        mails.get(0).getHtmlMessage(new Locale("cs")).getText(), "<p>Český text mailu <b>v html</b>.</p>");
    assertEquals("Plain text message (en) doesn't contain correct text",
        mails.get(0).getMessage(new Locale("en")).getText(), "Anglický text mailu.");
    assertEquals("Html message (en) doesn't contain correct text",
        mails.get(0).getHtmlMessage(new Locale("en")).getText(), "<p>Anglický text mailu <b>v html</b>.</p>");
  }

  @Test
  public void createAppMailWithHtmlMessageTemplate() throws PerunException {
    System.out.println("createAppMailWithBothMessageTemplate()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("cs"));
    html.setSubject("Český předmět mailu pro html");
    html.setText("<p>Český text mailu <b>v html</b>.</p>");
    MailText html2 = mail.getHtmlMessage(new Locale("en"));
    html2.setSubject("Anglický předmět mailu pro html");
    html2.setText("<p>Anglický text mailu <b>v html</b>.</p>");

    int id = mailManager.addMail(session, form, mail);
    mail = mailManager.getMailById(session, id);

    List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
    assertTrue("Mails are empty", (mails != null && !mails.isEmpty()));
    assertTrue("Our mail was not returned", mails.contains(mail));
    assertNull("Plain text message doesn't contain correct text", mails.get(0).getMessage(new Locale("cs")).getText());
    assertEquals("Html message doesn't contain correct text", mails.get(0).getHtmlMessage(new Locale("cs")).getText(),
        "<p>Český text mailu <b>v html</b>.</p>");
  }

  @Test(expected = InvalidHtmlInputException.class)
  public void createAppMailWithInvalidHtmlSubject() throws PerunException {
    System.out.println("createAppMailWithInvalidHtmlSubject()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("<script>alert(\"I AM UNSAFE!\");</script>");
    html.setText("English <b>html</b> text");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      mailManager.addMail(session, form, mail);
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test(expected = InvalidHtmlInputException.class)
  public void createAppMailWithInvalidHtmlText() throws PerunException {
    System.out.println("createAppMailWithInvalidHtmlText()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("English <b>html</b> subject");
    html.setText("<script>alert(\"I AM UNSAFE!\");</script>");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      mailManager.addMail(session, form, mail);
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test
  public void createAppMailWithSanitizedHtmlSubject() throws PerunException {
    System.out.println("createAppMailWithSanitizedHtmlSubject()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("<b>subject< script>");
    html.setText("English <b>html</b> text");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      String warning = registrarManager.checkHtmlInput(session, html.getSubject());
      assertNotEquals("", warning);

      mailManager.addMail(session, form, mail);
      List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
      assertEquals("Html input should be sanitized", mails.get(0).getHtmlMessage(new Locale("en")).getSubject(),
          "<b>subject&lt; script&gt;</b>");
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test(expected = InvalidHtmlInputException.class)
  public void createAppMailWithSanitizedHtmlSubjectError() throws PerunException {
    System.out.println("createAppMailWithSanitizedHtmlSubjectError()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("<a href>< script>");
    html.setText("English <b>html</b> text");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      registrarManager.checkHtmlInput(session, html.getSubject());
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test
  public void createAppMailWithSanitizedHtmlText() throws PerunException {
    System.out.println("createAppMailWithSanitizedHtmlText()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("English <b>html</b> subject");
    html.setText("<b>text< script>");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      String warning = registrarManager.checkHtmlInput(session, html.getText());
      assertNotEquals("", warning);

      mailManager.addMail(session, form, mail);
      List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
      assertEquals("Html input should be sanitized", mails.get(0).getHtmlMessage(new Locale("en")).getText(),
          "<b>text&lt; script&gt;</b>");
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test(expected = InvalidHtmlInputException.class)
  public void createAppMailWithSanitizedHtmlTextError() throws PerunException {
    System.out.println("createAppMailWithSanitizedHtmlTextError()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText html = mail.getHtmlMessage(new Locale("en"));
    html.setSubject("English <b>html</b> subject");
    html.setText("<a href>< script>");

    boolean originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    try {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
      registrarManager.checkHtmlInput(session, html.getText());
    } finally {
      BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
    }
  }

  @Test
  public void createApplicationFormItem() throws Exception {
    var form = registrarManager.getFormForVo(vo);

    var newItem = new ApplicationFormItem();
    newItem.setShortname("test");
    newItem.setUpdatable(true);

    newItem = registrarManager.addFormItem(session, form, newItem);

    assertThat(newItem.getHidden()).isEqualTo(ApplicationFormItem.Hidden.NEVER);
    assertThat(newItem.getDisabled()).isEqualTo(ApplicationFormItem.Disabled.NEVER);
    assertThat(newItem.isUpdatable()).isTrue();
  }

  @Test
  public void createApplicationFormItemValidHiddenId() throws Exception {
    var form = registrarManager.getFormForVo(vo);

    var firstItem = new ApplicationFormItem();
    firstItem.setShortname("test");
    firstItem = registrarManager.addFormItem(session, form, firstItem);

    var secondItem = new ApplicationFormItem();
    secondItem.setShortname("dependant");
    secondItem.setHidden(ApplicationFormItem.Hidden.IF_PREFILLED);
    secondItem.setHiddenDependencyItemId(firstItem.getId());

    secondItem = registrarManager.addFormItem(session, form, secondItem);

    assertThat(secondItem.getHiddenDependencyItemId()).isEqualTo(firstItem.getId());
  }

  @Test
  public void createVOformIntegrationTest() throws PerunException {
    System.out.println("createVOformIntegrationTest()");

    // get form for VO (if not exists, it's created)

    // put in standard options

    ApplicationFormItem i0 = new ApplicationFormItem();
    i0.setShortname("pokecI");
    i0.setType(ApplicationFormItem.Type.HTML_COMMENT);
    i0.getTexts(CS).setLabel("Vyplňte, prosím, přihlášku.");
    i0.getTexts(EN).setLabel("Fill in the initial application, please.");
    i0.setApplicationTypes(Collections.singletonList(INITIAL));
    ApplicationForm applicationForm = registrarManager.getFormForVo(vo);
    registrarManager.addFormItem(session, applicationForm, i0);

    ApplicationFormItem i0b = new ApplicationFormItem();
    i0b.setShortname("pokecE");
    i0b.setType(ApplicationFormItem.Type.HTML_COMMENT);
    i0b.getTexts(CS).setLabel("Zkontrolujte, prosím, před podáním žádosti o prodloužení účtu, svoje údaje.");
    i0b.getTexts(EN).setLabel("Please check you personal data before applying for account extension.");
    i0b.setApplicationTypes(Collections.singletonList(AppType.EXTENSION));
    registrarManager.addFormItem(session, applicationForm, i0b);

    ApplicationFormItem i1 = new ApplicationFormItem();
    i1.setShortname("titleBefore");
    i1.setPerunDestinationAttribute("urn:perun:user:attribute-def:core:titleBefore");
    i1.setRequired(false);
    i1.setType(ApplicationFormItem.Type.TEXTFIELD);
    i1.getTexts(CS).setLabel("Titul před jménem");
    i1.getTexts(CS).setHelp("Ing.,RNDr.,pplk., atd.");
    i1.getTexts(EN).setLabel("Title before name");
    registrarManager.addFormItem(session, applicationForm, i1);

    ApplicationFormItem i2 = new ApplicationFormItem();
    i2.setShortname("displayName");
    i2.setPerunDestinationAttribute("urn:perun:user:attribute-def:core:displayName");
    i2.setType(ApplicationFormItem.Type.TEXTFIELD);
    i2.setHidden(ApplicationFormItem.Hidden.ALWAYS);
    i2.setUpdatable(false);
    i2.setRequired(true);
    i2.setFederationAttribute("Shib-Person-displayName");
    registrarManager.addFormItem(session, applicationForm, i2);

    ApplicationFormItem i2a = new ApplicationFormItem();
    i2a.setShortname("firstName");
    i2a.setPerunDestinationAttribute("urn:perun:user:attribute-def:core:firstName");
    i2a.setType(ApplicationFormItem.Type.TEXTFIELD);
    i2a.setRequired(true);
    i2a.setFederationAttribute("Shib-Person-givenName");
    i2a.getTexts(CS).setLabel("Jméno");
    i2a.getTexts(EN).setLabel("First name");
    registrarManager.addFormItem(session, applicationForm, i2a);

    ApplicationFormItem i2b = new ApplicationFormItem();
    i2b.setShortname("lastName");
    i2b.setPerunDestinationAttribute("urn:perun:user:attribute-def:core:lastName");
    i2b.setType(ApplicationFormItem.Type.TEXTFIELD);
    i2b.setRequired(true);
    i2b.setFederationAttribute("Shib-Person-sureName");
    i2b.getTexts(CS).setLabel("Příjmení");
    i2b.getTexts(EN).setLabel("Last name");
    registrarManager.addFormItem(session, applicationForm, i2b);

    ApplicationFormItem i3 = new ApplicationFormItem();
    i3.setShortname("titleAfter");
    i3.setPerunDestinationAttribute("urn:perun:user:attribute-def:core:titleAfter");
    i3.setRequired(false);
    i3.setType(ApplicationFormItem.Type.TEXTFIELD);
    i3.getTexts(CS).setLabel("Titul za jménem");
    i3.getTexts(CS).setHelp("Ph.D., CSc., DrSc., atd.");
    i3.getTexts(EN).setLabel("Title after name");
    registrarManager.addFormItem(session, applicationForm, i3);

    ApplicationFormItem i4 = new ApplicationFormItem();
    i4.setShortname("preferredMail");
    i4.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:preferredMail");
    i4.setRequired(true);
    i4.setType(ApplicationFormItem.Type.VALIDATED_EMAIL);
    i4.getTexts(CS).setLabel("Email");
    i4.getTexts(CS).setHelp("Bude ověřen zasláním zprávy.");
    i4.getTexts(EN).setLabel("Email");
    i4.getTexts(EN).setHelp("Will be validated by sending an email message.");
    registrarManager.addFormItem(session, applicationForm, i4);

    ApplicationFormItem i5 = new ApplicationFormItem();
    i5.setShortname("organization");
    i5.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:organization");
    i5.setType(ApplicationFormItem.Type.TEXTFIELD);
    i5.setUpdatable(false);
    i5.setDisabled(ApplicationFormItem.Disabled.ALWAYS);
    i5.setRequired(true);
    i5.setFederationAttribute("Shib-Person-o");
    i5.getTexts(CS).setLabel("Organizace");
    i5.getTexts(EN).setLabel("Organisation");
    registrarManager.addFormItem(session, applicationForm, i5);

    ApplicationFormItem i5c = new ApplicationFormItem();
    i5c.setShortname("mail");
    i5c.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:mail");
    i5c.setType(ApplicationFormItem.Type.TEXTFIELD);
    i5c.setUpdatable(false);
    i5c.setHidden(ApplicationFormItem.Hidden.ALWAYS);
    i5c.setRequired(true);
    i5c.setFederationAttribute("Shib-InetOrgPerson-mail");
    registrarManager.addFormItem(session, applicationForm, i5c);

    ApplicationFormItem i6 = new ApplicationFormItem();
    i6.setShortname("vyuziti");
    i6.setPerunDestinationAttribute("urn:perun:member:attribute-def:opt:registrationNote");
    i6.setRequired(true);
    i6.setType(ApplicationFormItem.Type.TEXTAREA);
    i6.getTexts(CS).setLabel("Popis plánovaného využití MetaCentra");
    i6.getTexts(CS).setHelp(
        "Uveďte stručně, jakou činností se hodláte v MetaCentru zabývat. Uveďte také Vaše nadstandardní požadavky, " +
        "požadavky, které nejsou pokryty položkami formuláře, případně jiné skutečnosti, které považujete za " +
        "podstatné pro vyřízení přihlášky.");
    i6.getTexts(EN).setLabel("Description of planned activity");
    i6.getTexts(EN).setHelp(
        "Describe shortly activity which you plane to perform at MetaCentrum. Mention your nonstandard demands, " +
        "requests which are not covered in this form, eventually anything you consider important for this " +
        "registration too.");
    registrarManager.addFormItem(session, applicationForm, i6);

    ApplicationFormItem i7 = new ApplicationFormItem();
    i7.setShortname("phone");
    i7.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:phone");
    i7.setRequired(true);
    i7.setRegex("\\+*[0-9 ]*");
    i7.setType(ApplicationFormItem.Type.TEXTFIELD);
    i7.getTexts(CS).setLabel("Telefon");
    i7.getTexts(EN).setLabel("Phone");
    registrarManager.addFormItem(session, applicationForm, i7);

    ApplicationFormItem i8 = new ApplicationFormItem();
    i8.setShortname("workplace");
    i8.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:workplace");
    i8.setRequired(true);
    i8.setType(ApplicationFormItem.Type.TEXTAREA);
    i8.getTexts(CS).setLabel("Katedra/ústav/výzkumná skupina");
    i8.getTexts(EN).setLabel("Department/research group");
    registrarManager.addFormItem(session, applicationForm, i8);

    ApplicationFormItem i9 = new ApplicationFormItem();
    i9.setShortname("jazyk");
    i9.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:preferredLanguage");
    i9.setRequired(true);
    i9.setType(ApplicationFormItem.Type.SELECTIONBOX);
    i9.getTexts(CS).setLabel("Preferovaný jazyk");
    i9.getTexts(CS).setHelp("Zvolte jazyk, ve kterém chcete dostávát novinky a upozornění.");
    i9.getTexts(CS).setOptions("cs#česky|en#anglicky");
    i9.getTexts(EN).setLabel("Preffered language");
    i9.getTexts(EN).setHelp("Choose the language in which you want to receive news and notifications.");
    i9.getTexts(EN).setOptions("en#English|cs#Czech");
    registrarManager.addFormItem(session, applicationForm, i9);

    ApplicationFormItem i10 = new ApplicationFormItem();
    i10.setShortname("username");
    i10.setRequired(true);
    i10.setRegex("[a-z][a-z0-9_]+");
    i10.setType(ApplicationFormItem.Type.TEXTFIELD);
    i10.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:login-namespace:einfra");
    i10.getTexts(CS).setLabel("Zvolte si uživatelské jméno");
    i10.getTexts(CS).setHelp(
        "Uživatelské jméno musí začínat malým písmenem, a obsahovat pouze malá písmena, číslice a podtržení. " +
        "Doporučujeme délku nanejvýš 8 znaků.");
    i10.getTexts(EN).setLabel("Choose you user name");
    i10.getTexts(EN).setHelp(
        "User name must begin with a small letter, and can contain only small letters, digits and underscores. We " +
        "recommend length max 8 characters.");
    i10.setApplicationTypes(Collections.singletonList(INITIAL));
    registrarManager.addFormItem(session, applicationForm, i10);

    ApplicationFormItem i11 = new ApplicationFormItem();
    i11.setShortname("heslo");
    i11.setRequired(true);
    i11.setRegex("\\p{Print}{8,20}");
    i11.setType(ApplicationFormItem.Type.PASSWORD);
    i11.getTexts(CS).setLabel("Heslo");
    i11.getTexts(CS)
        .setHelp("Heslo musí být 8 až 20 znaků dlouhé, a obsahovat aspoň 3 písmena a 1 znak jiný než písmeno.");
    i11.getTexts(EN).setLabel("Password");
    i11.getTexts(EN).setHelp("Password must be from 8 up to 20 characters long and contain printable characters only.");
    i11.setApplicationTypes(Collections.singletonList(INITIAL));
    registrarManager.addFormItem(session, applicationForm, i11);

    ApplicationFormItem i12 = new ApplicationFormItem();
    i12.setShortname("pokec");
    i12.setType(ApplicationFormItem.Type.HTML_COMMENT);
    i12.getTexts(CS).setLabel(
        "Stiskem tlačítka 'Podat žádost o členství ve VO MetaCentrum' souhlaste s pravidly využití VO MetaCentrum.");
    i12.getTexts(EN).setLabel("By pressing the button you agree with MetaCentrum rules.");
    i12.setApplicationTypes(Collections.singletonList(INITIAL));
    registrarManager.addFormItem(session, applicationForm, i12);

    ApplicationFormItem i13 = new ApplicationFormItem();
    i13.setShortname("souhlasI");
    i13.setType(ApplicationFormItem.Type.SUBMIT_BUTTON);
    i13.getTexts(CS).setLabel("Podat žádost o členství ve VO MetaCentrum");
    i13.getTexts(EN).setLabel("Apply for membership in the MetaCentrum VO");
    i13.setApplicationTypes(Collections.singletonList(INITIAL));
    registrarManager.addFormItem(session, applicationForm, i13);

    ApplicationFormItem i13b = new ApplicationFormItem();
    i13b.setShortname("souhlasE");
    i13b.setType(ApplicationFormItem.Type.SUBMIT_BUTTON);
    i13b.getTexts(CS).setLabel("Podat žádost o prodloužení účtu");
    i13b.getTexts(EN).setLabel("Apply for account extension");
    i13b.setApplicationTypes(Collections.singletonList(AppType.EXTENSION));
    registrarManager.addFormItem(session, applicationForm, i13b);

    // update form not to auto aprove
    applicationForm.setAutomaticApproval(false);
    registrarManager.updateForm(session, applicationForm);

    // test returned app form
    assertEquals("Application form not same as expected", applicationForm, registrarManager.getFormForVo(vo));

    // test form items
    List<ApplicationFormItem> items = registrarManager.getFormItems(session, applicationForm);

    assertTrue("Item i0 was not returned from form", items.contains(i0));
    assertTrue("Item i0b was not returned from form", items.contains(i0b));
    assertTrue("Item i1 was not returned from form", items.contains(i1));
    assertTrue("Item i2 was not returned from form", items.contains(i2));
    assertTrue("Item i2a was not returned from form", items.contains(i2a));
    assertTrue("Item i2b was not returned from form", items.contains(i2b));
    assertTrue("Item i3 was not returned from form", items.contains(i3));
    assertTrue("Item i4 was not returned from form", items.contains(i4));
    assertTrue("Item i5 was not returned from form", items.contains(i5));
    assertTrue("Item i5c was not returned from form", items.contains(i5c));
    assertTrue("Item i6 was not returned from form", items.contains(i6));
    assertTrue("Item i7 was not returned from form", items.contains(i7));
    assertTrue("Item i8 was not returned from form", items.contains(i8));
    assertTrue("Item i9 was not returned from form", items.contains(i9));
    assertTrue("Item i10 was not returned from form", items.contains(i10));
    assertTrue("Item i11 was not returned from form", items.contains(i11));
    assertTrue("Item i12 was not returned from form", items.contains(i12));
    assertTrue("Item i13 was not returned from form", items.contains(i13));
    assertTrue("Item i13b was not returned from form", items.contains(i13b));

    Random random = new Random();
    PerunSession applicant = perun.getPerunSession(
        new PerunPrincipal("rumcajs" + random.nextInt(100000) + "@raholec.cz", "http://www.raholec.cz/idp/",
            ExtSourcesManager.EXTSOURCE_IDP), new PerunClient());

  }

  @Test(expected = RelationExistsException.class)
  public void deleteAttributeRelatedToGroupFormAsSrcAttr() throws Exception {
    System.out.println("RegistrarManager.deleteAttributeRelatedToFormAsDestAttr");

    Vo vo = perun.getVosManagerBl().createVo(session, new Vo(0, "voTest", "voTest"));
    Group group = perun.getGroupsManagerBl().createGroup(session, vo, new Group("testGroup", "testGroup description"));
    registrarManager.createApplicationFormInGroup(session, group);

    Group group2 =
        perun.getGroupsManagerBl().createGroup(session, vo, new Group("testGroup2", "testGroup2 description"));
    registrarManager.createApplicationFormInGroup(session, group2);

    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setDescription("attributesManagerTestAttrDef");
    attrDef.setFriendlyName("attrDef");
    attrDef.setNamespace("urn:perun:member:attribute-def:opt");
    attrDef.setType(String.class.getName());
    perun.getAttributesManager().createAttribute(session, attrDef);

    ApplicationFormItem item = new ApplicationFormItem();
    item.setShortname("displayName");
    item.setPerunSourceAttribute("urn:perun:member:attribute-def:opt:attrDef");
    item.setType(ApplicationFormItem.Type.TEXTFIELD);
    item.setHidden(ApplicationFormItem.Hidden.ALWAYS);
    item.setUpdatable(false);
    item.setRequired(true);
    ApplicationForm groupForm = registrarManager.getFormForGroup(group);
    registrarManager.addFormItem(session, groupForm, item);

    ApplicationFormItem item2 = new ApplicationFormItem();
    item2.setShortname("displayName2");
    item2.setPerunDestinationAttribute("urn:perun:member:attribute-def:opt:attrDef");
    item2.setType(ApplicationFormItem.Type.TEXTFIELD);
    item2.setHidden(ApplicationFormItem.Hidden.ALWAYS);
    item2.setUpdatable(false);
    item2.setRequired(true);
    ApplicationForm groupForm2 = registrarManager.getFormForGroup(group2);
    registrarManager.addFormItem(session, groupForm2, item2);

    perun.getAttributesManager().deleteAttribute(session, attrDef);
  }

  @Test(expected = RelationExistsException.class)
  public void deleteAttributeRelatedToVoFormAsDestAttr() throws Exception {
    System.out.println("RegistrarManager.deleteAttributeRelatedToFormAsDestAttr");


    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setDescription("attributesManagerTestAttrDef");
    attrDef.setFriendlyName("attrDef");
    attrDef.setNamespace("urn:perun:member:attribute-def:opt");
    attrDef.setType(String.class.getName());
    perun.getAttributesManager().createAttribute(session, attrDef);

    ApplicationFormItem item = new ApplicationFormItem();
    item.setShortname("displayName");
    item.setPerunDestinationAttribute("urn:perun:member:attribute-def:opt:attrDef");
    item.setType(ApplicationFormItem.Type.TEXTFIELD);
    item.setHidden(ApplicationFormItem.Hidden.ALWAYS);
    item.setUpdatable(false);
    item.setRequired(true);
    Vo vo = perun.getVosManagerBl().createVo(session, new Vo(0, "voTest", "voTest"));
    ApplicationForm form = registrarManager.getFormForVo(vo);
    registrarManager.addFormItem(session, form, item);

    perun.getAttributesManager().deleteAttribute(session, attrDef);
  }

  @Test
  public void deleteGroupsFromAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.deleteGroupsFromAutoRegistration");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);
    assertEquals(List.of(group), registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));

    registrarManager.deleteGroupsFromAutoRegistration(session, List.of(group), embeddedGroupsItem);
    assertEquals(Collections.emptyList(),
        registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));
  }

  @Test(expected = FormItemNotExistsException.class)
  public void deleteGroupsFromAutoRegistrationFormItemNotExists() throws Exception {
    System.out.println("RegistrarManager.deleteGroupsFromAutoRegistrationFormItemNotExists");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    // create embedded groups form item but don't store it !!!
    ApplicationForm form = registrarManager.getFormForVo(vo);
    ApplicationFormItem embeddedGroupsItem = new ApplicationFormItem();
    embeddedGroupsItem.setType(ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION);
    embeddedGroupsItem.setShortname("embeddedGroups");

    registrarManager.deleteGroupsFromAutoRegistration(session, List.of(group), embeddedGroupsItem);
  }

  @Test
  public void deleteGroupsFromAutoRegistrationGroupForm() throws Exception {
    System.out.println("RegistrarManager.deleteGroupsFromAutoRegistrationGroupForm");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group group2 = new Group("Group2", "Group2 description");
    perun.getGroupsManagerBl().createGroup(session, group, group2);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForGroupForm(group);

    registrarManager.addGroupsToAutoRegistration(session, List.of(group2), group, embeddedGroupsItem);
    assertEquals(List.of(group2), registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));

    registrarManager.deleteGroupsFromAutoRegistration(session, List.of(group2), group, embeddedGroupsItem);
    assertEquals(Collections.emptyList(),
        registrarManager.getGroupsForAutoRegistration(session, vo, embeddedGroupsItem));
  }

  @Test
  public void doNotAllowUpdateDestinationAttribute() throws Exception {
    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationFormItem savedItem = new ApplicationFormItem();
    savedItem.setShortname("item");
    savedItem.setPerunDestinationAttribute("saved");
    registrarManager.addFormItem(session, form, savedItem);

    List<ApplicationFormItem> items = registrarManager.getFormItems(session, registrarManager.getFormForVo(vo));
    assertThat(items).hasSize(1);

    User user = setUpUser("User", "Test");
    Application application = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, application, new ArrayList<>());

    List<Application> applications = registrarManager.getApplicationsForVo(session, vo, null, true);
    assertThat(items).hasSize(1);

    savedItem.setPerunDestinationAttribute("updated");
    assertThrows(OpenApplicationExistsException.class,
        () -> registrarManager.updateFormItems(session, form, List.of(savedItem)));
  }

  @Test
  public void getAllGroupsForAutoRegistration() throws Exception {
    System.out.println("RegistrarManager.getAllGroupsForAutoRegistration");

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group group2 = new Group("Group2", "Group 2 description");
    perun.getGroupsManagerBl().createGroup(session, vo, group2);

    Vo vo2 = new Vo(0, "registrarTestVO2", "regTestVO2");
    vo2 = perun.getVosManagerBl().createVo(session, vo2);
    Group group3 = new Group("Group3", "Group 3 description");
    perun.getGroupsManagerBl().createGroup(session, vo2, group3);

    registrarManager.addGroupsToAutoRegistration(session, List.of(group, group2, group3));

    assertEquals(2, registrarManager.getGroupsForAutoRegistration(session, vo).size());
    assertEquals(1, registrarManager.getGroupsForAutoRegistration(session, vo2).size());
    assertEquals(3, registrarManager.getAllGroupsForAutoRegistration(session).size());
  }

  @Test
  public void getApplicationsPageApplicationFormSearch() throws Exception {
    System.out.println("getApplicationsPageApplicationFormSearch");
    ApplicationForm form = registrarManager.getFormForVo(vo);


    // set up application


    ApplicationFormItem testItem = new ApplicationFormItem();
    testItem.setType(ApplicationFormItem.Type.TEXTFIELD);
    testItem.setShortname("testItem");

    testItem = registrarManager.addFormItem(session, form, testItem);
    registrarManager.updateFormItems(session, form, Collections.singletonList(testItem));

    ApplicationFormItem testItem2 = new ApplicationFormItem();
    testItem2.setType(ApplicationFormItem.Type.TEXTFIELD);
    testItem2.setShortname("testItem2");

    testItem2 = registrarManager.addFormItem(session, form, testItem2);
    registrarManager.updateFormItems(session, form, Collections.singletonList(testItem2));
    ApplicationFormItemData testData2 = new ApplicationFormItemData(testItem2, "test2", "banana", "0");

    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    ApplicationFormItemData testData = new ApplicationFormItemData(testItem, "test", "testval", "0");
    appItemsData.add(testData);
    appItemsData.add(testData2);

    Group group1 = setUpGroup("Group1", "Cool folks");
    User user1 = setUpUser("Joe", "Doe");
    Application application1 = setUpApplicationGroupWithData(user1, group1, appItemsData);
    Group group2 = setUpGroup("Group2", "Cooler folks");
    User user2 = setUpUser("Barney", "Stinson");
    Application application2 = setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE, "testval",
            List.of(Application.AppState.VERIFIED, Application.AppState.APPROVED), true);
    query.setGetDetails(true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<User> returnedIds = result.getData().stream().map(Application::getUser).toList();

    assertThat(returnedIds).containsOnly(user1);
    assertThat(result.getData().get(0).getFormData().size()).isEqualTo(2);
  }

  @Test
  public void getApplicationsPageBasedOnSearchString() throws Exception {
    System.out.println("getApplicationsPageBasedOnSearchString");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group1);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.ID, "barn",
            List.of(Application.AppState.APPROVED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<User> returnedUsers = result.getData().stream().map(Application::getUser).toList();

    assertThat(returnedUsers).containsOnly(user2);
  }

  @Test
  public void getApplicationsPageFindByApplicationId() throws Exception {
    System.out.println("getApplicationsPageFindByApplicationId");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application application1 = setUpApplicationGroup(user1, group1);
    Application application2 = setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE,
            Integer.toString(application1.getId()),
            List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Integer> returnedIds = result.getData().stream().map(Application::getId).toList();

    assertThat(returnedIds).containsExactly(application1.getId());

  }

  @Test
  public void getApplicationsPageFindByGroupDescription() throws Exception {
    System.out.println("getApplicationsPageFindByGroupDescription");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.ID, "cooler",
            List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Group> returnedGroups =
        result.getData().stream().map(Application::getGroup).filter(Predicate.not(Objects::isNull)).toList();

    assertThat(returnedGroups).containsOnly(group2);
  }

  @Test
  public void getApplicationsPageFindByGroupId() throws Exception {
    System.out.println("getApplicationsPageFindByGroupId");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query = new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.ID,
        Integer.toString(group2.getId()), List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Group> returnedGroups =
        result.getData().stream().map(Application::getGroup).filter(Predicate.not(Objects::isNull)).toList();

    List<Integer> returnedIds = returnedGroups.stream().map(Group::getId).toList();

    assertThat(returnedIds).containsOnly(group2.getId());

  }

  @Test
  public void getApplicationsPageFindByGroupName() throws Exception {
    System.out.println("getApplicationsPageFindByGroupName");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.ID, "Group2",
            List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Group> returnedGroups =
        result.getData().stream().map(Application::getGroup).filter(Predicate.not(Objects::isNull)).toList();

    assertThat(returnedGroups).containsOnly(group2);
  }

  @Test
  public void getApplicationsPageFindByGroupUuid() throws Exception {
    System.out.println("getApplicationsPageFindByGroupUuid");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query = new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.ID,
        group2.getUuid().toString(), List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Group> returnedGroups =
        result.getData().stream().map(Application::getGroup).filter(Predicate.not(Objects::isNull)).toList();

    List<UUID> returnedIds = returnedGroups.stream().map(Group::getUuid).toList();

    assertThat(returnedIds).containsOnly(group2.getUuid());

  }

  @Test
  public void getApplicationsPageForGroup() throws Exception {
    System.out.println("getApplicationsPageForGroup");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application application1 = setUpApplicationGroup(user1, group1);
    Application application2 = setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE,
            List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), group1.getId());

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Group> returnedGroups = result.getData().stream().map(Application::getGroup).toList();

    List<Integer> returnedIds = returnedGroups.stream().map(Group::getId).toList();

    assertThat(returnedIds).containsExactly(group1.getId());
  }

  @Test
  public void getApplicationsPageForUserIsMember() throws Exception {
    System.out.println("getApplicationsPageForUserIsMember");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application application1 = setUpApplicationGroup(user1, group1);
    Application application2 = setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE,
            List.of(Application.AppState.VERIFIED), user1.getId(), group1.getId());

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    assertEquals(1, result.getData().size());
  }

  @Test
  public void getApplicationsPageForUserIsNotMember() throws Exception {
    System.out.println("getApplicationsPageForUserIsNotMember");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    Group group2 = setUpGroup("Group2", "Cooler folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application application1 = setUpApplicationGroup(user1, group1);
    Application application2 = setUpApplicationGroup(user2, group2);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE,
            List.of(Application.AppState.VERIFIED), user1.getId(), group2.getId());

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    assertEquals(0, result.getData().size());
  }

  @Test
  public void getApplicationsPageIdSortWorks() throws Exception {
    System.out.println("getApplicationsPageIdSortWorks");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    setUpApplicationGroup(user1, group1);
    setUpApplicationGroup(user2, group1);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE,
            List.of(Application.AppState.APPROVED, Application.AppState.VERIFIED), true);


    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    List<Application.AppState> returnedAppStates = result.getData().stream().map(Application::getState).toList();

    assertThat(returnedAppStates).containsExactly(Application.AppState.VERIFIED, Application.AppState.VERIFIED,
        Application.AppState.APPROVED, Application.AppState.APPROVED);
  }

  @Test
  public void getApplicationsPageMultipleFormItems() throws Exception {
    System.out.println("getApplicationsPageMultipleFormItems");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationFormItem testItem = new ApplicationFormItem();
    testItem.setType(ApplicationFormItem.Type.TEXTFIELD);
    testItem.setShortname("testItem");

    testItem = registrarManager.addFormItem(session, form, testItem);
    registrarManager.updateFormItems(session, form, Collections.singletonList(testItem));

    ApplicationFormItem testItem2 = new ApplicationFormItem();
    testItem2.setType(ApplicationFormItem.Type.TEXTFIELD);
    testItem2.setShortname("testItem2");

    testItem2 = registrarManager.addFormItem(session, form, testItem2);
    registrarManager.updateFormItems(session, form, Collections.singletonList(testItem2));
    ApplicationFormItemData testData2 = new ApplicationFormItemData(testItem2, "test2", "banana", "0");

    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    ApplicationFormItemData testData = new ApplicationFormItemData(testItem, "test", "testval", "0");
    appItemsData.add(testData);
    appItemsData.add(testData2);

    Group group1 = setUpGroup("Group1", "Cool folks");
    User user1 = setUpUser("Joe", "Doe");
    Application application1 = setUpApplicationGroupWithData(user1, group1, appItemsData);

    ApplicationsPageQuery query =
        new ApplicationsPageQuery(4, 0, SortingOrder.DESCENDING, ApplicationsOrderColumn.STATE, "",
            List.of(Application.AppState.APPROVED), true);
    query.setGetDetails(true);

    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    assertEquals(1, result.getData().size());
  }

  @Test
  public void getApplicationsPageOffsetWorks() throws Exception {
    System.out.println("getApplicationsPageOffsetWorks");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application voApplication1 = setUpApplicationGroup(user1, group1);
    Application voApplication2 = setUpApplicationGroup(user2, group1);

    ApplicationsPageQuery query = new ApplicationsPageQuery(1, 1, SortingOrder.ASCENDING, ApplicationsOrderColumn.ID,
        List.of(Application.AppState.APPROVED), true);


    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    assertThat(result.getData()).hasSize(1);
    assertThat(result.getData().get(0).getUser().getId()).isEqualTo(voApplication2.getUser().getId());
  }

  @Test
  public void getApplicationsPagePageSizeWorks() throws Exception {
    System.out.println("getApplicationsPagePageSizeWorks");

    // create groups
    Group group1 = setUpGroup("Group1", "Cool folks");
    // create users
    User user1 = setUpUser("Joe", "Doe");
    User user2 = setUpUser("Barney", "Stinson");

    Application voApplication1 = setUpApplicationGroup(user1, group1);
    Application voApplication2 = setUpApplicationGroup(user2, group1);

    ApplicationsPageQuery query = new ApplicationsPageQuery(1, 0, SortingOrder.ASCENDING, ApplicationsOrderColumn.ID,
        List.of(Application.AppState.APPROVED), true);


    Paginated<RichApplication> result = registrarManager.getApplicationsPage(session, vo, query);

    assertThat(result.getData()).hasSize(1);
    assertThat(result.getData().get(0).getId()).isEqualTo(voApplication1.getId());

  }

  @Test
  public void invitationFormExistsForGroup() throws Exception {
    Group groupWithInvitation =
        perun.getGroupsManagerBl().createGroup(session, vo, new Group("group1", "group with form"));


    registrarManager.createApplicationFormInGroup(session, groupWithInvitation);
    ApplicationForm form = registrarManager.getFormForGroup(groupWithInvitation);
    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));

    mailManager.addMail(session, form, mail);

    assertTrue(mailManager.invitationFormExists(session, vo, groupWithInvitation));
    Group groupWithoutInvitation =
        perun.getGroupsManagerBl().createGroup(session, vo, new Group("group2", "group without form"));
    assertFalse(mailManager.invitationFormExists(session, vo, groupWithoutInvitation));
  }

  @Test
  public void invitationFormExistsForVo() throws Exception {
    Vo voWithoutInvitation = perun.getVosManager().createVo(session, new Vo(1234, "test", "test"));

    ApplicationForm form = registrarManager.getFormForVo(vo);
    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));
    mailManager.addMail(session, form, mail);

    assertTrue(mailManager.invitationFormExists(session, vo, null));
    assertFalse(mailManager.invitationFormExists(session, voWithoutInvitation, null));
  }

  @Test
  public void isInvitationEnabledForGroup() throws Exception {
    Group groupWithInvitation =
        perun.getGroupsManagerBl().createGroup(session, vo, new Group("group1", "group with form"));
    Group groupWithoutInvitation =
        perun.getGroupsManagerBl().createGroup(session, vo, new Group("group2", "group without form"));

    registrarManager.createApplicationFormInGroup(session, groupWithInvitation);
    ApplicationForm form = registrarManager.getFormForGroup(groupWithInvitation);
    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));

    mailManager.addMail(session, form, mail);

    registrarManager.createApplicationFormInGroup(session, groupWithoutInvitation);
    ApplicationForm form2 = registrarManager.getFormForGroup(groupWithoutInvitation);
    ApplicationMail mail2 = new ApplicationMail(0, INITIAL, form2.getId(), MailType.USER_INVITE, true);
    mail2.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));

    mailManager.addMail(session, form2, mail2);

    ApplicationFormItem submitButton = new ApplicationFormItem();
    submitButton.setType(ApplicationFormItem.Type.SUBMIT_BUTTON);
    submitButton.setShortname("submitButton");
    registrarManager.addFormItem(session, form, submitButton);

    assertTrue(mailManager.isInvitationEnabled(session, vo, groupWithInvitation));
    assertFalse(mailManager.isInvitationEnabled(session, vo, groupWithoutInvitation));
  }

  @Test
  public void isInvitationEnabledForVo() throws Exception {
    Vo voWithoutInvitation = perun.getVosManager().createVo(session, new Vo(1234, "test", "test"));

    ApplicationForm form = registrarManager.getFormForVo(vo);
    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));

    mailManager.addMail(session, form, mail);

    ApplicationForm form2 = registrarManager.getFormForVo(voWithoutInvitation);
    ApplicationMail mail2 = new ApplicationMail(0, INITIAL, form2.getId(), MailType.USER_INVITE, true);
    mail2.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","test"));

    mailManager.addMail(session, form2, mail2);

    ApplicationFormItem submitButton = new ApplicationFormItem();
    submitButton.setType(ApplicationFormItem.Type.SUBMIT_BUTTON);
    submitButton.setShortname("submitButton");
    registrarManager.addFormItem(session, form, submitButton);

    assertTrue(mailManager.isInvitationEnabled(session, vo, null));
    assertFalse(mailManager.isInvitationEnabled(session, voWithoutInvitation, null));
  }

  @Test
  public void isPreApprovedInvitationEnabled() throws Exception {
    System.out.println("isPreApprovedInvitationEnabled");

    Group groupWithInvitation =
            perun.getGroupsManagerBl().createGroup(session, vo, new Group("group1", "group with form"));
    Group groupWithoutInvitation =
            perun.getGroupsManagerBl().createGroup(session, vo, new Group("group2", "group without form"));

    registrarManager.createApplicationFormInGroup(session, groupWithInvitation);
    ApplicationForm form = registrarManager.getFormForGroup(groupWithInvitation);

    registrarManager.createApplicationFormInGroup(session, groupWithoutInvitation);
    ApplicationForm form2 = registrarManager.getFormForGroup(groupWithoutInvitation);

    ApplicationFormItem submitButton = new ApplicationFormItem();
    submitButton.setType(ApplicationFormItem.Type.SUBMIT_BUTTON);
    submitButton.setShortname("submitButton");

    registrarManager.addFormItem(session, form, submitButton);
    registrarManager.addFormItem(session, form2, submitButton);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","{preapprovedInvitationLink} {expirationDate}"));

    mailManager.addMail(session, form, mail);

    assertTrue(mailManager.isPreApprovedInvitationEnabled(session, vo, groupWithInvitation));
    assertFalse(mailManager.isPreApprovedInvitationEnabled(session, vo, groupWithoutInvitation));
  }

  @Test
  public void addMailWithRequiredTagsInPlaintext() throws Exception {
    System.out.println("addMailWithRequiredTagsInPlaintext");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","Submit your application here {preapprovedInvitationLink} until {expirationDate}"));

    mailManager.addMail(session, form, mail);
  }

  @Test
  public void addMailWithoutRequiredTagsInPlaintext() throws Exception {
    System.out.println("addMailWithoutRequiredTagsInPlaintext");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","Message text without tags"));

    Exception e = assertThrows(ApplicationMailTextMissingException.class,
        () -> mailManager.addMail(session, form, mail));
    assertTrue(e.getMessage().contains(
        "The mail message text for this notification type must contain following tags: '{preapprovedInvitationLink}', '{expirationDate}'."));
  }

  @Test
  public void addMailWithRequiredTagsInHtml() throws Exception {
    System.out.println("addMailWithRequiredTagsInHtml");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getHtmlMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","<div>Submit your application here {preapprovedInvitationLink} until {expirationDate}</div>"));

    mailManager.addMail(session, form, mail);
  }

  @Test
  public void addMailWithoutRequiredTagsInHtml() throws Exception {
    System.out.println("addMailWithoutRequiredTagsInHtml");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","<div>Message text without tags</div>"));

    Exception e = assertThrows(ApplicationMailTextMissingException.class,
        () -> mailManager.addMail(session, form, mail));
    assertTrue(e.getMessage().contains(
        "The mail message text for this notification type must contain following tags: '{preapprovedInvitationLink}', '{expirationDate}'."));
  }

  @Test
  public void updateMailWithRequiredTagsInPlainText() throws Exception {
    System.out.println("updateMailWithRequiredTagsInPlainText()");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","Submit your application here {preapprovedInvitationLink} until {expirationDate}"));

    int mailId = mailManager.addMail(session, form, mail);
    ApplicationMail updatedMail = mailManager.getMailById(session, mailId);

    MailText message = mail.getMessage(Locale.ENGLISH);
    message.setText("Updated version: Submit your application here {preapprovedInvitationLink} until {expirationDate}");

    mailManager.updateMailById(session, mail);
  }

  @Test
  public void updateMailWithoutRequiredTagsInPlainText() throws Exception {
    System.out.println("updateMailWithoutRequiredTagsInPlainText()");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","Submit your application here {preapprovedInvitationLink} until {expirationDate}"));

    int mailId = mailManager.addMail(session, form, mail);
    ApplicationMail updatedMail = mailManager.getMailById(session, mailId);

    MailText message = mail.getMessage(Locale.ENGLISH);
    message.setText("Updated version without tags");

    Exception e = assertThrows(ApplicationMailTextMissingException.class,
        () -> mailManager.updateMailById(session, mail));
    assertTrue(e.getMessage().contains(
        "The mail message text for this notification type must contain following tags: '{preapprovedInvitationLink}', '{expirationDate}'."));
  }

  @Test
  public void updateMailWithRequiredTagsInHtml() throws Exception {
    System.out.println("updateMailWithRequiredTagsInHtml()");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getHtmlMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","<div>Submit your application here {preapprovedInvitationLink} until {expirationDate}</div>"));

    int mailId = mailManager.addMail(session, form, mail);
    ApplicationMail updatedMail = mailManager.getMailById(session, mailId);

    MailText htmlMessage = mail.getHtmlMessage(Locale.ENGLISH);
    htmlMessage.setText("<div>Updated version: Submit your application here {preapprovedInvitationLink} until {expirationDate}</div>");

    mailManager.updateMailById(session, mail);
  }

  @Test
  public void updateMailWithoutRequiredTagsInHtml() throws Exception {
    System.out.println("updateMailWithoutRequiredTagsInHtml()");

    Group group = setUpGroup("group", "test group");

    ApplicationForm form = registrarManager.getFormForGroup(group);

    ApplicationMail mail = new ApplicationMail(0, INITIAL, form.getId(), MailType.USER_PRE_APPROVED_INVITE, true);
    mail.getHtmlMessage().put(Locale.ENGLISH, new MailText(Locale.ENGLISH, "test","<div>Submit your application here {preapprovedInvitationLink} until {expirationDate}</div>"));

    int mailId = mailManager.addMail(session, form, mail);
    ApplicationMail updatedMail = mailManager.getMailById(session, mailId);

    MailText htmlMessage = mail.getHtmlMessage(Locale.ENGLISH);
    htmlMessage.setText("<div>Updated message text without tags</div>");

    Exception e = assertThrows(ApplicationMailTextMissingException.class,
        () -> mailManager.updateMailById(session, mail));
    assertTrue(e.getMessage().contains(
        "The mail message text for this notification type must contain following tags: '{preapprovedInvitationLink}', '{expirationDate}'."));
  }

  @Test
  public void moveGroupInvolvedInVoAutoRegistrationProcess() throws Exception {
    System.out.println("RegistrarManager.moveSubgroupInvolvedInGroupAutoRegistrationProcess");

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();

    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);

    assertThatExceptionOfType(GroupMoveNotAllowedException.class).isThrownBy(
        () -> groupsManagerBl.moveGroup(session, null, group));
  }

  @Test
  public void moveGroupWithSubgroupInvolvedInVoAutoRegistrationProcess() throws Exception {
    System.out.println("RegistrarManager.moveSubgroupInvolvedInGroupAutoRegistrationProcess");


    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group subgroup1 = new Group("Subgroup1", "Subgroup1 description");
    perun.getGroupsManagerBl().createGroup(session, group, subgroup1);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    registrarManager.addGroupsToAutoRegistration(session, List.of(subgroup1), embeddedGroupsItem);

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();
    assertThatExceptionOfType(GroupMoveNotAllowedException.class).isThrownBy(
        () -> groupsManagerBl.moveGroup(session, null, group));
  }

  @Test
  public void moveSubgroupInvolvedInGroupAutoRegistrationProcess() throws Exception {
    System.out.println("RegistrarManager.moveSubgroupInvolvedInGroupAutoRegistrationProcess");


    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group subgroup1 = new Group("Subgroup1", "Subgroup1 description");
    perun.getGroupsManagerBl().createGroup(session, group, subgroup1);

    ApplicationFormItem groupEmbeddedGroupsItem = setUpEmbeddedGroupApplicationItemForGroupForm(group);

    registrarManager.addGroupsToAutoRegistration(session, List.of(subgroup1), group, groupEmbeddedGroupsItem);

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();
    assertThatExceptionOfType(GroupMoveNotAllowedException.class).isThrownBy(
        () -> groupsManagerBl.moveGroup(session, null, subgroup1));
  }

  @Test
  public void moveSubgroupWithSubgroupInvolvedInGroupAutoRegistrationProcess() throws Exception {
    System.out.println("RegistrarManager.moveSubgroupInvolvedInGroupAutoRegistrationProcess");


    Group group = new Group("Group", "Group description");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    Group subgroup1 = new Group("Subgroup1", "Subgroup1 description");
    perun.getGroupsManagerBl().createGroup(session, group, subgroup1);
    Group subgroup2 = new Group("Subgroup2", "Subgroup2 description");
    perun.getGroupsManagerBl().createGroup(session, subgroup1, subgroup2);

    ApplicationFormItem groupEmbeddedGroupsItem = setUpEmbeddedGroupApplicationItemForGroupForm(group);

    registrarManager.addGroupsToAutoRegistration(session, List.of(subgroup2), group, groupEmbeddedGroupsItem);

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();
    assertThatExceptionOfType(GroupMoveNotAllowedException.class).isThrownBy(
        () -> groupsManagerBl.moveGroup(session, null, subgroup1));
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

  @Test
  public void saveDependencyOnUnsavedItemWithTempId() throws Exception {
    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationFormItem savedItem = new ApplicationFormItem();
    savedItem.setShortname("saved");
    savedItem = registrarManager.addFormItem(session, form, savedItem);
    registrarManager.updateFormItems(session, form, new ArrayList<>(Arrays.asList(savedItem)));

    ApplicationFormItem firstUnsavedItem = new ApplicationFormItem();
    firstUnsavedItem.setShortname("unsaved1");
    firstUnsavedItem.setId(-1);

    ApplicationFormItem secondUnsavedItem = new ApplicationFormItem();
    secondUnsavedItem.setShortname("unsaved2");
    secondUnsavedItem.setId(-2);

    savedItem.setHidden(ApplicationFormItem.Hidden.IF_PREFILLED);
    savedItem.setHiddenDependencyItemId(-1);
    secondUnsavedItem.setHidden(ApplicationFormItem.Hidden.IF_PREFILLED);
    secondUnsavedItem.setHiddenDependencyItemId(-1);

    savedItem.setDisabled(ApplicationFormItem.Disabled.IF_PREFILLED);
    savedItem.setDisabledDependencyItemId(-2);
    firstUnsavedItem.setDisabled(ApplicationFormItem.Disabled.IF_PREFILLED);
    firstUnsavedItem.setDisabledDependencyItemId(-2);

    registrarManager.updateFormItems(session, form,
        new ArrayList<>(Arrays.asList(savedItem, firstUnsavedItem, secondUnsavedItem)));
    var items = registrarManager.getFormItems(session, registrarManager.getFormForVo(vo));
    assertThat(items).hasSize(3);

    // ids were changed from negative to positive
    assertThat(items.get(1).getId()).isGreaterThan(0);
    assertThat(items.get(2).getId()).isGreaterThan(0);

    // dependencies are valid
    assertThat(items.get(0).getHiddenDependencyItemId()).isEqualTo(items.get(1).getId());
    assertThat(items.get(2).getHiddenDependencyItemId()).isEqualTo(items.get(1).getId());
    assertThat(items.get(0).getDisabledDependencyItemId()).isEqualTo(items.get(2).getId());
    assertThat(items.get(1).getDisabledDependencyItemId()).isEqualTo(items.get(2).getId());
  }

  @Test
  @SuppressWarnings("unchecked") // we know the exact type of the data
  public void setGroupOptionsToGroupCheckbox() throws Exception {
    Group groupA = new Group("A", "test");
    groupA = perun.getGroupsManagerBl().createGroup(session, vo, groupA);

    ApplicationFormItem groupCheckboxItem = setUpEmbeddedGroupApplicationItemForVoForm();

    perun.getGroupsManagerBl().addGroupsToAutoRegistration(session, List.of(groupA), groupCheckboxItem);

    Map<String, Object> data = registrarManager.initRegistrar(session, vo.getShortName(), null);
    var items = (List<ApplicationFormItemWithPrefilledValue>) data.get("voFormInitial");

    String expectedOptions = groupA.getId() + "#A";

    assertThat(items.get(0).getFormItem().getI18n().get(ApplicationFormItem.EN).getOptions()).isEqualTo(
        expectedOptions);
    assertThat(items.get(0).getFormItem().getI18n().get(ApplicationFormItem.CS).getOptions()).isEqualTo(
        expectedOptions);
  }

  // seems to remove group from application object upon approving, couldn't figure out why
  private Application setUpApplicationGroup(User user, Group group) throws PerunException {
    Application voApplication = prepareApplicationToVo(user);
    Application groupApplication = prepareApplicationToGroup(user, group);
    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    voApplication = registrarManager.submitApplication(session, voApplication, appItemsData);
    registrarManager.submitApplication(session, groupApplication, appItemsData);
    registrarManager.approveApplication(session, voApplication.getId());

    return voApplication;
  }

  private Application setUpApplicationGroupWithData(User user, Group group, List<ApplicationFormItemData> data)
      throws PerunException {
    Application voApplication = prepareApplicationToVo(user);
    Application groupApplication = prepareApplicationToGroup(user, group);
    voApplication = registrarManager.submitApplication(session, voApplication, data);
    registrarManager.submitApplication(session, groupApplication, data);
    registrarManager.approveApplication(session, voApplication.getId());

    return voApplication;
  }

  private ApplicationFormItem setUpEmbeddedGroupApplicationItemForGroupForm(Group group) throws PerunException {
    registrarManager.createApplicationFormInGroup(session, group);
    ApplicationForm form = registrarManager.getFormForGroup(group);
    // create embedded groups form item
    ApplicationFormItem embeddedGroupsItem = new ApplicationFormItem();
    embeddedGroupsItem.setType(ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION);
    embeddedGroupsItem.setShortname("embeddedGroups");
    return registrarManager.addFormItem(session, form, embeddedGroupsItem);
  }

  private ApplicationFormItem setUpEmbeddedGroupApplicationItemForVoForm() throws PerunException {
    ApplicationForm form = registrarManager.getFormForVo(vo);
    // create embedded groups form item
    ApplicationFormItem embeddedGroupsItem = new ApplicationFormItem();
    embeddedGroupsItem.setType(ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION);
    embeddedGroupsItem.setShortname("embeddedGroups");
    return registrarManager.addFormItem(session, form, embeddedGroupsItem);
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

  @Before
  public void setupTest() throws Exception {

    if (vo == null || session == null) {

      session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
          ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());

      // create test VO
      vo = new Vo(0, "registrarTestVO", "regTestVO");
      vo = perun.getVosManagerBl().createVo(session, vo);

    }

  }

  @Test
  public void testAddFormItem_multipleEmbeddedGroupsItems() throws PerunException {

    // create 2 embedded groups form items
    setUpEmbeddedGroupApplicationItemForVoForm();

    ApplicationFormItem embeddedGroupsItem2 = new ApplicationFormItem();
    embeddedGroupsItem2.setType(ApplicationFormItem.Type.EMBEDDED_GROUP_APPLICATION);
    embeddedGroupsItem2.setShortname("embeddedGroups2");
    ApplicationForm form = registrarManager.getFormForVo(vo);
    assertThrows(MultipleApplicationFormItemsException.class, () -> {
      registrarManager.addFormItem(session, form, embeddedGroupsItem2);
    });
  }

  @Test
  public void testApproveApplications() throws PerunException {
    User user1 = new User(-1, "User1", "Test1", "", "", "");
    User user2 = new User(-2, "User2", "Test2", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());

    registrarManager.approveApplications(session,
        new ArrayList<>(Arrays.asList(application1.getId(), application2.getId())));

    List<Integer> approvedAppIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED"), false).stream().map(Application::getId)
            .toList();
    assertEquals(2, approvedAppIds.size());
    assertThat(approvedAppIds).containsOnly(application1.getId(), application2.getId());
  }

  @Test
  public void testApproveApplicationsOrder() throws PerunException {
    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    applicationToVo = registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());

    Application applicationToGroup = prepareApplicationToGroup(user, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>());

    registrarManager.approveApplications(session,
        new ArrayList<>(Arrays.asList(applicationToGroup.getId(), applicationToVo.getId())));

    List<Integer> approvedAppIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED"), true).stream().map(Application::getId)
            .toList();
    assertEquals(2, approvedAppIds.size());
    assertThat(approvedAppIds).containsOnly(applicationToVo.getId(), applicationToGroup.getId());
  }

  @Test
  public void testApproveDeletedApplicationAndNormalApplications() throws PerunException {
    User user1 = new User(1, "User1", "Test1", "", "", "");
    User user2 = new User(2, "User2", "Test2", "", "", "");
    User user3 = new User(3, "User3", "Test3", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);
    user3 = perun.getUsersManagerBl().createUser(session, user3);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    application2.setCreatedBy("perunTests2");
    Application application3 = prepareApplicationToVo(user3);
    application3.setCreatedBy("perunTests3");
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());
    registrarManager.submitApplication(session, application3, new ArrayList<>());

    registrarManager.rejectApplications(session, new ArrayList<>(List.of(application3.getId())), "");

    registrarManager.deleteApplications(session, new ArrayList<>(List.of(application3.getId())));

    List<ApplicationOperationResult> approveResultList = registrarManager.approveApplications(session,
        new ArrayList<>(List.of(application1.getId(), application2.getId(), application3.getId())));
    Map<Integer, Exception> approveResult = approveResultList.stream()
        .collect(HashMap::new, (map, val) -> map.put(val.getApplicationId(), val.getError()), HashMap::putAll);
    assertNull(approveResult.get(application1.getId()));
    assertNull(approveResult.get(application2.getId()));
    assertTrue(approveResult.get(application3.getId()) instanceof RegistrarException);
    List<Integer> approvedAppIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED"), false).stream().map(Application::getId)
            .toList();
    assertEquals(2, approvedAppIds.size());
    assertThat(approvedAppIds).containsOnly(application1.getId(), application2.getId());
  }

  @Test
  public void testApproveNormalAndAlreadyApprovedApplication() throws PerunException {
    User user1 = new User(-1, "User1", "Test1", "", "", "");
    User user2 = new User(-2, "User2", "Test2", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());

    registrarManager.approveApplications(session, new ArrayList<>(List.of(application2.getId())));
    List<ApplicationOperationResult> approveResultList = registrarManager.approveApplications(session,
        new ArrayList<>(Arrays.asList(application1.getId(), application2.getId())));

    List<Integer> approvedAppIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED"), false).stream().map(Application::getId)
            .toList();
    Map<Integer, Exception> approveResult = approveResultList.stream()
        .collect(HashMap::new, (map, val) -> map.put(val.getApplicationId(), val.getError()), HashMap::putAll);
    assertEquals(approveResult.keySet(), new HashSet<>(approvedAppIds));
    assertNull(approveResult.get(application1.getId()));
    assertTrue(approveResult.get(application2.getId()) instanceof RegistrarException);
    assertEquals(2, approvedAppIds.size());
    assertThat(approvedAppIds).containsOnly(application1.getId(), application2.getId());
  }

  @Test
  public void testDeleteApplications() throws PerunException {
    User user1 = new User(-1, "User1", "Test1", "", "", "");
    User user2 = new User(-2, "User2", "Test2", "", "", "");
    User user3 = new User(-3, "User3", "Test3", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);
    user3 = perun.getUsersManagerBl().createUser(session, user3);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    Application application3 = prepareApplicationToVo(user3);
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());
    registrarManager.submitApplication(session, application3, new ArrayList<>());

    registrarManager.rejectApplications(session,
        Arrays.asList(application1.getId(), application2.getId(), application3.getId()), "");

    registrarManager.deleteApplications(session,
        List.of(application1.getId(), application2.getId(), application3.getId()));

    List<Integer> applicationIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED", "NEW", "VERIFIED", "REJECTED"), false)
            .stream().map(Application::getId).toList();
    assertEquals(0, applicationIds.size());
  }

  @Test
  public void testDeleteApprovedApplicationAndRejectedApplications() throws PerunException {
    User user1 = new User(-1, "User1", "Test1", "", "", "");
    User user2 = new User(-2, "User2", "Test2", "", "", "");
    User user3 = new User(-3, "User3", "Test3", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);
    user3 = perun.getUsersManagerBl().createUser(session, user3);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    Application application3 = prepareApplicationToVo(user3);
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());
    registrarManager.submitApplication(session, application3, new ArrayList<>());

    registrarManager.approveApplication(session, application2.getId());
    registrarManager.rejectApplications(session, Arrays.asList(application1.getId(), application3.getId()), "");


    List<ApplicationOperationResult> deleteResultList = registrarManager.deleteApplications(session,
        List.of(application1.getId(), application2.getId(), application3.getId()));

    List<Integer> applicationIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("APPROVED", "NEW", "VERIFIED", "REJECTED"), false)
            .stream().map(Application::getId).toList();
    Map<Integer, Exception> deleteResult = deleteResultList.stream()
        .collect(HashMap::new, (map, val) -> map.put(val.getApplicationId(), val.getError()), HashMap::putAll);
    assertEquals(1, applicationIds.size());
    assertThat(applicationIds).containsOnly(application2.getId());
    assertEquals(new HashSet<>(List.of(application1.getId(), application2.getId(), application3.getId())),
        deleteResult.keySet());
    assertNull(deleteResult.get(application1.getId()));
    assertNull(deleteResult.get(application3.getId()));
    assertTrue(deleteResult.get(application2.getId()) instanceof RegistrarException);
  }

  @Test
  public void testEmbeddedGroupsSubmission() throws PerunException {
    GroupsManager groupsManager = perun.getGroupsManager();

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    // create groups in VO
    Group group1 = new Group("GroupA", "Cool folks");
    Group group2 = new Group("GroupB", "Cooler folks");
    groupsManager.createGroup(session, vo, group1);
    groupsManager.createGroup(session, vo, group2);

    registrarManager.addGroupsToAutoRegistration(session, List.of(group1, group2), embeddedGroupsItem);

    // create user
    User user = new User(-1, "Jo", "Doe", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    // prepare application
    Application application = prepareApplicationToVo(user);

    //set embedded groups as item in application and fill with our two groups
    String embGroupsValue = String.format("Group A#%d|Group B#%d", group1.getId(), group2.getId());
    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    appItemsData.add(new ApplicationFormItemData(embeddedGroupsItem, "Embedded groups", embGroupsValue, "0"));
    registrarManager.submitApplication(session, application, appItemsData);

    List<Group> expectedEmbeddedGroups = List.of(group1, group2);

    RegistrarManagerImpl registrarManagerImpl = AopTestUtils.getTargetObject(registrarManager);
    assertEquals(expectedEmbeddedGroups, registrarManagerImpl.getEmbeddedGroups(session, application.getId()));
    assertEquals(1, registrarManager.getApplicationsForUser(user).size());

    registrarManager.approveApplication(session, application.getId());

    assertEquals(3, registrarManager.getApplicationsForUser(user).size());
    assertEquals(1, registrarManager.getApplicationsForGroup(session, group1, List.of("NEW", "VERIFIED")).size());
    assertEquals(1, registrarManager.getApplicationsForGroup(session, group2, List.of("NEW", "VERIFIED")).size());
  }

  @Test
  public void testEmbeddedGroupsSubmission_groupAutoApprove() throws PerunException {
    GroupsManager groupsManager = perun.getGroupsManager();

    // create embedded groups form item
    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    // create groups in VO
    Group group = new Group("GroupA", "Cool folks");
    groupsManager.createGroup(session, vo, group);
    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);

    // allow auto-approve
    ApplicationForm groupForm = registrarManager.getFormForGroup(group);
    groupForm.setAutomaticApprovalEmbedded(true);
    registrarManager.updateForm(session, groupForm);

    // create user
    final User user = new User(-1, "Jo", "Doe", "", "", "");

    perun.getUsersManagerBl().createUser(session, user);

    Application application = prepareApplicationToVo(user);

    String embGroupsValue = String.format("Group A#%d", group.getId());
    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    appItemsData.add(new ApplicationFormItemData(embeddedGroupsItem, "Embedded groups", embGroupsValue, "0"));
    registrarManager.submitApplication(session, application, appItemsData);

    registrarManager.approveApplication(session, application.getId());

    List<Member> groupMembers = groupsManager.getGroupMembers(session, group);

    assertThat(groupMembers).anyMatch(member -> member.getUserId() == user.getId());
  }

  @Test
  public void testEmbeddedGroupsSubmission_initEmbeddedConflict() throws PerunException {
    GroupsManager groupsManager = perun.getGroupsManager();

    // create embedded groups form item
    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    // create group in VO, generate group application form
    Group group1 = new Group("GroupA", "Cool folks");
    groupsManager.createGroup(session, vo, group1);
    registrarManager.addGroupsToAutoRegistration(session, List.of(group1), embeddedGroupsItem);

    // create user
    User user = new User(-1, "Jo", "Doe", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    Application application = prepareApplicationToVo(user);

    // set embedded groups in VO application
    String embGroupsValue = String.format("Group A#%d", group1.getId());
    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    appItemsData.add(new ApplicationFormItemData(embeddedGroupsItem, "Embedded groups", embGroupsValue, "0"));
    registrarManager.submitApplication(session, application, appItemsData);

    // prepare group application and approve vo application
    Application groupApp = prepareApplicationToVo(user);
    groupApp.setGroup(group1);
    registrarManager.submitApplication(session, groupApp, new ArrayList<>());
    // normally, approval of VO generates and submits embedded groups applications
    registrarManager.approveApplication(session, application.getId());

    // embedded application is expected to not be created as init already exists
    List<Application> group1Apps =
        registrarManager.getApplicationsForGroup(session, group1, List.of("NEW", "VERIFIED"));
    assertEquals(1, group1Apps.size());
    assertEquals(INITIAL, group1Apps.get(0).getType());
  }

  @Test
  public void testHandleGroupApplications() throws PerunException {
    GroupsManager groupsManager = perun.getGroupsManager();

    // create group in VO, generate group application form
    Group group1 = new Group("GroupA", "Cool folks");
    group1 = groupsManager.createGroup(session, vo, group1);

    registrarManager.createApplicationFormInGroup(session, group1);
    ApplicationForm groupForm = registrarManager.getFormForGroup(group1);
    groupForm.setAutomaticApproval(true);
    registrarManager.updateForm(session, groupForm);

    // create user
    User user = new User(-1, "Jo", "Doe", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);
    ExtSource source = new ExtSource("ExtSource", ExtSourcesManager.EXTSOURCE_IDP);
    perun.getExtSourcesManagerBl().createExtSource(session, source, new HashMap<>());
    UserExtSource ues = new UserExtSource(source, session.getPerunPrincipal().getActor());
    perun.getUsersManagerBl().addUserExtSource(session, user, ues);

    Application voApplication = prepareApplicationToVo(user);
    Application groupApplication = prepareApplicationToGroup(null, group1);

    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    voApplication = registrarManager.submitApplication(session, voApplication, appItemsData);
    registrarManager.submitApplication(session, groupApplication, appItemsData);

    registrarManager.approveApplication(session, voApplication.getId());
    //We have to call this method explicitly due to transactions
    registrarManager.handleUsersGroupApplications(session, vo, user);

    List<Application> group1Apps = registrarManager.getApplicationsForGroup(session, group1, List.of("APPROVED"));
    assertEquals(1, group1Apps.size());
    assertEquals(user, group1Apps.get(0).getUser());
  }

  @Test
  public void testIsGroupForAutoRegistration() throws PerunException {
    System.out.println("GroupManager.isGroupsForAutoRegistration");

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();

    Group group = new Group("Group", "Group description");
    groupsManagerBl.createGroup(session, vo, group);
    Group nonEmbeddedGroup = new Group("NonEmbeddedGroup", "NonEmbeddedGroup description");
    groupsManagerBl.createGroup(session, vo, nonEmbeddedGroup);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForVoForm();

    assertFalse(groupsManagerBl.isGroupForAnyAutoRegistration(session, group));
    assertFalse(groupsManagerBl.isGroupForAutoRegistration(session, group, List.of(embeddedGroupsItem.getId())));
    assertFalse(
        groupsManagerBl.isGroupForAutoRegistration(session, nonEmbeddedGroup, List.of(embeddedGroupsItem.getId())));

    registrarManager.addGroupsToAutoRegistration(session, List.of(group), embeddedGroupsItem);
    assertTrue(groupsManagerBl.isGroupForAnyAutoRegistration(session, group));
    assertTrue(groupsManagerBl.isGroupForAutoRegistration(session, group, List.of(embeddedGroupsItem.getId())));
    // this group is still not added for auto registration
    assertFalse(
        groupsManagerBl.isGroupForAutoRegistration(session, nonEmbeddedGroup, List.of(embeddedGroupsItem.getId())));
  }

  @Test
  public void testIsGroupForAutoRegistrationGroupForm() throws PerunException {
    System.out.println("GroupManager.testIsGroupForAutoRegistrationGroupForm");

    GroupsManagerBl groupsManagerBl = perun.getGroupsManagerBl();

    Group group = new Group("Group", "Group description");
    groupsManagerBl.createGroup(session, vo, group);
    Group group2 = new Group("Group2", "Group2 description");
    groupsManagerBl.createGroup(session, group, group2);
    Group nonEmbeddedSubgroup = new Group("NonEmbeddedSubgroup", "NonEmbeddedSbgroup description");
    groupsManagerBl.createGroup(session, group, nonEmbeddedSubgroup);

    ApplicationFormItem embeddedGroupsItem = setUpEmbeddedGroupApplicationItemForGroupForm(group);

    assertFalse(groupsManagerBl.isGroupForAnyAutoRegistration(session, group2));
    assertFalse(groupsManagerBl.isGroupForAutoRegistration(session, group2, List.of(embeddedGroupsItem.getId())));
    assertFalse(
        groupsManagerBl.isGroupForAutoRegistration(session, nonEmbeddedSubgroup, List.of(embeddedGroupsItem.getId())));

    registrarManager.addGroupsToAutoRegistration(session, List.of(group2), group, embeddedGroupsItem);
    assertTrue(groupsManagerBl.isGroupForAnyAutoRegistration(session, group2));
    assertTrue(groupsManagerBl.isGroupForAutoRegistration(session, group2, List.of(embeddedGroupsItem.getId())));
    // this group is still not added for auto registration
    assertFalse(
        groupsManagerBl.isGroupForAutoRegistration(session, nonEmbeddedSubgroup, List.of(embeddedGroupsItem.getId())));
  }

  @Test
  public void testRejectApplications() throws PerunException {
    User user1 = new User(1, "User1", "Test1", "", "", "");
    User user2 = new User(2, "User2", "Test2", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);

    Application application1 = prepareApplicationToVo(user1);
    application1 = registrarManager.submitApplication(session, application1, new ArrayList<>());

    Application application2 = prepareApplicationToVo(user2);
    application2.setCreatedBy("perunTests2");
    application2 = registrarManager.submitApplication(session, application2, new ArrayList<>());

    registrarManager.rejectApplications(session,
        new ArrayList<>(Arrays.asList(application1.getId(), application2.getId())), null);

    List<Integer> rejectedAppIdsVO =
        registrarManager.getApplicationsForVo(session, vo, List.of("REJECTED"), false).stream().map(Application::getId)
            .toList();
    assertThat(rejectedAppIdsVO).containsOnly(application1.getId(), application2.getId());
  }

  @Test
  public void testRejectApplicationsAfterMemberRemoval() throws PerunException {
    GroupsManagerBl groupsManager = perun.getGroupsManagerBl();

    // create group in VO, generate group application form
    Group group1 = new Group("GroupA", "Cool folks");
    groupsManager.createGroup(session, vo, group1);
    registrarManager.createApplicationFormInGroup(session, group1);

    // create user
    User user = new User(-1, "Jo", "Doe", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    MembersManagerBl membersManager = perun.getMembersManagerBl();
    Member member = membersManager.createMember(session, vo, user);

    Application groupApplication = prepareApplicationToGroup(user, group1);

    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    registrarManager.submitApplication(session, groupApplication, appItemsData);

    membersManager.deleteMember(session, member);

    List<Application> group1Apps = registrarManager.getApplicationsForGroup(session, group1, List.of("REJECTED"));
    assertEquals(1, group1Apps.size());
  }

  @Test
  public void testRejectApplicationsOrder() throws PerunException {
    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    applicationToVo = registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());

    Application applicationToGroup = prepareApplicationToGroup(user, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>());

    registrarManager.rejectApplications(session,
        new ArrayList<>(Arrays.asList(applicationToVo.getId(), applicationToGroup.getId())), null);

    List<Integer> rejectedAppIds =
        registrarManager.getApplicationsForVo(session, vo, List.of("REJECTED"), true).stream().map(Application::getId)
            .toList();
    assertThat(rejectedAppIds).containsOnly(applicationToVo.getId(), applicationToGroup.getId());
  }

  @Test
  public void testRejectNormalAndAlreadyRejectedApplications() throws PerunException {
    User user1 = new User(1, "User1", "Test1", "", "", "");
    User user2 = new User(2, "User2", "Test2", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);

    Application application1 = prepareApplicationToVo(user1);
    application1 = registrarManager.submitApplication(session, application1, new ArrayList<>());

    Application application2 = prepareApplicationToVo(user2);
    application2.setCreatedBy("perunTests2");
    application2 = registrarManager.submitApplication(session, application2, new ArrayList<>());

    registrarManager.rejectApplications(session, new ArrayList<>(List.of(application2.getId())), null);
    List<ApplicationOperationResult> rejectResultList = registrarManager.rejectApplications(session,
        new ArrayList<>(Arrays.asList(application1.getId(), application2.getId())), null);

    List<Integer> rejectedAppIdsVO =
        registrarManager.getApplicationsForVo(session, vo, List.of("REJECTED"), false).stream().map(Application::getId)
            .toList();
    Map<Integer, Exception> rejectResult = rejectResultList.stream()
        .collect(HashMap::new, (map, val) -> map.put(val.getApplicationId(), val.getError()), HashMap::putAll);
    assertEquals(rejectResult.keySet(), new HashSet<>(rejectedAppIdsVO));
    assertNull(rejectResult.get(application1.getId()));
    assertTrue(rejectResult.get(application2.getId()) instanceof RegistrarException);
    assertThat(rejectedAppIdsVO).containsOnly(application1.getId(), application2.getId());
  }

  @Test
  public void testResendNotifications() throws PerunException {
    JavaMailSender mailSender = (JavaMailSender) ReflectionTestUtils.getField(mailManager, "mailSender");
    assert mailSender != null;
    JavaMailSender spyMailSender = spy(mailSender);
    ReflectionTestUtils.setField(mailManager, "mailSender", spyMailSender);

    User user1 = new User(-1, "User1", "Test1", "", "", "");
    User user2 = new User(-2, "User2", "Test2", "", "", "");
    User user3 = new User(-3, "User3", "Test3", "", "", "");
    user1 = perun.getUsersManagerBl().createUser(session, user1);
    user2 = perun.getUsersManagerBl().createUser(session, user2);
    user3 = perun.getUsersManagerBl().createUser(session, user3);

    Application application1 = prepareApplicationToVo(user1);
    Application application2 = prepareApplicationToVo(user2);
    Application application3 = prepareApplicationToVo(user3);
    registrarManager.submitApplication(session, application1, new ArrayList<>());
    registrarManager.submitApplication(session, application2, new ArrayList<>());
    registrarManager.submitApplication(session, application3, new ArrayList<>());

    application1.setState(Application.AppState.REJECTED);
    application2.setState(Application.AppState.REJECTED);
    application3.setState(Application.AppState.REJECTED);

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_REJECTED_USER, true);
    MailText t = mail.getMessage(new Locale("en"));
    t.setSubject("Test subject");
    t.setText("Test Mail content.");
    mailManager.addMail(session, form, mail);


    mailManager.sendMessages(session, List.of(application1, application2, application3), MailType.APP_REJECTED_USER,
        null);

    verify(spyMailSender, times(3)).send(any(MimeMessage.class));
  }

  @Test
  public void updateAppMailMessageTemplates() throws PerunException {
    System.out.println("updateAppMailMessageTemplates()");

    ApplicationForm form = registrarManager.getFormForVo(vo);

    ApplicationMail mail = new ApplicationMail(0, AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
    MailText t = mail.getMessage(new Locale("cs"));
    t.setSubject("Český předmět mailu");
    t.setText("Český text mailu.");
    MailText html = mail.getHtmlMessage(new Locale("cs"));
    html.setSubject("Český předmět mailu pro html");
    html.setText("<p>Český text mailu <b>v html</b>.</p>");
    MailText t2 = mail.getMessage(new Locale("en"));
    t2.setSubject("Anglický předmět mailu");
    t2.setText("Anglický text mailu.");
    MailText html2 = mail.getHtmlMessage(new Locale("en"));
    html2.setSubject("Anglický předmět mailu pro html");
    html2.setText("<p>Anglický text mailu <b>v html</b>.</p>");

    int id = mailManager.addMail(session, form, mail);
    mail = mailManager.getMailById(session, id);

    List<ApplicationMail> mails = mailManager.getApplicationMails(session, form);
    assertTrue("Mails are empty", (mails != null && !mails.isEmpty()));
    assertTrue("Our mail was not returned", mails.contains(mail));
    assertEquals("Plain text message (cs) doesn't contain correct text",
        mails.get(0).getMessage(new Locale("cs")).getText(), "Český text mailu.");
    assertEquals("Html message (cs) doesn't contain correct text",
        mails.get(0).getHtmlMessage(new Locale("cs")).getText(), "<p>Český text mailu <b>v html</b>.</p>");
    assertEquals("Plain text message (en) doesn't contain correct text",
        mails.get(0).getMessage(new Locale("en")).getText(), "Anglický text mailu.");
    assertEquals("Html message (en) doesn't contain correct text",
        mails.get(0).getHtmlMessage(new Locale("en")).getText(), "<p>Anglický text mailu <b>v html</b>.</p>");

    t = mail.getMessage(new Locale("cs"));
    t.setText("Upravený český text mailu.");
    html = mail.getHtmlMessage(new Locale("cs"));
    html.setText("<p>Upravený český text mailu <b>v html</b>.</p>");
    mailManager.updateMailById(session, mail);

    mail = mailManager.getMailById(session, id);
    assertEquals("Plain text message (cs) doesn't contain correct text", mail.getMessage(new Locale("cs")).getText(),
        "Upravený český text mailu.");
    assertEquals("Html message (cs) doesn't contain correct text", mail.getHtmlMessage(new Locale("cs")).getText(),
        "<p>Upravený český text mailu <b>v html</b>.</p>");
  }

  @Test
  public void updateFormItems_formItemsMismatch() throws PerunException {
    System.out.println("updateFormItems_formItemsMismatch");

    Vo vo2 = new Vo(0, "registrarTestVO2", "regTestVO2");
    vo2 = perun.getVosManagerBl().createVo(session, vo2);


    ApplicationFormItem testItem = new ApplicationFormItem();
    testItem.setType(ApplicationFormItem.Type.TEXTFIELD);
    testItem.setShortname("testItem");

    ApplicationForm form = registrarManager.getFormForVo(vo);
    ApplicationFormItem updatedItem = registrarManager.addFormItem(session, form, testItem);
    updatedItem.setShortname("updatedItem");
    ApplicationForm anotherForm = registrarManager.getFormForVo(vo2);
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
        () -> registrarManager.updateFormItems(session, anotherForm, List.of(updatedItem)));
  }

  @Test
  public void updateShortnameDataWithFormItemOnOpenApps() throws PerunException {
    System.out.println("updateShortnameDataWithFormItemOnOpenApps");


    ApplicationFormItem toChange = new ApplicationFormItem();
    toChange.setType(ApplicationFormItem.Type.TEXTFIELD);
    toChange.setShortname("wrongone");

    ApplicationFormItem toNotChange = new ApplicationFormItem();
    toNotChange.setType(ApplicationFormItem.Type.TEXTFIELD);
    toNotChange.setShortname("correctone");

    ApplicationForm form = registrarManager.getFormForVo(vo);
    registrarManager.updateFormItems(session, form, List.of(toChange, toNotChange));

    User user = setUpUser("Jo", "Jo");
    Application application = prepareApplicationToVo(user);

    List<ApplicationFormItemData> appItemsData = new ArrayList<>();
    appItemsData.add(new ApplicationFormItemData(toChange, "wrongone", "value1", "0"));
    appItemsData.add(new ApplicationFormItemData(toNotChange, "correctone", "value2", "0"));
    Application app = registrarManager.submitApplication(session, application, appItemsData);

    assertTrue(registrarManager.getApplicationDataById(session, app.getId()).stream()
        .map(ApplicationFormItemData::getShortname).anyMatch(n -> n.equals("wrongone")));
    assertTrue(registrarManager.getApplicationDataById(session, app.getId()).stream()
        .map(ApplicationFormItemData::getShortname).anyMatch(n -> n.equals("correctone")));

    List<ApplicationFormItem> items = registrarManager.getFormItems(session, form);
    items.stream().filter(f -> f.getShortname().equals("wrongone")).findFirst().get().setShortname("newone");

    registrarManager.updateFormItems(session, form, items);

    assertTrue(registrarManager.getApplicationDataById(session, app.getId()).stream()
        .map(ApplicationFormItemData::getShortname).noneMatch(n -> n.equals("wrongone")));
    assertTrue(registrarManager.getApplicationDataById(session, app.getId()).stream()
        .map(ApplicationFormItemData::getShortname).anyMatch(n -> n.equals("newone")));
    assertTrue(registrarManager.getApplicationDataById(session, app.getId()).stream()
        .map(ApplicationFormItemData::getShortname).anyMatch(n -> n.equals("correctone")));
  }

  @Test
  public void submitApplicationWithInvitationTokenTest() throws PerunException {
    System.out.println("submitApplicationWithInvitationTokenTest");

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

    assertEquals(applicationToGroup.getId(), (long) invitationsManager.getInvitationById(session, invitation.getId()).getApplicationId());
  }

  @Test
  public void autoApproveApplicationWithInvitationTokenTest() throws PerunException {
    System.out.println("autoApproveApplicationWithInvitationTokenTest");

    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);
    User sender = new User(2, "Sender", "Sending", "", "", "");
    sender = perun.getUsersManagerBl().createUser(session, sender);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());
    registrarManager.approveApplication(session, applicationToVo.getId());

    Invitation invitation = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation = invitationsManager.createInvitation(session, invitation);

    Application applicationToGroup = prepareApplicationToGroup(user, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>(), invitation.getToken());

    List<Application> groupApps = registrarManager.getApplicationsForGroup(session, group, List.of("APPROVED"));
    assertEquals(1, groupApps.size());
    assertEquals(user, groupApps.get(0).getUser());
    assertEquals(applicationToGroup.getId(), (long) invitationsManager.getInvitationById(session, invitation.getId()).getApplicationId());
    assertEquals(InvitationStatus.ACCEPTED, invitationsManager.getInvitationById(session, invitation.getId()).getStatus());
    assertEquals(session.getPerunPrincipal().getActor(), registrarManager.getApplicationById(session, applicationToGroup.getId()).getModifiedBy());
  }

  @Test
  public void autoApproveApplicationWithInvitationAfterVoApplicationApprovalTest() throws PerunException {
    System.out.println("autoApproveApplicationWithInvitationAfterVoApplicationApprovalTest");

    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);
    User sender = new User(2, "Sender", "Sending", "", "", "");
    sender = perun.getUsersManagerBl().createUser(session, sender);
    ExtSource source = new ExtSource("ExtSource", ExtSourcesManager.EXTSOURCE_IDP);
    perun.getExtSourcesManagerBl().createExtSource(session, source, new HashMap<>());
    UserExtSource ues = new UserExtSource(source, session.getPerunPrincipal().getActor());
    perun.getUsersManagerBl().addUserExtSource(session, user, ues);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    Application applicationToVo = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());

    Invitation invitation = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", "receiver@email.com", Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation = invitationsManager.createInvitation(session, invitation);

    Application applicationToGroup = prepareApplicationToGroup(null, group);
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, new ArrayList<>(), invitation.getToken());

    List<Application> groupApps = registrarManager.getApplicationsForGroup(session, group, List.of("APPROVED"));
    assertEquals(0, groupApps.size());
    registrarManager.approveApplication(session, applicationToVo.getId());
    // We have to call this method explicitly as it is called asynchronously in the code.
    registrarManager.handleUsersGroupApplications(session, vo, user);

    groupApps = registrarManager.getApplicationsForGroup(session, group, List.of("APPROVED"));
    assertEquals(1, groupApps.size());
    assertEquals(user, groupApps.get(0).getUser());
    assertEquals(applicationToGroup.getId(), (long) invitationsManager.getInvitationById(session, invitation.getId()).getApplicationId());
    assertEquals(InvitationStatus.ACCEPTED, invitationsManager.getInvitationById(session, invitation.getId()).getStatus());
    assertEquals(session.getPerunPrincipal().getActor(), registrarManager.getApplicationById(session, applicationToGroup.getId()).getModifiedBy());
  }

  @Test
  public void autoApproveApplicationWithInvitationTokenAndCheckAutomaticEmailVerificationTest() throws PerunException {
    System.out.println("autoApproveApplicationWithInvitationTokenAndCheckAutomaticEmailVerificationTest");

    User user = new User(1, "User1", "Test1", "", "", "");
    user = perun.getUsersManagerBl().createUser(session, user);
    User sender = new User(2, "Sender", "Sending", "", "", "");
    sender = perun.getUsersManagerBl().createUser(session, sender);

    Group group = new Group("Test", "Test group");
    perun.getGroupsManagerBl().createGroup(session, vo, group);
    registrarManager.createApplicationFormInGroup(session, group);

    ApplicationForm form = registrarManager.getFormForGroup(group);
    ApplicationFormItem mailItem = new ApplicationFormItem();
    mailItem.setType(ApplicationFormItem.Type.VALIDATED_EMAIL);
    mailItem.setShortname("embeddedGroups");
    registrarManager.addFormItem(session, form, mailItem);

    Application applicationToVo = prepareApplicationToVo(user);
    registrarManager.submitApplication(session, applicationToVo, new ArrayList<>());
    registrarManager.approveApplication(session, applicationToVo.getId());

    String receiverMail = "receiver@email.com";
    Invitation invitation = new Invitation(0, vo.getId(), group.getId(), sender.getId(),
        "receiver name", receiverMail, Locale.ENGLISH,
        LocalDate.now().plusDays(1));
    invitation = invitationsManager.createInvitation(session, invitation);

    Application applicationToGroup = prepareApplicationToGroup(user, group);

    List<ApplicationFormItemData> data = new ArrayList<>();
    data.add(new ApplicationFormItemData(mailItem, mailItem.getShortname(), receiverMail, "0"));
    applicationToGroup = registrarManager.submitApplication(session, applicationToGroup, data, invitation.getToken());

    List<Application> groupApps = registrarManager.getApplicationsForGroup(session, group, List.of("APPROVED"));
    List<ApplicationFormItemData> processedData = registrarManager.getApplicationDataById(session, applicationToGroup.getId());

    assertEquals("1", processedData.get(0).getAssuranceLevel());
    assertEquals(1, groupApps.size());
    assertEquals(user, groupApps.get(0).getUser());
    assertEquals(applicationToGroup.getId(), (long) invitationsManager.getInvitationById(session, invitation.getId()).getApplicationId());
    assertEquals(InvitationStatus.ACCEPTED, invitationsManager.getInvitationById(session, invitation.getId()).getStatus());
    assertEquals(session.getPerunPrincipal().getActor(), registrarManager.getApplicationById(session, applicationToGroup.getId()).getModifiedBy());
  }

  /*
         @Ignore
         @Test
         @Transactional
         public void customTest() throws Exception {
         System.out.println("customTest()");

         Vo vo = perun.getVosManager().getVoByShortName(session, "meta");
         ApplicationForm applicationForm = registrarManager.getFormForVo(vo);

         ApplicationFormItem i7 = new ApplicationFormItem();
         i7.setShortname("testovaci");
         i7.setPerunDestinationAttribute("urn:perun:test:attribute-def:def:test");
         i7.setRequired(true);
         i7.setRegex("\\+*[0-9 ]*");
         i7.setType(ApplicationFormItem.Type.TEXTFIELD);
         i7.getTexts(CS).setLabel("Testovaci");
         i7.getTexts(EN).setLabel("Testing");
         i7.setApplicationTypes(Arrays.asList(Application.AppType.INITIAL));
         i7 = registrarManager.addFormItem(session, applicationForm, i7);

         System.out.println(registrarManager.getFormItemById(i7.getId()));

         i7.setShortname("nic");
         i7.setOrdnum(2);
         i7.setRegex("empty");
// delete all texts
i7.getI18n().clear();
registrarManager.updateFormItems(session, vo, Arrays.asList(i7));

System.out.println(registrarManager.getFormItemById(i7.getId()));

// System.out.println(registrarManager.getApplicationDataById(session, 18));

         }
         */

  /*
         @Test
         public void approveExtensionApplication() {
         System.out.println("approveExtensionApplication()");
// extension is not yet supported
registrarManager.approveApplication(25, session);

         }
         */

}
