package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class urn_perun_entityless_attribute_def_def_orgAupsTest {

	private static urn_perun_entityless_attribute_def_def_orgAups classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_entityless_attribute_def_def_orgAups();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithEmptyValue() throws Exception {
		System.out.println("testCheckValueWithEmptyValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingVersion() throws Exception {
		System.out.println("testCheckValueWithMissingVersion()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "[{date: date, link: link, text: text}]");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingDate() throws Exception {
		System.out.println("testCheckValueWithMissingDate()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "[{version: version, link: link, text: text}]");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingLink() throws Exception {
		System.out.println("testCheckValueWithMissingLink()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "[{version: version, date: date, text: text}]");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingText() throws Exception {
		System.out.println("testCheckValueWithMissingText()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "[{version: version, date: date, link: link}]");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("key", "[{version: version, date: date, link: link, text: text}]");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}
}
