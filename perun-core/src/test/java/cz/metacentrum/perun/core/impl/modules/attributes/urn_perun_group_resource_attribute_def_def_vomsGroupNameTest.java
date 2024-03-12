package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_resource_attribute_def_def_vomsGroupNameTest {

  private urn_perun_group_resource_attribute_def_def_vomsGroupName classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_vomsGroupName();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("5");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");

    attributeToCheck.setValue("<0");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);

    attributeToCheck.setValue(">0");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);

    attributeToCheck.setValue("&0");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);

    attributeToCheck.setValue("=0");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }
}
