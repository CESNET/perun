package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_phoneTest {

	private static urn_perun_user_attribute_def_def_phone classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_phone();
		session = mock(PerunSessionImpl.class);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("+420123456789");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		attributeToCheck.setValue("123456789");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testNullValue() throws Exception {
		System.out.println("testNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("+420123456789");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
