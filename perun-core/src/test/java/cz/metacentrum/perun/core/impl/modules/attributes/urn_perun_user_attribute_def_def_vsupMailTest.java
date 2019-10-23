package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_vsupMailTest {

	private static urn_perun_user_attribute_def_def_vsupMail classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_vsupMail();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		attributeToCheck = new Attribute();
		reqAttribute = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:vsup")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckSemanticsWithInvalidLogin() throws Exception {
		System.out.println("testCheckSemanticsWithInvalidLogin()");
		reqAttribute.setValue("bad_example");
		attributeToCheck.setValue("example@vsup.cz");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		reqAttribute.setValue("example");
		attributeToCheck.setValue("example@vsup.cz");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testSyntaxWithIncorrectEmail() throws Exception {
		System.out.println("testSyntaxWithIncorrectEmail()");
		attributeToCheck.setValue("bad@example");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("example@vsup.cz");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}
}
