package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

		ArrayList<String> shells = new ArrayList<>();
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
	 * Test of checkAttributeSemantics method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with all parameters properly set.
	 */
	@Test
	public void testCheckAttributeSemantics() throws Exception {
		System.out.println("testCheckAttributeSemantics()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/bash");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells"))).thenReturn(listOfShells);

		classInstance.checkAttributeSemantics(session, user, facility, attributeToCheck);
	}



	@Test(expected=WrongReferenceAttributeValueException.class)
	public void testCheckAttributeSemanticsOfUnknownUser() throws Exception{
		System.out.println("testCheckAttributeSemanticsOfUnknownUser()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<>());
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		Attribute atr = new Attribute();
		atr.setValue(("/bin/bash"));

		classInstance.checkAttributeSemantics(session, user, facility, atr);

	}

	/**
	 * Test of checkAttributeSemantics method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with empty attribute.
	 */
	public void testCheckAttributeSemanticsWithEmptyAttribute() throws Exception {
		System.out.println("testCheckAttributeSemanticsWithEmptyAttribute()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeSemantics(session, user, facility, new Attribute());
	}

	/**
	 * Test of checkAttributeSemantics method, of class urn_perun_user_facility_attribute_def_def_shell.
	 * with shell containing forbiden character.
	 */
	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWrongShellFormat() throws Exception {
		System.out.println("testCheckAttributeSemanticsWrongShellFormat()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/\n/bash");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeSemantics(session, user, facility, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWrongShellFormatInvalidCharacter() throws Exception {
		System.out.println("testCheckAttributeSemanticsWrongShellFormatInvalidCharacter()");

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		classInstance.checkAttributeSemantics(session, user, facility, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testCheckAttributeSemanticsWrongShellFormatShellIsDirectory() throws Exception {
		System.out.println("testCheckAttributeSemanticsWrongShellFormatShellIsDirectory()");

		when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSession.class), any(Facility.class), any(User.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(PerunSession.class), any(Facility.class))).thenReturn(new ArrayList<Resource>(){{add(resource);}});
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(listOfShells);

		Attribute attributeToCheck = new Attribute();
		attributeToCheck.setValue("/bin/bash/");

		classInstance.checkAttributeSemantics(session, user, facility, attributeToCheck);
		fail("Wrong shell format should have thrown an exception");
	}
}
