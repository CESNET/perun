package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_isCesnetEligibleLastSeenTest {
  private urn_perun_user_attribute_def_def_isCesnetEligibleLastSeen classInstance;
  private Attribute attributeToCheck;
  private User user;
  private PerunSessionImpl session;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_isCesnetEligibleLastSeen();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
    user = new User();
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");

    attributeToCheck.setValue("2019-06-17 17:18:28");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntaxCorrectWithMilliseconds() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrectWithMilliseconds()");

    attributeToCheck.setValue("2019-06-17 17:18:28.22");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrect()");

    attributeToCheck.setValue("incorrect");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectMinutes() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectMinutes()");

    attributeToCheck.setValue("2019-12-17 17:68:28");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectMonths() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectMonths()");

    attributeToCheck.setValue("2019-13-17 17:18:28");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntaxNull() throws Exception {
    System.out.println("testCheckAttributeSyntaxNull()");

    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
