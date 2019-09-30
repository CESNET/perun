package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_entityless_attribute_def_def_namespace_uid_policyTest {
	private static urn_perun_entityless_attribute_def_def_namespace_uid_policy classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_entityless_attribute_def_def_namespace_uid_policy();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testWrongValue() throws Exception {
		System.out.println("testWrongValue()");
		attributeToCheck.setValue("wrong value");

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testValueLesserThan1()");

		attributeToCheck.setValue("recycle");
		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);

		attributeToCheck.setValue("increment");
		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}
}
