package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_homeMountPointsTest {

  private urn_perun_resource_attribute_def_def_homeMountPoints classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_homeMountPoints();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    sess = mock(PerunSessionImpl.class);
    Facility facility = new Facility();

    List<String> value = new ArrayList<>();
    value.add("/example");
    attributeToCheck.setValue(value);
    reqAttribute.setValue(value);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":homeMountPoints")).thenReturn(
        reqAttribute);

    ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(perunBl.getResourcesManagerBl()).thenReturn(resourcesManagerBl);
    when(resourcesManagerBl.getFacility(sess, resource)).thenReturn(facility);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongValue() throws Exception {
    System.out.println("testSyntaxWithWrongValue()");
    List<String> badValue = new ArrayList<>();
    badValue.add("bad_example");
    attributeToCheck.setValue(badValue);

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithNullValue() throws Exception {
    System.out.println("testSemanticsReqAttributeWithNullValue()");
    reqAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithDifferentValues() throws Exception {
    System.out.println("testSemanticsReqAttributeWithNullValue()");
    List<String> badValue = new ArrayList<>();
    badValue.add("bad_example");
    reqAttribute.setValue(badValue);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}
