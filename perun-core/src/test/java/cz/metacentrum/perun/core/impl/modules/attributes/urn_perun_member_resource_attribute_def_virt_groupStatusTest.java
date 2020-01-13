package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class urn_perun_member_resource_attribute_def_virt_groupStatusTest {

	private static urn_perun_member_resource_attribute_def_virt_groupStatus classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Member member;
	private static Resource resource;

	@Before
	public void SetUp() throws Exception {
		classInstance = new urn_perun_member_resource_attribute_def_virt_groupStatus();
		attributeToCheck = new Attribute();
		member = new Member();
		resource = new Resource();
		session = mock(PerunSessionImpl.class);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		attributeToCheck.setValue("wrong_value");

		classInstance.checkAttributeSyntax(session, member, resource, attributeToCheck);
	}

	@Test
	public void testCheckAttributeValueCorrectSyntax() throws Exception {
		System.out.println("testCheckAttributeValueCorrectSyntax()");

		attributeToCheck.setValue("VALID");
		classInstance.checkAttributeSyntax(session, member, resource, attributeToCheck);

		attributeToCheck.setValue("EXPIRED");
		classInstance.checkAttributeSyntax(session, member, resource, attributeToCheck);
	}
}
