package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_adGroupNameTest extends AbstractPerunIntegrationTest {

  private static final String A_R_D_AD_RESOURCE_REPRESENTATION =
      AttributesManager.NS_RESOURCE_ATTR_DEF + ":adResourceRepresentation";
  private static final String A_G_D_AD_GROUP_NAME = AttributesManager.NS_GROUP_ATTR_DEF + ":adGroupName";
  private PerunSessionImpl sess;
  private urn_perun_group_attribute_def_def_adGroupName classInstance;
  private Attribute attributeToCheck;
  private Attribute requiredAttribute;
  private Attribute requiredAttribute2;
  private Group group;
  private Group parentGroup;
  private Group subGroup;
  private Resource resource;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_adGroupName();
    attributeToCheck = new Attribute();
    requiredAttribute = new Attribute();
    requiredAttribute2 = new Attribute();
    parentGroup = new Group("parentGroupTest", "parentGroupTest");
    group = new Group(0, "groupTest", "groupTest");
    subGroup = new Group(0, "subgroupTest", "subgroupTest");
    resource = new Resource();

    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    List<Group> subgroups = new ArrayList<>();
    subgroups.add(group);
    subgroups.add(subGroup);

    when(sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group)).thenReturn(List.of(resource));
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource, A_R_D_AD_RESOURCE_REPRESENTATION)).thenReturn(requiredAttribute);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, subGroup, A_G_D_AD_GROUP_NAME)).thenReturn(
        requiredAttribute2);
    when(sess.getPerunBl().getGroupsManagerBl().getSubGroups(sess, parentGroup)).thenReturn(subgroups);
    when(sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, group)).thenReturn(parentGroup);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongSyntax() throws Exception {
    System.out.println("testWrongSyntax()");
    attributeToCheck.setValue("bad@value");

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongSyntaxWithSpaceAtStart() throws Exception {
    System.out.println("testWrongSyntaxWithSpaceAtStart()");
    attributeToCheck.setValue(" badValue");

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongSyntaxWithSpaceAtEnd() throws Exception {
    System.out.println("testWrongSyntaxWithSpaceAtEnd()");
    attributeToCheck.setValue("badValue ");

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("correctValue");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test
  public void testCorrectSyntaxWithDash() {
    System.out.println("testCorrectSyntaxWithDash()");
    attributeToCheck.setValue("-correct-Value-with-Dash-");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test
  public void testCorrectSyntaxWithUnderscore() {
    System.out.println("testCorrectSyntaxWithUnderscore()");
    attributeToCheck.setValue("_correct_Value_with_Dash_");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test
  public void testCorrectSyntaxWithSpace() {
    System.out.println("testCorrectSyntaxWithSpace()");
    attributeToCheck.setValue("correct Value with Dash");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsNameAlreadyTaken() throws Exception {
    System.out.println("testSemanticsNameAlreadyTaken()");
    attributeToCheck.setValue("name");
    requiredAttribute.setValue("tree");
    requiredAttribute2.setValue("name");

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test
  public void testSemanticsResourceAttrNotTree() throws Exception {
    System.out.println("testSemanticsResourceAttrNotTree()");
    attributeToCheck.setValue("name");
    requiredAttribute.setValue("");
    requiredAttribute2.setValue("name");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSemantics(sess, group, attributeToCheck));
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("name");
    requiredAttribute.setValue("tree");
    requiredAttribute2.setValue("name2");

    assertThatNoException().isThrownBy(
        () -> classInstance.checkAttributeSemantics(sess, group, attributeToCheck));
  }
}
