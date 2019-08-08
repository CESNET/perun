package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Test of user attribute module for timezone.
 *
 * @author Jiří Mauritz <jirmauritz@gmail.com>
 */
public class urn_perun_user_attribute_def_def_timezoneTest {

	private static PerunSessionImpl session;
	private static urn_perun_user_attribute_def_def_timezone classInstance;
	private static User user;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_timezone();
		user = new User();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("Europe/Prague");
		classInstance.checkAttributeSemantics(session, user, attributeToCheck);

		attributeToCheck.setValue("Africa/Johannesburg");
		classInstance.checkAttributeSemantics(session, user, attributeToCheck);

		attributeToCheck.setValue("Jamaica");
		classInstance.checkAttributeSemantics(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeSemanticsWithWrongValue() throws Exception {
			System.out.println("testCheckAttributeSemanticsWithWrongValue()");

			Attribute attributeToCheck = new Attribute();
			attributeToCheck.setValue("123");

			classInstance.checkAttributeSemantics(session, user, attributeToCheck);
		}
}
