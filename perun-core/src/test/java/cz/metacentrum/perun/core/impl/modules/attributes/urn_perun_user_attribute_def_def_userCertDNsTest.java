package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_userCertDNsTest {

	private static urn_perun_user_attribute_def_def_userCertDNs classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_userCertDNs();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithIncorrectKeyValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithIncorrectKeyValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("bad_example", "/example");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithIncorrectValueValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithIncorrectValueValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("/example", "");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test
	public void testCheckAttributeSyntaxCorrect() throws Exception {
		System.out.println("testCheckAttributeSyntaxCorrect()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("/example", "/example");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeSemanticsWithNullValue() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCheckAttributeSemanticsCorrect() throws Exception {
		System.out.println("testCheckAttributeSemanticsCorrect()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("/example", "/example");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
