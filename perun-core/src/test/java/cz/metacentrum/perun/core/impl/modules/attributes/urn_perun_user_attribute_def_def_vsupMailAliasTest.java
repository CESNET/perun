package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;


import java.util.LinkedHashMap;
import java.util.Map;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsKeyVsup;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_vsupMailAliasTest {

	private static urn_perun_user_attribute_def_def_vsupMailAlias classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_vsupMailAlias();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		attributeToCheck = new Attribute();
		reqAttribute = new Attribute();
		User user2 = new User(5, "John", "Doe", "", "", "");

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, usedMailsKeyVsup, usedMailsUrn)).thenReturn(reqAttribute);

		UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
		when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
		when(session.getPerunBl().getUsersManagerBl().getUserById(session, 5)).thenReturn(user2);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckSemanticsWithInvalidLogin() throws Exception {
		System.out.println("testCheckSemanticsWithInvalidLogin()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("john.doe@vsup.cz", "5");
		reqAttribute.setValue(value);
		attributeToCheck.setValue("john.doe@vsup.cz");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCheckSemanticsWithInvalidLogin()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("john.doe@vsup.cz", "5");
		reqAttribute.setValue(value);
		attributeToCheck.setValue("john.doe1@vsup.cz");

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
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("john.doe@vsup.cz");

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}
}
