package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_resource_attribute_def_def_m365GroupNameTest {
	private static urn_perun_resource_attribute_def_def_m365GroupName classInstance;
	private static PerunSessionImpl session;
	private static Resource resource;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_resource_attribute_def_def_m365GroupName();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		resource = new Resource();
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("m365GroupName");
	}

	// SYNTAX VALID

	@Test
	public void testAttributeSyntaxValid() throws Exception {
		System.out.println("testAttributeSyntaxValid()");

		attributeToCheck.setValue("PlainGroupName123");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxValidEmpty() throws Exception {
		System.out.println("testAttributeSyntaxValidEmpty()");

		attributeToCheck.setValue("");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxValidLengthLimit() throws Exception {
		System.out.println("testAttributeSyntaxValidLengthLimit()");

		attributeToCheck.setValue("!#$%&'*+-./0123456789=?ABCDEFGHIJKLMNOPQRSTUVWXYZ^_`abcdefghijkl");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxValidRestOfCharacters() throws Exception {
		System.out.println("testAttributeSyntaxValidRestOfCharacters()");

		// Together with testAttributeSyntaxValidLengthLimit(), these should contain all valid printable characters
		attributeToCheck.setValue("mnopqrstuvwxyz{|}~");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxNull() throws Exception {
		System.out.println("testAttributeSyntaxNull()");

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	// SYNTAX INVALID

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidTooLong() throws Exception {
		System.out.println("testAttributeSyntaxInvalidTooLong()");

		String groupName65chars = "12345678901234567890123456789012345678901234567890123456789012345";

		attributeToCheck.setValue(groupName65chars);
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidSpace() throws Exception {
		System.out.println("testAttributeSyntaxInvalidSpaces()");

		attributeToCheck.setValue("ThisGroupNameContains Space");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidAt() throws Exception {
		System.out.println("testAttributeSyntaxInvalidAt()");

		attributeToCheck.setValue("ThisGroupNameContains@");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidLeftParenthesis() throws Exception {
		System.out.println("testAttributeSyntaxInvalidLeftParenthesis()");

		attributeToCheck.setValue("ThisGroupNameContains(");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidRightParenthesis() throws Exception {
		System.out.println("testAttributeSyntaxInvalidRightParenthesis()");

		attributeToCheck.setValue("ThisGroupNameContains)");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidBackslash() throws Exception {
		System.out.println("testAttributeSyntaxInvalidBackslash()");

		attributeToCheck.setValue("ThisGroupNameContains\\");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidLeftBracket() throws Exception {
		System.out.println("testAttributeSyntaxInvalidLeftBracket()");

		attributeToCheck.setValue("ThisGroupNameContains[");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidRightBracket() throws Exception {
		System.out.println("testAttributeSyntaxInvalidRightBracket()");

		attributeToCheck.setValue("ThisGroupNameContains]");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidQuotationMarks() throws Exception {
		System.out.println("testAttributeSyntaxInvalidQuotationMarks()");

		attributeToCheck.setValue("ThisGroupNameContains\"");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidSemicolon() throws Exception {
		System.out.println("testAttributeSyntaxInvalidSemicolon()");

		attributeToCheck.setValue("ThisGroupNameContains;");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidColon() throws Exception {
		System.out.println("testAttributeSyntaxInvalidColon()");

		attributeToCheck.setValue("ThisGroupNameContains:");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidLessThan() throws Exception {
		System.out.println("testAttributeSyntaxInvalidLessThan()");

		attributeToCheck.setValue("ThisGroupNameContains<");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidGreaterThan() throws Exception {
		System.out.println("testAttributeSyntaxInvalidGreaterThan()");

		attributeToCheck.setValue("ThisGroupNameContains>");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidComma() throws Exception {
		System.out.println("testAttributeSyntaxInvalidComma()");

		attributeToCheck.setValue("ThisGroupNameContains,");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidSpecial() throws Exception {
		System.out.println("testAttributeSyntaxInvalidSpecial()");

		attributeToCheck.setValue("This-is-the-Ř-group");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testAttributeSyntaxInvalidMultiple() throws Exception {
		System.out.println("testAttributeSyntaxInvalidMultiple()");

		attributeToCheck.setValue("There!Are@Many[Problems]With<This>(name)");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	// SEMANTICS

	@Test
	public void testAttributeSemanticsValid() throws Exception {
		System.out.println("testAttributeSemanticsValid()");

		attributeToCheck.setValue("");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test (expected = WrongReferenceAttributeValueException.class)
	public void testAttributeSemanticsInvalid() throws Exception {
		System.out.println("testAttributeSemanticsInvalid()");

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}
}
