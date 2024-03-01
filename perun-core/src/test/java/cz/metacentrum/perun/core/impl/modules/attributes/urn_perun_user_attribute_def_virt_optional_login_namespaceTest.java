package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_virt_optional_login_namespaceTest {

  private static urn_perun_user_attribute_def_virt_optional_login_namespace classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_optional_login_namespace();
    session = mock(PerunSessionImpl.class);

    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("login-namespace:einfra");
    attributeToCheck.setValue("test");

    String namespace =
        AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + attributeToCheck.getFriendlyNameParameter();
    when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(session.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, namespace)).thenReturn(
        attributeToCheck);
  }

  @Test
  public void testCheckWithAttribute() {
    System.out.println("testCheckWithAttribute()");
    attributeToCheck.setValue("test-value");
    attributeToCheck.setValueCreatedBy("testCreator");

    Attribute attr = new Attribute();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:einfra");

    assertEquals(attributeToCheck.getValue(), classInstance.getAttributeValue(session, user, attr).getValue());
    assertEquals(attributeToCheck.getValueCreatedBy(),
        classInstance.getAttributeValue(session, user, attr).getValueCreatedBy());
  }

  @Test
  public void testCheckWithNull() {
    System.out.println("testCheckWithNull()");
    attributeToCheck.setValue(null);

    Attribute attr = new Attribute();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:einfra");

    assertNull(classInstance.getAttributeValue(session, user, attr).getValue());
  }
}
