package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_resource_attribute_def_def_projectDataLimitTest {

  private urn_perun_group_resource_attribute_def_def_projectDataLimit classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group();
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_resource_attribute_def_def_projectDataLimit();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    reqAttribute = new Attribute();

    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group,
        AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":projectDataQuota")).thenReturn(reqAttribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testWrongValue() throws Exception {
    System.out.println("testWrongValue()");
    attributeToCheck.setValue("0");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue("1T");

    classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsQuotaHigherThanLimit() throws Exception {
    System.out.println("testSemanticsQuotaHigherThanLimit()");
    attributeToCheck.setValue("22M");
    reqAttribute.setValue("10T");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }


  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue("22T");
    reqAttribute.setValue("10M");

    classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
  }
}
