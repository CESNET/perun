package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

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
	 * Test of checkAttributeValue method, of class urn_perun_facility_attribute_def_def_shells.
	 * with all properly set
	 */
	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		ArrayList<String> shells = new ArrayList<String>();
		shells.add("/bin/bash");
		shells.add("/bin/csh");
		attribute.setValue(shells);

		classInstance.checkAttributeValue(session, new Facility(), attribute);

	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeValueEmptyAttribute()");

		classInstance.checkAttributeValue(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueNoShellsSet() throws Exception {
		System.out.println("testCheckAttributeValueNoShellsSet()");

		attribute.setValue(new ArrayList<String>());
		classInstance.checkAttributeValue(session, new Facility(), attribute);
	}
}
