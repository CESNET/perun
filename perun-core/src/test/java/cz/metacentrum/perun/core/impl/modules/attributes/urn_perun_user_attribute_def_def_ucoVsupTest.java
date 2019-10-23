package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_ucoVsupTest {

	private static PerunSessionImpl session;
	private static urn_perun_user_attribute_def_def_ucoVsup classInstance;
	private static User user;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_ucoVsup();
		user = new User();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckCorrectAttributeSemantics() throws Exception {
		System.out.println("testCheckCorrectAttributeSemantics()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("not_null");
		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeSemanticsWithNullValue() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithNullValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
