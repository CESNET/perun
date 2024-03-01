package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_defaultHomeMountPointTest {

  private urn_perun_resource_attribute_def_def_defaultHomeMountPoint classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_defaultHomeMountPoint();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    sess = mock(PerunSessionImpl.class);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":homeMountPoints")).thenReturn(
        reqAttribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithIncorrectValue() throws Exception {
    System.out.println("testSyntaxWithIncorrectValue()");
    attributeToCheck.setValue("bad_example");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("/example");

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
    attributeToCheck.setValue("/example");
    reqAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithoutNeededValue() throws Exception {
    System.out.println("testSemanticsReqAttributeWithoutNeededValue()");
    attributeToCheck.setValue("/example");
    List<String> value = new ArrayList<>();
    value.add("something");
    reqAttribute.setValue(value);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("/example");
    List<String> value = new ArrayList<>();
    value.add("/example");
    reqAttribute.setValue(value);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}
