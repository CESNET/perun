package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_resource_attribute_def_def_accountExpirationTimeTest {

  private urn_perun_resource_attribute_def_def_accountExpirationTime classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_accountExpirationTime();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    Facility facility = new Facility();

    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR + ":accountExpirationTime")).thenReturn(
        reqAttribute);
    when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource)).thenReturn(facility);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue(4);
    reqAttribute.setValue(5);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithLesserValue() throws Exception {
    System.out.println("testSemanticsReqAttributeWithLesserValue()");
    attributeToCheck.setValue(6);
    reqAttribute.setValue(5);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithNullValue() throws Exception {
    System.out.println("testSemanticsReqAttributeWithNullValue()");
    reqAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}
