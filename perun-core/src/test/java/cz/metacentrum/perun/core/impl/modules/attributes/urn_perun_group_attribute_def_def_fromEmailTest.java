package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_fromEmailTest {

	private urn_perun_group_attribute_def_def_fromEmail classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private ModulesUtilsBl modulesUtilsBl;
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_fromEmail();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);
		modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testWrongTypeOfValue() throws Exception {
		System.out.println("testWrongTypeOfValue()");
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		String correctValue = "my@example.com";
		when(modulesUtilsBl.isNameOfEmailValid(sess, correctValue)).thenReturn(true);
		String correctValue2 = "\"my example\" <my@example.com>";
		when(modulesUtilsBl.isNameOfEmailValid(sess, correctValue2)).thenReturn(true);

		attributeToCheck.setValue(correctValue);
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);

		attributeToCheck.setValue(correctValue2);
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testNullValue() throws Exception {
		System.out.println("testNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("\"my example\" <my@example.com>");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}
}
