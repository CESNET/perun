package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizationsTest {
  private final static Logger log =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizationsTest.class);

  private final urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizations classInstance =
      new urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizations();
  private final AttributeDefinition sponsoredMembershipInOrganizationsAttrDef = classInstance.getAttributeDefinition();
  private final String organizationIdentifierAttrFriendlyName = "sponsorOrganizationIdentifier";
  private final String organizationIdentifierAttrName =
      AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationIdentifierAttrFriendlyName;

  private final User user = new User(1, "Joe", "Doe", "W.", "", "");
  private final Member member1 = new Member(1, 1, 1, Status.VALID);
  private final Member member2 = new Member(2, 1, 2, Status.VALID);
  private final Group group1 = new Group("group1", "group1");
  private final Group group2 = new Group("group2", "group2");

  private final String value1 = "value1";
  private final String value2 = "value2";

  private final Attribute groupAttr1 =
      setUpGroupAttribute(1, organizationIdentifierAttrFriendlyName, String.class.getName(), value1);
  private final Attribute groupAttr2 =
      setUpGroupAttribute(1, organizationIdentifierAttrFriendlyName, String.class.getName(), value2);

  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    sponsoredMembershipInOrganizationsAttrDef.setId(100);
    group1.setId(1);
    group2.setId(2);

    //prepare mocks
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    when(sess.getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member1))
        .thenReturn(Arrays.asList(group1));
    when(sess.getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member2))
        .thenReturn(Arrays.asList(group2));

    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group1, organizationIdentifierAttrName))
        .thenReturn(groupAttr1);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group2, organizationIdentifierAttrName))
        .thenReturn(groupAttr2);

    when(sess.getPerunBl().getMembersManagerBl().getMembersByUserWithStatus(sess, user, Status.VALID))
        .thenReturn(Arrays.asList(member1, member2));
  }

  private Attribute setUpGroupAttribute(int id, String friendlyName, String type, Object value) {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setId(id);
    attrDef.setFriendlyName(friendlyName);
    attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attrDef.setType(type);
    Attribute attr = new Attribute(attrDef);
    attr.setValue(value);
    return attr;
  }

  @Test
  public void getAttributeValue() {
    ArrayList<String> attributeValue =
        classInstance.getAttributeValue(sess, user, sponsoredMembershipInOrganizationsAttrDef).valueAsList();

    assertNotNull(attributeValue);
    assertEquals(2, attributeValue.size());
    assertTrue(attributeValue.contains(value1));
    assertTrue(attributeValue.contains(value2));
  }
}
