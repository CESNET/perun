package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_resource_attribute_def_def_projectNameTest {

  private urn_perun_group_resource_attribute_def_def_projectName classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_projectName();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    Facility facility = new Facility();
    reqAttribute = new Attribute();

    Resource resource2 = new Resource();
    List<Resource> resources = new ArrayList<>();
    resources.add(resource2);
    resources.add(resource);

    Group group2 = new Group();
    List<Group> groups = new ArrayList<>();
    groups.add(group);
    groups.add(group2);

    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":projectsBasePath")).thenReturn(
        reqAttribute);
    when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource)).thenReturn(facility);
    when(sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility)).thenReturn(resources);
    when(sess.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource2)).thenReturn(groups);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource2, group2,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":projectName")).thenReturn(reqAttribute);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("correct_value");
    reqAttribute.setValue("another_correct_value");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("correct_value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsMultipleGroupsWithSameName() throws Exception {
    System.out.println("testSemanticsMultipleGroupsWithSameName()");
    attributeToCheck.setValue("correct_value");
    reqAttribute.setValue("correct_value");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsReqAttributeWithNullValue() throws Exception {
    System.out.println("testSemanticsReqAttributeWithNullValue()");
    attributeToCheck.setValue("correct_value");
    reqAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("bad@value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }
}
