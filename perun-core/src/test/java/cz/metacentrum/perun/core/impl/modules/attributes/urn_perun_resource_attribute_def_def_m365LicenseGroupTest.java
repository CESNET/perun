package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_m365LicenseGroupTest {
	private static urn_perun_resource_attribute_def_def_m365LicenseGroup classInstance;
	private static PerunSessionImpl session;
	private static Resource resource;
	private static Facility facility;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_resource_attribute_def_def_m365LicenseGroup();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		resource = new Resource();
		facility = new Facility();
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("m365LicenseGroup");
	}

	@Test
	public void testAttributeSyntaxValid() throws Exception {
		System.out.println("testAttributeSyntaxValid()");

		attributeToCheck.setValue("LicenseGroupA");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxValidNull() throws Exception {
		System.out.println("testAttributeSyntaxInvalidNull()");

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSyntaxValidEmpty() throws Exception {
		System.out.println("testAttributeSyntaxInvalidEmpty()");

		attributeToCheck.setValue("");
		classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSemanticsNullLicenseGroup() throws Exception {
		System.out.println("testAttributeSemanticsNullLicenseGroup()");

		attributeToCheck.setValue(null);
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSemanticsEmptyLicenseGroup() throws Exception {
		System.out.println("testAttributeSemanticsEmptyLicenseGroup()");

		attributeToCheck.setValue("");
		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test
	public void testAttributeSemanticsValidLicenseGroup() throws Exception {
		System.out.println("testAttributeSemanticsValidLicenseGroup()");

		attributeToCheck.setValue("LicenseGroupA");
		when(session.getPerunBl().getResourcesManagerBl().getFacility(any(), eq(resource))).thenReturn(facility);

		// Mock allowed licenses for the facility
		Attribute allowedLicensesAttribute = new Attribute();
		LinkedHashMap<String, String> allowedLicensesMap = new LinkedHashMap<>();
		allowedLicensesMap.put("1", "LicenseGroupA");
		allowedLicensesMap.put("2", "LicenseGroupB");
		allowedLicensesAttribute.setValue(allowedLicensesMap);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(facility), any())).thenReturn(allowedLicensesAttribute);

		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testAttributeSemanticsInvalidLicenseGroup() throws Exception {
		System.out.println("testAttributeSemanticsInvalidLicenseGroup()");

		attributeToCheck.setValue("LicenseGroupB");
		when(session.getPerunBl().getResourcesManagerBl().getFacility(any(), eq(resource))).thenReturn(facility);

		// Mock allowed licenses for the facility
		Attribute allowedLicensesAttribute = new Attribute();
		LinkedHashMap<String, String> allowedLicensesMap = new LinkedHashMap<>();
		allowedLicensesMap.put("1", "LicenseGroupA");
		allowedLicensesAttribute.setValue(allowedLicensesMap);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(facility), any())).thenReturn(allowedLicensesAttribute);

		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testAttributeSemanticsExceptionInFetchingLicenses() throws Exception {
		System.out.println("testAttributeSemanticsExceptionInFetchingLicenses()");

		attributeToCheck.setValue("LicenseGroupA");
		when(session.getPerunBl().getResourcesManagerBl().getFacility(any(), eq(resource))).thenReturn(facility);

		// Throw an exception when trying to fetch allowed licenses
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(facility), any())).thenThrow(new AttributeNotExistsException(""));

		classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
	}

}
