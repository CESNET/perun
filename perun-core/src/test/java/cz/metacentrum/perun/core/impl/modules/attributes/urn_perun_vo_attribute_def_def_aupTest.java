package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_vo_attribute_def_def_aupTest {

	private static urn_perun_vo_attribute_def_def_aup classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Vo vo;

	@Before
	public void setUp() {
		classInstance = new urn_perun_vo_attribute_def_def_aup();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
		vo = new Vo();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingVersion() throws Exception {
		System.out.println("testCheckValueWithMissingVersion()");
		attributeToCheck.setValue("[{date: date, link: link, text: text}]");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingDate() throws Exception {
		System.out.println("testCheckValueWithMissingDate()");
		attributeToCheck.setValue("[{version: version, link: link, text: text}]");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingLink() throws Exception {
		System.out.println("testCheckValueWithMissingLink()");
		attributeToCheck.setValue("[{version: version, date: date, text: text}]");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingText() throws Exception {
		System.out.println("testCheckValueWithMissingText()");
		attributeToCheck.setValue("[{version: version, date: date, link: link}]");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("[{version: version, date: date, link: link, text: text}]");

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}
}
