package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class urn_perun_vo_attribute_def_def_applicationAffiliationRegexTest {
	private urn_perun_vo_attribute_def_def_applicationAffiliationRegex classInstance;
	private Attribute attributeToCheck;
	private PerunSessionImpl session;
	private Vo vo;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_vo_attribute_def_def_applicationAffiliationRegex();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		session = mock(PerunSessionImpl.class);
		vo = mock(Vo.class);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckNull() throws Exception {
		System.out.println("testCheckNull()");
		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void checkInvalidSyntax() throws Exception {
		System.out.println("checkInvalidSyntax()");
		attributeToCheck.setValue(new ArrayList<>(List.of("^[a-zA-Z0-9._%+-+@cesnet.cz$")));
		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue(new ArrayList<>(List.of("^[a-zA-Z0-9._%+-]+@cesnet.cz$")));
		classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue(new ArrayList<>(List.of("^[a-zA-Z0-9._%+-]+@cesnet.cz$")));
		classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
	}
}
