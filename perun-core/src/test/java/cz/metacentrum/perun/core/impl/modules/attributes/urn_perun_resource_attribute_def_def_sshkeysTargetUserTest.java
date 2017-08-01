package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_resource_attribute_def_def_sshkeysTargetUserTest {

	private static PerunSessionImpl session;
	private static urn_perun_resource_attribute_def_def_sshkeysTargetUser classInstance;
	private static Resource resource;

	@Before
	public void setUp() {
		classInstance = new urn_perun_resource_attribute_def_def_sshkeysTargetUser();
		resource = new Resource();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Jan_Nepomucky");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);

		attributeToCheck.setValue(".John_Dale.");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);

		attributeToCheck.setValue("_Adele-Frank");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithWrongValueHyphen() throws Exception {
		System.out.println("testCheckAttributeValueWithWrongValueHyphen()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("-Adam");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithWrongValueWhitespace() throws Exception {
		System.out.println("testCheckAttributeValueWithWrongValueWhitespace()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Elena Fuente");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithWrongValueDiacritic() throws Exception {
		System.out.println("testCheckAttributeValueWithWrongValueDiacritic()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Jan_Veselý");
		classInstance.checkAttributeValue(session, resource, attributeToCheck);
	}
}
