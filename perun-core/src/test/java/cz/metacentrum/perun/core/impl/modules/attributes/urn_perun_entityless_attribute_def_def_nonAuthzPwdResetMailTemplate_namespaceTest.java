package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_entityless_attribute_def_def_nonAuthzPwdResetMailTemplate_namespaceTest {
	private static urn_perun_entityless_attribute_def_def_nonAuthzPwdResetMailTemplate_namespace classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_entityless_attribute_def_def_nonAuthzPwdResetMailTemplate_namespace();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithMissingTag() throws Exception {
		System.out.println("testCheckValueWithMissingTag()");
		attributeToCheck.setValue("value");

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("{link}");

		classInstance.checkAttributeSyntax(session, "key", attributeToCheck);
	}
}
