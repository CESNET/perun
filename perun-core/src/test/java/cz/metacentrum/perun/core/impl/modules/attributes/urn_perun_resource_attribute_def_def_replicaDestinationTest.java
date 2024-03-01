package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_replicaDestinationTest {

  private urn_perun_resource_attribute_def_def_replicaDestination classInstance;
  private Attribute attributeToCheck;
  private Resource resource = new Resource();
  private PerunSessionImpl sess;
  private ModulesUtilsBl modulesUtilsBl;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_resource_attribute_def_def_replicaDestination();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongValue() throws Exception {
    System.out.println("testSyntaxWithWrongValue()");
    attributeToCheck.setValue("bad_example");
    when(modulesUtilsBl.isFQDNValid(sess, attributeToCheck.valueAsString())).thenReturn(false);

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("example.my");
    when(modulesUtilsBl.isFQDNValid(sess, attributeToCheck.valueAsString())).thenReturn(true);

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("example.my");

    classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
  }
}