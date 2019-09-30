package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class urn_perun_entityless_attribute_def_def_usedGidsTest {

	private static urn_perun_entityless_attribute_def_def_usedGids classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_entityless_attribute_def_def_usedGids();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithNullKeyInMap() throws Exception {
		System.out.println("testCheckValueWithNullKeyInMap()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put(null, "");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithWrongKeyInMap() throws Exception {
		System.out.println("testCheckValueWithWrongKeyInMap()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("bad value", "");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithNullValueInMap() throws Exception {
		System.out.println("testCheckValueWithNullValueInMap()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("R11", null);
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithWrongValueInMap() throws Exception {
		System.out.println("testCheckValueWithWrongValueInMap()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("R11", "bad value");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithDepletedAndUsedValue() throws Exception {
		System.out.println("testCheckValueWithNullKey()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("R11", "11");
		value.put("D11", "11");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCheckValueWithNullKey()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("R11", "11");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

}
