package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_resource_attribute_def_def_projectOwnerLoginTest {

  private urn_perun_group_resource_attribute_def_def_projectOwnerLogin classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute namespaceAttribute;
  private Attribute loginAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_projectOwnerLogin();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    Facility facility = new Facility();
    namespaceAttribute = new Attribute();
    namespaceAttribute.setValue("namespace");
    loginAttribute = new Attribute();

    User user = new User();

    when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource)).thenReturn(facility);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace")).thenReturn(
        namespaceAttribute);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:namespace")).thenReturn(
        loginAttribute);
    when(sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, loginAttribute)).thenReturn(
        Collections.singletonList(user));
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("correct_value");
    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("correct_value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsNoUserWithGivenName() throws Exception {
    System.out.println("testSemanticsNoUserWithGivenName()");
    attributeToCheck.setValue("correct_value");
    namespaceAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("@value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }
}
