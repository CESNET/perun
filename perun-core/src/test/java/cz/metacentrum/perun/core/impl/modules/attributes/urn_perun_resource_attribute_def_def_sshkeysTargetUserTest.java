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
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Jan_Nepomucky");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

		attributeToCheck.setValue(".John_Dale.");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

		attributeToCheck.setValue("_Adele-Frank");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValueHyphen() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithWrongValueHyphen()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("-Adam");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValueWhitespace() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithWrongValueWhitespace()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Elena Fuente");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValueDiacritic() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithWrongValueDiacritic()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Jan_Veselý");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}
}
