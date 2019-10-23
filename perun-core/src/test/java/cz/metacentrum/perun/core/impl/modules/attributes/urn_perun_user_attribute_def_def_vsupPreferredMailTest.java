package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_vsupPreferredMailTest {

	private static urn_perun_user_attribute_def_def_vsupPreferredMail classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_vsupPreferredMail();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		attributeToCheck = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);
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
