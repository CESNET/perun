/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 9.6.2011
 */
public class urn_perun_user_facility_attribute_def_def_homeMountPointTest {

	private static PerunSessionImpl session;
	private static urn_perun_user_facility_attribute_def_def_homeMountPoint classInstance;
	private static Attribute listOfMntPts;
	private static User user;
	private static Facility facility;
	private static Resource resource;
	private static Resource resource1;

	@Before
	public void SetUp() {
		listOfMntPts = new Attribute();
		classInstance = new urn_perun_user_facility_attribute_def_def_homeMountPoint();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		ArrayList<String> mntPts = new ArrayList<String>();
		mntPts.add("/mnt/mnt1");
		mntPts.add("/tmp/mnt2");
		listOfMntPts.setValue(mntPts);

		user = new User();
		facility = new Facility();
		resource = new Resource();
		resource.setName("myResource");
		resource.setDescription("desc");

		resource1 = new Resource();
		resource1.setId(1);
		resource1.setName("myResource");
		resource1.setDescription("desc");
	}

	/**
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_homeMountPoint.
	 * with all parameters properly set.
	 */
	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/mnt/mnt1");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

			{
				add(resource);
			}
		});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

			{
				add(resource);
			}
		});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

		classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
	}

	/**
	 * Test of fillAttribute method, of class urn_perun_user_facility_attribute_def_def_homeMountPoint.
	 * with all parameters properly set.
	 */
	@Test
	public void testFillAttribute() throws Exception {
		System.out.println("testFillAttribute()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

			{
				add(resource);
			}
		});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

			{
				add(resource);
			}
		});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

		Attribute filledAttribute = classInstance.fillAttribute(session, facility, user, new AttributeDefinition());
		assertTrue("A different homeMountPoint was filled than those available", ( listOfMntPts.getValue()).equals(filledAttribute.getValue()));
	}

	/**
	 * Test of fillAttribute method, of class urn_perun_user_facility_attribute_def_def_homeMountPoint.
	 * with user who does not have an access at specified resource.
	 */
	@Test
	public void testFillAttributeOfUnknownUser() throws Exception {
		System.out.println("testFillAttributeOfUnknownUser()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>());
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

		Attribute atr = classInstance.fillAttribute(session, facility, user, new AttributeDefinition());

		assertNull("User's homeMountPoint was filled even they don't have an account there.", atr.getValue());
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
		public void testCheckAttributeValueOfUnknownUser() throws Exception {
			System.out.println("testCheckAttributeValueOfUnknownUser()");

			when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>());
			when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

			Attribute atr = new Attribute();
			atr.setValue(("/mnt/mnt1"));

			classInstance.checkAttributeValue(session, facility, user, atr);

		}

	/**
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_homeMountPoint.
	 * with empty attribute.
	 */
	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeValueWithEmptyAttribute() throws Exception {
			System.out.println("testCheckAttributeValueWithEmptyAttribute()");

			when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

			classInstance.checkAttributeValue(session, facility, user, new Attribute());
			fail("Empty attribute should have thrown an exception");

		}

	/**
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_homeMountPoint.
	 * with homeMountPoint containing forbiden character.
	 */
	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeValueWronghomeMountPointFormat() throws Exception {
			System.out.println("testCheckAttributeValueWronghomeMountPointFormat()");

			Attribute attributeToCheck = new Attribute();
			attributeToCheck.setValue("/bin/\n/bash");

			when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

			classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
			fail("Wrong homeMountPoint format should have thrown an exception");
		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeValueWronghomeMountPointFormatInvalidCharacter() throws Exception {
			System.out.println("testCheckAttributeValueWronghomeMountPointFormatInvalidCharacter()");

			Attribute attributeToCheck = new Attribute();
			attributeToCheck.setValue("/ok/(&^%");

			when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

			classInstance.checkAttributeValue(session, facility, user, attributeToCheck) ;
			fail("Wrong homeMountPoint format should have thrown an exception");
		}

	@Test(expected = WrongAttributeValueException.class)
		public void testCheckAttributeValueWronghomeMountPointFormathomeMountPointIsDirectory() throws Exception {
			System.out.println("testCheckAttributeValueWronghomeMountPointFormathomeMountPointIsDirectory()");

			when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>() {

				{
					add(resource);
				}
			});
			when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfMntPts);

			Attribute attributeToCheck = new Attribute();
			attributeToCheck.setValue("/mnt/mnt1/");

			classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
			fail("Wrong homeMountPoint format should have thrown an exception");
		}
}
