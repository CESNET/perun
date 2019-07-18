package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailText;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cz.metacentrum.perun.registrar.model.Application.AppType.INITIAL;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.CS;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.EN;
import static org.junit.Assert.*;

/**
 * Base registrar-lib test class
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-core.xml", "classpath:perun-registrar-lib.xml" } )
@Transactional(transactionManager = "perunTransactionManager")
public class RegistrarBaseIntegrationTest {


	@Autowired PerunBl perun;
	@Autowired RegistrarManager registrarManager;
	@Autowired MailManager mailManager;
	PerunSession session;
	private Vo vo;

	@Before
	public void setupTest() throws Exception {

		if (vo == null || session == null) {

			session = perun.getPerunSession(
					new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
					new PerunClient());

			// create test VO
			vo = new Vo(0,"registrarTestVO","regTestVO");
			vo = perun.getVosManagerBl().createVo(session, vo);

		}

	}

	@After
	public void cleanTest() throws Exception {

		//perun.getVosManagerBl().deleteVo(session, vo, true);

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

	@Test
	public void createAppMail() throws PerunException {
		System.out.println("createAppMail()");

		// get form for VO (if not exists, it's created)
		//Vo vo = perun.getVosManager().getVoByShortName(session, "meta");
		ApplicationForm form = registrarManager.getFormForVo(vo);

		ApplicationMail mail = new ApplicationMail(0,AppType.INITIAL, form.getId(), MailType.APP_CREATED_USER, true);
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
	public void createVOformIntegrationTest() throws PerunException {
		System.out.println("createVOformIntegrationTest()");

		// get form for VO (if not exists, it's created)
		ApplicationForm applicationForm = registrarManager.getFormForVo(vo);

		// put in standard options

		ApplicationFormItem i0 = new ApplicationFormItem();
		i0.setShortname("pokecI");
		i0.setType(ApplicationFormItem.Type.HTML_COMMENT);
		i0.getTexts(CS).setLabel("Vyplňte, prosím, přihlášku.");
		i0.getTexts(EN).setLabel("Fill in the initial application, please.");
		i0.setApplicationTypes(Collections.singletonList(INITIAL));
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
		i2.setType(ApplicationFormItem.Type.FROM_FEDERATION_HIDDEN);
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
		i5.setType(ApplicationFormItem.Type.FROM_FEDERATION_SHOW);
		i5.setRequired(true);
		i5.setFederationAttribute("Shib-Person-o");
		i5.getTexts(CS).setLabel("Organizace");
		i5.getTexts(EN).setLabel("Organisation");
		registrarManager.addFormItem(session, applicationForm, i5);

		ApplicationFormItem i5b = new ApplicationFormItem();
		i5b.setShortname("affiliation");
		i5b.setPerunDestinationAttribute("urn:perun:member:attribute-def:opt:eduPersonAffiliation");
		i5b.setType(ApplicationFormItem.Type.FROM_FEDERATION_HIDDEN);
		i5b.setRequired(true);
		i5b.setFederationAttribute("Shib-EP-Affiliation");
		registrarManager.addFormItem(session, applicationForm, i5b);

		ApplicationFormItem i5c = new ApplicationFormItem();
		i5c.setShortname("mail");
		i5c.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:mail");
		i5c.setType(ApplicationFormItem.Type.FROM_FEDERATION_HIDDEN);
		i5c.setRequired(true);
		i5c.setFederationAttribute("Shib-InetOrgPerson-mail");
		registrarManager.addFormItem(session, applicationForm, i5c);

		ApplicationFormItem i6 = new ApplicationFormItem();
		i6.setShortname("vyuziti");
		i6.setPerunDestinationAttribute("urn:perun:member:attribute-def:opt:registrationNote");
		i6.setRequired(true);
		i6.setType(ApplicationFormItem.Type.TEXTAREA);
		i6.getTexts(CS).setLabel("Popis plánovaného využití MetaCentra");
		i6.getTexts(CS).setHelp("Uveďte stručně, jakou činností se hodláte v MetaCentru zabývat. Uveďte také Vaše nadstandardní požadavky, požadavky, které nejsou pokryty položkami formuláře, případně jiné skutečnosti, které považujete za podstatné pro vyřízení přihlášky.");
		i6.getTexts(EN).setLabel("Description of planned activity");
		i6.getTexts(EN).setHelp("Describe shortly activity which you plane to perform at MetaCentrum. Mention your nonstandard demands, requests which are not covered in this form, eventually anything you consider important for this registration too.");
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
		i10.setPerunDestinationAttribute("urn:perun:user:attribute-def:def:login-namespace:meta");
		i10.getTexts(CS).setLabel("Zvolte si uživatelské jméno");
		i10.getTexts(CS).setHelp("Uživatelské jméno musí začínat malým písmenem, a obsahovat pouze malá písmena, číslice a podtržení. Doporučujeme délku nanejvýš 8 znaků.");
		i10.getTexts(EN).setLabel("Choose you user name");
		i10.getTexts(EN).setHelp("User name must begin with a small letter, and can contain only small letters, digits and underscores. We recommend length max 8 characters.");
		i10.setApplicationTypes(Collections.singletonList(INITIAL));
		registrarManager.addFormItem(session, applicationForm, i10);

		ApplicationFormItem i11 = new ApplicationFormItem();
		i11.setShortname("heslo");
		i11.setRequired(true);
		i11.setRegex("\\p{Print}{8,20}");
		i11.setType(ApplicationFormItem.Type.PASSWORD);
		i11.getTexts(CS).setLabel("Heslo");
		i11.getTexts(CS).setHelp("Heslo musí být 8 až 20 znaků dlouhé, a obsahovat aspoň 3 písmena a 1 znak jiný než písmeno.");
		i11.getTexts(EN).setLabel("Password");
		i11.getTexts(EN).setHelp("Password must be from 8 up to 20 characters long and contain printable characters only.");
		i11.setApplicationTypes(Collections.singletonList(INITIAL));
		registrarManager.addFormItem(session, applicationForm, i11);

		ApplicationFormItem i12 = new ApplicationFormItem();
		i12.setShortname("pokec");
		i12.setType(ApplicationFormItem.Type.HTML_COMMENT);
		i12.getTexts(CS).setLabel("Stiskem tlačítka 'Podat žádost o členství ve VO MetaCentrum' souhlaste s pravidly využití VO MetaCentrum.");
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
		assertTrue("Item i5b was not returned from form", items.contains(i5b));
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
				new PerunPrincipal("rumcajs" + random.nextInt(100000) + "@raholec.cz", "http://www.raholec.cz/idp/", ExtSourcesManager.EXTSOURCE_IDP),
				new PerunClient());

	}

	private static void applyForMembershipInVO(RegistrarManager registrarManager, PerunBl perun, Vo vo,PerunSession user) throws PerunException {

		Map<String,String> feder = new HashMap<>();
		feder.put("Shib-Person-displayName","pplk. doc. Ing. Václav Rumcajs, DrSc.");
		feder.put("Shib-Person-commonName","Václav Rumcajs");
		feder.put("Shib-Person-givenName","Václav");
		feder.put("Shib-Person-sureName","Rumcajs");
		feder.put("Shib-Person-o","Les Řáholec");
		feder.put("Shib-EP-Affiliation","member");
		feder.put("Shib-InetOrgPerson-mail","mail@gmail.org");
		feder.put("Shib-EP-PrincipalName",user.getPerunPrincipal().getActor());

		user.getPerunPrincipal().getAdditionalInformations().putAll(feder);

		List<ApplicationFormItemWithPrefilledValue> prefilledForm = registrarManager.getFormItemsWithPrefilledValues(user, INITIAL, registrarManager.getFormForVo(vo));

		//data z federace a od uzivatele
		Application application = new Application();
		application.setType(INITIAL);
		application.setCreatedAt(user.getPerunPrincipal().getActor());
		application.setExtSourceName(user.getPerunPrincipal().getExtSourceName());
		application.setExtSourceType(ExtSourcesManager.EXTSOURCE_IDP);
		application.setFedInfo(feder.toString());
		application.setVo(vo);

		List<ApplicationFormItemData> data = new ArrayList<>();
		for (ApplicationFormItemWithPrefilledValue itemW : prefilledForm) {
			ApplicationFormItem item = itemW.getFormItem();
			//log.info("prefilled item "+itemW);
			if(item.getShortname().equals("preferredMail")) {
				data.add(new ApplicationFormItemData(item, item.getShortname(),"rumcajs@gmail.com" , "0"));
			} else if(item.getShortname().equals("username")) {
				data.add(new ApplicationFormItemData(item, item.getShortname(),"rumcik" , "0"));
			} else {
				//nechej predvyplnenou hodnotu
				data.add(new ApplicationFormItemData(item, item.getShortname(), itemW.getPrefilledValue(), itemW.getAssuranceLevel()));
			}
		}
		registrarManager.createApplication(user, application, data);
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
