package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_groupSynchronizationIntervalTest {
  private urn_perun_group_attribute_def_def_groupSynchronizationInterval classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private Attribute syncTimes;
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_groupSynchronizationInterval();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    syncTimes = new Attribute(classInstance.getAttributeDefinition());
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, group, GroupsManager.GROUP_SYNCHRO_TIMES_ATTRNAME)).thenReturn(syncTimes);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testMissingReqAttribute() throws Exception {
    System.out.println("testMissingReqAttribute()");
    syncTimes.setValue("true");
    attributeToCheck.setValue("value");

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("value");

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }
}
