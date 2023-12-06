package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_facility_attribute_def_virt_enabledO365MailForwardTest {

	private static urn_perun_user_facility_attribute_def_virt_enabledO365MailForward classInstance;
	private static PerunSessionImpl session;
	private static Attribute enabledMailFwdAttr;
	private static Attribute disabledMailFwdAttr;
	private static Attribute nullMailFwdAttr;
	private static Attribute o365MailAttr;
	private static User user;
	private static Facility facility;
	private static Resource resource;
	private static final String exampleMail = "example mail";


	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_facility_attribute_def_virt_enabledO365MailForward();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		user = new User();
		facility = new Facility();
		resource = new Resource();

		enabledMailFwdAttr = new Attribute();
		disabledMailFwdAttr = new Attribute();
		nullMailFwdAttr = new Attribute();
		o365MailAttr = new Attribute();
		enabledMailFwdAttr.setValue(false);
		disabledMailFwdAttr.setValue(true);
		nullMailFwdAttr.setValue(null);
		o365MailAttr.setValue(exampleMail);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":o365MailForward"))).thenReturn(o365MailAttr);
	}

	@Test
	public void getAttributeValueWithEnabledFwdTest() throws Exception {
		System.out.println("urn_perun_user_facility_attribute_def_virt_enabledO365MailForward.GetAttributeValue()");
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":disableO365MailForward"))).thenReturn(enabledMailFwdAttr);

		Attribute testAttr = classInstance.getAttributeValue(session, user, facility, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "enabledO365MailForward"));
		assertEquals(exampleMail, testAttr.getValue());
	}

	@Test
	public void getAttributeValueWithNullFwdTest() throws Exception {
		System.out.println("urn_perun_user_facility_attribute_def_virt_enabledO365MailForward.GetAttributeValue()");
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":disableO365MailForward"))).thenReturn(nullMailFwdAttr);

		Attribute testAttr = classInstance.getAttributeValue(session, user, facility, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "enabledO365MailForward"));
		assertEquals(exampleMail, testAttr.getValue());
	}

	@Test
	public void getAttributeValueWithDisabledFwdTest() throws Exception {
		System.out.println("urn_perun_user_facility_attribute_def_virt_enabledO365MailForward.GetAttributeValue()");
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":disableO365MailForward"))).thenReturn(disabledMailFwdAttr);

		Attribute testAttr = classInstance.getAttributeValue(session, user, facility, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "enabledO365MailForward"));
		assertEquals("", testAttr.getValue());
	}
}
