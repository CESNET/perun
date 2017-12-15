package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Set of tests for class urn_perun_user_facility_attribute_def_def_shell
 *
 * @author Lukas Pravda  <luky.pravda@gmail.com>
 * @date 20.5.2011 17:00:49
 */
public class urn_perun_user_facility_attribute_def_def_shellTest {

	private static urn_perun_user_facility_attribute_def_def_shell classInstance;
	private static PerunSessionImpl session;
	private static Attribute listOfShells;
	private static User user;
	private static Facility facility;
	private static Resource resource;
	private static Resource resource1;
	private static Attribute userPreferredShell;

	@Before
	public void setUp() {
		listOfShells = new Attribute();
		classInstance = new urn_perun_user_facility_attribute_def_def_shell();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		ArrayList<String> shells = new ArrayList<String>();
		shells.add("/bin/bash");
		shells.add("/bin/csh");
		listOfShells.setValue(shells);

		userPreferredShell = new Attribute();
		userPreferredShell.setValue("/bin/csh");

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
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with all parameters properly set.
	 */
	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/bash");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells"))).thenReturn(listOfShells);

		classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
	}



	@Test(expected=WrongReferenceAttributeValueException.class)
	public void testCheckAttributeValueOfUnknownUser() throws Exception{
		System.out.println("testCheckAttributeValueOfUnknownUser()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>());
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		Attribute atr = new Attribute();
		atr.setValue(("/bin/bash"));

		classInstance.checkAttributeValue(session, facility, user, atr);

	}

	/**
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with empty attribute.
	 */
	public void testCheckAttributeValueWithEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeValueWithEmptyAttribute()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeValue(session, facility, user, new Attribute());
	}

	/**
	 * Test of checkAttributeValue method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with shell containing forbiden character.
	 */
	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueWrongShellFormat() throws Exception {
		System.out.println("testCheckAttributeValueWrongShellFormat()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/\n/bash");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueWrongShellFormatInvalidCharacter() throws Exception {
		System.out.println("testCheckAttributeValueWrongShellFormatInvalidCharacter()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeValueWrongShellFormatShellIsDirectory() throws Exception {
		System.out.println("testCheckAttributeValueWrongShellFormatShellIsDirectory()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/bash/");

		classInstance.checkAttributeValue(session, facility, user, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}
}
