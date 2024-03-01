package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class urn_perun_facility_attribute_def_def_m365AllowedLicensesPrioritiesTest {
  private static urn_perun_facility_attribute_def_def_m365AllowedLicensesPriorities classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_facility_attribute_def_def_m365AllowedLicensesPriorities();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    facility = new Facility();
    attributeToCheck = new Attribute();
    attributeToCheck.setFriendlyName("m365AllowedLicensesPriorities");
  }

  // SYNTAX CHECKS

  @Test
  public void testAttributeSyntaxValidMap() throws Exception {
    System.out.println("testAttributeSyntaxValidMap()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    map.put("2", "A2");
    attributeToCheck.setValue(map);
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testAttributeSyntaxNullKey() throws Exception {
    System.out.println("testAttributeSyntaxNullKey()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put(null, "A1");
    attributeToCheck.setValue(map);
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testAttributeSyntaxNullMapValue() throws Exception {
    System.out.println("testAttributeSyntaxNullMapValue()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    map.put("3", null);
    attributeToCheck.setValue(map);
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxNullValue() throws Exception {
    System.out.println("testAttributeSyntaxNullValue()");

    attributeToCheck.setValue(null);
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  // SEMANTICS CHECKS

  @Test
  public void testCheckAttributeSemanticsAllLicensesMatch() throws Exception {
    System.out.println("testCheckAttributeSemanticsAllLicensesMatch()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    map.put("2", "A2");
    attributeToCheck.setValue(map);

    // Mock two resources with matching licences
    List<Resource> resources = Arrays.asList(mock(Resource.class), mock(Resource.class));
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any())).thenReturn(resources);

    Attribute res1License = new Attribute();
    Attribute res2License = new Attribute();
    res1License.setValue("A1");
    res2License.setValue("A2");
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(0)), any())).thenReturn(
        res1License);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(1)), any())).thenReturn(
        res2License);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }


  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsLicenseMismatchSingleLicense() throws Exception {
    System.out.println("testCheckAttributeSemanticsLicenseMismatchSingleLicense()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    attributeToCheck.setValue(map);

    // Mock a resource with mismatched license
    List<Resource> resources = Collections.singletonList(mock(Resource.class));
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any())).thenReturn(resources);

    Attribute resLicense = new Attribute();
    resLicense.setValue("A2");
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(0)), any())).thenReturn(
        resLicense);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsSingleMismatchedLicense() throws Exception {
    System.out.println("testCheckAttributeSemanticsSingleMismatchedLicense()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    map.put("2", "A2");
    map.put("3", "A3");
    attributeToCheck.setValue(map);

    // Mock multiple resources with one of them having a mismatched license
    Resource res1 = mock(Resource.class);
    Resource res2 = mock(Resource.class);
    Resource res3 = mock(Resource.class);

    List<Resource> resources = Arrays.asList(res1, res2, res3);
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any())).thenReturn(resources);

    Attribute res1License = new Attribute();
    Attribute res2License = new Attribute();
    Attribute res3License = new Attribute();
    res1License.setValue("A2");
    res1License.setValue("A1");
    res1License.setValue("B42");
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(0)), any())).thenReturn(
        res1License);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(1)), any())).thenReturn(
        res2License);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(2)), any())).thenReturn(
        res3License);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsNoLicensesNoResources() throws Exception {
    System.out.println("testCheckAttributeSemanticsNoLicensesNoResources()");

    attributeToCheck.setValue(null);
    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsNoLicensesResourcesExist() throws Exception {
    System.out.println("testCheckAttributeSemanticsNoLicensesResourcesExist()");

    attributeToCheck.setValue(null);

    // Mock a resource, its license does not matter
    List<Resource> resources = Collections.singletonList(mock(Resource.class));
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any())).thenReturn(resources);

    Attribute resLicense = new Attribute();
    resLicense.setValue("A1");
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resources.get(0)), any())).thenReturn(
        resLicense);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = InternalErrorException.class)
  public void testCheckAttributeSemanticsExceptionInFetchingResources() throws Exception {
    System.out.println("testCheckAttributeSemanticsExceptionInFetchingResources()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    attributeToCheck.setValue(map);

    // Throw an exception when fetching resources
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any()))
        .thenThrow(new InternalErrorException(""));

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testExceptionInFetchingLicenseForResource() throws Exception {
    System.out.println("testExceptionInFetchingLicenseForResource()");

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("1", "A1");
    attributeToCheck.setValue(map);

    // Mock a resource
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);
    when(session.getPerunBl().getFacilitiesManagerBl().getAssignedResources(any(), any())).thenReturn(resources);

    // Throw an exception when fetching a license for the resource
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(), eq(resource), any()))
        .thenThrow(new AttributeNotExistsException(""));

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

}
