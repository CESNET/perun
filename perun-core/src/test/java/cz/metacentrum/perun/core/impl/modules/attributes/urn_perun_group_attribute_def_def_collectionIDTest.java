package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_collectionIDTest {

  private urn_perun_group_attribute_def_def_collectionID classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_collectionID();
    sess = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckNullValue() throws Exception {
    System.out.println("testCheckNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("0");

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }
}
