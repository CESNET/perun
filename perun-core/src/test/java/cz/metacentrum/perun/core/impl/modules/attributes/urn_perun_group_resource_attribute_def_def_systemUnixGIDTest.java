package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_resource_attribute_def_def_systemUnixGIDTest {

  private urn_perun_group_resource_attribute_def_def_systemUnixGID classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private Resource resource = new Resource(1, "resource1", "Resource 1", 0);
  private PerunSessionImpl sess;
  private Attribute reqAttribute;
  private Attribute reqAttribute2;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_systemUnixGID();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    Facility facility = new Facility();
    reqAttribute = new Attribute();
    reqAttribute2 = new Attribute();
    Resource resource2 = new Resource();
    Group group2 = new Group();
    Pair<Group, Resource> pair = new Pair<>(group2, resource2);

    when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource2)).thenReturn(facility);
    when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource)).thenReturn(facility);
    when(sess.getPerunBl().getGroupsManagerBl().getGroupResourcePairsByAttribute(sess, attributeToCheck)).thenReturn(
        Collections.singletonList(pair));
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":isSystemUnixGroup")).thenReturn(reqAttribute);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGroupName")).thenReturn(reqAttribute);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource2, group2,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":systemUnixGroupName")).thenReturn(reqAttribute2);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue(0);

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue(1);

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsNullValueWithUnixGroup() throws Exception {
    System.out.println("testSemanticsNullValueWithUnixGroup()");
    attributeToCheck.setValue(null);
    reqAttribute.setValue(1);

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsGIDAlreadyUsed() throws Exception {
    System.out.println("testSemanticsGIDAlreadyUsed()");
    attributeToCheck.setValue(1);
    reqAttribute.setValue("name");
    reqAttribute2.setValue("different_name");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue(1);
    reqAttribute.setValue("same_name");
    reqAttribute2.setValue("same_name");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }
}
