package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_virt_login_namespace_bbmriid_persistentTest {
  private static urn_perun_user_attribute_def_virt_login_namespace_bbmriid_persistent classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_login_namespace_bbmriid_persistent();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    user = new User();
    user.setId(123456);
    reqAttribute = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, "bbmriid",
      AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":disableIDGeneration")).thenReturn(reqAttribute);

    Attribute attributeToReturn = new Attribute();
    attributeToReturn.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToReturn.setFriendlyName("login-namespace:bbmriid-persistent-shadow");
    attributeToReturn.setType("def");

    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, user, urn_perun_user_attribute_def_virt_login_namespace_bbmriid_persistent.SHADOW))
        .thenReturn(attributeToReturn);

    Attribute attributeToFill = new Attribute();
    attributeToFill.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToFill.setFriendlyName("login-namespace:bbmriid-persistent-shadow");
    attributeToFill.setType("def");
    attributeToFill.setValue("879a224546cf11fe53863737de037d2d39640258@bbmriid");

    when(session.getPerunBl().getAttributesManagerBl()
        .fillAttribute(any(PerunSession.class), any(User.class), any(Attribute.class))).thenReturn(attributeToFill);
  }

  @Test
  public void getAttributeValue() {
    System.out.println("getAttributeValue()");

    assertNotNull(classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition()).getValue());
  }

  @Test
  public void getAttributeValueBlocked() {
    System.out.println("getAttributeValueBlocked()");
    when(session.getPerunBl().getModulesUtilsBl().isNamespaceIDGenerationDisabled(session, "bbmriid")).thenReturn(true);




    assertNull(classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition()).getValue());
  }
}
