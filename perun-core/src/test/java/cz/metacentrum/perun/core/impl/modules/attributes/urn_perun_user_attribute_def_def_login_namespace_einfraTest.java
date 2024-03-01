package cz.metacentrum.perun.core.impl.modules.attributes;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.pwdmgr.EinfraPasswordManagerModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class urn_perun_user_attribute_def_def_login_namespace_einfraTest {

  private static final User user = new User(1, "User", "1", "", "", "");
  private static final User user2 = new User(2, "User", "2", "", "", "");
  private static final User user3 = new User(3, "User", "3", "", "", "");
  private static final List<User> users = new ArrayList<>();
  private static final CoreConfig mockedCoreConfig = mock(CoreConfig.class);
  private static urn_perun_user_attribute_def_def_login_namespace_einfra classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;
  private static CoreConfig originalCoreConfig;

  static {
    users.add(user);
    users.add(user2);
    users.add(user3);
  }

  @BeforeClass
  public static void setUpCoreConfig() {
    originalCoreConfig = BeansUtils.getCoreConfig();
    BeansUtils.setConfig(mockedCoreConfig);
    when(mockedCoreConfig.getGeneratedLoginNamespaces()).thenReturn(emptyList());
  }

  @AfterClass
  public static void resetCoreConfig() {
    BeansUtils.setConfig(originalCoreConfig);
  }

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_login_namespace_einfra();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("login-namespace:einfra");
    attributeToCheck.setValue("test");

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
    PasswordManagerModule module = mock(EinfraPasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl().getPasswordManagerModule(session, "einfra")).thenReturn(module);

    ModulesUtilsBl modulesUtilsBlSpy = spy(mock(ModulesUtilsBl.class));
    when(session.getPerunBl().getModulesUtilsBl()).thenReturn(modulesUtilsBlSpy);

    Mockito.doThrow(InvalidLoginException.class).when(modulesUtilsBlSpy)
        .checkLoginNamespaceRegex(eq("einfra"), ArgumentMatchers.matches("(?!^[a-z][a-z0-9_-]{1,14}$)"),
            // negated einfra check
            ArgumentMatchers.any(Pattern.class));
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testAlreadyReservedLogin() throws Exception {
    System.out.println("testAlreadyReservedLogin()");
    UsersManagerBl usersManager = mock(UsersManagerBl.class);
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManager);
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    when(usersManager.getUsersByAttribute(session, attributeToCheck, true)).thenReturn(tmp);
    attributeToCheck.setValue(attributeToCheck.getValue().toString().toUpperCase());
    doThrow(new AlreadyReservedLoginException("")).when(usersManager)
        .checkReservedLogins(session, "einfra", attributeToCheck.valueAsString(), true);
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsAlreadyExistIgnoresCase() throws Exception {
    System.out.println("testCheckAttributeSemanticsAlreadyExistIgnoresCase()");
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    tmp.add(user2);
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true)).thenReturn(tmp);
    attributeToCheck.setValue(attributeToCheck.getValue().toString().toUpperCase());
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsCorrectValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrectValue()");
    List<User> tmp = new ArrayList<>();
    tmp.add(user);
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true)).thenReturn(tmp);
    attributeToCheck.setValue(attributeToCheck.getValue().toString().toUpperCase());
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
    verify(session.getPerunBl().getUsersManagerBl()).getUsersByAttribute(session, attributeToCheck, true);
    verify(session.getPerunBl().getUsersManagerBl()).checkReservedLogins(session,
        attributeToCheck.getFriendlyNameParameter(), attributeToCheck.valueAsString(), true);
  }

  @Test(expected = ConsistencyErrorException.class)
  public void testCheckAttributeSemanticsDuplicateDetected() throws Exception {
    System.out.println("testCheckAttributeSemanticsDuplicateDetected()");
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true)).thenReturn(
        new ArrayList<>(users));
    attributeToCheck.setValue(attributeToCheck.getValue().toString().toUpperCase());
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullAttribute()");
    attributeToCheck.setValue(null);
    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValue()");
    String value = "tAšřmksdů";
    attributeToCheck.setValue(value);

    var moduleMock = mock(PasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl().getPasswordManagerModule(eq(session), eq("einfra"))).thenReturn(
        moduleMock);
    doThrow(new InvalidLoginException("")).when(moduleMock).checkLoginFormat(eq(session), eq(value));
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
