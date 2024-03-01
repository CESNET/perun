package cz.metacentrum.perun.core.impl.modules.attributes;

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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_fairshareGroupNameTest {

  private urn_perun_resource_attribute_def_def_fairshareGroupName classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_fairshareGroupName();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    sess = mock(PerunSessionImpl.class);
    Facility facility = new Facility();
    List<Resource> resources = new ArrayList<>();
    Resource resource2 = new Resource();
    resources.add(resource2);
    resources.add(resource);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource2, attributeToCheck.getName())).thenReturn(reqAttribute);

    ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(perunBl.getResourcesManagerBl()).thenReturn(resourcesManagerBl);
    when(resourcesManagerBl.getFacility(sess, resource)).thenReturn(facility);

    FacilitiesManagerBl facilitiesManagerBl = mock(FacilitiesManagerBl.class);
    when(perunBl.getFacilitiesManagerBl()).thenReturn(facilitiesManagerBl);
    when(facilitiesManagerBl.getAssignedResources(sess, facility)).thenReturn(resources);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongValue() throws Exception {
    System.out.println("testSyntaxWithWrongValue()");
    attributeToCheck.setValue("bad_example");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("example");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNameAlreadyTaken() throws Exception {
    System.out.println("testSemanticsWithNameAlreadyTaken()");
    attributeToCheck.setValue("example");
    reqAttribute.setValue("example");

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("example");
    reqAttribute.setValue("anotherValue");

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}
