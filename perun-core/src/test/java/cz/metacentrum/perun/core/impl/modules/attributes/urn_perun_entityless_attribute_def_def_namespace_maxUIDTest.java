package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_entityless_attribute_def_def_namespace_maxUIDTest {

	private static urn_perun_entityless_attribute_def_def_namespace_maxUID classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Attribute reqAttribute;
	private static String key = "key";

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_entityless_attribute_def_def_namespace_maxUID();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		reqAttribute = new Attribute(classInstance.getAttributeDefinition());

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, key, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minUID")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testValueLesserThan1() throws Exception {
		System.out.println("testValueLesserThan1()");
		attributeToCheck.setValue(0);

		classInstance.checkAttributeSyntax(session, key, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testValueLesserThan1()");
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSyntax(session, key, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testValueLesserThanMinUID() throws Exception {
		System.out.println("testValueLesserThanMinUID()");
		attributeToCheck.setValue(1);
		reqAttribute.setValue(2);

		classInstance.checkAttributeSemantics(session, key, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue(2);
		reqAttribute.setValue(1);

		classInstance.checkAttributeSemantics(session, key, attributeToCheck);
	}
}
