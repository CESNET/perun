package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_googleGroupName_namespaceTest {

  private urn_perun_group_attribute_def_def_googleGroupName_namespace classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_googleGroupName_namespace();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("my_example");

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongSyntax() throws Exception {
    System.out.println("testWrongSyntax()");
    attributeToCheck.setValue("???");

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }
}
