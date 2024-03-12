package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class DefaultBlockedLoginCheckerTest extends AbstractPerunIntegrationTest {
  private static final String CLASS_NAME = "DefaultBlockedLoginChecker.";
  private static final String LOGIN = "testLogin";
  DefaultBlockedLoginChecker defaultBlockedLoginChecker;
  private User user;
  private Attribute attr;

  @Test
  public void defaultBlockedLoginAlreadyUsed() {
    System.out.println(CLASS_NAME + "defaultBlockedLoginIsAlreadyUsed");

    List<String> originalAdmins = BeansUtils.getCoreConfig().getAdmins();
    try {
      // configure admins to contain one login - testLogin
      BeansUtils.getCoreConfig().setAdmins(Collections.singletonList(LOGIN));
      assertThrows(InternalErrorException.class, () -> defaultBlockedLoginChecker.checkDefaultBlockedLogins());
    } finally {
      // set admins back to the original admins
      BeansUtils.getCoreConfig().setAdmins(originalAdmins);
    }
  }

  @Test
  public void defaultBlockedLoginAreNotUsed() {
    System.out.println(CLASS_NAME + "defaultBlockedLoginAreNotUsed");

    CoreConfig originalConfig = BeansUtils.getCoreConfig();
    try {
      // set new core config
      CoreConfig cfNew = new CoreConfig();

      cfNew.setAdmins(new ArrayList<>());
      cfNew.setEnginePrincipals(new ArrayList<>());
      cfNew.setNotificationPrincipals(new ArrayList<>());
      cfNew.setDontLookupUsers(new HashSet<>());
      cfNew.setRegistrarPrincipals(new ArrayList<>());
      cfNew.setRpcPrincipal(null);
      cfNew.setInstanceId("test");

      BeansUtils.setConfig(cfNew);

      defaultBlockedLoginChecker.checkDefaultBlockedLogins();
    } finally {
      // set core config back to original
      BeansUtils.setConfig(originalConfig);
    }
  }

  private void setLoginNamespaceAttribute()
      throws AttributeDefinitionExistsException, WrongAttributeAssignmentException,
      WrongReferenceAttributeValueException, WrongAttributeValueException {
    attr = new Attribute();
    attr.setNamespace("urn:perun:user:attribute-def:def");
    attr.setFriendlyName("login-namespace:META-login");
    attr.setType(String.class.getName());
    attr.setValue(LOGIN);

    assertNotNull("unable to create login namespace attribute",
        perun.getAttributesManagerBl().createAttribute(sess, attr));

    perun.getAttributesManagerBl().setAttribute(sess, user, attr);
  }

  @Before
  public void setUp() throws Exception {
    defaultBlockedLoginChecker = new DefaultBlockedLoginChecker(perun);

    setUser();
    setLoginNamespaceAttribute();
  }

  private void setUser() {
    user = new User();
    user.setFirstName("Joe");
    user.setLastName("Doe");
    user = perun.getUsersManagerBl().createUser(sess, user);
    assertNotNull(user);
  }
}
