package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_group_attribute_def_def_authoritativeGroupTest {

	private urn_perun_group_attribute_def_def_authoritativeGroup classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_authoritativeGroup();
		sess = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckSmallValue() throws Exception {
		System.out.println("testCheckSmallValue()");
		attributeToCheck.setValue(-1);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckHighValue() throws Exception {
		System.out.println("testCheckHighValue()");
		attributeToCheck.setValue(2);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckMemberGroup() throws Exception {
		System.out.println("testCheckHighValue()");
		attributeToCheck.setValue(1);
		group.setName(VosManager.MEMBERS_GROUP);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");

		attributeToCheck.setValue(0);
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);

		attributeToCheck.setValue(1);
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}
}
