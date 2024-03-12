package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_virt_anonymizedTest {

  private static urn_perun_user_attribute_def_virt_anonymized classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_anonymized();
    session = mock(PerunSessionImpl.class);
    user = mock(User.class);

    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attributeToCheck.setFriendlyName("anonymized");
    attributeToCheck.setValue(true);

    String attrName = AttributesManager.NS_USER_ATTR_VIRT + ":anonymized";
    when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(mock(UsersManagerBl.class));
    when(session.getPerunBl().getUsersManagerBl().isUserAnonymized(session, user)).thenReturn(
        (boolean) attributeToCheck.getValue());
    when(session.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));
  }

  @Test
  public void testCheckWithAttribute() {
    System.out.println("testCheckWithAttribute()");

    Attribute attr = new Attribute();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("anonymized");

    assertEquals(attributeToCheck.getValue(), classInstance.getAttributeValue(session, user, attr).getValue());
  }
}
