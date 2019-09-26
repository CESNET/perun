package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_vo_attribute_def_def_contactEmailTest {

	private static urn_perun_vo_attribute_def_def_contactEmail classInstance;
	private static PerunSessionImpl session;
	private static Attribute attributeToCheck;
	private static Vo vo;
	private static String correctEmail = "my@example.com";
	private static String incorrectEmail = "myBadExample.com";

	@Before
	public void setUp() {
		classInstance = new urn_perun_vo_attribute_def_def_contactEmail();
		session = mock(PerunSessionImpl.class);
		attributeToCheck = new Attribute();
		vo = new Vo();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
		when(session.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(session, correctEmail)).thenReturn(true);
		when(session.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(session, incorrectEmail)).thenReturn(false);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckNullValue() throws Exception {
		System.out.println("testCheckNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckValueWithIncorrectEmail() throws Exception {
		System.out.println("testCheckValueWithIncorrectEmail()");
		List<String> value = new ArrayList<>();
		value.add(incorrectEmail);
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		List<String> value = new ArrayList<>();
		value.add(correctEmail);
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		List<String> value = new ArrayList<>();
		value.add(correctEmail);
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
	}
}
