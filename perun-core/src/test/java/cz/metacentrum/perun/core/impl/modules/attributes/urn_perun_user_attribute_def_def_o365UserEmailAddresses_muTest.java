package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_o365UserEmailAddresses_muTest {

  private static User user;
  private urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu classInstance;
  private PerunSessionImpl session;
  private Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu();
    //prepare mocks
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    user = new User();

    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    attributeToCheck.setId(100);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckEmailSyntax() throws Exception {
    System.out.println("testCheckEmailSyntax()");
    attributeToCheck.setValue(Lists.newArrayList("my@example.com", "a/-+"));
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckUcoEmailSyntax() throws Exception {
    System.out.println("testCheckUcoEmailSyntax()");
    attributeToCheck.setValue(Lists.newArrayList("my@example.com", "451570@muni.cz"));
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckDuplicates() throws Exception {
    System.out.println("testCheckDuplicates()");
    attributeToCheck.setValue(Lists.newArrayList("my@example.com", "aaa@bbb.com", "my@example.com"));
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    attributeToCheck.setValue(Lists.newArrayList("my@example.com"));
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
