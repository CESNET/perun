package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_resource_attribute_def_def_fsScratchDirsTest {
  private static urn_perun_resource_attribute_def_def_fsScratchDirs classInstance;
  private static PerunSessionImpl session;
  private final Resource resource = new Resource();
  private static Attribute attributeToCheck;
  private static Attribute reqAttribute;


  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_fsScratchDirs();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    Facility facility = new Facility();
    List<Resource> resources = new ArrayList<>();
    Resource resource2 = new Resource();
    resources.add(resource);
    resources.add(resource2);

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, resource2, attributeToCheck.getName())).thenReturn(reqAttribute);

    ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(perunBl.getResourcesManagerBl()).thenReturn(resourcesManagerBl);
    when(resourcesManagerBl.getFacility(session, resource)).thenReturn(facility);

    FacilitiesManagerBl facilitiesManagerBl = mock(FacilitiesManagerBl.class);
    when(perunBl.getFacilitiesManagerBl()).thenReturn(facilitiesManagerBl);
    when(facilitiesManagerBl.getAssignedResources(session, facility)).thenReturn(resources);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    attributeToCheck.setValue(scratchDirs);

    Map<String, String> scratchDirsOther = new LinkedHashMap<>();
    scratchDirsOther.put("/mnt/mymountpoint2", "0700");
    reqAttribute.setValue(scratchDirsOther);

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxScratchDoubleSlash() throws Exception {
    System.out.println("testCheckAttributeSyntaxScratchDoubleSlash()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt//mymountpoint1", "0700");
    attributeToCheck.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxScratchTrailingSlash() throws Exception {
    System.out.println("testCheckAttributeSyntaxScratchTrailingSlash()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt/mymountpoint1/", "0700");
    attributeToCheck.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsScratchTaken() throws Exception {
    System.out.println("testCheckAttributeSemanticsScratchTaken()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    attributeToCheck.setValue(scratchDirs);

    Map<String, String> scratchDirsOther = new LinkedHashMap<>();
    scratchDirsOther.put("/mnt/mymountpoint1", "713");
    reqAttribute.setValue(scratchDirsOther);

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsScratchTakenTrailing() throws Exception {
    System.out.println("testCheckAttributeSemanticsScratchTakenTrailing()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1/", "0700");
    attributeToCheck.setValue(scratchDirs);

    Map<String, String> scratchDirsOther = new LinkedHashMap<>();
    scratchDirsOther.put("/mnt/mymountpoint1", "713");
    reqAttribute.setValue(scratchDirsOther);

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }


  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsScratchTakenDouble() throws Exception {
    System.out.println("testCheckAttributeSemanticsScratchTakenDouble()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    attributeToCheck.setValue(scratchDirs);

    Map<String, String> scratchDirsOther = new LinkedHashMap<>();
    scratchDirsOther.put("/mnt//mymountpoint1", "713");
    reqAttribute.setValue(scratchDirsOther);

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsEmptyAttributeCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsEmptyAttributeCorrect()");

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongHomePoint() throws Exception {
    System.out.println("testCheckAttributeSyntaxWrongHomePoint()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1@@s", "0700");
    attributeToCheck.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongPermissions() throws Exception {
    System.out.println("testCheckAttributeSyntaxWrongPermissions()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0x700");
    attributeToCheck.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt/mymountpoint2", "713");
    attributeToCheck.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }
}
