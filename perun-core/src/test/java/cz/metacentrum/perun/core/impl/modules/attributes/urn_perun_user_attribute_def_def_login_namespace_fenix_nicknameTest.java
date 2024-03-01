package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.pwdmgr.GenericPasswordManagerModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_login_namespace_fenix_nicknameTest {
  private static urn_perun_user_attribute_def_def_login_namespace_fenix_nickname classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_login_namespace_fenix_nickname();
    session = mock(PerunSessionImpl.class);
    user = new User();
    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("login-namespace:fenix-nickname");

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);

    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(perunBl.getUsersManagerBl()).thenReturn(usersManagerBl);

    PasswordManagerModule module = mock(GenericPasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl().getPasswordManagerModule(session, "fenix-nickname")).thenReturn(
        module);


  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValue()");
    String value = "too_long_to_be_namespace";
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    String value = "my_example";
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
