package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_virt_unscopedLogin_namespace_einfraid_persistent_shadowTest {
  private static final User user = new User(1, "User", "1", "", "", "");
  private static urn_perun_user_attribute_def_virt_unscopedLogin_namespace_einfraid_persistent_shadow classInstance;
  private static PerunSessionImpl session;
  private static AttributeDefinition attributeDefinitionToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_unscopedLogin_namespace_einfraid_persistent_shadow();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeDefinitionToCheck = new AttributeDefinition();
    attributeDefinitionToCheck.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attributeDefinitionToCheck.setFriendlyName("unscopedLogin-namespace:einfraid-persistent-shadow");
  }

  @Test
  public void testGetValueSourceWithMultipleScopes() throws Exception {
    System.out.println("testGetValueSourceWithMultipleScopes()");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attribute.setFriendlyName("login-namespace:einfraid-persistent-shadow");
    attribute.setType("def");
    attribute.setValue("test@test@test@scope");
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(User.class), any(String.class))).thenReturn(attribute);

    Attribute output = classInstance.getAttributeValue(session, user, attributeDefinitionToCheck);
    assertEquals("test@test@test", output.getValue());
  }

  @Test
  public void testGetValueSourceWithScope() throws Exception {
    System.out.println("testGetValueSourceWithScope()");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attribute.setFriendlyName("login-namespace:einfraid-persistent-shadow");
    attribute.setType("def");
    attribute.setValue("test@scope");
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(User.class), any(String.class))).thenReturn(attribute);

    Attribute output = classInstance.getAttributeValue(session, user, attributeDefinitionToCheck);
    assertEquals("test", output.getValue());
  }

  @Test
  public void testGetValueSourceWithoutScope() throws Exception {
    System.out.println("testGetValueSourceWithoutScope()");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attribute.setFriendlyName("login-namespace:einfraid-persistent-shadow");
    attribute.setType("def");
    attribute.setValue("test");
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(User.class), any(String.class))).thenReturn(attribute);

    Attribute output = classInstance.getAttributeValue(session, user, attributeDefinitionToCheck);
    assertEquals("test", output.getValue());
  }

  @Test
  public void testGetValueSourceWithoutValue() throws Exception {
    System.out.println("testGetValueSourceWithoutValue()");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attribute.setFriendlyName("login-namespace:einfraid-persistent-shadow");
    attribute.setType("def");
    attribute.setValue(null);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(User.class), any(String.class))).thenReturn(attribute);

    Attribute output = classInstance.getAttributeValue(session, user, attributeDefinitionToCheck);
    assertNull(output.getValue());
  }
}
