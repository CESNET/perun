package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_facility_attribute_def_def_defaultUnixGIDTest {
  private urn_perun_user_facility_attribute_def_def_defaultUnixGID classInstance;
  private Attribute attributeToCheck;
  private Attribute namespace;
  private Attribute unixGroupNamespace;
  private Attribute resourceGidAttribute;
  private Attribute groupGidAttribute;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_def_defaultUnixGID();
    attributeToCheck = new Attribute();
    attributeToCheck.setValue(5);
    namespace = new Attribute();
    namespace.setValue("namespace");
    unixGroupNamespace = new Attribute();
    unixGroupNamespace.setValue("unixGroupNamespace");
    resourceGidAttribute = new Attribute();
    resourceGidAttribute.setValue("resourceGidAttribute");
    groupGidAttribute = new Attribute();
    groupGidAttribute.setValue("groupGidAttribute");

    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).thenReturn(
        namespace);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace")).thenReturn(
        unixGroupNamespace);
    when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess,
        AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace.valueAsString())).thenReturn(
        resourceGidAttribute);
    when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess,
        AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace.valueAsString())).thenReturn(
        groupGidAttribute);

    ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
    when(sess.getPerunBl().getResourcesManagerBl()).thenReturn(resourcesManagerBl);

    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(sess.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);

    GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
    when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);
    when(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupGidAttribute)).thenReturn(
        new ArrayList<>());

    FacilitiesManagerBl facilitiesManagerBl = mock(FacilitiesManagerBl.class);
    when(sess.getPerunBl().getFacilitiesManagerBl()).thenReturn(facilitiesManagerBl);
    when(sess.getPerunBl().getFacilitiesManagerBl().getAllowedGroups(sess, facility, null, null)).thenReturn(
        new ArrayList<>());
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    Resource resource = new Resource();
    List<Resource> list = new ArrayList<>();
    list.add(resource);
    when(sess.getPerunBl().getResourcesManagerBl()
        .getResourcesByAttribute(any(PerunSessionImpl.class), any(Attribute.class))).thenReturn(list);
    when(sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user)).thenReturn(list);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNoGroupsForGivenGID() throws Exception {
    System.out.println("testSemanticsWithNoGroupsForGivenGID()");

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValueOfNamespaceAttribute() throws Exception {
    System.out.println("testSemanticsWithNullValueOfNamespaceAttribute()");
    namespace.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValueOfUnixGroupNameNamespace() throws Exception {
    System.out.println("testSemanticsWithNullValueOfUnixGroupNameNamespace()");
    unixGroupNamespace.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }
}
