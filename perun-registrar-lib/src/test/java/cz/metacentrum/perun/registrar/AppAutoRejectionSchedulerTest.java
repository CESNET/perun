package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.registrar.impl.AppAutoRejectionScheduler;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.registrar.model.Application.AppType.INITIAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
	private ExtSource extSource = new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Vo vo = new Vo(0, "ApplicationExpirationTestVO", "AppExpTestVo");

	private AppAutoRejectionScheduler scheduler;
	private AppAutoRejectionScheduler spyScheduler;

	private final Auditer auditerMock = mock(Auditer.class);

	private JdbcPerunTemplate jdbc;

	public AppAutoRejectionScheduler getScheduler() {
		return scheduler;
	}

	@Autowired
	public void setScheduler(AppAutoRejectionScheduler scheduler) {
		this.scheduler = scheduler;
		this.spyScheduler = spy(scheduler);
	}

	@Before
	public void setUp() throws Exception {
		setUpJdbc();
		setUpExtSource();
		setUpVo();

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
		Group group = createGroup();
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

		Group group = createGroup();

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(70, group, GROUP_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", Collections.singletonList(group));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.REJECTED);
	}
	@Test
	public void checkGroupApplicationShouldNotBeAutoRejected() throws Exception {
		System.out.println(CLASS_NAME + "checkGroupApplicationShouldNotBeAutoRejected");

		Group group = createGroup();

		Application submitApp = setUpAndSubmitAppForPotentialAutoRejection(50, group, GROUP_APP_EXP_RULES);

		ReflectionTestUtils.invokeMethod(spyScheduler, "groupApplicationsAutoRejection", Collections.singletonList(group));

		// check results
		Application returnedApp = registrarManager.getApplicationById(session, submitApp.getId());
		assertEquals("Application should be rejected.", returnedApp.getState(), Application.AppState.VERIFIED);
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

	/**
	 * Creates new group.
	 *
	 * @return created group
	 * @throws GroupExistsException if group already exists
	 */
	private Group createGroup() throws GroupExistsException {
		Group group = new Group();
		group.setName("Group for apply");
		return perun.getGroupsManagerBl().createGroup(session, vo, group);
	}

	/**
	 * Converts object ApplicationFormItemWithPrefillValue to ApplicationFormItemData
	 *
	 * @param object ApplicationFormItemWithPrefilledValue to convert
	 * @return converted object ApplicationFormItemData
	 */
	private ApplicationFormItemData convertAppFormItemWithPrefValToAppFormItemData(ApplicationFormItemWithPrefilledValue object) {
		return new ApplicationFormItemData(object.getFormItem(), object.getFormItem().getShortname(), "", "0");
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
			.map(this::convertAppFormItemWithPrefValToAppFormItemData)
			.collect(Collectors.toList());

		return registrarManager.submitApplication(session, application, data);
	}
}
