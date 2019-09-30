package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_userPreferredCertDNTest {

	private static urn_perun_user_attribute_def_def_userPreferredCertDN classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;
	private static Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_userPreferredCertDN();
		session = mock(PerunSessionImpl.class);
		user = new User(0, "John", "Doe", "", "", "");
		attributeToCheck = new Attribute();
		reqAttribute = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(session.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertDNs")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithPreferredValueButNoCertificates() throws Exception {
		System.out.println("testCheckWithPreferredValueButNoCertificates()");
		reqAttribute.setValue(null);
		attributeToCheck.setValue("example");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithNullPreferredValue() throws Exception {
		System.out.println("testCheckWithNullPreferredValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("example", "example");
		reqAttribute.setValue(value);
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithInvalidPreferredValue() throws Exception {
		System.out.println("testCheckWithInvalidPreferredValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("example", "example");
		reqAttribute.setValue(value);
		attributeToCheck.setValue("bad_example");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("example", "example");
		reqAttribute.setValue(value);
		attributeToCheck.setValue("example");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
