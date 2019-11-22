package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class urn_perun_member_resource_attribute_def_def_filesLimitTest {

	private static urn_perun_member_resource_attribute_def_def_filesLimit classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Member member;
	private static Resource resource;
	private static Attribute reqAttribute;
	private static Attribute reqAttribute2;

	@Before
	public void SetUp() throws Exception {
		classInstance = new urn_perun_member_resource_attribute_def_def_filesLimit();
		attributeToCheck = new Attribute();
		member = new Member();
		resource = new Resource();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		reqAttribute = new Attribute();
		reqAttribute2 = new Attribute();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, member, resource, AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":filesQuota")).thenReturn(reqAttribute2);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesLimit")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeNegativeValue() throws Exception {
		System.out.println("testCheckAttributeNegativeValue()");
		attributeToCheck.setValue(-1);

		classInstance.checkAttributeSyntax(session, member, resource, attributeToCheck);
	}

	@Test
	public void testCheckAttributeValueCorrectSyntax() throws Exception {
		System.out.println("testCheckAttributeValueCorrectSyntax()");
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSyntax(session, member, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeNullValueWithNegativeDefaultValue() throws Exception {
		System.out.println("testCheckAttributeNullValueWithNegativeDefaultValue()");
		attributeToCheck.setValue(null);
		reqAttribute.setValue(-1);

		classInstance.checkAttributeSemantics(session, member, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeValueWithNullQuotaButPositiveLimit() throws Exception {
		System.out.println("testCheckAttributeValueWithNullQuotaButPositiveLimit()");
		attributeToCheck.setValue(5);
		reqAttribute.setValue(1);
		reqAttribute2.setValue(null);

		classInstance.checkAttributeSemantics(session, member, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckAttributeValueWithQuotaHigherThanLimit() throws Exception {
		System.out.println("testCheckAttributeValueWithQuotaHigherThanLimit()");
		attributeToCheck.setValue(1);
		reqAttribute.setValue(1);
		reqAttribute2.setValue(3);

		classInstance.checkAttributeSemantics(session, member, resource, attributeToCheck);
	}

	@Test
	public void testCheckAttributeValueCorrectSemantics() throws Exception {
		System.out.println("testCheckAttributeValueCorrectSemantics()");
		attributeToCheck.setValue(5);
		reqAttribute.setValue(1);
		reqAttribute2.setValue(3);

		classInstance.checkAttributeSemantics(session, member, resource, attributeToCheck);
	}
}
