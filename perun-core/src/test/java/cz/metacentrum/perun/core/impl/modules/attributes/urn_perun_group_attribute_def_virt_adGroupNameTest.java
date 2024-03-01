package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_virt_adGroupNameTest {

  private static final String defAdGroupNameAttributeName = AttributesManager.NS_GROUP_ATTR_DEF + ":adGroupName";

  private final urn_perun_group_attribute_def_virt_adGroupName classInstance =
      new urn_perun_group_attribute_def_virt_adGroupName();
  private final AttributeDefinition adDGroupNameDefinition = classInstance.getAttributeDefinition();

  private final Group groupA = setUpGroup(1, null, "groupA", "A");
  private final Group groupB = setUpGroup(2, 1, "groupB", "B");
  private final Group groupC = setUpGroup(3, 2, "groupC", "C");

  private final Attribute attributeA = new Attribute();
  private final Attribute attributeB = new Attribute();
  private final Attribute attributeC = new Attribute();

  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    attributeA.setValue("A");
    attributeB.setValue("B");
    attributeC.setValue("C");

    //prepare mocks
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    AttributesManagerBl am = mock(AttributesManagerBl.class);
    GroupsManagerBl gm = mock(GroupsManagerBl.class);

    when(sess.getPerunBl())
        .thenReturn(perunBl);
    when(perunBl.getAttributesManagerBl())
        .thenReturn(am);
    when(perunBl.getGroupsManagerBl())
        .thenReturn(gm);

    when(gm.getParentGroup(sess, groupB))
        .thenReturn(groupA);
    when(gm.getParentGroup(sess, groupC))
        .thenReturn(groupB);

    when(am.getAttribute(sess, groupA, defAdGroupNameAttributeName))
        .thenReturn(attributeA);
    when(am.getAttribute(sess, groupB, defAdGroupNameAttributeName))
        .thenReturn(attributeB);
    when(am.getAttribute(sess, groupC, defAdGroupNameAttributeName))
        .thenReturn(attributeC);
  }

  @Test
  public void testCorrectGetAttributeValue() {
    System.out.println("testCorrectGetAttributeValue()");

    Attribute result = classInstance.getAttributeValue(sess, groupC, adDGroupNameDefinition);
    Assert.assertEquals("A-B-C", result.getValue());
  }

  @Test
  public void testIncorrectGetAttributeValue() {
    System.out.println("testIncorrectGetAttributeValue()");

    attributeB.setValue(null);
    Attribute result = classInstance.getAttributeValue(sess, groupC, adDGroupNameDefinition);
    Assert.assertNull(result.getValue());
  }

  private Group setUpGroup(int id, Integer parentGroupId, String name, String description) {
    Group group = new Group(name, description);
    group.setId(id);
    group.setParentGroupId(parentGroupId);
    return group;
  }
}
