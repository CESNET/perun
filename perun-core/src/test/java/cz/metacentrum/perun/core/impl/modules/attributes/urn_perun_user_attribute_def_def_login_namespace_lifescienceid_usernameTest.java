package cz.metacentrum.perun.core.impl.modules.attributes;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.pwdmgr.LifescienceidusernamePasswordManagerModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_usernameTest {
  private static final User user = new User(1, "User", "1", "", "", "");
  private static final User user2 = new User(2, "User", "2", "", "", "");
  private static final CoreConfig mockedCoreConfig = mock(CoreConfig.class);
  private static final String lifescienceidUsername = "login-namespace:lifescienceid-username";
  private static final String elixirUsername = "login-namespace:elixir";
  private static final String bbmriUsername = "login-namespace:bbmri";
  private static urn_perun_user_attribute_def_def_login_namespace_lifescienceid_username classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;
  private static CoreConfig originalCoreConfig;

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
  public void setUp() {
    classInstance = new urn_perun_user_attribute_def_def_login_namespace_lifescienceid_username();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName(lifescienceidUsername);
    attributeToCheck.setValue("test");

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(session.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
    PasswordManagerModule module = mock(LifescienceidusernamePasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl()
             .getPasswordManagerModule(session, "lifescienceid-username")).thenReturn(module);
  }

  @Test
  public void testCheckAttributeSemanticsCorrectValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrectValue()");

    AttributeDefinition lifescienceAttrDefinition = new AttributeDefinition();
    lifescienceAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    lifescienceAttrDefinition.setFriendlyName(lifescienceidUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, lifescienceAttrDefinition.getName())).thenReturn(
        lifescienceAttrDefinition);

    AttributeDefinition elixirAttrDefinition = new AttributeDefinition();
    elixirAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    elixirAttrDefinition.setFriendlyName(elixirUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, elixirAttrDefinition.getName())).thenReturn(elixirAttrDefinition);

    AttributeDefinition bbmriAttrDefinition = new AttributeDefinition();
    bbmriAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    bbmriAttrDefinition.setFriendlyName(bbmriUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, bbmriAttrDefinition.getName())).thenReturn(bbmriAttrDefinition);

    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true)).thenReturn(
        new ArrayList<>(List.of(user)));

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsDuplicateDetected() throws Exception {
    System.out.println("testCheckAttributeSemanticsDuplicateDetected()");
    when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(session, attributeToCheck, true)).thenReturn(
        new ArrayList<>(List.of(user, user2)));

    AttributeDefinition lifescienceAttrDefinition = new AttributeDefinition();
    lifescienceAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    lifescienceAttrDefinition.setFriendlyName(lifescienceidUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, lifescienceAttrDefinition.getName())).thenReturn(
        lifescienceAttrDefinition);

    AttributeDefinition elixirAttrDefinition = new AttributeDefinition();
    elixirAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    elixirAttrDefinition.setFriendlyName(elixirUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, elixirAttrDefinition.getName())).thenReturn(elixirAttrDefinition);

    AttributeDefinition bbmriAttrDefinition = new AttributeDefinition();
    bbmriAttrDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    bbmriAttrDefinition.setFriendlyName(bbmriUsername);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttributeDefinition(session, bbmriAttrDefinition.getName())).thenReturn(bbmriAttrDefinition);

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullAttribute()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxOnlyNumbers() throws Exception {
    System.out.println("testSyntaxOnlyNumbers()");
    attributeToCheck.setValue("1234");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testSyntaxStartWithLetter() throws Exception {
    System.out.println("testSyntaxStartWithLetter()");
    attributeToCheck.setValue("a111");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxStartWithNumber() throws Exception {
    System.out.println("testSyntaxStartWithNumber()");
    attributeToCheck.setValue("1aaa");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
