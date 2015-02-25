package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Michal Šťava <stava.michal@gmail.com>
 * @date 12.04.2012 14:35:00
 */
public class urn_perun_member_attribute_def_def_membershipExpirationTest {

	private static urn_perun_member_attribute_def_def_membershipExpiration classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;

	@Before
	public void SetUp() {
		classInstance = new urn_perun_member_attribute_def_def_membershipExpiration();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		attributeToCheck = new Attribute();
	}

	@Test
	public void testCheckAttributeReturnNull() throws Exception {
		System.out.println("testCheckAttriubuteReturnNull()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeValue(session, new Member(), attributeToCheck);
	}

	@Test
	public void testCheckAttributeCommonValue() throws Exception {
		System.out.println("testCheckAttributeCommonValue()");
		attributeToCheck.setValue("2001-12-25");

		classInstance.checkAttributeValue(session, new Member(), attributeToCheck);
	}

	@Test
	public void testCheckAttributeLowBorderValue() throws Exception {
		System.out.println("testCheckAttributeLowBorderValue()");
		attributeToCheck.setValue("1000-01-01");

		classInstance.checkAttributeValue(session, new Member(), attributeToCheck);
	}

	@Test
	public void testCheckAttributeHighBorderValue() throws Exception {
		System.out.println("testCheckAttributeHighBorderValue()");
		attributeToCheck.setValue("9999-12-31");

		classInstance.checkAttributeValue(session, new Member(), attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongMonths() throws Exception {
			System.out.println("testCheckAttributeWrongMonth()");
			attributeToCheck.setValue("1500-15-25");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongYears() throws Exception {
			System.out.println("testCheckAttributeWrongYear()");
			attributeToCheck.setValue("500-10-25");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongDays() throws Exception {
			System.out.println("testCheckAttributeWrongDay()");
			attributeToCheck.setValue("1500-10-32");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongMonthWithBadDaysValueTime() throws Exception {
			System.out.println("testCheckAttributeWrongMonthWithBadDaysValueTime()");
			attributeToCheck.setValue("3595-11-31");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongCharInDate() throws Exception {
			System.out.println("testCheckAttributeWrongCharsInDate()");
			attributeToCheck.setValue("3595-11-31s");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeWrongCharsBetweenDate() throws Exception {
			System.out.println("testCheckAttributeWrongCharsBetweenDate()");
			attributeToCheck.setValue("3595.11.31");
			classInstance.checkAttributeValue(session, new Member(), attributeToCheck);

		}

	@Test
	public void testFillAttributeValue() throws Exception {
		System.out.println("testCheckAttriubuteReturnNull()");
		Attribute attribute = classInstance.fillAttribute(session, new Member(), classInstance.getAttributeDefinition());
		assertNull("Test", attribute.getValue());
		classInstance.checkAttributeValue(session, new Member(), attribute);
	}
}
