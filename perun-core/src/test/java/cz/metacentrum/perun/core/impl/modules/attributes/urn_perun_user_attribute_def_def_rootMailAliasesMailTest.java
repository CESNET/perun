package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_rootMailAliasesMailTest {

  private static urn_perun_user_attribute_def_def_rootMailAliasesMail classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_user_attribute_def_def_rootMailAliasesMail();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    user = new User();
    attributeToCheck = new Attribute();
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValue()");
    attributeToCheck.setValue("bad@example");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCheckCorrectAttributeSyntax() throws Exception {
    System.out.println("testCheckCorrectAttributeSyntax()");
    attributeToCheck.setValue("my@example.com");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
