package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_groupMembershipExpirationRulesTest {
  private urn_perun_group_attribute_def_def_groupMembershipExpirationRules classInstance;
  private Attribute attributeToCheck;
  private LinkedHashMap<String, String> rules;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private Attribute conflictAttribute;
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_groupMembershipExpirationRules();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    conflictAttribute = new Attribute(classInstance.getAttributeDefinition());

    rules = new LinkedHashMap<>();
    rules.put("period", "+1m");

    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
    when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);

    AttributesManagerBl attributesManagerBl;
    attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME)).thenReturn(conflictAttribute);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testConflictingAttributeSet() throws Exception {
    System.out.println("testConflictingAttributeSet()");

    attributeToCheck.setValue(rules);
    conflictAttribute.setValue("true");

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");

    attributeToCheck.setValue(rules);

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }
}
