package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_member_group_attribute_def_virt_groupStatusTest {

	private static urn_perun_member_group_attribute_def_virt_groupStatus classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void SetUp() {
		classInstance = new urn_perun_member_group_attribute_def_virt_groupStatus();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		session = mock(PerunSessionImpl.class);
	}

	@Test
	public void testCheckAttributeValueNull() throws Exception {
		System.out.println("testCheckAttributeValueNull()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);
	}

	@Test
	public void testCheckAttributeValueCorrect() throws Exception {
		System.out.println("testCheckAttributeValueCorrect()");

		attributeToCheck.setValue("VALID");
		classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);

		attributeToCheck.setValue("EXPIRED");
		classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongValue() throws Exception {
		System.out.println("testCheckAttributeWrongValue()");
		attributeToCheck.setValue("SUSPENDED");

		classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);
	}
}