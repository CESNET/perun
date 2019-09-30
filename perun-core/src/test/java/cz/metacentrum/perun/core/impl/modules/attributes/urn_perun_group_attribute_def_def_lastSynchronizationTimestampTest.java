package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_group_attribute_def_def_lastSynchronizationTimestampTest {

	private urn_perun_group_attribute_def_def_lastSynchronizationTimestamp classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_lastSynchronizationTimestamp();
		sess = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
	}

	@Test
	public void testCheckAttributeReturnNull() throws Exception {
		System.out.println("testCheckAttriubuteReturnNull()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test
	public void testCheckCorrectSyntax() throws Exception {
		System.out.println("testCheckCorrectSyntax()");
		attributeToCheck.setValue("2001-12-25 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongMonths() throws Exception {
		System.out.println("testCheckAttributeWrongMonth()");
		attributeToCheck.setValue("1500-15-25 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongTime() throws Exception {
		System.out.println("testCheckAttributeWrongYear()");
		attributeToCheck.setValue("500-10-25 25:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongDays() throws Exception {
		System.out.println("testCheckAttributeWrongDay()");
		attributeToCheck.setValue("1500-10-32 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongMonthWithBadDaysValueTime() throws Exception {
		System.out.println("testCheckAttributeWrongMonthWithBadDaysValueTime()");
		attributeToCheck.setValue("3595-11-31 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongCharInDate() throws Exception {
		System.out.println("testCheckAttributeWrongCharsInDate()");
		attributeToCheck.setValue("3595-11-31s 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeWrongCharsBetweenDate() throws Exception {
		System.out.println("testCheckAttributeWrongCharsBetweenDate()");
		attributeToCheck.setValue("3595.11.31 22:22:22.0");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}
}
