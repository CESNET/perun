package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_vo_attribute_def_def_notificationsDefLangTest {
	private static urn_perun_vo_attribute_def_def_notificationsDefLang classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Vo vo;

	@Before
	public void setUp() {
		classInstance = new urn_perun_vo_attribute_def_def_notificationsDefLang();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
		vo = new Vo();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testUnsupportedNotificationLanguage() throws Exception {
		System.out.println("testUnsupportedNotificationLanguage()");
		attributeToCheck.setValue("de");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test
	public void testSupportedNotificationLanguage() throws Exception {
		System.out.println("testSupportedNotificationLanguage()");
		attributeToCheck.setValue("en");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}
}
