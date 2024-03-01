package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_resource_attribute_def_def_sshkeysTargetUserTest {

  private static PerunSessionImpl session;
  private static urn_perun_resource_attribute_def_def_sshkeysTargetUser classInstance;
  private static Resource resource;

  @Before
  public void setUp() {
    classInstance = new urn_perun_resource_attribute_def_def_sshkeysTargetUser();
    resource = new Resource();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
  }

  @Test
  public void testCheckAttributeSemantics() throws Exception {
    System.out.println("testCheckAttributeSemantics()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("Jan_Nepomucky");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

    attributeToCheck.setValue(".John_Dale.");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

    attributeToCheck.setValue("_Adele-Frank");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullValue()");
    Attribute attributeToCheck = new Attribute();

    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntax() throws Exception {
    System.out.println("testCheckAttributeSyntax()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("Jan_Nepomucky");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

    attributeToCheck.setValue(".John_Dale.");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);

    attributeToCheck.setValue("_Adele-Frank");
    classInstance.checkAttributeSemantics(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueDiacritic() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueDiacritic()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("Jan_Veselý");
    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueHyphen() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueHyphen()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("-Adam");
    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueWhitespace() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueWhitespace()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("Elena Fuente");
    classInstance.checkAttributeSyntax(session, resource, attributeToCheck);
  }
}
