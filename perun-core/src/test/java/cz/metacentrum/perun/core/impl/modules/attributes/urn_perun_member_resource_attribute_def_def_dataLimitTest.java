package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_member_resource_attribute_def_def_dataLimitTest {

	private urn_perun_member_resource_attribute_def_def_dataLimit classInstance;
	private Attribute attributeToCheck;
	private Member member = new Member();
	private Resource resource = new Resource();
	private PerunSessionImpl sess;
	private Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_member_resource_attribute_def_def_dataLimit();
		attributeToCheck = new Attribute();
		sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		reqAttribute = new Attribute();

		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, resource, AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":dataQuota")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testSyntaxWrongValue() throws Exception {
		System.out.println("testSyntaxWrongValue()");
		attributeToCheck.setValue("-22F");

		classInstance.checkAttributeSyntax(sess, member, resource, attributeToCheck);
	}

	@Test
	public void testSyntaxCorrect() throws Exception {
		System.out.println("testSyntaxWrongValue()");
		attributeToCheck.setValue("22M");

		classInstance.checkAttributeSyntax(sess, member, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsQuotaHigherThanLimit() throws Exception {
		System.out.println("testSemanticsQuotaHigherThanLimit()");
		attributeToCheck.setValue("22M");
		reqAttribute.setValue("10T");

		classInstance.checkAttributeSemantics(sess, member, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsUnlimitedQuotaWithLimit() throws Exception {
		System.out.println("testSemanticsQuotaHigherThanLimit()");
		attributeToCheck.setValue("22M");
		reqAttribute.setValue("0");

		classInstance.checkAttributeSemantics(sess, member, resource, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("22T");
		reqAttribute.setValue("10M");

		classInstance.checkAttributeSemantics(sess, member, resource, attributeToCheck);
	}
}
