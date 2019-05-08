package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestExecutionListeners;
import org.w3c.dom.Attr;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Test of UserExtSource storedAttributes
 *
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_ues_attribute_def_def_storedAttributesTest {

	private static PerunSessionImpl session;
	private static UserExtSource userExtSource;
	private static urn_perun_ues_attribute_def_def_storedAttributes classInstance;

	@Before
	public void setUp() {
		classInstance = new urn_perun_ues_attribute_def_def_storedAttributes();
		userExtSource = new UserExtSource();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue(null);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue("{}");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue("{\"key\":\"value\"}");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue("{\"key\":\"value\",\"key2\":\"value2\",\"key3\":[\"val1\",\"val2\"]}");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithEmptyValue() throws Exception{
		System.out.println("testCheckAttributeValueWithEmptyValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithInvalidJSON1() throws Exception{
		System.out.println("testCheckAttributeValueWithInvalidJSON1()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("{");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithInvalidJSON2() throws Exception{
		System.out.println("testCheckAttributeValueWithInvalidJSON1()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("{key\":\"value\",\"key2\":\"value2\"}");
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}
}
