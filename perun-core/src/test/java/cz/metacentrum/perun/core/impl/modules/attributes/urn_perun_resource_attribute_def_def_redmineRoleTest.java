package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_resource_attribute_def_def_redmineRoleTest {

	private urn_perun_resource_attribute_def_def_redmineRole classInstance;
	private Attribute attributeToCheck;
	private Resource resource = new Resource();
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_resource_attribute_def_def_redmineRole();
		attributeToCheck = new Attribute();
		sess = mock(PerunSessionImpl.class);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testSyntaxWithWrongValue() throws Exception {
		System.out.println("testSyntaxWithWrongValue()");
		attributeToCheck.setValue("bad_example");

		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
	}

	@Test
	public void testSyntaxCorrect() throws Exception {
		System.out.println("testSyntaxCorrect()");

		attributeToCheck.setValue("Manager");
		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);

		attributeToCheck.setValue("Reporter");
		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);

		attributeToCheck.setValue("Developer");
		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
	}

	@Test
	public void testSemanticsWithNullValue() throws Exception {
		System.out.println("testSemanticsWithNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
	}

	@Test
	public void testSemanticsCorrect() throws Exception {
		System.out.println("testSemanticsCorrect()");
		attributeToCheck.setValue("Manager");

		classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
	}
}
