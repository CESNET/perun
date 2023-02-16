package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.registrar.impl.AppAutoRejectionScheduler;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.registrar.model.Application.AppType.INITIAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for auto rejection of expired applications
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class AppAutoRejectionSchedulerTest extends RegistrarBaseIntegrationTest{

	private final static String CLASS_NAME = "ApplicationAutoRejectorTest.";

	private static final String VO_APP_EXP_RULES = "urn:perun:vo:attribute-def:def:applicationExpirationRules";
	private static final String GROUP_APP_EXP_RULES = "urn:perun:group:attribute-def:def:applicationExpirationRules";
	private static final String A_V_D_REJECT_MESSAGES = AttributesManager.NS_VO_ATTR_DEF + ":applicationAutoRejectMessages";
	private static final String A_G_D_REJECT_MESSAGES = AttributesManager.NS_GROUP_ATTR_DEF + ":applicationAutoRejectMessages";
	private static final String A_U_D_PREF_LANG = AttributesManager.NS_USER_ATTR_DEF + ":preferredLanguage";

	private ExtSource extSource = new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Vo vo = new Vo(0, "ApplicationExpirationTestVO", "AppExpTestVo");
	private User applicationUser;

	private AppAutoRejectionScheduler scheduler;
	private AppAutoRejectionScheduler spyScheduler;

	private final MailManager mockMailManager = mock(MailManager.class);

	private final Auditer auditerMock = mock(Auditer.class);

	private JdbcPerunTemplate jdbc;

	public AppAutoRejectionScheduler getScheduler() {
		return scheduler;
	}

	@Autowired
	public void setScheduler(AppAutoRejectionScheduler scheduler) {
		this.scheduler = scheduler;
		ReflectionTestUtils.setField(this.scheduler.getRegistrarManager(), "mailManager", mockMailManager);
		this.spyScheduler = spy(scheduler);
	}

	@Before
	public void setUp() throws Exception {
		setUpJdbc();
		setUpExtSource();
		setUpVo();
		setApplicationUser();


		ReflectionTestUtils.setField(spyScheduler.getPerun(), "auditer", auditerMock);
	}

	@Test
	public void checkApplicationsExpirationForVo() throws Exception {
		System.out.println(CLASS_NAME + "checkApplicationsExpirationForVo");

		// set up expired application and reject it
		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);
		//ReflectionTestUtils.invokeMethod(spyScheduler, "checkApplicationsExpiration");
		spyScheduler.checkApplicationsExpiration();

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);
	}

	@Test
	public void checkApplicationsExpirationForGroup() throws Exception {
		System.out.println(CLASS_NAME + "checkApplicationsExpirationForGroup");

		// set up expired application and reject it
		Group group = createGroup("Group for apply");
		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);
		spyScheduler.checkApplicationsExpiration();

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);

	}

	@Test
	public void checkVoApplicationShouldBeAutoRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkVoApplicationShouldBeAutoRejected");

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", Collections.singletonList(vo));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);
	}

	@Test
	public void checkVoApplicationShouldNotBeAutoRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkVoApplicationShouldNotBeAutoRejected");

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(50, null, VO_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", Collections.singletonList(vo));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application shouldn't be rejected.", returnedApp.getState(), Application.AppState.VERIFIED);
	}

	@Test
	public void checkGroupApplicationShouldBeAutoRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkGroupApplicationShouldBeAutoRejected");

		Group group = createGroup("Group for apply");

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", Collections.singletonList(group));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);
	}
	@Test
	public void checkGroupApplicationShouldNotBeAutoRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkGroupApplicationShouldNotBeAutoRejected");

		Group group = createGroup("Group for apply");

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(50, group, GROUP_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", Collections.singletonList(group));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.VERIFIED);
	}

	@Test
	public void checkGroupApplicationsRejectedWhenVoApplicationRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkGroupApplicationsRejectedWhenVoApplicationRejected");

		Group group1 = createGroup("Group1");
		Group group2 = createGroup("Group2");

		Application expiredVoApp = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		Application nonExpiredGroupApp = setUpAndSubmitAppForPotentialAutoRejection(50, group1, GROUP_APP_EXP_RULES);
		Application expiredGroupApp = setUpAndSubmitAppForPotentialAutoRejection(70, group2, GROUP_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", Collections.singletonList(vo));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, expiredVoApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);

		returnedApp = registrarManager.getApplicationById(session, nonExpiredGroupApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);

		returnedApp = registrarManager.getApplicationById(session, expiredGroupApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);
	}

	@Test
	public void voAdminIgnoredCustomMessage() throws Exception {
		System.out.println(CLASS_NAME + "voAdminIgnoredCustomMessage");

		setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String messageTemplate = "Your application to %vo_name% was rejected";
		String expectedReason = "Your application to " + vo.getName() + " was rejected";

		setVoMessagesAttribute("ignoredByAdmin-en", messageTemplate);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voAdminIgnoredDefaultMessage() throws Exception {
		System.out.println(CLASS_NAME + "voAdminIgnoredDefaultMessage");

		setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String expectedReason = "Your application to VO " + vo.getName() + " was automatically rejected, because " +
				"admin didn't approve your application in a timely manner.";

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voEmailVerificationCustomMessage() throws Exception {
		System.out.println(CLASS_NAME + "voEmailVerificationCustomMessage");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String messageTemplate = "Your application to %vo_name% was rejected";
		String expectedReason = "Your application to " + vo.getName() + " was rejected";

		setVoMessagesAttribute("emailVerification-en", messageTemplate);

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voEmailVerificationDefaultMessage() throws Exception {
		System.out.println(CLASS_NAME + "voEmailVerificationDefaultMessage");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String expectedReason = "Your application to VO " + vo.getName() + " was automatically rejected, because you" +
				" didn't verify your email address.";

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupAdminIgnoredCustomMessage() throws Exception {
		System.out.println(CLASS_NAME + "groupAdminIgnoredCustomMessage");

		Group group = createGroup("Group for apply");

		setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String messageTemplate = "Your application to %group_name% was rejected";
		String expectedReason = "Your application to " + group.getName() + " was rejected";

		setGroupMessagesAttribute("ignoredByAdmin-en", messageTemplate, group);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupAdminIgnoredDefaultMessage() throws Exception {
		System.out.println(CLASS_NAME + "groupAdminIgnoredDefaultMessage");

		Group group = createGroup("Group for apply");

		setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String expectedReason = "Your application to group " + group.getName() + " was automatically rejected, because " +
				"admin didn't approve your application in a timely manner.";

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupEmailVerificationCustomMessage() throws Exception {
		System.out.println(CLASS_NAME + "groupEmailVerificationCustomMessage");

		Group group = createGroup("Group for apply");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String messageTemplate = "Your application to %group_name% was rejected";
		String expectedReason = "Your application to " + group.getName() + " was rejected";

		setGroupMessagesAttribute("emailVerification-en", messageTemplate, group);

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupEmailVerificationDefaultMessage() throws Exception {
		System.out.println(CLASS_NAME + "groupEmailVerificationDefaultMessage");

		Group group = createGroup("Group for apply");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String expectedReason = "Your application to group " + group.getName() + " was automatically rejected, " +
				"because you didn't verify your email address.";

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voAdminIgnoredCustomMessageByPreferredLangFromApplication() throws Exception {
		System.out.println(CLASS_NAME + "voAdminIgnoredCustomMessageByPreferredLangFromApplication");

		ApplicationForm voform = registrarManager.getFormForVo(vo);
		registrarManager.addFormItem(session, voform, new ApplicationFormItem(-1, "lang", true,
				ApplicationFormItem.Type.TEXTFIELD, null, null, A_U_D_PREF_LANG, ""));

		setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES, "cs", null);

		String messageTemplate = "Vase zadost do %vo_name% byla zamitnuta";
		String expectedReason = "Vase zadost do " + vo.getName() + " byla zamitnuta";

		setVoMessagesAttribute("ignoredByAdmin-cs", messageTemplate);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}
	@Test
	public void voAdminIgnoredCustomMessageByPreferredLang() throws Exception {
		System.out.println(CLASS_NAME + "voAdminIgnoredCustomMessageByPreferredLang");

		setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String messageTemplate = "Vase zadost do %vo_name% byla zamitnuta";
		String expectedReason = "Vase zadost do " + vo.getName() + " byla zamitnuta";

		Attribute attr = perun.getAttributesManagerBl().getAttribute(session, applicationUser, A_U_D_PREF_LANG);
		attr.setValue("cs");
		perun.getAttributesManagerBl().setAttribute(session, applicationUser, attr);

		setVoMessagesAttribute("ignoredByAdmin-cs", messageTemplate);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voAdminIgnoredCustomMessageDefault() throws Exception {
		System.out.println(CLASS_NAME + "voAdminIgnoredCustomMessageDefault");

		setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String messageTemplate = "Your application to %vo_name% was rejected";
		String expectedReason = "Your application to " + vo.getName() + " was rejected";

		Attribute attr = perun.getAttributesManagerBl().getAttribute(session, applicationUser, A_U_D_PREF_LANG);
		attr.setValue("cs");
		perun.getAttributesManagerBl().setAttribute(session, applicationUser, attr);

		setVoMessagesAttribute("ignoredByAdmin", messageTemplate);

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void voMailVerificationCustomMessageDefault() throws Exception {
		System.out.println(CLASS_NAME + "voMailVerificationCustomMessageDefault");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, null, VO_APP_EXP_RULES);

		String messageTemplate = "Your application to %vo_name% was rejected";
		String expectedReason = "Your application to " + vo.getName() + " was rejected";

		Attribute attr = perun.getAttributesManagerBl().getAttribute(session, applicationUser, A_U_D_PREF_LANG);
		attr.setValue("cs");
		perun.getAttributesManagerBl().setAttribute(session, applicationUser, attr);

		setVoMessagesAttribute("emailVerification", messageTemplate);

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "voApplicationsAutoRejection", List.of(vo));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupAdminIgnoredCustomMessageDefault() throws Exception {
		System.out.println(CLASS_NAME + "groupAdminIgnoredCustomMessageDefault");

		Group group = createGroup("Group for apply");

		setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String messageTemplate = "Your application to %group_name% was rejected";
		String expectedReason = "Your application to " + group.getName() + " was rejected";

		Attribute attr = perun.getAttributesManagerBl().getAttribute(session, applicationUser, A_U_D_PREF_LANG);
		attr.setValue("cs");
		perun.getAttributesManagerBl().setAttribute(session, applicationUser, attr);

		setGroupMessagesAttribute("ignoredByAdmin", messageTemplate, group);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	@Test
	public void groupMailVerificationCustomMessageDefault() throws Exception {
		System.out.println(CLASS_NAME + "groupMailVerificationCustomMessageDefault");

		Group group = createGroup("Group for apply");

		Application application = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		String messageTemplate = "Your application to %group_name% was rejected";
		String expectedReason = "Your application to " + group.getName() + " was rejected";

		Attribute attr = perun.getAttributesManagerBl().getAttribute(session, applicationUser, A_U_D_PREF_LANG);
		attr.setValue("cs");
		perun.getAttributesManagerBl().setAttribute(session, applicationUser, attr);

		setGroupMessagesAttribute("emailVerification", messageTemplate, group);

		// fake that the application is waiting for mail verification
		jdbc.update("UPDATE application SET state = 'NEW' WHERE application.id = ?", application.getId());

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", List.of(group));
		verify(mockMailManager)
				.sendMessage(any(), any(), eq(expectedReason), eq(null));
	}

	// ------------------------------------------------ PRIVATE METHODS ------------------------------------------------

	private void setUpJdbc() {
		jdbc = (JdbcPerunTemplate) ReflectionTestUtils.getField(scheduler, "jdbc");
	}

	private void setUpExtSource() throws Exception {
		extSource = perun.getExtSourcesManager().createExtSource(session, extSource, null);
	}

	private void setUpVo() throws Exception {
		vo = perun.getVosManager().createVo(session, vo);
		perun.getExtSourcesManager().addExtSource(session, vo, extSource);
	}

	private void setApplicationUser() {
		applicationUser = new User(-1, "John", "Doe", "", "", "");
		applicationUser = perun.getUsersManagerBl().createUser(session, applicationUser);
	}

	/**
	 * Creates new group.
	 *
	 * @return created group
	 * @throws GroupExistsException if group already exists
	 */
	private Group createGroup(String name) throws GroupExistsException {
		Group group = new Group();
		group.setName(name);
		return perun.getGroupsManagerBl().createGroup(session, vo, group);
	}

	/**
	 * Converts object ApplicationFormItemWithPrefillValue to ApplicationFormItemData. If the given
	 * item is mapped to user:preferredLanguage attribute, sets it the given lang value.
	 *
	 * @param object ApplicationFormItemWithPrefilledValue to convert
	 * @return converted object ApplicationFormItemData
	 */
	private ApplicationFormItemData convertAppFormItemWithPrefValToAppFormItemData(ApplicationFormItemWithPrefilledValue object, String lang) {
		String value = "";
		if (A_U_D_PREF_LANG.equals(object.getFormItem().getPerunDestinationAttribute())) {
			value = lang;
		}
		return new ApplicationFormItemData(object.getFormItem(), object.getFormItem().getShortname(), value, "0");
	}

	/**
	 * In the first step method sets the return value for getCurrentLocalDay method. Then sets the application expiration
	 * attribute for Vo or Group and creates and submits application.
	 *
	 * @param days number of days for move today
	 * @param group optional group
	 * @param attribute application expiration attribute for Vo or Group
	 * @return submitted application
	 * @throws Exception exception
	 */
	private Application setUpAndSubmitAppForPotentialAutoRejection(int days, Group group, String attribute) throws Exception {
		return setUpAndSubmitAppForPotentialAutoRejection(days, group, attribute, null, applicationUser);
	}

	/**
	 * In the first step method sets the return value for getCurrentLocalDay method. Then sets the application expiration
	 * attribute for Vo or Group and creates and submits application. If the application has an item mapping to
	 * user:preferredLanguage attribute, set it the given lang value.
	 *
	 * @param days number of days for move today
	 * @param group optional group
	 * @param attribute application expiration attribute for Vo or Group
	 * @return submitted application
	 * @throws Exception exception
	 */
	private Application setUpAndSubmitAppForPotentialAutoRejection(int days, Group group, String attribute, String lang, User user) throws Exception {
		// change today date for test
		LocalDate today = LocalDate.now().plusDays(days);
		when(spyScheduler.getCurrentLocalDate())
			.thenReturn(today);

		// set application expiration attribute for Vo
		Attribute appExp = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, attribute));
		Map<String, String> attrValue = new LinkedHashMap<>();
		attrValue.put("emailVerification", "14");
		attrValue.put("ignoredByAdmin", "60");
		appExp.setValue(attrValue);
		if (group != null) {
			perun.getAttributesManagerBl().setAttribute(session, group, appExp);
		} else {
			perun.getAttributesManagerBl().setAttribute(session, vo, appExp);
		}

		// create application
		Application application = new Application();
		application.setType(INITIAL);
		application.setCreatedBy("testUser");
		application.setVo(vo);

		// get application form item data
		ApplicationForm applicationForm;
		if (group != null) {
			application.setGroup(group);
			registrarManager.createApplicationFormInGroup(session, group);
			applicationForm = registrarManager.getFormForGroup(group);
		} else {
			applicationForm = registrarManager.getFormForVo(vo);
		}
		List<ApplicationFormItemData> data = registrarManager.getFormItemsWithPrefilledValues(session, INITIAL, applicationForm).stream()
			.map(item -> convertAppFormItemWithPrefValToAppFormItemData(item, lang))
			.collect(Collectors.toList());

		application.setUser(user);

		return registrarManager.submitApplication(session, application, data);
	}

	private void setVoMessagesAttribute(String key, String messageTemplate) throws Exception {
		Attribute attrToSet = perun.getAttributesManager().getAttribute(session, vo, A_V_D_REJECT_MESSAGES);
		HashMap<String, String> attrValue = new LinkedHashMap<>();
		attrValue.put(key, messageTemplate);
		attrToSet.setValue(attrValue);
		perun.getAttributesManagerBl().setAttribute(session, vo, attrToSet);
	}

	private void setGroupMessagesAttribute(String key, String messageTemplate, Group group) throws Exception {
		Attribute attrToSet = perun.getAttributesManager().getAttribute(session, group, A_G_D_REJECT_MESSAGES);
		HashMap<String, String> attrValue = new LinkedHashMap<>();
		attrValue.put(key, messageTemplate);
		attrToSet.setValue(attrValue);
		perun.getAttributesManagerBl().setAttribute(session, group, attrToSet);
	}
}
