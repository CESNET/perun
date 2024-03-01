package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
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

public class urn_perun_user_attribute_def_virt_mfaStatusTest {
  private static urn_perun_user_attribute_def_virt_mfaStatus classInstance;
  private static Attribute attributeToCheck;
  private static User user;
  private static PerunSessionImpl sess;

  @Test
  public void getFullEnforce() {
    System.out.println("getFullEnforce()");
    attributeToCheck.setValue("{\"all\":true}");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attribute.setFriendlyName("mfaStatus:mu");

    assertEquals("ENFORCED_ALL", classInstance.getAttributeValue(sess, user, attribute).getValue());
  }

  @Test
  public void getNoEnforce() {
    System.out.println("getNoEnforce()");
    attributeToCheck.setValue(null);

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attribute.setFriendlyName("mfaStatus:mu");

    assertEquals("", classInstance.getAttributeValue(sess, user, attribute).getValue());
  }

  @Test
  public void getPartialEnforce() {
    System.out.println("getPartialEnforce()");
    attributeToCheck.setValue("{\"not_all\":true}");

    Attribute attribute = new Attribute();
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attribute.setFriendlyName("mfaStatus:mu");

    assertEquals("ENFORCED_PARTIALLY", classInstance.getAttributeValue(sess, user, attribute).getValue());
  }

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_mfaStatus();
    sess = mock(PerunSessionImpl.class);

    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("mfaEnforceSettings:mu");

    String mfaEnforceMU = AttributesManager.NS_USER_ATTR_DEF + ":mfaEnforceSettings:mu";
    when(sess.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(sess.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, mfaEnforceMU)).thenReturn(
        attributeToCheck);
  }
}
