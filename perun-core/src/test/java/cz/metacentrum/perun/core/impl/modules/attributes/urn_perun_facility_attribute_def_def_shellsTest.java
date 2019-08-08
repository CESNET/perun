package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Lukas Pravda  <luky.pravda@gmail.com>
 * @date 21.5.2011 8:26:49
 */
public class urn_perun_facility_attribute_def_def_shellsTest {

	private static urn_perun_facility_attribute_def_def_shells classInstance;
	private static PerunSessionImpl session;
	private static Attribute attribute;
	private static ModulesUtilsBlImpl modulesUtils;

	@Before
	public void setUp() {
		classInstance = new urn_perun_facility_attribute_def_def_shells();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		attribute = new Attribute();
	}

	/**
	 * Test of checkAttributeSemantics method, of class urn_perun_facility_attribute_def_def_shells.
	 * with all properly set
	 */
	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		ArrayList<String> shells = new ArrayList<>();
		shells.add("/bin/bash");
		shells.add("/bin/csh");
		attribute.setValue(shells);

		classInstance.checkAttributeSemantics(session, new Facility(), attribute);

	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeSemanticsEmptyAttribute()");

		classInstance.checkAttributeSemantics(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsNoShellsSet() throws Exception {
		System.out.println("testCheckAttributeSemanticsNoShellsSet()");

		attribute.setValue(new ArrayList<String>());
		classInstance.checkAttributeSemantics(session, new Facility(), attribute);
	}
}
