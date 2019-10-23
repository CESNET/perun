package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_osbIddc2Test {

	private static urn_perun_user_attribute_def_def_osbIddc2 classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_osbIddc2();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeWithNullValue() throws Exception {
		System.out.println("testCheckAttributeWithNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test
	public void testCheckCorrectSemantics() throws Exception {
		System.out.println("testCheckCorrectSemantics()");
		attributeToCheck.setValue("my_example");

		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}
}
