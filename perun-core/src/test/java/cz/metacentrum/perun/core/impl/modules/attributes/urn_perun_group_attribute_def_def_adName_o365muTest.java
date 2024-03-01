package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_adName_o365muTest {

  ResourcesManagerBl resourcesManagerBl;
  AttributesManagerBl attributesManagerBl;
  private urn_perun_group_attribute_def_def_adName_o365mu classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource1 = new Resource(1, "test", "test", 1);
  private Resource resource2 = new Resource(2, "test", "test", 1);
  private PerunSessionImpl sess;
  private Attribute reqAttribute1;
  private Attribute reqAttribute2;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_adName_o365mu();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);
    reqAttribute1 = new Attribute();
    reqAttribute2 = new Attribute();

    //perunBl
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl())
        .thenReturn(perunBl);

    //managers
    attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl())
        .thenReturn(attributesManagerBl);
    resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(perunBl.getResourcesManagerBl())
        .thenReturn(resourcesManagerBl);

    //specific methods
    when(attributesManagerBl.getAttribute(sess, resource1, AttributesManager.NS_RESOURCE_ATTR_DEF + ":adOuName"))
        .thenReturn(reqAttribute1);
    when(attributesManagerBl.getAttribute(sess, resource2, AttributesManager.NS_RESOURCE_ATTR_DEF + ":adOuName"))
        .thenReturn(reqAttribute2);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongSyntax() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("bad@value");
    when(resourcesManagerBl.getAssignedResources(sess, group))
        .thenReturn(Collections.singletonList(resource1));

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("correctValue");
    when(resourcesManagerBl.getAssignedResources(sess, group))
        .thenReturn(Collections.singletonList(resource1));

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testWrongSemanticsTwoResourcesWithOuSet() throws Exception {
    System.out.println("testSemanticsWithSameADNameAlreadySetElsewhere()");
    attributeToCheck.setValue("correctValue");
    reqAttribute1.setValue("ouTest1");
    reqAttribute2.setValue("ouTest2");

    when(resourcesManagerBl.getAssignedResources(sess, group))
        .thenReturn(Arrays.asList(resource1, resource2));

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("correctValue");
    reqAttribute1.setValue("ouTest1");
    reqAttribute2.setValue(null);

    when(resourcesManagerBl.getAssignedResources(sess, group))
        .thenReturn(Arrays.asList(resource1, resource2));

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }
}
