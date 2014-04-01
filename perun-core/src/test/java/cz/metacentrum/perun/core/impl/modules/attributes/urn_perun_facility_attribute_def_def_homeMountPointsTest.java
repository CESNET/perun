/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
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
	 * Test of checkAttributeValue method, of class urn_perun_facility_attribute_def_def_homeMountPoints.
	 * with all properly set
	 */
	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		ArrayList<String> homeMountPts= new ArrayList<String>();
		homeMountPts.add("/mnt/mymountpoint1");
		homeMountPts.add("/mnt/mymountpoint2");
		attribute.setValue(homeMountPts);

		classInstance.checkAttributeValue(session, new Facility(), attribute);

	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeValueEmptyAttribute()");

		classInstance.checkAttributeValue(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueBadFormat() throws Exception {
		System.out.println("testCheckAttributeValueBadFormat");
		ArrayList<String> homeMountPts= new ArrayList<String>();
		homeMountPts.add("/mnt/mymountpoint/");
		homeMountPts.add("/mnt/mymountpoin@@t2\n");
		attribute.setValue(homeMountPts);

		classInstance.checkAttributeValue(session, new Facility(), attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueNoHomeMountPointsSet() throws Exception {
		System.out.println("testCheckAttributeValueNoHomeMountPointsSet()");

		attribute.setValue(new ArrayList<String>());
		classInstance.checkAttributeValue(session, new Facility(), attribute);
	}
}
