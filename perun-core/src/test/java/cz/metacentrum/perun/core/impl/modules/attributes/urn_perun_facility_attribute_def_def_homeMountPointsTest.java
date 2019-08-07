/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 9.6.2011
 */
public class urn_perun_facility_attribute_def_def_homeMountPointsTest {
	private static urn_perun_facility_attribute_def_def_homeMountPoints classInstance;
	private static PerunSessionImpl session;
	private static Attribute attribute;

	@Before
	public void setUp() {
		classInstance = new urn_perun_facility_attribute_def_def_homeMountPoints();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		attribute = new Attribute();
	}

	/**
	 * Test of checkAttributeSemantics method, of class urn_perun_facility_attribute_def_def_homeMountPoints.
	 * with all properly set
	 */
	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		ArrayList<String> homeMountPts= new ArrayList<>();
		homeMountPts.add("/mnt/mymountpoint1");
		homeMountPts.add("/mnt/mymountpoint2");
		attribute.setValue(homeMountPts);

		classInstance.checkAttributeSemantics(session, new Facility(), attribute);

	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeSemanticsEmptyAttribute()");

		classInstance.checkAttributeSemantics(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsBadFormat() throws Exception {
		System.out.println("testCheckAttributeSemanticsBadFormat");
		ArrayList<String> homeMountPts= new ArrayList<>();
		homeMountPts.add("/mnt/mymountpoint/");
		homeMountPts.add("/mnt/mymountpoin@@t2\n");
		attribute.setValue(homeMountPts);

		classInstance.checkAttributeSemantics(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsNoHomeMountPointsSet() throws Exception {
		System.out.println("testCheckAttributeSemanticsNoHomeMountPointsSet()");

		attribute.setValue(new ArrayList<String>());
		classInstance.checkAttributeSemantics(session, new Facility(), attribute);
	}
}
