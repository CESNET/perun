package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.pwdmgr.AdminmetaPasswordManagerModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_login_namespace_admin_metaTest {

  private static final User user = new User(1, "User", "1", "", "", "");
  private static final User user2 = new User(2, "User", "2", "", "", "");
  private static final CoreConfig mockedCoreConfig = mock(CoreConfig.class);
  private static urn_perun_user_attribute_def_def_login_namespace_admin_meta classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;
  private static CoreConfig originalCoreConfig;
  private PasswordManagerModule module;

  @BeforeClass
  public static void setUpCoreConfig() {
    originalCoreConfig = BeansUtils.getCoreConfig();
    BeansUtils.setConfig(mockedCoreConfig);
    when(mockedCoreConfig.getGeneratedLoginNamespaces())
        .thenReturn(emptyList());
  }

  @AfterClass
  public static void resetCoreConfig() {
    BeansUtils.setConfig(originalCoreConfig);
  }

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_login_namespace_admin_meta();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("login-namespace:admin-meta");
    attributeToCheck.setValue("test");

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
    module = mock(AdminmetaPasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl().getPasswordManagerModule(session, "admin-meta")).thenReturn(module);
    ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCorrectSyntax()");
    String value = "my_example23";
    attributeToCheck.setValue(value);

    doNothing()
        .when(module)
        .checkLoginFormat(session, value);
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
    verify(module)
        .checkLoginFormat(session, value);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testIncorrectCharSyntax() throws Exception {
    System.out.println("testIncorrectCharSyntax()");
    String value = "my_example#3";
    attributeToCheck.setValue(value);

    doThrow(new InvalidLoginException(""))
        .when(module)
        .checkLoginFormat(session, value);
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testIncorrectLengthSyntax() throws Exception {
    System.out.println("testIncorrectLengthSyntax()");
    String value = "my_example222223";
    attributeToCheck.setValue(value);

    doThrow(new InvalidLoginException(""))
        .when(module)
        .checkLoginFormat(session, value);
    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullAttribute()");
    attributeToCheck.setValue(null);
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsCorrectValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrectValue()");
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true))
        .thenReturn(tmp);

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
    verify(session.getPerunBl().getUsersManagerBl())
        .getUsersByAttribute(session, attributeToCheck, true);
    verify(session.getPerunBl().getUsersManagerBl())
        .checkReservedLogins(session, attributeToCheck.getFriendlyNameParameter(), attributeToCheck.valueAsString(),
            true);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsAlreadyExistIgnoresCase() throws Exception {
    System.out.println("testCheckAttributeSemanticsAlreadyExistIgnoresCase()");
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    tmp.add(user2);
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true))
        .thenReturn(tmp);
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testAlreadyReservedLogin() throws Exception {
    System.out.println("testAlreadyReservedLogin()");
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true))
        .thenReturn(tmp);

    UsersManagerBl userManagerMock = session.getPerunBl().getUsersManagerBl();
    doThrow(new AlreadyReservedLoginException(""))
        .when(userManagerMock)
        .checkReservedLogins(session, "admin-meta", attributeToCheck.valueAsString(), true);
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }
}
