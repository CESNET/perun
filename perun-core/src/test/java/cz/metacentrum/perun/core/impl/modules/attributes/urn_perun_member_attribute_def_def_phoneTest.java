package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Tests attribute module.
 */
public class urn_perun_member_attribute_def_def_phoneTest {

	private urn_perun_member_attribute_def_def_phone classInstance;
	private PerunSessionImpl session;
	private Attribute attributeToCheck;
	private Member member;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_member_attribute_def_def_phone();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		session = mock(PerunSessionImpl.class);
		member = mock(Member.class);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckNull() throws Exception {
		System.out.println("testCheckNull()");
		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, member, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckType() throws Exception {
		System.out.println("testCheckType()");
		attributeToCheck.setValue(Lists.newArrayList("+420123456789"));
		classInstance.checkAttributeSyntax(session, member, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckEmailSyntax() throws Exception {
		System.out.println("testCheckEmailSyntax()");
		attributeToCheck.setValue("a/-+");
		classInstance.checkAttributeSyntax(session, member, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("+420123456789");
		classInstance.checkAttributeSemantics(session, member, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("+420123456789");
		classInstance.checkAttributeSyntax(session, member, attributeToCheck);
	}
}
