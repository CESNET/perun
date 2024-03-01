package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_virt_userEligibilitiesTest {

  private static urn_perun_user_attribute_def_virt_userEligibilities classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute uesAttribute1;
  private static Attribute uesAttribute2;
  private static Attribute userEligibilitiesAttribute;
  private static AttributeDefinition userEligibilitiesAttributeDefinition;

  @Test
  public void getEligibilitiesAttributeValue() throws Exception {
    System.out.println("getEligibilitiesAttributeValue()");

    Attribute attr = classInstance.getAttributeValue(session, user, userEligibilitiesAttributeDefinition);
    Map<String, String> values = (LinkedHashMap<String, String>) attr.getValue();

    assertEquals(2, values.size());
    assertTrue(values.keySet().containsAll(List.of("cesnet", "mu")));

    for (String key : values.keySet()) {
      String value = values.get(key);
      if (key.equals("cesnet")) {
        assertEquals("9999999", value);
      } else {
        assertEquals("1000000", value);
      }
    }
  }

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_userEligibilities();
    session = mock(PerunSessionImpl.class);
    user = new User();
    UserExtSource extSource =
        new UserExtSource(new ExtSource("test ext source", ExtSourcesManager.EXTSOURCE_LDAP), "test-login");
    UserExtSource extSource2 =
        new UserExtSource(new ExtSource("another test ext source", ExtSourcesManager.EXTSOURCE_LDAP), "test-login2");

    userEligibilitiesAttributeDefinition = new AttributeDefinition();
    userEligibilitiesAttributeDefinition.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    userEligibilitiesAttributeDefinition.setFriendlyName("userEligibilities");
    userEligibilitiesAttributeDefinition.setDisplayName("user eligibilities");
    userEligibilitiesAttributeDefinition.setType(HashMap.class.getName());
    userEligibilitiesAttributeDefinition.setDescription(
        "Virtual attribute, which collects all eligibilities user ext source attributes " +
        "with keys and values (map). Only the highest value is selected for each key.");
    userEligibilitiesAttribute = new Attribute(userEligibilitiesAttributeDefinition);

    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setFriendlyName("eligibilities");
    attrDef.setType(AttributesManager.NS_UES_ATTR_DEF);
    attrDef.setDescription("eligibilities test attribute");
    uesAttribute1 = new Attribute(attrDef);
    uesAttribute2 = new Attribute(attrDef);

    Map<String, String> attrValue = new LinkedHashMap<>();
    attrValue.put("cesnet", "8150000");
    uesAttribute1.setValue(attrValue);

    attrValue = new LinkedHashMap<>();
    attrValue.put("cesnet", "9999999");
    attrValue.put("mu", "1000000");
    uesAttribute2.setValue(attrValue);

    when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(mock(UsersManagerBl.class));
    when(session.getPerunBl().getAttributesManagerBl()).thenReturn(mock(AttributesManagerBl.class));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        List.of(extSource, extSource2));
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, user, AttributesManager.NS_USER_ATTR_VIRT + ":userEligibilities")).thenReturn(
        userEligibilitiesAttribute);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, extSource, AttributesManager.NS_UES_ATTR_DEF + ":eligibilities")).thenReturn(
        uesAttribute1);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, extSource2, AttributesManager.NS_UES_ATTR_DEF + ":eligibilities")).thenReturn(
        uesAttribute2);
  }
}
