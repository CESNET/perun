package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_login_namespace_eduroam_vsupTest {

	private static urn_perun_user_attribute_def_def_login_namespace_eduroam_vsup classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static Attribute attribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_login_namespace_eduroam_vsup();
		session = mock(PerunSessionImpl.class);
		user = new User();
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("friendly_name");
		attribute = new Attribute();
		attribute.setValue("same_value");

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);

		UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
		when(perunBl.getUsersManagerBl()).thenReturn(usersManagerBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(attributesManagerBl.getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:vsup")).thenReturn(attribute);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCheckAttributeSyntax()");
		String value = "my_example";
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		String value = "admin";
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCheckAttributeSyntax()");
		String value = "same_value";
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		String value = "not_same_value";
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
