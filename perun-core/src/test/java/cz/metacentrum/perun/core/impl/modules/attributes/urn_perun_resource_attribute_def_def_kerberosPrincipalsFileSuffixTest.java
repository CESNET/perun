package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_resource_attribute_def_def_kerberosPrincipalsFileSuffixTest {

  private urn_perun_resource_attribute_def_def_kerberosPrincipalsFileSuffix classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_kerberosPrincipalsFileSuffix();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("correct_example");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongValue() throws Exception {
    System.out.println("testSyntaxWithWrongValue()");
    attributeToCheck.setValue("bad example");

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }
}
