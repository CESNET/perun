package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_attribute_def_def_lastExtensionTest {

  private static final String CLASS_NAME = "urn_perun_member_attribute_def_def_lastExtensionTest.";

  private static urn_perun_member_attribute_def_def_lastExtension classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_member_attribute_def_def_lastExtension();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
  }

  @Test
  public void testCheckAttributeSyntaxValidDate() throws Exception {
    System.out.println(CLASS_NAME + "testCheckAttributeSyntaxValidDate");
    attributeToCheck.setValue("2026-10-14");

    classInstance.checkAttributeSyntax(session, new Member(), attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntaxNullValue() throws Exception {
    System.out.println(CLASS_NAME + "testCheckAttributeSyntaxNullValue");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(session, new Member(), attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongCharsInDate() throws Exception {
    System.out.println(CLASS_NAME + "testCheckAttributeSyntaxWrongCharsInDate");
    attributeToCheck.setValue("2026-13-45");

    classInstance.checkAttributeSyntax(session, new Member(), attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxNotADate() throws Exception {
    System.out.println(CLASS_NAME + "testCheckAttributeSyntaxNotADate");
    attributeToCheck.setValue("not-a-date");

    classInstance.checkAttributeSyntax(session, new Member(), attributeToCheck);
  }

  @Test
  public void testAttributeDefinition() throws Exception {
    System.out.println(CLASS_NAME + "testAttributeDefinition");
    AttributeDefinition definition = classInstance.getAttributeDefinition();

    assertEquals(AttributesManager.NS_MEMBER_ATTR_DEF, definition.getNamespace());
    assertEquals("lastExtension", definition.getFriendlyName());
    assertEquals(String.class.getName(), definition.getType());
  }
}
