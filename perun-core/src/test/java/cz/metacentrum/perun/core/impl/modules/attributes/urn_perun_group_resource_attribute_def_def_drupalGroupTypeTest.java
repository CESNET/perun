package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_resource_attribute_def_def_drupalGroupTypeTest {

  private urn_perun_group_resource_attribute_def_def_drupalGroupType classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_drupalGroupType();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("public");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");

    attributeToCheck.setValue("public");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);

    attributeToCheck.setValue("private");
    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("bad_value");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }
}
