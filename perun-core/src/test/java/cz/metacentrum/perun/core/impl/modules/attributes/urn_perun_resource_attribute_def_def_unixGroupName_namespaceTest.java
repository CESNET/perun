/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Norexan
 */
public class urn_perun_resource_attribute_def_def_unixGroupName_namespaceTest {

  private urn_perun_resource_attribute_def_def_unixGroupName_namespace classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_unixGroupName_namespace();
    attributeToCheck = new Attribute();
    attributeToCheck.setFriendlyName("unixGID-namespace");

    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:")).thenReturn(
        attributeToCheck);

    GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
    when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);

    ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(sess.getPerunBl().getModulesUtilsBl()).thenReturn(modulesUtilsBl);

    ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(sess.getPerunBl().getResourcesManagerBl()).thenReturn(resourcesManagerBl);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("my name");

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testGroupWithSameGID() throws Exception {
    System.out.println("testGroupWithSameGID()");
    attributeToCheck.setValue("my name");
    Group group2 = new Group();
    when(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attributeToCheck)).thenReturn(
        Collections.singletonList(group2));
    when(sess.getPerunBl().getModulesUtilsBl()
        .getListOfGroupGIDsFromListOfResourceGIDs(sess, new ArrayList<>())).thenReturn(
        Collections.singletonList(attributeToCheck));
    when(sess.getPerunBl().getModulesUtilsBl()
        .haveRightToWriteAttributeInAnyGroupOrResource(sess, Collections.singletonList(group2), new ArrayList<>(),
            attributeToCheck, attributeToCheck)).thenReturn(true);
    when(sess.getPerunBl().getModulesUtilsBl()
        .haveTheSameAttributeWithTheSameNamespace(sess, group2, attributeToCheck)).thenReturn(2);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testGroupWithSameGIDAndNoRightsToUseIt() throws Exception {
    System.out.println("testGroupWithSameGIDAndNoRightsToUseIt()");
    attributeToCheck.setValue("my name");
    Group group2 = new Group();
    List<Group> listOfGroups = new ArrayList<>();
    listOfGroups.add(group2);
    when(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attributeToCheck)).thenReturn(listOfGroups);
    when(sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource,
        AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:")).thenReturn(
        Collections.singletonList(attributeToCheck));
    when(sess.getPerunBl().getModulesUtilsBl()
        .haveRightToWriteAttributeInAnyGroupOrResource(sess, listOfGroups, new ArrayList<>(), attributeToCheck,
            attributeToCheck)).thenReturn(false);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testResourceWithSameGID() throws Exception {
    System.out.println("testResourceWithSameGID()");
    attributeToCheck.setValue("my name");
    Resource resource2 = new Resource();
    when(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, attributeToCheck)).thenReturn(
        Arrays.asList(resource, resource2));
    when(sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource,
        AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:")).thenReturn(
        Collections.singletonList(attributeToCheck));
    when(sess.getPerunBl().getModulesUtilsBl()
        .haveRightToWriteAttributeInAnyGroupOrResource(sess, new ArrayList<>(), Collections.singletonList(resource),
            attributeToCheck, attributeToCheck)).thenReturn(true);
    when(sess.getPerunBl().getModulesUtilsBl()
        .haveTheSameAttributeWithTheSameNamespace(sess, resource, attributeToCheck)).thenReturn(2);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}
