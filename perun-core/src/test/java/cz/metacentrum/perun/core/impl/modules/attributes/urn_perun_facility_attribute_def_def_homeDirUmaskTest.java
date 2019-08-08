package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Test of unix permission mask attribute.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_homeDirUmaskTest {

	private static PerunSessionImpl session;
	private static Facility facility;
	private static urn_perun_facility_attribute_def_def_homeDirUmask classInstance;

	@Before
	public void setUp() {
		classInstance = new urn_perun_facility_attribute_def_def_homeDirUmask();
		facility = new Facility();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);

		attributeToCheck.setValue("0542");
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);

		attributeToCheck.setValue("215");
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);

		attributeToCheck.setValue("0521");
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithWrongValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("5891");
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWithWrongValueLength() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithWrongValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue("12");
		classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
	}
}