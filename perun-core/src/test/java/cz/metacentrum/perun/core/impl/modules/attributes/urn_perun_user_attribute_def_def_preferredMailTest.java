package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_preferredMailTest {

	private static urn_perun_user_attribute_def_def_preferredMail classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_preferredMail();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test
	public void testCheckCorrectAttributeSyntax() throws Exception {
		System.out.println("testCheckCorrectAttributeSyntax()");
		attributeToCheck.setValue("my@example.com");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		attributeToCheck.setValue("bad@example");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckSemanticsWithNullAttributeValue() throws Exception {
		System.out.println("testCheckSemanticsWithNullAttributeValue()");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");
		attributeToCheck.setValue("my@example.com");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
